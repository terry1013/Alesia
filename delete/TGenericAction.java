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
package delete;

import gui.*;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import action.*;

import core.*;



/** las acciones generias son aquellas que reciben como argumento de entrada una instancia
 * de <code>ActionPerformer</code>. El contrato general para estas acciones es:
 * 
 * - al momento de la invoacion de <code>actionPerformed(ActionEvent)</code> esta accion
 * solicita una instancia de <code>UIComponentPanel</code> la cual sera presentada como
 * dialogo de entrada. 
 * 
 * - dentro de este panel de entrada, se espera que existan acciones
 * que sean instancias de <code>RedirectAction</code>. estas acciones cambiaran la propiedad
 * <code>ACTION_PERFORMED</code> 
 * 
 * - cuando esta propiedad sea cambiada para el componente de entrada, esta accion redirecciona
 * el la llamada invocando <code>ActionPerformer.executeAction(LTActionEvent)</code> para 
 * que el este procese la peticion
 * 
 */
public class TGenericAction extends TAbstractAction implements PropertyChangeListener {
	
	private UIComponentPanel rdInput;
	private JDialog dialog;
	private String dlg_title;

	/** nueva accion. cualquiera de los argumentos de entrada puede ser <code>null</code>
	 * exeptuando el alcance, cuyo valor equivalente es <code>NO_SCOPE</code>
	 * 
	 * @param tex - id para Texto 
	 * @param inam - Nombre del icono
	 * @param tid - identificador de tooltip
	 * @param dlt - id de resourceBundle para titulo del dialogo
	 * @param sup - instancia de <code>ActionPerformer</code>
	 */
	public TGenericAction(String tex, String inam, String tid, String dlt, 
		UIComponentPanel uicp, ActionPerformer sup) {
		super(tex, inam, TAbstractAction.NO_SCOPE, tid);
		this.rdInput = uicp;
		this.supplier = sup;
		this.dialog = null;
		this.dlg_title = dlt;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		rdInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(rdInput, dlg_title);
		dialog.setVisible(true);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		TActionEvent ltevt = new TActionEvent(this);
		ltevt.setRedirectAction((RedirectAction) evt.getNewValue());
		boolean ok = supplier.executeAction(ltevt);
		if (ok) {
			dialog.dispose();
		}
	}
}
