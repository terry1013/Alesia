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

import java.util.*;

/** Una transaccion son un conjunto de operaciones que deben ser ejecutadas juntas para que
 * pueda darse como correcta una solicitud de servicio. Esta clase define el protocolo de 
 * comunicaciones entre un servidor de servicios y cada transaccion. 
 * 
 */
public abstract class AbstractTransaction {
	
	protected ServiceRequest request;
	protected Hashtable parameters;
	
	/** ejecutado por el despachador, establece la solicitud de servicio para esta transaccion
	 * 
	 * @param r
	 */
	public void setServiceRequest(ServiceRequest r) {
		this.request = r;
	}
	
	/** procesa la transaccion representada por este objeto
	 * 
	 * @param resultado de la ejecucion del proceso
	 *
	 */
	public abstract ServiceResponse commit();
	
	/**
	 * @return Returns the parameters.
	 */
	public Object getParameter(String pn) {
		return parameters.get(pn);
	}
	/** establecer un parametro para esta transaccion
	 * @param pn - nombre
	 * @param pv - valor
	 * 
	 */
	public void setParameter(String pn, Object pv) {
		this.parameters.put(pn, pv);
	}
}
