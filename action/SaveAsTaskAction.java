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
package action;

import gui.*;

import java.awt.event.*;
import java.beans.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

/**
 * Action to save a plugin parameter as a internal task. this action gatter all information necesari for futher task
 * execution.
 * 
 */
public class SaveAsTaskAction extends TAbstractAction implements PropertyChangeListener {

	private AbstractDataInput teditor;
	private JDialog dialog;
	private DBAccess access;
	private TaskRecord taskRecord;

	private String icon;
	private String ttask;

	/**
	 * new instance.
	 * 
	 * @param adi - instance of {@link AbstractDataInput} who collect the task information
	 * @param ti - icon name for this task
	 * @param tt - class name instance of {@link TRunnable}
	 */
	public SaveAsTaskAction(AbstractDataInput adi, String ti, String tt) {
		super("action.save.task", "gear_add", RECORD_SCOPE, "ttaction.save.task");
		this.teditor = adi;
		this.icon = ti;
		this.ttask = tt;
		this.access = ConnectionManager.getAccessTo("T_TASKS");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record rm = access.getModel();
		Hashtable parms = new Hashtable();

		if (teditor instanceof AbstractRecordDataInput) {
			Record r = ((AbstractRecordDataInput) teditor).getRecord();
			for (int i = 0; i < r.getFieldCount(); i++) {
				parms.put(r.getFieldName(i), r.getFieldValue(i));
			}
		} else {
			parms = teditor.getFields();
		}
		ImageIcon ii = TResources.getIcon(icon);	
		rm.setFieldValue("t_tacode", TPreferences.getByteArrayFromObject(parms));
		rm.setFieldValue("t_taicon", TPreferences.getBytearrayFromImage(ii));
		rm.setFieldValue("t_tattask", ttask);
		rm.setFieldValue("t_taid", TStringUtils.getUniqueID());
		// rm.setFieldValue("t_tastatus", "enab");
		rm.setFieldValue("t_tadate_time", new Timestamp(System.currentTimeMillis()));

		this.taskRecord = new TaskRecord(rm, true);
		taskRecord.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(taskRecord, "action.save.task");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (act instanceof AceptAction) {
			Record ta = taskRecord.getRecord();
			access.add(ta);
			// TODO: fire properti to update tasklist record list
			// DockingContainer.getInstance().fireProperty(src, pn, nval)
			// dispose parent dialog
			JDialog dl = TAbstractAction.getActiveJDialog(teditor.getClass().getName());
			dl.dispose();
		}
		dialog.dispose();
	}
}
