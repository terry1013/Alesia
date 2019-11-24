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
package delete;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.utils.laf.*;

import action.*;
import core.*;

/**
 * componente que una un campo de entrada con un pulsador. Este es para presentar un componente con la forma de un texto
 * de busqueda. utilize <code>addActionListener(ActionListener)</code> para saber si sobre este componente se a
 * precionado el boton o telceado intro.
 * 
 * NOTA: ver this.actionPerformed() para ver detalles de datos
 */
public class SearchTextField extends JPanel implements ActionListener {

	public JTextField textField;
	private JButton search, delete;
	private ActionPerformer performer;

	/**
	 * nueva instancia
	 * 
	 * @param col - columnas para indicar el largo del componente
	 * @param ap - instancia de <code>ActionPerformer</code> que sera invocado para procesar busqueda
	 * @param sup - <code>true</code> para adicionar opcion de supresion
	 */
	public SearchTextField(int col, ActionPerformer ap, boolean sup) {
		super();
		performer = ap;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		String tt = "ttsearch.textfield";
		this.textField = TUIUtils.getJTextField(tt, "", col);
		textField.setName(tt);
		tt = "ttsearch.button";
		this.search = new JButton(TResources.getSmallIcon("msearch"));
		search.setToolTipText(TStringUtils.getString(tt));
		search.setName(tt);
		tt = "ttsearch.button1";
		this.delete = new JButton(TResources.getSmallIcon("mdelete"));
		delete.setToolTipText(TStringUtils.getString(tt));
		delete.setName(tt);
		delete.setVisible(false);
		add(textField);
		add(new JSeparator(JSeparator.VERTICAL));
		add(search);
		if (sup) {
			add(new JSeparator(JSeparator.VERTICAL));
			add(delete);
		}
		addActionListener(this);
		// tama;o de componente
//		CompoundBorder com = (CompoundBorder) textField.getBorder();
//		Border out = com.getOutsideBorder();
//		CompoundBorder com1 = new CompoundBorder(new EmptyBorder(0, 0, 0, 0), com.getInsideBorder());
		//textField.setBorder(com1);
		search.setBorder(new EmptyBorder(0, 1, 0, 0));
		delete.setBorder(new EmptyBorder(0, 1, 0, 0));
		setBorder(new WeblafBorder(0,0,0,0));

		// texto
		Dimension dte = textField.getPreferredSize();

		// tama;o del pulsador
		Dimension dbu = new Dimension(dte.height, dte.height);
		setDimension(search, dbu);
		setDimension(delete, dbu);

		Dimension ds = new Dimension(dte.width + dbu.width, dbu.height);
		setMinimumSize(ds);
		setMaximumSize(ds);
		setPreferredSize(ds);
	}

	private void setDimension(JComponent c, Dimension d) {
		c.setMinimumSize(d);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		TActionEvent lt = new TActionEvent(this);
		// e.getName() se usan datos para identificar origen
		String d = "";
		if (e.getSource() instanceof Component) {
			d = ((Component) e.getSource()).getName();
		}
		lt.setData(d);
		performer.executeAction(lt);

	}

	/**
	 * adiciona una instacia de <code>ActionListener</code> a este componente
	 * 
	 * @param al - listener
	 */
	private void addActionListener(ActionListener al) {
		textField.addActionListener(al);
		search.addActionListener(al);
		delete.addActionListener(al);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField.setEnabled(enabled);
		search.setEnabled(enabled);
		delete.setEnabled(enabled);
	}
}
