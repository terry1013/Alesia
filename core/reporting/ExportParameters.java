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
package core.reporting;

import gui.*;
import gui.wlaf.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import action.*;

import com.alee.extended.layout.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import core.datasource.*;

/**
 * panel de entrada para la seleccion de opciones para exportar tablas de base de datos. clases interezadas en los
 * valores seleccionados en los distintos componentes debe usar <code>getFields()</code>. utilice las constantes
 * definidas dentro de esta clase para localizar el valor para los componentes seleccionados. los posibles valores
 * tambien estan definidos aqui. expeto para <code>FILE_FORMAT y FILE_NAME</code>
 * 
 */
public class ExportParameters extends AbstractDataInput {

	public static final String FILE_FORMAT = "export.fileopt";
	public static final String FILE_NAME = "export.outfile";
	public static final String EXPORT_HEADER = "export.header";
	public static final String FIELD_LIST = "export.fieldList";
	public static final String SERVICE_REQUEST = "serviceRequest";
	public static final String DECIMAL_SEPARATOR = "export.decimal.separator";
	public static final String DATE_FORMAT = "export.dateformat";

	private JRadioButton[] outFmt;
	private Hashtable hmap;
	private Exportable supplier;
	private JCheckBox[] fieldList;
	private String noFieldList;
	private ServiceRequest request;
	private JPanel binaryExcelPanel;
	// private Properties myProperties;

	public ExportParameters(Exportable s, String nfld) {
		super("title_exportTofile");
		this.supplier = s;
		this.noFieldList = nfld;
		setActionBar(new AceptAction(this), new CancelAction(this));
		add(getAssembledComponents());

		preValidate(null);
	}

	@Override
	public Hashtable getFields() {
		this.hmap = super.getFields();
		hmap.put(SERVICE_REQUEST, request);
		// file format
		for (int k = 0; k < outFmt.length; k++) {
			if (outFmt[k].isSelected()) {
				hmap.put(FILE_FORMAT, outFmt[k].getName());
			}
		}

		// fieldList
		String flds = "";
		for (int k = 0; k < fieldList.length; k++) {
			if (fieldList[k].isSelected()) {
				flds += fieldList[k].getName() + ";";
			}
		}
		hmap.put(FIELD_LIST, flds.substring(0, flds.length() - 1));

		return hmap;
	}

	@Override
	public void validateFields() {
		showAplicationExceptionMsg(null);
		// seleccion de campos: al menos 1
		boolean alo = false;
		for (int k = 0; k < fieldList.length; k++) {
			if (fieldList[k].isSelected()) {
				alo = true;
			}
		}
		if (alo == false) {
			showAplicationExceptionMsg("outfile.msg07");
			return;
		}
	}

