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

import gui.datasource.*;

import java.util.*;

import javafx.application.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * plugin entry for mail
 * 
 * @author terry
 * 
 */
public class MailBomber extends PluginAdapter {

	@Override
	public void startPlugin(Properties prps) throws Exception {
		super.startPlugin(prps);
		Platform.setImplicitExit(false);
	}
	
	@Override
	public Object executePlugin(Object obj) {

		JMenu jm = new JMenu(myProperties.getProperty("plugin.caption"));
		jm.add(new ConnectionProfileAction());
		jm.addSeparator();
		jm.add(new AddressBookUpdateAction("create"));
		jm.add(new AddressBookUpdateAction("update"));
		jm.add(new AddressBookUpdateAction("write"));
		jm.addSeparator();
		jm.add(new MailBomberAction());
		return jm;
	}

	public static TEntry formatMail(Record abr) {
		return new TEntry(abr.getFieldValue(0), abr.getFieldValue("m_abname") + " ["
				+ abr.getFieldValue("m_abemail") + "]");
	}
}
