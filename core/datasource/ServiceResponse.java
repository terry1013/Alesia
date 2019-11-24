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
/*
 * 
 */
package core.datasource;

import java.io.*;
import java.util.*;

/**
 * Representa una respuesta a una solicitud de servicio.
 * 
 */
public class ServiceResponse implements Serializable {

	public static final String EXCEPTION = "Exception";
	public static final String RECORD_MODEL = "RecordModel";
	/**
	 * set/get hashtable where all fields and descriptions are. client generated list may use this parameter to inform
	 * the id, name relation of his own generated record
	 */
	public static final String RECORD_FIELDS_DESPRIPTION = "RecordFieldsDescriptions";
	
	private Object rData;
	private HashMap parameters;

	/**
	 * nueva instancia
	 * 
	 * @param dta - datos a enviar al cliente
	 */
	public ServiceResponse(Object dta) {
		this.parameters = new HashMap();
		this.rData = dta;
	}

	/**
	 * establece parametros para esta respuesta a servicio
	 * 
	 * @param k - identificador de parametro
	 * @param val - valor para parametro
	 */
	public void setParameter(String k, Object val) {
		parameters.put(k, val);
	}

	public void setParameters(HashMap par) {
		this.parameters = par;
	}

	/**
	 * retorna el parametro identrificado con el argumento de entrada
	 * 
	 * @param k - id de parametro
	 * @return valor para este parametro
	 */
	public Object getParameter(String k) {
		return parameters.get(k);
	}

	/**
	 * establece datos a retornar hacia el cliente
	 * 
	 * @param d - datos
	 */
	public void setData(Object d) {
		this.rData = d;
	}

	/**
	 * retorna los datos establecidos para esta respuesta
	 * 
	 * @return datos
	 */
	public Object getData() {
		return rData;
	}
}
