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

import javax.swing.*;

import core.*;

/**
 * this acction will be migrated to generic record actions.
 * <p>
 * request for AbstractDataInput
 * <p>
 * show it
 * <p>
 * do nothing more (unless actionperformed2) are override
 */
@Deprecated
public class EditRecord extends TAbstractAction implements PropertyChangeListener, DefaultDobleClicAction {

	private AbstractDataInput rdInput;
	private JDialog dialog;

	/**
	 * schedule to deprecate ??
	 * 
	 * @param s
	 */
	public EditRecord(ActionPerformer s) {
		super(TAbstractAction.RECORD_SCOPE);
		this.supplier = s;
		this.messagePrefix = "action.EditRecord.";
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		rdInput = (AbstractDataInput) supplier.getUIFor(this);
		if (rdInput != null) {
			rdInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
			dialog = getDialog(rdInput, messagePrefix + "title");
			dialog.setVisible(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		TActionEvent ae = new TActionEvent(this);
		ae.setData(rdInput);
		ae.setRedirectAction((RedirectAction) evt.getNewValue());
		boolean fl = true;
		fl = supplier.executeAction(ae);
		if (fl) {
			dialog.dispose();
		}
	}
}
