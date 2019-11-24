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
package core;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import core.datasource.*;

/**
 * Preferences and properties for the aplication. This class handle 3 types of task:
 * <ul>
 * <li>send and recive message to and from internal comunication file. this file control comunicacion between this
 * instance and another posible active instance, controling that only one app intance are active.
 * <p>
 * To send and recive message using this channel, use {@link #sendMessage(String, String)} and
 * {@link #readMessage(String)}
 * <li>store the aplication preferences in local db file. to set and get preferences, use the
 * {@link #setPreference(String, String, Object)} and {@link #getPreference(String, String, Object)} mthods.
 * 
 * </ol>
 * 
 * @author terry
 * 
 */
public class TPreferences {

	private static Properties properties, aProperties;	
	private static final String COMM_FILE = "_.properties";
	private static FileOutputStream outputStream;
	private static FileInputStream inputStream;
	
	public static final String USER = "User";
	public static final String LAST_SELECTED_PATH = "LastSelectedPath";
	public static final String LF_THEME = "theme";
	public static final String REMIND_USER = "RemindUser";
	public static final String DOCKING_STATE = "DockingState";
	public static final String LAST_DIR_SELECTED = "LastOpenDirSelected";
	public static final String LAST_COLOR_SELECTED = "LastColorSelected";
	public static final String TABLE_COLUMN_WIDTH = "TableColumnWidth";
	public static final String DIVIDER_LOCATION = "DividerLocation";
	public static final String WINDOW_BOUND = "WindowBound";
	public static final String WINDOW_STATE = "WindowState";
	public static final String PRINT_PARAMETERS = "PrintParameters";
	public static final String PRINT_FIELD_SELECTION = "PrintFieldSel";
	public static final String LIST_VIEW_STYLE = "listVewStyle";
	public static final String APP_INSTALL_INFO = "AplicationInstallInfo";
	public static final String PLUGIN_INSTALL_INFO = "pluginInstallInfo";


	/**
	 * inicializa preferencias
	 * 
	 * 
	 */
	public static void init() {
		try {
			properties = new Properties();
			inputStream = new FileInputStream(new File(COMM_FILE));
			properties.load(inputStream);
			sendMessage(REQUEST_MAXIMIZE, "false");

			// if aditionalConfigFile is present, try load aditional properties
			Object acf = properties.get("aditionalConfigFile");
			aProperties = new Properties();
			if (!acf.equals("")) {
				FileInputStream fis = new FileInputStream(acf.toString());
				aProperties.load(fis);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read property form communication file
	 * 
	 * @param mid - property id
	 * @return property value
	 */
	public static String readMessage(String mid) {
		try {
			inputStream = new FileInputStream(new File(COMM_FILE));
			properties.load(inputStream);
			inputStream.close();
			return properties.getProperty(mid, null);
		} catch (Exception e) {
			// 180203: remove error log due to ttasnmanager request maximize runnable. the com file is urecheable while
			// native file chooser is displayed
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * set the property directly in property file.
	 * 
	 * @param msg - property id
	 * @param dta - mesage data
	 */
	public static void sendMessage(String msg, String dta) {
		try {
			properties.setProperty(msg, dta);
			outputStream = new FileOutputStream(new File(COMM_FILE));
			properties.store(outputStream, null);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * look the properties in the standar comunication properti files. if not found, try to find in aditional property
	 * file (if it was loaded)
	 * 
	 * 
	 * @param k - property
	 * @return property values or <code>null</code>
	@Deprecated
	public static String get(String k) {
		String val = properties.getProperty(k);
		if (val == null) {
			val = aProperties.getProperty(k);
		}
		return val;
	}
	 */

	/**
	 * look the properties in the standar comunication properti files. if not found, try to find in aditional property
	 * file (if it was loaded)
	 * 
	 * @param k - property to find
	 * @param dftv - default value if properti is not found
	 * 
	 * @return found property or <code>dftv</code>
	 */
	public static String getProperty(String k, String dftv) {
		String val = properties.getProperty(k);
		if (val == null) {
			val = aProperties.getProperty(k);
		}
		return val == null ? dftv : val;
	}

	public static void setProperty(String k, Object v) {
		properties.put(k, v);
	}

	public static Object getPreference(String pid, String obj, Object dftv) {
		// some properties don't need user or are requered before user signin
//		Record ur = Session.getUser();
//		String uid = ur == null ? "" : (String) Session.getUserName();
		String uid = "";
		DBAccess dba = ConnectionManager.getAccessTo("t_local_properties");
		Record prf = dba.getModel();
		prf.setFieldValue("t_lpuserid", uid);
		prf.setFieldValue("t_lpid", pid);
		prf.setFieldValue("t_lpobject", obj);
		Record r = dba.exist(prf);
		Object o = null;
		if (r != null) {
			// spetial case: DOCKING_STATE is already in byte[]
			o = pid.equals(DOCKING_STATE) ? r.getFieldValue("t_lpvalue") : getObjectFromByteArray((byte[]) r
					.getFieldValue("t_lpvalue"));
		}
		return (o == null) ? dftv : o;
	}

	/**
	 * salva el objeto pasado como argumeto dentro del archivo T_LOCAL_PROPERTIES usando
	 * <code>ByteArrayOutputStream</code> para el objeto valor
	 * 
	 * @param pid - identificador de preferencia. campo <code>preference_id</code>
	 * @param obj - identificador de objeto. campo <code>object</code>. puede ser ""
	 * @param val - valor a almacenar
	 */
	public static void setPreference(String pid, String obj, Object val) {
		// some properties don't need user or are requered before user signin
//		Record ur = Session.getUser();
//		String un = ur == null ? "" : (String) Session.getUserName();
		String un = "";
		Record prf = ConnectionManager.getAccessTo("t_local_properties").getModel();
		prf.setFieldValue("t_lpuserid", un);
		prf.setFieldValue("t_lpid", pid);
		prf.setFieldValue("t_lpobject", obj);
		// spetial case DOCKING_STATE
		Object sval = pid.equals(DOCKING_STATE) ? val : getByteArrayFromObject(val);
		prf.setFieldValue("t_lpvalue", sval);
		ConnectionManager.getAccessTo("t_local_properties").write(prf);
	}

	/**
	 * Return the <code>val</code> argument in byte array generated by {@link ObjectOutputStream#writeObject(Object)}
	 * methdo execution. this method is used generaly to store data inside a <code>BLOG</code> field
	 * 
	 * @param val - object
	 * @return serialized object
	 */
	public static byte[] getByteArrayFromObject(Object val) {
		byte[] rdta = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(val);
			out.flush();
			rdta = bos.toByteArray();
			out.close();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return rdta;
	}
	
	public static byte[] getBytearrayFromImage(ImageIcon ii) {
		byte[] dta = null;
		try {
			BufferedImage bi = new BufferedImage(ii.getIconWidth(), ii.getIconHeight(), BufferedImage.TYPE_INT_RGB);
			bi.getGraphics().drawImage(ii.getImage(), 0, 0, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "JPG", baos);
			dta = baos.toByteArray();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return dta;
	}


	/**
	 * Utility method to convert byte array to object. the return value is deserialized using
	 * {@link ObjectInputStream#readObject()} method. Designed for store purpose
	 * 
	 * @param bao - byte array of stored object
	 * @return object read form serialized form
	 */
	public static Object getObjectFromByteArray(byte[] bao) {
		Object o = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bao);
			ObjectInputStream in = new ObjectInputStream(bis);
			o = in.readObject();
			in.close();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return o;
	}
}
