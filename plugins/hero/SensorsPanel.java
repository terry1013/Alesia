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
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.laf.tabbedpane.*;
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
	private TUIPanel sensorsPanelMain;
	private JPanel sensorsPanel;
	private JPanel simulationDataPanel;
private SensorsArray sensorsArray;
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
		this.sensorsPanelMain = new TUIPanel(false);
		sensorsPanelMain.setBodyComponent(ajsp);

		WebTabbedPane wtp = new WebTabbedPane();
		wtp.add(sensorsPanelMain, "Sensor Array");
		wtp.add(simulationDataPanel, "Simulator data");
		wtp.add(Hero.consolePanel, "Log console");
		wtp.registerSettings(new Configuration<TabbedPaneState>("SensorsPanel.tabbedPanel"));

		setBodyComponent(wtp);
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
					if (filter.equals("type: textareas"))
						ss.setVisible(ss.isTextArea());
					if (filter.equals("type: numareas"))
						ss.setVisible(ss.isNumericArea());
					if (filter.equals("type: cardareas"))
						ss.setVisible(ss.isCardArea());
					if (filter.equals("type: actions"))
						ss.setVisible(ss.isActionArea());
				} else {
					boolean vis = TStringUtils.wildCardMacher(ss.getName(), filter);
					ss.setVisible(vis);
				}
			}
		}
		sensorsPanel.setVisible(true);
	}

	public void setArray(SensorsArray array) {
		setVisible(false);
		this.sensorsArray = array;

		simulationDataPanel.removeAll();
		simulationDataPanel.add(sensorsArray.getPokerSimulator().getReportPanel(), BorderLayout.CENTER);
		setSensorPanel();

		// list of options to filter sensors
		sensorTypeComboBox = new WebComboBox();
		sensorTypeComboBox.addItem(new TEntry("*", "All"));
		sensorTypeComboBox.addItem(new TEntry("villan*", "Only villans"));
		int vils = sensorsArray.getVillans();
		for (int i = 1; i <= vils; i++) {
			sensorTypeComboBox.addItem(new TEntry("villan" + i + "*", "only villan" + i));
		}
		// sensorTypeComboBox.addItem(new TEntry("*.card?", "Only card areas"));
		sensorTypeComboBox.addItem(new TEntry("*.call", "Only Call areas"));
		sensorTypeComboBox.addItem(new TEntry("type: textareas", "Only OCR text areas"));
		sensorTypeComboBox.addItem(new TEntry("type: numareas", "Only OCR numeric areas"));
		sensorTypeComboBox.addItem(new TEntry("type: cardareas", "Only cards areas"));
		sensorTypeComboBox.addItem(new TEntry("type: actions", "Only Actions areas"));
		sensorTypeComboBox.addActionListener(evt -> filterSensors());

		// options to show captured or prepared images
		this.imageTypeComboBox = new WebComboBox();
		imageTypeComboBox.addItem(new TEntry("true", "show captured images"));
		imageTypeComboBox.addItem(new TEntry("false", "show prepared images"));
		imageTypeComboBox.addActionListener(evt -> filterSensors());

		// set tool bar clear al previous toolbar components
		setToolBar(loadAction, Hero.actionMap.get("runTrooper"), Hero.actionMap.get("testTrooper"),
				Hero.actionMap.get("stopTrooper"), Hero.actionMap.get("pauseTrooper"),
				Hero.actionMap.get("takeCardSample"), Hero.actionMap.get("takeActionSample"));

		sensorsPanelMain.getToolBarPanel().removeAll();
		sensorsPanelMain.getToolBarPanel().add(sensorTypeComboBox, imageTypeComboBox);

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
		// test the panel disposition just sorting the sensors by name
		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		List<String> names = ssl.stream().map(ss -> ss.getName()).collect(Collectors.toList());
		names.sort(null);
		for (String name : names) {
			sensorsPanel.add(sensorsArray.getSensor(name));
		}

		// // general info
		// sensorsPanel.add(sensorsArray.getSensor("pot"));
		// sensorsPanel.add(sensorsArray.getSensor("flop1"));
		// sensorsPanel.add(sensorsArray.getSensor("flop2"));
		// sensorsPanel.add(sensorsArray.getSensor("flop3"));
		// sensorsPanel.add(sensorsArray.getSensor("turn"));
		// sensorsPanel.add(sensorsArray.getSensor("river"));
		//
		// // hero
		// sensorsPanel.add(sensorsArray.getSensor("hero.card1"));
		// sensorsPanel.add(sensorsArray.getSensor("hero.card2"));
		// sensorsPanel.add(sensorsArray.getSensor("hero.button"));
		// sensorsPanel.add(sensorsArray.getSensor("hero.call"));
		// sensorsPanel.add(sensorsArray.getSensor("hero.chips"));
		//
		// // villans
		//
		// int tv = sensorsArray.getVillans();
		// for (int i = 1; i <= tv; i++) {
		// sensorsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".name"));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".card1"));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".card2"));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".button"));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".call"));
		// sensorsPanel.add(sensorsArray.getSensor("villan" + i + ".chips"));
		// }
		//
		// // actions and binary areas
		// List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		// for (ScreenSensor ss : ssl) {
		// if (ss.isActionArea() || ss.isBinaryArea())
		// sensorsPanel.add(ss);
		// }
	}
}
