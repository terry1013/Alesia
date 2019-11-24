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

class PopupListener extends MouseAdapter {
	
	private HTMLEditor editor;
	private JPopupMenu popupMenu;
	
	public PopupListener(HTMLEditor he) {
		this.editor = he;
		this.popupMenu = new JPopupMenu();
		popupMenu.setFocusable(false);
		popupMenu.add(editor.jMenuItemUndo);
		popupMenu.add(editor.jMenuItemRedo);
		popupMenu.addSeparator();
		popupMenu.add(editor.jMenuItemCut);
		popupMenu.add(editor.jMenuItemCopy);
		popupMenu.add(editor.jMenuItemPaste);
	}
	
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
