package plugins.hero;

import java.awt.*;
import java.awt.event.*;

import gui.prueckl.draw.*;

/**
 * Base class to send programaticily events throw the mouse or th keyboard.
 * 
 * @author terry
 *
 */
public class RobotActuator {

	private Robot robot;
	private int mouseDelay = 200;
	private int keyStrokeDelay = 20;

	private SensorDisposition sensorDisposition;

	public RobotActuator() {
		this.robot = Hero.getNewRobot();
		robot.setAutoDelay(40);
		robot.setAutoWaitForIdle(true);
	}

	/**
	 * this method perform an action name <code>aName</code>. The action must be any standar action inside the
	 * enviorement. e.g. if perform("fold") is called, this method look for the <code>fold</code> action name, locate
	 * the coordenates and perform {@link #doClick()} mouse action.
	 * 
	 * @param aName - the action name to perform
	 */
	public void perform(String aName) {

		// TODO: return boolean to indicate if the action was performed succesfully
		Shape fig = sensorDisposition.getShapes().get(aName);
		if (fig != null) {
			Point p = fig.getCenterPoint();
			mouseMove(p.x, p.y);
			doClick();
			Hero.logger.fine("Action " + aName + " performed.");
		} else {
			Hero.logger.fine("From RobotActuator.perform: Action " + aName
					+ " not performed. no button was found with that name");
		}
	}
	/**
	 * Set the enviorement for this instance. this method extract all areas setted as <code>area.type=action</code> in
	 * the figure property inside of the {@link DrawingPanel} pass as argument
	 * 
	 * @param dpanel - the panel
	 */
	public void setEnviorement(SensorDisposition sDisp) {
		this.sensorDisposition = sDisp;
	}

	/**
	 * Perform mouse left click. In test mode, this method send the {@link KeyEvent#VK_CONTROL} using the keyboard to
	 * signal only. the property "show location of pointer when press control key" must be set on in mouse properties
	 */
	public void doClick() {
		if (Hero.trooper.isTestMode()) {
			type(KeyEvent.VK_CONTROL);
			robot.delay(mouseDelay);
			return;
		}
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(mouseDelay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(mouseDelay);
	}

	/**
	 * Same as {@link Robot#mouseMove(int, int)} but whit safe dalay
	 * 
	 * @param x - X Position
	 * @param y - Y Position
	 */
	public void mouseMove(int x, int y) {
		robot.mouseMove(x, y);
		robot.delay(mouseDelay);
	}

	/**
	 * Perform key press on the keyboard. This key must be any of the {@link KeyEvent} key codes
	 * 
	 * @param vk - the key code to type
	 */
	public void type(int vk) {
		robot.delay(keyStrokeDelay);
		robot.keyPress(vk);
		robot.keyRelease(vk);
	}

	/**
	 * Type the text <code>str</code> using the keyboard. This method only process the characters from A-Z and numbers.
	 * To sent especial key, use {@link #type(int)} method.
	 * 
	 * @param str - text to type
	 */
	public void type(String str) {
		byte[] bytes = str.getBytes();
		for (byte b : bytes) {
			int code = b;
			// A-Z convertion
			if ((code > 96 && code < 123)) {
				code = code - 32;
			}
			robot.delay(keyStrokeDelay);
			robot.keyPress(code);
			robot.keyRelease(code);
		}
	}
}