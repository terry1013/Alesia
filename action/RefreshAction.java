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
import gui.tree.*;

import java.awt.event.*;

import javax.swing.*;

public class RefreshAction extends TAbstractAction {

	private UIComponentPanel panel;
	public RefreshAction(UIComponentPanel uip) {
		super(TABLE_SCOPE);
		this.panel = uip;
		putValue(TAbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (panel instanceof UIListPanel) {
			((UIListPanel) panel).freshen();
		}
		if (panel instanceof TAbstractTree) {
			((TAbstractTree) panel).freshen();
		}
	}
}
