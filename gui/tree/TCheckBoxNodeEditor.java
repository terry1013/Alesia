package gui.tree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.alee.laf.checkbox.*;

import core.*;
import core.datasource.*;

/**
 * implementation of {@link AbstractCellEditor} that give support to {@link TAbstractTree} when it build tree for data
 * and with {@link TConstants#TREE_BOOLEAN_FIELD}
 * 
 * @author terry
 * 
 */
public class TCheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	private TJCheckBoxTreeCellRenderer renderer;
	private WebCheckBox editor;
	private TEntry tEntry;
	private JTree tree;
	private String fieldName;

	/**
	 * new instance
	 * 
	 * @param jt - JTree
	 * @param in - icon field name
	 * @param no - node field
	 * @param na - descripcion field
	 * @param fn - boolean fiel
	 */
	public TCheckBoxNodeEditor(JTree jt, String in, String no, String na, String fn) {
		this.tree = jt;
		this.renderer = new TJCheckBoxTreeCellRenderer(in, no, na, fn);
		this.editor = new WebCheckBox();
		editor.setAnimated(false);
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// System.out.println(e.toString());
				fireEditingStopped();
			}
		};
		editor.addActionListener(al);
		this.fieldName = fn;
	}

	@Override
	public Object getCellEditorValue() {
		Record r = (Record) tEntry.getKey();
		r.setFieldValue(fieldName, editor.isSelected());
		// System.out.println(record.getFieldValue("name") + " ---> " + editor.isSelected());
		return tEntry;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row) {

		JCheckBox jcb = (JCheckBox) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		// copy properties
		editor.setForeground(jcb.getForeground());
		editor.setBackground(jcb.getBackground());
		editor.setText(jcb.getText());
		editor.setSelected(jcb.isSelected());
		editor.setPreferredSize(jcb.getPreferredSize());
		return editor;
	}

	@Override
	public boolean isCellEditable(EventObject event) {
		boolean returnValue = false;
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (path != null) {
				Object node = path.getLastPathComponent();
				if (node != null) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
					returnValue = dmtn.isLeaf();
					if (returnValue) {
						this.tEntry = (TEntry) dmtn.getUserObject();
						// System.out.println(record.getFieldValue("name"));
					}
				}
			}
		}
		return returnValue;
	}
}