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

package gui.html;

import gui.*;

import java.awt.event.*;

import action.*;


/** <<< anterior
 * 
 */
public class PreviousAction extends TAbstractAction {

	private Navigator nav;

	/** Nueva instancia
	 * 
	 */
	public PreviousAction(Navigator nav) {
		super("a04", "Previous", TAbstractAction.NO_SCOPE, null);
		this.nav = nav;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ae.core.AppAbstractAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		nav.previous();
	}
}
