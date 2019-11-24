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



/**
 * simple sql script executor. this class try exit execution if any error is found.
 * 
 * @author terry
 * 
 */
public class ScriptRunner {

	private Connection connection;
	private boolean autoCommit;
	private String delimiter = ";";
	private boolean fullLineDelimiter = false;

	/**
	 * new instance
	 * 
	 * @param connection - connection to database
	 * 
	 * @param autoCommit - whether the executed execution is autocomit or not
	 */
	public ScriptRunner(Connection connection, boolean autoCommit) {
		this.connection = connection;
		this.autoCommit = autoCommit;
	}

	/**
	 * execute the script.
	 * 
	 * @param reader script source
	 * 
	 * @throws Exception - if somthing is wrong
	 */
	public void runScript(Reader reader) throws Exception {
		try {
			StringBuffer command = null;
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					SystemLog.info(trimmedLine);
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
					// Do nothing
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
					// Do nothing
				} else if (!fullLineDelimiter && trimmedLine.endsWith(delimiter) || fullLineDelimiter
						&& trimmedLine.equals(delimiter)) {
					command.append(line.substring(0, line.lastIndexOf(delimiter)));
					command.append(" ");

					ExecuteSQL(command.toString());
					command = new StringBuffer();
				} else {
					command.append(line);
					command.append(" ");
				}
			}
			if (!autoCommit) {
				connection.commit();
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
			connection.rollback();
			throw e;
		}
	}

	/**
	 * execute the given sql statement
	 * 
	 * @param command - sql to execute
	 * 
	 * @throws Exception
	 */
	private void ExecuteSQL(String command) throws Exception {
		SystemLog.info(command);
		Statement statement = connection.createStatement();
		boolean hasResults = false;
		hasResults = statement.execute(command);

		if (autoCommit && !connection.getAutoCommit()) {
			connection.commit();
		}

		ResultSet rs = statement.getResultSet();
		if (hasResults && rs != null) {
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			String name = "";
			for (int i = 0; i < cols; i++) {
				name += md.getColumnLabel(i) + "\t";
			}
			SystemLog.info(name);
			while (rs.next()) {
				String value = "";
				for (int i = 0; i < cols; i++) {
					value += rs.getObject(i) + "\t";
				}
				SystemLog.info(value);
			}
		}

		command = null;
		statement.close();
		Thread.yield();
	}
}
