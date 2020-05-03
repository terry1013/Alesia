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

import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class SelectorCopyOf1 {

	private static NumberFormat percentformat;
	private static NumberFormat numberFormat;
	private int race;
	private Date date;
	private boolean writePdistribution;
	private boolean writeStatistics;

	int horseSample;
	int jockeySample;

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
	public SelectorCopyOf1(int race, Date date) {
		this.race = race;
		this.date = date;
		this.horseSample = 4;
		this.jockeySample = 20;
		this.selRange = 3;
		this.statThreshold = 2;
		this.writePdistribution = true;
		this.writeStatistics = true;

		percentformat = NumberFormat.getPercentInstance();
		percentformat.setMinimumFractionDigits(4);
		percentformat.setMaximumFractionDigits(4);
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMinimumFractionDigits(4);
		numberFormat.setMaximumFractionDigits(4);

	}

	/**
	 * Called from {@link CountEndPositions} action
	 * 
	 * @param rcdList
	 * @param hs
	 * @param js
	 */
	public static void countEndPositions(Record[] rcdList, int hs, int js) {
		SelectorCopyOf1 sel = new SelectorCopyOf1(0, null);
		sel.writeStatistics = false;
		sel.clearTables();
		DBAccess pddba = ConnectionManager.getAccessTo("pdistribution");
		DBAccess lrdba = ConnectionManager.getAccessTo("reslr");

		for (Record rcd : rcdList) {
			sel.date = (Date) rcd.getFieldValue("redate");
			sel.race = (Integer) rcd.getFieldValue("rerace");
			Vector<Record> elements = lrdba.search("rerace = " + sel.race + " AND redate = '" + sel.date + "'", null);
			for (Record elem : elements) {
				pddba.add(sel.countEndPosition(hs, "rehorse", (String) elem.getFieldValue("rehorse")));
				pddba.add(sel.countEndPosition(js, "rejockey", (String) elem.getFieldValue("rejockey")));
			}
		}
		DockingContainer.signalFreshgen(PDistribution.class.getName());
	}

	public static void runSimulation(int race, Date date) {
		runSimulation(race, date, 4);
	}

	public static void runSimulation(int race, Date date, int hs) {
		SelectorCopyOf1 sel = new SelectorCopyOf1(race, date);
		sel.horseSample = hs;
		sel.writeStatistics = false;
		sel.clearTables();
		sel.select();
		DockingContainer.signalFreshgen(PDistribution.class.getName());
	}

	public static void runSimulation(Record[] rcdList, int hs) {
		TTaskManager.executeTask(() -> {
			SelectorCopyOf1 sel = new SelectorCopyOf1(0, null);
//			sel.horseSample = hs;
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
	 * calculate the mass center of the pd_ fields. this method store such number in pdmasscenter field. Although for
	 * prediction, the better is correlated to left, this method perform a reflection and translation of x coordenated
	 * to reflech the mass center to the right. this translated mass center is stored in pdprediction field for use in
	 * {@link #takeDecision(Vector)} method's sort system.
	 * 
	 * @param pdRcd - pdistribution record
	 */
	private void centerOfMass(Record pdRcd) {
		double mc = 0;
		for (int c = 1; c < 15; c++) {
			double pi = (Double) pdRcd.getFieldValue("pd_" + c);
			mc += pi * c;

		}
		pdRcd.setFieldValue("pdmasscenter", mc);
		// refect and translate
		// pdRcd.setFieldValue("pdprediction", -mc + 14);
		pdRcd.setFieldValue("pdprediction", mc);

		// by cps
		double mcc = 0;
		CpsLine cps = (CpsLine) pdRcd.getFieldValue("pdline");
		Vector<Double> xc = new Vector<Double>(cps.keySet());
		for (Double x : xc) {
			mcc += cps.get(x) * (x + 0.01);
		}
		pdRcd.setFieldValue("pdline", "");
		// refect and translate
		// pdRcd.setFieldValue("pdprediction", -mcc + 14);
		// pdRcd.setFieldValue("pdprediction", mcc);

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
	 * return a {@link Record} of pdistribution table counting the reend_pos and set the corresponding pd_# field. This
	 * method:
	 * <ol>
	 * <li>retrive the subset of elements form reslr table according to given argumento where pdField = pdValue sorted
	 * by race date and look back until the number of records indicated by <code>max</code> parameter has been counted.
	 * <li>count the reend_pos field an put in the correspoding pd_# field
	 * </ol>
	 * The result is a record with all pd_# field setted whit the sum of all corresponding reend_pos
	 * 
	 * @param max - numbers of record to take into account
	 * @param pdField - field
	 * @param pdValue - field value
	 * 
	 * @return record
	 */
	private Record countEndPosition(int max, String pdField, String pdValue) {
		DBAccess pddba = ConnectionManager.getAccessTo("pdistribution");
		Record pdMod = pddba.getModel();
		CpsLine htcps = new CpsLine();
		int cnt = 0;
		
		// step 1
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		// 290718: dont take into account simulations races (when count is for jockey)
		String wc = "redate < '" + date + "' AND " + pdField + " = '" + pdValue + "' AND rehorsegender != 'S'";
		Vector<Record> rcds = dba.search(wc, "redate DESC");
		pdMod.setFieldValue("pddate", date);
		pdMod.setFieldValue("pdrace", race);
		pdMod.setFieldValue("pdfield", pdField);
		pdMod.setFieldValue("pdvalue", pdValue);

		// step 2
		for (Record rcd : rcds) {
			// count the reend_pos and set the corresponding pd_ field
			int endp = (Integer) rcd.getFieldValue("reend_pos");
			double p = (Double) pdMod.getFieldValue("pd_" + endp);
			pdMod.setFieldValue("pd_" + endp, ++p);

			// prepare the line for center of mass based on recps field
			double cp = (Double) rcd.getFieldValue("recps");
			if (cp > -1) {
//				cp = (cp > 14) ? 14 : cp;
				double t = htcps.get(cp);
				htcps.put(cp, ++t);
			}

			cnt++;
			// if the numbers of records are already reached end iteration.
			if (cnt == max) {
				break;
			}
		}
		pdMod.setFieldValue("pdline", htcps);
		pdMod.setFieldValue("pdsamplesize", cnt);
		return pdMod;
	}

	/**
	 * return a {@link Vector} of pdistribution table counting the reend_pos and set the corresponding pd_# field for
	 * the list of reslr elements. This method invoke {@link #countEndPosition(int, String, String)} foreach element of
	 * the list.
	 * 
	 * @param samSize - sample size
	 * @param pdField - fielt to count
	 * @param rsList - list of reslr table to count
	 * 
	 * @return list of counted elements
	 */
	private Vector<Record> countEndPosition(int samSize, String pdField, Vector<Record> rsList) {
		Vector<Record> rlist = new Vector<Record>();
		for (Record rcd : rsList) {
			Record r = countEndPosition(samSize, pdField, (String) rcd.getFieldValue(pdField));
			// set missing fields
			r.setFieldValue("pdevent", rcd.getFieldValue("reend_pos"));
			r.setFieldValue("pdselrange", selRange);
			rlist.add(r);
		}
		return rlist;

	}

	/**
	 * Calculate the single prob distribution and cumulative probability for the given uper bound.
	 * <ol>
	 * <li>sum the numbers of events stored in pd_ field
	 * <li>forall pd_ field, caculate single probability
	 * <li>Store the cumulative prob in pdprediction field
	 * </ol>
	 * <p>
	 * TODO: if sum = 0 maybe is a new element. this method does nothin when sum = 0 but maybe a imputation is required
	 * 
	 * @param pdRcd - Record to calc the pdf
	 */
	private void frequencyDistribution(Record pdRcd) {
		double sum = 0;

		// descriptiv statistics for pd_ fields
		DescriptiveStatistics stat = new DescriptiveStatistics();
		stat.clear();
		for (int l = 1; l < 15; l++) {
			stat.addValue((Double) pdRcd.getFieldValue("pd_" + l));
		}
		pdRcd.setFieldValue("pdstdev", stat.getStandardDeviation());
		pdRcd.setFieldValue("pdmean", stat.getMean());

		// sum of pd_ field
		for (int l = 1; l < 15; l++) {
			sum += (Double) pdRcd.getFieldValue("pd_" + l);
		}

		// sum = 0? do nothing
		if (sum == 0) {
			return;
		}
		// single weight for every pd_# field.
		for (int l = 1; l < 15; l++) {
			double we = (Double) pdRcd.getFieldValue("pd_" + l);
			pdRcd.setFieldValue("pd_" + l, we / sum);
		}

		// single weight for every element in line of cps.
		double sumc = 0;
		CpsLine cps = (CpsLine) pdRcd.getFieldValue("pdline");
		Vector<Double> vals = new Vector<Double>(cps.values());
		for (Double v1 : vals) {
			sumc += v1;
		}
		vals = new Vector<Double>(cps.keySet());
		for (Double k1 : vals) {
			double we = cps.get(k1);
			cps.put(k1, we / sumc);
		}
		centerOfMass(pdRcd);
	}

	private void frequencyDistribution(Vector<Record> pdList) {
		for (Record record : pdList) {
			frequencyDistribution(record);
		}
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
	 * <li>Retrive all the records involved in the race and date passed as argument
	 * <li>Prepare the the prediction counting the end position field & prepare the line from body diference. See
	 * {@link #countEndPosition(int, String, Vector)}
	 * <li>Append 10% of weight from jockey frequency distribution. in this way, the resultant mass centar will be
	 * influenced by it
	 * <li>update the pdistribution and statistics tables.
	 * <p>
	 * 
	 */
	private void select() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String ud = sdf.format(date);
		log("Selecting elements for race " + race + " of date " + date, null);
		
		// step 1 
		DBAccess dba = ConnectionManager.getAccessTo("reslr");
		Vector<Record> rcds = dba.search("rerace = " + race + " AND redate = '" + ud + "'", "redate DESC");

		// step 2
		Vector<Record> pdlstH = countEndPosition(horseSample, "rehorse", rcds);

		// 3
		// RESULT: selecting salmple size = 20: convergenci: 180 , Area1-2: 100, Lane 1: 55, per 2-1: 45
		// RESULT: selecting salmple size = 10: convergenci: 175 , Area1-2: 103, Lane 1: 49, per 2-1: 54
		// about 2% increment (from 171 to 180)
		Vector<Record> pdlisJ = countEndPosition(jockeySample, "rejockey", rcds);
		for (int h = 0; h < pdlstH.size(); h++) {
			Record hr = pdlstH.elementAt(h);
			Record jr = pdlisJ.elementAt(h);

			// pd_ fields
			for (int c = 1; c < 15; c++) {
				double hp = (Double) hr.getFieldValue("pd_" + c);
				double jp = (Double) jr.getFieldValue("pd_" + c);
				hp += jp * 0.10;
				hr.setFieldValue("pd_" + c, hp);
			}

			// cps line
			CpsLine hoc = (CpsLine) hr.getFieldValue("pdline");
			CpsLine joc = (CpsLine) jr.getFieldValue("pdline");
			Vector<Double> vec = new Vector<Double>(joc.keySet());
			for (Double k1 : vec) {
				double hp = hoc.get(k1);
				double jp = joc.get(k1);
				hp += jp * 0.10;
				hoc.put(k1, hp);
			}
		}
		frequencyDistribution(pdlstH);
		takeDecision(pdlstH);
		writeTables(pdlstH);
	}

	/**
	 * take a descision for the entire list. the descision is stored in pddecision field.
	 * <ol>
	 * <li>call the method {@link #checkSampleSize(Vector)} to check the validity of sample. if ths sample is rejected,
	 * fill the list argumento whit empty record showing that the race was rejected
	 * 
	 * <li>Sort the list based on pdprediction, field and selecting the {@link #selRange} lowers values. The selection
	 * performed by this metod is stored in pd_descicion field.
	 * 
	 * @param pdList - list of record from pdistribution table
	 */
	private void takeDecision(Vector<Record> pdList) {

		// step 1
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

		// step 2
		ArrayList<TEntry> vals = new ArrayList<TEntry>();
		for (Record r : pdList) {
			TEntry te = new TEntry(r, r.getFieldValue("pdprediction"));
			vals.add(te);
		}
		Collections.sort(vals);
		for (int i = 0; i < selRange; i++) {
			TEntry te = (TEntry) vals.get(i);
			Record r = (Record) te.getKey();
			r.setFieldValue("pddecision", i + 1);
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

	class CpsLine extends Hashtable<Double, Double> {
		CpsLine() {
			super();
		}
		@Override
		public synchronized Double get(Object key) {
			Double obj = super.get(key);
			return obj == null ? 0.0 : obj;
		}
	}
}
