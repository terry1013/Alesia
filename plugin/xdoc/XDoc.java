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

import java.util.*;


import action.*;
import core.*;
import core.datasource.*;

/**
 * plugin entry point for document generation.
 * 
 * @author terry
 * 
 */
public class XDoc extends PluginAdapter {


	@Override
	public Object executePlugin(Object obj) {
		Vector<TAbstractAction> v = new Vector<TAbstractAction>();
		v.add(new XDocAction());
		v.add(new MenuActionFactory(DocumentsTemplates.class));

		return v;
	}

	public static String getRecipientFieldsValue(Record br) {
		String flds[] = PluginManagerIld.getPluginProperty("XDoc", "xdoc.config.recipient.fields").split(";");
		String re = "";
		for (String s : flds) {
			re += br.getFieldValue(s) + " ";
		}
		return re.trim();
	}
}
