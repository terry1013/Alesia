package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.border.*;

import core.*;
import gui.prueckl.draw.*;

/**
 * This class control the array of sensor inside of the screen. This class is responsable for reading all the sensor
 * configurated in the {@link DrawingPanel} passsed as argument in the {@link #createSensorsArray(DrawingPanel)} method.
 * <p>
 * althout this class are the eyes of the tropper, numerical values must be retrives throw {@link PokerSimulator}. the
 * poker simulator values are populated during the reading process using the method
 * {@link PokerSimulator#addCard(String, String)} at every time that a change in the enviorement is detected.
 * <p>
 * TODO: what about the pot information ????
 * 
 * @author terry
 *
 */
public class SensorsArray {

	private Vector<ScreenSensor> screenSensors;
	private Robot robot;
	private Border readingBorder, lookingBorder, standByBorder;
	private PokerSimulator pokerSimulator;
	private SensorDisposition sensorDisposition;
	private Vector<String> attentionAreas;

	public SensorsArray(PokerSimulator ps) {
		this.robot = Hero.getNewRobot();
		this.pokerSimulator = ps;

		this.readingBorder = new LineBorder(Color.BLUE, 2);
		this.lookingBorder = new LineBorder(Color.GREEN, 2);
		this.standByBorder = new LineBorder(Color.lightGray, 2);
	}

	/**
	 * return <code>true</code> if there are attentions areas to perform ocr operations <b>but not all of them</b>. If
	 * the method {@link #setAttentionOn(String...)} was called with <code>null</code> argument, this method return
	 * <code>false</code> but if some areas are setted to be read ({@link #setAttentionOn(String...)} called with some
	 * areas names) this methos return <code>true</code>
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	public boolean isSpetialAttentionSetted() {
		return screenSensors.size() > attentionAreas.size();
	}
	/**
	 * Return a list of all actions areas
	 * 
	 * @see SensorsPanel
	 * @return list of actions
	 */
	public Vector<ScreenSensor> getActionAreas() {
		Vector<ScreenSensor> vec = new Vector<>();
		for (ScreenSensor sensor : screenSensors) {
			if (sensor.isActionArea()) {
				vec.add(sensor);
			}
		}
		return vec;
	}

	/**
	 * Return the number of current active players (me plus active villans). a villan is active if he has dealed cards
	 * 
	 * @return - num of active villans
	 */
	public int getActivePlayers() {
		int av = 1;
		for (int i = 1; i <= getVillans(); i++) {
			ScreenSensor vc1 = getScreenSensor("villan" + i + ".card1");
			ScreenSensor vc2 = getScreenSensor("villan" + i + ".card2");
			if (vc1.isEnabled() && vc2.isEnabled()) {
				av++;
			}
		}
		return av;
	}

