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

import org.javalite.activejdbc.*;

import core.*;
import core.datasource.model.*;

/**
 * esta clase representa el principal medio de acceso a los datos persistentes. Esta controla los acceso a las distintas
 * tablas, monitorea las conecciones y mantiene las operaciones coordinadas con el resto de las capas que conforman la
 * aplicacion.
 * 
 */
public class ConnectionManager {

	private static Hashtable<String, DatabaseMetaData> dbMetaData = new Hashtable();
	private static Hashtable<String, Properties> dbProperties = new Hashtable();
	private static Connection systemDBC;
	private static Hashtable dbTableConnection = new Hashtable();
	private static Hashtable dbConnection = new Hashtable();
	private static Hashtable dbAccess = new Hashtable();
	private static Vector dbTableFK = new Vector();

	private static DB localDB;

	// private static AutoConnection autoC;

	/**
	 * realiza coneccion con las bases de datos. este metodo actualiza las lista de conecciones y de metadata los
	 * nombres de las tablas en el orden en que son especificadas las bases de datos.
	 * 
	 * @param - murl - nombre de base de datos principal o <code>null</code> para coneccicon con base de datos del
	 *        sistema
	 * 
	 * @throws Exception - error durante la coneccion
	 * 
	 */
	public static void connect() throws Exception {
		String dbDriver = "org.hsqldb.jdbcDriver";
		String dbUrl = "jdbc:hsqldb:file:_local";
		Base.open(dbDriver, dbUrl, "sa", "");
		localDB = new DB("localDb");
		localDB.open(dbDriver, dbUrl, null);
		}
	public static DB getDB() {
		return localDB;
	}



	/**
	 * Test the connection profile check that all parameters are correctly in place acording to the framework rules.
	 * 
	 * @param pr - conection profile.
	 * @throws TError message for the user if any issue is found
	 */
	public static void testProfile(Record pr) throws Exception {
		// for oracle connections, schema is mandatory
		String drn = (String) pr.getFieldValue("t_cndrname");
		String prps = (String) pr.getFieldValue("t_cnextended_prp");
		if (drn.equalsIgnoreCase("Oracle")) {
			if (!prps.contains("schema")) {
				throw new IllegalArgumentException("the connection parameter ");
			}
		}
	}

	/**
	 * Set the connection to external database profiled by the <code>cf</code> argument. This method use the entire
	 * properties setted in Extended properties field form the profile record.
	 * 
	 * @param cf - profile connection (record from T_CONNECTIONS file)
	 * @param conf - <code>true</code> to configure the enviorement. false otherwise. Use false for test purpose
	 * 
	 * @throws Exception if any error
	 */
	public static void connect(Record cf, boolean conf) throws Exception {
		Properties prps = new Properties();
		TStringUtils.parseProperties((String) cf.getFieldValue("t_cnextended_prp"), prps);

		// user and passwort to properties
		prps.put("user", (String) cf.getFieldValue("t_cnuser"));
		prps.put("password", (String) cf.getFieldValue("t_cnpassword"));

		Class.forName((String) cf.getFieldValue("t_cndriver")).newInstance();
		Connection con = DriverManager.getConnection((String) cf.getFieldValue("t_cnurl"), prps);
		if (conf) {
			prps.put("filterTable", (String) cf.getFieldValue("t_cntable_filter"));
			prps.put("connectionName", (String) cf.getFieldValue("t_cnname"));
			configureConn(con, prps);
		}
	}

	/**
	 * Set the connection to external database profiled by the <code>cf</code> argument and setted the enviorement
	 * 
	 * @param cf - Connection profile
	 * @see #connect(Record, boolean)
	 * 
	 * @throws Exception if any error
	 */
	public static void connect(Record cf) throws Exception {
		connect(cf, true);
	}

	/**
	 * retorna <code>true</code> si las bases de datos locales y remotas estan conectadas. si no lo estan, este metodo
	 * establece una condicion general de no coneccion.
	 * 
	 * @return <code>true o false</code> si hay coneccion o no
	 */
	public static boolean isConnected() {
		boolean con = false;
		try {
			boolean lc = (systemDBC != null && !systemDBC.isClosed());
			// con = rc && lc;
			con = lc;
		} catch (Exception e) {
			SystemLog.logException1(e, false);
		}
		return con;
	}

	/**
	 * return the {@link Connection} to database asociated whith connection name. for internal database, pass "" as
	 * parameter
	 * 
	 * @param cn - conection name or "" for internal DB
	 * 
	 * @return connection to database
	 */
	public static Connection getDBConnection(String cn) {
		return (Connection) dbConnection.get(cn.toUpperCase());
	}

