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
package action;

import gui.*;

import java.awt.event.*;
import java.util.*;

import core.*;
import core.datasource.*;



/**
 * esta accion duplica un registro de base de datos obteniendo el registro a clonar de una instancia de
 * <code>TAbstractTable</code> y modificando el ultimo campo clave que encuentre colocanto un contador al estilo
 * estandar de compia. Ej: Si un registro contiene 2 campos claves, apellido y nompre uy se desea cloar a <b>Jose
 * Perez</b>, esta opcio creara <b>Jose Perez (1)</b>
 */
public class CloneRecord extends TAbstractAction implements NoActionForSpecialRecord {

	private UIListPanel abstractTable;

	/**
	 * nueva instancia
	 * 
	 * @param at - <code>TAbstractTable</code> desde donde se obtendran los datos
	 */
	public CloneRecord(UIListPanel at) {
		super(TAbstractAction.RECORD_SCOPE);
		abstractTable = at;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record rcdn = new Record(abstractTable.getRecord());
		DBAccess dba = ConnectionManager.getAccessTo(rcdn.getTableName());
		int lstk = -1;
		for (int i = 0; i < rcdn.getFieldCount(); i++) {
			lstk = rcdn.isKeyField(i) ? i : lstk;
		}
		// no field key found
		if (lstk == -1) {
			throw new NoSuchElementException("no key found in record for " + rcdn.getTableName());
		}
		for (int i = 1; i < 1000; i++) {
			String ix = rcdn.getFieldValue(lstk) + " (" + i + ")";
			rcdn.setFieldValue(lstk, ix);
			if (dba.exist(rcdn) == null) {
				dba.add(rcdn);
				abstractTable.freshen();
				abstractTable.selectRecord(rcdn);
				// algunos dialogos necesitan notificacion de que un registro fue clonado
				abstractTable.putClientProperty(TConstants.ACTION_PERFORMED, this);
				break;
			}
		}
	}
}
