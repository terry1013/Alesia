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
package plugin.eteam;

import core.*;

/**
 * plugin entry for team work
 * 
 * @author terry
 * 
 */
public class ETeam extends PluginAdapter {

	@Override
	public Object executePlugin(Object obj) {

		return new ETeamAction();
	}
}
