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
package plugin.dbtweezer;

import gui.datasource.*;

import java.util.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * plugin entry for mail
 * 
 * @author terry
 * 
 */
public class DBTweezer extends PluginAdapter {

	public static String SLE_DIVIDE_AND_ROUND = "${field} = SleUtilities.divideAndRoundDecimal(${field}, divisor, iScale, iRoundDec, iTopScale)";

	@Override
	public Object executePlugin(Object obj) {

		JMenu jm = new JMenu(myProperties.getProperty("plugin.caption"));
		// MenuActionFactory maf = new MenuActionFactory(ConnectionProfile.class);
		// maf.setDimentionFactor(-1);
		jm.add(new ConnectionProfileAction(true, true));
		jm.addSeparator();
		jm.add(new DBTweezerAction());
		return jm;
	}

	/**
	 * Return a new Record from s_script table with all pattern setted.
	 * 
	 * @param act - action (for s_scaction field)
	 * @param stype - scrip tipe (for s_sctype field)
	 * @param srcd - Source record from who its retrive the fields names and values for replae on pattern (for
	 *        s_scscript field)
	 * @param patt - source pattern for variable replacement
	 * 
	 * @return Record
	 */
	public static Record getNewScriptRecord(String act, String stype, Record srcd, String patt) {
		DBAccess dba = ConnectionManager.getAccessTo("S_SCRIPT");
		Vector slist = dba.search(null, null);
		int or = slist.size();
		if (or > 0) {
			Record sr = (Record) slist.elementAt(or - 1);
			or = (Integer) sr.getFieldValue("s_scstep");
		}
		String script = "";
		String tmp = "";
		// format the pattern (groovy)
		if (stype.equals("Groovy")) {
			String tn = (String) srcd.getFieldValue("table_name");
			Record ttmod = ConnectionManager.getAccessTo(tn).getModel();
			for (int c = 0; c < ttmod.getFieldCount(); c++) {
				if (ttmod.getFieldValue(c) instanceof Double) {
					tmp += SLE_DIVIDE_AND_ROUND + "\n";
					tmp = tmp.replace("${field}", ttmod.getFieldName(c));
				}
			}
			// if no double field was found, show empty 
			if (tmp.length() > 0) {
				script = patt.replace("${divideAndRoundDecimal}", tmp.substring(0, tmp.length() - 1));
			} else {
				script = patt.replace("${divideAndRoundDecimal}", "");
			}
		}

		if (stype.equals("Sql")) {
			script = TStringUtils.format(patt, srcd);
		}

		Record scmod = dba.getModel();
		scmod.setFieldValue("s_scstep", or + 1);
		scmod.setFieldValue("s_scaction", act);
		scmod.setFieldValue("s_sctype", stype);
		scmod.setFieldValue("s_scstatus", "active");
		scmod.setFieldValue("s_sccnname",
				DBTConnectionManager.getSourceDB().getConectionRecord().getFieldValue("t_cnname"));
		scmod.setFieldValue("s_sctable", srcd.getFieldValue("table_name"));
		scmod.setFieldValue("s_scscript", script);
		return scmod;
	}
}
