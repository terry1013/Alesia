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
import core.datasource.*;

/**
 * test implementation for futher migration of all actions framework
 * 
 */
public class EditRecord2 extends TAbstractAction implements PropertyChangeListener, DefaultDobleClicAction {

	protected AbstractRecordDataInput dataInput;
	protected JDialog dialog;
	protected RedirectAction redirectAction;

	public EditRecord2() {
		super(TAbstractAction.RECORD_SCOPE);
		messagePrefix = "action.EditRecord.";
		// temp
		setDefaultValues("EditRecord");
	}
	
	public EditRecord2(EditableList el) {
		this();
		setEditableList(el);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		dataInput = (AbstractRecordDataInput) editableList.getUIFor(this);
		if (dataInput != null) {
			dataInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
			dialog = getDialog(dataInput, messagePrefix + "title");
			dialog.setVisible(true);
		}
	}

	@Override
	public void actionPerformed2() {
		Record rcd = dataInput.getRecord();
		boolean ok;
		if (allowWrite) {
			ok = true;
			ConnectionManager.getAccessTo(rcd.getTableName()).write(rcd);
		} else {
			ok = ConnectionManager.getAccessTo(rcd.getTableName()).update(rcd);
		}
		// dispose dialog if error ????
		// maybe apped record validation
		dialog.dispose();
		if (ok) {
			editableList.freshen();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		redirectAction = (RedirectAction) evt.getNewValue();
		// does nothing if cancel action
		if (redirectAction instanceof DefaultCancelAction) {
			dialog.dispose();
			return;
		}
		// perform edit
		if (redirectAction instanceof DefaultAceptAction) {
			actionPerformed2();
		}
	}
}