	/**
	 * Compare the table name argumento against the list of tables. return <code>true</code> iff the table name are not
	 * in the list. the list element may contanin regular expretions. if the <code>tblsf</code> is empty, allow all
	 * 
	 * @param tn - the table name
	 * @param tblsf - list of not allowed table names
	 * 
	 * @return <code>true</code> if the tn are allowed
	 */
	public static boolean allowTable(String tn, String[] tblsf) {
		for (String ft : tblsf) {
			if (tn.matches(ft)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Actualiza relaciones tabla -> coneccion, y metadata
	 * 
	 * @param con - conexion
	 * @param sch - esquema name or n
	 */
	private static void configureConn(Connection con, Properties prps) {
		try {
			String sch = prps.getProperty("schema", null);
			String ft = prps.getProperty("filterTable", null);
			String conn = prps.getProperty("connectionName", "");
			String[] tblsf = ft == null ? new String[0] : ft.split("\n");
			DatabaseMetaData meta = con.getMetaData();
			ResultSet res = meta.getTables(null, sch, null, new String[]{"TABLE", "VIEW"});
			while (res.next()) {
				String tn = res.getString("TABLE_NAME").toUpperCase();
				if (allowTable(tn, tblsf)) {
					dbMetaData.put(tn, meta);
					dbProperties.put(tn, prps);
					dbTableConnection.put(tn, con);
					dbConnection.put(conn, con);
					findFK(tn, meta, sch);
				}
			}
			/*
			 * ResultSet res1 = meta.getProcedures(null, sch, "%"); while (res1.next()) { String pn =
			 * res1.getString("PROCEDURE_NAME"); if (pn.equals("GENERATOR")) { SLEPlanC.callStoreProcedure(1L, "01",
			 * "01"); } // System.out.println(res1.getString("PROCEDURE_CAT")+ " " +
			 * res1.getString("PROCEDURE_SCHEM")+" "+res1.getString("PROCEDURE_NAME")); }
			 */
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}

	/**
	 * este metodo recopila toda la informacion necesaria sobre las claves foraneas definidas dentro en las tablas que
	 * no permitan operacion de supresion
	 * 
	 * @param tn - nombre de tabla
	 * @param meta - instancia de <code>DatabaseMetaData</code>
	 */
	private static void findFK(String tn, DatabaseMetaData meta, String sch) {
		try {
			ResultSet res = meta.getExportedKeys(null, sch, tn);
			while (res.next()) {
				/*
				 * System.out.println(tn); System.out.println("\t PKTABLE_NAME " + res.getString("PKTABLE_NAME"));
				 * System.out.println("\t FKTABLE_NAME " + res.getString("FKTABLE_NAME"));
				 * System.out.println("\t FKCOLUMN_NAME " + res.getString("FKCOLUMN_NAME"));
				 * System.out.println("\t DELETE_RULE " + res.getShort("DELETE_RULE"));
				 */
				if (res.getShort("DELETE_RULE") == DatabaseMetaData.importedKeyRestrict) {
					Hashtable info = new Hashtable();
					info.put("PKTABLE_NAME", res.getString("PKTABLE_NAME"));
					info.put("FKTABLE_NAME", res.getString("FKTABLE_NAME"));
					info.put("FKCOLUMN_NAME", res.getString("FKCOLUMN_NAME"));
					dbTableFK.addElement(info);
				}
			}
			res.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * retorna informacion de claves foraneas para la tabla pasada como argumento
	 * 
	 * @param = fn - nombre de tabla
	 * @return - informacion
	 */
	public static String getFKListFor(String tn) {
		String inf = "";
		for (int i = 0; i < dbTableFK.size(); i++) {
			Hashtable ht = (Hashtable) dbTableFK.elementAt(i);
			if (ht.get("PKTABLE_NAME").equals(tn)) {
				String infptt = TStringUtils.getString("foreingKey.ms02");
				String c_t = infptt.replaceAll("<fld>", TStringUtils.getString((String) ht.get("FKCOLUMN_NAME")));
				c_t = c_t.replaceAll("<fldid>", (String) ht.get("FKCOLUMN_NAME"));
				c_t = c_t.replaceAll("<fileid>", (String) ht.get("FKTABLE_NAME"));
				inf += c_t.replaceAll("<file>", TStringUtils.getString((String) ht.get("FKTABLE_NAME")));
			}
		}
		return inf;
	}

	/**
	 * retorna metadata de base de datos
	 * 
	 * @return - databaseMetaData public static DatabaseMetaData getDatabaseMetaData(String tn) { return
	 *         (DatabaseMetaData) dbMetaData.get(tn.toUpperCase()); }
	 */

	/**
	 * retorna un acceso a tabla. si un acceso a la tabla pasado como argumento ya ha sido creado, este devuelbe dicho
	 * acceso, de lo contrario, crea uno nuevo y lo retorna.
	 * 
	 * @param tn - nombre de table
	 * @return acceso a tabla
	 */
	public static DBAccess getAccessTo(String tn) {
		DBAccess dba = null;
		String tn1 = tn.toUpperCase();
		if (dbAccess.get(tn1) != null) {
			dba = (DBAccess) dbAccess.get(tn1);
		} else {
			dba = new DBAccess(tn1, dbMetaData.get(tn1), dbProperties.get(tn1));
			dbAccess.put(tn1, dba);
		}
		return dba;
	}

	/**
	 * Finaliza transacciones con bases de datos
	 * 
	 * 
	 */
	public static void shutdown() {
		try {
//			localDB.exec("SHUTDOWN");
			localDB.close();
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}
}
