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

import java.awt.event.*;

import core.*;


/** Establecer como omision establece una marca en un registro seleccionado y lo establece como por
 * omision.
 * 
 */
public class SetAsDefault extends TAbstractAction {

	private String fieldN;

	/** nueva instancia
	 * 
	 * @param per - ejecutor
	 * @param fn - nombre de campo a establecer como dato para sopote base 
	 */
	public SetAsDefault(ActionPerformer per, String fn) {
		super("e11", "AsDefault", TAbstractAction.RECORD_SCOPE, "tte11");
		supplier = per;
		this.fieldN = fn;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		TActionEvent ae = new TActionEvent(this);
		ae.setData(fieldN);
		supplier.executeAction(ae);
	}
}
