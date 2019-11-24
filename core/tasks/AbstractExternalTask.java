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
package core.tasks;

import java.util.*;
import java.util.logging.*;

import action.*;

import core.*;

public abstract class AbstractExternalTask implements Runnable {
	
	private Properties properties;
	private String className;
	
	public AbstractExternalTask() {
		this.className = getClass().getSimpleName();
		this.properties = new Properties();
		loadProperties();
	}
	
	public void loadProperties() {
		// 180208: commented due massive modification on tpreferences. check for futher implementation of this method
		/*
		Vector kls = new Vector(TPreferences.aProperties.keySet());
		for (int i = 0; i < kls.size(); i++) {
			String k = (String) kls.elementAt(i);
			if (k.startsWith(className)) {
				int s = k.indexOf(".") + 1;
				properties.setProperty(k.substring(s), TPreferences.aProperties.get(k));
			}
		}
		*/
	}
	
	@Override
	public void run() {
		try {
			Alesia.logger.log(Level.INFO, this.getClass().getName() + " started.");
			Alesia.logger.log(Level.INFO, this.getClass().getName() + " found parameters: " + properties.toString());
			
			executeExternalTask();

			Alesia.logger.log(Level.INFO, this.getClass().getName() + " End.");
		} catch (Exception e) {
			Alesia.logger.log(Level.SEVERE, e.getMessage(), e);
			Exit.shutdown();
		}
	}
	
	public abstract void executeExternalTask() throws Exception;
	
	
	public String getProperty(String k) {
		return properties.getProperty(k);
	}
}
