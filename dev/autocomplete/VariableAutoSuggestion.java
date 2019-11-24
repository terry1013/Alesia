package dev.autocomplete;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import com.alee.laf.list.*;
import com.alee.managers.popup.*;

import core.*;

/**
 * ths keyword are an array list of tentry where the key is the variable name or description and the name are the
 * variable id that will be commit for the target component
 * 
 * @author terry
 * 
 */
public class VariableAutoSuggestion {

	class AutoSuggestionListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			TEntry te = (TEntry) value;
			String in = (String) te.getValue();
			in = in.substring(2, in.lastIndexOf('.'));
			ImageIcon ii = TResources.getIcon(in, 14);
			setIcon(ii);
			setText("<html><FONT COLOR=#000000>" + te.getValue() + "</font> - <FONT COLOR=#808080>" + te.getKey()
					+ "</font></html>");
			return this;
		}
	}
	private JTextComponent textComponent;
	private final ArrayList<TEntry> keywords;
	private WebPopup popup;
	private WebList jList;
	private WebListModel listModel;

	public VariableAutoSuggestion(ArrayList<TEntry> ks) {
		this.keywords = ks;
		Collections.sort(keywords);

		listModel = new WebListModel();
		this.jList = new WebList(listModel);
		jList.setVisibleRowCount(10);
		jList.setFixedCellWidth(400);// /////////
		jList.setPrototypeCellValue(ks.get(0));
		jList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				// comint changes and
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					TEntry sel = (TEntry) jList.getSelectedValue();
					// if no down or up key, no selection was made
					if (sel != null) {
						append(sel);
					}
				}

				// hide popup / restore focus
				if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					popup.hidePopup();
					textComponent.requestFocus();
				}

				if (e.getSource() == textComponent) {
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
				append(sel);
			}
		});
		jList.setCellRenderer(new AutoSuggestionListCellRenderer());

		this.popup = new WebPopup();
		popup.setRequestFocusOnShow(true);
		popup.setAnimated(false);
		popup.setCloseOnFocusLoss(true);
		JScrollPane jsp = new JScrollPane(jList);
		jsp.setBorder(null);
		popup.add(jsp);
		popup.setDefaultFocusComponent(jList);
	}

	/**
	 * set this autosuttestion for the text component
	 * 
	 * @param jtf - link this autosuggestion for this text component
	 */
	public void setAutoSuggestionFor(JTextComponent jtf) {
		this.textComponent = jtf;
		Dimension ps = jList.getPreferredSize();
		ps.width = textComponent.getPreferredSize().width;
		jList.setPreferredWidth(textComponent.getPreferredSize().width);

		InputMap inputMap = textComponent.getInputMap();
		ActionMap actionMap = textComponent.getActionMap();

		// tmp?
		/*
		 * if (textComponent instanceof JEditorPane) { JEditorPane jep = (JEditorPane) textComponent; inputMap =
		 * jep.getInputMap(JComponent.WHEN_FOCUSED); }
		 */
		// show popup
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK), "showPopUp");
		actionMap.put("showPopUp", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
				listModel.addElements(keywords);
				showPopUp();
			}
		});
	}

	private void append(TEntry as) {
		if (as == null) {
			popup.hidePopup();
			textComponent.requestFocus();
			return;
		}
		int cp = textComponent.getCaretPosition();
		// TODO: temporal for jeditorpane ??
		if (textComponent instanceof JEditorPane) {
			try {
				JEditorPane jep = (JEditorPane) textComponent;
				// HTMLEditorKit kit = (HTMLEditorKit) jep.getEditorKit();
				Document doc = jep.getDocument();
				doc.insertString(cp, as.toString(), null);
				// StringReader reader = new StringReader(as.toString());
				// kit.read(reader, doc, cp);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} else {
			String content = textComponent.getText();
			StringBuffer sb = new StringBuffer(content);
			sb.insert(cp, as);
			String newstr = sb.toString();
			textComponent.setText(newstr);
			textComponent.setCaretPosition(as.toString().length() + cp);
			// textField.replaceSelection("\t");
		}
		popup.hidePopup();
		textComponent.requestFocus();
	}

	private void showPopUp() {
		popup.packPopup();

		// show the pop up base on carret pos
		if (textComponent instanceof JTextArea || textComponent instanceof JEditorPane) {
			Rectangle rect = null;
			int windowX = 0;
			int windowY = 0;
			try {
				// get carets position
				rect = textComponent.getUI().modelToView(textComponent, textComponent.getCaret().getDot());
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
			// TODO: temporal +- some pixels
			windowX = (int) (rect.getX()) - 10;
			windowY = (int) (rect.getY()) + 15;
			popup.showPopup(textComponent, new Point(windowX, windowY));
		} else {
			// show the pop up base on component pos
			popup.showAsPopupMenu(textComponent);
		}
	}
}