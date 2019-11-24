package gui;

import java.awt.*;

import javax.swing.*;

import com.alee.laf.checkbox.*;

import core.*;

/**
 * use by {@link CheckComboBox} to custom diplay list of elements as selectables {@link JCheckBox}
 * 
 * @author terry
 * 
 */
class CheckComboBoxCellRenderer extends DefaultListCellRenderer {
	private final JLabel label ;
	private final WebCheckBox check;
	
	public CheckComboBoxCellRenderer() {
		this.label = new JLabel();
		this.check = new WebCheckBox();
		check.setAnimated(false);
		check.setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		// selected whe needs to update combobox text
		if (index < 0) {
			label.setText(getCheckedItemString(list.getModel()));
			label.setPreferredSize(getPreferredSize());
			return label;
		} else {
			CheckComboBox.BooleanTEntry be = (CheckComboBox.BooleanTEntry) value;
			check.setText(be.value.getValue().toString());
			check.setSelected(be.selected);
			if (isSelected) {
				check.setBackground(list.getSelectionBackground());
				check.setForeground(list.getSelectionForeground());
			} else {
				check.setBackground(list.getBackground());
				check.setForeground(list.getForeground());
			}
			check.setBorder(getBorder());
			check.setPreferredSize(getPreferredSize());
			return check;
		}
	}
	/**
	 * String representation of selected items
	 * 
	 * @param model - model to obtain list status
	 * 
	 * @return string of {@link TEntry#getValue()} comma separated 
	 */
	private static String getCheckedItemString(ListModel model) {
		String txt = "";
		for (int i = 0; i < model.getSize(); i++) {
			CheckComboBox.BooleanTEntry be = (CheckComboBox.BooleanTEntry) model.getElementAt(i);
			if (be.selected) {
				txt += be.value.getValue() + ", ";
			}
		}
		return txt.length() > 0 ? txt.substring(0, txt.length() - 2) : "Seleccione...";
	}

}
