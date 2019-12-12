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

import gui.*;
import core.datasource.*;

/**
 * esta centraliza las peticiones de comunicacion cliente/servidor. p
 * 
 * 
 */
public class ServiceConnection {

	/**
	 * este metodo envia una peticion de solicitud al servidor y espera por una respuesta. es <code>synchronized </code>
	 * debido a que AWT-Thread & Fresgen Thread pueden emitir solicitudes de servicio silmuntaneamente.
	 * 
	 * @param sr - servicio
	 * @return - respuesta del servidor
	 */
	public static synchronized ServiceResponse sendTransaction(ServiceRequest sr) {

		return null;
	}

	/**
	 * igual a <code>sendTransaction(ServiceRequest)</code> pero con los paramentros de constructor de
	 * <code>Servicerequest</code>
	 * 
	 * @param nam - nomnbre del servicion. Ej: ServiceRequest.DB_EXIST
	 * @param tn - nombre del archivo de base de datos (si aplica)
	 * @param dta - datos para la transaccion (si aplica)
	 * @return <code>ServiceResponse</code>
	 */
	public static ServiceResponse sendTransaction(String nam, String tn, Object dta) {
		return sendTransaction(new ServiceRequest(nam, tn, dta));
	}

}
