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
package gui.html;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

import core.*;

/**
 * editor de texto
 * 
 */
public class HTMLEditor extends JPanel implements ActionListener {
	private JEditorPane editor;
	private JScrollPane jScrollPane1;
	private HTMLEditorKit editorKit;
	private HTMLDocument document;
	private Action boldAction, italicAction, underAction, ulAction, olAction, lAlignAction, cAlignAction, rAlignAction,
			selectAllAction, insertHRAction, insCharAction;
	private boolean enbAct = true;
	public UndoAction undoAction;
	public RedoAction redoAction;
	private Hashtable<Action, AbstractButton> action_buttons;
	private JComboBox ffaceCB, fSizeCB, fcolorCB;
	public Action cutAction = new HTMLEditorKit.CutAction();
	public Action copyAction = new HTMLEditorKit.CopyAction();;
	public Action pasteAction = new HTMLEditorKit.PasteAction();
	private CharTablePanel charTablePanel;
	boolean charTableShow = false;
	public JTabbedPane toolsPanel = new JTabbedPane();
	public boolean toolsPanelShow = false;
	public JMenuItem jMenuItemUndo;
	JMenuItem jMenuItemRedo;
	JMenuItem jMenuItemCut = new JMenuItem(cutAction);
	JMenuItem jMenuItemCopy = new JMenuItem(copyAction);
	JMenuItem jMenuItemPaste = new JMenuItem(pasteAction);
	protected UndoableEditListener undoHandler;
	public UndoManager undo;
	public JToolBar editToolbar;
	boolean blockCBEventsLock = false;
	private boolean inlineCBEventsLock = false;
	int currentCaret = 0;
	int currentFontSize = 4;
	private Vector<TEntry> colorMod;

	abstract class HTMLEditorAction extends AbstractAction {
		HTMLEditorAction(String name, String ic) {
			super("", getIcon(ic));
			super.putValue(Action.SHORT_DESCRIPTION, name);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (enbAct && (src == ffaceCB || src == fSizeCB || src == fcolorCB)) {
			setTextProperty();
		}
	}

	/**
	 * retorna texto en formato HTML contendido dentro de este editor
	 * 
	 * @return - texto
	 */
	public String getText() {
		return editor.getText();
	}
	
	public JEditorPane getJEditorPane() {
		return editor;
	}

