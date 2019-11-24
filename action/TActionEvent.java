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
 * Copyright (c) QQ - All right reserved

 */
package action;

import java.awt.event.*;

/** sub clase de <code>ActionEvent</code> que permite almacenar datos o secuenca de acciones.
 * la accion por omision es del tipo <code>ActionEvent.ACTION_PERFORMED</code>
 * 
 * Si el objeto pasado como argumento al momento de construir esta clase, es una instancia de
 * <code>AppAbstractAction</code> el comando sera el obtenido por 
 * <code>source.getValue(AppAbstractAction.ACTION_COMMAND_KEY)</code> que sera el
 * ID de ResourceBundle que se uso para describir la accion
 */
public class TActionEvent extends ActionEvent {
	
	private Object data;
	private RedirectAction redirect;
	
	/** nueva instancia del tipo <code>ActionEvent.ACTION_PERFORMED</code> y comando 
	 * ID de resource bundle
	 * 
	 * @param source - objeto que genera el evento
	 */
	public TActionEvent(Object source) {
		super(source, ActionEvent.ACTION_PERFORMED, 
			(source instanceof TAbstractAction) ? 
			((TAbstractAction) source).getValue(TAbstractAction.NAME_ID).toString() :
			null);
	}
    public TActionEvent(Object source, int id, String command) {
        super(source, id, command, 0);
    }
	
	/**
	 * @return Returns the data.
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * @param data The data to set.
	 */
	public void setData(Object data) {
		this.data = data;
	}
	/**
	 * @return Returns the redirect.
	 */
	public RedirectAction getRedirectAction() {
		return redirect;
	}
	/**
	 * @param redirect The redirect to set.
	 */
	public void setRedirectAction(RedirectAction redirect) {
		this.redirect = redirect;
	}
}
