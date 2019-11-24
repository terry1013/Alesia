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
package gui.beej;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


import core.*;

/** An ComplexExpressionPanel has multiple ExpressionPanels */
public class ComplexExpressionPanel extends ExpressionPanel implements ActionListener {
	protected Vector subExpressions;
	protected Vector booleanComboBoxes;

	protected TextExpressionPanel expressionPanel;

	protected int depth;

	/**
	 * Construct a new ExpressionPanel. Default has no sub expressions
	 * 
	 * @param expr the first ExpressionPanel to use. all new ones will be of this type
	 * @param bool the type of BooleanComboBox to use
	 * @param depth how deeply nested this panel is
	 */
	public ComplexExpressionPanel(TextExpressionPanel expr, int depth) {
		super();
		this.expressionPanel = expr;

		// default values
		subExpressions = new Vector();
		booleanComboBoxes = new Vector();

		// typeOfBooleanComboBox = bool.getClass();

		this.depth = depth;

		// padding border around a etched border
		// setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),
		// BorderFactory.createLineBorder(Color.white)));
		// setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// darkness based on depth
		Color c = getBackground();
		for (int i = 0; i < depth; i++) {
			c = c.darker();
			setBorder(new EmptyBorder(0,10,0,10));
		}
		setBackground(c);

		addExpression(expr);
	}

	/**
	 * Construct a new ExpressionPanel with a depth of 0
	 */
	public ComplexExpressionPanel(TextExpressionPanel expr) throws NullPointerException {
		this(expr, 0);
	}

	/**
	 * string representation. Must be defined in children
	 * 
	 * @return string representation
	 */
	public String toString() {
		if (subExpressions.size() == 0)
			return "";
		else if (subExpressions.size() == 1)
			return subExpressions.get(0).toString();
		else {
			String result = "(" + subExpressions.get(0).toString();
			for (int a = 1; a < subExpressions.size(); a++)
				result += "\n" + booleanComboBoxes.get(0).toString() + " " + subExpressions.get(a).toString();
			result += ")";
			return result;
		}
	}

	/** rebuild the whole layout */
	private void showPanels() {
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// for each subExpression..
		for (int i = 0; i < subExpressions.size(); i++) {
			if (subExpressions.get(i) instanceof ComplexExpressionPanel)
				((ExpressionPanel) subExpressions.get(i)).setOpaque(true);
			else
				((ExpressionPanel) subExpressions.get(i)).setOpaque(false);
			add((ExpressionPanel) subExpressions.get(i));

			Box buttonPanel = Box.createHorizontalBox();
			buttonPanel.add(Box.createHorizontalGlue());
			// buttonPanel.setOpaque(false);

			// booleanCombobox
			if (i != subExpressions.size() - 1)
				buttonPanel.add((BooleanComboBox) booleanComboBoxes.get(i));

			AddExpressionButton a = new AddExpressionButton();
			a.setActionCommand(Integer.toString(i)); // so we know where to add
			a.addActionListener(this);
			buttonPanel.add(Box.createHorizontalStrut(TUIUtils.H_GAP));
			buttonPanel.add(a);

			// cannot split an already complex
			SplitExpressionButton s = new SplitExpressionButton();
			s.setActionCommand(Integer.toString(i)); // so we know what one to split
			s.addActionListener(this);
			buttonPanel.add(Box.createHorizontalStrut(TUIUtils.H_GAP));
			buttonPanel.add(s);
			s.setEnabled(false);
			if (!(subExpressions.get(i) instanceof ComplexExpressionPanel)) {
				s.setEnabled(true);
			}

			RemoveExpressionButton r = new RemoveExpressionButton();
			// no remove button if the last one
			r.setEnabled(subExpressions.size() > 1);
			r.setActionCommand(Integer.toString(i)); // so we know what one to remove
			r.addActionListener(this);
			buttonPanel.add(Box.createHorizontalStrut(TUIUtils.H_GAP));
			buttonPanel.add(r);
			buttonPanel.add(Box.createHorizontalStrut(TUIUtils.H_GAP));

			add(Box.createVerticalStrut(TUIUtils.H_GAP));
			add(buttonPanel);
			add(Box.createVerticalStrut(TUIUtils.H_GAP));
//			setBorder(new LineBorder(Color.BLUE));
			
			if (i < subExpressions.size() - 1) {
				add(Box.createVerticalStrut(TUIUtils.H_GAP));
				add(new JSeparator(JSeparator.HORIZONTAL));
			}
		}
	}

	/**
	 * add an expression to this panel
	 * 
	 * @param expr the expression to add
	 * @param index the location (in the array of ExpressionPanels) in which to place the expression
	 */
	public void addExpression(ExpressionPanel expr, int index) {
		// listen to this expression panel
		expr.addActionListener(this);

		BooleanComboBox bool = null;
		bool = new BooleanComboBox();
		bool.addActionListener(this);

		subExpressions.add(index, expr);
		booleanComboBoxes.add(index, bool);

		// make this bool's keyword the same as others
		// if (booleanComboBoxes.size() > 0) {
		// bool.setKeyword(booleanComboBoxes.get(0).toString());
		// }

		// since we've changed stuff, redo the layout, etc
		showPanels();
	}

