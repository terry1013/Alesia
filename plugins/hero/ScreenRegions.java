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

import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.application.Action;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.laf.button.*;

import core.*;
import gui.*;

/**
 * present component and information about the game.
 * 
 * @author terry
 *
 */
public class ScreenRegions extends TUIPanel {

	// private JScrollPane scrollPane;
	private JPanel mainJPanel;
//	private ActionMap actionMap;

	public ScreenRegions() {
		setAditionalInformationVisible(false);
//		actionMap = Alesia.getInstance().getContext().getActionMap(this);

		WebToggleButton run = TUIUtils.getWebToggleButton(Hero.actionMap.get("runTrooper"));
		WebToggleButton test = TUIUtils.getWebToggleButton(Hero.actionMap.get("testTrooper"));
		WebToggleButton stop = TUIUtils.getWebToggleButton(Hero.actionMap.get("stopTrooper"));

		javax.swing.Action load = TActionsFactory.getAction("fileChooserOpen");
		load.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				Hero.trooper.setEnviorement(load.getValue(TActionsFactory.DATA_LOADED));
				createPanel();
			}
		});
		setToolBar(load);

		GroupPanel g = new GroupPanel(run, stop, test);
		getToolBarPanel().add(g, LineLayout.START);

		this.mainJPanel = new JPanel(new BorderLayout());

		JPanel jp = new JPanel(new BorderLayout());
		jp.add(mainJPanel, BorderLayout.CENTER);
		jp.add(Hero.console, BorderLayout.SOUTH);
		setBodyComponent(jp);
	}

	@Action
	public void takeSample(ActionEvent event) {
		Hero.trooper.getSensorsArray().takeSample();
	}
	/**
	 * create the panel whit my cards and comunity cards
	 * 
	 * @return
	 */
	private JPanel createCardPanel() {
		JPanel mycard = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		mycard.setBorder(new TitledBorder("My cards"));
		mycard.add(Hero.trooper.getSensorsArray().getScreenSensor("hero.card1"));
		mycard.add(Hero.trooper.getSensorsArray().getScreenSensor("hero.card2"));
		mycard.add(Hero.trooper.getSensorsArray().getScreenSensor("hero.button"));
		mycard.add(Hero.trooper.getSensorsArray().getScreenSensor("hero.call"));
		mycard.add(Hero.trooper.getSensorsArray().getScreenSensor("pot"));

		JPanel comcard = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		comcard.setBorder(new TitledBorder("Comunity cards"));
		comcard.add(Hero.trooper.getSensorsArray().getScreenSensor("flop1"));
		comcard.add(Hero.trooper.getSensorsArray().getScreenSensor("flop2"));
		comcard.add(Hero.trooper.getSensorsArray().getScreenSensor("flop3"));
		comcard.add(Hero.trooper.getSensorsArray().getScreenSensor("turn"));
		comcard.add(Hero.trooper.getSensorsArray().getScreenSensor("river"));

		JPanel pot = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		pot.setBorder(new TitledBorder("Pot"));

		// my card + community + pot
		JPanel mcpo = new JPanel(new GridLayout(0, 2, 4, 4));
		mcpo.add(mycard);
		mcpo.add(comcard);
		// mcpo.add(pot);
		return mcpo;

	}

	/**
	 * create a panel for all configured villans. v1|v1|v2|...
	 * 
	 * @return
	 */
	private JPanel createVillansPanel() {
		int tv = Hero.trooper.getSensorsArray().getVillans();
		JPanel villans = new JPanel(new GridLayout(1, tv, 4, 4));
		for (int i = 1; i <= tv; i++) {
			JPanel vinf_p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
			vinf_p.add(Hero.trooper.getSensorsArray().getScreenSensor("villan" + i + ".name"));
			vinf_p.add(Hero.trooper.getSensorsArray().getScreenSensor("villan" + i + ".call"));

			JPanel vcar_p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
			vcar_p.add(Hero.trooper.getSensorsArray().getScreenSensor("villan" + i + ".card1"));
			vcar_p.add(Hero.trooper.getSensorsArray().getScreenSensor("villan" + i + ".card2"));
			vcar_p.add(Hero.trooper.getSensorsArray().getScreenSensor("villan" + i + ".button"));

			JPanel jp = new JPanel(new GridLayout(2, 0, 4, 4));
			jp.add(vcar_p);
			jp.add(vinf_p);
			jp.setBorder(new TitledBorder("villan " + i));

			villans.add(jp);
		}
		// villans.setBorder(new TitledBorder("Villans"));
		return villans;
	}

	private JPanel createActionAreaPanel() {
		JPanel aapanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		aapanel.setBorder(new TitledBorder("Buttons & actions"));
		Vector<ScreenSensor> btns = Hero.trooper.getSensorsArray().getActionAreas();
		for (ScreenSensor btn : btns) {
			aapanel.add(btn);
		}
		return aapanel;
	}
	/**
	 * create the {@link ScreenSensor} array plus all UI components
	 * 
	 */
	private void createPanel() {

		mainJPanel.removeAll();
		// left panel: all sensors from the screen
		// JPanel jpleft = new JPanel(new GridLayout(3, 0, 4, 4));
		JPanel arrayjp = new JPanel();
		arrayjp.setLayout(new BoxLayout(arrayjp, BoxLayout.Y_AXIS));
		arrayjp.add(createCardPanel());
		arrayjp.add(createVillansPanel());
		arrayjp.add(createActionAreaPanel());

		// sensor array + pokerprothesis
		// JPanel jp = new JPanel(new GridLayout(0, 2, 4, 4));
		// jp.add(jpleft);

		JComponent jl = Hero.trooper.getPokerSimulator().getInfoJTextArea();
		// jl.setBorder(new TitledBorder("Simulation"));
		JTabbedPane jtp = new JTabbedPane();
		jtp.add(new JScrollPane(arrayjp), "Sensor Array");
		jtp.add(jl, "simulator data");

		mainJPanel.add(jtp, BorderLayout.CENTER);

		// jp.add(jl);

		// mainJPanel.add(jp, BorderLayout.CENTER);
		// scrollPane.setViewportView(jp);
	}

}
