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
import java.sql.Date;
import java.util.*;

import core.*;

/**
 * this class is a database file row.
 * 
 */
public class Record implements java.io.Serializable {

	private Field[] fields;
	private String tableName;

	/**
	 * create a new record copying all data from record pass as argument
	 * 
	 * @param sr - source record
	 */
	public Record(Record sr) {
		this.tableName = sr.tableName;
		this.fields = new Field[sr.getFieldCount()];
		for (int l = 0; l < fields.length; l++) {
			fields[l] = new Field(sr.fields[l].name, sr.fields[l].value, sr.fields[l].length, sr.fields[l].presition,
					sr.fields[l].iskey, sr.fields[l].isnullable, sr.fields[l].classname);
		}
	}

	/**
	 * nueva instancia
	 * 
	 * @param tn - Nombre de tabla de base de datos
	 * @param kd - profundidad de la clave.
	 * @param flds - arreglo de campos
	 */
	public Record(String tn, Field[] fs) {
		this.tableName = tn.toUpperCase();
		this.fields = fs;
	}

	public void addNewField(Field f) {
		addNewField(f.name, f.value, f.length, f.presition, f.iskey, f.isnullable, f.classname);
	}

	/**
	 * create a new field to this record
	 * 
	 * @param n - name
	 * @param v - value
	 * @param l - length
	 * @param p - numerical presition
	 * @param k - is key field
	 * @param in - is nullable
	 * @param c - class name of field value
	 */
	public void addNewField(String n, Object v, int l, int p, boolean k, boolean in, String c) {
		Field f = new Field(n.toUpperCase(), v, l, p, k, in, c);
		Field[] fld = new Field[fields.length + 1];
		System.arraycopy(fields, 0, fld, 0, fields.length);
		fields = fld;
		fields[fields.length - 1] = f;
	}

	/**
	 * crea una copia en blanco de este registro
	 * 
	 * @return copia en blanco
	 * 
	 * */

	public Record createModel() {
		Record r = new Record(this);
		for (int l = 0; l < fields.length; l++) {
			Object o = r.getFieldValue(l);
			if (o instanceof String) {
				o = "";
			}
			if (o instanceof Boolean) {
				o = Boolean.FALSE;
			}
			if (o instanceof Double) {
				o = Double.valueOf(0);
			}
			if (o instanceof Long) {
				o = Long.valueOf(0);
			}
			if (o instanceof Integer) {
				o = Integer.valueOf(0);
			}
			if (o instanceof Date) {
				o = new java.sql.Date(TStringUtils.ZERODATE.getTime());
			}
			if (o instanceof Time) {
				o = new Time(TStringUtils.ZERODATE.getTime());
			}
			if (o instanceof Timestamp) {
				o = new Timestamp(TStringUtils.ZERODATE.getTime());
			}
			if (o instanceof Byte[]) {
				o = new Byte[0];
			}
			r.setFieldValue(l, o);
		}
		return r;
	}

