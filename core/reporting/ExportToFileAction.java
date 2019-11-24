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

import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;
import core.tasks.*;

public class ExportToFileAction extends TAbstractAction implements PropertyChangeListener {

	private ExportParameters exportToFile;
	private JDialog dialog;
	private String noFlields;
	private Exportable supplier;
	private Class class1;
	private Hashtable addparm;

	/**
	 * new instance.
	 * 
	 * @param sup - instance of {@link Exportable} for retrive servicereques
	 * @param nfld - list of "no include" fields. "" means all fields are exportables
	 */
	public ExportToFileAction(Exportable sup, String nfld) {
		super(TABLE_SCOPE);
		this.supplier = sup;
		this.noFlields = nfld;
		this.addparm = new Hashtable();
	}

	public void addParameter(Object key, Object value) {
		addparm.put(key, value);
	}
	public ExportToFileAction(Exportable sup, String nfld, Class ins) {
		this(sup, nfld);
		this.class1 = ins;
	}
	
	public void setNoFields(String nfld) {
		this.noFlields = nfld;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// class for newinstance is setted ??
		if (class1 != null) {
			try {
				Constructor c = class1.getConstructor(Exportable.class, String.class);
				exportToFile = (ExportParameters) c.newInstance(supplier, noFlields);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			exportToFile = new ExportParameters(supplier, noFlields);
		}
		exportToFile.setFields(addparm);
		exportToFile.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(exportToFile, "action.ExportToFileAction");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();

		if (act instanceof CancelAction) {
			dialog.dispose();
		}

		if (act instanceof AceptAction) {
			Hashtable ht = exportToFile.getFields();
			ExportTask et = new ExportTask(ht);
			TTaskManager.submitRunnable(et, null, false);
			//et.run();
			dialog.dispose();
		}
	}
}
