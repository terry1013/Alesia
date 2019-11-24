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
package core.reporting;

import gui.*;

import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.csv.*;

import action.*;
import core.*;
import core.datasource.*;

public class ImportFromFile extends UIListPanel implements PropertyChangeListener {

	private String columnModelDef;
	private Vector<Record> tempBuffer;
	private Record recordModel;
	private String fileColumns;
	private File csvInputFile;
	private TAbstractAction saveAction, refreshAction;

	public ImportFromFile(Record rmod, String coldeff) {
		super("title_imporFromfile");
		this.recordModel = rmod;
		this.columnModelDef = coldeff;
		this.tempBuffer = new Vector<Record>();
		this.saveAction = new SaveAction(this);
		saveAction.setEnabled(false);
		this.refreshAction = new TAbstractAction(TAbstractAction.TABLE_SCOPE) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				validateCSVFile();
			}
		};
		refreshAction.setIcon("RefreshAction");
		refreshAction.setToolTip("ttaction.RefreshAction");

		setToolBar(false, new NativeFileChooser(NativeFileChooser.OPEN_STYLE, this), refreshAction, saveAction);
		getJTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		addPropertyChangeListener(this);

		String fldl = "<ol>";
		File f = TResources.getFile(columnModelDef + ".csv");
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String lin = br.readLine();
			while ((lin = br.readLine()) != null) {
				fldl += "<li>" + lin.split("[,]")[0];
			}
			fldl += "</ol>";
			setMessage("inputfile.msg01", fldl);
			setView(TABLE_VIEW);
			getToolBar().setVisible(true);
		} catch (Exception e) {
			setMessage("inputfile.msg06", f);
			SystemLog.logException(e);
		}

	}
	@Override
	public void init() {

	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof DisplayRecord) {
			pane = new AuditLogRecord(getRecord());
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object nval = evt.getNewValue();
		String prp = evt.getPropertyName();
		if (prp.equals(TConstants.FILE_SELECTED)) {
			csvInputFile = (File) evt.getNewValue();
			validateCSVFile();
		}
		if (nval == saveAction) {
			importCSVRecors();
		}
	}

	private void importCSVRecors() {
		DBAccess dba = ConnectionManager.getAccessTo(recordModel.getTableName());
		for (Record rcd : tempBuffer) {
			dba.write(rcd);
		}
		firePropertyChange(TConstants.ACTION_PERFORMED, null, this);
	}

	private void validateCSVFile() {
		Iterable<CSVRecord> inrlst = null;
		try {
			Reader in = new FileReader(csvInputFile);
			inrlst = (new CSVParser(in, CSVFormat.EXCEL.withHeader()).getRecords());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		int err = validateRecord(inrlst);
		// error found, show errors in system log
		ServiceRequest sr = null;
		getJTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (err > 0) {
			String wc = "t_sluserid = '" + Session.getUserName() + "' AND t_slflag = 'ie'";
			sr = new ServiceRequest(ServiceRequest.DB_QUERY, "t_system_log", wc);
			setIconParameters("0;;t_sltype");
			setColumns("t_slmessage");
			saveAction.setEnabled(false);

		} else {
			sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", tempBuffer);
			sr.setParameter(ServiceResponse.RECORD_MODEL, ConnectionManager.getAccessTo(recordModel.getTableName())
					.getModel());
			setIconParameters("-1;aa");
			setColumns(fileColumns);
			saveAction.setEnabled(true);
		}
		setServiceRequest(sr);

		/*
		 * File f = (File) evt.getNewValue(); Record rcon =
		 * ConnectionManager.getAccessTo("t_connections").exist("t_cnname = 'CsvJdbc'"); String u = (String)
		 * rcon.getFieldValue("t_cnurl"); // rcon.setFieldValue("t_cnurl", u.replace("<filename>",
		 * f.getAbsolutePath())); rcon.setFieldValue("t_cnurl", u.replace("<filename>", f.getParent()));
		 * ConnectionManager.connect(rcon); String tn = "CsvJdbc." + f.getName().split("[.]")[0]; Record rm =
		 * ConnectionManager.getAccessTo(tn).getModel(); ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY,
		 * tn, null); sr.setParameter(ServiceRequest.ORDER_BY, "ficha"); setServiceRequest(sr);
		 */

	}

	/**
	 * validate the content of csv file against the column definition file. this methos store the valid record into a
	 * buffer and record in system log file any error found in input file
	 * 
	 * @param infile - record list parsed from imput file
	 * 
	 * @return number of error found.
	 */
	private int validateRecord(Iterable<CSVRecord> ircdlist) {
		// clear all previous log for import flag
		SystemLog.clearLogByFlag("ie");
		// column definition
		Iterable<CSVRecord> coldefl = null;
		try {
			Reader in = new FileReader(TResources.getFile(columnModelDef + ".csv"));
			coldefl = (new CSVParser(in, CSVFormat.EXCEL.withHeader()).getRecords());
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		SimpleDateFormat dfomat = null;
		tempBuffer.clear();
		int line = 0;
		int error = 0;
		int coldeflen = 0;
		fileColumns = "";
		for (CSVRecord ircd : ircdlist) {
			Record frcd = new Record(recordModel);
			line++;
			coldeflen = 0;
			// premature return if error > coldefl
			if (error > coldeflen) {
				SystemLog.log("inputfile.msg05", "ie", "", error);
				return error;
			}
			for (CSVRecord cdr : coldefl) {
				coldeflen++;
				Object iobj = null;
				String siobj = null;
				String fieldn = null;
				// contain field name
				try {
					fieldn = cdr.get("field");
					ircd.get(fieldn);
				} catch (Exception e) {
					// if field is mandatory, log error
					if (cdr.get("mandatory").equals("true")) {
						SystemLog.log("inputfile.msg02", "ie", "", line, cdr.get("field"));
						error++;
					}
					continue;
				}
				// value class.
				try {
					String cls = cdr.get("class");
					// String class by default
					iobj = ircd.get(fieldn);
					if (cls.equals("Integer")) {
						iobj = Integer.parseInt(ircd.get(fieldn));
					}
					if (cls.equals("Double")) {
						iobj = Double.parseDouble(ircd.get(fieldn));
					}
					if (cls.equals("Date")) {
						// date may be not present
						dfomat = new SimpleDateFormat(cdr.get("format"));
						Date d = iobj.equals("") ? TStringUtils.ZERODATE : dfomat.parse((String) iobj);
						iobj = new java.sql.Date(d.getTime());
					}
				} catch (Exception e) {
					SystemLog.log("inputfile.msg03", "ie", "", line, cdr.get("field"), cdr.get("class"),
							cdr.get("format"));
					error++;
					continue;
				}
				// valid value
				siobj = ircd.get(fieldn);
				boolean vvb = true;
				String vv = cdr.get("valid values");
				if (!vv.equals("")) {
					vvb = false;
					String[] vvlst = vv.split(";");
					for (String vvi : vvlst) {
						vvb = (siobj.equals(vvi)) ? true : vvb;
					}
				}
				if (vvb == false) {
					SystemLog.log("inputfile.msg04", "ie", "", line, cdr.get("field"), cdr.get("valid values"));
					error++;
					continue;
				}
				// no problem? add field
				String tf = cdr.get("target_field");
				fileColumns += tf + ";";
				frcd.setFieldValue(tf, iobj);
			}
			tempBuffer.add(frcd);
		}
		fileColumns = fileColumns.substring(0, fileColumns.length() - 1);
		return error;
	}
}
