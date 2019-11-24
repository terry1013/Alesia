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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.sf.jasperreports.engine.*;
import action.*;

import com.alee.extended.layout.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;

/**
 * this pane display all default parameters required to load, fill and print a {@link JasperReport}.
 * <p>
 * Any action interesting in generate a report must use this class to obtain mimimun report generation parameters. if
 * more parameters are needed use the {@link #insert(AbstractDataInput, String)} method.
 * <p>
 * The <code>rn</code> argument in the constructor MUST be equal to the .jasper file name. this field value is stored in
 * {@link #REPORT_NAME} parameter and is used to locate and load the binary jasper file in the resource folder
 * 
 * 
 * @author terry
 * 
 */
public class ReportParameters extends AbstractDataInput {

	public static final String FILE_FORMAT = "FileFormat";
	public static final String FILE_NAME = "FileName";

	// tipos de salida
	public static final String OUT_TYPE = "OutType";
	public static final String PRINTER = "Printer";
	public static final String WINDOW = "Windows";
	public static final String EXPORT = "Export";
	public static final String REPORT_DESCRIPTION = "ReportDescription";
	public static final String REPORT_NAME = "ReportName";
	public static final String REPORT_GENERATE_ONLY = "ReportGenerateOnly";
	public static final String REPORT_PROPERTY_LISTENER = "propertyListener";

	private JComponent jc_outType;
	// private WebFileChooserField fileChooserField;
	private JRadioButton out_print, out_window, out_file;
	private JRadioButton[] outFmt;
	private String reportName;
	private JTabbedPane tabbedPane;
	private Hashtable fields;
	private SelectPrinterAction printers;
	private Hashtable<String, AbstractDataInput> aditionalADI;
	private TWebFileChooserField fileChooserField;

	public ReportParameters(String rn) {
		super("title_print");
		this.reportName = rn;
		// saved parameters or new black hashtable
		this.fields = (Hashtable<String, Object>) TPreferences.getPreference(TPreferences.PRINT_PARAMETERS, reportName,
				new Hashtable<String, Object>());
		this.jc_outType = outTypeContainer();
		this.tabbedPane = new JTabbedPane();
		this.aditionalADI = new Hashtable<String, AbstractDataInput>();
		tabbedPane.add(TStringUtils.getString("print.outtype"), jc_outType);
		printers = new SelectPrinterAction(this);
		printers.setEnabled(false);
		setActionBar(new AbstractAction[]{new SaveAction(this), printers, new AceptAction(this), new CancelAction(this)});
		add(tabbedPane);
		// setOutputType((String) getSavedParameter(OUT_TYPE, WINDOW));
		setOutputType((String) getSavedParameter(OUT_TYPE, EXPORT));
		preValidate(null);
	}

	@Override
	public Hashtable getFields() {
		this.fields = super.getFields();

		// para cada componente dentro del tabpane insertado usando insert
		int tc = tabbedPane.getTabCount();
		for (int k = 0; k < tc; k++) {
			Component cmp = tabbedPane.getComponent(k);

			// inserted AbstractDataInput
			if (cmp instanceof AbstractDataInput) {
				Hashtable th = ((AbstractDataInput) cmp).getFields();
				fields.putAll(th);
			}
		}

		fields.put(REPORT_NAME, reportName);
		fields.put(REPORT_GENERATE_ONLY, false);

		// formato de salida , out_window, out_file
		if (out_print.isSelected()) {
			fields.put(OUT_TYPE, PRINTER);
		}
		if (out_window.isSelected()) {
			fields.put(OUT_TYPE, WINDOW);
		}
		if (out_file.isSelected()) {
			fields.put(OUT_TYPE, EXPORT);
			for (int k = 0; k < outFmt.length; k++) {
				if (outFmt[k].isSelected()) {
					fields.put(FILE_FORMAT, outFmt[k].getName());
				}
			}
		}

		// target file: remove filedname and set FILE_NAME constant
		Object sf = fields.remove("print.outfilename");
		if (!sf.equals("*none")) {
			fields.put(FILE_NAME, sf);
		}

		return fields;
	}

	/**
	 * return saved parameters. if no parameter with <code>key</code> is found, return the default object
	 * 
	 * @param key - parameter id
	 * @param dft - default value if no saved property found
	 * 
	 */
	public Object getSavedParameter(String key, Object dft) {
		Object o = fields.get(key);
		return o == null ? dft : o;
	}

	/**
	 * insert an instance of {@link AbstractDataInput} as a new tab preceding all tabs added before. for each of this,
	 * it will execute the method {@link AbstractDataInput#getFields()} and the result append to the fields returned by
	 * this class.
	 * <p>
	 * Also, for the adi argument, this method invoke {@link #setParent(this)} and disable the message panel.
	 * 
	 * @param adi - instance of {@link AbstractDataInput}
	 * @param bid - id for text
	 */
	public void insert(AbstractDataInput adi, String bid) {
		String cl = adi.getClass().getSimpleName();
		if (!aditionalADI.contains(cl)) {
			aditionalADI.put(cl, adi);
			adi.setParent(this);
			adi.setVisibleMessagePanel(false);
			tabbedPane.add(adi, TStringUtils.getString(bid), 0);
			tabbedPane.setSelectedIndex(0);
		}
	}

