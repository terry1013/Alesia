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
import java.util.*;

import gui.console.*;
import gui.datasource.*;
import gui.prueckl.draw.*;

import javax.swing.*;

import net.sourceforge.tess4j.*;
import core.*;

public class Hero extends TPlugin {

	protected static Tesseract iTesseract;
	protected static Trooper trooper;
	protected static ActionMap actionMap;
	protected static MessageConsolePanel console; 

	public Hero() {
		iTesseract = new Tesseract(); // JNA Interface Mapping
		iTesseract.setDatapath("plugins/hero/tessdata"); // path to tessdata directory
		// iTesseract.setLanguage("pok");
		trooper = new Trooper();
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		console = new MessageConsolePanel();
	}

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("screenRegions"));
//		alist.add(actionMap.get("drawEditor"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void screenRegions(ActionEvent event) {
		Alesia.getMainPanel().setContentPanel(new ScreenRegions());
	}

	@org.jdesktop.application.Action
	public void drawEditor(ActionEvent event) {
		DrawingEditor de = new DrawingEditor();
		Alesia.getMainPanel().setContentPanel(de);
	}

	@org.jdesktop.application.Action
	public void runTrooper(ActionEvent event) {
		console.clearConsole();
		Hero.trooper.setTestMode(false);
		Hero.trooper.start();
	}
	@org.jdesktop.application.Action
	public void testTrooper(ActionEvent event) {
		Hero.trooper.setTestMode(true);
		Hero.trooper.start();
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		Hero.trooper.stop();
	}

	public static void logInfo(String txt) {
		// log("info", txt);
	}
	public static void logGame(String txt) {
		log("game", txt);
	}
	public static void logDebug(String txt) {
		String mn = Thread.currentThread().getStackTrace()[2].getMethodName();
		// log("fine", mn + ": " + txt);
	}
	private static void log(String level, String txt) {
		System.out.println("[" + level + "] " + txt);
	}

	public static void logPerformance(String txt, long t1) {
		// StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		// the 3th element contain the method´s name who call this method
		String mn = Thread.currentThread().getStackTrace()[2].getMethodName();
		String sp = TStringUtils.formatSpeed(System.currentTimeMillis() - t1);
		// log("fine", mn + ": " + txt + ": " + sp);
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
}
