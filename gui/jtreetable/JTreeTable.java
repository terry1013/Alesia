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
package gui.jtreetable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.alee.laf.tree.*;

import core.*;
import core.datasource.*;

/**
 * This example shows how to create a simple JTreeTable component, by using a JTree as a renderer (and editor) for the
 * cells in a particular column in the JTable.
 * 
 * @version 1.2 10/27/98
 * 
 * @author Philip Milne
 * @author Scott Violet
 */
public class JTreeTable extends JTable {
	/** A subclass of JTree. */
	protected TreeTableCellRenderer tree;
	protected AbstractTreeTableModel treeTableModel;

	public JTreeTable(AbstractTreeTableModel treeTableModel) {
		super();
		this.treeTableModel = treeTableModel;

		// Create the tree. It will be used as a renderer and editor.
		// TDefaultTreeCellRenderer ren = new TDefaultTreeCellRenderer("src_file", "node", "name", null);

		tree = new TreeTableCellRenderer(treeTableModel);

		// Install a tableModel representing the visible rows in the tree.
		super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

		// Force the JTable and JTree to share their row selection models.
		ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());

		// Install the tree editor renderer and editor.
		// TDefaultTableCellRenderer ren = new TDefaultTableCellRenderer()
		setDefaultRenderer(TreeTableModel.class, tree);
		// setDefaultRenderer(String.class, renderer)
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

