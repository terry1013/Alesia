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
 * Copyright (c) QQ - All right reserved QQ PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */
package gui.html;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import core.*;



class Canvas extends JPanel {
	
	protected JEditorPane editorPane;
	private JScrollPane scrollPane;
	private HTMLEditorKit editorKit;

	public Canvas(URL url) {
		try {
			setLayout(new BorderLayout());
			editorPane = new JEditorPane();
			editorPane.setEditable(false);
			editorKit = new HTMLEditorKit();
			HTMLEditor.setStyleSheet(editorKit, "HtmlEditor.css");
			editorPane.setEditorKit(editorKit);
			editorPane.setPage(url);
			/*
			document = (HTMLDocument) editorKit.createDefaultDocument();
//			editorPane.setDocument(document);
//			editorKit = (HTMLEditorKit) editorPane.getEditorKit();
			editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			*/
			scrollPane = new JScrollPane(editorPane);
			add(scrollPane, BorderLayout.CENTER);
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/**
	 * Follows the reference in an
	 * link.  The given url is the requested reference.
	 * By default this calls <a href="#setPage">setPage</a>,
	 * and if an exception is thrown the original previous
	 * document is restored and a beep sounded.  If an 
	 * attempt was made to follow a link, but it represented
	 * a malformed url, this method will be called with a
	 * null argument.
	 *
	 * @param u the URL to follow
	 */
	protected void linkActivated(URL u) {
		Cursor c = editorPane.getCursor();
		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		editorPane.setCursor(waitCursor);
		SwingUtilities.invokeLater(new PageLoader(u, c));
	}

	/**
	 * temporary class that loads synchronously (although
	 * later than the request so that a cursor change
	 * can be done).
	 */
	class PageLoader implements Runnable {

		PageLoader(URL u, Cursor c) {
			url = u;
			cursor = c;
		}

		public void run() {
			if (url == null) {
				// restore the original cursor
				editorPane.setCursor(cursor);

				// PENDING(prinz) remove this hack when 
				// automatic validation is activated.
				Container parent = editorPane.getParent();
				parent.repaint();
			} else {
				Document doc = editorPane.getDocument();
				try {
					editorPane.setPage(url);
				} catch (IOException ioe) {
					editorPane.setDocument(doc);
					getToolkit().beep();
				} finally {
					// schedule the cursor to revert after
					// the paint has happended.
					url = null;
					SwingUtilities.invokeLater(this);
				}
			}
		}

		URL url;
		Cursor cursor;
	}

}
