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

import javax.swing.*;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class SqlScriptUI extends AbstractRecordDataInput {

	public SqlScriptUI(Record rcd, boolean newr) {
		super("scriptui.title", rcd);

		addInputComponent("s_scname", TUIUtils.getJTextField(rcd, "s_scname"), true, true);
		addInputComponent("s_scstatus", TUIUtils.getJComboBox("scstatus_", rcd, "s_scstatus"), false, true);
		
		JScrollPane jsp = TUIUtils.getJTextArea(rcd, "s_scscript", 12);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		addInputComponent("s_scscript", jsp, true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 200dlu", // columns
				"pref, 3dlu, pref, 3dlu, pref, pref"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		build.add(getLabelFor("s_scname"), cc.xy(1, 1));
		build.add(getInputComponent("s_scname"), cc.xyw(3, 1, 5));
		build.add(getLabelFor("s_scstatus"), cc.xy(1, 3));
		build.add(getInputComponent("s_scstatus"), cc.xy(3, 3));
		build.add(getLabelFor("s_scscript"), cc.xy(1, 5));
		build.add(getInputComponent("s_scscript"), cc.xyw(1, 6,7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
