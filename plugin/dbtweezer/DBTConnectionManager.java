package plugin.dbtweezer;

import java.math.*;
import java.sql.*;
import java.util.*;

import org.apache.ddlutils.*;
import org.apache.ddlutils.model.*;
import org.apache.ddlutils.platform.oracle.*;

import core.*;
import core.datasource.*;

public class DBTConnectionManager {

	private static DBTConnectionManager source;
	public static String TABLES = "Tables";
	public static String TABLE_COLUMNS = "TableColumns";
	
	private Connection connection;
	private Properties myProperties;
	private DatabaseMetaData metaData;
	private String productName;
	private String schema, catalog;
	private Record conProfile;
	private Database databaseModel;
	private Platform platformModel;
	private String[] tableTypes = new String[]{"TABLE", "VIEW"};
	
	public static void connect(Record pr) throws Exception {
		source = new DBTConnectionManager(pr);
	}
	
	DBTConnectionManager(Record pr) throws Exception {
		String dbdriver = (String) pr.getFieldValue("t_cndriver");
		String dburl = (String) pr.getFieldValue("t_cnurl");
		Class.forName(dbdriver).newInstance();
		this.connection = DriverManager.getConnection(dburl, (String) pr.getFieldValue("t_cnuser"),
				(String) pr.getFieldValue("t_cnpassword"));
		this.myProperties = new Properties();
		TStringUtils.parseProperties((String) pr.getFieldValue("t_cnextended_prp"), myProperties);
		this.metaData = connection.getMetaData();
		this.schema = myProperties.getProperty("schema", null);
		this.catalog = myProperties.getProperty("catalog", null);
		productName = metaData.getDatabaseProductName();
//		String version = metaData.getDatabaseProductVersion();
		int maj = metaData.getDatabaseMajorVersion();
		int min = metaData.getDatabaseMinorVersion();
		this.conProfile = pr;
		SystemLog.info("Detected Database " + productName + " minorVersion: " + min + " MajorVersion: " + maj);
		if (productName.equalsIgnoreCase("Oracle")) {
			if (maj == 10) {
				platformModel = new Oracle10Platform();
			}
			if (maj == 9) {
				platformModel = new Oracle9Platform();
			}
			// 8 by default
			if (platformModel == null) {
				platformModel = new Oracle8Platform();
			}
		} else {
			platformModel = PlatformFactory.createNewPlatformInstance(dbdriver, dburl);
		}
		SystemLog.info("Platform " + platformModel.getClass().getSimpleName() + " selected.");
		databaseModel = platformModel.readModelFromDatabase(connection, "modelDB", catalog, schema, tableTypes);
	}
	public static Record getModelFor(String mid) {
		Record mod = null;
		if (mid.equals(TABLES)) {
			Field[] fs = new Field[]{new Field("TABLE_CAT", "", 30), new Field("TABLE_SCHEM", "", 30),
					new Field("TABLE_NAME", "", 30), new Field("TABLE_TYPE", "", 30), new Field("REMARKS", "", 30)};

			mod = new Record(mid, fs);
		}
		if (mid.equals(TABLE_COLUMNS)) {
			// DATA_TYPE converted to string
			Field[] fs = new Field[]{new Field("TABLE_CAT", "", 30), new Field("TABLE_SCHEM", "", 30),
					new Field("TABLE_NAME", "", 30), new Field("COLUMN_NAME", "", 30), new Field("DATA_TYPE", "", 2),
					new Field("TYPE_NAME", "", 30), new Field("COLUMN_SIZE", 0, 3), new Field("DECIMAL_DIGITS", 0, 3),
					new Field("REMARKS", "", 30), new Field("COLUMN_DEF", "", 30), new Field("IS_NULLABLE", "", 30)};
			//,				new Field("NULLABLE", 0, 2)};
			mod = new Record(mid, fs);
		}
		return mod;
	}

	public static DBTConnectionManager getSourceDB() {
		return source;
	}

	private static Record getRecord(ResultSet rset, Record mod) throws SQLException {
		Record nr = new Record(mod);
		for (int c = 0; c < mod.getFieldCount(); c++) {
			String fna = nr.getFieldName(c);
			Object v = rset.getObject(fna);
			// need String DATA_TYPE
			if (fna.equals("DATA_TYPE")) {
				v = v.toString();
			}
			if (v instanceof BigDecimal) {
				v = ((BigDecimal) v).intValue();
			}
			v = (v == null) ? "" : v;
			nr.setFieldValue(c, v);
			// System.out.println(mod.getTableName() + " - "+fna+"  - "+v.getClass().getName());
		}
		return nr;
	}

	public String getAlterTable(Record oldcolr, Record newcolr) {
		Database tmp = platformModel.readModelFromDatabase(connection, "modelDB", catalog, schema, tableTypes);
		Table tab = tmp.findTable((String) oldcolr.getFieldValue("table_name"));
		Column col = tab.findColumn((String) oldcolr.getFieldValue("column_name"));
		//
		int tnu = Integer.valueOf((String) newcolr.getFieldValue("data_type"));
		col.setName((String) newcolr.getFieldValue("column_name"));
		col.setTypeCode(tnu);
		col.setSize(newcolr.getFieldValue("column_size").toString());
		if (col.isOfNumericType()) {
			col.setScale((Integer) newcolr.getFieldValue("decimal_digits"));
		}
		col.setRequired(newcolr.getFieldValue("is_nullable").equals("NO"));
		return platformModel.getAlterTablesSql(connection, catalog, schema, tableTypes, tmp);
	}

