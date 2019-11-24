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

import gui.wlaf.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import core.*;

/**
 * Clase raiz de todas las acciones dentro de la aplicacion. Esta clase define un elemento llamado alcance. el alcance
 * en general indica al un contenedor el destino de las operaciones definidas dentro de esta accion.
 * 
 * El arguento de entrada rid identifica el rol para el cual esta accion es visible. de esta forma, toda accion
 * dependiente del rol es capas de determinar si se presenta o no segun el usuario que actualmente esta in sesion.
 * 
 * 
 */
public abstract class TAbstractAction extends AbstractAction {

	public static final int NO_SCOPE = 0;
	public static final int TABLE_SCOPE = 1;
	public static final int RECORD_SCOPE = 2;
	public static final String NAME_ID = "nameID";
	public static final String ICON_ID = "iconID";

	// ideas for implementation: change th actinperformer interface for antorer implementation. in this new
	// implementation, extends the class actionlistener and replace the method getuifor to somethin like previousAction
	// and delete the execute action method. in this way, i can use this interface for more generic purpose that only
	// for UI and:
	// - ganin preproces, and pos proces of generic actions, thas is more clean implementation of actionperformed2
	// 2
	
	protected ActionPerformer supplier;

	private double dimentionFactor = -1;
	private int scope;
	protected static Hashtable<String, JDialog> dialogs = new Hashtable<String, JDialog>();

	// TEST FOR MIGRATION
	// ------------------------------------------------------------------------------
	// this prefix allow change the text display en confirmation dialog
	protected String messagePrefix = "";
	// for new/edit, set if use write instead of add/update
	protected boolean allowWrite;
	protected EditableList editableList;

	public EditableList getEditableList() {
		return editableList;
	}

	public void setEditableList(EditableList editableList) {
		this.editableList = editableList;
	}

	protected RedirectAction redirectAction;
	protected JDialog dialog;

	/**
	 * update the {@link #setIcon(String)}, {@link #setName(String)}, {@link #setToolTip(String)} using the parm
	 * arguments as base for this parameteres
	 * 
	 * @param base - base string for retrive standar values for this action,
	 */
	public void setDefaultValues(String cls) {
		String aid = "action." + cls;
		setIcon(cls);
		setName(aid);
		// for actions, if no tooltip is present, avoid presentation of id (recomended for all rest of component to
		// force programer to write the tooltip text
		String ttid = "tt" + aid;
		if (!TStringUtils.getString(ttid).equals(ttid)) {
			setToolTip(ttid);
		}
	}

	public void setMessagePrefix(String messagePrefix) {
		this.messagePrefix = messagePrefix;
	}

	public boolean isAllowWrite() {
		return allowWrite;
	}

	public void setAllowWrite(boolean allowWrite) {
		this.allowWrite = allowWrite;
	}

	// convert to abstract
	public void actionPerformed2() {

	}

	public TAbstractAction() {
		this(NO_SCOPE);
	}

	public TAbstractAction(int sco) {
		setScope(sco);
		String cls = this.getClass().getSimpleName();
		setDefaultValues(cls);
	}
	/**
	 * 171214: schedule to deprecated use {@link TAbstractAction#TAbstractAction(int)}
	 * 
	 */
	protected TAbstractAction(String tid, String inam, int sco, String ttid) {
		setIcon(inam);
		setName(tid);
		setScope(sco);
		setToolTip(ttid);
	}

	/**
	 * retorna alcance de esta accion.
	 * 
	 * @return alcance
	 */
	public int getScope() {
		return scope;
	}

	@Override
	public boolean isEnabled() {
		boolean ena = super.isEnabled();
		// TODO: complete code for aut to finish implemetation
		boolean aut = true;
		// if action is disabled for any reason, remain disabled
		return ena ? aut : false;
	}
	/**
	 * establece descripcion corta (ttoltip)
	 * 
	 * @param tid - id de tooltip
	 */
	public void setToolTip(String tid) {
		if (tid != null) {
			String sd = TStringUtils.getInsertedBR((String) TStringUtils.getString(tid), 80);
			putValue(SHORT_DESCRIPTION, sd);
		}
	}

