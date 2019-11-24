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
package plugin.flicka;

import gui.*;
import gui.docking.*;

import java.awt.*;
import java.beans.*;

import javax.swing.*;

import action.*;
import core.datasource.*;
import core.reporting.*;

public class PDistributionBySpeed extends UIListPanel implements DockingComponent {

	ServiceRequest request;

	public PDistributionBySpeed() {
		super(null);
		// this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "pdistribution", "pd_decision > 0");
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "pdistribution", null);
		request.setParameter(ServiceRequest.ORDER_BY, "pdprediction DESC");
		setToolBar(new DeleteRecord2(this), new ExportToFileAction(this, ""));
		setColumns("pdrace;pdvalue;pdprediction;pddecision;pdevent");
		getJTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// putClientProperty(TConstants.ICON_PARAMETERS,"0;/plugin/flicka/flicka");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		return pane;
	}

	@Override
	public void init() {
		TDefaultTableCellRenderer tdcr = new TDefaultTableCellRenderer() {
			@Override
			public void setBackgroud(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				TAbstractTableModel stm = (TAbstractTableModel) table.getModel();
				Record r = stm.getRecordAt(row);
				if (!isSelected) {
					int evt = (Integer) r.getFieldValue("pdevent");
					int dec = (Integer) r.getFieldValue("pddecision");
					int sr = (Integer) r.getFieldValue("pdselrange");
					setBackground(pair_color);

					// decision
//					if (dec > 0 && r.getFieldName(column).equals("pddecision")) {
					if (dec > 0 && column == 3) {
						float f = Math.abs((float) ((dec * .3 / 3) - .3)); // from green to red
						Color c = new Color(Color.HSBtoRGB(f, .15f, .95f));
						setBackground(c);
					}

					// event
					if (evt <= sr && column == 4) {
//					if (evt <= Selector.selRange && column == 10) {
						float f = Math.abs((float) ((evt * .3 / 3) - .3)); // from green to red
						Color c = new Color(Color.HSBtoRGB(f, .15f, .95f));
						setBackground(c);
					}
				}
			}
		};
		setDefaultRenderer(tdcr);
		setServiceRequest(request);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
