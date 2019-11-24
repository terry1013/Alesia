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
/*			
 * Copyright (c) QQ - All right reserved

 */
package gui.html;

import gui.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import core.*;


/** panel principal de trabajo que contiene y gestiona las perspectivas y las vistas. 
 * 
 */
public class Browser extends JPanel implements Navigator, ActionListener, HyperlinkListener {

	private int currentComponent;
	private Vector <Canvas> components;
	private boolean navAction;
	private NextAction next;
	private PreviousAction previous;
	private JToolBar toolbar;
	private Canvas homeCanvas;

	public static void main(String arg[]) {
		try {
			JFrame jf = new JFrame();
			TPreferences.init();
			TStringUtils.init();
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.setBounds(20,20,300,600);
			jf.setContentPane(new Browser());
			jf.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** nueva instancia 
	 * 
	 *
	 */
	public Browser() {
		super(new BorderLayout());
		this.components = new Vector();
		this.currentComponent = -1;
		add(getMainToolbar(), BorderLayout.NORTH);
		this.navAction = false;
		sincronizeNavigation();
		homeCanvas = showPanel(TResources.getURL("/help/metalworks.html"));
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			showPanel(e.getURL());
		}
	}

	public Canvas showPanel(URL u) {
		Canvas c = new Canvas(u);
		c.editorPane.addHyperlinkListener(this);
		return showPanel(c);
	}
	
	/** precenta el componente pasado como argumento al lado derecho del la barra de tareas
	 * 
	 * @param jc - componente
	 */
	public Canvas showPanel(Canvas c) {
		c.setBorder(new LineBorder(Color.GRAY));
		if (!navAction) {
			if (currentComponent > -1) {
				remove((JComponent) components.elementAt(currentComponent));
			}
			// si se presiona otro enlace, 
			// se suprimen todos los elementos posteriores a la posicion actual
			if ((currentComponent + 1) < components.size()) {
				for (int i = (currentComponent + 1); i < components.size(); i++) {
					components.remove(currentComponent + 1);
				}
			}
			components.add(c);
			currentComponent = components.size() - 1;
		}
		navAction = false;
		add(BorderLayout.CENTER, c);
		sincronizeNavigation();

		revalidate();
		repaint();
		return c;
	}

	/** crea y retorna la barra de herramientas pricipal
	 * 
	 * @return barra
	 */
	private JToolBar getMainToolbar() {
		this.toolbar = TUIUtils.getJToolBar();
		this.previous = new PreviousAction(this);
		JButton jb = new JButton(previous);
		jb.setFocusable(false);
		toolbar.add(jb);
		this.next = new NextAction(this);
		jb = new JButton(next);
		jb.setFocusable(false);
		jb.setHorizontalTextPosition(JButton.LEFT);
		toolbar.add(jb);
		jb = new JButton(new HomeAction(this));
		jb.setFocusable(false);
		toolbar.add(jb);
		return toolbar;
	}

	/* (non-Javadoc)
	 * @see client.ui.Navigator#home()
	 */
	public void home() {
		showPanel(homeCanvas);
	}

	/*
	 *  (non-Javadoc)
	 * @see client.ui.Navigator#next()
	 */
	public void next() {
		navAction = true;
		remove((JComponent) components.elementAt(currentComponent));
		showPanel(components.elementAt(++currentComponent));
	}

	/** sincroniza los estados de las acciones de nevegacion para que esten acordes con los paneles
	 * presentados
	 *
	 */
	private void sincronizeNavigation() {
		//		toolbar.setVisible(false);
		next.setEnabled(true);
		previous.setEnabled(true);
		if (currentComponent + 1 == components.size()) {
			next.setEnabled(false);
		}
		if (currentComponent < 1) {
			previous.setEnabled(false);
		}
		//		toolbar.setVisible(true);
	}

	/*
	 *  (non-Javadoc)
	 * @see client.ui.Navigator#previous()
	 */
	public void previous() {
		navAction = true;
		remove((JComponent) components.elementAt(currentComponent));
		showPanel(components.elementAt(--currentComponent));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

	}
}
