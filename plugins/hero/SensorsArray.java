package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.math3.stat.descriptive.*;

import com.jgoodies.common.base.*;

/**
 * This class control the array of sensor inside of the screen. This class is responsable for reading all the sensor
 * configurated in the {@link DrawingPanel} passsed as argument in the {@link #createSensorsArray(DrawingPanel)} method.
 * <p>
 * althout this class are the eyes of the tropper, numerical values must be retrives throw {@link PokerSimulator}. the
 * poker simulator values are populated during the reading process using the method
 * {@link PokerSimulator#addCard(String, String)} at every time that a change in the enviorement is detected.
 * 
 * @author terry
 *
 */
public class SensorsArray {

	/**
	 * this is the color max color on the villanX.button area indicating that i´this area has the dealler button. this
	 * is uded by {@link #updateTablePosition()} to calculate the Hero.s position in the table
	 */
	public static String DEALER_BUTTON_COLOR = "008080";
	/**
	 * Read/see only numeric sensors. Numeric sensors are chips, pot, calls Etc
	 */
	public final static String TYPE_NUMBERS = "Numbers";
	/**
	 * Read/see only text sensors, names of the villans mainly
	 */
	public final static String TYPE_TEXT = "Text";

	/**
	 * Read/see TODO: read only villans information (all)
	 * 
	 * @see #TYPE_CARDS
	 */
	public final static String TYPE_VILLANS = "Villans";

	/**
	 * Read/see only cards areas. this type is only for hero cards and comunity cards
	 * 
	 * @see #TYPE_VILLANS_CARDS
	 */
	public final static String TYPE_CARDS = "Cards";
	/**
	 * Read/see only actions areas (call button, raise, continue)
	 */
	public final static String TYPE_ACTIONS = "Actions";
	private TreeMap<String, ScreenSensor> screenSensors;
	private Robot robot;
	private Border readingBorder, lookingBorder, standByBorder;
	private PokerSimulator pokerSimulator;
	private ShapeAreas screenAreas;
	DescriptiveStatistics tesseractTime = new DescriptiveStatistics(10);
	DescriptiveStatistics imageDiffereceTime = new DescriptiveStatistics(10);
	private Hashtable<String, Integer> blinds;

	public SensorsArray() {
		this.pokerSimulator = new PokerSimulator();
		this.robot = Hero.getNewRobot();
		this.readingBorder = new LineBorder(Color.BLUE, 2);
		this.lookingBorder = new LineBorder(Color.GREEN, 2);
		this.standByBorder = new LineBorder(new JPanel().getBackground(), 2);
		this.blinds = new Hashtable<>();
		this.screenSensors = new TreeMap<>();
	}

	/**
	 * Return a list of all actions areas
	 * 
	 * @see SensorsPanel
	 * @return list of actions public Vector<ScreenSensor> getActionAreas() { Vector<ScreenSensor> vec = new Vector<>();
	 *         for (ScreenSensor sensor : screenSensors.values()) { if (sensor.isActionArea()) { vec.add(sensor); } }
	 *         return vec; }
	 */

	/**
	 * Return the number of current villans active seats.
	 * 
	 * @see #isSeatActive(int)
	 * @return - num of active villans + me
	 */
	public int getActiveSeats() {
		int av = 0;
		for (int i = 1; i <= getVillans(); i++) {
			av += isSeatActive(i) ? 1 : 0;
		}
		// at this point at least must be 1 villan active set
		if (av == 0)
			Hero.logger.severe("Fail to detect active seats");
		return av;
	}

	/**
	 * Return the number of current active villans.
	 * 
	 * @see #isVillanActive(int)
	 * @see #getActiveSeats()
	 * @return - num of active villans
	 */
	public int getActiveVillans() {
		int av = 0;
		for (int i = 1; i <= getVillans(); i++) {
			if (isVillanActive(i))
				av++;
		}
		return av;
	}

