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

import core.*;
import core.datasource.*;


import action.*;

/**
 * action for combined documents. this action call native program to execute any of valid operations:
 * {@link MergeDocActions#OPEN} or {@link MergeDocActions#PRINT}
 * 
 * @author terry
 * 
 */
public class MergeDocActions extends TAbstractAction {

	public static int OPEN = 0;
	public static int PRINT = 1;

	private UIListPanel listPanel;
	private int actType;

	/**
	 * new instance
	 * 
	 * @param tat - instace of {@link UIListPanel}
	 * @param act action for record: OPEN or PRINT
	 */
	public MergeDocActions(UIListPanel tat, int act) {
		super(act == OPEN ? "xdoc.merge.action.open" : "xdoc.merge.action.print", act == OPEN
				? "/plugin/xdoc/document_view"
				: "/plugin/xdoc/printer", RECORD_SCOPE, act == OPEN ? "xdoc.action.mdopen" : "xdoc.action.mdprint");
		this.listPanel = tat;
		this.actType = act;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			Record r = listPanel.getRecord();
			File tmpf = File.createTempFile("tmp", ".docx");
			tmpf.deleteOnExit();
			OutputStream out = new FileOutputStream(tmpf);
			out.write((byte[]) r.getFieldValue("XD_MDMERGED_DOC"));
			out.close();
			Desktop dsk = Desktop.getDesktop();
			if (actType == OPEN) {
				dsk.open(tmpf);
			} else {
				dsk.print(tmpf);
			}
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}
}
