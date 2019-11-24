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

package delete.gui.docking.view;

import gui.*;
import gui.docking.*;

import java.awt.*;
import java.beans.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import action.*;
import core.*;

/**
 * Consola de salida para mensajes emitidos durante la edicion de reporte
 * 
 */
public class OutputConsole extends UIComponentPanel implements DockingComponent {

	private JTextPane outConsole;
	private StyledDocument document;
	private DateFormat dateFormat;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public OutputConsole() {
		super(null, false);
		this.outConsole = new JTextPane();
		outConsole.setEditable(false);
		// outConsole.setAlignmentX(LEFT_ALIGNMENT);
		this.document = outConsole.getStyledDocument();

		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style normal = document.addStyle("normal", def);
		StyleConstants.setFontFamily(normal, "Courier New");
		StyleConstants.setFontSize(normal, 12);
		StyleConstants.setAlignment(normal, StyleConstants.ALIGN_LEFT);

		Style alert = document.addStyle(TError.ERROR, normal);
		StyleConstants.setForeground(alert, Color.RED);

		// adiciono pero no cambio para que la consola no parezca un carnaval
		// 161113 quefinooooo la sra carmen me regalo un paquete de cafe y un poco de azucar !!! que peladera de bolas
		Style info = document.addStyle(TError.INFORMATION, normal);
		StyleConstants.setForeground(info, Color.BLUE);

		document.addStyle(TError.ACTION, normal);
		document.addStyle(TError.WARNING, normal);

		this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

		setToolBar(new SaveAction(this));
		addWithoutBorder(new JScrollPane(outConsole));
		addPropertyChangeListener(this);
	}

	@Override
	public void init() {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		TError ex = null;
		Object obj = evt.getNewValue();
		if (evt.getPropertyName().equals(TConstants.FIND_TEXT)) {
			ex = new TError("DR.halt", obj);

		}
		if (evt.getPropertyName().equals(TConstants.LOG_MESSAGE)) {
			if (obj instanceof Exception) {
				ex = new TError("DR.Execption", ((Exception) obj).getMessage());
			} else {
				ex = new TError((String) obj);
			}
		}
		if (obj == null || ex == null) {
			return;
		}
		try {
			document.insertString(document.getLength(), "[" + dateFormat.format(new Date()) + "] " + ex.getMessage()
					+ "\n", document.getStyle(ex.getExceptionType()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
