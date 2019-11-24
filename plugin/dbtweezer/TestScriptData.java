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
package plugin.dbtweezer;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

public class TestScriptData extends UIListPanel implements DockingComponent {

	private ExportToFileAction export;
	public TestScriptData() {
		super(null);
		this.export = new ExportToFileAction(this, "");
		setToolBar(export);
		getJTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setIconParameters("-1; ");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;

		return pane;
	}

	@Override
	public void init() {
		setMessage("dbt.ui.msg02");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object nval = evt.getNewValue();
		if (nval != null && nval instanceof ServiceRequest) {
			ServiceRequest sr = (ServiceRequest) evt.getNewValue();
			// clear previous formtas
			TDefaultTableCellRenderer dtcr = (TDefaultTableCellRenderer) getJTable().getDefaultRenderer(Double.class);
			dtcr.getFormats().clear();
			Record mod = (Record) sr.getParameter(ServiceResponse.RECORD_MODEL);
			String sc = "";
			for (int c = 0; c < mod.getFieldCount(); c++) {
				sc += mod.getFieldName(c) + ";";
				if (mod.getFieldValue(c) instanceof Double) {
					setColumnFormat(c, TStringUtils.getFormattForDecimalPlaces(mod.getFieldPresition(c)));
				}
			}
			setColumns(sc.substring(0, sc.length() - 1));
			setServiceRequest(sr);
		}
	}
}
