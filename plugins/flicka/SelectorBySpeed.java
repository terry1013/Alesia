/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package plugins.flicka;

import gui.docking.*;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.function.*;

import javax.swing.*;

import org.apache.commons.math3.analysis.integration.*;
import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.*;
import org.apache.commons.math3.geometry.euclidean.twod.*;
import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class SelectorBySpeed {

	private static NumberFormat percentformat;
	private static DecimalFormat decimalFormat;
	private int race;
	private Date date;
	private boolean writePdistribution;
	private boolean writeStatistics;

	int sampleSize;

	/**
	 * indicate the amount of data that is consider enought to take a decision. this number is relative to the list
	 * size. IE: 3 indicate that the list is rejected if number of ceros in list is > listSize/3
	 *
	 */
	int statThreshold;

	/**
	 * number of elements to be selected.
	 */
	int selRange;

	private int total, winner, place, exacta, trifectaC, trifecta = 0;
	private int[] distribution = new int[14];
	public SelectorBySpeed(int race, Date date) {
		this.race = race;
		this.date = date;
		this.sampleSize = 2;
		this.selRange = 3;
		this.statThreshold = 2;
		this.writePdistribution = true;
		this.writeStatistics = true;

		percentformat = NumberFormat.getPercentInstance();
		percentformat.setMinimumFractionDigits(4);
		percentformat.setMaximumFractionDigits(4);
		decimalFormat = new DecimalFormat();
		decimalFormat.setMinimumFractionDigits(4);
		decimalFormat.setMaximumFractionDigits(4);

	}

	public static void runSimulation(int race, Date date) {
		runSimulation(race, date, 4);
	}

	public static void runSimulation(int race, Date date, int hs) {
		SelectorBySpeed sel = new SelectorBySpeed(race, date);
		sel.writeStatistics = false;
		sel.clearTables();
		sel.select();
		DockingContainer.signalFreshgen(PDistributionList.class.getName());
	}

	public static void runSimulation(Record[] rcdList, int hs) {
		TTaskManager.executeTask(() -> {
			SelectorBySpeed sel = new SelectorBySpeed(0, null);
			sel.writePdistribution = false;
			sel.clearTables();
			int max = rcdList.length;
			ProgressMonitor pg = new ProgressMonitor(Alesia.mainFrame, "Running a Long Task", "", 0, max);
			pg.setMillisToPopup(250);
			int step = 0;
			for (Record rcd : rcdList) {
				sel.date = (Date) rcd.getFieldValue("redate");
				sel.race = (Integer) rcd.getFieldValue("rerace");
				pg.setProgress(++step);
				pg.setNote(sel.date + " " + sel.race + " HS: " + sel.sampleSize);
				sel.select();
			}
			sel.printStats();
			DockingContainer.signalFreshgen(Statistics.class.getName());
			pg.close();
		});
	}
	private static void log(String msg, Record rcd) {
		if (msg != null) {
			System.out.println(msg);
		}
		if (rcd != null) {
			String ms = "";
			for (int c = 0; c < rcd.getFieldCount(); c++) {
				ms += rcd.getFieldName(c) + ": " + rcd.getFieldValue(c) + "\t";
			}
			System.out.println(ms);
		}
	}

	/**
	 * evaluate the incoming list searchin if the elements has enought statistical data. this method:
	 * <ol>
	 * <li>Count the pdsamplesize field forall record in the argument list. and element is cosider with enought data if
	 * the sample size >= {@link #statThreshold} variable
	 * <li>Based on the previous count, the list consider valid only if there are valid records > (list size /
	 * {@link #statThreshold}). else , the list is cleaned and a log msg is printed
	 * 
	 * @param pdList - list of pdistribution's record to evaluate
	 */
	private void checkSampleSize(Vector<Record> pdList) {
		int valid = 0;
		int th = pdList.size() / statThreshold;

		// count the valid samples
		for (int i = 0; i < pdList.size(); i++) {
			Record r = pdList.elementAt(i);
			int ss = (Integer) r.getFieldValue("pdsamplesize");
			// valid = (ss >= statThreshold) ? valid + 1 : valid;
			if (ss >= statThreshold) {
				valid++;
			} else {
				// TEST: if a sample is not valid, remove from list. it is in case that, if the list is valid, take
				// desision based in know data, not taking into account invalid elements
				// RESULT: no improvement !!!
				// pdList.removeElementAt(i--);
			}
		}

		// enougth valid samples?
		if (valid < th) {
			log("Selection of elements rejected by lack of historical data. The list contain only " + valid
					+ " samples.", null);
			pdList.clear();
		}
	}

	/**
	 * clear pdistribuion and/or statistics tables according to {@link #writePdistribution} and {@link #writeStatistics}
	 * variables
	 */
	private void clearTables() {
		try {
			if (writePdistribution) {
				Connection con = ConnectionManager.getConnection("pdistribution");
				con.createStatement().execute("DELETE FROM pdistribution");
			}

			if (writeStatistics) {
				Connection con = ConnectionManager.getConnection("statistics");
				con.createStatement().execute("DELETE FROM statistics");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * this method return the aproximated (probabilistic) position of the horse at the given sensor expresed in body
	 * distance from 0 (actual in 1st position). This method is based on the asumption that the spreead observed at the
	 * end of the race has the same distribution across the all sensor but narrow. this method:
	 * <ol>
	 * <li>take the final position and the spread at the end of the race
	 * <li>compute the number of x-point = # of sensor + distance
	 * <li>copute the distance of the given horse at the given partial (sensor) as ..........
	 * </ol>
	 * 
	 * @param rcds - elements of the race
	 * @param pdValue - horse
	 * @param partial - sensor to calc
	 * @return distance to the first element
	 */
	private double getDistanceAt(Vector<Record> rcds, String pdValue, int partial) {
		Record grcd = rcds.elementAt(0);
		// double dif = (Double) grcd.getFieldValue("recps");
		int dist = (Integer) grcd.getFieldValue("redistance");
		int sensors = ((int) Math.ceil(dist / 400)) + 1;
		double mycps = 0.0;
		for (Record rcd : rcds) {
			if (rcd.getFieldValue("rehorse").equals(pdValue)) {
				mycps = (Double) rcd.getFieldValue("recps");
			}
		}
		double tmp = partial / sensors * mycps;
		return tmp;
	}

	/**
	 * this method compute a {@link PolynomialSplineFunction} that represent the speed curve for a specific element.
	 * <p>
	 * this method work under the asumption that the horse time of arrival (measured in body relative to the first
	 * positions) is the same on all sensor during the race. in this way, the horse position in a specific sensor is a
	 * fraction of the final sensor (finish line). in this way, we can estimate the aproximated speed of the horse at
	 * any point of the race. this curve is calculate following:
	 * <ol>
	 * <li>for the specific race, set the number of xcoordenates represent the sensor positions + the distance of the
	 * race
	 * <li>the y-coordenates represent the time at each sensor position
	 * <li>for each sensor position, compute the aproximation of the horse inside the cloud. this value is calculated
	 * taking a fraction of the final position.
	 * </ol>
	 * 
	 * @param date - race date
	 * @param race - race number
	 * @param pdValue - element to compute
	 * 
	 * @return performace curve
	 */
	private PolynomialSplineFunction getInterpolator(Record rcd, int todist) {
		// 1seg = 5cps
		double mycps = (Double) rcd.getFieldValue("recps") / 5;
		int dist = (Integer) rcd.getFieldValue("redistance");
		Vector<Integer> sens = Selector.getSensors(dist);
		double[] xval = new double[sens.size()];
		double[] yval = new double[sens.size()];

		// asumption: the horse reach 13.15m/s at 46m.
		// xval[0] = 46;
		// yval[0] = 13.1;

		for (int c = 1; c < sens.size(); c++) {
			xval[c] = sens.elementAt(c);
			// position inside the cloud at sensor c+1
			double dif = ((double) (c + 1)) / ((double) sens.size()) * mycps;
			// partial or final time
			double par = (sens.elementAt(c) != dist) ? (Double) rcd.getFieldValue("repartial" + c) : (Double) rcd
					.getFieldValue("reracetime");
			// stimated time
			yval[c] = par + dif;
			// par + dif = time. velocity = distance/time expressed in m/seg
			// yval[c] = xval[c] / (par + dif);
		}
		// append a last point if distance of this element is less that distance to compute
		int ns = sens.size() + 1;
		if (dist < todist) {
			double[] xtmp = new double[ns];
			double[] ytmp = new double[ns];
			System.arraycopy(xval, 0, xtmp, 0, xval.length);
			System.arraycopy(yval, 0, ytmp, 0, yval.length);
			// TODO: calc the spread derivative to extrapolate based on this race tendency
			xtmp[ns - 1] = xval[ns - 2] + (todist - dist);
			ytmp[ns - 1] = yval[ns - 2];
			xval = xtmp;
			yval = ytmp;
		}

		// LinearInterpolator inter = new LinearInterpolator();
		SplineInterpolator inter = new SplineInterpolator();
		PolynomialSplineFunction psf = inter.interpolate(xval, yval);

		String xv = "";
		String yv = "";
		// function value at m meters
		for (int m = 46; m < todist; m += 10) {
			xv += m + "\t";
			yv += decimalFormat.format(psf.value(m)) + "\t";
		}
		System.out.println(rcd.getFieldValue("rerace") + " (" + dist + " to " + todist + ") "
				+ rcd.getFieldValue("rehorse"));
		System.out.println(yv);

		return psf;
	}

	/**
	 * return the speed curbe for the specific element.
	 * 
	 * @param sampleS
	 * @param pdField
	 * @param pdValue
	 * @return
	 */
	private Record calcSpeedCurve(String pdValue, int todist) {
		DBAccess pddba = ConnectionManager.getAccessTo("pdistribution");
		Record pdMod = pddba.getModel();
		int cnt = 0;
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		// 290718: dont take into account simulations races
		String wc = "redate < '" + date + "' AND rehorse = '" + pdValue + "' AND rehorsegender != 'S'";
		Vector<Record> rcds = dba.search(wc, "redate DESC");
		pdMod.setFieldValue("pddate", date);
		pdMod.setFieldValue("pdrace", race);
		pdMod.setFieldValue("pdfield", "");
		pdMod.setFieldValue("pdvalue", pdValue);
		pdMod.setFieldValue("pdprediction", 0.0);
		pdMod.setFieldValue("pdsamplesize", 0);

		// for element without historical data, pdprediction = 0;
		if (rcds.size() == 0) {
			return pdMod;
		}

		Vector<PolynomialSplineFunction> psflist = new Vector<PolynomialSplineFunction>();
		for (Record rcd : rcds) {
			// dont compute for recps=-1
			double mycps = (Double) rcd.getFieldValue("recps");
			if (mycps > -1) {
				psflist.add(getInterpolator(rcd, todist));
				cnt++;
				// if the numbers of records are already reached end iteration.
				if (cnt == sampleSize) {
					break;
				}
			}
		}
		// integrate each function and calculate and select the average of it
		DescriptiveStatistics ds = new DescriptiveStatistics();
		SimpsonIntegrator si = new SimpsonIntegrator();
		for (PolynomialSplineFunction psf : psflist) {
			double ar = si.integrate(todist - 46, psf, 46, todist);
			System.out.println(ar);
			ds.addValue(ar);
		}
		Double me = ds.getMean();
		System.out.println("mean: " + me);
		pdMod.setFieldValue("pdprediction", me);

		// sample size
		pdMod.setFieldValue("pdsamplesize", cnt);
		return pdMod;
	}

	/**
	 * calculate the speed curve for all elements of the race passed as argument. this method return an list of records
	 * of pddistribution file with the pdprediction set to the computed value
	 * 
	 * @param rsList - list of elements belong to the race
	 * @return recors of pdistribution file
	 */
	private Vector<Record> calcSpeedCurve(Vector<Record> rsList) {
		// get the distance to compute
		int todist = (Integer) rsList.elementAt(0).getFieldValue("redistance");
		Vector<Record> rlist = new Vector<Record>();
		for (Record rcd : rsList) {
			Record r = calcSpeedCurve((String) rcd.getFieldValue("rehorse"), todist);
			// set missing fields
			r.setFieldValue("pdevent", rcd.getFieldValue("reend_pos"));
			r.setFieldValue("pdselrange", selRange);
			rlist.add(r);
		}
		return rlist;

	}

	private void printStats() {

		System.out.println("Total\t\t" + total);
		double totd = total;
		System.out.println("Winner      " + winner + "\t" + percentformat.format(winner / totd));
		System.out.println("Place       " + place + "\t" + percentformat.format(place / totd));
		System.out.println("Exacta      " + exacta + "\t" + percentformat.format(exacta / totd));
		System.out.println("Trifecta    " + trifecta + "\t" + percentformat.format(trifecta / totd));
		System.out.println("C. Trifecta " + trifectaC + "\t" + percentformat.format(trifectaC / totd));

		System.out.println("Distribution");
		for (int i = 0; i < distribution.length; i++) {
			System.out.println("from " + (i + 1) + " to 1:\t" + distribution[i] + "\t"
					+ percentformat.format(distribution[i] / totd));
		}
	}

	/**
	 * Predict the future outcome based on selected parameters. This method try to predict the outcome for the given
	 * date/race parameters based on horseSample, jockeySample and cumulative probability (upBound). The data is
	 * prepared following the steps:
	 * <ol>
	 * <li>retrive all the records involved in the race and date passed as argument
	 * <li>build the single PDF of horse and jockey taking horseSample numebers or records for horse and jockeySample
	 * number of records for jockey
	 * <li>sum the events H + Y (horse samples PDF + jockey sample PDF)
	 * <li>update the pdistribution and statistics tables.
	 * <p>
	 * the data in the result table are of 3 types: horse, jockey and union.
	 * 
	 */
	private void select() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String ud = sdf.format(date);

		log("Selecting elements for race " + race + " of date " + date, null);
		// selection of elements for prediction
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		Vector<Record> rcds = dba.search("rerace = " + race + " AND redate = '" + ud + "'", "redate DESC");

		// get the prediction
		Vector<Record> pdlstH = calcSpeedCurve(rcds);

		takeDecision(pdlstH);
		writeTables(pdlstH);
	}

	/**
	 * take a descision for the entire list. the descision is stored in pddecision field.
	 * <ol>
	 * <li>call the method {@link #checkSampleSize(Vector)} to check the validity of sample,. if ths sample is rejected,
	 * fill the list argumento whit empty record
	 * <li>Sort the list based on pdprediction, and selecting the {@link #selRange} higest values. The selection
	 * performed by this metod is stored in pd_descicion field.
	 * 
	 * @param pdList - list of record from pdistribution table
	 */
	private void takeDecision(Vector<Record> pdList) {
		// check samples
		Record sr = pdList.get(0);
		checkSampleSize(pdList);
		if (pdList.isEmpty()) {
			for (int c = 0; c < selRange; c++) {
				Record pdMod = new Record(sr);
				pdMod.setFieldValue("pddate", date);
				pdMod.setFieldValue("pdrace", race);
				pdMod.setFieldValue("pdfield", sr.getFieldValue("pdfield"));
				pdMod.setFieldValue("pdvalue", "List rejected " + (c + 1));
				pdMod.setFieldValue("pddecision", -(c + 1)); // mark for analisys
				pdList.addElement(pdMod);
			}
			return;
		}

		ArrayList<TEntry> vals = new ArrayList<TEntry>();
		for (Record r : pdList) {
			TEntry te = new TEntry(r, r.getFieldValue("pdprediction"));
			vals.add(te);
		}

		Collections.sort(vals);

		int cnt = vals.size() - 1;
		int min = Math.min(vals.size(), selRange);
		for (int i = 1; i <= min; i++) {
			TEntry te = (TEntry) vals.get(cnt--);
			Record r = (Record) te.getKey();
			r.setFieldValue("pddecision", i);
		}
	}

	private void writeTables(Vector<Record> col) {

		// pdistribution table
		if (writePdistribution) {
			DBAccess pddba = ConnectionManager.getAccessTo("pdistribution");
			for (Record rcd : col) {
				pddba.add(rcd);
			}

			// temporal: print the selected elements to easy copy to phone
			DBAccess dba = ConnectionManager.getAccessTo("reslr");
			for (int i = 1; i <= selRange; i++) {
				for (Record rcd : col) {
					if (((Integer) rcd.getFieldValue("pddecision")) == i) {
						String ho = (String) rcd.getFieldValue("pdvalue");
						int ra = (Integer) rcd.getFieldValue("pdrace");
						Record r = dba.exist("rerace = " + ra + " AND rehorse = '" + ho + "'");
						System.out.println(r.getFieldValue("restar_lane") + " \t" + ho);
					}
				}
			}
		}

		// statistics table
		if (!writeStatistics) {
			return;
		}
		// look for missing data
		DBAccess reslrdba = ConnectionManager.getAccessTo("reslr");
		Vector tmp = reslrdba.search("redate = '" + date + "' AND rerace = " + race, null);
		// take first. (any of them have the missing data)
		Record tr = (Record) tmp.elementAt(0);

		DBAccess statdba = ConnectionManager.getAccessTo("statistics");
		Record stsmod = statdba.getModel();

		total++;
		int exaccnt = 0, tricnt = 0, triCcnt = 0;
		boolean winb = false, placeb = false, third = false;
		for (Record rcd : col) {
			int dec = (Integer) rcd.getFieldValue("pddecision");
			int evt = (Integer) rcd.getFieldValue("pdevent");
			if (dec > 0) {
				stsmod.setFieldValue("stfield", rcd.getFieldValue("pdfield"));
				stsmod.setFieldValue("stdate", date);
				stsmod.setFieldValue("strace", race);
				// stsmod.setFieldValue("stsignature", horseSample * 100 + jockeySample * 10 + upBound);
				stsmod.setFieldValue("stsignature", selRange);
				stsmod.setFieldValue("stprediction", rcd.getFieldValue("pdprediction"));
				stsmod.setFieldValue("stdecision", rcd.getFieldValue("pddecision"));
				stsmod.setFieldValue("stevent", evt);
				stsmod.setFieldValue("stdistance", tr.getFieldValue("redistance"));
				stsmod.setFieldValue("sthorsegender", tr.getFieldValue("rehorsegender"));
				stsmod.setFieldValue("stelements", tmp.size());
				stsmod.setFieldValue("stserie", tr.getFieldValue("reserie"));
				stsmod.setFieldValue("stmean", rcd.getFieldValue("pdmean"));
				stsmod.setFieldValue("ststdev", rcd.getFieldValue("pdstdev"));
				statdba.add(stsmod);

				// winner
				winb = (dec == evt && dec == 1);
				winner = winb ? winner + 1 : winner;
				// place
				placeb = (dec == evt && dec == 2);
				place = placeb ? place + 1 : place;
				// 3th place
				third = (dec == evt && dec == 3);
				// exacta
				if (winb || placeb) {
					exaccnt++;
					if (exaccnt == 2) {
						exacta++;
					}
				}
				// trifecta
				if (winb || placeb || third) {
					tricnt++;
					if (tricnt == 3) {
						trifecta++;
					}
				}
				// combined trifecta
				if (dec < 4 && evt < 4) {
					triCcnt++;
					if (triCcnt == 3) {
						trifectaC++;
					}
				}

				// distribution: from any prediction to event 1
				for (int i = 0; i < distribution.length; i++) {
					if (dec == i + 1 && evt == 1) {
						distribution[i]++;
					}
				}
			}
		}
	}
}
