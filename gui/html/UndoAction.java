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

import java.awt.event.*;

import javax.swing.*;

class UndoAction extends AbstractAction {
	
	private HTMLEditor editor;
	
	public UndoAction(HTMLEditor he) {
		super("Undo");
		this.editor = he;
		setEnabled(false);
		putValue(Action.SMALL_ICON, HTMLEditor.getIcon("undo16"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
			KeyEvent.CTRL_MASK));

	}

	public void actionPerformed(ActionEvent e) {
		editor.undo.undo();
		update();
		editor.redoAction.update();
	}

	protected void update() {
		if (editor.undo.canUndo()) {
			setEnabled(true);
			putValue(Action.SHORT_DESCRIPTION, editor.undo.getUndoPresentationName());
		} else {
			setEnabled(false);
			putValue(Action.SHORT_DESCRIPTION, editor.getString("Undo"));
		}
	}
}