	/** add an expression to the end */
	public void addExpression(ExpressionPanel expr) {
		addExpression(expr, subExpressions.size());
	}

	/**
	 * Collapse a ComplexExpressionPanel to just it's first ExpressionPanel
	 * 
	 * @param complexExpr the ComplexExpressionPanel to collapse
	 * @param expr the ExpressionPanel to collapse it to (generally, the only ExpressionPanel in complexExpr)
	 * @throws BEEJException if complexExpr is not found
	 */
	public void collapseExpression(ComplexExpressionPanel complexExpr, ExpressionPanel expr) {
		// loop through until we find it
		for (int i = 0; i < subExpressions.size(); i++) {
			if (subExpressions.get(i) == complexExpr) {
				// add after it
				addExpression(expr, i + 1);
				// delete it
				removeExpression(i);
				return;
			}
		}
	}

	/**
	 * removes an expression
	 * 
	 * @param index indicates the ExpressionPanel to remove
	 */
	public void removeExpression(int index) {
		subExpressions.remove(index);
		booleanComboBoxes.remove(index);

		// if just one expression left (and our parent is a ComplexExpressionPanel; it may be an ExpressionEditor)
		if (subExpressions.size() == 1 && getParent() instanceof ComplexExpressionPanel) {
			((ComplexExpressionPanel) getParent()).collapseExpression(this, (ExpressionPanel) subExpressions.get(0));
		}

		// since stuff has changed
		showPanels();
	}

	/**
	 * splits an expression into a ComplexExpressionPanel
	 * 
	 * @param index the expression to split
	 */
	protected void splitExpression(int index) {
		ComplexExpressionPanel newComplexExpressionPanel = new ComplexExpressionPanel(
				(TextExpressionPanel) subExpressions.get(index), depth + 1);
		newComplexExpressionPanel.addExpression((TextExpressionPanel) expressionPanel.getClone());
		newComplexExpressionPanel.addActionListener(this);
		// newComplexExpressionPanel.setOpaque(false);

		subExpressions.set(index, newComplexExpressionPanel);

		showPanels();
	}

	/**
	 * process an event and pass it on to listeners
	 * 
	 * @param e the event
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof AddExpressionButton) {
			try {
				// add a new one of the same type as previously
				addExpression(expressionPanel.getClone(), Integer.parseInt(e.getActionCommand()) + 1);
			} catch (Exception ex) {
				SystemLog.logException(ex);
			}
		} else if (e.getSource() instanceof RemoveExpressionButton) {
			removeExpression(Integer.parseInt(e.getActionCommand()));
		} else if (e.getSource() instanceof BooleanComboBox) {
			// change them all
			// for (int i = 0; i < booleanComboBoxes.size(); i++) {
			// ((BooleanComboBox) booleanComboBoxes.get(i)).setKeyword(e.getSource().toString());
			// }
		} else if (e.getSource() instanceof SplitExpressionButton) {
			splitExpression(Integer.parseInt(e.getActionCommand()));
		} else if (e.getSource() instanceof ComplexExpressionPanel) {
			// things may have resized
			showPanels();
		}

		// notify all listeners that WE changed (not that one of our elements changed)
		ActionEvent ae = new ActionEvent(this, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
		for (Enumeration en = listeners.elements(); en.hasMoreElements();)
			((ActionListener) en.nextElement()).actionPerformed(ae);
	}

	public class AddExpressionButton extends JButton {

		public AddExpressionButton() {
			super();
			setText(TStringUtils.getString("b.addnc"));
			setToolTipText(TStringUtils.getString("ttb.addnc"));
		}
	}

	public class RemoveExpressionButton extends JButton {

		public RemoveExpressionButton() {
			super();
			setText(TStringUtils.getString("b.remov"));
			setToolTipText(TStringUtils.getString("ttb.remov"));
		}
	}

	public class SplitExpressionButton extends JButton {

		public SplitExpressionButton() {
			super();
			setText(TStringUtils.getString("b.splic"));
			setToolTipText(TStringUtils.getString("ttb.splic"));
		}
	}

	/** An BooleanComboBox provides the and/or combo box */
	public class BooleanComboBox extends JComboBox implements ActionListener {

		/** Construct a new BooleanPanel. */
		public BooleanComboBox() {
			super();
			TEntry tl[] = TStringUtils.getTEntryGroup("b.bocj");
			setToolTipText(TStringUtils.getString("ttb.bocj"));
			for (TEntry t : tl) {
				addItem(t);
			}
			setSelectedIndex(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Component#toString()
		 */
		public String toString() {
			return ((TEntry) getSelectedItem()).getKey().toString();
		}

		/**
		 * set the keyword option.
		 * 
		 * @throws BEEJException if keyword is not a valid item public void setKeyword(String keyword) { for (int i = 0;
		 *             i < getItemCount(); i++) { if (keyword.equals((String) getItemAt(i))) { setSelectedIndex(i);
		 *             break; } } }
		 */
	}
}
