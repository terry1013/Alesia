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
package core;

import java.io.*;
import java.sql.*;
import java.text.*;

import core.datasource.*;
import gui.*;

/**
 * TODO: end the implementation. all methods must migrato to {@link #log(String, String, String, Object...)} (i think
 * :()
 * 
 * @author terry
 * 
 */
public class SystemLog {

	private static MessageFormat messageFormat = new MessageFormat("");

	/**
	 * log exception on local log file
	 * 
	 * @param e - <code>Exception</code> to log
	 * @param sd - <code>true</code> to display exception dialog
	 */
	public static void logException1(Exception e, boolean sd) {
		Alesia.logger.error("", e);
		if (sd) {
			ExceptionDialog.showDialog(e);
		}
	}

	public static void severe(String msg) {
		Alesia.logger.error(msg);
	}

	public static void warning(String msg) {
		Alesia.logger.warn(msg);
	}

	public static void info(String msg) {
		Alesia.logger.info(msg);
	}

	public static void logException1(Exception e) {
		logException1(e, false);
	}

	/**
	 * append a new entry on main system log file. This method is intented for task trace to inform the user of task
	 * execution and other messages.
	 * 
	 * @param mid - message ID
	 * @param flag - flag
	 * @param det - aditional details
	 * @param dta - values for message sustitution
	 */
	public static void log(String mid, String flag, String det, Object... dta) {
		TError ae = new TError(mid);
		String msg = ae.getMessage();
		if (dta != null) {
			messageFormat.applyPattern(msg);
			msg = messageFormat.format(dta);
		}

		DBAccess dba = ConnectionManager.getAccessTo("t_system_log");
		Record logr = dba.getModel();
//		logr.setFieldValue("t_sluserid", Session.getUserName());
		logr.setFieldValue("t_sluserid", "terry");
		logr.setFieldValue("t_slnano", System.nanoTime());
		logr.setFieldValue("t_sldatetime", new Timestamp(System.currentTimeMillis()));
		logr.setFieldValue("t_slflag", flag);
		logr.setFieldValue("t_sltype", ae.getExceptionType());
		logr.setFieldValue("t_slmessage", msg);
		logr.setFieldValue("t_sldetail", det);
		dba.add(logr);
	}

	/**
	 * Log the execption into the log database file. This method is intended to task's trace. This metod store the
	 * {@link Exception#printStackTrace()} into the aditional information field.
	 * 
	 * TODO: maybe add to log file to keepit in sync???
	 * 
	 * @param mid - message id
	 * @param flag - flag
	 * @param ex - exception to log and retrive stacktrace.
	 */
	public static void log(String mid, String flag, Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.close();
		try {
			sw.close();
		} catch (IOException e) {
		}
		String det = sw.toString();
		String dta = ex.getMessage();
		log(mid, flag, det, dta);
	}

	public static void logException(Exception ex) {
		logException1(ex);
	}
}
