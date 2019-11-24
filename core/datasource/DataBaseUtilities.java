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

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;

import core.*;

/*
 *  contiene metodos de uso comun por parte de las clases que realizan transacciones de base de
 * datos
 */
public class DataBaseUtilities {

	/**
	 * metodo que retorna un registro con estructura predice;ada para direcciones.
	 * 
	 * @return registro con formato para direcciones
	 */
	public static Record getAddressRecord() {
		Field flds[] = new Field[]{new Field("state", "", 20), new Field("city", "", 20),
				new Field("district", "", 20), new Field("parish", "", 20), new Field("urbanitation", "", 20),
				new Field("street", "", 20), new Field("building", "", 20), new Field("establishment", "", 20),
				new Field("geographic_code", "", 4), new Field("postal_zone", "", 4), new Field("telephone_1", "", 20),
				new Field("telephone_2", "", 20), new Field("Fax", "", 20)};
		return new Record("Address", flds);
	}

	public static String getSerializableRecord(Record rcd) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rcd);
	}

	/**
	 * este metodo recibe un registro y efectua la serializacion estadar que permite guardar los datos de un registro
	 * completo en un campo dentro de otro campo de una tabla de base de datos
	 * 
	 * @param rcd - registro
	 * @return serializacion.
	 * 
	 *         public static String getSerializableRecord2(Record rcd) { StringBuffer sb = new StringBuffer(); int cc =
	 *         rcd.getFieldCount();
	 * 
	 *         // header Random r = new Random(); String serid = "<" + String.valueOf(r.nextInt(9999)) + ">";
	 *         sb.append(rcd.getTableName() + "<;>" + serid + "<;>" + serid); // data for (int j = 0; j < cc; j++) {
	 *         Object cls = rcd.getFieldValue(j); String clsn = cls.getClass().getName(); String val =
	 *         rcd.getFieldValue(j).toString(); if (cls instanceof byte[]) { clsn = "java.lang.Object"; val = new
	 *         String((byte[]) rcd.getFieldValue(j)); } sb.append(rcd.getFieldName(j) + serid + val + serid +
	 *         String.valueOf(rcd.getFieldSize(j)) + serid + clsn + ((j + 1 < cc) ? serid : "")); } return
	 *         sb.toString(); }
	 */

	public static Record getFromSerializedForm(String seria) {
		XStream xstream = new XStream(new DomDriver());
		return (Record) xstream.fromXML(seria);
	}

	/**
	 * este metodo retorna una instancia de <code>Record</code> basado en la serializacion estandar (generada por
	 * <code>getSerializableRecord</code>) pasada como argumento.
	 * 
	 * Nota: la deserializacion es independiente de la tabla, por lo tanto no se conoce la profundidad de la clave,
	 * 
	 * @param seria - serializacion estandar
	 * 
	 * @return Record
	 * 
	 *         TODO: delete
	 * 
	 * 
	 *         public static Record getFromSerializedForm2(String seria) { // header String[] head = seria.split("<;>");
	 *         String tn = head[0]; String serid = head[1]; // data String[] fn_fv_si_cl = seria.split(serid); // String
	 *         tn = fn_fv_si_cl[0]; int elem = (fn_fv_si_cl.length - 2) / 4; TEntry[] flds = new TEntry[elem]; int[] siz
	 *         = new int[elem]; try { Object obj = null; for (int k = 2; k < fn_fv_si_cl.length; k += 4) { String cl =
	 *         fn_fv_si_cl[k + 3]; String fv = fn_fv_si_cl[k + 1]; boolean prp = false; if (cl.equals("java.sql.Time"))
	 *         { obj = getInstance(Types.TIME, fv, 0); prp = true; } if (cl.equals("java.lang.Object")) { obj =
	 *         fv.getBytes(); prp = true; } if (cl.equals("java.sql.Date")) { obj = getInstance(Types.DATE, fv, 0); prp
	 *         = true; } if (!prp) { // Class cls = Class.forName(fn_fv_si_cl[k + 3]); Class cls = Class.forName(cl);
	 *         Constructor cnt = cls.getConstructor(new Class[]{String.class}); // obj = cnt.newInstance(new Object[] {
	 *         fn_fv_si_cl[k + 1] }); obj = cnt.newInstance(new Object[]{fv}); } flds[k / 4] = new
	 *         TEntry(fn_fv_si_cl[k], obj); siz[k / 4] = Integer.parseInt(fn_fv_si_cl[k + 2]); } } catch (Exception e) {
	 *         SystemLog.logException(e); } return new Record(tn, 0, flds, siz); }
	 */

	/**
	 * este metodo retorna una instancia de <code>Record</code> basado en la serializacion estandar (generada por
	 * <code>getSerializableRecord</code>) pasada como argumento.
	 * 
	 * Nota: la deserializacion es independiente de la tabla, por lo tanto no se conoce la profundidad de la clave,
	 * 
	 * @param seria - serializacion estandar
	 * 
	 * @return Record public static Record getFromSerializedForm(String seria) { String[] fn_fv_si_cl =
	 *         seria.split("<terry>"); String tn = fn_fv_si_cl[0]; int elem = (fn_fv_si_cl.length - 1) / 4; LTEntry[]
	 *         flds = new LTEntry[elem]; int[] siz = new int[elem]; try { Object obj; for (int k = 1; k <
	 *         fn_fv_si_cl.length; k += 4) { if (fn_fv_si_cl[k + 3].equals("java.lang.Object")) { obj = fn_fv_si_cl[k +
	 *         1].getBytes(); } else { if (fn_fv_si_cl[k + 3].equals("java.sql.Time")) { obj =
	 *         Time.valueOf(fn_fv_si_cl[k + 1]); } else { Class cls = Class.forName(fn_fv_si_cl[k + 3]); Constructor cnt
	 *         = cls.getConstructor(new Class[]{String.class}); obj = cnt.newInstance(new Object[]{fn_fv_si_cl[k + 1]});
	 *         } } flds[k/4] = new LTEntry(fn_fv_si_cl[k], obj); siz[k/4] = Integer.parseInt(fn_fv_si_cl[k + 2]); } }
	 *         catch (Exception e) { SystemLog.logException(e); // nada } return new Record(tn, 0, flds, siz); }
	 */

	/**
	 * Metodo que ejecuta una sentencia sql compleja. este retorna los datos seleccionados en formato estandar.
	 * 
	 * @param tn - nombre del archivo
	 * @param sqls - sentencia SQL
	 * 
	 * @return vector con datos resultantes de la seleccion.
	 * @throws SQLException public static Vector complexQuery(String tn, String sqls) { Vector v = new Vector(); try {
	 *         Statement sta = ConnectionManager.getConnection(tn).createStatement(); ResultSet rs =
	 *         sta.executeQuery(sqls); // v.add(createModel(tn, rs)); while (rs.next()) { Record r = getRecord(tn, rs);
	 *         v.addElement(r); } rs.close(); sta.close(); } catch (Exception e) { SystemLog.logException1(e); } return
	 *         v; }
	 */

	/**
	 * retorna un entero que representa el valor <code>count(*)</code> de la cuenta de elementos reotrnados para la
	 * table <code>tn</code> segun la sentencia <code>where</code>
	 * 
	 * @param tn - nombre de table
	 * @param qry - valores para variable WHERE
	 * @return nro de elementos public static Integer getCountOf(String tn, String where) { String qry =
	 *         "SELECT count(*) FROM " + tn + (where == null ? "" : " WHERE " + where); Vector v =
	 *         DataBaseUtilities.complexQuery(tn, qry); Record r = (Record) v.elementAt(0); Integer i = ((Number)
	 *         r.getFieldValue("count(*)")).intValue(); return i; }
	 */

	/**
	 * utilitario que permite la ejecucion de una sentencia sql que no requiere resultado retorno de datos
	 * 
	 * @param sql - sentencia sql.
	 * @param tn - nombre de tabla
	 */
	public static void executeUpdate(String sql, String tn) {
		try {
			Statement sta = ConnectionManager.getConnection(tn).createStatement();
			sta.executeUpdate(sql);
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}
}
