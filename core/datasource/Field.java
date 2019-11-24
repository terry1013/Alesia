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

/**
 * enclose all object that define a database file field
 * 
 * @author terry
 * 
 */
public class Field implements java.io.Serializable{

	/**
	 * field name
	 */
	public String name;
	/**
	 * value object
	 */
	public Object value;
	/**
	 * field length
	 */
	public int length;
	/**
	 * numeric presition
	 */
	public int presition;
	/**
	 * is a table key field
	 */
	public boolean iskey;
	
	/**
	 * this field can be null
	 */
	public boolean isnullable;
	
	/**
	 * class name of value property
	 */
	public String classname;


	/**
	 * new instance
	 * 
	 * @param fn - field name
	 * @param v - field value
	 * @param l - length of field
	 * @param p - numeric presition
	 * @param k - is key field
	 * @param in - is nullable
	 */
	public Field(String fn, Object v, int l, int p, boolean k, boolean in, String c) {
		this.name = fn;
		this.value = v;
		this.length = l;
		this.presition = p;
		this.iskey = k;
		this.isnullable = in;
		this.classname = c;
	}
	
	public Field(String fn, Object v, int l) {
		this(fn, v, l, 0, false, false, "String");
	}
	
	@Override
	public String toString() {
		return name + " = " + value.toString();
	}
}
