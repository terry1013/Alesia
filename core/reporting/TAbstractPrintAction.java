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

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import net.sf.jasperreports.engine.*;
import action.*;
import core.*;
import core.tasks.*;

/**
 * base implementation for reporting subsistem. This class contains all necessary steps required to print a report using
 * jaspereport library. subclass generaly needs only:
 * <ul>
 * <li>implements {@link #actionPerformed(java.awt.event.ActionEvent)} event to insert new input componets to
 * {@link ReportParameters} using {@link ReportParameters#insert(AbstractDataInput, String)}
 * <li>set the {@link #reportInstance} parameter to indicate the correct report instance who take care of print the
 * report generation process
 * </ul>
 * 
 * @author terry
 * 
 *  NOTE: maybe this action does't need tobe abstract !!
 */
public abstract class TAbstractPrintAction extends TAbstractAction implements PropertyChangeListener, TaskListener {

	protected JDialog dialog;
	protected ReportParameters defaultDataInput;
	protected Hashtable reportParameters;
	protected String reportName;
	protected Class reportInstance;

	public TAbstractPrintAction(String rn, String tid) {
		super(TAbstractAction.NO_SCOPE);
		setName(tid);
		this.reportName = rn;
		this.defaultDataInput = new ReportParameters(reportName);
		this.reportInstance = Report.class;
	}

	/**
	 * display the print dialog. Invoke this method after {@link #actionPerformed(java.awt.event.ActionEvent)}.
	 */
	protected void displayPrintDialog() {
		defaultDataInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(defaultDataInput, (String) getValue(NAME));
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		this.reportParameters = defaultDataInput.getFields();
		reportParameters.put(ReportParameters.REPORT_DESCRIPTION, getValue(NAME));

		if (act instanceof CancelAction) {
			dialog.dispose();
		}

		if (act instanceof AceptAction) {
			dialog.dispose();
			Report r = getReportInstance();
//			try { r.call(); } catch (Exception e) { e.printStackTrace(); }
			TTaskManager.submitCallable(r, null, true);
		}
		if (act instanceof SaveAction) {
			defaultDataInput.saveParameters();
		}

		if (act instanceof SelectPrinterAction) {
			reportParameters.put(ReportParameters.REPORT_GENERATE_ONLY, true);
			Report r = getReportInstance();
			TTaskManager.submitCallable(r, this, false);
		}
	}

	@Override
	public void taskDone(Future f) {
		try {
			if (!f.isCancelled()) {
				JasperPrint jp = (JasperPrint) f.get();
				if (JasperPrintManager.printReport(jp, true)) {
					dialog.dispose();
				}
			}
		} catch (Exception e) {
			// do nothing. error hs been processed in call method
		}
	}
	/**
	 * create the instance of {@link Report} stored in {@link #reportInstance} and set the report parameters entered by
	 * user.
	 * 
	 * @return Report
	 */
	private Report getReportInstance() {
		Report r = null;
		try {
			Constructor c = reportInstance.getConstructor(Hashtable.class);
			r = (Report) c.newInstance(reportParameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}
}
