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

import javax.swing.*;

import action.*;
import core.*;

public class ImportFromFileAction extends TAbstractAction implements PropertyChangeListener {

	protected TProgressMonitor progressPanel;
	private JDialog dialog;

	public ImportFromFileAction(ActionPerformer per) {
		super(TABLE_SCOPE);
		supplier = per;
		this.dialog = null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		UIComponentPanel uicp = supplier.getUIFor(this);
		if (uicp != null) {
			uicp.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
			setDimentionFactor(.5);
			dialog = getDialog(uicp, "action.ImportFromFileAction");
			dialog.setVisible(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (act instanceof ImportFromFile) {
			dialog.dispose();
			((UIListPanel) supplier).freshen();
		}
	}
}
