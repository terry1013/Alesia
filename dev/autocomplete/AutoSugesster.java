package dev.autocomplete;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.alee.laf.list.*;
import com.alee.managers.popup.*;

/**
 * 
 * @author About Scott Robinson from: http://stackabuse.com/example-adding-autocomplete-to-jtextfield/ Example
 * 
 */
public class AutoSugesster implements DocumentListener, KeyListener {

	private JTextField textField;
	private final ArrayList<String> keywords;
	private WebPopup popup;
	private WebList jList;
	private WebListModel listModel;

	public AutoSugesster(JTextField tf, ArrayList<String> ks) {
		this.textField = tf;
		this.keywords = ks;
		Collections.sort(keywords);

		listModel = new WebListModel();
		this.jList = new WebList(listModel);
		jList.addKeyListener(this);
		this.popup = new WebPopup();
		popup.add(jList);
		popup.setDefaultFocusComponent(jList);
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

	@Override
	public void changedUpdate(DocumentEvent ev) {
	}

	@Override
	public void removeUpdate(DocumentEvent ev) {
	}

	@Override
	public void insertUpdate(DocumentEvent ev) {
		if (ev.getLength() != 1)
			return;

		int pos = ev.getOffset();
		String content = null;
		try {
			content = textField.getText(0, pos + 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		// Find where the word starts
		int w;
		for (w = pos; w >= 0; w--) {
			if (!Character.isLetter(content.charAt(w))) {
				break;
			}
		}

		// Too few chars
		if (pos - w < 2)
			return;

		String prefix = content.substring(w + 1).toLowerCase();
		listModel.clear();
		for (int i = 0; i < keywords.size(); i++) {
			String item = keywords.get(i);
			// A completion is found
			if (item.startsWith(prefix)) {
				listModel.add(item);
			}
		}
		if (listModel.size() > 0) {
			showPopUp();
			textField.requestFocus();
		} else {
			if (popup.isShowing()) {
				popup.hidePopup();
			}
		}
	}

	public void showPopUp() {
		popup.packPopup();
		popup.showAsPopupMenu(textField);
	}
	public void commit(String as) {
		String content = textField.getText();
		int pos = content.length();
		// Find where the word starts
		int w = 0;
		for (w = pos; w > 0; w--) {
			if (!Character.isLetter(content.charAt(w - 1))) {
				break;
			}
		}
		String space = w == 0 ? "" : " ";
		String ns = content.substring(0, w) + space + as;
		textField.setText(ns);
		textField.setCaretPosition(ns.length());
		// textField.replaceSelection("\t");
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// comint changes and
		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
			String sel = (String) jList.getSelectedValue();
			commit(sel);
			popup.hidePopup();
			textField.requestFocus();
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

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}