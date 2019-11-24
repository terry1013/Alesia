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
package gui.datasource;

import java.awt.event.*;
import javax.swing.*;
import action.*;

/**
 * presenta dialogo para gestion de drivers de origenes de datos
 * 
 */
public class DriverManagerAction extends TAbstractAction {

	public DriverManagerAction() {
		super(TAbstractAction.NO_SCOPE);
		setDefaultValues("DriverManagerAction");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JDialog dialog = getDialog(new TDriverDialog(), "action.DriverManagerAction.title");
		dialog.setVisible(true);
	}
}
