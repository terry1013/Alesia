package dev.autocomplete;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.alee.laf.list.*;
import com.alee.managers.popup.*;

import core.*;

public class VariableAutoSuggestionCopyOf {

	class AutoSuggestionListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			TEntry te = (TEntry) value;
			ImageIcon ii = TResources.getIcon(te.getValue().toString().substring(1));
			setIcon(ii);
			setText("<html><b>" + te.getValue() + "</b> - " + te.getKey() + "</html>");
			return this;
		}
	}
	private JTextField textField;
	private final ArrayList<TEntry> keywords;
	private WebPopup popup;
	private WebList jList;
	
	private WebListModel listModel;

	public VariableAutoSuggestionCopyOf(ArrayList<TEntry> ks) {
		this.keywords = ks;
		Collections.sort(keywords);

		listModel = new WebListModel();
		this.jList = new WebList(listModel);
		jList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				// comint changes and
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					TEntry sel = (TEntry) jList.getSelectedValue();
					commit(sel);
				}

				// hide popup / restore focus
				if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					popup.hidePopup();
					textField.requestFocus();
				}

				if (e.getSource() == textField) {
					if (e.getKeyChar() == KeyEvent.VK_SPACE && e.getModifiers() == KeyEvent.CTRL_MASK) {
						listModel.clear();
						listModel.addElements(keywords);
						showPopUp();
					}
				}
			}
		});
		
		jList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TEntry sel = (TEntry) jList.getSelectedValue();
				commit(sel);
			}
		});
		jList.setCellRenderer(new AutoSuggestionListCellRenderer());

		this.popup = new WebPopup();
		popup.add(jList);
		popup.setDefaultFocusComponent(jList);
	}

	/**
	 * set this autosuttestion for the text component 
	 * 
	 * @param jtf - link this autosuggestion for this text component
	 */
	public void setAutoSuggestionFor(JTextField jtf) {
		this.textField = jtf;
		// prefered size for popup
		Dimension ps = jList.getPreferredSize();
		ps.width = textField.getPreferredSize().width;
		jList.setPreferredWidth(textField.getPreferredSize().width);
		
		// tranfer focus to popup if is showinf
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "downKey");
		textField.getActionMap().put("downKey", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (popup.isShowing()) {
					popup.getDefaultFocusComponent().requestFocusInWindow();
				}
			}
		});

		// show popup
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK), "showPopUp");
		textField.getActionMap().put("showPopUp", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
				listModel.addElements(keywords);
				showPopUp();
			}
		});
	}

	private void commit(TEntry as) {
		String content = textField.getText();
		String newstr = content + as;
		textField.setText(newstr);
		textField.setCaretPosition(newstr.length());
		// textField.replaceSelection("\t");
		popup.hidePopup();
		textField.requestFocus();

	}

	private void showPopUp() {
		popup.packPopup();
		popup.showAsPopupMenu(textField);
	}
}