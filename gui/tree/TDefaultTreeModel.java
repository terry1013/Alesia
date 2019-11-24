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
package gui.tree;

import java.util.*;

import javax.swing.tree.*;

import core.*;
import core.datasource.*;

/**
 * esta clase sirve de model para las vistas en forma de arbol de datos contenidos en tablas de base de datos.
 * 
 * segun el tipo el valor para el argumento <code>sn</code> usado en el constructor se determina el metodo usado para
 * construir el arbol. estos son:
 * 
 * metodo <code>buildByNodeSubNode()</code> se construye un arbol usando una nombre de campo que identifica el nodo y
 * otro que identifica el sub-node (indica el nodo al cual pertenece). este es el metodo usado si el argumento
 * <code>sn</code> del constructor es distinto de <code>null</code>
 * 
 * metodo <code>buildByLengthOfField()</code> se construye un arbol donde los nodos y sub-nodos estan identificados por
 * la longitud y el contenido de un unico campo pasado como argumento en el constructor. este es el metodo usado si el
 * argumento <code>sn</code> del constructor es <code>null</code>
 */
public class TDefaultTreeModel extends DefaultTreeModel {

	private ServiceRequest sRequest;
	private ServiceResponse sResponse;
	private String nodeIdField, nodeNameField, subNodeField;
	private Record rcdModel;

	public TDefaultTreeModel() {
		super(null);
	}

	public TDefaultTreeModel(String no, String na, String sn) {
		this();
		this.nodeIdField = no;
		this.nodeNameField = na;
		this.subNodeField = sn;
	}

	public Record getModel() {
		return new Record(rcdModel);
	}

	public ServiceRequest getServiceRequest() {
		return sRequest;
	}

	/**
	 * return the {@link ServiceResponse}. Unfrecuently but somne times the service response contain aditional data
	 * needed by other class
	 * 
	 * @return ServiceResponse;
	 */
	public ServiceResponse getServiceResponse() {
		return sResponse;
	}

	public void setServiceRequest(ServiceRequest sr) {
		this.sRequest = sr;
		if (subNodeField == null) {
			buildByStringToken();
		} else {
			buildByNodeSubNode();
		}
	}

	/**
	 * metodo <code>buildByNodeSubNode()</code> se construye un arbo usando una nombre de campo que identifica el nodo y
	 * otro que identifica el sub-node (indica el nodo al cual pertenece). este es el metodo usado si el argumento
	 * <code>sn</code> del constructor es distinto de <code>null</code>
	 */
	private void buildByNodeSubNode() {
		Vector nodes = new Vector();
		sResponse = ServiceConnection.sendTransaction(sRequest);

		// 180210: copy the original content because this method destroy the source vector
		Vector src = (Vector) sResponse.getData();
		Vector dta = new Vector(src);

		this.rcdModel = (Record) sResponse.getParameter(ServiceResponse.RECORD_MODEL);
		int nd_cnt = 0;
		// 171203: node_val can be number or String
		// String node_val = "";
		String node_val = rcdModel.getFieldValue(nodeIdField).toString();
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new TEntry(new Record(rcdModel), "root"));
		nodes.add(dmtn);
		/*
		 * for (Object object : dta) { Record r = (Record) object; String pr = ""; for (int i = 0; i <
		 * r.getFieldCount(); i++) { pr += r.getFieldValue(i) + "\t"; } System.out.println(pr); }
		 */
		while (dta.size() > 0) {
			for (int k = 0; k < dta.size(); k++) {
				Record rcd = (Record) dta.elementAt(k);
				String key = rcd.getFieldValue(subNodeField).toString().trim();
				if (key.equals(node_val)) {

					// old addNode()
					dmtn = (DefaultMutableTreeNode) nodes.elementAt(nd_cnt);
					DefaultMutableTreeNode m = new DefaultMutableTreeNode(new TEntry(rcd,
							rcd.getFieldValue(nodeNameField)));
					dmtn.add(m);
					nodes.add(m);

					dta.remove(k);
					k--;
				}
			}
			nd_cnt = (nd_cnt < nodes.size()) ? nd_cnt + 1 : 0;
			dmtn = (DefaultMutableTreeNode) nodes.elementAt(nd_cnt);
			Record r = (Record) ((TEntry) dmtn.getUserObject()).getKey();
			node_val = r.getFieldValue(nodeIdField).toString();
		}
		setRoot((DefaultMutableTreeNode) nodes.elementAt(0));
	}

	/**
	 * metodo que contruye la estructura interna del arbol analizando los valores dentro del campo {@link #nodeIdField}.
	 */
	private void buildByStringToken() {
		Hashtable<String, DefaultMutableTreeNode> key_node = new Hashtable();
		sResponse = ServiceConnection.sendTransaction(sRequest);
		Vector data = (Vector) sResponse.getData();
		String pathsep = null;
		this.rcdModel = (Record) sResponse.getParameter(ServiceResponse.RECORD_MODEL);
		// 171203: node_val can be number or String
		// String nod_id = "";
		String nod_id = rcdModel.getFieldValue(nodeIdField).toString();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TEntry(new Record(rcdModel), "root"));
		key_node.put(nod_id, root);

		// first scan: natural sort and path separator (firs ocurrence used)
		ArrayList<TEntry> sortl = new ArrayList<TEntry>();
		for (int k = 0; k < data.size(); k++) {
			Record rcd = (Record) data.elementAt(k);
			String key = rcd.getFieldValue(nodeIdField).toString().trim();
			if (pathsep == null) {
				pathsep = key.contains(".") ? "." : pathsep;
				pathsep = key.contains("/") ? "/" : pathsep;
			}
			sortl.add(new TEntry(rcd, key));
		}

		Collections.sort(sortl);

		// second scan: build tree
		for (int k = 0; k < sortl.size(); k++) {
			TEntry te = (TEntry) sortl.get(k);
			String key = (String) te.getValue();
			int li = key.lastIndexOf(pathsep);
			DefaultMutableTreeNode dmtn = root;
			while (li > 0) {
				String ph = key.substring(0, li);
				DefaultMutableTreeNode n = key_node.get(ph);
				dmtn = n == null ? root : n;
				// if a node is found, exits loop
				li = n == null ? ph.lastIndexOf(pathsep) : -1;
			}
			Record rcd = (Record) te.getKey();
			DefaultMutableTreeNode m = new DefaultMutableTreeNode(new TEntry(rcd, rcd.getFieldValue(nodeNameField)));
			dmtn.add(m);
			key_node.put(key, m);
		}
		setRoot(root);
	}
}
