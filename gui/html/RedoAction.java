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

class RedoAction extends AbstractAction {

	private HTMLEditor editor;

	public RedoAction(HTMLEditor he) {
		super("Redo");
		this.editor = he;
		setEnabled(false);
		putValue(Action.SMALL_ICON, HTMLEditor.getIcon("redo16"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		editor.undo.redo();
		update();
		editor.undoAction.update();
	}

	protected void update() {
		if (editor.undo.canRedo()) {
			setEnabled(true);
			putValue(Action.SHORT_DESCRIPTION, editor.undo.getRedoPresentationName());
		} else {
			setEnabled(false);
			putValue(Action.SHORT_DESCRIPTION, editor.getString("Redo"));
		}
	}
}
