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
package gui;

import javax.swing.*;

import core.datasource.*;



/** esta clase sirve de model para las vistas en forma de lista de datos contenidos en tablas de base 
 * de datos. 
 * 
 */
public class TAbstractListModel extends AbstractListModel {

	private TAbstractTableModel abstractTableModel;

	/** nueva instancia
	 * 
	 *
	 */
	public TAbstractListModel(TAbstractTableModel ultm) {
		this.abstractTableModel = ultm;
	}
	
	public void freshen() {
		fireContentsChanged(this, 0, abstractTableModel.getRowCount());
	}
	
	
	/** retorna modelo
	 * 
	 * @return - modelo
	 */
	public Record getModel() {
		return abstractTableModel.getRecordModel();
	}

	/** retorna la solicitud de servicio usada para construir el modelo
	 * 
	 * @return solicitud de servicio
	 */
	public ServiceRequest getServiceRequest() {
		return abstractTableModel.getServiceRequest();
	}
	
	/** establece solicitud de servicio. este metodo crea un nuevo modelo de arbol
	 * 
	 * @param sr - servicio
	 */
	public void setServiceRequest(ServiceRequest sr) {
		abstractTableModel.setServiceRequest(sr);
	}
	/** retorna el registro que se encuentra en la fila row
	 * 
	 * @param row - fila
	 * @return registro
	 */
	public Record getRecordAt(int row) {
		return abstractTableModel.getRecordAt(row);
	}

	/** retrona el indice donde se encuentra el registro parado como argumento o -1 si no esta dentro
	 * de la lista. esete elemento verifica <code>Record.toString()</code> que retorna solo los valores
	 * de la clave de registro. 
	 * 
	 * @param rcd - registro a localizar
	 * 
	 */
	public int indexOf(Record rcd) {
		return abstractTableModel.indexOf(rcd); 
	}

	

	@Override
	public int getSize() {
		return abstractTableModel.getRowCount();

	}


	@Override
	public Object getElementAt(int index) {
		Record r = (Record) abstractTableModel.getRecords().elementAt(index);
		return r;
	}
}
