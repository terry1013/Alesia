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

/** clase base para manipulacion de expresiones booleanas
 * 
 */
public class BaseEditor extends JPanel implements ActionListener {

	protected ExpressionPanel expr;
	protected Vector listeners;

	/** nueva instancia
	 * 
	 * @param expr - instancia de <code>ExpressionPanel</code>
	 */
	public BaseEditor(TextExpressionPanel expr) {
		super(new FlowLayout(FlowLayout.CENTER,0,0));
		setOpaque(false); // transparent
		listeners = new Vector();
		// start with a complex panel that has a regular panel of the specified type
		setExpressionPanel(new ComplexExpressionPanel(expr));
	}

	/**
	 * get the current expression
	 * 
	 * @return the current expression
	 */
	public ExpressionPanel getExpressionPanel() {
		return expr;
	}

	/**
	 * Set a new expression to use.
	 * 
	 * @param expr the expression to use
	 * @throws NullPointerException if the expression was <code>null</code>
	 */
	public void setExpressionPanel(ExpressionPanel expr) throws NullPointerException {
		if (expr == null)
			throw new NullPointerException();

		this.expr = expr;
		expr.addActionListener(this);
		add(expr);
	}

	/**
	 * add a listener
	 * 
	 * @param l the listener to add
	 */
	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	/**
	 * remove a listener
	 * 
	 * @param l the listener to remove
	 */
	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}

	/**
	 * get string format, the code produced by all expression panels
	 * 
	 * @return the code produced by all expression panels
	 */
	public String toString() {
		return expr.toString();
	}

	/**
	 * Listens to the expression panels and pass the event on
	 * 
	 * @param e the event
	 */
	public void actionPerformed(ActionEvent e) {
		// notify all listeners that WE changed (not that one of our elements changed)
		ActionEvent ae = new ActionEvent(this, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
		for (Enumeration en = listeners.elements(); en.hasMoreElements();)
			((ActionListener) en.nextElement()).actionPerformed(ae);
	}
}
