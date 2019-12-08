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
import java.util.*;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.managers.settings.*;

import core.*;
import gui.*;

/**
 * present component and information about the game.
 * 
 * @author terry
 *
 */
public class SensorsPanel extends TUIPanel {

	// private JScrollPane scrollPane;
	private JPanel sensorsPanel;
	private JPanel simulationDataPanel;

	private Trooper trooper;
	private Action loadAction;
	// private ActionMap actionMap;
	private WebComboBox sensorTypeComboBox;
	private WebComboBox imageTypeComboBox;

	public SensorsPanel() {
		showAditionalInformation(false);
		// actionMap = Alesia.getInstance().getContext().getActionMap(this);
		this.loadAction = Hero.getLoadAction();
		setToolBar(loadAction);
		this.sensorsPanel = new JPanel(new GridLayout(0, 2));
		sensorsPanel.setPreferredSize(new Dimension(10, 2500));
		this.simulationDataPanel = new JPanel(new BorderLayout());

		JScrollPane ajsp = new JScrollPane(sensorsPanel);

		JTabbedPane jtp = new JTabbedPane();
		jtp.add(ajsp, "Sensor Array");
		jtp.add(simulationDataPanel, "Simulator data");

		JPanel jp = new JPanel(new BorderLayout());
		jp.add(jtp, BorderLayout.CENTER);
		jp.add(Hero.consolePanel, BorderLayout.SOUTH);
		setBodyComponent(jp);
	}

	private void filterSensors() {
		sensorsPanel.setVisible(false);
		String filter = ((TEntry) sensorTypeComboBox.getSelectedItem()).getKey().toString();
		boolean sCapture = Boolean.parseBoolean(((TEntry) imageTypeComboBox.getSelectedItem()).getKey().toString());

		Component components[] = sensorsPanel.getComponents();
		for (Component component : components) {
			if (component instanceof ScreenSensor) {
				ScreenSensor ss = (ScreenSensor) component;
				ss.setVisible(false);
				ss.showCapturedImage(sCapture);
				// spetial name or wildcard string (the structure type: xxx has noting in spetial, just a name) 
				if (filter.startsWith("type:")) {
					if (filter.equals("type: ocrareas"))
						ss.setVisible(ss.isOCRArea());
					if (filter.equals("type: cardareas"))
						ss.setVisible(ss.isCardArea());
				} else {
					boolean vis = TStringUtils.wildCardMacher(ss.getName(), filter);
					ss.setVisible(vis);
				}
			}
		}
		sensorsPanel.setVisible(true);
	}
	/**
	 * create the {@link ScreenSensor} array plus all UI components
	 * 
	 */
	public void setEnviorement(Trooper trooper) {
		setVisible(false);
		this.trooper = trooper;

		simulationDataPanel.removeAll();
		simulationDataPanel.add(trooper.getSensorsArray().getPokerSimulator().getInfoJTextArea(), BorderLayout.CENTER);
		setSensorPanel();

		// list of options to filter sensors
		sensorTypeComboBox = new WebComboBox();
		sensorTypeComboBox.addItem(new TEntry("*", "All"));
		sensorTypeComboBox.addItem(new TEntry("villan*", "Only villans"));
		int vils = trooper.getSensorsArray().getVillans();
		for (int i = 1; i <= vils; i++) {
			sensorTypeComboBox.addItem(new TEntry("villan" + i + "*", "only villan" + i));
		}
		// sensorTypeComboBox.addItem(new TEntry("*.card?", "Only card areas"));
		sensorTypeComboBox.addItem(new TEntry("*.call", "Only Call areas"));
		sensorTypeComboBox.addItem(new TEntry("type: ocrareas", "Only OCR areas"));
		sensorTypeComboBox.addItem(new TEntry("type: cardareas", "Only cards areas"));
		sensorTypeComboBox.addActionListener(evt -> filterSensors());

		// options to show captured or prepared images
		this.imageTypeComboBox = new WebComboBox();
		imageTypeComboBox.addItem(new TEntry("true", "show captured images"));
		imageTypeComboBox.addItem(new TEntry("false", "show prepared images"));
		imageTypeComboBox.addActionListener(evt -> filterSensors());

		// set tool bar clear al previous toolbar components
		setToolBar(loadAction, Hero.actionMap.get("runTrooper"), Hero.actionMap.get("testTrooper"),
				Hero.actionMap.get("stopTrooper"), Hero.actionMap.get("pauseTrooper")
		// ,Hero.actionMap.get("takeCardSample"), Hero.actionMap.get("takeActionSample"));
		);
		getToolBarPanel().add(sensorTypeComboBox, imageTypeComboBox);

		// after all component has been created
		imageTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.imageType"));
		sensorTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.filter"));

		setVisible(true);
	}
	/**
	 * create the panel whit all sensors
	 * 
	 * @return
	 */
	private void setSensorPanel() {
		sensorsPanel.removeAll();

		// general info
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("pot"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("flop1"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("flop2"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("flop3"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("turn"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("river"));

		// hero
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("hero.card1"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("hero.card2"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("hero.button"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("hero.call"));
		sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("hero.chips"));

		// villans

		int tv = trooper.getSensorsArray().getVillans();
		for (int i = 1; i <= tv; i++) {
			sensorsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".name"));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".card1"));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".card2"));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".button"));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".call"));
			sensorsPanel.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".chips"));
		}

		// actions areas
		Vector<ScreenSensor> btns = trooper.getSensorsArray().getActionAreas();
		for (ScreenSensor btn : btns) {
			sensorsPanel.add(btn);
		}
	}
}
