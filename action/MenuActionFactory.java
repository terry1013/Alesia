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

import javax.swing.*;

import core.*;

/**
 * esta accion extiende <code>TAbstractAction</code> con soporte basico para crear acciones masivas y colocarlas en una
 * barra de menu. la clase recibida como argumento debe ser una instancia de <code>UIComponentPanel</code> que sera
 * instanciada y colocada dentro de un dialogo
 */
public class MenuActionFactory extends TAbstractAction {

	private Class clas;
	private UIComponentPanel uicmp;

	public MenuActionFactory(Class cls) {
		super(TAbstractAction.NO_SCOPE);
		this.clas = cls;
		String csn = cls.getSimpleName();
		setName(csn);
		setIcon(csn);
		// standar dimention for this kind of actions
		setDimentionFactor(.4);
	}

	public MenuActionFactory(String tid, String inam, ActionPerformer ap) {
		super(TAbstractAction.RECORD_SCOPE);
		this.supplier = ap;
		setIcon(inam);
		setName(tid + ".title");
		setToolTip("tt" + tid);
		// standar dimention for this kind of actions
		setDimentionFactor(.4);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			// by class
			if (clas != null) {
				uicmp = (UIComponentPanel) clas.newInstance();
			}
			// by ui supplier
			if (supplier != null) {
				uicmp = supplier.getUIFor(this);
			}
			if (uicmp instanceof UIListPanel) {
				((UIListPanel) uicmp).init();
			}
			JDialog dialog = getDialog(uicmp, (String) getValue(NAME));
			dialog.setVisible(true);
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}
}
