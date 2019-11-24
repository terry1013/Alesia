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

import java.io.*;

/**
 * encapsula un par de elementos descritos como clave y valor. los elementos claves son los valores que internamente
 * necesita la aplicacion y el elemento valor es el mostrado. EJ: en instancias de <code>JCombobox</code> los elementos
 * son instancias de <code>TEntry</code> y el valor presentado es el almacenado en valor. el contrato general es que se
 * desea manipular un pár de elementos pero la manipulacion es a travez del valor. EJ: metodo
 * <code>	public int compareTo(Object)</code> perimitiria ordenar una lista de elemntos usando el valor enlazado con el
 * objeto (clave) al cual pertence
 * 
 * @author terry
 * 
 */
public class TEntry implements Serializable, Comparable {
	private Object key;
	private Object value;

	/**
	 * nueva instnaica
	 * 
	 * @param key - elemento clave
	 * @param value - elemento valor
	 */
	public TEntry(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * nueva instancia
	 * 
	 * @param arg - cadena de caracteres de forma key;description
	 */
	public TEntry(String arg) {
		String[] kv = arg.split(";");
		if (kv.length != 2) {
			throw new IllegalArgumentException("argument is not in key;description form");
		} else {
			this.key = kv[0];
			this.value = kv[1];
		}
	}

	/**
	 * retorna elemento clave
	 * 
	 * @return clave
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * retorna elemento valor
	 * 
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * establece elemento valor
	 * 
	 * @param value - nuevo valor
	 */
	public void setValue(Object value) {
		if (value == null)
			throw new NullPointerException();
		this.value = value;
	}

	/**
	 * establece elemento clave
	 * 
	 * @param key - nueva clave
	 */
	public void setKey(Object key) {
		if (key == null)
			throw new NullPointerException();
		this.key = key;
	}

	public boolean equals(Object o) {
		if (!(o instanceof TEntry)) {
			return false;
		}
		TEntry e = (TEntry) o;
		return (key == null ? e.getKey() == null : key.equals(e.getKey()))
				&& (value == null ? e.getValue() == null : value.equals(e.getValue()));
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}

	/**
	 * try to dispatch to {@link Comparable#compareTo(Object)} of underlying value. if this is not posible, use
	 * toString()
	 */
	@Override
	public int compareTo(Object anotherTentry) {
		TEntry te = (TEntry) anotherTentry;
		if (te.getValue() instanceof Integer) {
			Integer inum = (Integer) te.getValue();
			return ((Integer) value).compareTo(inum);
		}
		if (te.getValue() instanceof Double) {
			Double inum = (Double) te.getValue();
			return ((Double) value).compareTo(inum);
		}
		// 20161109 que ladilla !!! parece que contamos may y le tumbaron 500 bs a mama en el deposito de mercantil
		return value.toString().compareTo(((TEntry) anotherTentry).getValue().toString());
	}
}
