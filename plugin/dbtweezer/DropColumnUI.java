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

import gui.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class DropColumnUI extends AbstractRecordDataInput {

	public DropColumnUI(Record rcd, boolean newr) {
		super(null, rcd);

		JLabel msgjl = TUIUtils.getJLabel("action.DropColumn.message", false, true);
		addInputComponent("s_scscript", TUIUtils.getJTextArea(rcd, "s_scscript", 2), false, true);

		FormLayout lay = new FormLayout("200dlu", // columns
				"p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(msgjl, cc.xy(1, 1));
		build.add(getInputComponent("s_scscript"), cc.xy(1, 3));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
