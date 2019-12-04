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
package plugins.hero;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.laf.*;

import core.*;
import gui.console.*;
import gui.prueckl.draw.*;
import net.sourceforge.tess4j.*;

public class Hero extends TPlugin {

	protected static Tesseract iTesseract;
	protected static ActionMap actionMap;
	protected static ConsolePanel consolePanel;
	protected static SensorsPanel sensorsPanel;
	protected static Logger logger;

	public Hero() {
		iTesseract = new Tesseract(); // JNA Interface Mapping
		iTesseract.setDatapath("plugins/hero/tessdata"); // path to tessdata directory
		// iTesseract.setLanguage("pok");
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		logger = Logger.getLogger("Hero");
		consolePanel = new ConsolePanel(logger);
	}
	public static Action getLoadAction() {
		Action load = TActionsFactory.getAction("fileChooserOpen");
		load.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				new Trooper();
				Trooper.getInstance().setEnviorement((File) load.getValue(TActionsFactory.DATA_LOADED));
			}
		});
		return load;
	}
	/**
	 * This metod is separated because maybe in the future we will need diferents robot for diferent graphics
	 * configurations
	 * 
	 * @return
	 */
	public static Robot getNewRobot() {
		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static void logPerformance(String txt, long t1) {
		String sp = TStringUtils.formatSpeed(System.currentTimeMillis() - t1);
		logger.finest(txt + ": " + sp);
	}
	@org.jdesktop.application.Action
	public void drawEditor(ActionEvent event) {
		DrawingEditor de = new DrawingEditor();
		Alesia.getMainPanel().setContentPanel(de);
	}

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("screenRegions"));
		// alist.add(actionMap.get("drawEditor"));
		return alist;
	}

	@org.jdesktop.application.Action
	public Task runTrooper(ActionEvent event) {
		return start(false);
	}

	@org.jdesktop.application.Action
	public void screenRegions(ActionEvent event) {
		sensorsPanel = new SensorsPanel();
		Alesia.getMainPanel().setContentPanel(sensorsPanel);
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		actionMap.get("testTrooper").setEnabled(true);
		actionMap.get("runTrooper").setEnabled(true);
		Trooper.getInstance().cancel(false);
		WebLookAndFeel.setForceSingleEventsThread(true);
	}

	@org.jdesktop.application.Action
	public void takeCardSample(ActionEvent event) {
		Trooper.getInstance().getSensorsArray().takeCardSample();
	}

	@org.jdesktop.application.Action
	public void takeActionSample(ActionEvent event) {
		Trooper.getInstance().getSensorsArray().takeActionSample();
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		return start(true);
	}

	private Task start(boolean test) {
		WebLookAndFeel.setForceSingleEventsThread(false);
		// Hero.console.cleanConsole();
		Trooper t = new Trooper(Trooper.getInstance());
		t.setTestMode(test);
		actionMap.get("testTrooper").setEnabled(false);
		actionMap.get("runTrooper").setEnabled(false);
		return t;
	}
}
