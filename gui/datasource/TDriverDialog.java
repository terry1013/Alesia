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
package gui.datasource;

import gui.*;

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * present a list of valid drivers ({@link TDrivers}) and record edition component ({@link TDriverRecord})
 *   
 * @author terry
 *
 */
public class TDriverDialog extends UIComponentPanel implements PropertyChangeListener {

	private TDrivers drivers;
	private TDriverRecord driverManagerRecord;
	private Record recordModel;
	private Record lastselectRecord;

	public TDriverDialog() {
		super("drivermanager.title", true);
		this.drivers = new TDrivers();
		drivers.init();
		drivers.setBorder(new TitledBorder(TStringUtils
				.getString("drivermanager.border.left")));

		drivers.setBorder(new TitledBorder("Drivers"));
		drivers.setPreferredSize(new Dimension(250, 500));
		this.recordModel = drivers.getRecordModel();
		this.driverManagerRecord = new TDriverRecord(recordModel, false);

		driverManagerRecord.setParent(this);
		driverManagerRecord.setBorder(new TitledBorder(TStringUtils
				.getString("drivermanager.border.right")));
		TUIUtils.setEnabled(driverManagerRecord, false);

		setActionBar(new CloseAction(this));

		JPanel jp = new JPanel(new BorderLayout(4, 4));
		jp.add(drivers, BorderLayout.WEST);
		jp.add(driverManagerRecord, BorderLayout.CENTER);
		add(jp);

		drivers.addPropertyChangeListener(TConstants.RECORD_SELECTED, this);
		driverManagerRecord.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.RECORD_SELECTED)) {
			Record rcd = drivers.getRecord();
			TUIUtils.setEnabled(driverManagerRecord, rcd != null);
			if (rcd == null) {
				driverManagerRecord.showAplicationExceptionMsg(null); // borra mensajes anteriores
				driverManagerRecord.setModel(recordModel);
			} else {
				driverManagerRecord.setModel(rcd);
				driverManagerRecord.preValidate(null);
				lastselectRecord = rcd;
//				driverManagerRecord.setEnabledInputComponent("t_drname", false);

			}
		}
		// aplicar cambios a registro
		if (evt.getNewValue() instanceof ApplyAction) {
			Record newrcd = driverManagerRecord.getRecord();
			DBAccess dba = ConnectionManager.getAccessTo(newrcd.getTableName());
			if (dba.exist(newrcd) != null) {
				dba.delete(lastselectRecord);
				dba.add(newrcd);
				lastselectRecord = newrcd;
				drivers.freshen();
				drivers.selectRecord(newrcd);
			} else {
				showAplicationExceptionMsg("msg03");
			}
		}

		// cerrar ventana
		if (evt.getNewValue() instanceof CloseAction) {
			JDialog jdl = (JDialog) getRootPane().getParent();
			jdl.dispose();
		}
	}
}
