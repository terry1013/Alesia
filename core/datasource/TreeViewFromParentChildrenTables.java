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
package core.datasource;

import java.util.*;

/**
 * this transactions build a tree view from the relation between parent table and children table. The parent table is
 * the tablename parameter in service request. the initial list of data (parent table) is treated as a normal database
 * search filter by where clause.
 * <ul>
 * <li> the key fields of both records are appended
 * <li>the parameters {@link ServiceRequest#NODE_FIELD} and {@link ServiceRequest#SUB_NODE_FIELD} ar of diferent class,
 * <code>node</code> and <code>subnode</code> fields in return list are converted to String representations trying to
 * equalize the values. ie. for numbers 0 convert to ""
 * </ul>
 * 
 * @author terry
 * 
 */
public class TreeViewFromParentChildrenTables extends AbstractTransaction {

	private Record rModel;
	private Vector<Record> rList;
	private boolean equalize;

	@Override
	public ServiceResponse commit() {
		ServiceResponse resp = new ServiceResponse(null);

		// service parameters
		String parentfile = request.getTableName();
		String childfile = (String) request.getParameter(ServiceRequest.CHILDREN_TABLE_NAME);
		String partxtfn = (String) request.getParameter(ServiceRequest.PARENT_STRING);
		String chitxtfn = (String) request.getParameter(ServiceRequest.CHILDREN_STRING);
		String leftfn = (String) request.getParameter(ServiceRequest.PARENT_JOIN_BY);
		String rightfn = (String) request.getParameter(ServiceRequest.CHILDREN_JOIN_BY);
		String nodefn = (String) request.getParameter(ServiceRequest.NODE_FIELD);
		String subnodefn = (String) request.getParameter(ServiceRequest.SUB_NODE_FIELD);

		DBAccess pardba = ConnectionManager.getAccessTo(parentfile);
		DBAccess chidba = ConnectionManager.getAccessTo(childfile);

		// create new record model based on incoming parameters
		Record parrcd = pardba.getModel();
		Record chircd = chidba.getModel();
		rModel = new Record("", new Field[0]);
		Field nf = parrcd.getField(nodefn);
		Field snf = chircd.getField(subnodefn);

		// if node/subnode are direferent class. equalize to string
		if (nf.value.getClass().equals(snf.value.getClass())) {
			equalize = false;
			rModel.addNewField("node", nf.value, nf.length, nf.presition, false, nf.isnullable, nf.classname);
			rModel.addNewField("subnode", snf.value, snf.length, snf.presition, false, snf.isnullable,
					snf.classname);
		} else {
			equalize = true;
			rModel.addNewField(new Field("node", "", 10));
			rModel.addNewField(new Field("subnode", "", 10));
		}
		rModel.addNewField(new Field("name", "", 20));
		rModel.addNewField(new Field("icon", "", 20));

		// fore every record in parent file, append children recors
		Vector<Record> parlist = pardba.search((String) request.getData(), null);
		this.rList = new Vector<Record>();
		for (Record parr : parlist) {
			Record rcd = new Record(rModel);
			rcd.setFieldValue("node", getValue(parr.getFieldValue(nodefn)));
			rcd.setFieldValue("subnode", getValue(rModel.getFieldValue("subnode")));
			rcd.setFieldValue("name", parr.getFieldValue(partxtfn));
			rcd.setFieldValue("icon", parentfile);
			copyKeyFields(parr, rcd);
			rList.add(rcd);
			// append children recods
			String sep = (rModel.getFieldValue("node") instanceof Number) ? "" : "'";
			Vector<Record> chilist = chidba.search(rightfn + " = " + sep + parr.getFieldValue(leftfn) + sep, null);
			for (Record chir : chilist) {
				rcd = new Record(rModel);
				rcd.setFieldValue("node", getValue(chir.getFieldValue(subnodefn)));
				rcd.setFieldValue("subnode", getValue(parr.getFieldValue(nodefn)));
				rcd.setFieldValue("name", chir.getFieldValue(chitxtfn));
				rcd.setFieldValue("icon", childfile);
				copyKeyFields(chir, rcd);
				rList.add(rcd);
			}
		}

		resp.setParameter(ServiceResponse.RECORD_MODEL, rModel);
		resp.setData(rList);
		return resp;
	}

	/**
	 * copy the key fields form soruce record to target record. plus, set the tablename
	 * 
	 * @param srcr - source record
	 * @param tarr - target record
	 */
	private void copyKeyFields(Record srcr, Record tarr) {
		tarr.setTableName(srcr.getTableName());
		for (int c = 0; c < srcr.getFieldCount(); c++) {
			Field kf = srcr.getField(c);
			if (kf.iskey) {
				tarr.addNewField(kf);
			}
			// 180217: pendient to implement
//			tarr.addNewField(srcr.getField(c));
		}
	}

	/**
	 * method that return the equalized value fo the <code>val</code>argument.<br>
	 * - numbers are converted to "" string
	 * 
	 * @param val - object to equalize
	 * 
	 * @return object to set in node or subnode field
	 */
	private Object getValue(Object val) {
		Object rv = val;
		if (equalize) {
			if (val instanceof Number) {
				Number nv = (Number) val;
				rv = nv.doubleValue() == 0.0 ? "" : nv.toString();
			}
		}
		return rv;
	}
}
