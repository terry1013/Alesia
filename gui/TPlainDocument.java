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
package gui;

import javax.swing.*;
import javax.swing.text.*;

import core.*;




/** clase que verifica la longitud de los datos entrantes
 * 
 */
public class TPlainDocument extends PlainDocument {

	private int length;
	private boolean init;

	/** nueva instancia
	 * 
	 * @param l - longitud del documento
	 * @param t - texto inicial
	 */
	public TPlainDocument(String t, int l) {
		super();
		this.init = true;
		try {
	        replace(0, getLength(), t, null);
		} catch (Exception ex) {
			SystemLog.logException(ex);
		}
		this.length = l;
		this.init = false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (!init) {
			int sl = str.length();
			int dl = getLength();
			
			if (dl == length) {
				 UIManager.getLookAndFeel().provideErrorFeedback(null);
				 return;
			}
			if ((sl + dl) > length) {
				 UIManager.getLookAndFeel().provideErrorFeedback(null);
				str = str.substring(0, length - dl);
			}
		}
		super.insertString(offs, str, a);
	}
}
