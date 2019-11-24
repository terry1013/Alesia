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

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class MailTemplateRecord extends AbstractRecordDataInput {

	private byte[] docData;
	private ImageIcon docIcon;
	private DocumentLoader docLoader;

	public MailTemplateRecord(Record rcd, boolean newr) {
		super(null, rcd);
		addInputComponent("m_tename", TUIUtils.getJTextField(rcd, "m_tename"), false, true);
		addInputComponent("m_tedescription", TUIUtils.getJTextArea(rcd, "m_tedescription", 3), true, true);
		addInputComponent("m_teversion", TUIUtils.getJTextField(rcd, "m_teversion"), true, true);

		docData = new byte[0];
		byte[] icon = new byte[0];
		if (!newr) {
			docData = (byte[]) rcd.getFieldValue("m_tetemplate");
			icon = (byte[]) rcd.getFieldValue("m_teicon");
			docIcon = new ImageIcon(icon);
		}

		this.docLoader = new DocumentLoader(this, docIcon);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, 70dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("m_tename"), cc.xy(1, 1));
		build.add(getInputComponent("m_tename"), cc.xy(3, 1));
		build.add(getLabelFor("m_tedescription"), cc.xy(1, 3));
		build.add(getInputComponent("m_tedescription"), cc.xyw(1, 4, 3));
		build.add(getLabelFor("m_teversion"), cc.xy(1, 6));
		build.add(getInputComponent("m_teversion"), cc.xy(3, 6));
		build.add(docLoader, cc.xywh(5, 1, 1, 6));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		// documento loader
		if (ae.getSource() instanceof DocumentLoader) {
			docData = docLoader.getDocumentData();
			docIcon = docLoader.getImageIcon();
		}
	}

	@Override
	public Record getRecord() {
		Record rcd = super.getRecord();
		rcd.setFieldValue("m_teicon", TPreferences.getBytearrayFromImage(docIcon));
		rcd.setFieldValue("m_tetemplate", docData);
		return rcd;
	}

	public class DocumentLoader extends JPanel {
		private JLabel jLabel = new JLabel();
		private FileDialog fileDialog = new FileDialog(Alesia.mainFrame, "Seleccionar", FileDialog.LOAD);
		private ActionListener listener;
		private ActionEvent event;
		private byte[] documentData;

		public DocumentLoader(ActionListener al, ImageIcon ii) {
			super(new BorderLayout());
			listener = al;
			event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "DocumentLoaded");
			jLabel.setBorder(new LineBorder(Color.gray));
			jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			jLabel.setHorizontalAlignment(JLabel.CENTER);
			if (ii != null) {
				jLabel.setIcon(ii);
			}
			jLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					fileDialog.setLocationRelativeTo(Alesia.mainFrame);
					fileDialog.setVisible(true);
					String f = fileDialog.getFile();
					String d = fileDialog.getDirectory();
					try {
						File sf = new File(d + f);
						if (f != null) {
							documentData = TResources.loadFile(sf.getPath());
							ImageIcon ii = FileUtils.getStandartFileIcon(sf, true);
							Image i = ii.getImage().getScaledInstance(120, 120, Image.SCALE_REPLICATE);
							jLabel.setIcon(new ImageIcon(i));
							listener.actionPerformed(event);
						}
					} catch (Exception e2) {
						// show exeption in dialog
						e2.printStackTrace();
					}
				}
			});
			add(jLabel, BorderLayout.CENTER);
		}
		public byte[] getDocumentData() {
			return documentData;
		}
		public ImageIcon getImageIcon() {
			return (ImageIcon) jLabel.getIcon();
		}
	}
}
