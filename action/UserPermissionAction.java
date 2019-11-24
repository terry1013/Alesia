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

import core.*;

/**
 * dialogo para mantener autorizaciones a usuarios y grupos
 * 
 */
public class UserPermissionAction extends EditRecord implements NoActionForSpecialRecord {

	/**
	 * nueva accion
	 * 
	 */
	public UserPermissionAction(ActionPerformer s) {
		super(s);
		setName("action.user.autorization");
		setIcon("lock_open");
		setToolTip("ttaction.user.autorization");
	}
}