		// No grid.
		setShowGrid(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setColumnSelectionAllowed(true);
		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));
		// getColumnModel().getColumn(0).setPreferredWidth(300);
		setRowHeight(18);

		// And update the height of the trees row to match that of
		// the table.
		if (tree.getRowHeight() < 1) {
			// Metal looks better like this.
			setRowHeight(18);
		}
	}

	public TreeTableCellRenderer getTreeTableCellRenderer() {
		return tree;
	}

	public Record[] getRecords() {
		int[] ridx = getSelectedRows();
		Record[] rcds = new Record[ridx.length];
		for (int rc = 0; rc < ridx.length; rc++) {
			// rcds[rc] = sortableModel.getRecordAt(ridx[rc]);
		}
		return rcds;
	}

	/**
	 * Overridden to message super and forward the method to the tree. Since the tree is not actually in the component
	 * hieachy it will never receive this unless we forward it in this manner.
	 */
	public void updateUI() {
		super.updateUI();
		if (tree != null) {
			tree.updateUI();
		}
		// Use the tree's default foreground and background colors in the
		// table.
		LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
	}

	/*
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to paint the editor. The UI currently uses
	 * different techniques to paint the renderers and editors and overriding setBounds() below is not the right thing
	 * to do for an editor. Returning -1 for the editing row in this case, ensures the editor is never painted.
	 */
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 */
	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if (tree != null && tree.getRowHeight() != rowHeight) {
			tree.setRowHeight(getRowHeight());
		}
	}

	/**
	 * Returns the tree that is being shared between the model.
	 */
	public JTree getTree() {
		return tree;
	}

	/**
	 * A TreeCellRenderer that displays a JTree. WARING: this implementation is only for {@link AmountViewer}
	 */
	public class TreeTableCellRenderer extends WebTree implements TableCellRenderer, TreeSelectionListener {
		/** Last table/tree row asked to renderer. */
		protected int visibleRow;
		// private ExtendedJLabel formatter;

		public TreeTableCellRenderer(TreeModel model) {
			super(model);
			// this.formatter = new ExtendedJLabel();
			setRootVisible(false);
			// TODO: temporal for amountViewer
			addTreeSelectionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
			setCellRenderer(new TreeTableTreeCellRenderer("av_src_file"));
			setRolloverSelectionEnabled(false);
		}

		/**
		 * updateUI is overridden to set the colors of the Tree's renderer to match that of the table.
		 */
		public void updateUI() {
			super.updateUI();
			// Make the tree's cell renderer use the table's cell selection
			// colors.
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) {
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
				// For 1.1 uncomment this, 1.2 has a bug that will cause an
				// exception to be thrown if the border selection color is
				// null.
				// dtcr.setBorderSelectionColor(null);
				dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
			}
		}

		/**
		 * Sets the row height of the tree, and forwards the row height to the table.
		 */
		public void setRowHeight(int rowHeight) {
			if (rowHeight > 0) {
				super.setRowHeight(rowHeight);
				if (JTreeTable.this != null && JTreeTable.this.getRowHeight() != rowHeight) {
					JTreeTable.this.setRowHeight(getRowHeight());
				}
			}
		}

		/**
		 * This is overridden to set the height to match that of the JTable.
		 */
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, JTreeTable.this.getHeight());
		}

		/**
		 * Sublcassed to translate the graphics such that the last visible row will be drawn at 0,0.
		 */
		public void paint(Graphics g) {
			g.translate(0, -visibleRow * getRowHeight());
			super.paint(g);
		}

		/**
		 * TreeCellRenderer method. Overridden to update the visible row.
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (isSelected)
				setBackground(table.getSelectionBackground());
			else
				setBackground(table.getBackground());

			visibleRow = row;
			return this;
		}

		@Override
		public void valueChanged(TreeSelectionEvent tse) {
			TreePath tp = tse.getNewLeadSelectionPath();
			if (tp != null) {
				// JTreeTable.TreeTableCellRenderer ttcr = (JTreeTable.TreeTableCellRenderer) tse.getSource();
				// TreeCellRenderer tcr = ttcr.getCellRenderer();
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
				Record rcd = (Record) ((TEntry) dmtn.getUserObject()).getKey();
				String ttt = null;
				// dmtn.isLeaf();
				if (rcd.getFieldValue("av_src_file").equals("sle_planc_workforce")) {
					String eval = (String) rcd.getFieldValue("av_formula_eval");
					String expr = (String) rcd.getFieldValue("av_formula_expr");
					eval = eval.replaceAll("\n", "<br>");
					expr = expr.replaceAll("\n", "<br>");
					// String tip = "Evaluacion:\n" + eval + "\nExpresion:\n" + expr;
					ttt = "<html><b>Evaluacion:</b><br>" + eval + "<br><b>Expresion:</b><br>" + expr + "</html>";
					System.out.println(ttt);
					// TooltipManager.showOneTimeTooltip(this, null, tip, TooltipWay.trailing);
					// TooltipManager.setTooltip(this, tip, TooltipWay.trailing, 0);
				}
				setToolTipText(ttt);
				// TooltipManager.showOneTimeTooltip(this, null, ttt, TooltipWay.trailing);
				// TooltipManager.setTooltip(this, ttt, TooltipWay.trailing);

			}
		}
	}

	/**
	 * TreeTableCellEditor implementation. Component returned is the JTree.
	 */
	class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
			return tree;
		}

		/**
		 * Overridden to return false, and if the event is a mouse event it is forwarded to the tree.
		 * <p>
		 * The behavior for this is debatable, and should really be offered as a property. By returning false, all
		 * keyboard actions are implemented in terms of the table. By returning true, the tree would get a chance to do
		 * something with the keyboard events. For the most part this is ok. But for certain keys, such as left/right,
		 * the tree will expand/collapse where as the table focus should really move to a different column. Page up/down
		 * should also be implemented in terms of the table. By returning false this also has the added benefit that
		 * clicking outside of the bounds of the tree node, but still in the tree column will select the row, whereas if
		 * this returned true that wouldn't be the case.
		 * <p>
		 * By returning false we are also enforcing the policy that the tree will never be editable (at least by a key
		 * sequence).
		 */
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
					if (getColumnClass(counter) == TreeTableModel.class) {
						MouseEvent me = (MouseEvent) e;
						MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX()
								- getCellRect(0, counter, true).x, me.getY(), me.getClickCount(), me.isPopupTrigger());
						tree.dispatchEvent(newME);
						break;
					}
				}
			}
			return false;
		}

		public Object getCellEditorValue() {
			return null;
		}
	}

	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel to listen for changes in the ListSelectionModel
	 * it maintains. Once a change in the ListSelectionModel happens, the paths are updated in the
	 * DefaultTreeSelectionModel.
	 */
	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
		/** Set to true when we are updating the ListSelectionModel. */
		protected boolean updatingListSelectionModel;

		public ListToTreeSelectionModelWrapper() {
			super();
			getListSelectionModel().addListSelectionListener(createListSelectionListener());
		}

		/**
		 * Returns the list selection model. ListToTreeSelectionModelWrapper listens for changes to this model and
		 * updates the selected paths accordingly.
		 */
		ListSelectionModel getListSelectionModel() {
			return listSelectionModel;
		}

		/**
		 * This is overridden to set <code>updatingListSelectionModel</code> and message super. This is the only place
		 * DefaultTreeSelectionModel alters the ListSelectionModel.
		 */
		public void resetRowSelection() {
			if (!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					super.resetRowSelection();
				} finally {
					updatingListSelectionModel = false;
				}
			}
			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}

		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 */
		protected ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}

		/**
		 * If <code>updatingListSelectionModel</code> is false, this will reset the selected paths from the selected
		 * rows in the list selection model.
		 */
		protected void updateSelectedPathsFromSelectedRows() {
			if (!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				final Vector<Record> v = new Vector<Record>();
				try {
					// This is way expensive, ListSelectionModel needs an
					// enumerator for iterating.
					int min = listSelectionModel.getMinSelectionIndex();
					int max = listSelectionModel.getMaxSelectionIndex();

					clearSelection();
					if (min != -1 && max != -1) {
						for (int counter = min; counter <= max; counter++) {
							if (listSelectionModel.isSelectedIndex(counter)) {
								TreePath selPath = tree.getPathForRow(counter);

								DefaultMutableTreeNode dmtn = tree.getNodeForRow(counter);
								v.add((Record) ((TEntry) dmtn.getUserObject()).getKey());
								if (selPath != null) {
									addSelectionPath(selPath);
								}
							}
						}
					}
				} finally {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							firePropertyChange(TConstants.RECORD_SELECTED, null, v.toArray(new Record[v.size()]));
						}
					});
					updatingListSelectionModel = false;
				}
			}
		}

		/**
		 * Class responsible for calling updateSelectedPathsFromSelectedRows when the selection of the list changse.
		 */
		class ListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedPathsFromSelectedRows();
			}
		}
	}

	public class TreeTableTreeCellRenderer extends DefaultTreeCellRenderer {

		private String fieldIconN;

		public TreeTableTreeCellRenderer(String in) {
			this.fieldIconN = in;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			setVerticalTextPosition(JLabel.TOP);
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
			Record rcd = (Record) ((TEntry) dmtn.getUserObject()).getKey();
			setName(value.toString());
			// icono
			if (fieldIconN != null) {
				String inam = (String) rcd.getFieldValue(fieldIconN);
				ImageIcon ii = TResources.getSmallIcon(inam);
				setIcon(ii);
			}
			return this;
		}
	}
}
