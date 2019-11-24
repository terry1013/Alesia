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
package delete;

import java.util.*;

import net.sf.jasperreports.engine.*;
import core.*;
import core.datasource.*;

/**
 * Clase que provee metodo que hacen interface entre el generador de reporte y el framework del sistema de manera q la
 * programacion del reporte sean invocaciones a metodos estaticos de esta clase.
 * 
 * @author terry
 * 
 */
public class DR {

	private static DBAccess dbAccess;

	public static RecordList records;
	public static JasperPrint jasperPrint;
	public static Record model;
	public static String halt;
	public static String GROUP_FIELD = "GroupField";

	/**
	 * Establece conexion por defecto a taba de base de datos
	 * 
	 * @param ctn - nombre calificado de origen_de_datos.nombre_de_tabla
	 */
	public static void prepareDBAccess(String ctn) {
		halt = null;
		dbAccess = ConnectionManager.getAccessTo(ctn);
	}

	/**
	 * adiciona un nuevo campo a la lista de registro actual. estos campos de trabajos pueden ser usados como destino de
	 * otras operaciones que se deseen almacenar junto con los demas datos del registro. NOTA: este metodo no verifica
	 * que el valor para el campo este inizializado en blanco.
	 * 
	 * TODO: asegurarse que valores esten inizializados en blanco
	 * 
	 * @param fname - nombre del campo.
	 * @param fval - valor para el campo (valor inicial).
	 */
	public static void addWorkField(String fname, Object fval) {
		for (Record rcd : records) {
			rcd.addNewField(new Field(fname, fval, 100));
		}
		model.addNewField(new Field(fname, fval, 100));
	}

	public static void search(String wc) {
		halt = null;
		// no dbAccess
		if (dbAccess == null) {
			log("DR.noDBAcces");
			return;
		}
		Vector<Record> v = dbAccess.search(wc, null);
		if (records == null) {
			records = new RecordList(v.size());
		} else {
			records.clear();
		}
		records.addAll(v);

		// modelo puede variar segun seleccion de columnas.
		if (v == null) {
			model = dbAccess.getModel();
		} else {
			model = records.elementAt(0).createModel();
		}
		records.setModel(model);
	}

	/**
	 * establece una nueva lista actual de elementos basada en la lista actual mas un nuevo campo de archivo de base de
	 * datos. el resultado es una nueva lista cuyo nuevo campo fue creado basado el el parametro <code>newField</code> y
	 * cuyo valor es obtenido comparando los parametros de entrada <code>righ_f = valor de left_f </code> Ej: si se
	 * tiene una lista de elementos dode uno de los campos es un codigo cuya descripcion se encuentra en otro archivo.
	 * usando este metodo obtendremos una nueva lista cuyos campos son iguales a la anterior mas un nuevo campo que
	 * contienen la descripcion de cada elemento
	 * 
	 * @param left_f - campo actual dentro de la lista de elementos
	 * @param righ_f - campo para la comparacion dentro del archivo de base de datos
	 * @param file - archivo donde se desea buscar
	 * @param newfield - nuevo campo que sera adicionado a la lista de elementos.
	 */

	public static void searchAndJoin(String left_f, String righ_f, String file, String newfield) {
		RecordList join = new RecordList(records.size());

		DBAccess rdba = ConnectionManager.getAccessTo(file);
		Record rmod = rdba.getModel();

		// new model based on actual model + rfield
		Record rmodel = new Record(model);
		rmod.addNewField(newfield, rmod.getFieldValue(newfield), rmod.getFieldSize(newfield),
				rmod.getFieldPresition(newfield), rmod.isKeyField(newfield), rmod.isNullable(newfield), rmod.getFieldValue(newfield).getClass().getName());

		for (Record l_rcd : records) {
			Record r = rdba.exist(righ_f + " = '" + l_rcd.getFieldValue(left_f) + "'");
			Record nr = new Record(l_rcd);
			if (r != null) {
				nr.addNewField(newfield, r.getFieldValue(newfield), r.getFieldSize(newfield),
						rmod.getFieldPresition(newfield), rmod.isKeyField(newfield), rmod.isNullable(newfield), r.getFieldValue(newfield).getClass().getName());
			} else {
				// nr.addNewField(newfield, rmod.getFieldValue(newfield), rmod.getFieldSize(newfield));
			}
			join.add(nr);
		}
		model = rmodel;
		records = join;
	}

