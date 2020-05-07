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

import javax.swing.*;

import org.apache.commons.math3.geometry.euclidean.twod.*;
import org.apache.commons.math3.stat.*;
import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class Selector {

	private static NumberFormat percentformat;
	private static NumberFormat numberFormat;
	/**
	 * default tolerance for numerical calculation
	 */
	static final double TOLERANCE = 1.0e-4;
	private int race;
	private Date date;
	private boolean writePdistribution;

	private boolean writeStatistics;
	int horseSample;

	int jockeySample;

	final int minSample = 2;

	/**
	 * number of elements to be selected.
	 */
	int selRange;
	private int total, winner, place, exacta, trifectaC, trifecta = 0;
	private int[] toOneDistribution = new int[14];
	private int[] oneToOneDistribution = new int[14];
	private int[] exactaByElements = new int[14];
	private int[] winnerByElements = new int[14];

	public Selector(int race, Date date) {
		this.race = race;
		this.date = date;
		this.horseSample = 4;
		this.jockeySample = 20;
		this.selRange = 3;
		this.writePdistribution = true;
		this.writeStatistics = true;

		percentformat = NumberFormat.getPercentInstance();
		percentformat.setMinimumFractionDigits(4);
		percentformat.setMaximumFractionDigits(4);
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMinimumFractionDigits(4);
		numberFormat.setMaximumFractionDigits(4);

	}

	public static void checkEndPositionCPS(Record[] rcdList) {
		DBAccess lrdba = ConnectionManager.getAccessTo("reslr");
		boolean df = false;
		for (Record rcd : rcdList) {
			Date date = (Date) rcd.getFieldValue("redate");
			int race = (Integer) rcd.getFieldValue("rerace");
			Vector<Record> endlist = lrdba.search("recps > -1 AND redate = '" + date + "' AND rerace = " + race,
					"reend_pos");
			Vector<Record> cpslist = lrdba.search("recps > -1 AND redate = '" + date + "' AND rerace = " + race,
					"recps, reend_pos");
			for (int i = 0; i < endlist.size(); i++) {
				String rehorse = (String) endlist.elementAt(i).getFieldValue("rehorse");
				if (!rehorse.equals(cpslist.elementAt(i).getFieldValue("rehorse"))) {
					df = true;
					System.out.println("Discrepancy found in race " + date + " " + race + " on " + rehorse);
					break;
				}
			}
		}
		// TODO: check that recps=-1 must be the last element of the race (last end_pos)
		if (!df) {
			System.out.println("no discrepancy found");
		}
	}

	public static void runSimulation(int race, Date date) {
		runSimulation(race, date, 4);
	}

	public static void runSimulation(int race, Date date, int hs) {
		Selector sel = new Selector(race, date);
		sel.horseSample = hs;
		sel.writeStatistics = false;
		sel.clearTables();
		sel.select();
		DockingContainer.signalFreshgen(PDistributionList.class.getName());
	}

	public static void runSimulation(Record[] rcdList, int hs) {
		TTaskManager.executeTask(() -> {
			Selector sel = new Selector(0, null);
			// sel.horseSample = hs;
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
					pg.setNote(sel.date + " " + sel.race);
					sel.select();
				}
				sel.printStats();
				DockingContainer.signalFreshgen(Statistics.class.getName());
				pg.close();
			});
	}

	private void log(String msg) {
		if (msg != null && !countingHits) {
			System.out.println(msg);
		}
	}

	/**
	 * This method return a proyected time of the horse speed based on the know historical data.
	 * 
	 * <ol>
	 * <li>for each element of the incoming sample, build a curve that represent the time at each race tack sensor. see
	 * {@link Selector#getSpeedPoints(Record)}.
	 * <li>If a rece don't reach the normalization distance, a line between 400m and this race distance is build for
	 * proyection (naive extrapolation), else, such line is build using the last 2 know points (last sensor and final
	 * distance)
	 * <li>compute the intersctions point between the vertical line at a fixed distance and the line calc at previos
	 * step
	 * <li>use a desicion rule: "Discart the worst, select the second best" to compute the normalized time. if a element
	 * has no enough historical data, -1 is returnded
	 * 
	 * @param reslst sample of the element to estimate
	 * @return proyected time at normalized distance or -1 if not enough data was found.
	 */
	public Histogram getProyectedTime(Vector<Record> reslst, int dist) {
		Vector<Line> linlst = new Vector();
		for (Record rcd : reslst) {
			Vector2D[] pts = Selector.getSpeedPoints(rcd);
			int d = (Integer) rcd.getFieldValue("redistance");
			Line lin = null;
			// extrapolation line form 400m to last know point
			if (d < dist) {
				lin = new Line(pts[1], pts[pts.length - 1], TOLERANCE);
			} else {
				// line based on intersection interval
				lin = new Line(pts[pts.length - 2], pts[pts.length - 1], TOLERANCE);
			}
			linlst.add(lin);
		}

		// intersection point
		Line verl = new Line(new Vector2D(dist, 0), new Vector2D(dist, 1000), TOLERANCE);
		Histogram his = new Histogram();
		linlst.stream().forEach(lin -> {
			Vector2D pi = lin.intersection(verl);
			his.increment(pi.getY());
		});

		/*
		 * ArrayList<Double> tmp = new ArrayList(); for (Line lin : linlst) { Vector2D pi = lin.intersection(verl);
		 * tmp.add(pi.getY()); }
		 * 
		 * // discart the worst, select the second best (the middle of three left) Collections.sort(tmp); return
		 * tmp.size() > 2 ? tmp.get(1) : tmp.get(0);
		 */
		return his;
	}
	private void addJockeyWeight1(Vector<Record> horseSample, Vector<Record> raceList) {
		for (int h = 0; h < horseSample.size(); h++) {
			Record hr = horseSample.elementAt(h);
			Record jr = raceList.elementAt(h);

			// numbers of horse samples           
			int hsa = (Integer) hr.getFieldValue("pdsamplesize");
			String val = (String) jr.getFieldValue("rejockey");
			int jsa = hsa * 5;
			Vector<Record> samplst = getSample(jsa, "rejockey", val);
			Histogram jlin = getFieldCount("reend_pos", samplst);

			// pdend_position line
			Histogram hlin = (Histogram) hr.getFieldValue("pdend_posline");
			// Histogram jlin = (Histogram) jr.getFieldValue("pdend_posline");
			Collection<Double> jkeys = jlin.keySet();
			for (Double jk : jkeys) {
				double hp = hlin.get(jk);
				double jp = jlin.get(jk);
				hp += jp * 0.10;
				hlin.put(jk, hp);
			}
		}
	}
	/**
	 * add a fraction of the joceky performance to the horse performance. (from jockey histogram line to horse histogram
	 * line). this method is based on asumption that the jockey infuence the outcome.
	 * <p>
	 * RESULT: selecting salmple size = 20: convergenci: 180 , Area1-2: 100, Lane 1: 55, per 2-1: 45 RESULT: selecting
	 * salmple size = 10: convergenci: 175 , Area1-2: 103, Lane 1: 49, per 2-1: 54 about 2% increment (from 171 to 180)
	 * 
	 * @param horseSample - horse list
	 * @param jockeySample
	 */
	private void addJockeyWeight(Vector<Record> horseSample, Vector<Record> jockeySample) {
		for (int h = 0; h < horseSample.size(); h++) {
			Record hr = horseSample.elementAt(h);
			Record jr = jockeySample.elementAt(h);

			// pdend_position line
			Histogram hlin = (Histogram) hr.getFieldValue("pdend_posline");
			Histogram jlin = (Histogram) jr.getFieldValue("pdend_posline");
			Collection<Double> jkeys = jlin.keySet();
			for (Double jk : jkeys) {
				double hp = hlin.get(jk);
				double jp = jlin.get(jk);
				hp += jp * 0.10;
				hlin.put(jk, hp);
			}

			// cps line
			hlin = (Histogram) hr.getFieldValue("pdcpsline");
			jlin = (Histogram) jr.getFieldValue("pdcpsline");
			jkeys = jlin.keySet();
			for (Double jk : jkeys) {
				double hp = hlin.get(jk);
				double jp = jlin.get(jk);
				hp += jp * 0.10;
				hlin.put(jk, hp);
			}
			// setStatisticsFields(hr);
		}
	}

	/**
	 * calc the center of mass of the given histogram.
	 * 
	 * @param histo histogram to calc
	 * @return center of mass
	 */
	private double centerOfMass(Histogram histo) {
		// summation of al values
		final double sum = histo.values().stream().mapToDouble(Double::doubleValue).sum();

		// sum = 0? set an arbitrary high value to put this element to the en of the list
		// sum = 0 means this element has no statistical data
		if (sum == 0) {
			return 100.0;
		}
		Collection<Double> xc = histo.keySet();
		double mc = 0.0;
		for (Double x : xc) {
			// single weight
			double val = histo.get(x) / sum;
			// correlate 0.01 to the right to allow 0 x-coordenate values
			mc += val * (x + 0.01);
		}
		return mc;
	}

	private final double kelly_p = 0.30;
	private final double kelly_bDiv = 3;

	/**
	 * apply kelly criterion to the incoming list based on previous parameter p and b divisor. this cirtirion has 2
	 * uses:
	 * <ol>
	 * <li>to accept only races that historicaly has more win probability (races with 7 or more elemens)
	 * <li>based on previous step, has the practical purporse for invest in billboards
	 * </ol>
	 */
	private boolean isValidKelly(Vector<Record> pdList) {
		boolean val = true;
		double b = ((double) pdList.size()) / kelly_bDiv;
		double kc = (kelly_p * b - (1 - kelly_p)) / b;
		if (kc <= 0) {
			log("Selection of elements rejected by kelly criterion: " + numberFormat.format(kc));
			val = false;
		}
		return val;
	}
	/**
	 * evaluate the incoming list searchin if the elements has enought statistical data. this method:
	 * <ol>
	 * <li>Count the pdsamplesize field forall record in the argument list. and element is cosider with enought data if
	 * the sample size >= {@link #minSample} variable
	 * <li>Based on the previous count, the list consider valid only if there are valid records > (list.size / 3). else
	 * , the list is cleaned and a log msg is printed
	 * 
	 * @param pdList - list of pdistribution's record to evaluate
	 */
	private boolean isValidSample(Vector<Record> pdList) {
		int valid = 0;
		boolean val = true;
		// int minvalid = pdList.size() * 2 / 3; // 2/3 test
		// int minvalid = pdList.size() * 3 / 5; // 3/5 test

		int minvalid = pdList.size() / 2; // 1/2 base for comparation

		// count invalid samples
		for (Record r : pdList) {
			int ss = (Integer) r.getFieldValue("pdsamplesize");
			valid = (ss >= minSample) ? valid + 1 : valid;
		}

		// enougth valid samples?
		if (valid < minvalid) {
			log("Selection of elements rejected by lack of historical data. The list contain only " + valid + "/"
					+ pdList.size() + " valid samples.");
			val = false;
		}
		return val;
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

	public static String END_POSITION = "End position";
	public static String DIFERENCE_FROM_FIRST = "Diference form first";
	public static String PROYECTED_TIME = "Proyected time";
	private String selectBy = END_POSITION;

	/**
	 * set the prediction field based on the select algorithm.
	 * 
	 * @param pdList
	 */
	private void computePrediction(Vector<Record> pdList) {
		for (Record pdRcd : pdList) {
			if (selectBy == END_POSITION) {
				Histogram histo = (Histogram) pdRcd.getFieldValue("pdend_posline");
				pdRcd.setFieldValue("pdprediction", centerOfMass(histo));
			}
			if (selectBy == DIFERENCE_FROM_FIRST) {
				Histogram histo = (Histogram) pdRcd.getFieldValue("pdcpsline");
				pdRcd.setFieldValue("pdprediction", centerOfMass(histo));
			}
			if (selectBy == PROYECTED_TIME) {
				Histogram histo = (Histogram) pdRcd.getFieldValue("pdtimeline");
				pdRcd.setFieldValue("pdprediction", centerOfMass(histo));
			}
		}
	}

	/**
	 * return a {@link Vector} of pdistribution table colecting all necesary data for prediction.
	 * 
	 * @param samSize - sample size
	 * @param field - fielt to count
	 * @param raceList - list of reslr table to count
	 * 
	 * @return list of counted elements
	 */
	private Vector<Record> countEndPosition(int samSize, String field, Vector<Record> raceList) {
		// DescriptiveStatistics stat = new DescriptiveStatistics();
		// pdRcd.setFieldValue("pdstdev", stat.getStandardDeviation());
		// pdRcd.setFieldValue("pdmean", stat.getMean());
		Vector<Record> rlist = new Vector<Record>();
		Record pdMod = ConnectionManager.getAccessTo("pdistribution").getModel();
		pdMod.setFieldValue("pddate", date);
		pdMod.setFieldValue("pdrace", race);
		pdMod.setFieldValue("pdfield", field);
		int dist = (Integer) raceList.elementAt(0).getFieldValue("redistance");
		for (Record raceR : raceList) {
			Record pdRcd = new Record(pdMod);
			String val = (String) raceR.getFieldValue(field);
			pdRcd.setFieldValue("pdvalue", val);
			Vector<Record> samplst = getSample(samSize, field, val);
			pdRcd.setFieldValue("pdend_posline", getFieldCount("reend_pos", samplst));
			pdRcd.setFieldValue("pdcpsline", getFieldCount("recps", samplst));
			pdRcd.setFieldValue("pdtimeline", getProyectedTime(samplst, dist));
			pdRcd.setFieldValue("pdsamplesize", samplst.size());
			pdRcd.setFieldValue("pdselrange", selRange);
			pdRcd.setFieldValue("pdevent", raceR.getFieldValue("reend_pos"));
			rlist.add(pdRcd);
		}
		return rlist;

	}

	private void setStatisticsFields(Record pdRcd) {
		DescriptiveStatistics stat = new DescriptiveStatistics();
		Histogram histo = null;
		if (selectBy == END_POSITION) {
			histo = (Histogram) pdRcd.getFieldValue("pdend_posline");
		}
		if (selectBy == DIFERENCE_FROM_FIRST) {
			histo = (Histogram) pdRcd.getFieldValue("pdcpsline");
		}
		if (selectBy == PROYECTED_TIME) {
			histo = (Histogram) pdRcd.getFieldValue("pdtimeline");
		}
		Set<Double> tmp = histo.keySet();
		for (Double k : tmp) {
			stat.addValue(k * (histo.get(k) + 1));
		}
		if (stat.getN() > 0) {
			pdRcd.setFieldValue("pdstdev", stat.getStandardDeviation());
			pdRcd.setFieldValue("pdmean", stat.getMean());
		} else {
			// if has no data, put 99
			pdRcd.setFieldValue("pdmean", 99.0);
		}
	}

	private int arriveWindow = 4;
	boolean countingHits = false;
	private double countHits(Record pdRcd) {
		double hits = 0.0;
		String hor = (String) pdRcd.getFieldValue("pdvalue");
		Vector<Record> samplst = getSample(4, "rehorse", hor);
		for (Record srcd : samplst) {
			int ra = (Integer) srcd.getFieldValue("rerace");
			Date da = (Date) srcd.getFieldValue("redate");
			Selector sel = new Selector(ra, da);
			sel.writePdistribution = false;
			sel.writeStatistics = false;
			sel.countingHits = true;
			Vector<Record> pdlst = sel.select();
			for (Record pdrcd : pdlst) {
				String pdhor = (String) pdrcd.getFieldValue("pdvalue");
				int dec = (Integer) pdrcd.getFieldValue("pddecision");
				if (pdhor.equals(hor) && dec > 0) {
					// if a take a decicion and the asociated event is < frames
					int evt = (Integer) pdrcd.getFieldValue("pdevent");
					// int res = dec > evt ? dec - evt : evt - dec;
					if (evt <= arriveWindow) {
						// if (res <= arriveWindow) {
						hits++;
					}
				}
			}
		}
		// to probability
		if (samplst.size() > 0) {
			hits = hits / samplst.size();
		}
		return hits;
	}

	/**
	 * return {@link Histogram} where the x-coordenate represent the element value and y-coordente, the element's number
	 * of occurrence
	 * 
	 * @param ofField - numeric field to count
	 * @param sample - list of records form reslr file to count
	 * 
	 * @return array of Vector2D
	 */
	private Histogram getFieldCount(String ofField, Vector<Record> sample) {
		Histogram pts = new Histogram();
		for (Record rcd : sample) {
			double k = Double.valueOf(rcd.getFieldValue(ofField).toString());
			pts.increment(k);
		}
		return pts;
	}

	/**
	 * Return a list for {@link Record} form reslr table acording to selected parameters
	 * 
	 * a sample returned by this method follow:
	 * <ul>
	 * <li>ordered by field <code>redate DESC</code>
	 * <li>all elements are from date < actual date
	 * <li>not simulation elements are returned
	 * <li>don't retrive accidents (repcs == -1)
	 * </ul>
	 * 
	 * @param size - sample size
	 * @param field - field
	 * @param value - value of field
	 * @return desired sample
	 */
	private Vector<Record> getSample(int size, String field, String value) {
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		String wc = "redate < '" + date + "' AND " + field + " = '" + value
				// TODO: temporal 
				+ "' AND rehorsegender != 'S' AND recps != -1 AND retrack = 'lr'";
		return dba.search(wc, "redate DESC", size);
	}

	private void printStats() {

		System.out.println("Total\t\t" + total);
		double totd = total;
		System.out.println("Winner  \t" + winner + "\t" + percentformat.format(winner / totd));
		System.out.println("Place   \t" + place + "\t" + percentformat.format(place / totd));
		System.out.println("Exacta  \t" + exacta + "\t" + percentformat.format(exacta / totd));
		System.out.println("Trifecta\t" + trifecta + "\t" + percentformat.format(trifecta / totd));
		System.out.println("C. Trifecta\t" + trifectaC + "\t" + percentformat.format(trifectaC / totd));

		// System.out.println("From # to one:");
		for (int i = 0; i < toOneDistribution.length; i++) {
			System.out.println("from " + (i + 1) + " to 1:\t" + toOneDistribution[i] + "\t"
					+ percentformat.format(toOneDistribution[i] / totd));
		}
		// System.out.println("One to one:");
		double sum = 0;
		for (int i : oneToOneDistribution) {
			sum += i;
		}
		// to avoid div by cero. (if cero, does't matter because numerator is cero too)
		sum = sum == 0 ? 1 : sum;
		for (int i = 0; i < oneToOneDistribution.length; i++) {
			// System.out.println((i + 1) + "  to " + (i + 1) + ": \t" + oneToOneDistribution[i] + "\t"
			// + percentformat.format(oneToOneDistribution[i] / sum));
		}
		System.out.println("Exacta by numbers of race elements:");
		sum = 0;
		for (int i : exactaByElements) {
			sum += i;
		}
		// to avoid div by cero. (if cero, does't matter because numerator is cero too)
		sum = sum == 0 ? 1 : sum;
		for (int i = 0; i < exactaByElements.length; i++) {
			System.out.println((i + 1) + " Elements: \t" + exactaByElements[i] + "\t"
					+ percentformat.format(exactaByElements[i] / sum));
		}
		System.out.println("Winner by numbers of race elements:");
		sum = 0;
		for (int i : winnerByElements) {
			sum += i;
		}
		// to avoid div by cero. (if cero, does't matter because numerator is cero too)
		sum = sum == 0 ? 1 : sum;
		for (int i = 0; i < winnerByElements.length; i++) {
			System.out.println((i + 1) + " Elements: \t" + winnerByElements[i] + "\t"
					+ percentformat.format(winnerByElements[i] / sum));
		}
	}

	/**
	 * Predict the future outcome based on selected parameters. This method try to predict the outcome for the given
	 * date/race parameters based on horseSample, jockeySample and cumulative probability (upBound). The data is
	 * prepared following the steps:
	 * <ol>
	 * <li>Retrive all the records involved in the race and date passed as argument
	 * <li>collect the necesary data. See {@link #countEndPosition(int, String, Vector)}
	 * <li>Append 10% of weight from jockey frequency distribution. in this way, the resultant mass centar will be
	 * influenced by it
	 * <li>compute prediction according to selected algorithm
	 * <li>update the pdistribution and statistics tables.
	 * <p>
	 * 
	 */
	private Vector<Record> select() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String ud = sdf.format(date);
		log("Selecting elements for race " + race + " of date " + date);

		// step 1
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		Vector<Record> rcds = dba.search("rerace = " + race + " AND redate = '" + ud + "'", "redate DESC");

		// step 2
		Vector<Record> pdlstH = countEndPosition(horseSample, "rehorse", rcds);
		Vector<Record> pdlisJ = countEndPosition(jockeySample, "rejockey", rcds);

		// step 3
		addJockeyWeight(pdlstH, pdlisJ);
//		addJockeyWeight1(pdlstH, rcds);

		// step 4
		computePrediction(pdlstH);
		takeDecision(pdlstH);

		// step 5
		writeTables(pdlstH);
		return pdlstH;
	}

	/**
	 * take a descision for the entire list. the descision is stored in <code>pddecision</code> field.
	 * <ol>
	 * <li>call the method {@link #isValidSample(Vector)} and {@link #isValidKelly(Vector)} to check the validity of
	 * sample. if ths sample is rejected, this method clear the list.
	 * <li>Sort the list based on <code>pdprediction</code>, field and selecting the {@link #selRange} lowers values.
	 * The selection performed by this metod is stored in <code>pddescicion</code> field.
	 * 
	 * @param pdList list of record from pdistribution table
	 */
	private void takeDecision(Vector<Record> pdList) {

		// step 1
		if (!isValidSample(pdList) || !isValidKelly(pdList)) {
			pdList.clear();
			return;
		}

		// step 2
		ArrayList<TEntry> vals = new ArrayList<TEntry>();
		for (Record r : pdList) {
			TEntry te = new TEntry(r, r.getFieldValue("pdprediction"));
			vals.add(te);
		}
		Collections.sort(vals);
		// in hitcount may be less element that selrance
		int sis = vals.size() < selRange ? vals.size() : selRange;
		for (int i = 0; i < sis; i++) {
			// for (int i = 0; i < vals.size(); i++) {
			TEntry te = (TEntry) vals.get(i);
			Record r = (Record) te.getKey();
			r.setFieldValue("pddecision", i + 1);
		}

		/*
		 * if (countingHits) { return; } // check pdhits field double minProb = 0.50; int deccnt = selRange; for (int i
		 * = 0; i < vals.size(); i++) { TEntry te = (TEntry) vals.get(i); Record r = (Record) te.getKey(); int dec =
		 * (Integer) r.getFieldValue("pddecision"); if (dec > 0) { double h = countHits(r); r.setFieldValue("pdhits",
		 * h); if (h < minProb) { deccnt--; r.setFieldValue("pddecision", 0); } } if (dec == 0 && deccnt < selRange) {
		 * double h = countHits(r); r.setFieldValue("pdhits", h); if (h >= minProb) { deccnt++;
		 * r.setFieldValue("pddecision", i); } } } int decc = 1; for (TEntry te : vals) { Record r = (Record)
		 * te.getKey(); int dec = (Integer) r.getFieldValue("pddecision"); if (dec > 0) { r.setFieldValue("pddecision",
		 * decc++); } } // delete list if i can hoose unleas 2 elements for exacta if (decc < 3) { //
		 * pdList.stream().forEach(r -> r.setFieldValue("pddecision", 0)); pdList.clear();
		 * log("Selection of elements rejected by no decision based on hits fields."); }
		 */
	}
	private void writeTables(Vector<Record> col) {
		// nothing to write
		if (col.size() == 0) {
			return;
		}

		// pdistribution table
		if (writePdistribution) {
			DBAccess pddba = ConnectionManager.getAccessTo("pdistribution");
			for (Record rcd : col) {
				pddba.add(rcd);
			}

			DBAccess dba = ConnectionManager.getAccessTo("reslr");
			// temporal: print the selected elements to easy copy to phone
			for (int i = 1; i <= selRange; i++) {
				for (Record rcd : col) {
					if (((Integer) rcd.getFieldValue("pddecision")) == i) {
						String ho = (String) rcd.getFieldValue("pdvalue");
						int ra = (Integer) rcd.getFieldValue("pdrace");
						Record r = dba.exist("rerace = " + ra + " AND rehorse = '" + ho + "'");
						log(r.getFieldValue("restar_lane") + " \t" + ho);
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
				if (winb) {
					winnerByElements[col.size() - 1]++;
				}
				// place
				placeb = (dec == evt && dec == 2);
				place = placeb ? place + 1 : place;
				// 3th place
				third = (dec == evt && dec == 3);
				// exacta
				if (winb || placeb) {
					exaccnt++;
					if (exaccnt == 2) {
						exactaByElements[col.size() - 1]++;
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

				// distribution
				for (int i = 0; i < toOneDistribution.length; i++) {
					// from any prediction to event 1
					if (dec == i + 1 && evt == 1) {
						toOneDistribution[i]++;
					}
					// one to one
					if (dec == i + 1 && evt == i + 1) {
						oneToOneDistribution[i]++;
					}
				}
			}
		}
	}
	private Hashtable<String, Frequency> distributions;
	private Frequency getFrequency(String name) {
		Frequency f = distributions.get(name);
		if (f == null) {
			f = new Frequency();
			distributions.put(name, new Frequency());
		}
		return f;
	}
	/**
	 * Return a array of {@link Vector2D} that represent the calculated time at each sensor in the race track.
	 * <p>
	 * This method work based on the assumption that the final position of the horse is a reflection of its position at
	 * every sensor. i.e.: if a horse finish last at 10.50 from the winner, this horse at first sensor were last but
	 * within a fraction of the final difference. While the race evolves, this difference gets wider until reach the
	 * final values.
	 * 
	 * @param rcd - record form reslr file
	 * @param todist - distance to extrapolate
	 * @return
	 */
	public static Vector2D[] getSpeedPoints(Record rcd) {
		// 1seg = 5cps
		double mycps = (Double) rcd.getFieldValue("recps") / 5;
		int dist = (Integer) rcd.getFieldValue("redistance");
		Vector<Integer> sens = getSensors(dist);

		// iterate until 5 sensor ( 4 of my database implementation + 1 of star line)
		if (sens.size() > 6) {
			final int dist1 = sens.elementAt(5);
			sens.removeIf((d) -> d >= dist1);
			sens.add(dist);
			// Predicate<Integer> rem = (d) -> d > dist1;
		}
		Vector2D[] pts = new Vector2D[sens.size()];
		// asumption: the horse reach 13.15m/s at 46m.
		// xval[0] = 46;
		// yval[0] = 13.1;
		pts[0] = new Vector2D(0, 0);

		for (int c = 1; c < sens.size(); c++) {
			// position inside the cloud at sensor c+1
			double dif = ((double) (c + 1)) / ((double) sens.size()) * mycps;
			// partial or final time
			double par = (sens.elementAt(c) != dist) ? (Double) rcd.getFieldValue("repartial" + c) : (Double) rcd
					.getFieldValue("reracetime");
			// stimated time
			double tim = par + dif;
			pts[c] = new Vector2D((double) sens.elementAt(c), tim);
			// par + dif = time. velocity = distance/time expressed in m/seg
			// yval[c] = xval[c] / (par + dif);
		}
		return pts;
	}

	/**
	 * Return the positions of the sensor array placed in the racetrack starting at 0m (the star line). the last sensor
	 * is the finish line.
	 * 
	 * @param dist - distance of the race
	 * @return list of sensor positions (in meters)
	 */
	public static Vector<Integer> getSensors(int dist) {
		int sensors = ((int) Math.ceil(dist / 400));
		// only i have 4 partials
		// sensors = (sensors > 4) ? 4 : sensors;
		Vector<Integer> arr = new Vector<Integer>();
		for (int i = 0; i < sensors; i++) {
			arr.add((i + 1) * 400);
		}
		if ((dist % 400) > 0) {
			arr.add(dist);
		}
		// add star lane
		arr.add(0, 0);
		return arr;
	}
}
