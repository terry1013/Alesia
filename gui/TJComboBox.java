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
package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * extends {@link JComboBox} with basic filter list by keystroke
 * 
 * @author terry
 *
 * @param <E>
 */
public class TJComboBox<E> extends JComboBox implements KeyListener {

	private Dimension orgDim;
	private String filterString;

	private DefaultComboBoxModel originalModel, filterModel;
	public TJComboBox() {
		super();
		init();
	}
	public TJComboBox(E[] items) {
		super(items);
		init();
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}
	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if (!e.isActionKey()) {
			filterString += c;
			filterModel(filterString);
		}
		if (c == KeyEvent.VK_ENTER) {
			// TODO: finish implementation of selection by keyboard
		}

		// backspace if apply
		if (c == KeyEvent.VK_BACK_SPACE && filterString.length() > 0) {
			filterString = filterString.substring(0, filterString.length() - 1);
			filterModel(filterString);
		}

		// silent restore model
		if (c == KeyEvent.VK_ESCAPE) {
			filterString = "";
			hidePopup();
			super.setModel(originalModel);
		}
	}

	@Override
	public void setModel(ComboBoxModel aModel) {
		super.setModel(aModel);
		// sinc my internal lists
		this.originalModel = (DefaultComboBoxModel) getModel();
	}
	/**
	 * fileter the ComboBox model selecting only the element that contain <code>ftxt</code>. this method show popup
	 * window only if the list contains elements.
	 * 
	 * @param ftxt - String for filtering
	 * 
	 */
	private void filterModel(String ftxt) {
		orgDim = (orgDim == null) ? getSize() : orgDim;
		filterModel.removeAllElements();

		for (int i = 0; i < originalModel.getSize(); i++) {
			Object obj = originalModel.getElementAt(i);
			String tev = obj.toString();
			if (tev.toLowerCase().contains(ftxt.toLowerCase())) {
				filterModel.addElement(obj);
			}
		}
		// if fileter list contains elements, update model. else, beep to user keeping the last succesd filter list
		if (filterModel.getSize() > 0) {
			super.setModel(filterModel);
			setPreferredSize(orgDim);
			showPopup();
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	private void init() {
		addKeyListener(this);
		this.filterModel = new DefaultComboBoxModel();
		this.filterString = "";
	}
}