	/**
	 * return <code>true</code> if the villan identifyed as id argument is active. A VILLAN IS ACTIVE IF HE HAS CARDS IN
	 * THIS HANDS. if a player fold his card. this method will not count that player. from this method point of view.
	 * the player is in tha game, but in this particular moment are not active.
	 * 
	 * @param id - villan id or seat
	 * @return true if the villan is active
	 */
	public boolean isVillanActive(int id) {
		ScreenSensor vc1 = getSensor("villan" + id + ".card1");
		ScreenSensor vc2 = getSensor("villan" + id + ".card2");
		return vc1.isEnabled() && vc2.isEnabled();
	}
	/**
	 * return where in the table, the dealer button are. If hero has the button, this method return 0.
	 * 
	 * @return where the dealer button are or -1 for a fail in thable position detection
	 */
	public int getDealerButtonPosition() {
		int vil = getVillans();
		int bp = -1;
		String sscol = getSensor("hero.button").getMaxColor();
		bp = sscol.equals(DEALER_BUTTON_COLOR) ? 0 : -1;
		for (int i = 1; i <= vil; i++) {
			sscol = getSensor("villan" + i + ".button").getMaxColor();
			bp = (sscol.equals(DEALER_BUTTON_COLOR)) ? i : bp;
		}
		if (bp == -1) {
			Hero.logger.severe("Fail to detect table position.");
		}
		return bp;
	}

