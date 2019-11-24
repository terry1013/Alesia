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
 * Created on 26/07/2005
 * (c) QQ  
 */
package gui;

import core.datasource.*;



/** esta interface proporciona el protocolo que pertmite a los modelos (dentro de la arquitectura MVC)
 * de los componentes visuales que presentan lista de elementos, comunicarse con una implementacion de
 * esta que proporcione soporte basico desarrollado para esta aplicacion.
 * 
 * la principal funcion de las implementaciones seria el refrescado del contenido. esto permitira a 
 * los componentes visuales, actualizarce para reflejar el estado exacto del contenido de la base de 
 * datos. ademas, proporciona filtros y metodos utilitarios comunmente usados 
 * 
 */
public interface DBList {
	
	/** este metodo es invocado cuando se desea que la lista de elementos necesita ser 
	 * actualizada. Acciones como <code>DeleteRecordAction</code> pueden necesitar invocar a este 
	 * metodo para actualiza el contenido de una tabla. 
	 * 
	 * NOTA: usar este metodo es preferible a una actualizacion directa debido a que este reflejara
	 * los cambios emitidos por esta aplicaion asi como los emitos por otras en una arquitectura 
	 * cliente/servidor
	 * 
	 * Clases que implementan este metodo deben actualizar una instancia de <code>Date</code> 
	 * cade vez que se refresque la lista, para asi permitir a <code>FreshenTimer</code> balancear
	 * la carga de transaccions de actualizacion entre el cliente y el servidor.
	 * 
	 * @see getLastFreshen()
	 *
	 */
	public void freshen();
	
	/** este metodo retorna la ultima vez que se llamo al metodo <code>freshen()</code> esto permite
	 * a la clase encargada de refrescar la lista de contenido <code>FreshenTimer</code> determinar
	 * si es necesario efectuar una nueva actualizacion. 
	 * 
	 * @return ultima ejecucion de <code>freshen()</code>
	 */
	public long getLastFreshen();
	
	/** permite establecer la solicitud de servicio que sera origen de datos. esta solicitud 
	 * de servicio sera ejecutada y los datos resultantes (vector) seran colocados a disposicion 
	 * de los interezados.
	 * 
	 * @param sr - solicitud de servicio
	 */
	public void setServiceRequest(ServiceRequest sr);
	
	/** retorna la solicitud de servicio orige de datos de esta lista
	 * 
	 * @return - ServiceRequest
	 */
	public ServiceRequest getServiceRequest();
}
