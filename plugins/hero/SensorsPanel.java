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
import javax.swing.border.*;

import gui.*;

/**
 * present component and information about the game.
 * 
 * @author terry
 *
 */
public class SensorsPanel extends TUIPanel {

	// private JScrollPane scrollPane;
	private JPanel mainJPanel;
	private Trooper trooper;
	private Action loadAction;
	// private ActionMap actionMap;

	public SensorsPanel() {
		setAditionalInformationVisible(false);
		// actionMap = Alesia.getInstance().getContext().getActionMap(this);
		this.loadAction = Hero.getLoadAction();
		setToolBar(loadAction);
		this.mainJPanel = new JPanel(new BorderLayout());
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(mainJPanel, BorderLayout.CENTER);
		jp.add(Hero.console, BorderLayout.SOUTH);
		setBodyComponent(jp);
	}

	/**
	 * create the {@link ScreenSensor} array plus all UI components
	 * 
	 */
	public void setEnviorement(Trooper trooper) {
		setVisible(false);
		updateTrooper(trooper);
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

		JComponent jl = trooper.getSensorsArray().getPokerSimulator().getInfoJTextArea();
		// jl.setBorder(new TitledBorder("Simulation"));
		JTabbedPane jtp = new JTabbedPane();
		jtp.add(new JScrollPane(arrayjp), "Sensor Array");
		jtp.add(jl, "simulator data");

		mainJPanel.add(jtp, BorderLayout.CENTER);

		// jp.add(jl);

		// mainJPanel.add(jp, BorderLayout.CENTER);
		// scrollPane.setViewportView(jp);
		setVisible(true);
	}

	public void updateTrooper(Trooper trooper) {
		this.trooper = trooper;
		setToolBar(loadAction, Hero.actionMap.get("runTrooper"), Hero.actionMap.get("testTrooper"),
				Hero.actionMap.get("stopTrooper"), Hero.actionMap.get("takeCardSample"));
	}

	private JPanel createActionAreaPanel() {
		JPanel aapanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		aapanel.setBorder(new TitledBorder("Buttons & actions"));
		Vector<ScreenSensor> btns = trooper.getSensorsArray().getActionAreas();
		for (ScreenSensor btn : btns) {
			aapanel.add(btn);
		}
		return aapanel;
	}

	/**
	 * create the panel whit my cards and comunity cards
	 * 
	 * @return
	 */
	private JPanel createCardPanel() {
		JPanel mycard = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		mycard.setBorder(new TitledBorder("My cards"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("hero.card1"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("hero.card2"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("hero.button"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("hero.call"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("hero.chips"));
		mycard.add(trooper.getSensorsArray().getScreenSensor("pot"));

		JPanel comcard = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		comcard.setBorder(new TitledBorder("Comunity cards"));
		comcard.add(trooper.getSensorsArray().getScreenSensor("flop1"));
		comcard.add(trooper.getSensorsArray().getScreenSensor("flop2"));
		comcard.add(trooper.getSensorsArray().getScreenSensor("flop3"));
		comcard.add(trooper.getSensorsArray().getScreenSensor("turn"));
		comcard.add(trooper.getSensorsArray().getScreenSensor("river"));

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
		int tv = trooper.getSensorsArray().getVillans();
		JPanel villans = new JPanel(new GridLayout(1, tv, 4, 4));
		for (int i = 1; i <= tv; i++) {
			JPanel vinf_p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
			vinf_p.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".name"));
			vinf_p.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".call"));

			JPanel vcar_p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
			vcar_p.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".card1"));
			vcar_p.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".card2"));
			vcar_p.add(trooper.getSensorsArray().getScreenSensor("villan" + i + ".button"));

			JPanel jp = new JPanel(new GridLayout(2, 0, 4, 4));
			jp.add(vcar_p);
			jp.add(vinf_p);
			jp.setBorder(new TitledBorder("villan " + i));

			villans.add(jp);
		}
		// villans.setBorder(new TitledBorder("Villans"));
		return villans;
	}

}
