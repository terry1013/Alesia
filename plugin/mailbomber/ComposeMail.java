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
package plugin.mailbomber;

import java.awt.event.*;
import java.beans.*;

import action.*;
import core.*;
import core.datasource.*;
/**
 * the mesage body is either direct message:
 * <ul>
 * <li>template id is none and body has text on it
 * <li>whit template: the template parameter are selected. during email creation, the selected template is loades into
 * the message body and the user can edit values. these text are saved and will be used as source for variable
 * replacement. if the user need to reload the template, the text form the template are replaced
 * 
 * @author terry
 * 
 */
public class ComposeMail extends TAbstractAction implements PropertyChangeListener {

	private ComposeMailDialog composeMail;
	private Record mailRcd;
	
	public ComposeMail(AddressBookTree el) {
		super(RECORD_SCOPE);
		editableList = el;
		messagePrefix = "action.ComposeMail.";
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record[] rcds = editableList.getRecords();
		Record msgm = ConnectionManager.getAccessTo("m_messages").getModel();
		String mto = "";
		for (Record rcd : rcds) {
			mto += rcd.getFieldValue("m_abemail") + "; ";
		}
		mto = mto.substring(0, mto.length() - 2);
		msgm.setFieldValue(0, TStringUtils.getRecordId());
		msgm.setFieldValue("m_mid", mto);
		composeMail = new ComposeMailDialog(msgm, true);
		composeMail.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(composeMail, messagePrefix + "title", false, true);
//		dialog.getRootPane().setDefaultButton(null);
		dialog.setVisible(true);

	}
	@Override
	public void actionPerformed2() {
		mailRcd = composeMail.getRecord();
		boolean ok = ConnectionManager.getAccessTo(mailRcd.getTableName()).add(mailRcd);
		if (ok) {
			dialog.dispose();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		redirectAction = (RedirectAction) evt.getNewValue();
		// does nothing if cancel action
		if (redirectAction instanceof CancelAction) {
			dialog.dispose();
			return;
		}
		// save mail
		if (redirectAction instanceof AceptAction) {
			actionPerformed2();
		}
		if (redirectAction instanceof SaveAction) {
			// defaultDataInput.saveParameters();
		}
	}

}
