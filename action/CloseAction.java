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

/** Accion estandar para boton <code>Cerrar</code>. esta accion es similar a cancelar, exeptuando que
 * indica al usuario que no cancela las operaciones efectuadas dentro del dialogo. solo lo cierra 
 * 
 */
public class CloseAction extends RedirectAction implements DefaultCancelAction {
	
	/** nueva accion
	 * 
	 * @param ca - accion de para confirmacion
	 */
	public CloseAction(UIComponentPanel uip) {
		super(uip);
	}	
}