	/**
	 * establece alcance
	 * 
	 * @param sco - alcance
	 */
	public void setScope(int sco) {
		this.scope = sco;
		if (scope == RECORD_SCOPE) {
			setEnabled(false);
		}
	}

	@Override
	public abstract void actionPerformed(ActionEvent arg0);

	/**
	 * build and return the standar {@link JDialog} for the applicacion.
	 * <p>
	 * NOTE: this method is for spetias cases. for normal operations, use {@link #getDialog(JComponent, String)}.
	 * 
	 * @param pane - content inside the dialog
	 * @param tit - bundle id for the dialog title
	 * @param deface - <code>true</code> to set the default button for this dialog. <code>false</code> dont set the
	 *        default button
	 * @param defcan - <code>true</code>
	 * @return
	 */
	public JDialog getDialog(JComponent pane, String tit, boolean deface, boolean defcan) {
		JDialog dialog = new JDialog(Alesia.mainFrame, true);
		dialog.setContentPane(pane);
		dialogs.put(pane.getClass().getName(), dialog);
		JButton jb = (JButton) pane.getClientProperty(TConstants.DEFAULT_BUTTON);
		if (jb != null && !deface) {
			dialog.getRootPane().setDefaultButton(jb);
		}

		// operaciones para cerrar ventana se desvia al boton cuya accion sea instancia de DefaultCancelAction. si
		// ningun boton ha sido descrito dentro de esta propiedad, el dialogo se cierra normalmente.
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton jb2 = (JButton) pane.getClientProperty(TConstants.DEFAULT_CANCEL_BUTTON);
		TAbstractAction taa = jb2 == null ? null : (TAbstractAction) jb2.getAction();
		if (taa != null && !defcan) {
			dialog.addWindowListener(new DialogListener(taa, pane));
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}

		dialog.pack();
		if (dimentionFactor > 0) {
			dialog.setSize(Alesia.mainFrame.getBoundByFactor(dimentionFactor).getSize());
		}
		dialog.setLocationRelativeTo(Alesia.mainFrame);
		dialog.setResizable(false);
		dialog.setTitle(TStringUtils.getString(tit));
		return dialog;

	}

	public JDialog getDialog(JComponent pane, String tit) {
		return getDialog(pane, tit, true, true);
	}

	/**
	 * return the active instance of {@link JDialog}
	 * 
	 * @param cn - id of open dialog
	 * @return
	 */
	public static JDialog getActiveJDialog(String cn) {
		return dialogs.get(cn);
	}

	/**
	 * establece atributo <code>SMALL_ICON</code> para esta accion
	 * 
	 * @param tex - nombre de icono
	 */
	public void setIcon(String inam) {
		if (inam != null) {
			putValue(SMALL_ICON, TResources.getSmallIcon(inam));
			putValue(ICON_ID, inam);
		}
	}

	/**
	 * set the {@link Action#NAME} property with the string found in {@link TStringUtils#getString(String)}.
	 * Additionaly set the property {@link TAbstractAction#NAME_ID} with the id pass as argument
	 * 
	 * @param tex - id for string
	 */
	public void setName(String tex) {
		if (tex != null) {
			putValue(NAME, TStringUtils.getString(tex));
			putValue(NAME_ID, tex);
		}
	}

	/**
	 * Set the factor for calculating the dialog size. This value is used to define the jdialog size during
	 * {@link #getDialog(JComponent, String)} method
	 * 
	 * @see TWebFrame#getBoundByFactor(double)
	 * @see TWebFrame#getBoundByFactor(double, double)
	 * 
	 * @param f - size factor (0, 1). -1 means {@link Dialog#pack()}
	 */
	public void setDimentionFactor(double f) {
		this.dimentionFactor = f;
	}

	/**
	 * ejecuta <code>Jdialog.dispose()</code> para todos los dialogos creado usando el mentodo
	 * <code>TabstractAction.getDialog()</code>. esto permite que durante una finalizacion de secion, no queden dialogos
	 * abiertos
	 * 
	 */
	public static void shutdown() {
		Vector<JDialog> v = new Vector<JDialog>(dialogs.values());
		for (JDialog jDialog : v) {
			jDialog.dispose();
		}
		dialogs.clear();
	}
}
