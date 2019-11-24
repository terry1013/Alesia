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


import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * Panel de edicion de direcciones. Clases que editan registro con direccines en debe usar esta clase para editar y
 * presentar las direcciones en forma uniforme.
 * 
 */
public class AddressEditor extends AbstractRecordDataInput {

	private OpenAddressEditor openEdit;
	private JButton buttonAddrArea;
	private JTextField jftf_Addr;
	private String addrpatt;

	/**
	 * verifica la existencia del registro cuyo identificador esta almacenado en el campo de entrada usado en el
	 * constructor. si no existe, se obtiene un modelo y se crea un registro en blanco para desde ya, asignar un
	 * registro de diereccion.
	 * 
	 * @return - registro de direccion localizado o encontrado
	 */
	private Record localize() {
		String idn = jftf_Addr.getText();
		Record rc = null;
		if (idn == null || idn.equals("")) {
			rc = DataBaseUtilities.getAddressRecord();
		} else {
			rc = DataBaseUtilities.getFromSerializedForm(idn);
		}
		return rc;

	}

	/**
	 * nueva instancia
	 * 
	 * @param jftf - componente que sera actualizado con el identificador de registro de diereccion
	 */
	public AddressEditor(JTextField jftf) {
		// public AddressEditor(JLabel jl, JFormattedTextField jftf) {
		super("title_AddressInfo", null);
		this.addrpatt = TStringUtils.getString("address_patt");
		this.jftf_Addr = jftf;
		Record rcd = localize();
		setModel(rcd);

		this.openEdit = new OpenAddressEditor(this);
		// this.etiquete = jl;
		addInputComponent("state", TUIUtils.getJTextField(rcd, "state"), containdAddrField("state"), true);
		addInputComponent("city", TUIUtils.getJTextField(rcd, "city"), containdAddrField("city"), true);
		addInputComponent("district", TUIUtils.getJTextField(rcd, "district"), containdAddrField("district"), true);
		addInputComponent("parish", TUIUtils.getJTextField(rcd, "parish"), containdAddrField("parish"), true);
		addInputComponent("urbanitation", TUIUtils.getJTextField(rcd, "urbanitation"),
				containdAddrField("urbanitation"), true);
		addInputComponent("street", TUIUtils.getJTextField(rcd, "street"), containdAddrField("street"), true);
		addInputComponent("building", TUIUtils.getJTextField(rcd, "building"), containdAddrField("building"), true);
		addInputComponent("establishment", TUIUtils.getJTextField(rcd, "establishment"),
				containdAddrField("establishment"), true);
		addInputComponent("geographic_code", TUIUtils.getJTextField(rcd, "geographic_code"),
				containdAddrField("geographic_code"), true);
		addInputComponent("telephone_1", TUIUtils.getJTextField(rcd, "telephone_1"),
				containdAddrField("telephone_1"), true);
		addInputComponent("telephone_2", TUIUtils.getJTextField(rcd, "telephone_2"),
				containdAddrField("telephone_2"), true);
		addInputComponent("fax", TUIUtils.getJTextField(rcd, "fax"), containdAddrField("fax"), true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		// lay.setColumnGroups(new int[][]{{1, 5}, {3, 7}});
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("state"), cc.xy(1, 1));
		build.add(getInputComponent("state"), cc.xy(3, 1));
		build.add(getLabelFor("city"), cc.xy(5, 1));
		build.add(getInputComponent("city"), cc.xy(7, 1));
		build.add(getLabelFor("district"), cc.xy(1, 3));
		build.add(getInputComponent("district"), cc.xy(3, 3));
		build.add(getLabelFor("parish"), cc.xy(5, 3));
		build.add(getInputComponent("parish"), cc.xy(7, 3));
		build.add(getLabelFor("urbanitation"), cc.xy(1, 5));
		build.add(getInputComponent("urbanitation"), cc.xy(3, 5));
		build.add(getLabelFor("street"), cc.xy(5, 5));
		build.add(getInputComponent("street"), cc.xy(7, 5));
		build.add(getLabelFor("building"), cc.xy(1, 7));
		build.add(getInputComponent("building"), cc.xy(3, 7));
		build.add(getLabelFor("establishment"), cc.xy(5, 7));
		build.add(getInputComponent("establishment"), cc.xy(7, 7));
		build.add(getLabelFor("geographic_code"), cc.xy(1, 9));
		build.add(getInputComponent("geographic_code"), cc.xy(3, 9));
		build.add(getLabelFor("telephone_1"), cc.xy(5, 9));
		build.add(getInputComponent("telephone_1"), cc.xy(7, 9));
		build.add(getLabelFor("telephone_2"), cc.xy(1, 11));
		build.add(getInputComponent("telephone_2"), cc.xy(3, 11));
		build.add(getLabelFor("fax"), cc.xy(5, 11));
		build.add(getInputComponent("fax"), cc.xy(7, 11));
		setActionBar(new AbstractAction[] { new GenericClear(this), new AceptAction(this), new CancelAction(this) });
		add(build.getPanel());
		preValidate(null);
	}

	/**
	 * Retorna la interfaz de entrada/salida de direccion. Esta permite a las clases con campos de direccion editar
	 * direcciones a travez de este panel de entrada y a su vez, presenta la direccion redactada de manera uniforme
	 * 
	 * @return panel con componentes de entrada/salida de direcciones.
	 */
	public JButton getTicket() {

		this.buttonAddrArea = new JButton(openEdit);
		openEdit.setName(TResources.formatAddress(getRecord()));
		// openEdit.setToolTip("ttd03");
		// buttonAddrArea.setToolTipText(ConstantUtilities.getBundleString("ttd03"));
		return buttonAddrArea;
	}

	private boolean containdAddrField(String fn) {
		String ap = "<" + fn + ">";
		return addrpatt.contains(ap);
	}

	/**
	 * Invocado por <code>openAddressEditor</code> cuando el usuario presiona algun boton dentro del dialogo de entrada.
	 * este metodo da formato al contenido y actualiza el identificador de registro y el registro de base de datos
	 * 
	 * @param aaa - accion presionada
	 */
	public void done(AbstractAction aaa) {
		if (aaa instanceof GenericClear) {
			Record re = getRecord();
			for (int i = 0; i < re.getFieldCount(); i++) {
				JTextField jtf = (JTextField) getInputComponent(re.getFieldName(i));
				if (jtf != null) {
					jtf.setText("");
				}
			}
			jftf_Addr.setText("");
		}

		if (aaa instanceof AceptAction) {
			Record re = getRecord();
			jftf_Addr.setText((String) DataBaseUtilities.getSerializableRecord(re));

		}
		// addrArea.setText(ResourceUtilities.formatAddress(getRecord()));
		buttonAddrArea.setText(TResources.formatAddress(getRecord()));
	}
}
