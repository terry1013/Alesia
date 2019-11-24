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
package plugin.mailbomber;

import gui.*;
import gui.wlaf.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import net.atlanticbb.tantlinger.shef.*;
import action.*;

import com.alee.extended.filechooser.*;
import com.alee.extended.layout.*;
import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import core.datasource.*;
import dev.autocomplete.*;

/**
 * compose mail dialog
 */
public class ComposeMailDialog extends AbstractRecordDataInput {

	private HTMLEditorPane htmlEditor;
	private ComposeMailViewer mailViewer;
	private WebFileDrop fileDrop;
	private JComboBox templateComboBox;
	private boolean newr;
	private Record record;
	private TWebFileChooserField fileChooserField;
	private JPanel cardPanel;

	/**
	 * new instance
	 * 
	 * @param rcd - record
	 * @param newr - new or not
	 */
	public ComposeMailDialog(Record rcd, boolean newr) {
		super(null, rcd);
		this.newr = newr;
		this.record = rcd;

		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "M_ADDRESS_BOOK", "M_ABTYPE = 'company' OR M_ABTYPE = 'ounit'");
		RecordSelector tojcb = new RecordSelector(sr, "m_abid", "m_abname", record.getFieldValue("m_meto"), false);
		addInputComponent("m_meto", tojcb, false, true);

		TEntry ten = TStringUtils.getTEntry("tentry.none");
		TEntry[] tel = TStringUtils.getTEntryGroupFrom("M_TEMPLATES", "M_TEID", "M_TENAME", null, ten);
		templateComboBox = TUIUtils.getJComboBox("ttm_metemplate_id", tel, record.getFieldValue("m_metemplate_id"));
		templateComboBox.addActionListener(this);
		addInputComponent("m_metemplate_id", templateComboBox, false, true);

		addInputComponent("m_mesubject", TUIUtils.getJTextField(rcd, "m_mesubject"), true, true);

		// for new compose, read htmleditor.css and add style tag
		if (newr) {
			String tmp1 = "<style>"+FileUtils.readToString(TResources.getFile("htmleditor.css"))+"</style>";
			rcd.setFieldValue("m_mebody", tmp1);
		}
		// body editor & webview
		this.htmlEditor = new HTMLEditorPane();
		htmlEditor.setPreferredSize(new Dimension(650, 450));
		htmlEditor.setText((String) rcd.getFieldValue("m_mebody"));
		VariableAutoSuggestion vas = new VariableAutoSuggestion(getAdressBookVar());
		vas.setAutoSuggestionFor(htmlEditor.getJEditorPane());

		mailViewer = new ComposeMailViewer();
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(htmlEditor, "Editor");
		cardPanel.add(mailViewer, "Viewer");

		FormLayout lay = new FormLayout("left:pref, 3dlu, 350dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 250dlu"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		// build.add(createTOWebButton(), cc.xy(1, 1));
		build.add(getLabelFor("m_meto"), cc.xy(1, 1));
		build.add(getInputComponent("m_meto"), cc.xy(3, 1));
		build.add(getLabelFor("m_metemplate_id"), cc.xy(1, 3));
		build.add(getInputComponent("m_metemplate_id"), cc.xy(3, 3));
		build.add(getLabelFor("m_mesubject"), cc.xy(1, 7));
		build.add(getInputComponent("m_mesubject"), cc.xy(3, 7));
		// build.add(htmlEditor, cc.xyw(1, 9, 3));
		build.add(cardPanel, cc.xyw(1, 9, 3));

		JPanel jp = build.getPanel();
		JPanel jp2 = getAttachPanel();
		jp.setBorder(new EmptyBorder(4, 4, 4, 4));
		Dimension jp2d = new Dimension(jp.getPreferredSize().width, jp.getPreferredSize().height);
		jp2.setPreferredSize(jp2d);
		jp2.setBorder(new EmptyBorder(4, 4, 4, 4));

		JTabbedPane jtp = new JTabbedPane();
		jtp.add(jp, "Correo");
		jtp.add(jp2, "Adjuntos");

		setActionBar(new AbstractAction[]{new AceptAction(this), new CancelAction(this)});
		add(jtp);
		preValidate(null);
		changeView();
	}

