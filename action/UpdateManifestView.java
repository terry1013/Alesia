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
/* 
 * Copyright (c) 2003 Arnaldo Fuentes. Todos los derechos reservados.
 */

package action;

import gui.*;

import java.awt.event.*;
import java.beans.*;
import java.io.*;

import javax.swing.*;


import core.*;
import core.datasource.*;


/** Accion que presenta el visor de manifiesto de ptf.
 * 
 */
public class UpdateManifestView extends TAbstractAction implements PropertyChangeListener {
	
	private JDialog dialog;
	
	/** nueva accion
	 * 
	 */
	public UpdateManifestView() {
		super(TAbstractAction.NO_SCOPE);
		this.dialog = null;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		try {
			File f = new File(SystemVariables.getStringVar("updateManifest"));
			if (f.exists()) {
				ManifestViewer ptfv = new ManifestViewer("InstaledUpdate");
				ptfv.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
				dialog = getDialog(ptfv, "update.update");
				dialog.setVisible(true);		
			} else {
				JOptionPane.showMessageDialog(null, TStringUtils.getString("update.ms02"));
			}
		} catch(Exception e) {
			// no deberia
			SystemLog.logException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		dialog.dispose();
	}	
}
