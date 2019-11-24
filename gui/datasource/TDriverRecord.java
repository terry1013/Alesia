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
package gui.datasource;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * Record edition for t_drivers
 * 
 * 180405: the internal driver MUST BE manualy configured and tested.
 * 
 * @author terry
 *
 */
public class TDriverRecord extends AbstractRecordDataInput {

	public TDriverRecord(Record rcd, boolean newr) {
		super(null, rcd);

		setVisibleMessagePanel(false);

		addInputComponent("t_drname", TUIUtils.getJTextField(rcd, "t_drname"), true, true);
		addInputComponent("t_drclass", TUIUtils.getJTextField(rcd, "t_drclass"), true, true);
		addInputComponent("t_drlibrary", TUIUtils.getJTextField(rcd, "t_drlibrary"), true, true);
		addInputComponent("t_drsampleurl", TUIUtils.getJTextField(rcd, "t_drsampleurl"), true, true);

		FormLayout lay = new FormLayout("left:pref, 300dlu", // columns
				"p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_drname"), cc.xy(1, 1));
		build.add(getInputComponent("t_drname"), cc.xyw(1, 2, 2));
		build.add(getLabelFor("t_drclass"), cc.xy(1, 4));
		build.add(getInputComponent("t_drclass"), cc.xyw(1, 5, 2));
		build.add(getLabelFor("t_drlibrary"), cc.xy(1, 7));
		build.add(getInputComponent("t_drlibrary"), cc.xyw(1, 8, 2));
		build.add(getLabelFor("t_drsampleurl"), cc.xy(1, 10));
		build.add(getInputComponent("t_drsampleurl"), cc.xyw(1, 11, 2));

		// setActionBar(new ApplyAction(this));
		addWithoutBorder(build.getPanel());

		preValidate(null);
	}
}
