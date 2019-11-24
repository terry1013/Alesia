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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */
package action;

import gui.*;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import core.*;


/** Presenta el editor de direcciones dentro de un dialogo de entrada
 * 
 */
public class OpenAddressEditor extends TAbstractAction implements PropertyChangeListener {
	
	private AddressEditor editor;
	private JDialog dialog;

	/** nueva accion de edicion.
	 * 
	 * @param ae - editor
	 * @param nr - =true si es u registro nuevo. Usado para determinar el comando
	 * que alterara la informacion de la tabla
	 */	
	public OpenAddressEditor(AddressEditor ae) {
		super(null, "Address", TAbstractAction.NO_SCOPE, "ttd03");
		this.editor = ae;
		this.dialog = null;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		dialog = getDialog(editor, "d03");
		editor.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog.setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		TAbstractAction aa = (TAbstractAction) evt.getNewValue();
		if (!(aa instanceof CancelAction)) {
			editor.done(aa);
		}
		dialog.dispose();
	}
}
