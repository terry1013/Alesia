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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */
package gui.html;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

import core.*;

/**
 * implementa un explorador sencillo para paginas html simples
 */
public class MiniBrowser extends JPanel implements HyperlinkListener {

	private JButton backButton, homeButton, forwardButton;
	private JTextField locationTextField;
	private JEditorPane editorPane;
	private ArrayList pageList = new ArrayList();
//	private URL homePage;

	/**
	 * nueva instancia
	 * 
	 * @param hp - pagina inicial
	 */
	public MiniBrowser() {
		JToolBar toolbar = TUIUtils.getJToolBar();
		backButton = new JButton(TResources.getSmallIcon("backward_nav"));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionBack();
			}
		});
		backButton.setEnabled(false);
		toolbar.add(backButton);

		homeButton = new JButton(TResources.getSmallIcon("home_nav"));
		homeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionBack();
			}
		});
		toolbar.add(homeButton);

		forwardButton = new JButton(TResources.getSmallIcon("forward_nav"));
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionForward();
			}
		});
		forwardButton.setEnabled(false);
		toolbar.add(forwardButton);

		locationTextField = new JTextField(35);
		locationTextField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					actionGo();
				}
			}
		});
	//	toolbar.add(locationTextField);

		// Set up page display.
		editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(this);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		HTMLEditor.setStyleSheet(editorKit, "HtmlEditor.css");
		editorPane.setEditorKit(editorKit);

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(editorPane), BorderLayout.CENTER);
	}

	/** atras hacia la pagina vista antes que la actual
	 * 
	 */
	private void actionBack() {
		URL currentUrl = editorPane.getPage();
		int pageIndex = pageList.indexOf(currentUrl.toString());
		try {
			showPage(new URL((String) pageList.get(pageIndex - 1)), false);
		} catch (Exception e) {
		}
	}

	/** hacia adelante, a la pagina despues de la acutual
	 * 
	 */
	private void actionForward() {
		URL currentUrl = editorPane.getPage();
		int pageIndex = pageList.indexOf(currentUrl.toString());
		try {
			showPage(new URL((String) pageList.get(pageIndex + 1)), false);
		} catch (Exception e) {
		}
	}

	/** muestra la pagina descrita en el texto 
	 * 
	 */
	private void actionGo() {
		URL verifiedUrl = verifyUrl(locationTextField.getText());
		if (verifiedUrl != null) {
			showPage(verifiedUrl, true);
		} else {
			showError("Invalid URL");
		}
	}

	/** dialogo de error
	 * 
	 * @param errorMessage - error
	 */
	private void showError(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/** verifica url
	 * 
	 * @param url - texto 
	 * @return instancia de URL
	 */
	private URL verifyUrl(String url) {
		// Only allow HTTP URLs.
		if (!url.toLowerCase().startsWith("http://")) {
			return null;
		}
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (Exception e) {
			return null;
		}
		return verifiedUrl;
	}

	/**
	 *  muestra pagina pasada como argumento y adiciona a la lista si se desea
	 * @param pageUrl - URL a mostrar
	 * @param addToList - true si se desea añadir a la lista de paginas visitadas
	 */
	public void showPage(URL pageUrl, boolean addToList) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			// Get URL of page currently being displayed.
			URL currentUrl = editorPane.getPage();
			// Load and display specified page.
			editorPane.setPage(pageUrl);

			// Add page to list if specified.
			if (addToList) {
				int listSize = pageList.size();
				if (listSize > 0) {
					int pageIndex = pageList.indexOf(currentUrl.toString());
					if (pageIndex < listSize - 1) {
						for (int i = listSize - 1; i > pageIndex; i--) {
							pageList.remove(i);
						}
					}
				}
				pageList.add(pageUrl.toString());
			}

			// Update location text field with URL of current page.
	//		locationTextField.setText(newUrl.toString());

			// Update buttons based on the page being displayed.
			updateButtons();
		} catch (Exception e) {
			// Show error messsage.
			e.printStackTrace();
//			showError("Unable to load page");
		} finally {
			// Return to default cursor.
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/** actualzia estado de botones
	 * 
	 */
	private void updateButtons() {
		if (pageList.size() < 2) {
			backButton.setEnabled(false);
			forwardButton.setEnabled(false);
		} else {
			URL currentUrl = editorPane.getPage();
			int pageIndex = pageList.indexOf(currentUrl.toString());
			backButton.setEnabled(pageIndex > 0);
			forwardButton.setEnabled(pageIndex < (pageList.size() - 1));
		}
	}
	
	/** gestiona clic en enlaces dentro de la pagina
	 * 
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		HyperlinkEvent.EventType eventType = event.getEventType();
		if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
			if (event instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent linkEvent = (HTMLFrameHyperlinkEvent) event;
				HTMLDocument document = (HTMLDocument) editorPane.getDocument();
				document.processHTMLFrameHyperlinkEvent(linkEvent);
			} else {
				showPage(event.getURL(), true);
			}
		}
	}
}