	public PokerSimulator getPokerSimulator() {
		return pokerSimulator;
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * return the {@link ScreenSensor} by name. The name comes from property <code>name</code>
	 * 
	 * @param sensorName - screen sensor name
	 * 
	 * @return the screen sensor instance or <code>null</code> if no sensor is found.
	 */
	public ScreenSensor getSensor(String sensorName) {
		ScreenSensor ss = screenSensors.get(sensorName);
		Preconditions.checkNotNull(ss, "No sensor name " + sensorName + " was found.");
		return ss;
	}

	public ShapeAreas getSensorDisposition() {
		return screenAreas;
	}
	/**
	 * retriva an array of sensor.s names acording to the type argument. For example, to retrive all "call" sensors,
	 * pass to thid method ".call" will return sensor hero.call, villan1.call etc. <code>null</code> argument return all
	 * configured sensors.
	 * 
	 * <p>
	 * the list is sorted
	 * 
	 * @param type - type of sensor or <code>null</code> for all
	 * 
	 * @return list of sensors or empty list if no sensor was found
	 */
	public List<ScreenSensor> getSensors(String type) {
		List<ScreenSensor> list;
		if (type == null)
			list = screenSensors.values().stream().collect(Collectors.toList());
		else
			list = screenSensors.values().stream().filter(ss -> ss.getName().contains(type))
					.collect(Collectors.toList());
		return list;
	}

	/**
	 * This method return a list of all action sensors currently enables. For example. if a enviorement with 10 binary
	 * sensors, calling this method return <code>1469</code> means that the sensors 1, 4, 6 and 9 are enabled. all
	 * others are disabled.
	 * 
	 * @return list of binary sensors enabled public String getEnabledActions() { String onlist = "";
	 * 
	 *         List<ScreenSensor> sslist = screenSensors.values().stream().filter(ScreenSensor::isActionArea)
	 *         .collect(Collectors.toList()); for (int i = 0; i < sslist.size(); i++) { ScreenSensor ss =
	 *         screenSensors.get("binary.sensor" + i); onlist += ss.isEnabled() ? "" : i; } return onlist; }
	 */

	/**
	 * Return the number of villans configurated in this table.
	 * 
	 * @see SensorsPanel
	 * 
	 * @return total villans
	 */
	public int getVillans() {
		return (int) screenSensors.keySet().stream().filter(sn -> sn.startsWith("villan") && sn.contains("name"))
				.count();
	}
	/**
	 * initialize this sensor array. clearing all sensor and all variables
	 */
	public void init() {
		screenSensors.values().forEach((ss) -> ss.init());
		pokerSimulator.init();
		blinds.clear();
	}

	/**
	 * return <code>true</code> if the villanId seat is active. A seat is active if there are a villan sittion on it.
	 * this method check the villan name sensor and the villan chip sensor. if both are active, the seat is active.
	 * <p>
	 * from this method point of view, there are a villan sittin on a seat currently playing or not. maybe he abandom
	 * the action
	 * 
	 * @param villanId - the seat as configured in the ppt file. villan1 is at hero.s left
	 * @see #getActiveVillans()
	 * @return numers of villans active seats
	 */
	public boolean isSeatActive(int villanId) {
		ScreenSensor vname = getSensor("villan" + villanId + ".name");
		ScreenSensor vchip = getSensor("villan" + villanId + ".chips");
		return vname.isEnabled() && vchip.isEnabled();
	}

	/**
	 * Shortcut to get the enable/disable status from a sensor
	 * 
	 * @param sensorName - sensor name
	 * 
	 * @return <code>true</code> if the sensor is enabled
	 */
	public boolean isSensorEnabled(String sensorName) {
		ScreenSensor ss = getSensor(sensorName);
		return ss.isEnabled();
	}

	public void lookActionSensors() {
		long t1 = System.currentTimeMillis();
		List<ScreenSensor> sslist = screenSensors.values().stream().filter(ss -> ss.isActionArea())
				.collect(Collectors.toList());
		readSensors(false, sslist);
		System.out.println("lookActionSensors() " + (System.currentTimeMillis() - t1));
	}

	/**
	 * this metho campture all screeen´s areas without do any ocr operation. Use this mothod to retrive all sensor areas
	 * and set the enable status for fast comparation.
	 * 
	 */
	public void lookTable() {
		long t1 = System.currentTimeMillis();
		List<ScreenSensor> sslist = getSensors(null);
		readSensors(false, sslist);
		long t2 = System.currentTimeMillis() - t1;
		// System.out.println("SensorsArray.lookTable() " + t2);
	}

	/**
	 * Perform read operation on the {@link ScreenSensor} acoording to the type of the sensor. The type can be any of
	 * TYPE_ global constatn passed as argument. This method perform the OCR operation on the selected areas and update
	 * the {@link PokerSimulator} if it.s necesary.
	 * <p>
	 * After this method execution, the simulator reflect the actual game status
	 * 
	 * @param sensors - type of sensor to read
	 */
	public void read(String type) {
		Collection<ScreenSensor> allSensors = screenSensors.values();

		// TODO: complete implementation
		if (TYPE_VILLANS.equals(type)) {
			// List<ScreenSensor> slist = allSensors.stream().filter(ss -> ss.getName().startsWith("villan"))
			// .collect(Collectors.toList());
			// readSensors(true, slist);
		}

		// ation areas
		if (TYPE_ACTIONS.equals(type)) {
			List<ScreenSensor> slist = allSensors.stream().filter(ss -> ss.isActionArea()).collect(Collectors.toList());
			readSensors(true, slist);
		}

		// numeric types retrive all numers and update poker simulator
		if (TYPE_NUMBERS.equals(type)) {
			List<ScreenSensor> slist = allSensors.stream().filter(ss -> ss.isNumericArea())
					.collect(Collectors.toList());
			readSensors(true, slist);
			updateTablePosition();
			updateCalls();
			// TODO: Temporal for th: the pot is the previous pot value + all calls
			int potInt = getSensor("pot").getIntOCR();
			potInt = potInt + blinds.values().stream().mapToInt(iv -> iv.intValue()).sum();
			pokerSimulator.setPotValue(potInt);
			pokerSimulator.setCallValue(getSensor("call").getIntOCR());
			pokerSimulator.setHeroChips(getSensor("hero.chips").getIntOCR());
			pokerSimulator.setRaiseValue(getSensor("raise").getIntOCR());

			pokerSimulator.updateReport();
		}

		// cards areas sensor will perform a simulation
		if (TYPE_CARDS.equals(type)) {
			pokerSimulator.getCardsBuffer().clear();
			List<ScreenSensor> slist = allSensors.stream().filter(ss -> ss.isCardArea()).collect(Collectors.toList());
			readSensors(true, slist);
			for (ScreenSensor ss : slist) {
				if ((ss.isHoleCard() || ss.isComunityCard())) {
					String ocr = ss.getOCR();
					if (ocr != null)
						pokerSimulator.getCardsBuffer().put(ss.getName(), ocr);
				}
			}
			pokerSimulator.setNunOfPlayers(getActiveVillans() + 1);
			pokerSimulator.runSimulation();
		}
		pokerSimulator.setVariable("sensorArray.Tesseract OCR time", tesseractTime.getMean());
		pokerSimulator.setVariable("sensorArray.ImageDiference OCR time", imageDiffereceTime.getMean());
	}

	/**
	 * Utility method to take the image of the villans?.name areas for some and store in the
	 * {@link GameRecorder#IMAGE_ACTIONS}. This method is invoked during configuration step to retribe samples of the
	 * designated areas that contain image information for determinate the action performed by the villans during the
	 * gameplay
	 */
	public void takeActionSample() {
		try {
			for (String sn : screenSensors.keySet()) {
				// TODO temporal for TH. the action area is the same as the name area
				if (sn.contains(".name")) {
					ScreenSensor ss = screenSensors.get(sn);
					ss.capture(false);
					BufferedImage bi = ss.getCapturedImage();
					String ext = "png";
					File f = new File(GameRecorder.IMAGE_ACTIONS + "sample_" + System.currentTimeMillis() + "." + ext);
					f.createNewFile();
					ImageIO.write(bi, ext, f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Utility method to take the image of all card areas and store in the {@link ScreenSensor#IMAGE_CARDS} directory.
	 * Used for retrive the images of the cards in configuration step to used for detect the card rack during the
	 * gameplay
	 */
	public void takeCardSample() {
		try {
			String ext = "png";
			for (String sn : screenSensors.keySet()) {
				ScreenSensor ss = screenSensors.get(sn);
				if (ss.isComunityCard() || ss.isHoleCard()) {
					// if (ss.getName().equals("hero.card2")) {
					ss.capture(false);
					BufferedImage image = ss.getCapturedImage();
					// image = TColorUtils.getImageDataRegion(image);
					File f = new File(ScreenSensor.IMAGE_CARDS + "sample_" + System.currentTimeMillis() + "." + ext);
					f.createNewFile();
					ImageIO.write(image, ext, f);
				}
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Perform the read operation for all {@link ScreenSensor} passed int the list argument.
	 * 
	 * @param read - <code>true</code> to perform OCR operation over the selected sensors. <code>false</code> only
	 *        capture the image
	 * @param list - list of sensors to capture
	 * @see ScreenSensor#capture(boolean)
	 * @since 2.3
	 */
	private void readSensors(boolean read, List<ScreenSensor> list) {
		setStandByBorder();
		for (ScreenSensor ss : list) {
			ss.setBorder(read ? readingBorder : lookingBorder);
			ss.capture(read);
			if (ss.getOCRPerformanceTime() > 0) {
				if (ss.isCardArea()) {
					imageDiffereceTime.addValue(ss.getOCRPerformanceTime());
				} else {
					tesseractTime.addValue(ss.getOCRPerformanceTime());
				}
			}
		}
		setStandByBorder();
	}

	private void setStandByBorder() {
		screenSensors.values().stream().forEach(ss -> ss.setBorder(standByBorder));
	}

	/**
	 * Update the internal list of calls values. this list is used to calculate the correct pot value. The small and big
	 * blinds are updatet too. The small blind is determinated by the smaller value in the list, and the big blind is
	 * the next one.
	 * <p>
	 * NOTE: from this method point of view, the small/big blinds are the current detected smalles values. In some
	 * ocations, the gameplay is too agressive to detect the real values.
	 */
	private void updateCalls() {
		blinds.clear();
		int sb = pokerSimulator.getSmallBlind() == -1 ? Integer.MAX_VALUE : pokerSimulator.getSmallBlind();
		int bb = pokerSimulator.getBigBlind() == -1 ? Integer.MAX_VALUE : pokerSimulator.getBigBlind();
		List<ScreenSensor> calls = getSensors(".call");
		for (ScreenSensor ss : calls) {
			int intocr = ss.getIntOCR();
			// ignore errors or not available information
			if (intocr > 0) {
				sb = intocr < sb ? intocr : sb;
				bb = intocr < bb && intocr > sb ? intocr : bb;
				blinds.put(ss.getName(), intocr);
			}
		}
		// reset to -1 values in case of no one make a call
		sb = sb == Integer.MAX_VALUE ? -1 : sb;
		bb = bb == Integer.MAX_VALUE ? -1 : bb;
		pokerSimulator.setBlinds(sb, bb);
	}
	/**
	 * Update the table position. the Hero´s table position is determinated detecting the dealer button and counting
	 * clockwise. For examples, in a 4 villans table:
	 * <li>If hero has the dealer button, this method return 5;
	 * <li>if villan4 is the dealer, this method return 1. Hero is small blind
	 * <li>if villan1 is the dealer, this method return 4. Hero is in middle table position.
	 */
	private void updateTablePosition() {
		int dbp = getDealerButtonPosition();
		int tp = Math.abs(dbp - (getActiveSeats() + 1));
		pokerSimulator.setTablePosition(tp);
	}
	/**
	 * Create the array of sensors setted in the {@link ShapeAreas}.
	 * <p>
	 * dont use this method directly. use {@link Trooper#setEnviorement(DrawingPanel)}
	 * 
	 * @param areas - the enviorement
	 */
	protected void createSensorsArray(ShapeAreas areas) {
		this.screenAreas = areas;
		this.screenSensors.clear();
		this.blinds.clear();
		for (Shape shape : screenAreas.getShapes().values()) {
			ScreenSensor ss = new ScreenSensor(this, shape);
			screenSensors.put(ss.getName(), ss);
		}
		setStandByBorder();
		pokerSimulator.init();
	}

}
