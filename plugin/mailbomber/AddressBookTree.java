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
package plugin.mailbomber;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.tree.*;

import action.*;
import core.*;
import core.datasource.*;

public class AddressBookTree extends TAbstractTree implements DockingComponent {

	ServiceRequest request;
	private NewRecord2 newCompany, newOUnit, newPersonal;

	public AddressBookTree() {
		super(null, "m_abid", "m_abname", "m_abparentid");
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "M_ADDRESS_BOOK", null);
		request.setParameter(ServiceRequest.ORDER_BY, "m_abname");
		newCompany = new NewRecord2(this);
		newCompany.setDefaultValues("Company");
		newOUnit = new NewRecord2(this);
		newOUnit.setDefaultValues("OUnit");
		newPersonal = new NewRecord2(this);
		newPersonal.setDefaultValues("Personal");
		setToolBar(newCompany, newOUnit, newPersonal, new EditRecord2(this), new DeleteRecord2(this),
				new FixNodeSubnodeRelation(this, "m_abid", "m_abname", "m_abparentid"));

		putClientProperty(TConstants.TREE_EXPANDED, 2);
		putClientProperty(TConstants.TREE_ICON_FIELD, "m_abtype");
	}

	@Override
	public void enableActions(int sco, boolean ena) {
		super.enableActions(sco, ena);
		Record rcd = getRecord();

		// no selected record: enable only company
		newCompany.setEnabled(true);
		newOUnit.setEnabled(false);
		newPersonal.setEnabled(false);

		// on record selection
		if (rcd != null) {
			String rty = (String) rcd.getFieldValue("m_abtype");
			// disable all by default
			newCompany.setEnabled(false);
			newOUnit.setEnabled(false);
			newPersonal.setEnabled(false);
			// selected record is ounit? enable unit & personal
			if (rty.equals("ounit")) {
				newOUnit.setEnabled(true);
				newPersonal.setEnabled(true);
			}
			// selected record is company enable unit & personal
			if (rty.equals("company")) {
				newOUnit.setEnabled(true);
				newPersonal.setEnabled(true);
			}
		}
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		Record trcd = getRecord();
		boolean newr = aa instanceof NewRecord2;
		if (aa instanceof NewRecord2) {
			String pid = trcd == null ? "" : (String) trcd.getFieldValue("m_abid");
			trcd = getRecordModel();
			trcd.setFieldValue(0, TStringUtils.getRecordId());
			// type is equal to icon name
			String in = ((String) aa.getValue(TAbstractAction.ICON_ID)).toLowerCase();
			ImageIcon ii = TResources.getIcon(in, 100);
			trcd.setFieldValue("m_abtype", in);
			trcd.setFieldValue("m_abphoto", TPreferences.getBytearrayFromImage(ii));
			// parent form selected record
			trcd.setFieldValue("m_abparentid", pid);
		}
		return new AddressBookRecord(trcd, newr);
	}

	@Override
	public void init() {
		setServiceRequest(request);
		JTree jt = getJTree();
		jt.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