	public static void setReport(JasperPrint r) {
		jasperPrint = r;
	}

	public static TDRTableModel getTableModel() {
		records.setModel(model);
		return new TDRTableModel(records);
	}

	public static void halt(String msg) {
		// no dbAccess
		if (dbAccess == null) {
			log("DR.noDBAcces");
			return;
		}
		// no vector
		if (records == null) {
			log("DR.noVector");
			return;
		}
		halt = (msg == null) ? "" : msg;
	}
	
	/**
	 * retorna una instancia de <code>RecordList</code> con los elementos que se procesan en este momento. este metodo
	 * limpia dicla lista
	 * 
	 * @return copia de lista actual
	 */
	public static RecordList getCopy() {
		RecordList v = new RecordList(model);
		v.ensureCapacity(records.size());
		v.addAll(records);
//		records.clear();
		return v;
	}

	/**
	 * efectua la union de la lista de datos actual con la lista de datos pasadas como argumento usando la igualdad
	 * entre el campo left_f y right_f el resultado de la operacion es estabecido como lista de elementos actual
	 * 
	 * @param left_f - nombre de campo en lista actual
	 * @param right_f - nombre de campo en lista pasada como argumento
	 * @param right_rcds - elementos a unir con la lista actual
	 */
	public static void joinBy(String left_f, String right_f, RecordList right_rcds) {
		RecordList join = new RecordList(right_rcds.size());
		boolean mod = false;
		// legacy list
		for (Record r_rcd : right_rcds) {
			Object ro = r_rcd.getFieldValue(right_f);
			// actual list
			for (Record l_rcd : records) {
				Object lo = l_rcd.getFieldValue(left_f);
				if (ro.equals(lo)) {
					// new record form legacy list
					Record jrcd = new Record(r_rcd);
					if (!mod) {
						model = jrcd.createModel();
					}
					for (int i = 0; i < l_rcd.getFieldCount(); i++) {
						jrcd.addNewField(l_rcd.getFieldName(i), l_rcd.getFieldValue(i), l_rcd.getFieldSize(i),
								l_rcd.getFieldPresition(i), l_rcd.isKeyField(i), l_rcd.isNullable(i), l_rcd.getFieldValue(i).getClass().getName());
					}
					join.add(jrcd);
					/*
					 * for (int i = 0; i < jrcd.getFieldCount(); i++) {
					 * System.out.println(jrcd.getFieldValue(i).getClass() + "\t" + jrcd.getFieldName(i) + ": \t\t" +
					 * jrcd.getFieldValue(i).toString() ); }
					 */
				}
			}
		}
		records = join;
	}

	/**
	 * este metodo agrupara los datos segun los campos pasados como argumentos ordenandolos automaticamente
	 * 
	 * @param fields - nombre de campos para agrupacion
	 */
	public static void groupByFields(String... fields) {
		halt = null;
		ArrayList<TEntry> gvec = new ArrayList(records.size());
		RecordList newr = new RecordList(records.size());

		addWorkField(GROUP_FIELD, "");

		String under = "";
		for (int i = 1; i < model.getFieldSize(GROUP_FIELD); i++) {
			under += " ";
		}
		for (Record rcd : records) {
			String wv = "";
			for (String fn : fields) {
				Object o = rcd.getFieldValue(fn);
				int fz = rcd.getFieldSize(fn);
				String tmp = (o instanceof Number) ? under + o.toString() : o.toString() + under;
				tmp = (o instanceof Number) ? tmp.substring(tmp.length() - fz, tmp.length()) : tmp.substring(0, fz);
				wv += tmp;
			}
			rcd.setFieldValue(GROUP_FIELD, wv);
			gvec.add(new TEntry(rcd, wv));
		}
		// ordenamiento
		Collections.sort(gvec);
		for (TEntry te : gvec) {
			// System.out.println(te.getKey());
			newr.add((Record) te.getKey());
		}
		records = newr;
	}

	private static void log(String cid) {
		System.out.println(TStringUtils.getTEntryByKey(cid));
	}

}
