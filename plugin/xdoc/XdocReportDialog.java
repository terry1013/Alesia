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
package plugin.xdoc;

import gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import action.*;

import com.alee.extended.filechooser.*;
import com.alee.laf.button.*;
import com.alee.utils.swing.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class XdocReportDialog extends AbstractDataInput {

	private WebFileChooserField fileChooserField;
	private Hashtable hmap;
	private JRadioButton[] docxFiles;
	private Vector<JCheckBox> docxProperties;
	private Box boxSignature;
	private SaveAsTaskAction saveAction;
	private JRadioButton out_window, out_file, out_print, out_merge;
	private Vector baseRcds, baseNames;
	private String baseFile;
	private Color bgColor;

	/**
	 * nueva instancia
	 * 
	 * @param r - registro fuente de datos de combinacion
	 */
	public XdocReportDialog() {
		super("xdoc.title01");
		// setDefaultActionBar();
		saveAction = new SaveAsTaskAction(this, "/plugin/xdoc/xdicon", XDocReportTask.class.getName());
		saveAction.setEnabled(false);
		this.bgColor = Color.WHITE;
		setActionBar(new AbstractAction[]{saveAction, new AceptAction(this), new CancelAction(this)});

		JPanel jp = new JPanel(new GridLayout(2, 2, 4, 4));
		jp.add(createOutOptionPanel());
		jp.add(createExportOptionPanel());
		jp.add(createDocsPanel());
		jp.add(createPropertiesPanel());
		add(jp);
		out_window.setSelected(true);
		// cause aditional properties panel fill with this documents properties
		if (docxFiles.length > 0) {
			docxFiles[0].doClick();
		}
		preValidate(null);
	}

	public void setBaseRecords(Record... br) {
		this.baseRcds = new Vector(br.length);
		this.baseNames = new Vector(br.length);
		this.baseFile = br[0].getTableName();
		DBAccess dba = ConnectionManager.getAccessTo(baseFile);
		for (Record r : br) {
			baseRcds.add(dba.getKey(r));
			baseNames.add(XDoc.getRecipientFieldsValue(r));
		}

		// multiple record selection only allow to "merge only" and "save to file" actions. window or print action only
		// for single record selection
		boolean ss = baseRcds.size() == 1;
		out_window.setEnabled(ss);
		out_print.setEnabled(ss);
		out_merge.setSelected(ss);
	}

	@Override
	public Hashtable getFields() {
		this.hmap = super.getFields();

		hmap.put("xdoc.baseRecords", baseRcds);

		// docx files
		for (int k = 0; k < docxFiles.length; k++) {
			if (docxFiles[k].isSelected()) {
				// format: DOC:xxxxPRPS:xx;xx;...
				String t = docxFiles[k].getName().substring(4);
				t = t.split("PRPS:")[0];
				hmap.put("xdoc.pattern.document", t);
			}
		}

		// aditional properties
		String signid = "";
		for (int k = 0; k < docxProperties.size(); k++) {
			JCheckBox jcb = docxProperties.elementAt(k);
			if (jcb.isSelected()) {
				// contain tentry
				signid += jcb.getName() + ";";
			}
		}
		// documento puede que no tenga propiedades adicionales
		if (!signid.equals("")) {
			hmap.put("xdoc.out.docs.properties", signid.substring(0, signid.length() - 1));
		} else {
			hmap.put("xdoc.out.docs.properties", signid);
		}

		// directorio destino
		ArrayList<File> al = (ArrayList<File>) fileChooserField.getSelectedFiles();
		hmap.put("xdoc.out.targetdirectory", al.isEmpty() ? "" : al.get(0).getAbsolutePath());

		return hmap;
	}

	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		saveAction.setEnabled((!isShowingError() && out_file.isSelected() && isDefaultButtonEnabled())
				|| (!isShowingError() && out_merge.isSelected()));
	}

	@Override
	public void validateFields() {
		showAplicationExceptionMsg(null);

		// debe exitir al menos un documento seleccionado
		boolean alo = false;
		if (docxFiles.length != 0) {
			for (int k = 0; k < docxFiles.length; k++) {
				if (docxFiles[k].isSelected()) {
					alo = true;
				}
			}
		}
		if (alo == false) {
			showAplicationExceptionMsg("xdoc.msg07");
			return;
		}

		// archivo de salida
		if (fileChooserField.isEnabled()) {
			if (((ArrayList<File>) fileChooserField.getSelectedFiles()).isEmpty()) {
				showAplicationExceptionMsg("xdoc.msg05");
			}
		}
	}

	private JPanel createExportOptionPanel() {
		addInputComponent("export.dateformat",
				TUIUtils.getJComboBox("ttexport.dateformat", TStringUtils.getTEntryGroup("export.dateformatt"), ""),
				false, true);
		addInputComponent(
				"export.decimal.separator",
				TUIUtils.getJComboBox("ttexport.decimal.separator",
						TStringUtils.getTEntryGroup("export.decimal.separator"), ""), false, true);

		String re = TStringUtils.getString("xdoc.default.recipient");
		addInputComponent("xdoc.recipient", TUIUtils.getJTextField("ttxdoc.recipient", re, 40), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"pref, 3dlu, pref, 3dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		build.add(getLabelFor("xdoc.recipient"), cc.xy(1, 1));
		build.add(getInputComponent("xdoc.recipient"), cc.xy(3, 1));
		build.add(getLabelFor("export.dateformat"), cc.xy(1, 3));
		build.add(getInputComponent("export.dateformat"), cc.xy(3, 3));
		build.add(getLabelFor("export.decimal.separator"), cc.xy(1, 5));
		build.add(getInputComponent("export.decimal.separator"), cc.xy(3, 5));
		JPanel optPanel = build.getPanel();
		optPanel.setBorder(new TitledBorder(TStringUtils.getString("xdoc.options")));

		return optPanel;
	}

	private JPanel createDocsPanel() {
		Vector docxs = ConnectionManager.getAccessTo("xd_config").search(null, null);
		this.docxFiles = new JRadioButton[docxs.size()];
		ButtonGroup bg = new ButtonGroup();
		Box vb2 = Box.createVerticalBox();
		for (int i = 0; i < docxs.size(); i++) {
			Record r = (Record) docxs.elementAt(i);
			docxFiles[i] = new JRadioButton((String) r.getFieldValue("xd_coname"));
			docxFiles[i].setToolTipText((String) r.getFieldValue("xd_codescription"));
			docxFiles[i].setBackground(bgColor);
			docxFiles[i].addActionListener(this);
			bg.add(docxFiles[i]);
			vb2.add(docxFiles[i]);
			docxFiles[i].setName("DOC:" + r.getFieldValue("xd_coid") + "PRPS:"
					+ r.getFieldValue("xd_coproperties").toString());
			// docxFiles[i].setSelected(true);
		}
		JScrollPane sp = new JScrollPane(vb2);
		sp.getViewport().setBackground(bgColor);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(sp, BorderLayout.CENTER);
		jp.setBorder(new TitledBorder(TStringUtils.getString("xdoc.out.docs")));
		return jp;
	}

	private JPanel createPropertiesPanel() {
		this.docxProperties = new Vector<JCheckBox>();
		this.boxSignature = Box.createVerticalBox();

		JScrollPane sp1 = new JScrollPane(boxSignature);
		sp1.getViewport().setBackground(bgColor);
		// signs
		JPanel jp1 = new JPanel(new BorderLayout());
		jp1.add(sp1, BorderLayout.CENTER);
		jp1.setBorder(new TitledBorder(TStringUtils.getString("xdoc.out.docs.properties")));
		return jp1;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		if (ae.getSource() instanceof JRadioButton) {
			JRadioButton jrb = (JRadioButton) ae.getSource();
			// continua solo para instancias de JRadioButton que estan dentro de
			// la lista de documentos
			if (!jrb.getName().startsWith("DOC:")) {
				return;
			}
			String t[] = jrb.getName().substring(4).split("PRPS:");
			boxSignature.setVisible(false);
			boxSignature.removeAll();
			docxProperties.clear();
			// may contain no aditional properties
			if (t.length > 1) {
				String prpl = t[1];
				Vector<TEntry> prps = TStringUtils.getPropertys(prpl);
				for (TEntry te : prps) {
					JCheckBox jrb1 = new JCheckBox(te.getKey() + ": " + te.getValue());
					jrb1.setBackground(bgColor);
					jrb1.addActionListener(this);
					docxProperties.add(jrb1);
					// bg.add(docxFiles[i]);
					jrb1.setName(te.getKey() + ";" + te.getValue());
					boxSignature.add(jrb1);
				}
			}
			boxSignature.setVisible(true);
		}
	}

	private JPanel createOutOptionPanel() {
		ButtonGroup bg = new ButtonGroup();

		this.out_merge = TUIUtils.getJRadioButton("ttxdoc.out.mergeonly", "xdoc.out.mergeonly", false);
		this.out_print = TUIUtils.getJRadioButton("ttxdoc.out.print", "xdoc.out.print", false);
		this.out_window = TUIUtils.getJRadioButton("ttxdoc.out.window", "xdoc.out.window", false);
		this.out_file = TUIUtils.getJRadioButton("ttxdoc.out.file", "xdoc.out.file", false);
		// para validar
		out_file.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				preValidate(null);
			}
		});
		out_merge.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				preValidate(null);
			}
		});
		addInputComponent("xdoc.out.mergeonly", out_merge, false, true);
		addInputComponent("xdoc.out.print", out_print, false, true);
		addInputComponent("xdoc.out.window", out_window, false, true);
		addInputComponent("xdoc.out.file", out_file, false, true);
		bg.add(out_merge);
		bg.add(out_print);
		bg.add(out_window);
		bg.add(out_file);
		ComponentTitledPane ctp1 = new ComponentTitledPane(out_file, createOutFilePanel());

		FormLayout lay = new FormLayout("fill:pref", // columns
				"pref, pref, pref, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(out_print, cc.xy(1, 1));
		build.add(out_window, cc.xy(1, 2));
		build.add(out_merge, cc.xy(1, 3));
		build.add(ctp1, cc.xy(1, 4));

		JPanel jp = build.getPanel();
		TUIUtils.setEmptyBorder(jp);
		return jp;
	}

	private JPanel createOutFilePanel() {
		this.fileChooserField = new WebFileChooserField();
		fileChooserField.setPreferredWidth(200);
		fileChooserField.setMultiSelectionEnabled(false);
		fileChooserField.setShowFileShortName(false);
		fileChooserField.setShowRemoveButton(false);
		WebButton wb = fileChooserField.getChooseButton();
		wb.removeActionListener(wb.getActionListeners()[0]);
		wb.addActionListener(new ActionListener() {
			private WebDirectoryChooser directoryChooser = null;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (directoryChooser == null) {

					// FIXME: tht main frame cant be directly referred by appname !!!!!!!!!!!!
					directoryChooser = new WebDirectoryChooser(Alesia.mainFrame);
				}
				directoryChooser.setVisible(true);

				if (directoryChooser.getResult() == DialogOptions.OK_OPTION) {
					fileChooserField.setSelectedFile(directoryChooser.getSelectedDirectory());
					validateFields();
				}
			}
		});
		fileChooserField.getWebFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		JTextField tfi = TUIUtils.getJTextField("ttxdoc.out.targetfile", "", 30);
		// tfi.setDocument(new AppPlainDocument("", 256));
		tfi.getDocument().addDocumentListener(this);
		addInputComponent("xdoc.out.targetfile", tfi, true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref", // columns
				"pref, 3dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		build.add(TUIUtils.getJLabel("xdoc.out.targetdirectory", true, true), cc.xy(1, 1));
		build.add(fileChooserField, cc.xy(3, 1));
		build.add(getLabelFor("xdoc.out.targetfile"), cc.xy(1, 3));
		build.add(getInputComponent("xdoc.out.targetfile"), cc.xy(3, 3));

		return build.getPanel();
	}

}
