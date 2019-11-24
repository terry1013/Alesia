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
package gui.html;

import javax.swing.event.*;

class UndoHandler implements UndoableEditListener {

	private HTMLEditor editor;
	
	public UndoHandler(HTMLEditor he) {
		this.editor = he;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		editor.undo.addEdit(e.getEdit());
		editor.undoAction.update();
		editor.redoAction.update();
	}
}
