package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.xml.soap.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.extended.date.*;
import com.alee.extended.filechooser.*;
import com.jgoodies.common.base.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.html.*;
import gui.wlaf.*;

/**
 * notes: the method {@link #getValue(String)} and {@link #getValues()} method only will return the values for
 * components inserted in this calss using {@link #addInputComponent(JComponent, boolean, boolean)} methods. this allow
 * sublcass manualy append more ui component that are no related whit this form.
 * 
 * @author terry
 *
 */
public class TUIFormPanel extends TUIPanel implements DocumentListener, FilesSelectionListener {

	private TError msg12, msg17, msg18;
	private DefaultFormBuilder formBuilder;
	private Hashtable<String, JComponent> fields;
	private Vector<TError> reasons;
	private Model model;
	private Hashtable<String, Object> temporalStorage;

	public TUIFormPanel() {
		FormLayout fl = new FormLayout("right:pref, 3dlu, default:grow", "");
		this.fields = new Hashtable<String, JComponent>();
		this.temporalStorage = new Hashtable<>();
		this.reasons = new Vector<>();
		// TODO: set the mair resourcebuldle to this panel
		this.formBuilder = new DefaultFormBuilder(fl);
		formBuilder.border(Borders.DIALOG);
		this.msg12 = new TError("ui.msg12");
		this.msg17 = new TError("ui.msg17");
		this.msg18 = new TError("ui.msg18");
	}

	@org.jdesktop.application.Action
	public void acept() {
		if (validateFields()) {
			System.out.println("TUIFormPanel.acept()");
		}
	}
	/**
	 * metodo que verifica si el componente pasado como argumento es una instacia de
	 * <code>JScrollPane o SearchTextField</code> Si lo es, altera el argumento para que sea el componente interno del
	 * mismo o el que realmente contiene los datos
	 * 
	 * @param jcmp - componente a alterar
	 */
	private static JComponent getInternal(JComponent jcmp) {
		JComponent jc = jcmp;
		if (jcmp instanceof JScrollPane) {
			JViewport jvp = (JViewport) ((JScrollPane) jcmp).getViewport();
			jc = (JComponent) jvp.getView();
		}
		return jc;
	}