	/**
	 * delete field
	 * 
	 * @param fn - field name to delete
	 */
	public void deleteField(String fn) {
		int fcol = getIndexOf(fn);
		Field[] fld = new Field[fields.length - 1];
		for (int c = 0; c < fields.length; c++) {
			if (c != fcol) {
				fld[c] = fields[c];
			}
		}
		fields = fld;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Record))
			return false;
		Record r = (Record) obj;
		return toString().equals(r.toString());
	}

	public Object getDBFieldValue(int c) {
		return getExternal1(c, isNullable(c));
	}

	public Object getDBFieldValue(String fn) {
		return getDBFieldValue(getIndexOf(fn));
	}

	public Object getExternalFieldValue(int c) {
		return getExternal1(c, true);
	}

	public Object getExternalFieldValue(String fn) {
		return getExternalFieldValue(getIndexOf(fn));
	}

	/**
	 * return the numbers of fields inside this record
	 * 
	 * @return field count
	 */
	public int getFieldCount() {
		return fields.length;
	}
	
	public Field getField(int c) {
		return fields[c];
	}
	
	public Field getField(String fn) {
		return fields[getIndexOf(fn)];
	}

	/**
	 * return the name for this column
	 * 
	 * @param c - column
	 * @return field name
	 */
	public String getFieldName(int c) {
		return fields[c].name;
	}

	/**
	 * return field presition for numeric
	 * 
	 * @param c - column id
	 * @return field presition
	 */
	public int getFieldPresition(int c) {
		return fields[c].presition;
	}

	/**
	 * return field presition for numeric
	 * 
	 * @param cn - column namge
	 * @return field presition
	 */
	public int getFieldPresition(String fn) {
		return fields[getIndexOf(fn)].presition;
	}

	/**
	 * retorna la longitud de la columna pasada como argumento
	 * 
	 * @param c - columna
	 * @return tama;o
	 */
	public int getFieldSize(int c) {
		return fields[c].length;
	}

	/**
	 * retorna la longitud de la columna con nombre pasado como argumento
	 * 
	 * @param fn - nombre de la columna
	 * @return ancho de la columna o -1 si la columna no fue encontrada
	 */
	public int getFieldSize(String fn) {
		return fields[getIndexOf(fn)].length;
	}

	/**
	 * retorna el valor del campo en la columna c.
	 * 
	 * NOTA: solo debe ser usado por modelos de datos ya que estos no se veran afectados por un cambio en el orden de
	 * los campos dentro de la base de datos
	 * 
	 * @param c - columna
	 * @return valor del campo. <code>null</code> si longitud del registro es menor al valor c
	 */
	public Object getFieldValue(int c) {
		return fields[c].value;
	}

	/**
	 * retorna valor del campo n
	 * 
	 * @param n - nombre del campo
	 * @return valor del campo.
	 * @throws <code>NoSuchElementException</code> si el nombre del campo no existe
	 */
	public Object getFieldValue(String n) {
		return fields[getIndexOf(n)].value;
	}

	/**
	 * retorna el indice del campo.
	 * 
	 * @param fn - nombre del campo
	 * @return indice.
	 * @throws <code>NoSuchElementException</code> si el nombre de la columna no existe
	 */
	public int getIndexOf(String fn) {
		int p = -1;
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].name.compareToIgnoreCase(fn) == 0) {
				p = i;
				break;
			}
		}
		if (p == -1) {
			throw new NoSuchElementException("Column name: " + fn + " not found");
		}
		return p;
	}

	/**
	 * returnt key fields and values in format: <field label> = <field value>, <field label> = <field value>
	 * 
	 * @see DBAccess#auditLog(String, Record)
	 * 
	 * @return fields and values
	 */
	public String getKeyFieldValueComaSeparated() {
		String ss = "";
		for (int j = 0; j < fields.length; j++) {
			if (fields[j].iskey) {
				ss += TStringUtils.getString(fields[j].name) + " = " + fields[j].value + ", ";
			}
		}
		return ss.substring(0, ss.length() - 2);
	}

	/**
	 * return the key fields in sql <code>ORDER BY</code> style
	 * 
	 * @return columns to order by
	 */
	public String getOrderBy() {
		String ord = "";
		for (int k = 0; k < fields.length; k++) {
			if (fields[k].iskey) {
				ord += fields[k].name + ", ";
			}
		}
		return ord.substring(0, ord.length() - 2);
	}

	/**
	 * retorna el nombre de tabla de base de datos a la cual pertenece este registro
	 * 
	 * @return - nombre de tabla
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * set the tablename for this record
	 * 
	 * @param tn - table name
	 */
	public void setTableName(String tn) {
		tableName = tn.toUpperCase();
	}

	/**
	 * verifica si id de columna contiene un id de constante. cuyo valor debera ser reemplazado para efectos de
	 * presentacion. CHAR(4) o VARCHAR(13)
	 * 
	 * @param fn - nombre del campo
	 * @return true si es un campo de tipo constante.
	 * @throws <code>NoSuchElementException</code> si el nombre del campo no existe
	public boolean isConstantID(int c) {
		Object obj = fields[c].value;
		return (obj instanceof String && (fields[c].length == 4 || fields[c].length == 5));
	}
	 */

	/**
	 * verifica si el campo es una campo que contiene un id de constante.
	 * 
	 * @param fn - nombre del campo
	 * @return true si es un campo de tipo constante.
	 * @see isConstantID(int)
	 * @throws <code>NoSuchElementException</code> si el nombre del campo no existe
	public boolean isConstantID(String fn) {
		return isConstantID(getIndexOf(fn));
	}
	 */

	/**
	 * Returns whether the field idexed by <code>c</code> is a key field of this table.
	 * 
	 * @param c - index of field
	 * 
	 * @return <code>true</code> if <code>fn</code> is a key field
	 */
	public boolean isKeyField(int c) {
		return fields[c].iskey;
	}

	/**
	 * Returns whether the field <code>fn</code> is a key field of this table.
	 * 
	 * @param fn - field name
	 * 
	 * @return <code>true</code> if <code>fn</code> is a key field
	 */
	public boolean isKeyField(String fn) {
		return isKeyField(getIndexOf(fn));
	}

	public boolean isKeyFieldSetted() {
		boolean all = true;
		for (int c = 0; c < getFieldCount(); c++) {
			if (isKeyField(c)) {
				all = getExternal1(c, true).equals("") ? false : all;
			}
		}
		return all;
	}

	/**
	 * Returns whether the field idexed by <code>c</code> is is nullable
	 * 
	 * @param c - index of field
	 * 
	 * @return <code>true</code> if <code>fn</code> is nullable
	 */
	public boolean isNullable(int c) {
		return fields[c].isnullable;
	}

	/**
	 * Returns whether the field <code>fn</code> is nullable
	 * 
	 * @param fn - field name
	 * 
	 * @return <code>true</code> if <code>fn</code> is nullable
	 */
	public boolean isNullable(String fn) {
		return isNullable(getIndexOf(fn));
	}

	/**
	 * establece el nuevo valor v al campo en la columna c
	 * 
	 * @param c - columna
	 * @param v - nuevo valor
	 */
	public void setFieldValue(int c, Object v) {
		fields[c].value = v;
	}

	/**
	 * establece el valor del campo n con el nuevo valor v
	 * 
	 * @param n - nombre del campo
	 * @param v - nuevo valor
	 * @throws <code>NoSuchElementException</code> si el nombre del campo no existe
	 */
	public void setFieldValue(String n, Object v) {
		setFieldValue(getIndexOf(n), v);
	}
	@Override
	public String toString() {
		StringBuffer k = new StringBuffer(tableName + " Keys: ");
		for (int j = 0; j < fields.length; j++) {
			if (fields[j].iskey) {
				k.append(fields[j].name + " = " + fields[j].value.toString());
				k.append((j < fields[j].length) ? ", " : "");
			}
		}
		return k.toString();
	}

	/**
	 * Este metodo utilitario esta dice;ado cuando se desea usar campos instanciaas de <code>Timestamp</code> dentro de
	 * la clave. cuando se desea crear un nuevo registro. se invoca este metodo para que estos campos se actualicen y
	 * reflejen la fecha-hora actual.
	 * 
	 */
	public void updateTime() {
		for (int l = 0; l < fields.length; l++) {
			if (fields[l].iskey) {
				Object dat = fields[l].value;
				if (dat instanceof Timestamp) {
					Timestamp ts = (Timestamp) dat;
					ts.setTime(System.currentTimeMillis());
				}
			}
		}
	}

	/**
	 * Return the external representation of internal field value.
	 * 
	 * @param c - col index
	 * @param kon - bollean value. typically returned by <code>isNullable(int), isKeyField(int)</code>
	 * 
	 * @return Object
	 */
	private Object getExternal1(int c, boolean kon) {
		Object o = getFieldValue(c);
		if (o instanceof Date) {
			Date d = (Date) o;
			o = (d.equals(TStringUtils.ZERODATE) && kon) ? "" : d;
		}
		if (o instanceof Number) {
			Number n = (Number) o;
			o = (n.intValue() == 0 && kon) ? "" : n;
		}
		return o;
	}
}
