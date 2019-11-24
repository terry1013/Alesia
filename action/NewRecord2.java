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

import core.*;
import core.datasource.*;

/**
 * test implementation for futher migration of all actions framework
 * 
 */
public class NewRecord2 extends TAbstractAction implements PropertyChangeListener {
	protected AbstractRecordDataInput dataInput;

	public NewRecord2(EditableList el) {
		super(TAbstractAction.TABLE_SCOPE);
		setIcon("NewRecord");
		editableList = el;
		messagePrefix = "action.NewRecord.";
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

	/**
	 * Standar new record validation
	 * 
	 * @return <code>true</code> if record can be adde to database
	 */
	public boolean validateNewRecord(Record rcd) {
		boolean ok = true;
		Record er = ConnectionManager.getAccessTo(rcd.getTableName()).exist(rcd);
		if (er != null) {
			ok = false;
			dataInput.showAplicationExceptionMsg("msg03");
		}
		return ok;
	}

	@Override
	public void actionPerformed2() {
		Record rcd = dataInput.getRecord();
		boolean ok = false;
		if (allowWrite) {
			ok = true;
			ConnectionManager.getAccessTo(rcd.getTableName()).write(rcd);
		} else {
			if (validateNewRecord(rcd)) {
				ok = ConnectionManager.getAccessTo(rcd.getTableName()).add(rcd);
			}
		}
		if (ok) {
			dialog.dispose();
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
