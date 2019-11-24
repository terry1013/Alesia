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
package plugin.flicka;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import action.*;

/**
 * build and load dashboard for mail plugin
 * 
 * @author terry
 * 
 */
public class FlickaAction extends TAbstractAction {

	public FlickaAction() {
		super(NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		View[] v1 = new View[]{DockingContainer.createDynamicView(DBExplorer.class.getName()),
				DockingContainer.createDynamicView(RaceList.class.getName()),
				DockingContainer.createDynamicView(HorseHistory.class.getName()),
				DockingContainer.createDynamicView(JockeyHistory.class.getName()),
				DockingContainer.createDynamicView(Statistics.class.getName()),
				DockingContainer.createDynamicView(PDistribution.class.getName()),
		DockingContainer.createDynamicView(PDistributionBySpeed.class.getName())};
		TabWindow tw = new TabWindow(v1);
		SplitWindow sw = new SplitWindow(false, 0.6f, DockingContainer.createDynamicView(EntryPanel.class.getName()),
				tw);
		DockingContainer.setWindow(sw, getClass().getName());
	}
}
