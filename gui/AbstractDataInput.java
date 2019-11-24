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

import gui.html.*;
import gui.wlaf.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import action.*;

import com.alee.extended.date.*;
import com.alee.extended.filechooser.*;

import core.*;

/**
 * Provee funcionalidad basica para la entrada de datos por parte del usuario. el contrato general es:
 * 
 * se crea una sub. clase de esta, registranado los componentes visuales usando los metodos
 * <code>addInputComponent()</code>
 * 
 * se implementa el metodo <code>validataFields</code>
 * 
 * se invoca <code>getFields()</code>
 * 
 */
public abstract class AbstractDataInput extends UIComponentPanel
		implements
			ActionListener,
			DocumentListener,
			FocusListener,
			FilesSelectionListener {

	protected Hashtable name_component, name_jlabel, component_isRequired, fields;
	private TError msg12, msg17, msg18;
	// 180212: lazy implemetation of filechooser
	private TWebFileChooserField chooserField = null;

	/**
	 * nueva instancia
	 * 
	 * @param dnam - Nombre del documento a mostrar en la barra de informacion
	 * 
	 */
	public AbstractDataInput(String dnam) {
		super(dnam, true);
		this.name_component = new Hashtable();
		this.component_isRequired = new Hashtable();
		this.fields = new Hashtable();
		this.name_jlabel = new Hashtable();
		this.msg12 = new TError("ui.msg12");
		this.msg17 = new TError("ui.msg17");
		this.msg18 = new TError("ui.msg18");
	}

	public void actionPerformed(ActionEvent ae) {
		preValidate(ae.getSource());
	}

	@Override
	public void changedUpdate(DocumentEvent de) {
		// validateRecord();
	}

	/**
	 * invoca <code>TextFieldVerifier.verify()</code> para los componentes de entrada instancias de
	 * <code>JTextComponet</code>. Este metodo solo verificara los componentes que esten habilitados
	 * 
	 * @return <code>true</code> si se ha verificado todos los campos y estas estan Ok.
	 */
	public boolean checkInputVerifier() {
		boolean all = true;
		Enumeration en = name_component.keys();
		while (en.hasMoreElements()) {
			JComponent icm = (JComponent) name_component.get((String) en.nextElement());
			JComponent icmp = getInternal(icm);
			if (icmp.isEnabled() && icmp.getInputVerifier() != null) {
				TInputVerifier ver = (TInputVerifier) icmp.getInputVerifier();
				if (!ver.verify(icmp)) {
					all = false;
					break;
				}
			}
		}
		return all;
	}

	/**
	 * verifica instancias de <code>JComboBox</code>buscando si algunos de estos componentes no contiene elementos. si
	 * esto es cierto, este metodo emite una exepcion.
	 */
	public void checkComboBoxes() {
		Enumeration en = name_component.keys();
		while (en.hasMoreElements()) {
			JComponent icm = (JComponent) name_component.get((String) en.nextElement());
			JComponent icmp = getInternal(icm);
			if (icmp instanceof JComboBox) {
				JComboBox jcb = (JComboBox) icmp;
				if (jcb.getItemCount() == 0) {
					showAplicationException(msg12);
					// error grave. no es necesario guardar el color original
					jcb.setBackground(msg12.getErrorColor());
					break;
				}
			}
		}
	}

	@Override
	public void focusGained(FocusEvent fe) {

	}

	@Override
	public void focusLost(FocusEvent fe) {
		preValidate(fe.getSource());
	}

	@Override
	public void selectionChanged(List<File> files) {
		preValidate(null);
	}

	/**
	 * return the internal fields. This list is for store purpose. Some implementation of this class needs to store
	 * parameters that has no visual component associated with, but will be needed futher.
	 * 
	 * @param fid field name
	 * @param val - value to store
	 * @see #setFields(Hashtable)
	 */
	public Hashtable getInternalFields() {
		return fields;
	}

	/**
	 * Copy all fields form the <code>flds</code> argument to the internal field list.
	 * <p>
	 * This method is for store purpose. Some implementation of this class needs to store parameters that has no visual
	 * component associated with, but will be needed futher.
	 * 
	 * @param flds - parameters to copy
	 * @see #getFields()
	 * @see #getInternalFields()
	 */
	public void setFields(Hashtable flds) {
		this.fields.putAll(flds);

		/*
		 * 180218: THIS CODE IS OLD AND NEVER TESTED. BUT UPDATING THE INTERNAL HASHTABLE IS NEEDED NOW
		 * 
		 * set the fields for this UI. All component must be registred first using {@linkplain
		 * AbstractDataInput#addInputComponent(String, JComponent, boolean, boolean)} or similar method. this method
		 * assume the standar Object <--> Component relation. for example <ul> <li>Number <--> {@link
		 * JFormattedTextField} <li>Boolean <--> {@link JCheckBox} or {@link JRadioButton} <li>String <--> {@link
		 * JTextArea} or {@link JTextField} <li>... </ul>
		 * 
		 * @param flds list of fields names and values to set inside this GUI
		 * 
		 * @see AbstractDataInput#getFields()
		 */
		/*
		 * Enumeration ek = flds.keys(); for (; ek.hasMoreElements();) { String knam = (String) ek.nextElement();
		 * JComponent jcm = (JComponent) name_component.get(knam); if (jcm == null) { continue; } Object val =
		 * flds.get(knam);
		 * 
		 * JComponent jcmp = getInternal(jcm); // date field if (jcmp instanceof WebDateField) { WebDateField wdf =
		 * ((WebDateField) jcmp); wdf.setText(val.toString()); continue; } // numeros if (jcmp instanceof
		 * JFormattedTextField) { ((JFormattedTextField) jcmp).setValue(val); continue; } // fecha o hora if (jcmp
		 * instanceof DateTimeSpinner) { // TODO: implement // ((DateTimeSpinner) jcmp).setDate(val); } // texto if
		 * (jcmp instanceof JTextComponent) { ((JTextComponent) jcmp).setText(val.toString()); } // property table if
		 * (jcmp instanceof TPropertyJTable) { ((TPropertyJTable) jcmp).setPropertys(val.toString()); }
		 * 
		 * // selector de registros if (jcmp instanceof SearchTextField) {
		 * 
		 * } // html text if (jcmp instanceof HTMLEditor) { ((HTMLEditor) jcmp).setText(val.toString()); } // boolean if
		 * (jcmp instanceof JCheckBox) { ((JCheckBox) jcmp).setSelected((Boolean) val); } if (jcmp instanceof
		 * JRadioButton) { ((JRadioButton) jcmp).setSelected((Boolean) val); } // campo solo de salida. if (jcmp
		 * instanceof ExtendedJLabel) { ((ExtendedJLabel) jcmp).setValue(val); }
		 * 
		 * // key element in JComboBox Tentry list. // 170611: special elements (special Tentry values in
		 * constant.properties) must remain if (jcmp instanceof JComboBox) { ((JComboBox) jcmp).setSelectedItem(val); }
		 * // Conjuntos de elementos seleccionados del ListRecordSelector if (jcmp instanceof RecordSelectorList) {
		 * 
		 * } }
		 */
	}

	/**
	 * Return a new {@link Hashtable} with all values setted by this GUI. This method assume the standar Object <-->
	 * Component relation. for example:
	 * <ul>
	 * <li>Number <--> {@link JFormattedTextField}
	 * <li>Boolean <--> {@link JCheckBox} or {@link JRadioButton}
	 * <li>String <--> {@link JTextArea} or {@link JTextField}
	 * </ul>
	 * this method also will return all stored parameters using the {@link #setFields(Hashtable)} method
	 * 
	 * @return Hashtable with fields name and values found in this UI
	 * @see #setFields(Hashtable)
	 */
	public Hashtable getFields() {
		Hashtable h = new Hashtable(fields);
		Enumeration ek = name_component.keys();

		for (; ek.hasMoreElements();) {
			String knam = (String) ek.nextElement();
			JComponent jcm = (JComponent) name_component.get(knam);
			Object val = null;

			JComponent jcmp = getInternal(jcm);
			// date field
			if (jcmp instanceof WebDateField) {
				WebDateField wdf = ((WebDateField) jcmp);
				String dt = wdf.getText();
				val = (dt.equals("")) ? TStringUtils.ZERODATE : wdf.getDate();
				h.put(knam, val);
				continue;
			}
			// numeros
			if (jcmp instanceof JFormattedTextField) {
				val = ((JFormattedTextField) jcmp).getValue();
				h.put(knam, val);
				continue;
			}
			// fecha o hora
			if (jcmp instanceof DateTimeSpinner) {
				val = ((DateTimeSpinner) jcmp).getDate();
			}
			// texto
			if (jcmp instanceof JTextComponent) {
				val = ((JTextComponent) jcmp).getText();
			}
			// 170911: property table
			if (jcmp instanceof TPropertyJTable) {
				val = ((TPropertyJTable) jcmp).getProperties();
			}
			// 180212: TWebFileChooserField string representation of File or *none if no file selected
			if (jcmp instanceof TWebFileChooserField) {
				String s = ((TWebFileChooserField) jcmp).getSelectedFile();
				val = s == null ? "*none" : s;
			}
			// selector de registros
			// if (jcmp instanceof AssistedJTextField) {

			// }
			// html text
			if (jcmp instanceof HTMLEditor) {
				val = ((HTMLEditor) jcmp).getText();
			}
			// boolean
			if (jcmp instanceof JCheckBox) {
				val = new Boolean(((JCheckBox) jcmp).isSelected());
			}
			if (jcmp instanceof JRadioButton) {
				val = new Boolean(((JRadioButton) jcmp).isSelected());
			}
			// campo solo de salida. si el valor es instancia de LTEntry, retorna clave
			if (jcmp instanceof ExtendedJLabel) {
				val = ((ExtendedJLabel) jcmp).getValue();
				if (val instanceof TEntry) {
					val = ((TEntry) val).getKey();
				}
			}

			// key element in JComboBox Tentry list.
			// 170611: special elements (special Tentry values in constant.properties) must remain
			if (jcmp instanceof JComboBox) {
				if (jcmp instanceof CheckComboBox) {
					val = (String) ((CheckComboBox) jcmp).getSelectedItem();
				} else {
					// 180611: temporal for flicka: test class first. if is not TEntry, store the object found as value.
					// this is for editableJCombbox
					Object obj = ((JComboBox) jcmp).getSelectedItem();
					if (obj instanceof TEntry) {
//						TEntry ae = (TEntry) ((JComboBox) jcmp).getSelectedItem();
//						val = ae.getKey();
						val = ((TEntry) obj).getKey();
					} else {
						val = obj;
					}
				}
			}
			// Conjuntos de elementos seleccionados del ListRecordSelector
			if (jcmp instanceof RecordSelectorList) {
				val = ((RecordSelectorList) jcmp).getSelectedElement();
			}
			if (val == null) {
				throw new NullPointerException("No value fount for field " + knam);
			}
			h.put(knam, val);
		}
		return h;
	}

	/**
	 * retorna el componente de entrada registrado con el nombre del argumento
	 * 
	 * @param na - nombre del argumento
	 * @return componente
	 */
	public JComponent getInputComponent(String na) {
		return (JComponent) name_component.get(na);
	}

	/**
	 * retorna la etiqueta designada para el componente cuyo identificador fue pasado como argumento.
	 * 
	 * @param na - nombre del componente
	 * @return - etiqueta
	 */
	public JLabel getLabelFor(String na) {
		return (JLabel) name_jlabel.get(na);
	}

	/**
	 * habilita/desabilita los componentes relacionados con un campo de un registro
	 * 
	 * @param fn - nombre del campo
	 * @param ena - true = habilitado, false desabilitado
	 */
	public void setEnabledInputComponent(String fn, boolean ena) {
		getLabelFor(fn).setEnabled(ena);
		getInputComponent(fn).setEnabled(ena);
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		preValidate(de.getDocument());
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		preValidate(de.getDocument());
	}

	/**
	 * Validacion de campos de entrada de registro. Sub clases deben implementar este metodo para validar la situacion
	 * de los componentes de entrada y su relacion, presentar los mensajes pertinentes y realizar los cambios dentro del
	 * GUI para asegurar que el usuario coloque los valores conrrectos en los componentes correctos.
	 * 
	 * <b>Nota sobre implementacion:</b> validar los campos en orden inverso; es decir, validar primero los valores de
	 * los componentes que se encuentran mas abajo de la ventana continuando hacia los de arriba. Esto es para que se
	 * presente el mensaje de error del componente que se encuentre mas arriva
	 * 
	 * public abstract void validateFields();
	 */

	/**
	 * metodo que verifica si el componente pasado como argumento es una instacia de
	 * <code>JScrollPane o SearchTextField</code> Si lo es, altera el argumento para que sea el componente interno del
	 * mismo o el que realmente contiene los datos
	 * 
	 * @param jcmp - componente a alterar
	 */
	protected JComponent getInternal(JComponent jcmp) {
		JComponent jc = jcmp;
		if (jcmp instanceof JScrollPane) {
			JViewport jvp = (JViewport) ((JScrollPane) jcmp).getViewport();
			jc = (JComponent) jvp.getView();
		}
		return jc;
	}

	/**
	 * Adiciona el componente de entrada a la lista de componentes. si el componente es instancia de
	 * <code>JTextField</code>, establece los verificadores de entrada y los xxxListener() correspondientes.
	 * 
	 * para todos los componentes, este metodo utiliza el nombre del componente para localizar el identificador en el
	 * resourceBundle y con el construir una Instancia de JLabel que podria ser utilizada para apompañarlo.
	 * 
	 * Los parametro req, y ena, completaran el entorno
	 * 
	 * @param cnam - nombre para el registro
	 * @param cmp componente de entrada
	 * @param req = <code>true</code>, el componente es de entrada obligatoria. este paremetro es ignorado para
	 *        componentes que no sean instancias de JTextField
	 * @param ena = <code>true</code>, si el componente debe estar habilidado o no.
	 */
	protected void addInputComponent(String cnam, JComponent cmp, boolean req, boolean ena) {
		cmp.setEnabled(ena);
		name_component.put(cnam, cmp);
		name_jlabel.put(cnam, TUIUtils.getJLabel(cnam, req, ena));
		component_isRequired.put(getInternal(cmp), new Boolean(req));
		setInputVerifier(cnam, new TInputVerifier(this, req));
		// 180212: lazy implemetation of filechooser
		if (cmp instanceof TWebFileChooserField) {
			this.chooserField = (TWebFileChooserField) cmp;
			chooserField.addSelectedFilesListener(this);
		}
	}

	/**
	 * Enable/Disable the pair addeded using
	 * {@link AbstractDataInput#addInputComponent(String, JComponent, boolean, boolean)},
	 * {@link AbstractDataInput#addInputComponent(String, JComponent, boolean, boolean, long, long)}.
	 * <p>
	 * NOTE: enable or disable this component can be affected by field validations on user interaction.
	 * 
	 * @param cnam - componet name
	 * @param ena - enabled/disabled public void setEnableInputComponent(String cnam, boolean ena) { ((JComponent)
	 *        name_component.get(cnam)).setEnabled(ena); ((JComponent) name_jlabel.get(cnam)).setEnabled(ena); }
	 */

	/**
	 * igual que <code>addInputComponent</code> pero adiciona validaciones adicionales para componentes numericos. Ver
	 * <code>TextFieldVerifier</code> para conbinacion de valores
	 * 
	 * @param cnam - nombre para el registro
	 * @param cmp - componente de entrada
	 * @param req = <code>true</code>, el componente es de entrada obligatoria. este paremetro es ignorado para
	 *        componentes que no sean instancias de JTextField
	 * @param ena - habilitado / inhabilitado
	 * @param lr - valor numerico inferior
	 * @param hr - valor numerico superior
	 */
	protected void addInputComponent(String cnam, JComponent cmp, boolean req, boolean ena, long lr, long hr) {
		addInputComponent(cnam, cmp, req, ena);
		JComponent jcmp = getInternal(cmp);
		if ((jcmp instanceof JTextComponent)) {
			JTextComponent jtec = (JTextComponent) jcmp;
			jtec.setInputVerifier(new TInputVerifier(this, req, lr, hr));
		}
	}

	/**
	 * Inicia la validacion estandar de datos. Cualquier error encontrado durante esta secuencia de validacion,
	 * presentara el mensaje e inhabilitara el boton marcado como {@link TConstants#DEFAULT_BUTTON}
	 * <ol>
	 * <li>Toda instancia de {@link JComboBox} debe contener elementos
	 * <li>Campos de entrada obligatoria.
	 * <li>Componentes de fecha/hora
	 * <li>Instancias de {@link ExtendedJLabel} marcados como obligatorios.
	 * <p>
	 * Si todas las validaciones has sido superadas, se llama a {@link #validate()}
	 * 
	 * @param src - Objecto origen del evento que inicio la prevalidacion. puede ser null
	 * 
	 */
	public void preValidate(Object src) {
		showAplicationException(null);
		setEnableDefaultButton(false);

		// verifica comboboxes. si alguno no contiene elementos, no contiua
		checkComboBoxes();
		if (isShowingError()) {
			return;
		}
		// campos de entrada obligatoria (no presenta mensaje, solo inhanbilita boton)
		boolean all = checkInputVerifier();
		if (!all) {
			return;
		}
		// campos fecha
		checkDateFields();
		if (isShowingError()) {
			return;
		}
		// ExtendedJLabel de presencia obligatoria
		checkExtendedJLabel();
		if (isShowingError()) {
			return;
		}
		// 180212: lazy implemetation of filechooser
		if (chooserField != null && chooserField.isEnabled()) {
			boolean req = ((Boolean) component_isRequired.get(chooserField)).booleanValue();
			String sf = chooserField.getSelectedFile();
			if (req && sf == null) {
				showAplicationExceptionMsg("ui.msg22");
				return;
			}
		}

		// todos los pasos ok, habilitar default button
		setEnableDefaultButton(true);

		// Invoco validaciones adicionales
		if (!isShowingError()) {
			validateFields();
		}
	}

	/**
	 * verifica los componentes que sean instancia de <code>ExtendedJLabel</code> y que sean obligatorios tengan algun
	 * valor. si no es cierto, se presenta un error
	 * 
	 */
	private void checkExtendedJLabel() {
		Enumeration en = name_component.keys();
		while (en.hasMoreElements()) {
			JComponent icm = (JComponent) name_component.get((String) en.nextElement());
			JComponent icmp = getInternal(icm);
			boolean req = ((Boolean) component_isRequired.get(icmp)).booleanValue();
			if (icmp instanceof ExtendedJLabel && req) {
				if (!((ExtendedJLabel) icmp).isValueSet()) {
					showAplicationException(msg17);
				}
			}
		}
	}

	/**
	 * verifica instancias de <code>DateTimeSpinner</code> obligatorias y que se encuentran habilitadas
	 * 
	 * TODO: implementadas solo para fechas
	 * 
	 */
	private void checkDateFields() {
		Enumeration en = name_component.keys();
		while (en.hasMoreElements()) {
			JComponent icm = (JComponent) name_component.get((String) en.nextElement());
			if (icm instanceof DateTimeSpinner && icm.isEnabled()) {
				DateTimeSpinner ttf = (DateTimeSpinner) icm;
				ttf.resetColor();
				boolean req = ((Boolean) component_isRequired.get(icm)).booleanValue();
				Date d = TStringUtils.ZERODATE;
				try {
					// System.out.println(ttf.dateFormat.toLocalizedPattern());
					d = ttf.dateFormat.parse(ttf.getTextField().getText());
				} catch (Exception e) {
					// e.printStackTrace();
					// ya d esta como zerodate
				}
				if (d.equals(TStringUtils.ZERODATE) && req) {
					showAplicationException(msg18);
					ttf.setErrorColor(msg18.getErrorColor());
				}
			}
			if (icm instanceof WebDateField && icm.isEnabled()) {
				WebDateField wdf = (WebDateField) icm;
				Color bgc = UIManager.getColor("TextField.background");
				wdf.setBackground(bgc);
				String ds = wdf.getText();
				boolean req = ((Boolean) component_isRequired.get(icm)).booleanValue();
				boolean de = false;
				try {
					wdf.getDateFormat().parse(ds);
				} catch (Exception e) {
					de = true;
				}
				// requerired and parse errror or parse error and not blank
				if ((req && de) || (de && !ds.equals(""))) {
					showAplicationException(msg18);
					wdf.setBackground(msg18.getErrorColor());
				}
			}
		}
	}

	/**
	 * metodo practico para establecer barra de acciones con botones aceptar y cancelar
	 * 
	 */
	protected void setDefaultActionBar() {
		setActionBar(new AbstractAction[]{new AceptAction(this), new CancelAction(this)});
	}

	/**
	 * Validacion de campos de entrada de registro. Sub clases deben implementar este metodo para validar la situacion
	 * de los componentes de entrada y su relacion, presentar los mensajes pertinentes y realizar los cambios dentro del
	 * GUI para asegurar que el usuario coloque los valores conrrectos en los componentes correctos.
	 * <p>
	 * Este metodo es invocado por {@link #preValidate(Object)} SOLO cuando las validaciones standar de valores y
	 * componentes ya no presentan errores. Clases que implementan este metodo, debe controlar el estado de boton por
	 * defecto. Implementacion por defecto vacia
	 * 
	 */
	public void validateFields() {

	}

	/**
	 * set the enabled staus for the {@link TConstants#DEFAULT_BUTTON} client property. if this panel has parent
	 * UIComponentPanel, set the status for the parent component.
	 * 
	 * @param en - the enable status
	 */
	protected void setEnableDefaultButton(boolean en) {
		JButton djb = (JButton) getClientProperty(TConstants.DEFAULT_BUTTON);
		if (parentUiComponentPanel != null) {
			djb = (JButton) parentUiComponentPanel.getClientProperty(TConstants.DEFAULT_BUTTON);
		}
		if (djb != null) {
			djb.setEnabled(en);
		}
	}

	/**
	 * check if the button setted as {@link TConstants#DEFAULT_BUTTON} is enabled or not
	 * 
	 * 170913: some times another button need mimic default_button status
	 * 
	 * @return <code>true or false</code>
	 */
	public boolean isDefaultButtonEnabled() {
		boolean ena = false;
		JButton djb = (JButton) getClientProperty(TConstants.DEFAULT_BUTTON);
		if (djb != null) {
			ena = djb.isEnabled();
		}
		return ena;
	}

	/**
	 * este metodo establece (o remueve) los verificadores de entrada para los componentes instancias de
	 * <code>JTextComponent</code> registrados con el nombre <code>nam</code>no existe componente registrado con ese
	 * nombre o este no es un componente de texto, nada ocurre
	 * 
	 * @param nam - nombre del componente registrado
	 * @param req - true si el componente es requerido o no
	 */
	protected void setInputVerifier(String nam, TInputVerifier tfv) {
		JComponent jc = (JComponent) name_component.get(nam);
		if (jc != null) {
			JComponent jcmp = getInternal(jc);
			if ((jcmp instanceof JTextComponent)) {
				JTextComponent jtec = (JTextComponent) jcmp;
				jtec.setInputVerifier(tfv);
				jtec.addFocusListener(this);
				jtec.getDocument().addDocumentListener(this);
			}
			if ((jcmp instanceof DateTimeSpinner)) {

				DateTimeSpinner ttf = (DateTimeSpinner) jcmp;
				// JSpinner.DateEditor jce = (JSpinner.DateEditor) ttf.getEditor();
				// ttf.setInputVerfier(this);
				ttf.getTextField().addFocusListener(this);
			}
		}
	}
}
