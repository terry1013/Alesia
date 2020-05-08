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
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.laf.*;
import com.alee.utils.*;

import core.*;
import gui.*;
import gui.console.*;
import net.sourceforge.tess4j.*;

public class Hero extends TPlugin {

	// protected static Tesseract iTesseract;
	protected static ActionMap actionMap; 
	protected static ConsolePanel consolePanel;
	protected static SensorsPanel sensorsPanel;
	protected static Logger logger;

	public Hero() {

		// iTesseract.setLanguage("pok");
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		logger = Logger.getLogger("Hero");
		consolePanel = new ConsolePanel(logger);
		new Trooper();
	}
	
	public static Tesseract geTesseract() {
		// TODO: no visible performance improve by setting every sensor with his own teseract instance
		Tesseract iTesseract = new Tesseract(); // JNA Interface Mapping
		iTesseract.setDatapath("plugins/hero/tessdata"); // path to tessdata directory

		// DON:T SET THE PAGEMODE VAR: THIS DESTROY THE ACURACY OF THE OCR OPERATION. i don.t know why but it is
		// iTesseract.setPageSegMode(3); //

		iTesseract.setTessVariable("classify_enable_learning", "0");
		iTesseract.setTessVariable("OMP_THREAD_LIMIT", "1");
		// TODO: recheck performanece. no visible performance improve setting this variable
		// iTesseract.setOcrEngineMode(0); // Run Tesseract only - fastest
		return iTesseract;
	}

	public static Action getLoadAction() {
		Action load = TActionsFactory.getAction("fileChooserOpen");
		load.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				Trooper.getInstance().init((File) load.getValue(TActionsFactory.DATA_LOADED));
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

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("screenRegions"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		Trooper t = Trooper.getInstance();
		boolean pause = !t.isPaused();
		AbstractButton ab = (AbstractButton) event.getSource();
		ab.setSelectedIcon(TResources.getSmallIcon("plugins/hero/resources/ResumeTrooper"));
		ab.setSelected(pause);
		t.pause(pause);
	}

	@org.jdesktop.application.Action
	public Task runTrooper(ActionEvent event) {
		return start(event);
	}

	@org.jdesktop.application.Action
	public void screenRegions(ActionEvent event) {
		sensorsPanel = new SensorsPanel();
		Alesia.getMainPanel().setContentPanel(sensorsPanel);

//		temp: change the main frame using this coordenates: 0,40 547,735
		Alesia.mainFrame.setBounds(0, 40, 547, 735);
		
		// temporal
		Trooper.getInstance().init(new File("plugins/hero/resources/ps-main table.ppt"));
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		actionMap.get("testTrooper").setEnabled(true);
		actionMap.get("runTrooper").setEnabled(true);
		actionMap.get("pauseTrooper").setEnabled(true);
		Trooper.getInstance().cancelTrooper(true);
	}

	@org.jdesktop.application.Action
	public void takeActionSample(ActionEvent event) {
		Trooper.getInstance().getSensorsArray().takeActionSample();
	}

	@org.jdesktop.application.Action
	public void takeCardSample(ActionEvent event) {
		Trooper.getInstance().getSensorsArray().takeCardSample();
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		return start(event);
	}

	private Task start(ActionEvent event) {
		WebLookAndFeel.setForceSingleEventsThread(false);
		Trooper t = new Trooper(Trooper.getInstance());
		PropertyChangeListener tl = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (Trooper.PROP_DONE.equals(evt.getPropertyName())) {
					// WebLookAndFeel.setForceSingleEventsThread(true);
				}
			}
		};
		t.addPropertyChangeListener(tl);
		ApplicationAction aa = (ApplicationAction) ((AbstractButton) event.getSource()).getAction();
		t.getPokerSimulator().setTableParms(buyin, sb, bb);
		t.setTestMode(aa.getName().equals("testTrooper"));
		actionMap.get("testTrooper").setEnabled(false);
		actionMap.get("runTrooper").setEnabled(false);
		actionMap.get("pauseTrooper").setEnabled(true);
		return t;
	}
}
