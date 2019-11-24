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

import core.*;

/**
 * este subgrupo de acciones solo modifican la propiedad UIComponentPanel.ACTION_PERFORMED para las clases interezadas,
 * pasando como nuevo valor esta instancia. generalmente, los interezados son otras acciones que redireccionaran a si
 * vez el flujo del proceso hacia otro lugar
 * 
 */
public class RedirectAction extends TAbstractAction {

	private UIComponentPanel uipanel;

	public RedirectAction(String cls, UIComponentPanel uip) {
		super(TAbstractAction.NO_SCOPE);
		this.uipanel = uip;
		setDefaultValues(cls);
	}
	public RedirectAction(UIComponentPanel uip) {
		super(TAbstractAction.NO_SCOPE);
		this.uipanel = uip;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
//		uipanel.firePropertyChange(TConstants.ACTION_PERFORMED, null, this);
		uipanel.putClientProperty(TConstants.ACTION_PERFORMED, null);
		uipanel.putClientProperty(TConstants.ACTION_PERFORMED, this);
	}	
}
