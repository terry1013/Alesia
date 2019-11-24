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

import java.sql.*;
import java.util.*;

/**
 * metodos utilitarios para transacciones
 * 
 */
public class TransactionsUtilities {

	/**
	 * Return a new instance of transaction of name <code>tn</code>. for plugins transactions, <code>tn</code> argument
	 * bust be fully quelified. i.e: plugins.name.TrasnsactinName
	 * 
	 * @param tn - transaction name.
	 * 
	 * @return instance of {@link AbstractTransaction}
	 * 
	 * @throws Exception
	 */
	public static AbstractTransaction loadTransaction(String tn) throws Exception {
		String qtn = "service." + tn;

		// check for external transaction (plugins transactions)
		if (tn.split("[.]").length > 0) {
			qtn = tn;
		}
		Class cls = Class.forName(qtn);
		AbstractTransaction at = (AbstractTransaction) cls.newInstance();
		return at;
	}

	/**
	 * envia una solicitud de servicio al servidor. esta solicitud sera procesada y el resultado de la operacion sera
	 * devuelto al solicitante.
	 * 
	 * @return <code>ServiceResponse</code>
	 */
	public static ServiceResponse sendTransaction(ServiceRequest sr) {

		ServiceResponse resp = new ServiceResponse(null);
		try {

			boolean pro = false;
			// consulta a base de datos.
			if (sr.getName().equals(ServiceRequest.DB_QUERY)) {
				DBAccess dba = ConnectionManager.getAccessTo(sr.getTableName());
				// 180311: new parameter ignore security for transactions
				Boolean is = (Boolean) sr.getParameter(ServiceRequest.IGNORE_SECURITY);
				// 180406: new parameter for maxrow in search
				Integer qs = (Integer) sr.getParameter(ServiceRequest.DB_QUERY_SIZE);
				int qsize = qs == null ? 0 : qs;
				if (is != null && is == true) {
					dba.ignoreSecurity();
				}
				Vector v = dba.search((String) sr.getData(), (String) sr.getParameter(ServiceRequest.ORDER_BY), qsize);
				// filtro
				String ff = (String) sr.getParameter(ServiceRequest.FILTER_FIELDS);
				String fv = (String) sr.getParameter(ServiceRequest.FILTER_VALUE);
				filterList(v, ff, fv);
				resp.setData(v);
				resp.setParameter(ServiceResponse.RECORD_MODEL, dba.getModel());
				pro = true;
			}

			// union de archivos por coincidencia de campo
			if (sr.getName().equals(ServiceRequest.DB_JOIN_QUERY)) {
				resp = DBJoinQuery(sr);
				pro = true;
			}

			// nuevo registro.
			if (sr.getName().equals(ServiceRequest.DB_ADD)) {
				String tab = sr.getTableName();
				DBAccess dba = ConnectionManager.getAccessTo(tab);
				dba.add((Record) sr.getData());
				pro = true;
			}

			// escribir.
			if (sr.getName().equals(ServiceRequest.DB_WRITE)) {
				String tab = sr.getTableName();
				DBAccess dba = ConnectionManager.getAccessTo(tab);
				Record r = (Record) sr.getData();
				if (dba.exist(r) != null) {
					dba.update(r);
				} else {
					dba.add(r);
				}
				pro = true;
			}

			// actualizacion
			if (sr.getName().equals(ServiceRequest.DB_UPDATE)) {
				DBAccess dba = ConnectionManager.getAccessTo(sr.getTableName());
				dba.update((Record) sr.getData());
				pro = true;
			}

			// existencia. esta transaccion determina el tipo de metodo exist() que se
			// ejecutara en DBaseAccess determinando la instancia de los datos de la
			// solicitud
			if (sr.getName().equals(ServiceRequest.DB_EXIST)) {
				DBAccess dba = ConnectionManager.getAccessTo(sr.getTableName());
				if (sr.getData() instanceof String) {
					resp.setData(dba.exist((String) sr.getData()));
				} else {
					resp.setData(dba.exist((Record) sr.getData()));
				}
				pro = true;
			}

			// solicita modelo de registro para una tabla determinada
			if (sr.getName().equals(ServiceRequest.DB_REQUEST_MODEL)) {
				DBAccess dba = ConnectionManager.getAccessTo(sr.getTableName());
				resp.setData(dba.getModel());
				pro = true;
			}

			// suprimir
			if (sr.getName().equals(ServiceRequest.DB_DELETE)) {
				String tab = sr.getTableName();
				DBAccess dba = ConnectionManager.getAccessTo(tab);
				dba.delete((Record) sr.getData());
				pro = true;
			}

			// consulta compleja
			if (sr.getName().equals(ServiceRequest.DB_COMPLEX_QUERY)) {
				/*
				 * resp.setData(DataBaseUtilities.complexQuery(sr.getTableName(), (String) sr.getData())); DBAccess dba
				 * = ConnectionManager.getAccessTo(sr.getTableName()); resp.setParameter(ServiceResponse.RECORD_MODEL,
				 * dba.getModel()); pro = true;
				 */
			}
			// ejecutar actualizacion. no retorna datos
			if (sr.getName().equals(ServiceRequest.DB_EXECUTE_UPDATE)) {
				DataBaseUtilities.executeUpdate((String) sr.getData(), sr.getTableName());
				pro = true;
			}

			// -------------------------------
			// Pasados a transacciones
			// -------------------------------
			if (!pro) {
				AbstractTransaction at = TransactionsUtilities.loadTransaction(sr.getName());
				at.setServiceRequest(sr);
				resp = at.commit();
			}
		} catch (Exception e) {
			if (e instanceof SQLException) {
				((SQLException) e).getNextException();
			}
			resp.setParameter(ServiceResponse.EXCEPTION, e);
		}
		return resp;
	}

