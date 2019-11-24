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
package plugin.xdoc;

import gui.*;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import core.*;

import action.*;

public class XdocReportAction extends TAbstractAction implements PropertyChangeListener {

	private XdocReportDialog XdocReportDialog;
	private JDialog dialog;
	private UIListPanel listPanel;

	public XdocReportAction(UIListPanel uilp) {
		super("xdoc.combine.action", "/plugin/xdoc/xdocreport", RECORD_SCOPE, "ttxdoc.combine.action");
		this.listPanel = uilp;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		XdocReportDialog = new XdocReportDialog();
		XdocReportDialog.setBaseRecords(listPanel.getRecords());
		XdocReportDialog.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(XdocReportDialog, "xdoc.combine.txt01");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();

		if (act instanceof CancelAction) {
			dialog.dispose();
		}

		if (act instanceof AceptAction) {
			dialog.dispose();
			Hashtable ht = XdocReportDialog.getFields();
			final XDocReportTask ta = new XDocReportTask();
			ta.setTaskParameters(ht);
//			TTaskManager.submitInteractiveTask("/plugin/xdoc/data_gear", "xdoc.combine.txt01", ta);
		}
	}
}
