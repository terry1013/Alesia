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

import java.awt.Dimension;

import javax.swing.JTable;

public class TTreeTable extends JTable {

	private TTreeTableCellRenderer tree;

	public TTreeTable(TAbstractTreeTableModel treeTableModel) {
		super();

		// JTree erstellen.
		tree = new TTreeTableCellRenderer(this, treeTableModel);

		// Modell setzen.
		super.setModel(new TTreeTableModelAdapter(treeTableModel, tree));

		// Gleichzeitiges Selektieren fuer Tree und Table.
		TTreeTableSelectionModel selectionModel = new TTreeTableSelectionModel();
		tree.setSelectionModel(selectionModel); // For the tree
		setSelectionModel(selectionModel.getListSelectionModel()); // For the table

		// Renderer fuer den Tree.
		setDefaultRenderer(TTreeTableModel.class, tree);
		// Editor fuer die TreeTable
		setDefaultEditor(TTreeTableModel.class, new TTreeTableCellEditor(tree, this));

		// Kein Grid anzeigen.
		setShowGrid(false);

		// Keine Abstaende.
		setIntercellSpacing(new Dimension(0, 0));

	}
}
