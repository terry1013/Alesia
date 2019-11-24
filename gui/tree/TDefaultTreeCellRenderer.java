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
package gui.tree;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import core.*;
import core.datasource.*;

public class TDefaultTreeCellRenderer extends DefaultTreeCellRenderer {

	private String fieldIconN;
	private String[] fieldImageIcons;
	private Boolean separator = false;
	private String nodeIdField, nodeNameField;

	public TDefaultTreeCellRenderer(String in, String no, String na) {
		this.fieldIconN = in;
		this.nodeIdField = no;
		this.nodeNameField = na;
		this.separator = false;
		// 180220: multiples imageIcon Instances
		fieldImageIcons = null;
		if (in != null && in.contains(";")) {
			fieldImageIcons = in.split(";");
			fieldIconN = null;
		}
		/*
		 * selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor"); selectionForeground =
		 * UIManager.getColor("Tree.selectionForeground"); selectionBackground =
		 * UIManager.getColor("Tree.selectionBackground");
		 */
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		setVerticalTextPosition(JLabel.TOP);
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
		Object o = dmtn.getUserObject();
		Record rcd = (Record) ((TEntry) o).getKey();

		setText(rcd.getFieldValue(nodeIdField) + ": " + rcd.getFieldValue(nodeNameField));
		if (!separator) {
			setText(rcd.getFieldValue(nodeNameField).toString());
		}

		// custom defined icon. set th icon if the parameter is present. else, leave the icon setted in super method
		ImageIcon ii = null;
		// 280220: single icon name: the field value contain the icon name form file
		if (fieldIconN != null) {
			String inam = (String) rcd.getFieldValue(fieldIconN);
			ii = TResources.getSmallIcon(inam);
			setIcon(ii);
		}
		// 280220: multples icon: the field value contain the ImageIcon instance to show
		if (fieldImageIcons != null) {
			// is leaf node ? Tree.leafIcon
			if (leaf) {
				ii = (ImageIcon) rcd.getFieldValue(fieldImageIcons[2]);
			} else {
				// Tree.closedIcon or Tree.openIcon accordin to expanded value
				ii = expanded ? (ImageIcon) rcd.getFieldValue(fieldImageIcons[1]) : (ImageIcon) rcd
						.getFieldValue(fieldImageIcons[0]);
			}
			setIcon(ii);
		}
		return this;
	}

	/**
	 * set if this component must show the pare key: value thogether with a separator or only the value
	 * 
	 * @param ss - true or false
	 */
	public void showSeparator(boolean ss) {
		this.separator = ss;
	}
}
