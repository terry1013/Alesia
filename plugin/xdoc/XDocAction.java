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

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import action.*;

public class XDocAction extends TAbstractAction {

	public XDocAction() {
		super("xdoc.action", "/plugin/xdoc/xdicon", NO_SCOPE, "ttxdoc.action");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		/**
		 * TabWindow tabWindow = new TabWindow();
		 * tabWindow.add(DockingContainer.createDynamicView("plugin.xdoc.DocumentRecipient"));
		 * tabWindow.add(DockingContainer.createDynamicView("plugin.xdoc.MergedDocs")); tabWindow.add();
		 * 
		 */

		View hlp = DockingContainer.createDynamicView(XDocHelpBrowser.class.getName());

		SplitWindow sw = new SplitWindow(false, 0.6f, DockingContainer.createDynamicView(DocumentRecipient.class
				.getName()), DockingContainer.createDynamicView(MergedDocs.class.getName()));
		// SplitWindow sw1 = new SplitWindow(false, 0.7f, sw,
		// DockingContainer.createDynamicView("plugin.xdoc.XDocHelpBrowser"));
		SplitWindow sw1 = new SplitWindow(true, 0.6f, sw, hlp);

		DockingContainer.setWindow(sw1, getClass().getName());
		// WindowBar wb = rootw.getWindowBar(Direction.RIGHT);
		// wb.addTab(hlp);
	}
}
