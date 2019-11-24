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

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import core.*;
import core.datasource.*;

/**
 * extiende la funcionalidad basica provisa por <code>AbstractDataInput</code> y la adapta para la actualizacion de
 * informacion dentro de una base de datos. esta clase implementa redirige <code>AbstractDataInput.validateField</code>
 * hacia la instancia de Record establecida como modelo.
 *
 * TODO: 180504 change all hashtable to treemap with String.CASE_INSENSITIVE_ORDER to avoid miss type in addinputfiled
 */
public abstract class AbstractRecordDataInput extends AbstractDataInput {

	private Record rcdModel;
	private TError existE;

	/**
	 * nueva instancia
	 * 
	 * @param dnam - Nombre del documento a mostrar en la barra de informacion
	 * @param rcd - modelo
	 */
	public AbstractRecordDataInput(String dnam, Record rcd) {
		super(dnam);
		this.rcdModel = rcd;
		this.existE = new TError("msg03");
	}

	public Record getRecord() {
		Record r = new Record(rcdModel);
		Hashtable flds = getFields();
		Enumeration enu = flds.keys();
		// boolean existF;
		while (enu.hasMoreElements()) {
			String k = (String) enu.nextElement();
			// 180223: only consider fields that exist inside of record model. aditional information may be present
			// inside the field list
			try {
				rcdModel.getIndexOf(k);
			} catch (Exception e) {
				continue;
			}
			Object val = flds.get(k);

			// special values for JComboBox or RecordSelector
			// see (special TEntry values) in constants.properties
			if (val instanceof String) {
				String ov = (String) val;
				val = (ov.equals("*null") || ov.equals("*none")) ? "" : val;
			}

			// 180104: commented because is not needed anymore
			/*
			 * // solo para columnas existentes dentro de registro existF = false; for (int i = 0; i <
			 * rcdModel.getFieldCount(); i++) { if (rcdModel.getFieldName(i).equals(k)) { existF = true; break; } } if
			 * (existF) { // Coreccion de posible discordancia entre las instancias numericas if
			 * (rcdModel.getFieldValue(k) instanceof Integer) { val = new Integer(((Number) val).intValue()); } if
			 * (rcdModel.getFieldValue(k) instanceof Float) { val = new Float(((Number) val).floatValue()); } if
			 * (rcdModel.getFieldValue(k) instanceof Double) { val = new Double(((Number) val).doubleValue()); } if
			 * (rcdModel.getFieldValue(k) instanceof Long) { val = new Long(((Number) val).longValue()); }
			 * 
			 * r.setFieldValue(k, val);
			 * 
			 * }
			 */

			r.setFieldValue(k, val);
		}
		return r;
	}

	/**
	 * metodo que implementa una validacion estandar de un nuevo registro. este determina los campos clave y obtiene el
	 * valor de los componentes registradas para esos campos. luego verifica si existe un registro igual dentro de la
	 * base de datos. Subclases puede sobreescribir este metodo si es necesario valdiaciones adicionales al momento de
	 * ingresar nuevo registro.
	 * 
	 * TODO: schedule for deprecation. Moved to newRecord2
	 */
	public void validateNewRecord() {
		Record rcd = getRecord();
		rcd.updateTime();
		ServiceResponse rq = ServiceConnection.sendTransaction(new ServiceRequest(ServiceRequest.DB_EXIST, rcd
				.getTableName(), rcd));
		if (rq.getData() != null) {
			showAplicationException(existE);
		}
		setEnableDefaultButton(!isShowingError());
	}

	/**
	 * establece un nuevo modelo para esta entrada de datos. este metodo intentara actualizar los valores para los
	 * componentes asociados a cada campo dentro del registro.
	 * 
	 * NOTA: se espera que continue la relacion entre tipo de datos, componentes y al invocar este metodo, el modelo
	 * pasado como argumentos es "del mismo archivo" con el cual, inicialmente se creo esta instancia
	 * 
	 * @param mod - registro
	 */
	public void setModel(Record mod) {
		this.rcdModel = mod;
		for (int j = 0; j < rcdModel.getFieldCount(); j++) {
			String fn = rcdModel.getFieldName(j).toLowerCase();
			JComponent jc = getInternal(getInputComponent(fn));
			if (jc instanceof JFormattedTextField) {
				((JFormattedTextField) jc).setValue(rcdModel.getFieldValue(j));
				continue;
			}
			if (jc instanceof JTextComponent) {
				((JTextComponent) jc).setText(rcdModel.getFieldValue(j).toString());
			}
			if (jc instanceof JComboBox) {
				if (jc instanceof RecordSelector) {
					RecordSelector rs = (RecordSelector) jc;
					Object o = rcdModel.getFieldValue(j);
					rs.setSelectedItem(o);
				} else {
					JComboBox jcb = (JComboBox) jc;
					TEntry lt = TStringUtils.getTEntryByKey((String) rcdModel.getFieldValue(j));
					jcb.setSelectedItem(lt);
				}
			}
			if (jc instanceof ExtendedJLabel) {
				((ExtendedJLabel) jc).setValue(rcdModel.getFieldValue(j));
				/*
				 * if (rcdModel.isConstantID(j)) { ((ExtendedJLabel) jc) .setValue(TStringUtils.getTEntryByKey((String)
				 * rcdModel.getFieldValue(j))); } else { ((ExtendedJLabel) jc).setValue(rcdModel.getFieldValue(j)); }
				 */
			}
			if (jc instanceof JCheckBox) {
				((JCheckBox) jc).setSelected(((Boolean) rcdModel.getFieldValue(j)).booleanValue());
			}
		}
	}
}
