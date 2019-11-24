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

import java.awt.event.*;

import core.*;

/**
 * accion estandar para suprimir registro.
 * 
 */
public class DeleteRecord extends TAbstractAction implements NoActionForSpecialRecord {

	@Deprecated
	public DeleteRecord(ActionPerformer ap) {
		super(TAbstractAction.RECORD_SCOPE);
		supplier = ap;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		TActionEvent ae = new TActionEvent(this);
		supplier.executeAction(ae);
	}
}
