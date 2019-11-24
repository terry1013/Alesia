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
import gui.docking.*;

import java.awt.event.*;
import java.beans.*;

import plugin.dbtweezer.*;
import action.*;
import core.*;
import core.datasource.*;
import core.tasks.*;

/**
 * Show the {@link ConnectionProfile} dialog and process the conect to database action. This action:
 * <ol>
 * <li>Test the selected connection. show any error found
 * <li>fire the {@link TConstants#CONNECTION_CHANGE} with null value to notify the interested component about an
 * incoming change in database connection
 * <li>submith the conection process. this process will fire {@link TConstants#CONNECTION_CHANGE} with the profile
 * record if the connection was sucessefully.
 * 
 * @author terry
 *
 */
public class ConnectionProfileAction extends TAbstractAction implements PropertyChangeListener {

	private ConnectionProfile profile;
	private boolean allowConnect, allowDriverManager;
	
	public ConnectionProfileAction() {
		super(TAbstractAction.NO_SCOPE);
		setDefaultValues("ConnectionProfile");
		this.allowConnect = false;
		this.allowDriverManager = true;
	}
	
	public ConnectionProfileAction(boolean alCon, boolean alDrv) {
		this();
		this.allowConnect = alCon;
		this.allowDriverManager = alDrv;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		profile = new ConnectionProfile(allowConnect, allowDriverManager);
		profile.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(profile, "action.ConnectionProfile.title");
		dialog.setVisible(true);
	}

	public boolean isAllowConnect() {
		return allowConnect;
	}

	public boolean isAllowDriverManager() {
		return allowDriverManager;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		Object act = evt.getNewValue();
		String actn = (act != null) ? (String) ((TAbstractAction) act).getValue(TAbstractAction.NAME_ID) : "";
		
		// generig redirec action to connecto
		if (actn.equals("action.ConnectToDB")) {
			TConnections tc = profile.getConnections();
			final Record cf = tc.getRecord();

			// test the connection
			try {
				ConnectionManager.connect(cf, false);
			} catch (Exception e) {
				TError ae = new TError("ui.msg07", e);
				profile.showAplicationException(ae);
				return;
			}

			// the connection is about to change
			DockingContainer.fireProperty(this, TConstants.CONNECTION_CHANGE, null);
			dialog.dispose();

			Runnable run = new Runnable() {

				@Override
				public void run() {
					try {
						DBTConnectionManager.connect(cf);
						ConnectionManager.connect(cf);
						DockingContainer.fireProperty(this, TConstants.CONNECTION_CHANGE, cf);
					} catch (Exception ex) {
						ExceptionDialog.showDialog(ex);
					}
				}
			};
			TTaskManager.executeTask(run);
		}
	}

	public void setAllowConnect(boolean allowConnect) {
		this.allowConnect = allowConnect;
	}

	public void setAllowDriverManager(boolean allowDriverManager) {
		this.allowDriverManager = allowDriverManager;
	}
}