	/**
	 * save selected parameters to persistence storage. this method save this instance parameters and all inserted
	 * instances of {@link AbstractDataInput} inserted using {@link #insert(AbstractDataInput, String)};
	 */
	public void saveParameters() {
		getFields();
		Collection<AbstractDataInput> adilist = aditionalADI.values();
		for (AbstractDataInput adi : adilist) {
			fields.putAll(adi.getFields());
		}
		TPreferences.setPreference(TPreferences.PRINT_PARAMETERS, reportName, fields);
	}

	/**
	 * set the output type for the report
	 * 
	 * @param opt - output type {@link #PRINTER}, {@link #WINDOW} or {@link #EXPORT}
	 */
	public void setOutputType(String opt) {
		if (opt.equals(PRINTER)) {
			out_print.doClick();
		}
		if (opt.equals(WINDOW)) {
			out_window.doClick();
		}
		if (opt.equals(EXPORT)) {
			out_file.doClick();
		}
	}
	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		/*
		 * if (!isShowingError()) { // archivo de salida, entonces nombre en campo if (fileChooserField.isEnabled()) {
		 * if (((ArrayList<File>) fileChooserField.getSelectedFiles()).isEmpty()) {
		 * showAplicationExceptionMsg("ui.msg22"); return; } } }
		 */
		// invoke validatefields to inserted instances
		validateFields();
	}
	@Override
	public void validateFields() {
		// does nothing. but check the inserted instances. if any error is found, suspend validation.
		Vector<AbstractDataInput> v = new Vector<AbstractDataInput>(aditionalADI.values());
		for (AbstractDataInput adi : v) {
			adi.validateFields();
			// subclass must take care of default acept button and fields status.
			if (isShowingError()) {
				return;
			}
		}
	}

	/**
	 * create a panel with all component containing all output type options
	 * 
	 * @return panel
	 */
	private JPanel outTypeContainer() {
		ButtonGroup bg = new ButtonGroup();

		// salida impresa & por ventana
		this.out_print = TUIUtils.getJRadioButton("ttprint.outprint", "print.outprint", false);
		String sot = (String) getSavedParameter(OUT_TYPE, "");
		out_print.setSelected(sot.equals(PRINTER));
		out_print.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				printers.setEnabled(out_print.isSelected());
			}
		});
		this.out_window = TUIUtils.getJRadioButton("ttprint.outwindow", "print.outwindow", false);
		out_window.setSelected(sot.equals(WINDOW));

		this.out_file = TUIUtils.getJRadioButton("ttprint.outfile", "print.outfile", false);
		out_file.setSelected(sot.equals(EXPORT));
		// para validar
		out_file.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				preValidate(null);
			}
		});

		bg.add(out_print);
		bg.add(out_window);
		bg.add(out_file);
		ComponentTitledPane ctp1 = new ComponentTitledPane(out_file, outFilePanel());

		FormLayout lay = new FormLayout("fill:200dlu", // columns
				"pref, pref, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(out_print, cc.xy(1, 1));
		// 180312: temporaly unavialable
		// build.add(out_window, cc.xy(1, 2));
		build.add(ctp1, cc.xy(1, 3));

		JPanel jp = build.getPanel();
		TUIUtils.setEmptyBorder(jp);
		return jp;
	}

	/**
	 * locate in .properties files all constant available for export a jasperreport and build a component with all those
	 * options including a component to select a target ouput file
	 * 
	 * @return panel
	 */
	private JPanel outFilePanel() {
		TEntry[] expf = TStringUtils.getTEntryGroup("print.outfmt");
		this.outFmt = new JRadioButton[expf.length];
		Box vb1 = Box.createVerticalBox();
		ButtonGroup bg = new ButtonGroup();
		String filfo = (String) getSavedParameter(FILE_FORMAT, ".pdf");
		for (int i = 0; i < expf.length; i++) {
			outFmt[i] = new JRadioButton((String) expf[i].getValue());
			bg.add(outFmt[i]);
			vb1.add(outFmt[i]);
			String na = (String) expf[i].getKey();
			outFmt[i].setName(na);
			outFmt[i].setSelected(filfo.equals(na));
			outFmt[i].addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					JRadioButton jrb = (JRadioButton) e.getSource();
					// to avoid spetial parameter .xlso
					String sf = jrb.getName().equals(".xlso") ? ".xls" : jrb.getName(); 
					fileChooserField.setSuffix(sf);
				}
			});
		}
		outFmt[0].setSelected(true);

		// archivo de salida
		String sf = (String) getSavedParameter(FILE_NAME, null);
		fileChooserField = TUIUtils.getWebFileChooserField("ttprint.outfilename", sf);
		addInputComponent("print.outfilename", fileChooserField, true, true);

		// because outFmt[0].setSelected(true); 4 lines before
		fileChooserField.setSuffix(".pdf");

		JPanel jp = new JPanel(new VerticalFlowLayout());
		jp.add(vb1);
		jp.add(new JLabel(" "));
		jp.add(getLabelFor("print.outfilename"));
		jp.add(fileChooserField);
		TUIUtils.setEnabled(jp, false);
		return jp;
	}
}
