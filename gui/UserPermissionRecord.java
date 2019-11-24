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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * esta clase explora el contenido de la docking y de gui.impl en busca de instancias de tabstractable. con una lista de
 * estas (contenedor), localiza las acciones contenidas dentro de esta y construye una lista con todas las ventanas y
 * sus acciones para que se puedan autorizar o no al usuario pasado como argumento del constructor
 * 
 */
public class UserPermissionRecord extends AbstractRecordDataInput implements ActionListener {

	private Color bgColor;
	private Hashtable containerList;
	private String value;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro de usuario
	 */
	public UserPermissionRecord(Record rcd) {
		super("security.title03", rcd);
		this.bgColor = TUIUtils.brighter(new JPanel().getBackground());
		this.containerList = new Hashtable();
		this.value = (String) rcd.getFieldValue("t_uspermission");

		// RecordSelector rs = new RecordSelector(new ServiceRequest(ServiceRequest.DB_QUERY, "t_users",
		// "t_ususer_id != '*master'"), "t_ususer_id", "t_usname", rcd.getFieldValue("t_sv_id"));
		// jtfvalue = new JTextField((String) rcd.getFieldValue(t_svvalue));
		// addInputComponent("t_sv_id", rs, false, newr);
		// addInputComponent(t_svvalue, jtfvalue, true, true);

		FormLayout lay = new FormLayout("250dlu", // columns
				"top:200dlu");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		// agrega los componentes en el panel
		// build.add(getLabelFor("t_sv_id"), cc.xy(1, 1));
		// build.add(getInputComponent("t_sv_id"), cc.xy(3, 1));
		build.add(getTree(), cc.xy(1, 1));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	/**
	 * explora contenido de paquetes <code>Desk.dockPack y Desk.guiImpl</code> analizando el contenido de las clases y
	 * determinando que tipo son para con esta informacion armar la lista de ventanas y dentro de ellas, las acciones a
	 * las que el usuario puede tener acceso.
	 * 
	 * @return JScrollPane con elementos
	 */
	private JScrollPane getTree() {

		// 170502: TODO: docking package changed: fix
		// String[] cl = TResourceUtils.getClassFrom(PlanC.dockingModules[0]);
		String[] cl = {};
		Box vb2 = Box.createVerticalBox();
		JScrollPane jsp = new JScrollPane(vb2);;
		Color bgc = TUIUtils.brighter(new JPanel().getBackground());
		jsp.getViewport().setBackground(bgc);
		for (String cln1 : cl) {
			try {
				Class cls = Class.forName(cln1);
				Object dobj = cls.newInstance();
				if (dobj instanceof UIListPanel) {
					String cln = TResources.getClassName(dobj);
					boolean cntsel = isIn(cln, null);
					JCheckBox jcb = new JCheckBox(cln, cntsel);
					jcb.setName(cln);
					jcb.addActionListener(this);
					vb2.add(getBox(jcb, TUIUtils.H_GAP));
					UIListPanel dc = (UIListPanel) dobj;
					TAbstractAction[] jbs = dc.getToolBarActions();
					Vector v = new Vector();
					for (TAbstractAction jb : jbs) {
						/*
						 * 180101: changes in uicomponentpanel !! rebuild this class JCheckBox jcb1 = new
						 * JCheckBox(jb.getName(), isIn(cln, jb.getName())); jcb1.setName(jb.getName());
						 * jcb1.setEnabled(cntsel); v.add(jcb1); vb2.add(getBox(jcb1, TUIUtils.H_GAP * 4));
						 */
					}
					containerList.put(jcb, v);
				}
			} catch (Exception ex) {
				// si ocurre una ex, no se registra,. se asume que esa clase si no posee constructor por omision
				// no deberia estar en esta lista de autorizaciones.
			}
		}
		return jsp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractDataInput#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		// habilitado / desabilitado de acciones si contenedor esta seleccionado o no
		Object o = ae.getSource();
		if (o instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) o;
			Vector v = (Vector) containerList.get(o);
			if (v != null) {
				for (int i = 0; i < v.size(); i++) {
					JCheckBox jcb1 = (JCheckBox) v.elementAt(i);
					jcb1.setEnabled(jcb.isSelected());
				}
			}
			validateFields();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractRecordDataInput#validateFields()
	 */
	@Override
	public void validateFields() {
		boolean sel = false;
		// habilita acciones dentro de contenedor solo si el contenedor esta seleccionado
		Enumeration enume = containerList.keys();
		while (enume.hasMoreElements()) {
			JCheckBox jcb = (JCheckBox) enume.nextElement();
			sel = (!sel) ? jcb.isSelected() : sel;
			Vector v = (Vector) containerList.get(jcb);
			for (int i = 0; i < v.size(); i++) {
				JCheckBox j = (JCheckBox) v.elementAt(i);
				j.setEnabled(jcb.isSelected());
			}
		}
		// coloco algo en el campo para abilitar aceptar

		if (!sel) {
			showAplicationExceptionMsg("msg20");
		}
		setEnableDefaultButton(!isShowingError());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractRecordDataInput#getRecord()
	 */
	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		// construye la estuctura del campo t_uspermission segun la estructura estandar
		// cnt;act,act,act;cnt,act ...
		String s = "";
		Enumeration enume = containerList.keys();
		while (enume.hasMoreElements()) {
			JCheckBox jcb = (JCheckBox) enume.nextElement();
			if (jcb.isSelected()) {
				s += jcb.getName() + ",";
				Vector v = (Vector) containerList.get(jcb);
				for (int i = 0; i < v.size(); i++) {
					JCheckBox j = (JCheckBox) v.elementAt(i);
					if (j.isEnabled() && j.isSelected()) {
						s += j.getName() + ",";
					}
					j.setEnabled(jcb.isSelected());
				}
				s = s.substring(0, s.length() - 1) + ";";
			}
		}
		r.setFieldValue("t_uspermission", s.substring(0, s.length() - 1));
		return r;
	}

	/**
	 * verifica si el contenedor cnt y el objeto objn estan presentes dentro del campo <code>t_uspermission</code>
	 * dentro del registro de usuario
	 * 
	 * @param cnt - contenedor
	 * @param objn - objeto. puede ser null si se desea solo verificar el contenedor
	 * @return true si esta presente
	 */
	private boolean isIn(String cnt, String objn) {
		boolean cnta = false;
		boolean obja = (objn == null);
		// contenedor
		String[] c_o = value.split(";");
		for (int i = 0; i < c_o.length && !cnta; i++) {
			// objetos dentro de contenedor
			cnta = c_o[i].startsWith(cnt);
			if (cnta && !obja) {
				String[] cls = c_o[i].split(",");
				for (int j = 1; j < cls.length && !obja; j++) {
					obja = cls[j].equals(objn);
				}
			}
		}
		return cnta && obja;
	}

	/**
	 * retorna box segun argumentos de entrada
	 * 
	 * @param jcb - instancia de jcheckbox
	 * @param hg - gap izquierdo
	 * @return box
	 */
	private Box getBox(JCheckBox jcb, int hg) {
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalStrut(hg));
		b.add(jcb);
		b.add(Box.createHorizontalGlue());
		jcb.setBackground(bgColor);
		return b;
	}
}
