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
package plugins.flicka;

import java.awt.*;
import java.beans.*;
import java.util.List;
import java.util.function.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import core.datasource.model.*;
import gui.*;

public class PDistributionList extends TUIListPanel implements PropertyChangeListener {

	public PDistributionList() {
		showAditionalInformation(false);
//		this toolbar hat new ExportToFileAction(this, "") warum???
		setToolBar(TActionsFactory.getActions("deleteModel"));
		setColumns("pdrace;pdvalue;pdhits;pdmasscenter;pdprediction;pddecision;pdevent");
		getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}


	@Override
	public void init() {

		// super cellrenderer invented
		TDefaultTableCellRenderer tdcr = new TDefaultTableCellRenderer() {
			@Override
			public void setBackgroud(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				TAbstractTableModel stm = (TAbstractTableModel) table.getModel();
				Model r = stm.getModelAt(row);
				if (!isSelected) {
					int evt = r.getInteger("pdevent");
					int dec = r.getInteger("pddecision");
					int sr = r.getInteger("pdselrange");
					setBackground(pair_color);

					// decision
					if (dec > 0 && column == 5) {
						float f = Math.abs((float) ((dec * .3 / 3) - .3)); // from green to red
						Color c = new Color(Color.HSBtoRGB(f, .15f, .95f));
						setBackground(c);
					}

					// event
					if (evt <= sr && column == 6) {
						float f = Math.abs((float) ((evt * .3 / 3) - .3)); // from green to red
						Color c = new Color(Color.HSBtoRGB(f, .15f, .95f));
						setBackground(c);
					}
				}
			}
		};
		setDefaultRenderer(tdcr);
		Function<String, List<Model>> f = (par -> PDistribution.findAll().orderBy("pdprediction"));
		setDBParameters(f, PDistribution.getMetaModel().getColumnMetadata());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}


	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		// TODO Auto-generated method stub
		return null;
	}
}
