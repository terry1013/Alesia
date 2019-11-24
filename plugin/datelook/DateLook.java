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
package plugin.datelook;
import gui.docking.*;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import core.*;
import core.tasks.*;

public class DateLook extends JPanel implements Plugin, DockingComponent {

	JPanel contentPane;
	private DateLookPanel dateLookPanel;
	private EventMemory eventMemory;
	private AlarmHandler alarm_handler;

	public static String FILE_DIR = System.getProperty("user.dir");

	public DateLook() {
		super(new BorderLayout());
		try {
			dateLookPanel = new DateLookPanel(this);
			eventMemory = new EventMemory(dateLookPanel);
			eventMemory.read_data_file(); // read dates from file and store in memory
			dateLookPanel.set_event_memory(eventMemory);
			add(dateLookPanel, BorderLayout.CENTER);
			alarm_handler = new AlarmHandler(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DateLookPanel getDateLookPanel() {
		return dateLookPanel;
	}
	
	public EventMemory getEventMemory() {
		return eventMemory;
	}

	@Override
	public Object executePlugin(Object obj) {
		return new DateLookAction();
	}

	@Override
	public void startPlugin(Properties prps) throws Exception {
		// add AlarmHandler to main task
		TTaskManager.scheduleAtFixedRate(alarm_handler, 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public void endPlugin() throws Exception {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
	}	
}
