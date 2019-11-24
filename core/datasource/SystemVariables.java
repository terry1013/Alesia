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

import java.lang.reflect.*;
import java.text.*;
import java.util.*;

/**
 * clase utilitaria para acceso y actualizacion de variables de sistema
 * 
 * 
 */
public class SystemVariables {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

	/**
	 * actualiza valor para variable de sistema
	 * 
	 * @param var - nombre variable
	 * @param val - nuevo valor
	 */
	public static void updateVar(String var, Object val) {
		try {
			DBAccess cons = ConnectionManager.getAccessTo("t_system_var");
			Record r = cons.exist("t_svgroup = '*system' AND t_sv_id ='" + var + "'");
			if (r != null) {
				val = (val instanceof Date) ? dateFormat.format((Date) val) : val;
				r.setFieldValue("t_svvalue", val);
				cons.update(r);
			}
		} catch (Exception e) {
			throw new NoSuchElementException("Var with name " + var + " not exist");
		}
	}

	/**
	 * localiza el valor de la variable de sistema <code>k</code> y retorna (si aplica) el valor en
	 * formato <code>int</code>
	 * 
	 * @param k - varieble
	 * @return valor
	 */
	public static int getintVar(String k) {
		return ((Integer) getObject(k, "Integer"));
	}

	/**
	 * localiza el valor de la variable de sistema <code>k</code> y retorna (si aplica) el valor en
	 * formato <code>boolean</code>
	 * 
	 * @param k - varieble
	 * @return valor
	 */
	public static boolean getbooleanVar(String k) {
		return ((String) getObject(k, "String")).equals("1");
	}

	/**
	 * localiza el valor de la variable de sistema <code>k</code> y retorna el valor en formato
	 * <code>String</code>
	 * 
	 * @param k - varieble
	 * @return valor
	 */
	public static String getStringVar(String k) {
		return ((String) getObject(k, "String"));
	}

	/**
	 * localiza el valor de la variable de sistema <code>k</code> y retorna el valor en formato
	 * <code>Date</code>
	 * 
	 * @param k - varieble
	 * @return valor
	 */
	public static Date getDateVar(String k) {
		return ((Date) getObject(k, "Date"));
	}

	/**
	 * retorna instancia de clase <code>cls</code> establecida con el valor encontrado para la
	 * variable de sistema <code>k</code>
	 * 
	 * @param k - nombre variable
	 * @param cln - clase
	 * @return objeto
	 */
	private static Object getObject(String k, String cln) {
		try {
			DBAccess cons = ConnectionManager.getAccessTo("t_system_var");
			Record r = cons.exist("t_svgroup = '*system' AND t_sv_id ='" + k + "'");
			Object obj;
			if (cln.equals("Date")) {
				obj = dateFormat.parse((String) r.getFieldValue("t_svvalue"));
			} else {
				Class cls = Class.forName("java.lang." + cln);
				Constructor c = cls.getConstructor(new Class[]{String.class});
				obj = c.newInstance(new Object[]{r.getFieldValue("t_svvalue")});
			}
			return obj;
		} catch (Exception e) {
			throw new NoSuchElementException("Instantiation for variable " + k);
		}
	}

	/**
	 * retorna <code>true</code> si el servidor pasado como argumento esta conectado. El servidor
	 * esta conectado si la diferencia entre el momento de invocacion de este metodo y la ultima
	 * actualizacion es apox 1 seg.
	 * 
	 * @param s - servidor
	 * @return <code>true o false</code> public static boolean isServerConnected(String s) { long
	 *         act = serverAct.get(s); long la = (Long) getObject(s + "Messenge", "Long");
	 * 
	 *         System.out.println(s + " " + (act - la)); return act - la <= 1100; }
	 */
}
