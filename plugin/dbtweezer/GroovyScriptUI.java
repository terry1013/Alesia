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
package plugin.dbtweezer;

import groovy.ui.*;
import groovy.ui.text.*;
import gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import action.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import core.*;
import core.datasource.*;

public class GroovyScriptUI extends AbstractRecordDataInput {

	private TextEditor textEditor;
	private JTextArea whereJTA;
	private String targetTN;
	private boolean dirtySQL, dirtyGroovy;

	public GroovyScriptUI(Record rcd) {
		super("scriptui.title", rcd);
		this.targetTN = (String) rcd.getFieldValue("s_sctable");
		AbstractAction testWhere = new AbstractAction("Probar WHERE") {
			@Override
			public void actionPerformed(ActionEvent e) {
				testScript("Sql");
			}
		};

		AbstractAction testscript = new AbstractAction("Probar Groovy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				testScript("Groovy");
			}
		};

		ConsoleTextEditor cte = new ConsoleTextEditor();
		this.textEditor = cte.getTextEditor();
		textEditor.setText((String) rcd.getFieldValue("s_scscript"));
		cte.setPreferredSize(new Dimension(600, 300));

		addInputComponent("s_scname", TUIUtils.getJTextField(rcd, "s_scname"), true, true);
		addInputComponent("s_scstatus", TUIUtils.getJComboBox("scstatus_", rcd, "s_scstatus"), false, true);

		JScrollPane wsp = TUIUtils.getJTextArea(rcd, "s_scwhere", 4);
		whereJTA = (JTextArea) wsp.getViewport().getView();
		addInputComponent("s_scwhere", wsp, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 300dlu", // columns
				"pref, 3dlu, pref, 3dlu, pref, pref, 3dlu, pref, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		build.add(getLabelFor("s_scname"), cc.xy(1, 1));
		build.add(getInputComponent("s_scname"), cc.xyw(3, 1, 5));
		build.add(getLabelFor("s_scstatus"), cc.xy(1, 3));
		build.add(getInputComponent("s_scstatus"), cc.xy(3, 3));
		build.add(TUIUtils.getJLabel("s_scscript", true, true), cc.xy(1, 5));
		build.add(cte, cc.xyw(1, 6, 7));
		build.add(getLabelFor("s_scwhere"), cc.xy(1, 8));
		build.add(getInputComponent("s_scwhere"), cc.xyw(1, 9, 7));

		setActionBar(testWhere, testscript, new AceptAction(this), new CancelAction(this));
		// setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	private void testScript(String sc) {
		if (sc.equals("Sql")) {
			String wc = whereJTA.getText();
			wc = (wc.equals("")) ? null : wc;
			dirtySQL = true;
			try {
				int rc = DBTConnectionManager.getSourceDB().getRowCount(targetTN, wc);
				showAplicationException(new TError("dbt.msg06", rc));
				dirtySQL = false;
				setEnableDefaultButton(!(dirtyGroovy || dirtySQL));
			} catch (Exception e1) {
				showAplicationException(new TError("ui.msg07", e1));
				setEnableDefaultButton(false);
			}
		}
		if (sc.equals("Groovy")) {
			String scr = textEditor.getText();
			dirtyGroovy = true;
			try {
				DBTweezerTask.testGroovyScrip(targetTN, scr);
				showAplicationException(new TError("dbt.msg02"));
				dirtyGroovy = false;
				setEnableDefaultButton(!(dirtyGroovy || dirtySQL));

			} catch (Exception e1) {
				showAplicationException(new TError("ui.msg07", e1));
				setEnableDefaultButton(false);
			}
		}
	}

	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		if (isShowingError()) {
			return;
		}
		if (textEditor.getText().trim().equals("")) {
			setEnableDefaultButton(false);
			return;
		}

		// test button are the last allowed actions
		dirtyGroovy = true;
		dirtySQL = true;
		setEnableDefaultButton(false);
	}

	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		r.setFieldValue("s_scscript", textEditor.getText());
		return r;
	}
}
