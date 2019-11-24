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

/**
 * Action intended for save parameters from user UI 
 *  
 * @author terry
 *
 */

public class SaveAction extends RedirectAction implements DefaultAceptAction {
	
	public SaveAction(UIComponentPanel uip) {
		super(uip);
	}
}