	/**
	 * this method return a {@link Vector} of record by joining left file (tablename parameter in ServiceRequest) with
	 * right file ({@link ServiceRequest#RIGHT_TABLE_NAME} parameter) using {@link ServiceRequest#LEFT_KEY_FILED} =
	 * {@link ServiceRequest#RIGHT_KEY_FIELD} as join argument. the result list is a Vector of Records where every left
	 * file field + the fields especifyed at {@link ServiceRequest#RIGHT_FIELDS} appended at end.
	 * 
	 * @param sr - ServiceRequest with parameters
	 * 
	 * @return servicerResponse
	 * 
	 */
	private static ServiceResponse DBJoinQuery(ServiceRequest sr) {
		ServiceResponse resp = new ServiceResponse(null);
		DBAccess ldba = ConnectionManager.getAccessTo(sr.getTableName());
		Record lrcdmod = ldba.getModel();
		DBAccess rdba = ConnectionManager.getAccessTo((String) sr.getParameter(ServiceRequest.RIGHT_TABLE_NAME));
		Record rrcdmod = rdba.getModel();
		String[] rightfn = ((String) sr.getParameter(ServiceRequest.RIGHT_FIELDS)).split(";");
		String left_f = (String) sr.getParameter(ServiceRequest.LEFT_KEY_FILED);
		String righ_f = (String) sr.getParameter(ServiceRequest.RIGHT_KEY_FIELD);

		// new model based on left record model + right field(s)
		Record rmodel = new Record(lrcdmod);
		for (String fn : rightfn) {
			rmodel.addNewField(fn, rrcdmod.getFieldValue(fn), rrcdmod.getFieldSize(fn), rrcdmod.getFieldPresition(fn),
					rrcdmod.isKeyField(fn), rrcdmod.isNullable(fn), rrcdmod.getFieldValue(fn).getClass().getName());
		}

		Vector<Record> rcdList = ldba.search((String) sr.getData(), (String) sr.getParameter(ServiceRequest.ORDER_BY));
		Vector<Record> returnlist = new Vector<Record>(rcdList.size());

		// foreach left record, look for right record in right file.
		for (Record l_rcd : rcdList) {
			Object objv = l_rcd.getFieldValue(left_f);
			String qu = (objv instanceof Number) ? "" : "'";
			Record r = rdba.exist(righ_f + " = " + qu + objv + qu);
			Record nr = new Record(l_rcd);
			if (r != null) {
				for (String fn : rightfn) {
					nr.addNewField(fn, r.getFieldValue(fn), r.getFieldSize(fn), r.getFieldPresition(fn),
							r.isKeyField(fn), r.isNullable(fn), r.getFieldValue(fn).getClass().getName());
					returnlist.add(nr);
				}
			}
		}

		// filtro
		String ff = (String) sr.getParameter(ServiceRequest.FILTER_FIELDS);
		String fv = (String) sr.getParameter(ServiceRequest.FILTER_VALUE);
		filterList(returnlist, ff, fv);
		resp.setData(returnlist);
		resp.setParameter(ServiceResponse.RECORD_MODEL, rmodel);
		return resp;
	}

	/**
	 * metodo que implementa el filtrado de registos. Si los campos descritos en
	 * <code>ServiceReques.FILTER_FIELDS</code> no contienen la secuencia de caracteres descritas en
	 * <code>ServiceReques.FILTER_VALUE</code> se remueve del vector dejando solo los registros que si los contengan
	 * 
	 * @param src <code>Vector<Record></code> con los elementos a filtrar. este vector sera modificado por este metodo
	 * @param ff - lista de campos separados x ;
	 * @param fv - valor a buscar
	 */
	public static void filterList(Vector src, String ff, String fv) {
		if (ff != null & !(fv == null || fv.equals(""))) {
			String fv1 = fv.toUpperCase();
			String ff1 = ff.toUpperCase();
			boolean find;
			for (int i = 0; i < src.size(); i++) {
				Record r = (Record) src.elementAt(i);
				find = false;
				for (int j = 0; j < r.getFieldCount(); j++) {
					String fn = r.getFieldName(j);
					if (ff1.contains(fn)) {
						String v1 = r.getFieldValue(j).toString().toUpperCase();
						if (v1.contains(fv1)) {
							find = true;
						}
					}
				}
				if (!find) {
					src.remove(i);
					i--;
				}
			}
		}
	}
}
