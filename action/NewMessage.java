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

import core.ActionPerformer;

/** accion designada para creacion de nuevo mensaje
 * 
 *
 */
public class NewMessage extends NewRecord {

	/** nueva instancia 
	 * 
	 * @param per - instanica de <code>ActionPerformer</code>
	 */
	public NewMessage(ActionPerformer per) {
		super(per);
		setIcon("newMsg");
		setName("i.n01");
		setToolTip("tti.n01");
	}
}
