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
package gui;

import java.beans.*;

import javax.swing.*;

/**
 * extens {@link JSplitPane} with divider location property save
 * 
 * @author terry
 * 
 */
public class TJSplitPane extends JSplitPane implements PropertyChangeListener {

	private String classname;

	/**
	 * new instanace
	 * 
	 * @param cnt - source container
	 * @param orientation - {@link JSplitPane} orientation
	 */

	public TJSplitPane(JComponent cnt, int orientation) {
		super(orientation, true);
		this.classname = cnt.getClass().getName();
		addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		// String spw = TPreferences.getProperty(TPreferences.DIVIDER_LOCATION, classname);
		// if (spw != null) {
		// setDividerLocation(Integer.parseInt(spw));
		// }
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int dl = (Integer) evt.getNewValue();
		// TPreferences.putProperty(TPreferences.DIVIDER_LOCATION, classname, Integer.toString(dl));
	}
}
