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
package action;

import gui.*;
import gui.tree.*;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * allow quick fix of node/subnode relationship in tree view. this class display a recordselector with all record
 * retrived by original servicereques minus the selected records in the treeview.
 * 
 * WARING: This acction dont check circular references. so use with extreme caution. database may be drestroy
 * 
 * @author terry
 * 
 */
public class FixNodeSubnodeRelation extends TAbstractAction implements PropertyChangeListener {

	private TAbstractTree abstractTree;
	private Vector<Record> rList;;
	private ServiceRequest request;
	private FixNodeSubnodeRecord rdInput;
	private String nodeField, nameField, parentField;
	private JDialog dialog;
	private Record[] selRecords;

	public FixNodeSubnodeRelation(TAbstractTree tat, String no, String na, String pa) {
		super(RECORD_SCOPE);
		this.abstractTree = tat;
		this.nodeField = no;
		this.nameField = na;
		this.parentField = pa;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		request = abstractTree.getServiceRequest();

		selRecords = abstractTree.getRecords();
		rList = (Vector) TransactionsUtilities.sendTransaction(request).getData();
		for (Record r : selRecords) {
			rList.remove(r);
		}
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, request.getTableName(), rList);

		rdInput = new FixNodeSubnodeRecord(sr, nodeField, nameField);
		rdInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(rdInput, "action.FixNodeSubnodeRelation");
		dialog.setVisible(true);
	}
	
	@Override
	public void actionPerformed2() {
		DBAccess dba = ConnectionManager.getAccessTo(request.getTableName());
		Object o = rdInput.getFields().get("fixnode.newParent");
		for (Record r : selRecords) {
			//180126: (bug18.5) reset parent support
			if (o.equals("*none")) {
				o = (r.getFieldValue(parentField) instanceof Number) ? 0 : "";
			}
			r.setFieldValue(parentField, o);
			dba.update(r);
		}
		dialog.dispose();
		abstractTree.freshen();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// cancel
		if (evt.getNewValue() instanceof DefaultCancelAction) {
			dialog.dispose();
			return;
		}
		// update
		actionPerformed2();
	}

	/**
	 * dialgo entry to
	 * 
	 * @author terry
	 * 
	 */
	class FixNodeSubnodeRecord extends AbstractDataInput {

		FixNodeSubnodeRecord(ServiceRequest sr, String kfn, String vfn) {
			super("title_fixnode");

			RecordSelector rs = new RecordSelector(sr, kfn, vfn, null);
			//182601 (bug18.5): without parent support
			rs.insertItemAt(TStringUtils.getTEntry("tentry.none"), 0);
			addInputComponent("fixnode.newParent", rs, false, true);

			FormLayout lay = new FormLayout("left:pref, 3dlu, pref", // columns
					"p"); // rows
			// lay.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
			CellConstraints cc = new CellConstraints();
			PanelBuilder build = new PanelBuilder(lay);

			build.add(getLabelFor("fixnode.newParent"), cc.xy(1, 1));
			build.add(getInputComponent("fixnode.newParent"), cc.xy(3, 1));

			setDefaultActionBar();
			add(build.getPanel());
			preValidate(null);
		}

		@Override
		public void validateFields() {

		}
	}
}
