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
//		sensorsPanel.setPreferredSize(new Dimension(10, 2500));
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

	public void setArray(SensorsArray array) {
		setVisible(false);
		this.sensorsArray = array;

		simulationDataPanel.removeAll();
		simulationDataPanel.add(sensorsArray.getPokerSimulator().getReportPanel(), BorderLayout.CENTER);
		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			sensorsPanel.add(ss);
		}

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
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.CAPTURED, "show captured images"));
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.PREPARED, "show prepared images"));
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.COLORED, "show colored images"));
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

	private void filterSensors() {
		sensorsPanel.setVisible(false);
		String filter = ((TEntry) sensorTypeComboBox.getSelectedItem()).getKey().toString();
		String sCapture = ((TEntry) imageTypeComboBox.getSelectedItem()).getKey().toString();

		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			ss.showImage(sCapture);
			// spetial name or wildcard string (the structure type: xxx has noting in spetial, just a name)
			if (filter.startsWith("type:")) {
				if (filter.equals("type: textareas") && ss.isTextArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: numareas") && ss.isNumericArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: cardareas") && ss.isCardArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: actions") && ss.isActionArea())
					sensorsPanel.add(ss);
			} else {
				if (TStringUtils.wildCardMacher(ss.getName(), filter))
					sensorsPanel.add(ss);
			}
		}
		sensorsPanel.setVisible(true);
	}
}
