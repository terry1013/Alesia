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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * una <code>ExpressionPanel</code> almacena grupo de operadores y valores
 * 
 */
public abstract class ExpressionPanel extends JPanel {
	protected Vector listeners;
	protected JButton addExpressionButton;

	/**
	 * nueva instanica
	 * 
	 */
	public ExpressionPanel() {
		// super(new FlowLayout(FlowLayout.CENTER, 0,0));
		super();
		listeners = new Vector();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#toString()
	 */
	public abstract String toString();

	/**
	 * adiciona el <code>ActionListener</code> a la lista
	 * 
	 * @param l - <code>ActionListener</code> a añadir
	 */
	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	/**
	 * elimina listener de la lista
	 * 
	 * @param l - <code>ActionListener</code> a eliminar
	 */
	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}

	/**
	 * procesa evento. todas las subclases deben implementar este metodo y concluir pasando el evento a todos los
	 * listener en la lista
	 * 
	 * @param e - evento
	 */
	protected abstract void actionPerformed(ActionEvent e);

}
