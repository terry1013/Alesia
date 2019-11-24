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
package delete.gui.jtreetable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

public class TTreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	private JTree tree;
	private JTable table;

	public TTreeTableCellEditor(JTree tree, JTable table) {
		this.tree = tree;
		this.table = table;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
		return tree;
	}

	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent) {
			int colunm1 = 0;
			MouseEvent me = (MouseEvent) e;
			int doubleClick = 2;
			MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX()
					- table.getCellRect(0, colunm1, true).x, me.getY(), doubleClick, me.isPopupTrigger());
			tree.dispatchEvent(newME);
		}
		return false;
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

}
