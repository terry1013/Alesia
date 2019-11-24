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
 * accion estandar para crear una nueva entrada. esta accion es construida pasando como referencias una instancia de
 * <code>ActionPerformer</code> la cual es usada para comunicaciones diversas (solcitud de dialogo de entrada, ejecucion
 * de accion).
 * 
 */
@Deprecated
public class NewRecord extends TAbstractAction implements PropertyChangeListener {

	private AbstractDataInput rdInput;
	private JDialog dialog;

	/**
	 * nueva instancia
	 * 
	 * @param per - instancia de <code>ActionPerformer</code>
	 */
	public NewRecord(ActionPerformer per) {
		super(TAbstractAction.TABLE_SCOPE);
		this.rdInput = null;
		supplier = per;
		this.dialog = null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		rdInput = (AbstractDataInput) supplier.getUIFor(this);
		// podria ser nulo si no cumple requisitos
		if (rdInput != null) {
			rdInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
			dialog = getDialog(rdInput, "action.NewRecord.title");
			dialog.setVisible(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		TActionEvent ae = new TActionEvent(this);
		ae.setData(rdInput);
		ae.setRedirectAction((RedirectAction) evt.getNewValue());
		boolean fl = true;
		// if (evt.getNewValue() instanceof AceptAction) {
		fl = supplier.executeAction(ae);
		// }
		if (fl) {
			dialog.dispose();
		}
	}
}
