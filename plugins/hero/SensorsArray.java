package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.border.*;

import org.apache.commons.math3.stat.descriptive.*;

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
	private ScreenAreas screenAreas;
	private Vector<String> attentionAreas;

	public SensorsArray() {
		this.pokerSimulator = new PokerSimulator();
		this.robot = Hero.getNewRobot();
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
	 * @return - num of active villans + me
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

	public ScreenAreas getSensorDisposition() {
		return screenAreas;
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
		pokerSimulator.init();
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
	 * Shorcut method for read the seonsor <code>sensor</code>,perform the OCR operation an retrive the result
	 * 
	 * @param sensor - name of the sensor
	 * 
	 * @return value of the sensor
	 */
	public String readAndGetOCR(String sensor) {
		read(sensor);
		String ocr = getScreenSensor(sensor).getOCR();
		return ocr;
	}
	/**
	 * this metho campture all screeen´s areas without do any ocr operation. Use this mothod to retrive all sensor areas
	 * and set the enable status for fast comparation.
	 */
	public void lookTable(String... aname) {
		long t1 = System.currentTimeMillis();
		seeTable(false, aname);
		Hero.logPerformance(" for a total of " + (aname.length == 0 ? screenSensors.size() : aname.length) + " areas",
				t1);
	}

	/**
	 * this is the color max color on the villanX.button area indicating that i´this area has the dealler button. this
	 * is uded by {@link #updateTablePosition()} to calculate the Hero.s position in the table
	 */
	public static String DEALER_BUTTON_COLOR = "008080";

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
			dp = (sscol.equals(DEALER_BUTTON_COLOR)) ? i : dp;
		}
		int tp = dp == -1 ? vil + 1 : vil + 1 - dp;
		pokerSimulator.setTablePosition(tp);
	}
	DescriptiveStatistics ocrTime = new DescriptiveStatistics(10);

	private void seeTable(boolean read, String... sensors) {
		setAttentionOn(sensors);
		setStandByBorder();
		for (String sn : attentionAreas) {
			ScreenSensor ss = getScreenSensor(sn);
			ss.setBorder(read ? readingBorder : lookingBorder);
			ss.capture(read);
			if (ss.getOCRPerformanceTime() > 0) {
				ocrTime.addValue(ss.getOCRPerformanceTime());
			}
		}
		setStandByBorder();
	}

	/**
	 * Perform read operation on the {@link ScreenSensor} passed as argument. This method perform the OCR operation on
	 * the selected areas and update the {@link PokerSimulator} for numerical values.
	 * <p>
	 * if in the sensors argument, ther are a card area, this method update the pocker simulator and this may fire a new
	 * simulation.
	 * <p>
	 * After this method execution, the simulator reflect the actual game status
	 * 
	 * @param sensors - the list of sensors to read
	 */
	public void read(String... sensors) {
		seeTable(true, sensors);

		// update simulator
		pokerSimulator.setNunOfPlayers(getActivePlayers());
		updateTablePosition();
		pokerSimulator.setPotValue(getScreenSensor("pot").getIntOCR());
		pokerSimulator.setCallValue(getScreenSensor("call").getIntOCR());
		pokerSimulator.setHeroChips(getScreenSensor("hero.chips").getIntOCR());
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
		Hero.logger.info("average OCR time: " + ocrTime.getMean());
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

	/**
	 * Utility method to take the image of all card areas and store in the {@link ScreenSensor#IMAGE_CARDS} directory.
	 * Used for retrive the images of the cards in configuration step to used for detect the card rack during the
	 * gameplay
	 */
	public void takeCardSample() {
		try {
			for (ScreenSensor ss : screenSensors) {
				if (ss.isCardCard()) {
					ss.capture(false);
					BufferedImage bi = ss.getCapturedImage();
					String ext = "gif";
					File f = new File(ScreenSensor.IMAGE_CARDS + "sample_" + System.currentTimeMillis() + "." + ext);
					f.createNewFile();
					ImageIO.write(bi, ext, f);
				}
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
	protected void createSensorsArray(ScreenAreas areas) {
		this.screenAreas = areas;
		this.screenSensors = new Vector<ScreenSensor>();
		this.attentionAreas = new Vector<>();

		Vector<Shape> figs = new Vector<>(screenAreas.getShapes().values());
		for (Shape shape : figs) {
			ScreenSensor ss = new ScreenSensor(this, shape);
			screenSensors.addElement(ss);
		}
		setAttentionOn();
		setStandByBorder();
		pokerSimulator.init();
	}
	public PokerSimulator getPokerSimulator() {
		return pokerSimulator;
	}

}
