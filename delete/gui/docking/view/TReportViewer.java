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

import gui.docking.*;

import java.beans.*;

import net.sf.jasperreports.swing.*;
import core.*;
import delete.*;

/**
 * visor de resultado de datos durante generacion de reporte dinamico
 * 
 */
public class TReportViewer extends JRViewer implements DockingComponent {

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public TReportViewer() {
		super(null);

	}
	public String getInterestProperty() {
		return TConstants.FIND_TEXT;
	}

	public String getPropertyChanger() {
		return TTCodeEditor.class.getName();
	}
	@Override
	public void init() {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.FIND_TEXT)) {
			viewerContext.loadReport(DR.jasperPrint);
			viewerContext.refreshPage();
		}
	}
}