	public Vector getColumns(String tn) {
		Vector rlst = new Vector();
		try {
			Record mod = getModelFor(TABLE_COLUMNS);
			ResultSet rset = metaData.getColumns(catalog, schema, tn, null);
			while (rset.next()) {
				Record newr = getRecord(rset, mod);
				rlst.add(newr);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rlst;
	}

	public Record getConectionRecord() {
		return conProfile;
	}

	public Connection getConnection() {
		return connection;
	}

	public String[] getFiels(String tn) {
		Vector<String> flds = new Vector();
		try {
			ResultSet rset = metaData.getColumns(catalog, schema, tn, null);
			while (rset.next()) {
				String v = rset.getString("COLUMN_NAME");
				flds.add(v);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (String[]) flds.toArray(new String[flds.size()]);
	}

	/**
	 * Return the row count of a database query with WHERE clause or the size of the table (if argument
	 * <code>wc == null</code>)
	 * 
	 * @param tn - table name
	 * @param wc - where clause or <code>null</code> to retrive the table size
	 * 
	 * @return numbers of row of file or query
	 */
	public int getRowCount(String tn, String wc) throws Exception {
		int count = -1;
		String sql = "SELECT count(1) FROM " + tn;
		if (wc != null) {
			sql += " WHERE " + wc;
		}
		Statement sts = connection.createStatement();
		ResultSet rset = sts.executeQuery(sql);
		while (rset.next()) {
			count = rset.getInt(1);
		}
		return count;
	}

	/**
	 * Return a filtered list of schemas in the database. <br/>
	 * The list is obtained by calling DatabaseMetadata.getSchemas(). <br/>
	 * If the filter is not null, all entries that are matched by the filter are removed from the result.
	 * 
	 * @param filter the ObjectNameFilter to apply
	 * @return a list of available schemas if supported by the database
	 * @see ObjectNameFilter#isExcluded(java.lang.String)
	 */
	public List<String> getSchemas() {
		ArrayList<String> result = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = metaData.getSchemas();
			while (rs.next()) {
				String schema = rs.getString(1);
				if (schema == null)
					continue;
				result.add(schema);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public ServiceRequest getServiceRequestFor(String dtat, String tn) {
		Vector tlst = null;
		if (dtat.equals(TABLES)) {
			tlst = getTables();
		}
		if (dtat.equals(TABLE_COLUMNS)) {
			tlst = getColumns(tn);
		}
		if (tlst == null) {
			throw new NullPointerException("bad argumento for getServiceRequestFor(String, String)");
		}
		Record rm = DBTConnectionManager.getModelFor(dtat);
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, dtat, tlst);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rm);
		return sr;
	}
	/**
	 * the default object from connection configuration
	 * 
	 * @return
	 */
	public Vector getTables() {
		return getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"});
	}

	public Vector getTables(String catalogPattern, String schemaPattern, String namePattern, String[] types) {
		Vector<Record> data = new Vector<Record>();
		String ft = (String) conProfile.getFieldValue("t_cntable_filter");
		String[] tblsf = ft == null ? new String[0] : ft.split("\n");
		try {
			Record mod = getModelFor(TABLES);
			ResultSet rset = metaData.getTables(catalogPattern, schemaPattern, namePattern, types);
			while (rset.next()) {
				// fiter tables
				if (ConnectionManager.allowTable(rset.getString("TABLE_NAME"), tblsf)) {
					Record newr = getRecord(rset, mod);
					// use the remarck field to store some data
					String rem = getTableRemarck(newr);
					newr.setFieldValue("REMARKS", rem);
					data.add(newr);
				}
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	public Vector<String> retrieveTableTypes() {
		Vector<String> result = new Vector<String>();
		ResultSet rs = null;

		try {
			rs = metaData.getTableTypes();
			while (rs != null && rs.next()) {
				String type = rs.getString(1);
				if (type == null)
					continue;
				// for some reason oracle sometimes returns
				// the types padded to a fixed length. I'm assuming
				// it doesn't harm for other DBMS as well to
				// trim the returned value...
				type = type.trim();

				if (isIndexType(type))
					continue;
				result.add(type);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getTableRemarck(Record rtbl) {
		String rem = "";
		Vector<Record> rcols = DBTConnectionManager.getSourceDB().getColumns((String) rtbl.getFieldValue("table_name"));
		for (Record rcol : rcols) {
			String dds = rcol.getFieldValue("decimal_digits").toString();
			int dd = dds.equals("") ? 0 : Integer.valueOf(dds);
			// if (dt == java.sql.Types.DOUBLE || dt == java.sql.Types.DECIMAL || dt == java.sql.Types.
			if (dd > 0) {
				rem += rcol.getFieldValue("column_name") + ", ";
			}
		}
		// 18 = initial prefix "campos decimales"
		rem = rem.length() > 0 ? rem.substring(0, rem.length() - 2) : rem;
		return rem;
	}
	private boolean isIndexType(String type) {
		if (type == null)
			return false;
		return (type.indexOf("INDEX") > -1);
	}
}
