package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ColumnSelectorControl extends JPanel {

	JButton bAddAll = new JButton(">>");
	JButton bAdd = new JButton("> ");
	JButton bRemove = new JButton("< ");
	JButton bRemoveAll = new JButton("<<");

	JTable allColumns = new JTable();
	JTable selectedColumns = new JTable();

	public ColumnSelectorControl() {
		this("");
	}
	public ColumnSelectorControl(String title) {
		super();
		setBorder(new TitledBorder(new EtchedBorder(), title));
		setLayout(new GridBagLayout());
		JScrollPane scroll = new JScrollPane(allColumns);
		add(scroll, new GridBagConstraints(0, 0, 1, 5, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 50, 0));

		add(bAddAll, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(bAdd, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		JLabel l = new JLabel(" ");
		l.setPreferredSize(new Dimension(10, 20));
		add(l, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 20, 0));
		add(bRemove, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(bRemoveAll, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		scroll = new JScrollPane(selectedColumns);
		add(scroll, new GridBagConstraints(2, 0, 1, 5, 5, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 5, 0, 0), 0, 0));

		initListeners();
	}

	public void setColumns(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		int cnt = columnModel.getColumnCount();
		Object[][] sourceData = new Object[cnt][1];
		for (int i = 0; i < cnt; i++) {
			sourceData[i][0] = table.getColumnName(i);
		}
		allColumns.setModel(new SelectionModel(sourceData, new String[]{"Column name"}));
		selectedColumns.setModel(new SelectionModel(new String[]{"Column name", "Sort order"}, 0));
		selectedColumns.getColumnModel().getColumn(1).setPreferredWidth(50);
	}

	protected void initListeners() {
		bAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = allColumns.getSelectedRow();
				if (selectedRow >= 0) {
					DefaultTableModel model = (DefaultTableModel) allColumns.getModel();
					Object colName = model.getValueAt(selectedRow, 0);
					model.removeRow(selectedRow);

					model = (DefaultTableModel) selectedColumns.getModel();
					model.addRow(new Object[]{colName, new Boolean(true)});
				}
			}
		});
		bAddAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) allColumns.getModel();
				int rowCount = model.getRowCount();
				for (int i = 0; i < rowCount; i++) {
					model = (DefaultTableModel) allColumns.getModel();
					Object colName = model.getValueAt(0, 0);
					model.removeRow(0);

					model = (DefaultTableModel) selectedColumns.getModel();
					model.addRow(new Object[]{colName, new Boolean(true)});
				}
			}
		});

		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = selectedColumns.getSelectedRow();
				if (selectedRow >= 0) {
					DefaultTableModel model = (DefaultTableModel) selectedColumns.getModel();
					Object colName = model.getValueAt(selectedRow, 0);
					model.removeRow(selectedRow);

					model = (DefaultTableModel) allColumns.getModel();
					model.addRow(new Object[]{colName});
				}
			}
		});
		bRemoveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) selectedColumns.getModel();
				int rowCount = model.getRowCount();
				for (int i = 0; i < rowCount; i++) {
					model = (DefaultTableModel) selectedColumns.getModel();
					Object colName = model.getValueAt(0, 0);
					model.removeRow(0);

					model = (DefaultTableModel) allColumns.getModel();
					model.addRow(new Object[]{colName});
				}
			}
		});
	}

	public void setSelectedColumns(int[] selectedIndexes) {
		int size = selectedIndexes.length;
		DefaultTableModel allModel = (DefaultTableModel) allColumns.getModel();
		DefaultTableModel selModel = (DefaultTableModel) selectedColumns.getModel();
		for (int i = 0; i < selModel.getRowCount(); i++) {
			selModel.removeRow(0);
		}
		for (int i = size - 1; i >= 0; i--) {
			String col = (String) allModel.getValueAt(selectedIndexes[i], 0);
			allModel.removeRow(i);

			selModel.insertRow(0, new Object[]{col, new Boolean(true)});
		}
	}

	public int getSelectedColumnCount() {
		return selectedColumns.getModel().getRowCount();
	}

	public String getSelectedColumnName(int index) {
		return (String) selectedColumns.getModel().getValueAt(index, 0);
	}

	public boolean getSelectedColumnOrder(int index) {
		return ((Boolean) selectedColumns.getModel().getValueAt(index, 1)).booleanValue();
	}

	class SelectionModel extends DefaultTableModel {
		public SelectionModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}

		public SelectionModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public boolean isCellEditable(int row, int column) {
			return (column != 0);
		}

		public Class getColumnClass(int columnIndex) {
			if (columnIndex == 1) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
	}
}