	public SensorDisposition getSensorDisposition() {
		return sensorDisposition;
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * return the {@link ScreenSensor} by name. The name comes from property <code>name</code>
	 * 
	 * @param ssn - screen sensor name
	 * 
	 * @return the screen sensor instance or <code>null</code> if no sensor is found.
	 */
	public ScreenSensor getScreenSensor(String ssn) {
		ScreenSensor ss = null;
		for (ScreenSensor sensor : screenSensors) {
			if (sensor.getName().equals(ssn)) {
				ss = sensor;
			}
		}
		if (ss == null) {
			System.err.println("No sensor name " + ssn + " was found.");
		}
		return ss;
	}

	/**
	 * Return the number of villans configurated in this table.
	 * 
	 * @see SensorsPanel
	 * 
	 * @return total villans
	 */
	public int getVillans() {
		return (int) screenSensors.stream()
				.filter(ss -> ss.getName().startsWith("villan") && ss.getName().contains("name")).count();
	}

	/**
	 * initialize this sensor array. clearing all sensor and all variables
	 */
	public void init() {
		screenSensors.stream().forEach((ss) -> ss.init());
		setAttentionOn();
	}

	public boolean isAnyVillansCardVisible() {
		boolean ve = false;
		for (ScreenSensor ss : screenSensors) {
			ve = (ss.isVillanCard() && ss.getOCR() != null) ? true : ve;
		}
		return ve;
	}
	/**
	 * this metho campture all screeen´s areas without do any ocr operation. This method is intended to retrive all
	 * sensor areas and set the enable status for fast comparation.
	 * 
	 */
	public void lookTable(String... aname) {
		long t1 = System.currentTimeMillis();
		seeTable(false, aname);
		Hero.logPerformance(" for a total of " + (aname.length == 0 ? screenSensors.size() : aname.length) + " areas",
				t1);
	}

	/**
	 * Update the table position. the Herro´s table position is determinated detecting the dealer button and counting
	 * clockwise. the position 1 is th small blind, 2 big blind, 3 under the gun, and so on. The dealer position is the
	 * highest value
	 */
	private void updateTablePosition() {
		int vil = getVillans();
		int dp = -1;
		for (int i = 1; i <= vil; i++) {
			String sscol = getScreenSensor("villan" + i + ".button").getMaxColor();
			dp = (sscol.equals("008080")) ? i : dp;
		}
		int tp = dp == -1 ? vil + 1 : vil + 1 - dp;
		pokerSimulator.setTablePosition(tp);
	}

	private void seeTable(boolean read, String... ssn) {
		setAttentionOn(ssn);
		setStandByBorder();
		for (String sn : attentionAreas) {
			ScreenSensor ss = getScreenSensor(sn);
			ss.setBorder(read ? readingBorder : lookingBorder);
			ss.capture(read);
		}
		setStandByBorder();
	}

	/**
	 * Perform read operation on the ssn areas names or all areas. The read operation will perform OCR operation if the
	 * 
	 * @param ssn
	 */
	public void read(String... ssn) {
		seeTable(true, ssn);

		// update simulator
		pokerSimulator.setNunOfPlayers(getActivePlayers());
		updateTablePosition();
		pokerSimulator.setPotValue(getScreenSensor("pot").getIntOCR());
		pokerSimulator.setCallValue(getScreenSensor("call").getIntOCR());
		pokerSimulator.setRaiseValue(getScreenSensor("raise").getIntOCR());

		// update hero carts and comunity cards (if this method was called for card areas)
		// REMEMBER ADD METHOD FIRE RUNSIMULATION
		for (String sn : attentionAreas) {
			ScreenSensor sss = getScreenSensor(sn);
			String ocr = sss.getOCR();
			if ((sss.isHoleCard() || sss.isComunityCard()) && ocr != null) {
				pokerSimulator.addCard(sss.getName(), ocr);
			}
		}

	}
	/**
	 * Indicate to the array sensor that put attention only in an area (or o grup of them). This {@link SensorsArray}
	 * will only capture the images for the specific areas ignoring the rest of the sensors.
	 * 
	 * @param anames - name or names of the areas to pay attention.
	 */
	private void setAttentionOn(String... anames) {
		attentionAreas.clear();
		// no input argument? fill attention areas with all the sensors
		if (anames.length == 0) {
			screenSensors.stream().forEach(ss -> attentionAreas.add(ss.getName()));
		} else {
			Collections.addAll(attentionAreas, anames);
		}
	}

	public void takeSample() {
		try {
			for (ScreenSensor ss : screenSensors) {
				// if (ss.isCardArea()) {
				ss.capture(false);
				BufferedImage bi = ss.getCapturedImage();
				String ext = "gif";
				File f = new File(ScreenSensor.SAMPLE_PATH + "sample_" + System.currentTimeMillis() + "." + ext);
				f.createNewFile();
				ImageIO.write(bi, ext, f);
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isActionButtonAvailable() {
		boolean act = false;
		for (ScreenSensor ss : screenSensors) {
			act = (ss.isActionArea() && ss.isEnabled()) ? true : act;
		}
		return act;
	}

	private void setStandByBorder() {
		for (ScreenSensor ss : screenSensors) {
			ss.setBorder(standByBorder);
		}
	}

	/**
	 * Create the sensors setted in the {@link DrawingPanel}. This method only take care of the
	 * <code>area.type=sensor</code> area types.
	 * <p>
	 * dont use this method directly. use {@link Trooper#setEnviorement(DrawingPanel)}
	 * 
	 * @param dpanel - the enviorement
	 */
	protected void createSensorsArray(SensorDisposition sDisp) {
		this.sensorDisposition = sDisp;
		this.screenSensors = new Vector<ScreenSensor>();
		this.attentionAreas = new Vector<>();

		Vector<Shape> figs = new Vector<>(sensorDisposition.getShapes().values());
		for (Shape shape : figs) {
			ScreenSensor ss = new ScreenSensor(this, shape);
			screenSensors.addElement(ss);
		}
		setAttentionOn();
		setStandByBorder();
	}
}
