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

import com.alee.laf.checkbox.*;

import core.*;
import core.datasource.*;

public class TJCheckBoxTreeCellRenderer extends TDefaultTreeCellRenderer {
	private WebCheckBox checkBox;
	private String fieldName;
	private Color textForeground, textBackground;

	/**
	 * new instance
	 * 
	 * @param in - field icon name
	 * @param no - node
	 * @param na - name
	 * @param bfn - boolean field to set/unset
	 */
	public TJCheckBoxTreeCellRenderer(String in, String no, String na, String bfn) {
		super(in, no, na);
		this.checkBox = new WebCheckBox();
		checkBox.setAnimated(false);

		// dado que es creado directamente por TAbstractTree, no deberia ser null
		this.fieldName = bfn;

		Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		checkBox.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");

	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
		Record rcd = (Record) ((TEntry) dmtn.getUserObject()).getKey();

		checkBox.setText(getText());
		checkBox.setEnabled(tree.isEnabled());
		// checkBox.setSelected(false);

		if (sel) {
			// checkBox.setForeground(selectionForeground);
			// checkBox.setBackground(selectionBackground);
		} else {
			checkBox.setForeground(textForeground);
			checkBox.setBackground(textBackground);
		}
		boolean s = (Boolean) rcd.getFieldValue(fieldName);
		checkBox.setSelected(s);
		// FIXME: temporal size to try show all text !! fix this shit !!!!!!
		checkBox.setPreferredSize(new Dimension(250, getPreferredSize().height));
		return leaf ? checkBox : this;
	}
}
