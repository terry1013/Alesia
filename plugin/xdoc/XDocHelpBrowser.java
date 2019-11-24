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
import gui.docking.*;
import gui.html.*;

import java.beans.*;

import core.*;

/**
 *
 */
public class XDocHelpBrowser extends UIComponentPanel implements DockingComponent {

	protected MiniBrowser browser;

	public XDocHelpBrowser() {
		super(null, false);
		this.browser = new MiniBrowser();
		addWithoutBorder(browser);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	@Override
	public void init() {
		browser.showPage(TResources.getURL("/plugin/xdoc/help.html"), true);
	}
}
