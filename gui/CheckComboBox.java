package gui;

import java.awt.event.*;

import javax.swing.*;

import core.*;

/**
 * extend {@link JComboBox} to support multiples list seleccion using {@link CheckComboBoxCellRenderer} to display the
 * list of elements as {@link JCheckBox}.
 * 
 * @see CheckComboBoxCellRenderer
 * 
 * @author terry
 * 
 */
public class CheckComboBox extends JComboBox {
	private boolean keepOpen;

	/**
	 * new instance
	 * 
	 * @param tlist - array of {@link TEntry} to diplay in this CheckComboBox
	 * @param sele - selected key ; separated
	 */
	public CheckComboBox(TEntry[] tlist, String sele) {
		super();
		String[] seles = sele.split(";");
		BooleanTEntry[] bte = new BooleanTEntry[tlist.length];
		for (int i = 0; i < bte.length; i++) {
			bte[i] = new BooleanTEntry(false, tlist[i]);
			boolean sel = false;
			for (int j = 0; j < seles.length; j++) {
				sel = seles[j].equals(bte[i].value.getKey().toString()) ? true : sel;
			}
			bte[i].selected = sel;
		}
		setModel(new DefaultComboBoxModel(bte));
		setRenderer(new CheckComboBoxCellRenderer());
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
					updateItem(getSelectedIndex());
					keepOpen = true;
				}
			}
		});
	}
	
	private void updateItem(int index) {
		if (isPopupVisible()) {
			BooleanTEntry item = (BooleanTEntry) getItemAt(index);
			item.selected ^= true;
			setSelectedIndex(-1);
			setSelectedItem(item);
		}
	}

	/**
	 * override to return the string representation of {@link TEntry#getKey()} separated by ;
	 */
	@Override
	public Object getSelectedItem() {
		String kstr = "";
		ComboBoxModel mod = getModel();
		for (int i = 0; i < mod.getSize(); i++) {
			BooleanTEntry be = (BooleanTEntry) mod.getElementAt(i);
			kstr += be.selected ? be.value.getKey().toString() + ";" : "";
		}
		return kstr.length() > 0 ? kstr.substring(0, kstr.length() - 1) : "";
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (keepOpen) {
			keepOpen = false;
		} else {
			super.setPopupVisible(v);
		}
	}

	/**
	 * encapsulate a {@link TEntry} with a boolean value to save selected status inside {@link CheckComboBox} model
	 * 
	 * @author terry
	 * 
	 */
	public class BooleanTEntry {
		public boolean selected;
		public TEntry value;

		public BooleanTEntry(boolean sv, TEntry te) {
			this.selected = sv;
			this.value = te;
		}
	}
}