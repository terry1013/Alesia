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

import java.awt.event.*;

import javax.swing.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * panel de nuevo/edicion para archivo de constantes
 * 
 */
public class SystemVarsRecord extends AbstractRecordDataInput implements ActionListener {

	private JComboBox classSel;
	private JTextField jtfgroup, jtfid;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es nuevo registro
	 */
	public SystemVarsRecord(Record rcd, boolean newr) {
		super((newr) ? "title_newrecord" : "title_editrecord", rcd);

		// si newr y tengo identificador de constante, inabilito el campo
		boolean newr1 = ((String) rcd.getFieldValue("t_svgroup")).equals("");
		this.jtfgroup = TUIUtils.getJTextField(rcd, "t_svgroup");
		addInputComponent("t_svgroup", jtfgroup, true, newr && newr1);
		this.jtfid = TUIUtils.getJTextField(rcd, "t_sv_id");
		addInputComponent("t_sv_id", jtfid, true, newr);
		addInputComponent("t_svvalue", TUIUtils.getJTextArea(rcd, "t_svvalue"), true, true);

		this.classSel = TUIUtils.getJComboBox("val_cls", rcd, "t_svvalue_class");
		classSel.addActionListener(this);
		addInputComponent("t_svvalue_class", classSel, false, true);
		addInputComponent("t_svdescription", TUIUtils.getJTextArea(rcd, "t_svdescription", 3),
				true, true);

		// crea el layout con formato para presentar los datos
		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		// agrega los componentes en el panel
		build.add(getLabelFor("t_svgroup"), cc.xy(1, 1));
		build.add(getInputComponent("t_svgroup"), cc.xy(3, 1));
		build.add(getLabelFor("t_sv_id"), cc.xy(1, 3));
		build.add(getInputComponent("t_sv_id"), cc.xy(3, 3));
		build.add(getLabelFor("t_svvalue"), cc.xy(1, 5));
		build.add(getInputComponent("t_svvalue"), cc.xy(3, 5));
		build.add(getLabelFor("t_svvalue_class"), cc.xy(1, 7));
		build.add(getInputComponent("t_svvalue_class"), cc.xy(3, 7));
		build.add(getLabelFor("t_svdescription"), cc.xy(1, 9));
		build.add(getInputComponent("t_svdescription"), cc.xy(3, 9));

		JPanel jp = build.getPanel();
		// jp.setBackground(Color.BLACK);
		add(jp);
		setDefaultActionBar();
		preValidate(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractDataInput#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		boolean ena = true;
		if (ae.getSource() == classSel) {
			TEntry e = (TEntry) classSel.getSelectedItem();
			// TODO: verificar si hace falta verificacion de valor contra clase de valor
		}
		// setEnabledInputComponent(t_svvalue, ena);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractRecordDataInput#validateFields()
	 */
	@Override
	public void validateFields() {
		String v = jtfgroup.getText();
		String v1 = jtfid.getText();
		if (v.contains(" ") || v1.contains(" ")) {
			showAplicationException(new TError("msg26"));
		}
		setEnableDefaultButton(!isShowingError());
	}
}