	public void append(String rid, JComponent jcmp) {
		this.fields.put(rid, jcmp);
		formBuilder.append(rid, jcmp);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	/**
	 * verifica instancias de <code>JComboBox</code>buscando si algunos de estos componentes no contiene elementos. si
	 * esto es cierto, este metodo guarda la rason del error y provee feedback color para el componente asociado.
	 * <p>
	 * esta clase asume que toda instancia de {@link JComboBox} debe tener al menos 1 elemento (puede ser elementos
	 * especiales con ninguno)
	 */
	public void checkComboBoxes() {
		Vector<JComponent> jcmplist = new Vector(fields.values());
		for (JComponent jcmp : jcmplist) {
			JComponent icmp = getInternal(jcmp);
			if (icmp instanceof JComboBox) {
				JComboBox jcb = (JComboBox) icmp;
				if (jcb.getItemCount() == 0) {
					reasons.add(msg12);
					// error grave. no es necesario guardar el color original
					jcb.setBackground(msg12.getExceptionColor());
				}
			}
		}
	}

	/**
	 * invoca <code>TextFieldVerifier.verify()</code> para los componentes de entrada instancias de
	 * <code>JTextComponet</code>. Este metodo solo verificara los componentes que esten habilitados pero si se detecta
	 * algun error, provee feedback para los campos con errores
	 * 
	 */
	public void checkInputVerifier() {
		// TODO: Temporal.
		Vector<JComponent> jcmplist = new Vector(fields.values());
		for (JComponent jcmp : jcmplist) {
			JComponent icmp = getInternal(jcmp);
			boolean req = (Boolean) icmp.getClientProperty("isRequired");
			if (icmp.isEnabled() && icmp instanceof JTextComponent && req) {
				JTextComponent jtc = (JTextComponent) icmp;
				String txt = jtc.getText();
				if (txt == null || txt.trim().equals("")) {
					reasons.add(new TError("msg01"));
					jtc.setBackground(Color.red);
				}
			}
		}

		// original code
		// Vector<JComponent> jcmplist = new Vector(fields.values());
		// for (JComponent jcmp : jcmplist) {
		// JComponent icmp = getInternal(jcmp);
		// if (icmp.isEnabled() && icmp.getInputVerifier() != null) {
		// TInputVerifier ver = (TInputVerifier) icmp.getInputVerifier();
		// if (!ver.verify(icmp)) {
		// reasons.add(ver.getReason());
		// }
		// }
		// }
	}

	/**
	 * retorna el componente de entrada registrado con el nombre del argumento
	 * 
	 * @param field - nombre del argumento
	 * @return componente
	 */
	public JComponent getInputComponent(String field) {
		JComponent jcmp = fields.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %1$ not found.", field);
		return jcmp;
	}

	/**
	 * retorna la etiqueta designada para el componente cuyo identificador fue pasado como argumento.
	 * 
	 * @param na - nombre del componente
	 * @return - etiqueta
	 */
	public JLabel getLabel(String field) {
		JComponent jcmp = fields.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %1$ not found.", field);
		return (JLabel) jcmp.getClientProperty("myLabel");
	}

	/**
	 * return the internal fields. This list is for store purpose. Some implementation of this class needs to store
	 * parameters that has no visual component associated with, but will be needed futher.
	 * 
	 * @see #getValues()
	 */
	public Hashtable<String, Object> getTemporalStorage() {
		return temporalStorage;
	}

	/**
	 * set the {@link Model} for this component if this componet will be used as Data base CRUD operations
	 * 
	 * @param model - the model
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	/**
	 * return the instance of the {@link Model} setted whit all values introduced as input in this compoment. The model
	 * must be setted initialy using the method {@link #setModel(Model)}. All field values will be copied as are in the
	 * input window. Only attributes present in the input window will be copyed to the model.
	 * 
	 * @return the Model with all attributes set.
	 */
	public Model getModel() {
		if (model == null)
			return null;
		Hashtable<String, Object> vals = getValues();
		model.fromMap(vals);
		return model;
	}

	/**
	 * Return a new {@link Hashtable} with all values setted by this GUI. this method also will return all stored
	 * parameters in the temporal storage buffer for this class
	 * 
	 * @return Hashtable with fields name and values found in this UI
	 * @see #getTemporalStorage()
	 */
	public Hashtable<String, Object> getValues() {
		Hashtable h = new Hashtable(temporalStorage);
		fields.keySet().stream().forEach((key) -> {
			Object val = getValue(key);
			if (val != null)
				h.put(key, val);
		});
		return h;
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		preValidate();
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
	public void preValidate() {
		reasons.clear();

		setEnableActions("isCommint", "true", false);
		// verifica comboboxes. si alguno no contiene elementos, no contiua
		checkComboBoxes();
		if (!reasons.isEmpty()) {
			return;
		}
		// campos de texto
		checkInputVerifier();
		if (!reasons.isEmpty()) {
			return;
		}

		// campos fecha
		checkDateFields();
		if (!reasons.isEmpty()) {
			return;
		}

		// ExtendedJLabel de presencia obligatoria
		checkExtendedJLabel();
		if (!reasons.isEmpty()) {
			return;
		}

		// 180212: lazy implemetation of filechooser
		// TODO: complete implementation. code copied from oll framework

		// if (chooserField != null && chooserField.isEnabled()) {
		// boolean req = ((Boolean) component_isRequired.get(chooserField)).booleanValue();
		// String sf = chooserField.getSelectedFile();
		// if (req && sf == null) {
		// showAplicationExceptionMsg("ui.msg22");
		// return;
		// }
		// }

		// todos los pasos ok, habilitar default button
		setEnableActions("isCommint", "true", true);
	}

	/**
	 * this metod is invoqued by any default save action previous to continue normal operation. use this method to
	 * perform aditional UI validation. if this method return <code>false</code>, the action will not continue the
	 * normal flow of operations and all
	 * 
	 * @return
	 */
	public boolean validateFields() {
		return true;
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		preValidate();
	}

	@Override
	public void selectionChanged(List<File> files) {
		// TODO check implementation
		preValidate();

	}

	public void setAceptAction(Action acept) {
		putClientProperty("aceptAction", acept);
	}
	/**
	 * the the enable/disable status for the couple {@link JLabel} and {@link JComponent} asociated whit this field
	 * name.
	 * 
	 * @param field - field name of the input component
	 * @param enable - enable/disable value
	 */
	public void setEnable(String field, boolean enable) {
		JComponent jcmp = fields.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %1$ not found.", field);
		JLabel jl = (JLabel) jcmp.getClientProperty("myLabel");
		jl.setEnabled(enable);
		jcmp.setEnabled(enable);
	}

	/**
	 * Enable commint actions. Commit action are actions with the property <code>.isCommint = true</code> as property
	 * (for example, see Acept)
	 * 
	 * @param enable - true of false for enable/disable action public void setEnabledCommintActions(boolean enable) {
	 *        for (Action a : allActions) { ApplicationAction aa = (ApplicationAction) a; String isc =
	 *        aa.getResourceMap().getString(aa.getName() + ".Action.isCommint"); if (isc != null && isc.equals("true"))
	 *        { aa.setEnabled(enable); } } // actions.stream().filter((a) ->
	 *        a.getValue("isCommint").equals("true")).forEach(a -> a.setEnabled(enable)); }
	 */

	/**
	 * Enable/disable <code>Action.scope = element</code> actions. "element" actions are action than act over an element
	 * of a list. (for example, editModelAction) those action must be enabled/disables if the user select or not an
	 * elemento form the list.
	 * 
	 * @param enable - true of false for enable/disable action public void setEnabledElementActions(boolean enable) {
	 *        setEnableActions("scope", "element", enable); }
	 */

	/**
	 * este metodo establece (o remueve) los verificadores de entrada para los componentes instancias de
	 * <code>JTextComponent</code> registrados con el nombre <code>nam</code>no existe componente registrado con ese
	 * nombre o este no es un componente de texto, nada ocurre
	 * 
	 * @param nam - nombre del componente registrado
	 * @param req - true si el componente es requerido o no
	 */
	public void setInputVerifier(String field, TInputVerifier verifier) {
		JComponent jc = fields.get(field);
		Preconditions.checkNotNull(jc, "The component asociated with %1$ was not found.", field);
		JComponent jcmp = getInternal(jc);
		if ((jcmp instanceof JTextComponent)) {
			JTextComponent jtec = (JTextComponent) jcmp;
			// TODO:
			// jtec.setInputVerifier(verifier);
			// jtec.addFocusListener(this);
			jtec.getDocument().addDocumentListener(this);
		}
		if ((jcmp instanceof DateTimeSpinner)) {
			DateTimeSpinner ttf = (DateTimeSpinner) jcmp;
			// JSpinner.DateEditor jce = (JSpinner.DateEditor) ttf.getEditor();
			// ttf.setInputVerfier(this);
			ttf.getTextField().getDocument().addDocumentListener(this);
			// ttf.getTextField().addFocusListener(this);
		}
	}
	/**
	 * verifica instancias de <code>DateTimeSpinner</code> obligatorias y que se encuentran habilitadas
	 * 
	 * TODO: metodo copiado del viejo framework. Verificar y completar implementacion
	 * 
	 */
	private void checkDateFields() {
		Vector<JComponent> jcmplist = new Vector(fields.values());
		for (JComponent jcmp : jcmplist) {
			if (jcmp instanceof DateTimeSpinner && jcmp.isEnabled()) {
				DateTimeSpinner ttf = (DateTimeSpinner) jcmp;
				ttf.resetColor();
				boolean req = (Boolean) jcmp.getClientProperty("isRequired");
				Date d = TStringUtils.ZERODATE;
				try {
					// System.out.println(ttf.dateFormat.toLocalizedPattern());
					d = ttf.dateFormat.parse(ttf.getTextField().getText());
				} catch (Exception e) {
					// e.printStackTrace();
					// ya d esta como zerodate
				}
				if (d.equals(TStringUtils.ZERODATE) && req) {
					reasons.add(msg18);
					ttf.setErrorColor(msg18.getExceptionColor());
				}
			}
			if (jcmp instanceof WebDateField && jcmp.isEnabled()) {
				WebDateField wdf = (WebDateField) jcmp;
				Color bgc = UIManager.getColor("TextField.background");
				wdf.setBackground(bgc);
				Date dat = wdf.getDate();
				boolean req = (Boolean) jcmp.getClientProperty("isRequired");
				boolean de = false;
				// requerired and parse errror or parse error and not blank
				// if ((req && de) || (de && !ds.equals(""))) {
				// showAplicationException(msg18);
				// wdf.setBackground(msg18.getErrorColor());
				// }
			}
		}
	}

	/**
	 * verifica los componentes que sean instancia de <code>ExtendedJLabel</code> y que sean obligatorios tengan algun
	 * valor. si no es cierto, se presenta un error
	 * 
	 */
	private void checkExtendedJLabel() {
		// Vector<JComponent> jcmplist = new Vector(fields.values());
		// for (JComponent jcmp : jcmplist) {
		// JComponent icmp = getInternal(jcmp);
		// boolean req = (Boolean) jcmp.getClientProperty("isRequired");
		// if (icmp instanceof ExtendedJLabel && req) {
		// if (!((ExtendedJLabel) icmp).isValueSet()) {
		// reasons.add(msg17);
		// }
		// }
		// }
	}

	/**
	 * return the internal value stored in the {@link JComponent} asociated whit this field id. This method assume the
	 * standar Object <--> Component relation. for example:
	 * <ul>
	 * <li>Number <--> {@link JFormattedTextField}
	 * <li>Boolean <--> {@link JCheckBox} or {@link JRadioButton}
	 * <li>String <--> {@link JTextArea} or {@link JTextField}
	 * </ul>
	 * 
	 * @param field - internal field id
	 * @return value
	 */
	private Object getValue(String field) {
		JComponent jcmp = fields.get(field);

		if (jcmp instanceof JScrollPane) {
			JViewport jvp = (JViewport) ((JScrollPane) jcmp).getViewport();
			jcmp = (JComponent) jvp.getView();
		}
		Object val = null;

		// date field
		if (jcmp instanceof WebDateField) {
			WebDateField wdf = ((WebDateField) jcmp);
			// TODO: complete implementation to supoort old zero date
			// val = (dt.equals("")) ? TStringUtils.ZERODATE : wdf.getDate();
			return wdf.getDate();
		}

		// numeros
		if (jcmp instanceof JFormattedTextField) {
			val = ((JFormattedTextField) jcmp).getValue();
			return val;
		}
		// fecha o hora
		if (jcmp instanceof DateTimeSpinner) {
			val = ((DateTimeSpinner) jcmp).getDate();
			return val;
		}
		// texto
		if (jcmp instanceof JTextComponent) {
			val = ((JTextComponent) jcmp).getText();
			return val;
		}
		// 170911: property table
		if (jcmp instanceof TPropertyJTable) {
			val = ((TPropertyJTable) jcmp).getProperties();
			return val;
		}
		// 180212: TWebFileChooserField string representation of File or *none if no file selected
		if (jcmp instanceof TWebFileChooserField) {
			String s = ((TWebFileChooserField) jcmp).getSelectedFile();
			val = s == null ? "*none" : s;
			return val;
		}
		// selector de registros
		// if (jcmp instanceof AssistedJTextField) {

		// }
		// html text
		if (jcmp instanceof HTMLEditor) {
			val = ((HTMLEditor) jcmp).getText();
			return val;
		}
		// boolean
		if (jcmp instanceof JCheckBox) {
			val = new Boolean(((JCheckBox) jcmp).isSelected());
			return val;
		}
		if (jcmp instanceof JRadioButton) {
			val = new Boolean(((JRadioButton) jcmp).isSelected());
			return val;
		}
		// campo solo de salida. si el valor es instancia de LTEntry, retorna clave
		// if (jcmp instanceof ExtendedJLabel) {
		// val = ((ExtendedJLabel) jcmp).getValue();
		// if (val instanceof TEntry) {
		// val = ((TEntry) val).getKey();
		// }
		// return val;
		// }

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
					// TEntry ae = (TEntry) ((JComboBox) jcmp).getSelectedItem();
					// val = ae.getKey();
					val = ((TEntry) obj).getKey();
				} else {
					val = obj;
				}
			}
			return val;
		}
		// Conjuntos de elementos seleccionados del ListRecordSelector
		if (jcmp instanceof RecordSelectorList) {
			val = ((RecordSelectorList) jcmp).getSelectedElement();
			return val;
		}

		// nothig found
		throw new NullPointerException("No value fount for field " + field);
	}

	protected void addInputComponent(JComponent cmp, boolean required, boolean enable) {
		String name = cmp.getName();
		Preconditions.checkNotNull(name, "the component name can't be null");
		addInputComponent(name, cmp, required, enable);
	}

	/**
	 * add the input field for this {@link TUIFormPanel} instace. this component don.t set the name of the component.
	 * so, the component will not use {@link ResourceBundle} to retrive i18n strings.
	 * 
	 * @param field
	 * @param cmp
	 * @param required
	 * @param enable
	 */
	protected void addInputComponent(String field, JComponent cmp, boolean required, boolean enable) {
		fields.put(field, cmp);
		JLabel jl = TUIUtils.getJLabel(field, required, enable);
		cmp.putClientProperty("myLabel", jl);
		cmp.putClientProperty("isRequired", required);
		setEnable(field, enable);
		setInputVerifier(field, new TInputVerifier(required));
		if (cmp instanceof TWebFileChooserField) {
			TWebFileChooserField fc = (TWebFileChooserField) cmp;
			fc.addSelectedFilesListener(this);
		}
	}
}