	public HTMLEditor() {
		super(new BorderLayout());
		this.action_buttons = new Hashtable();

		ffaceCB = new JComboBox(new String[]{"Arial", "Bookman Old Style", "Courier", "Garamond", "Lucida Console",
				"Symbol", "Tahoma", "Times New Roman", "Verdana"});
		fSizeCB = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7"});
		colorMod = new Vector();
		colorMod.add(new TEntry(Integer.toHexString(Color.BLACK.getRGB()).substring(2), "Negro"));
		colorMod.add(new TEntry(Integer.toHexString(Color.BLUE.getRGB()).substring(2), "Azul"));
		colorMod.add(new TEntry(Integer.toHexString(Color.CYAN.getRGB()).substring(2), "Cian"));
		colorMod.add(new TEntry(Integer.toHexString(Color.DARK_GRAY.getRGB()).substring(2), "Gris oscuro"));
		colorMod.add(new TEntry(Integer.toHexString(Color.GRAY.getRGB()).substring(2), "Gris"));
		colorMod.add(new TEntry(Integer.toHexString(Color.LIGHT_GRAY.getRGB()).substring(2), "Gris claro"));
		colorMod.add(new TEntry(Integer.toHexString(Color.GREEN.getRGB()).substring(2), "Verde"));
		colorMod.add(new TEntry(Integer.toHexString(Color.MAGENTA.getRGB()).substring(2), "magenta"));
		colorMod.add(new TEntry(Integer.toHexString(Color.ORANGE.getRGB()).substring(2), "Naranja"));
		colorMod.add(new TEntry(Integer.toHexString(Color.PINK.getRGB()).substring(2), "Rosado"));
		colorMod.add(new TEntry(Integer.toHexString(Color.RED.getRGB()).substring(2), "Rojo"));
		colorMod.add(new TEntry(Integer.toHexString(Color.YELLOW.getRGB()).substring(2), "Amarillo"));
		fcolorCB = new JComboBox(colorMod);
		ffaceCB.addActionListener(this);
		ffaceCB.setFocusable(false);
		fSizeCB.setFocusable(false);
		fSizeCB.setSelectedIndex(2);
		fSizeCB.addActionListener(this);
		fcolorCB.setFocusable(false);
		fcolorCB.addActionListener(this);
		undoHandler = new UndoHandler(this);
		undoAction = new UndoAction(this);
		redoAction = new RedoAction(this);
		undo = new UndoManager();
		jMenuItemUndo = new JMenuItem(undoAction);
		jMenuItemRedo = new JMenuItem(redoAction);
		boldAction = new HTMLEditorAction("Bold", "bold") {
			public void actionPerformed(ActionEvent e) {
				JToggleButton jtb = (JToggleButton) e.getSource();
				jtb.setSelected(!jtb.isSelected());
				new StyledEditorKit.BoldAction().actionPerformed(e);
			}
		};

		italicAction = new HTMLEditorAction("Italic", "italic") {
			public void actionPerformed(ActionEvent e) {
				JToggleButton jtb = (JToggleButton) e.getSource();
				jtb.setSelected(!jtb.isSelected());
				new StyledEditorKit.ItalicAction().actionPerformed(e);
			}
		};
		/*
		 * pasteAction = new AbstractAction() { public void actionPerformed(ActionEvent e) { doPaste(); } };
		 */
		underAction = new HTMLEditorAction("Underline", "underline") {
			public void actionPerformed(ActionEvent e) {
				JToggleButton jtb = (JToggleButton) e.getSource();
				jtb.setSelected(!jtb.isSelected());
				new StyledEditorKit.UnderlineAction().actionPerformed(e);
			}
		};

		insCharAction = new HTMLEditorAction(null, "char") {
			public void actionPerformed(ActionEvent e) {
				if (((JToggleButton) e.getSource()).isSelected()) {
					if (toolsPanelShow) {
						return;
					}
					add(toolsPanel, BorderLayout.SOUTH);
					toolsPanelShow = true;
					toolsPanel.addTab("Characters", charTablePanel);
				} else {
					toolsPanel.remove(charTablePanel);
					if (toolsPanel.getTabCount() == 0)
						if (!toolsPanelShow) {
							return;
						}
					remove(charTablePanel);
					toolsPanelShow = false;
				}
			}
		};
		ulAction = new HTMLEditorAction("Unordered list", "listunordered") {
			public void actionPerformed(ActionEvent e) {
				String parentname = document.getParagraphElement(editor.getCaretPosition()).getParentElement()
						.getName();
				HTML.Tag parentTag = HTML.getTag(parentname);
				HTMLEditorKit.InsertHTMLTextAction ulAction = new HTMLEditorKit.InsertHTMLTextAction("insertUL",
						"<ul><li></li></ul>", parentTag, HTML.Tag.UL);
				ulAction.actionPerformed(e);
			}
		};
		olAction = new HTMLEditorAction("Ordered list", "listordered") {
			public void actionPerformed(ActionEvent e) {
				String parentname = document.getParagraphElement(editor.getCaretPosition()).getParentElement()
						.getName();
				HTML.Tag parentTag = HTML.getTag(parentname);
				HTMLEditorKit.InsertHTMLTextAction olAction = new HTMLEditorKit.InsertHTMLTextAction("insertOL",
						"<ol><li></li></ol>", parentTag, HTML.Tag.OL);
				olAction.actionPerformed(e);
			}
		};
		lAlignAction = new HTMLEditorAction("Align left", "alignleft") {
			public void actionPerformed(ActionEvent e) {
				HTMLEditorKit.AlignmentAction aa = new HTMLEditorKit.AlignmentAction("leftAlign",
						StyleConstants.ALIGN_LEFT);
				aa.actionPerformed(e);
			}
		};

		cAlignAction = new HTMLEditorAction("Align center", "aligncenter") {
			public void actionPerformed(ActionEvent e) {
				HTMLEditorKit.AlignmentAction aa = new HTMLEditorKit.AlignmentAction("centerAlign",
						StyleConstants.ALIGN_CENTER);
				aa.actionPerformed(e);
			}
		};
		rAlignAction = new HTMLEditorAction("Align right", "alignright") {
			public void actionPerformed(ActionEvent e) {
				HTMLEditorKit.AlignmentAction aa = new HTMLEditorKit.AlignmentAction("rightAlign",
						StyleConstants.ALIGN_RIGHT);
				aa.actionPerformed(e);
			}
		};

		selectAllAction = new HTMLEditorAction("Select all", null) {
			public void actionPerformed(ActionEvent e) {
				editor.selectAll();
			}
		};
		insertHRAction = new HTMLEditorAction("Insert horizontal rule", "hr") {
			public void actionPerformed(ActionEvent e) {
				try {
					editorKit.insertHTML(document, editor.getCaretPosition(), "<hr>", 0, 0, HTML.Tag.HR);
				} catch (Exception ex) {
					SystemLog.logException(ex);
				}
			}
		};

		cutAction.putValue(Action.SMALL_ICON, getIcon("cut"));
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
		cutAction.putValue(Action.NAME, getString("Cut"));
		cutAction.putValue(Action.SHORT_DESCRIPTION, getString("Cut"));

		copyAction.putValue(Action.SMALL_ICON, getIcon("copy"));
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		copyAction.putValue(Action.NAME, getString("Copy"));
		copyAction.putValue(Action.SHORT_DESCRIPTION, getString("Copy"));

		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
		pasteAction.putValue(Action.NAME, getString("Paste special"));
		pasteAction.putValue(Action.SHORT_DESCRIPTION, getString("Paste special"));

		selectAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));

		boldAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
		italicAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
		underAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));

		editor = new JEditorPane();
		editorKit = new HTMLEditorKit();
		HTMLEditor.setStyleSheet(editorKit, "HtmlEditor.css");
		editor.setEditorKit(editorKit);
		document = (HTMLDocument) editorKit.createDefaultDocument();
//		editor.setDocument(document);
//		editorKit = (HTMLEditorKit) editor.getEditorKit();
		editor.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				editor_caretUpdate(e);
			}
		});
//		editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
		// document.addUndoableEditListener(undoHandler);
		editToolbar = TUIUtils.getJToolBar();
		jScrollPane1 = new JScrollPane(editor);
		charTablePanel = new CharTablePanel(editor);
		this.add(jScrollPane1, BorderLayout.CENTER);
		this.add(editToolbar, BorderLayout.NORTH);

		editToolbar.add(ffaceCB);
		editToolbar.addSeparator();
		editToolbar.add(fSizeCB);
		editToolbar.addSeparator();
		editToolbar.add(fcolorCB);
		editToolbar.add(getToggleButton(boldAction));
		editToolbar.add(getToggleButton(italicAction));
		editToolbar.add(getToggleButton(underAction));
		editToolbar.addSeparator();
		editToolbar.add(getButton(ulAction));
		editToolbar.add(getButton(olAction));
		editToolbar.addSeparator();
		editToolbar.add(getButton(lAlignAction));
		editToolbar.add(getButton(cAlignAction));
		editToolbar.add(getButton(rAlignAction));
		editToolbar.addSeparator();
		editToolbar.add(getButton(insertHRAction));
		editToolbar.add(getToggleButton(insCharAction));

		toolsPanel.setTabPlacement(JTabbedPane.BOTTOM);
		toolsPanel.setFont(new Font("Dialog", 1, 10));

		/* KEY ACTIONS */

		editor.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		editor.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new ParaBreakAction());

		editor.getKeymap().removeKeyStrokeBinding((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY));
		editor.getKeymap().removeKeyStrokeBinding((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY));
		editor.getKeymap().removeKeyStrokeBinding((KeyStroke) copyAction.getValue(Action.ACCELERATOR_KEY));
		editor.getKeymap().removeKeyStrokeBinding((KeyStroke) cutAction.getValue(Action.ACCELERATOR_KEY));
		editor.getKeymap().removeKeyStrokeBinding((KeyStroke) pasteAction.getValue(Action.ACCELERATOR_KEY));
		editor.getKeymap().addActionForKeyStroke((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), undoAction);
		editor.getKeymap().addActionForKeyStroke((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), redoAction);
		editor.getKeymap().addActionForKeyStroke((KeyStroke) copyAction.getValue(Action.ACCELERATOR_KEY), copyAction);
		editor.getKeymap().addActionForKeyStroke((KeyStroke) cutAction.getValue(Action.ACCELERATOR_KEY), cutAction);
		editor.getKeymap().addActionForKeyStroke((KeyStroke) pasteAction.getValue(Action.ACCELERATOR_KEY), pasteAction);

		editor.addMouseListener(new PopupListener(this));
		document.getStyleSheet().setBaseFontSize(currentFontSize);
		this.requestFocusInWindow();
	}

	private JToggleButton getToggleButton(Action a) {
		JToggleButton jtb = new JToggleButton(a);
		jtb.setFocusable(false);
		action_buttons.put(a, jtb);
		return jtb;
	}

	private JButton getButton(Action a) {
		JButton jb = new JButton(a);
		jb.setFocusable(false);
		action_buttons.put(a, jb);
		return jb;
	}

	/**
	 * Resets the undo manager.
	 */
	protected void resetUndoManager() {
		undo.discardAllEdits();
		undoAction.update();
		redoAction.update();
	}

	public String getContent() {
		try {
			return editor.getText();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	void editor_caretUpdate(CaretEvent e) {
		currentCaret = e.getDot();
		AttributeSet charattrs = null;
		if (editor.getCaretPosition() > 0) {
			charattrs = document.getCharacterElement(editor.getCaretPosition() - 1).getAttributes();
		} else {
			charattrs = document.getCharacterElement(editor.getCaretPosition()).getAttributes();
		}

		boolean fl = charattrs.containsAttribute(StyleConstants.Bold, new Boolean(true));
		((JToggleButton) action_buttons.get(boldAction)).setSelected(fl);
		fl = charattrs.containsAttribute(StyleConstants.Italic, new Boolean(true));
		((JToggleButton) action_buttons.get(italicAction)).setSelected(fl);
		fl = charattrs.containsAttribute(StyleConstants.Underline, new Boolean(true));
		((JToggleButton) action_buttons.get(underAction)).setSelected(fl);
		inlineCBEventsLock = true;
		ffaceCB.setEnabled(!charattrs.isDefined(HTML.Tag.A));
		ffaceCB.setSelectedIndex(0);
		fSizeCB.setSelectedIndex(2);
		fcolorCB.setSelectedIndex(0);
		if (charattrs.isDefined(HTML.Tag.FONT)) {
			enbAct = false;
			Object at = charattrs.getAttribute(CSS.Attribute.FONT_FAMILY);
			ffaceCB.setSelectedItem(at.toString());
			at = charattrs.getAttribute(CSS.Attribute.FONT_SIZE);
			fSizeCB.setSelectedItem(at.toString());
			at = charattrs.getAttribute(CSS.Attribute.COLOR);
			String sv = at.toString().substring(1);
			for (TEntry le : colorMod) {
				if (le.getKey().equals(sv))
					fcolorCB.setSelectedItem(le);
			}
			enbAct = true;
		}
		inlineCBEventsLock = false;
	}

	void removeIfEmpty(Element elem) {
		if (elem.getEndOffset() - elem.getStartOffset() < 2) {
			try {
				document.remove(elem.getStartOffset(), elem.getEndOffset());
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
		}
	}

	class ParaBreakAction extends AbstractAction {
		ParaBreakAction() {
			super("ParaBreakAction");
		}

		public void actionPerformed(ActionEvent e) {

			Element elem = document.getParagraphElement(editor.getCaretPosition());
			String elName = elem.getName().toUpperCase();
			String parentname = elem.getParentElement().getName();
			HTML.Tag parentTag = HTML.getTag(parentname);
			if (parentname.toUpperCase().equals("P-IMPLIED"))
				parentTag = HTML.Tag.IMPLIED;
			if (parentname.toLowerCase().equals("li")) {
				if (elem.getEndOffset() - elem.getStartOffset() > 1) {
					try {
						document.insertAfterEnd(elem.getParentElement(), "<li></li>");
						editor.setCaretPosition(elem.getParentElement().getEndOffset());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					try {
						document.remove(editor.getCaretPosition(), 1);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					Element listParentElement = elem.getParentElement().getParentElement().getParentElement();
					HTML.Tag listParentTag = HTML.getTag(listParentElement.getName());
					String listParentTagName = listParentTag.toString();
					if (listParentTagName.toLowerCase().equals("li")) {
						Element listAncEl = listParentElement.getParentElement();
						try {
							editorKit.insertHTML(document, listAncEl.getEndOffset(), "<li><p></p></li>", 3, 0,
									HTML.Tag.LI);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						HTMLEditorKit.InsertHTMLTextAction pAction = new HTMLEditorKit.InsertHTMLTextAction("insertP",
								"<p></p>", listParentTag, HTML.Tag.P);
						pAction.actionPerformed(e);
					}
				}
			} else if ((elName.equals("PRE")) || (elName.equals("ADDRESS")) || (elName.equals("BLOCKQUOTE"))) {
				if (editor.getCaretPosition() > 0)
					removeIfEmpty(document.getParagraphElement(editor.getCaretPosition() - 1));
				HTMLEditorKit.InsertHTMLTextAction pAction = new HTMLEditorKit.InsertHTMLTextAction("insertP",
						"<p></p>", parentTag, HTML.Tag.P);
				pAction.actionPerformed(e);
			} else if (elName.equals("P-IMPLIED")) {
				try {
					document.insertAfterEnd(elem.getParentElement(), "<p></p>");
					editor.setCaretPosition(elem.getParentElement().getEndOffset());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			} else {
				editor.replaceSelection("\n");
				editorKit.getInputAttributes().removeAttribute(HTML.Attribute.ID);
				editorKit.getInputAttributes().removeAttribute(HTML.Attribute.CLASS);
			}
		}
	}

	public void jAlignActionB_actionPerformed(ActionEvent e) {
		HTMLEditorKit.AlignmentAction aa = new HTMLEditorKit.AlignmentAction("justifyAlign",
				StyleConstants.ALIGN_JUSTIFIED);
		aa.actionPerformed(e);
	}

	/**
	 * establece propiedades para el tag font obteniendo valores de componentes visuales
	 * 
	 * 
	 */
	private void setTextProperty() {
		try {
			if (inlineCBEventsLock)
				return;
			String text = "&nbsp;";
			if (editor.getSelectedText() != null)
				text = editor.getSelectedText();
			String tag = "font";
			String attrs = " size=\"<sz>\" face=\"<fc>\" color=\"#<cl>\"";
			String html = "<" + tag + attrs + ">" + text + "</" + tag + ">";
			// por el segundo constructor
			if (ffaceCB.isVisible()) {
				html = html.replaceAll("<sz>", fSizeCB.getSelectedItem().toString());
				html = html.replaceAll("<fc>", ffaceCB.getSelectedItem().toString());
				html = html.replaceAll("<cl>", (((TEntry) fcolorCB.getSelectedItem()).getKey().toString()));
			}
			if (editor.getCaretPosition() == document.getLength())
				html += "&nbsp;";
			editor.replaceSelection("");
			editorKit.insertHTML(document, editor.getCaretPosition(), html, 0, 0, HTML.getTag(tag));
			if (editor.getCaretPosition() == document.getLength())
				editor.setCaretPosition(editor.getCaretPosition() - 1);
		} catch (Exception ex) {
			SystemLog.logException(ex);
		}
	}

	public void setText(String txt) {
		setVisible(false);
		editor.setText(txt);
		initEditor();
		setVisible(true);
	}

	public void initEditor() {
		// editor.setDocument(document);
		// undo = new UndoManager();
		resetUndoManager();
		document.addUndoableEditListener(undoHandler);
		editor.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		editor.setCaretPosition(0);
	}

	public boolean isDocumentChanged() {
		return undo.canUndo();
	}

	/**
	 * establece <code>HtmlEditor.css</code> como css para la instancia de <code>HTMLEditorKit</code> pasado como
	 * argumento
	 * 
	 * @param ek - editor al cual se se elstablece las reglas css
	 * @param ccs - nombre de archivo ccs
	 */
	public static void setStyleSheet(HTMLEditorKit ek, String ccs) {
		try {
			FileInputStream fis = new FileInputStream(TResources.getFile(ccs));
			StyleSheet css = new StyleSheet();
			css.loadRules(new InputStreamReader(fis), null);
			ek.setStyleSheet(css);
		} catch (Exception ex) {
			SystemLog.logException(ex);
		}
	}

	/**
	 * localiza texto en ResourceBundle
	 * 
	 * @param id - id de texto
	 * @return constane
	 */
	protected String getString(String id) {
		return id;
	}

	public static ImageIcon getIcon(String in) {
		return TResources.getSmallIcon("html/" + in);
	}
}