	/**
	 * Create and return all panels assembled in one
	 * 
	 * @return JPanel
	 */
	private JPanel getAssembledComponents() {
		FormLayout lay = new FormLayout("fill:pref, 7dlu, fill:130dlu", // columns
				"pref, 7dlu, pref, 7dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getGeneralOptionsPanel(), cc.xy(1, 1));
		build.add(getOutputOptionsPanel(), cc.xy(1, 3));
		build.add(getFieldsSelectionPanel(), cc.xywh(3, 1, 1, 3));

		JPanel jp = build.getPanel();
		return jp;
	}

	private JPanel getNodePatternEditorPanel() {
		// at this point, a have the data, only need the samples
		ServiceResponse resp = ServiceConnection.sendTransaction(request);
		Vector<Record> rcds = (Vector) resp.getData();
		Vector<Record> buffer = new Vector<Record>();
		int deep = 0;
		for (Record rcd : rcds) {
			int pl = ((String) rcd.getFieldValue("av_path")).split("[/]").length;
			if (pl > deep) {
				buffer.add(rcd);
				deep = pl;
			}
		}

		JPanel jpleft = new JPanel(new GridLayout(buffer.size(), 1, 4, 4));
		JPanel jpright = new JPanel(new GridLayout(buffer.size(), 1, 4, 4));
		for (Record rcd : buffer) {
			String node = (String) rcd.getFieldValue("av_src_file");
			addInputComponent(node, TUIUtils.getJTextField(rcd, "av_pattern"), false, true);
			getLabelFor(node).setIcon(TResources.getSmallIcon(node));
			jpleft.add(getLabelFor(node));
			jpright.add(getInputComponent(node));
		}

		binaryExcelPanel = new JPanel(new BorderLayout(4, 4));
		binaryExcelPanel.add(jpleft, BorderLayout.WEST);
		binaryExcelPanel.add(jpright, BorderLayout.CENTER);
		binaryExcelPanel.setBorder(new TitledBorder(TStringUtils.getString("export.binaryexcel")));
		return binaryExcelPanel;
	}
	/**
	 * Create and return the components for selecctiond of fields to export
	 * 
	 * @return JPanel
	 */
	private JPanel getFieldsSelectionPanel() {
		Record m = null;
		request = supplier.getServiceRequest();
		Hashtable<String, String> fldstxt = TStringUtils.getFieldsDescriptions(request);
		if (request.getName().equals(ServiceRequest.DB_QUERY)) {
			m = ConnectionManager.getAccessTo(request.getTableName()).getModel();
		}
		if (request.getName().equals(ServiceRequest.CLIENT_GENERATED_LIST)) {
			m = (Record) request.getParameter(ServiceResponse.RECORD_MODEL);
		}
		// if no detected recordmodel, throw an execption
		if (m == null) {
			throw new IllegalArgumentException("Record model can't be retrived form Exportable.getServiceRequet()");
		}
		int fl = m.getFieldCount() - (noFieldList.equals("") ? 0 : noFieldList.split(";").length);
		this.fieldList = new JCheckBox[fl];
		Box vb2 = Box.createVerticalBox();
		Color cl = UIManager.getColor("List.background");
		int j = 0;
		for (int i = 0; i < m.getFieldCount(); i++) {
			String fn = m.getFieldName(i);
			// si no esta en la lista de exclucion, adiciono
			if (!noFieldList.contains(fn)) {
				// text form table or bundle
				String txt = fldstxt.get(fn);
				fieldList[j] = new JCheckBox(txt);
				TUIUtils.setToolTip("tt" + fn, fieldList[j]);
				fieldList[j].addActionListener(this);
				fieldList[j].setBackground(cl);
				vb2.add(fieldList[j]);
				fieldList[j].setName(fn);
				fieldList[j].setSelected(true);
				j++;
			}
		}
		JPanel fldPanel = new JPanel(new BorderLayout());
		JScrollPane sp = new JScrollPane(vb2);
		sp.getViewport().setBackground(cl);
		fldPanel.add(sp, BorderLayout.CENTER);
		fldPanel.setBorder(new TitledBorder(TStringUtils.getString("export.fieldlist")));
		return fldPanel;
	}

	/**
	 * Create and return the components of general options for export
	 * 
	 * @return JPanel
	 */
	private JPanel getGeneralOptionsPanel() {
		JComboBox jcb = TUIUtils.getJComboBox("tt" + EXPORT_HEADER, TStringUtils.getTEntryGroup(EXPORT_HEADER), "");
		addInputComponent(EXPORT_HEADER, jcb, false, true);
		jcb = TUIUtils.getJComboBox("tt" + DATE_FORMAT, TStringUtils.getTEntryGroup(DATE_FORMAT), "");
		addInputComponent(DATE_FORMAT, jcb, false, true);
		jcb = TUIUtils.getJComboBox("tt" + DECIMAL_SEPARATOR, TStringUtils.getTEntryGroup(DECIMAL_SEPARATOR), "");
		addInputComponent(DECIMAL_SEPARATOR, jcb, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref", // columns
				"pref, 3dlu, pref, 3dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor(EXPORT_HEADER), cc.xy(1, 1));
		build.add(getInputComponent(EXPORT_HEADER), cc.xy(3, 1));
		// 180212: not implemented yet
		// build.add(getLabelFor(DATE_FORMAT), cc.xy(1, 3));
		// build.add(getInputComponent(DATE_FORMAT), cc.xy(3, 3));
		// build.add(getLabelFor(DECIMAL_SEPARATOR), cc.xy(1, 5));
		// build.add(getInputComponent(DECIMAL_SEPARATOR), cc.xy(3, 5));
		JPanel optPanel = build.getPanel();
		optPanel.setBorder(new TitledBorder(TStringUtils.getString("export.generalopt")));
		return optPanel;
	}

	/**
	 * Create and return the components for output format
	 * 
	 * @return JPanel
	 */
	private JPanel getOutputOptionsPanel() {
		TEntry[] expf = TStringUtils.getTEntryGroup("export.outfmt");
		this.outFmt = new JRadioButton[expf.length];
		Box vb1 = Box.createVerticalBox();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < expf.length; i++) {
			String k = (String) expf[i].getKey();
			outFmt[i] = new JRadioButton((String) expf[i].getValue());
			TUIUtils.setToolTip("ttexport." + k, outFmt[i]);
			bg.add(outFmt[i]);
			vb1.add(outFmt[i]);
			outFmt[i].setName(k);

			// custom behavior for .xlsx button:
			// - disabled for all except for amoutviewer
			if (k.equals(".xlsx")) {
				// outFmt[i].setEnabled(false);
			}
		}
		outFmt[0].setSelected(true);

		TWebFileChooserField wfc = TUIUtils.getWebFileChooserField("ttexport.outfile", null);
		addInputComponent(FILE_NAME, wfc, true, true);

		JPanel expPanel = new JPanel(new VerticalFlowLayout());
		expPanel.add(vb1);
		expPanel.add(new JLabel(" "));
		expPanel.add(getLabelFor(FILE_NAME));
		expPanel.add(wfc);
		expPanel.setBorder(new TitledBorder(TStringUtils.getString("export.fileopt")));
		return expPanel;
	}
}
