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
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * present a list of valid data sources ({@link TConnections}) and record edition component ({@link TConnectionsRecord})
 * 
 * @author terry
 *
 */
public class ConnectionProfile extends UIComponentPanel implements PropertyChangeListener {

	private TConnections connections;
	private TConnectionsRecord connectionsRecord;
	private Record recordModel;
	private RedirectAction connectAction;
	
	public ConnectionProfile() {
		this(false, true);
	}

	public ConnectionProfile(boolean allwcon, boolean allwdrv) {
		super("ConnectionProfile.title", true);
		this.connections = new TConnections();
		connections.init();
		connections.setBorder(new TitledBorder(TStringUtils.getString("connection.border.left")));
		connections.setParent(this);
//		connections.setPreferredSize(new Dimension(450, 800));

		this.recordModel = connections.getRecordModel();
		this.connectionsRecord = new TConnectionsRecord(recordModel, false);
		connectionsRecord.setBorder(new TitledBorder(TStringUtils.getString("connection.border.right")));
		TUIUtils.setEnabled(connectionsRecord, false);

		Vector aal = new Vector();  
		this.connectAction = new RedirectAction("ConnectToDB", this);
		connectAction.setEnabled(false);
		// show conection action
		if (allwcon) {
			aal.add(connectAction);
		}
		// show drivermanager
		if (allwdrv) {
			aal.add(new DriverManagerAction());
		}
		aal.add(new CloseAction(this));
		setActionBar((AbstractAction[]) aal.toArray(new AbstractAction[aal.size()]));
		JPanel jp = new JPanel(new BorderLayout(4, 4));
		jp.add(connections, BorderLayout.WEST);
		jp.add(connectionsRecord, BorderLayout.CENTER);
		add(jp);

		connections.addPropertyChangeListener(TConstants.RECORD_SELECTED, this);
		connectionsRecord.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);		
	}
	
	public TConnections getConnections() {
		return connections;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.RECORD_SELECTED)) {
			Record rcd = connections.getRecord();
			TUIUtils.setEnabled(connectionsRecord, rcd != null);
			connectAction.setEnabled(rcd != null);
			if (rcd == null) {
				connectionsRecord.showAplicationExceptionMsg(null); // borra mensajes anteriores
				connectionsRecord.setModel(recordModel);
			} else {
				connectionsRecord.setModel(rcd);
//				connectionsRecord.setEnabledInputComponent("t_cnname", false);
				connectionsRecord.preValidate(null);
				connectionsRecord.getInternalFields().put("newRcd", false);
				connectionsRecord.getInternalFields().put("oldRcd", rcd);
			}
		}
		// conectar a base de datos
		if (evt.getNewValue() instanceof NewRecord2) {
			System.out.println("ConnectionProfile.propertyChange()");
		}
		
		// nuevo
		if (evt.getNewValue() instanceof NewRecord2) {
			connectionsRecord.showAplicationExceptionMsg(null); // borra mensajes anteriores
			connectionsRecord.setModel(recordModel);
			connectionsRecord.getInternalFields().put("newRcd", true);
			TUIUtils.setEnabled(connectionsRecord, true);
		}

		// aplicar cambios a registro (nuevo o editar)
		if (evt.getNewValue() instanceof ApplyAction) {
			Record rcd = connectionsRecord.getRecord();
			
			// test profile parameteres
			try {
				ConnectionManager.testProfile(rcd);
			} catch (TError e) {
				showAplicationException(e);
				return;
			}
			
			DBAccess dba = ConnectionManager.getAccessTo(rcd.getTableName());
			boolean newr = (Boolean) connectionsRecord.getInternalFields().get("newRcd");
			if (newr) {
				if (dba.exist(rcd) != null) {
					showAplicationExceptionMsg("msg03");
					return;
				} else {
					dba.add(rcd);
				}
			} else {
				// for edition, if exits, delete and write new in case of name change
				Record oldrcd = (Record) connectionsRecord.getInternalFields().get("oldRcd");
				if (dba.exist(oldrcd) != null) {
					dba.delete(oldrcd);
				}
				dba.write(rcd);
			}
			showAplicationExceptionMsg(null);
			connections.freshen();
			connections.selectRecord(rcd);
		}

		// cerrar ventana
		if (evt.getNewValue() instanceof CloseAction) {
			JDialog jdl = (JDialog) getRootPane().getParent();
			jdl.dispose();
		}
	}
	
}
