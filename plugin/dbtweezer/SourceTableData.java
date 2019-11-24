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

public class SourceTableData extends UIListPanel implements DockingComponent {

	private ServiceRequest request;
	public SourceTableData() {
		super(null);
		setToolBar(new ExportToFileAction(this, ""));
		getJTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		request = new ServiceRequest(ServiceRequest.DB_QUERY, null, null);
		request.setParameter(ServiceRequest.DB_QUERY_SIZE, 1000);
		setIconParameters("-1; ");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;

		return pane;
	}

	@Override
	public void init() {
		setMessage("dbt.ui.msg01");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object nval = evt.getNewValue();

		if (src instanceof SourceDBTables) {
			if (nval != null) {
				Record rcd = (Record) nval;
				String tn = (String) rcd.getFieldValue("table_name");
				Record mod = ConnectionManager.getAccessTo(tn).getModel();
				// clear previous formtas
				TDefaultTableCellRenderer dtcr = (TDefaultTableCellRenderer) getJTable().getDefaultRenderer(Double.class);
				dtcr.getFormats().clear();
				// showColumns
				String sc = "";
				for (int c = 0; c < mod.getFieldCount(); c++) {
					sc += mod.getFieldName(c) + ";";
					if (mod.getFieldValue(c) instanceof Double) {
						setColumnFormat(c, TStringUtils.getFormattForDecimalPlaces(mod.getFieldPresition(c)));
					}
				}
				setColumns(sc.substring(0, sc.length() - 1));
				request.setTableName(tn);
				setServiceRequest(request);
			} else {
				setMessage("dbt.ui.msg01");
			}
		}
	}
}
