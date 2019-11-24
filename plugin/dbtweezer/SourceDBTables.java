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

import action.*;

import com.alee.utils.*;

import core.*;
import core.datasource.*;
public class SourceDBTables extends UIListPanel implements DockingComponent {

	private EditRecord2 dataProcess;
	/**
	 * new instance
	 */
	public SourceDBTables() {
		super(null);
		dataProcess = new EditRecord2(this) {
			@Override
			public void actionPerformed2() {
				super.actionPerformed2();
				DockingContainer.signalFreshgen(ScriptList.class.getName());
			}
		};
		dataProcess.setDefaultValues("DataProcess");
		dataProcess.setAllowWrite(true);

		setToolBar(dataProcess);
		setColumns("table_name;remarks");
		setIconParameters("0;table_;table_type");
		getJTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa == dataProcess) {
			// retrive example from file
			String sc = FileUtils.readToString(TResources.getFile("example.groovy"));
			Record rcd = DBTweezer.getNewScriptRecord("DataProcess", "Groovy", getRecord(), sc);
			pane = new GroovyScriptUI(rcd);
		}
		return pane;
	}

	@Override
	public void init() {
		setMessage("dbt.ui.msg03");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object nwv = evt.getNewValue();
		Object prp = evt.getPropertyName();

		if (prp.equals(TConstants.CONNECTION_CHANGE)) {
			if (nwv != null) {
				ServiceRequest sr = DBTConnectionManager.getSourceDB().getServiceRequestFor(
						DBTConnectionManager.TABLES, null);
				setServiceRequest(sr);
				performTransition(getComponentPanel());
			} else {
				// fire null property to clear al related component and show wait panel 
				firePropertyChange(TConstants.RECORD_SELECTED, "", null);
				performTransition(getWaitPanel());
//				setMessage("dbt.ui.msg03");
			}
		}
	}
}