	private String getHtmlFromFile(String tid) {
		String html = null;
		try {
			Record ter = ConnectionManager.getAccessTo("M_TEMPLATES").exist("M_TEID = '" + tid + "'");
			File mt = MailBomberTask.extractMailTemplate((byte[]) ter.getFieldValue("m_tetemplate"));
			String d = mt.getParent();
			File f1 = new File(d + "/index.html");
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f1));
			byte[] bh = new byte[bis.available()];
			bis.read(bh);
			bis.close();
			html = new String(bh);
			// replace relative path to full path
			// File d2 = new File(d);
			// String url = d2.toURI().toURL().toString()+"inline/";
			// html = html.replace("inline/", url);
		} catch (Exception e) {
			showAplicationExceptionMsg("mail.msg09");
		}
		return html;
	}
	
	private void changeView() {
		TEntry te = (TEntry) templateComboBox.getSelectedItem();
		String tid = (String) te.getKey();
		String show = "";
		if (tid.equals("*none")) {
			show = "Editor";
		} else {
			mailViewer.load(tid);
			show = "Viewer";
		}
		CardLayout cl = (CardLayout) (cardPanel.getLayout());
		cl.show(cardPanel, show);
		mailViewer.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		if (ae.getSource() == templateComboBox) {
			changeView();
		}
	}

	@Override
	public Record getRecord() {
		Record rcd = super.getRecord();
		// message body only for no patter mail
		String msg = "";
		if (((TEntry) templateComboBox.getSelectedItem()).getKey().equals("*none")) {
			msg = htmlEditor.getText();
		}
		rcd.setFieldValue("m_mebody", msg);
		rcd.setFieldValue("m_mestatic_attch", TPreferences.getByteArrayFromObject(fileDrop.getSelectedFiles()));
		return rcd;
	}

	private ArrayList<TEntry> getAdressBookVar() {
		TEntry[] abtlist = TStringUtils.getTEntryGroup("mail.book.type_");
		Record abmod = ConnectionManager.getAccessTo("m_address_book").getModel();
		ArrayList<TEntry> keyw = new ArrayList<TEntry>();
		for (TEntry te : abtlist) {
			String k = (String) te.getKey();
			// String prf = k.equals("personal") ? "" : k + ".";
			String prf = k + ".";
			for (int c = 0; c < abmod.getFieldCount(); c++) {
				String fn = abmod.getFieldName(c);
				// no fields for list
				if (!(fn.equalsIgnoreCase("m_abid") || fn.equalsIgnoreCase("m_abparentid") || fn.equalsIgnoreCase("m_abphoto")|| fn.equalsIgnoreCase("m_abtype"))) {
					keyw.add(new TEntry(TStringUtils.getString(fn), "${" + prf + fn + "}"));
				}
			}
		}
		return keyw;
	}

	private JPanel getAttachPanel() {
		// dimanic file attachment
		// -------------------------
		fileChooserField = TUIUtils.getWebDirectoryChooserField(record, "m_medynamic_attch_dir");
		addInputComponent("m_medynamic_attch_dir", fileChooserField, false, true);

		JScrollPane sjta = TUIUtils.getJTextArea(record, "m_medynamic_attch", 2);
		JViewport jvp = (JViewport) ((JScrollPane) sjta).getViewport();
		JTextArea jta = (JTextArea) jvp.getView();
		jta.setFont(new Font("Courier New", Font.PLAIN, 12));
		VariableAutoSuggestion vas = new VariableAutoSuggestion(getAdressBookVar());
		vas.setAutoSuggestionFor(jta);
		addInputComponent("m_medynamic_attch", sjta, false, true);

		addInputComponent("m_medynamic_attch_err",
				TUIUtils.getJComboBox("mail.att.act_", record, "m_medynamic_attch_err"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 320dlu", // columns
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		build.add(getLabelFor("m_medynamic_attch_dir"), cc.xy(1, 3));
		build.add(fileChooserField, cc.xy(3, 3));
		build.add(getLabelFor("m_medynamic_attch"), cc.xy(1, 5));
		build.add(getInputComponent("m_medynamic_attch"), cc.xy(3, 5));
		build.add(getLabelFor("m_medynamic_attch_err"), cc.xy(1, 7));
		build.add(getInputComponent("m_medynamic_attch_err"), cc.xy(3, 7));
		JPanel jp1 = build.getPanel();
		jp1.setBorder(new TitledBorder(TStringUtils.getString("mail.attach.border1")));

		// File drop area
		// -------------------------
		addInputComponent("m_mestatic_attch_err",
				TUIUtils.getJComboBox("mail.att.act", record, "m_mestatic_attch_err"), false, true);
		this.fileDrop = new WebFileDrop();
		// JScrollPane jsp = new JScrollPane(fileDrop);
		fileDrop.setPreferredSize(new Dimension(100, 200));
		if (!newr) {
			ArrayList<File> fls = (ArrayList<File>) TPreferences.getObjectFromByteArray((byte[]) record
					.getFieldValue("m_mestatic_attch"));
			fileDrop.setSelectedFiles(fls);
		}
		lay = new FormLayout("left:pref, 3dlu, 350dlu", // columns
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"); // rows
		cc = new CellConstraints();
		build = new PanelBuilder(lay);
		build.add(fileDrop, cc.xyw(1, 1, 3));
		build.add(getLabelFor("m_mestatic_attch_err"), cc.xy(1, 3));
		build.add(getInputComponent("m_mestatic_attch_err"), cc.xy(3, 3));
		JPanel jp2 = build.getPanel();
		jp2.setBorder(new TitledBorder(TStringUtils.getString("mail.attach.border2")));

		JPanel jpf = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 4, true, false));
		jpf.add(jp1);
		jpf.add(jp2);
		return jpf;
	}
}
