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
import javax.swing.event.*;
import javax.swing.tree.*;

public abstract class TAbstractTreeTableModel implements TTreeTableModel {
	protected Object root;
	protected EventListenerList listenerList = new EventListenerList();

	private static final int CHANGED = 0;
	private static final int INSERTED = 1;
	private static final int REMOVED = 2;
	private static final int STRUCTURE_CHANGED = 3;

	public TAbstractTreeTableModel() {
		this(null);
	}
	public TAbstractTreeTableModel(Object root) {
		this.root = root;
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	/**
	 * Die Methode wird normalerweise nicht aufgerufen.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		return 0;
	}

	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	private void fireTreeNode(int changeType, Object source, Object[] path, int[] childIndices, Object[] children) {
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {

				switch (changeType) {
					case CHANGED :
						((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
						break;
					case INSERTED :
						((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
						break;
					case REMOVED :
						((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
						break;
					case STRUCTURE_CHANGED :
						((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
						break;
					default :
						break;
				}

			}
		}
	}

	protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		fireTreeNode(CHANGED, source, path, childIndices, children);
	}

	protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
		fireTreeNode(INSERTED, source, path, childIndices, children);
	}

	protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
		fireTreeNode(REMOVED, source, path, childIndices, children);
	}

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		fireTreeNode(STRUCTURE_CHANGED, source, path, childIndices, children);
	}

}
