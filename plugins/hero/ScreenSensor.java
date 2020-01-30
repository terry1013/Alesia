package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.alee.utils.*;

import core.*;
import marvin.image.*;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.*;

/**
 * This class is a visual representation of the aread read from the screen. Group of this class are used and controled
 * by {@link SensorsArray} class. This class has a dual purpose:
 * <li>The main function is campture and process the image readed from the screen. the bounds, type of process need to
 * perform for this class, etc. come from the {@link Shape} class created based on the configuration file.
 * <li>this class is an of jpanel, so, this allow swing class use this class to build component that displain the
 * information captured and porccessed by this class.</li>
 * <p>
 * 
 * @author terry
 *
 */
public class ScreenSensor extends JPanel {

	public static String CAPTURED = "captured";
	public static String COLORED = "colored";
	public static String PREPARED = "prepared";

	private Hashtable<String, BufferedImage> images;
	private String showImage;

	public static String CARDS = "plugins/hero/cards/";
	public static TreeMap<String, BufferedImage> cardsTable = TCVUtils.loadCards(CARDS);
	private Shape shape;
	private SensorsArray sensorsArray;
	private int scaledWidth, scaledHeight;
	private Color backgroundColor;
	private double colorPercent;
	private BufferedImage preparedImage, capturedImage, lastOcrImage;
	private JLabel dataLabel;
	private JLabel imageLabel;
	private String ocrResult;
	private Tesseract iTesseract;
	private int ocrTime = -1;

	public ScreenSensor(SensorsArray sa, Shape sha) {
		super(new BorderLayout());
		this.images = new Hashtable<>();
		this.sensorsArray = sa;
		this.shape = sha;
		this.imageLabel = new JLabel();
		this.dataLabel = new JLabel();
		dataLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		setName(shape.name);

		Dimension sd = TCVUtils.getScaledDimension(shape.bounds.width, shape.bounds.height);
		this.scaledWidth = sd.width;
		this.scaledHeight = sd.height;

		showImage(CAPTURED);

		// standar: image at left, data at center
		// if ratio is > 2. the component aling are vertical (image at top, data at center)
		double ratio = (float) shape.bounds.width / (float) shape.bounds.height;
		add(imageLabel, ratio > 2 ? BorderLayout.NORTH : BorderLayout.WEST);
		add(dataLabel, BorderLayout.CENTER);

		this.iTesseract = Hero.geTesseract();
		init();
		update();
	}

	@Deprecated
	public static String getOCRFromImage2(String sName, BufferedImage imagea, TreeMap<String, String> imageHashes) {
		String s1 = TCVUtils.imagePHash(imagea, null);
		double minDist = 21;
		String ocr = null;
		int dist = Integer.MAX_VALUE;
		Set<String> keys = imageHashes.keySet();
		for (String key : keys) {
			int d = TCVUtils.getHammingDistance(s1, imageHashes.get(key));
			Hero.logger.finer("file name: " + key + "Distance: " + d);
			if (d < dist) {
				dist = d;
				ocr = key;
			}
		}
		Hero.logger.finer("getOCRFromImage for sensor" + sName + ": image " + ocr + " found. Distance: " + dist);
		return ocr == null || dist > minDist ? null : ocr;
	}

	// public static double getImageDiferences(List<MarvinSegment> segments, BufferedImage imagea, BufferedImage imageb)
	// {
	// double dif = 0.0;
	//
	// // at this point i.m asumming that segments are retrived in the same way for all images. so, similar images
	// // haben the same segment order inside the list and ovious the same number of segments
	//
	// // segments from imageb
	// MarvinImage miB = new MarvinImage(imageb);
	// List<MarvinSegment> segmentsB = TCVUtils.getImageSegments(miB, false, null);
	//
	// // nto the same numbers of segments, bust be diferent images
	// if (segments.size() != segmentsB.size())
	// return 100.0;
	//
	// for (int i = 0; i < segments.size(); i++) {
	// MarvinSegment segA = segments.get(i);
	// MarvinSegment segB = segmentsB.get(i);
	// BufferedImage subA = imagea.getSubimage(segA.x1, segA.y1, segA.width, segA.height);
	// BufferedImage subB = imageb.getSubimage(segB.x1, segB.y1, segB.width, segB.height);
	// dif += TCVUtils.getImageDiferences(subA, subB);
	// }
	// return dif;
	// }

	private String getOCRFromImage(BufferedImage imagea, TreeMap<String, BufferedImage> images) {
		String ocr = null;
		double dif = 100.0;
		double difThreshold = 30.0;

		Set<String> names = images.keySet();
		for (String name : names) {
			BufferedImage imageb = images.get(name);
			double s = TCVUtils.getImageDiferences(imagea, imageb, true);
			Hero.logger.finer("file name: " + name + " Diference: " + s);
			if (s < dif) {
				dif = s;
				ocr = name;
			}
		}
		if (dif > difThreshold)
			Hero.logger.finer("OCR not found for sensor " + getName() + ". min diference: " + dif);
		else
			Hero.logger.finer("OCR for sensor " + getName() + ": " + ocr + " found. Diference: " + dif);

		return ocr == null || dif > difThreshold ? null : ocr;
	}

	/**
	 * Return a random {@link Point} selectd inside of the area (0,width) (0,height)
	 * 
	 * @param width - width of the area
	 * @param height - height of the area
	 * @return a random point inside area
	 */
	public static Point getRandCoordenates(int width, int height) {
		int x = (int) Math.random() * width;
		int y = (int) Math.random() * height;
		return new Point(x, y);
	}

	/**
	 * Replage the incomming argumento, which is spected to be only number, with the know replacemente due to tesseract
	 * caracter bad recognition. for example, is know that ocr operation detect 800 as a00 or 80o. this method will
	 * return 800 for thath incommin value
	 * 
	 * @param ocrString - posible nomeric value with leters
	 * @return string only numeric
	 */
	private static String replaceWhitNumbers(String ocrString) {
		String rstr = ocrString.replace("z", "2");
		rstr = rstr.replace("Z", "2");
		rstr = rstr.replace("o", "0");
		rstr = rstr.replace("O", "0");
		rstr = rstr.replace("a", "8");
		rstr = rstr.replace("s", "8");
		rstr = rstr.replace("S", "8");
		rstr = rstr.replace("U", "0");
		rstr = rstr.replace("u", "0");
		rstr = rstr.replace("e", "6");
		return rstr;
	}

	/**
	 * Capture the region of the screen specified by this sensor. this method is executed at diferent levels acording to
	 * the retrived information from the screen. The <code>doOcr</code> argument idicate the desire for retrive ocr from
	 * the image. the ocr will retrive if this argument is <code>true</code> and a diference between the las image and
	 * the actual image has been detected.
	 * <li>prepare the image
	 * <li>set the status enabled/disabled for this sensor if the image is considerer enabled. if this sensor is setted
	 * to disable, no more futher operations will be performed.
	 * <li>perform de asociated OCR operation according to this area type ONLY IF the image has change.
	 * 
	 * @see #getCapturedImage()
	 * 
	 * @param doOcr - <code>true</code> for perform ocr operation (if is available)
	 */
	public void capture(boolean doOcr) {
		Rectangle bou = shape.bounds;
		long t1 = System.currentTimeMillis();

		// capture the image
		if (Trooper.getInstance().isTestMode()) {
			// from the ppt file background
			ImageIcon ii = sensorsArray.getSensorDisposition().getBackgroundImage();
			BufferedImage bgimage = ImageUtils.getBufferedImage(ii);
			capturedImage = bgimage.getSubimage(bou.x, bou.y, bou.width, bou.height);
		} else {
			// from the screen
			capturedImage = sensorsArray.getRobot().createScreenCapture(bou);
		}

		/*
		 * color reducction and image treatment before OCR operation or enable/disable action: mandatory for all areas
		 */
		prepareImage();

		/*
		 * by default an area is enabled if against a dark background, there is some activation color. if the white
		 * color is over some %, the action is setted as enabled. use the property enable.when=% to set a diferent
		 * percentage
		 */
		setEnabled(false);
		if (!(colorPercent > 1)) {
			// if a sensor is disabled, his ocr is null by default. this avoid previous ocr values if the sensor was
			// enabled before
			ocrResult = null;
			update();
			return;
		}
		setEnabled(true);

		// TODO: IMPLEMEEEEEEETTTTTTTTTTTTTTTTTTTTTTTTTTTT
		if (doOcr) {
			double imgdif = 100.0;
			if (lastOcrImage != null)
				imgdif = TCVUtils.getImageDiferences(lastOcrImage, capturedImage, false);
			if ((lastOcrImage == null) || imgdif > 0 || ocrResult == null) {
				doOCR();
				lastOcrImage = capturedImage;
			}
		}
		update();
		ocrTime = (int) (System.currentTimeMillis() - t1);
	}

	public BufferedImage getImage(String type) {
		return images.get(type);
	}

	/**
	 * Return the int value from this sensor. Some sensor has only numerical information or text/numerical information.
	 * acording to this, this method will return that numerical information (if is available) or -1 if not. Also, -1 is
	 * returned if any error is found during the parsing operation.
	 * 
	 * @see #OCRCorrection(ScreenSensor)
	 * 
	 * @return int value or <code>-1</code>
	 */
	public double getNumericOCR() {
		String ocr = getOCR();
		double val = -1;
		try {
			if (ocr != null) {
				val = Double.parseDouble(ocr);
			}
		} catch (Exception e) {
			Hero.logger.severe(getName() + ": Fail getting double value. The OCR is: " + ocr);
		}
		return val;
	}
	/**
	 * Return the string representation of the {@link #backgroundColor} variable. the format is RRGGBB
	 * 
	 * @return
	 */
	public String getMaxColor() {
		return TColorUtils.getRGBColor(backgroundColor);
	}
	/**
	 * Retrun the optical caracter recognition extracted from the asociated area
	 * 
	 * @return OCR result
	 */
	public String getOCR() {
		return ocrResult;
	}

	public long getOCRPerformanceTime() {
		return ocrTime;
	}

	/**
	 * init this sensor variables. use this method to clean for a fresh start
	 * 
	 */
	public void init() {
		ocrResult = null;
		preparedImage = null;
		capturedImage = null;
		lastOcrImage = null;
		// TODO: put somethin to difierentiate the init status form others status
		imageLabel.setIcon(null);
		setEnabled(false);
		repaint();
	}

	public boolean isActionArea() {
		return shape.isActionArea;
	}

	public boolean isCardArea() {
		return shape.isCardArea;
	}

	/**
	 * Return <code>true</code> if this sensor is a comunity card sensor
	 * 
	 * @return <code>true</code> if i.m a community card sensor
	 * @since 2.3
	 */
	public boolean isComunityCard() {
		String sn = getName();
		return sn.startsWith("flop") || sn.equals("turn") || sn.equals("river");
	}

	/**
	 * Return <code>true</code> if this sensor is a hole card sensor
	 * 
	 * @return <code>true</code> if i.m a hole card sensor
	 * @since 2.3
	 */
	public boolean isHoleCard() {
		String sn = getName();
		return sn.startsWith("hero.card");
	}

	public boolean isNumericArea() {
		return shape.isOCRNumericArea;
	}
	public boolean isTextArea() {
		return shape.isOCRTextArea;
	}

	/**
	 * set for this sensor that draw the original caputured image or the prepared image. this method affect only the
	 * visual representation of the component.
	 * <p>
	 * WARNING: displaying prepared images will invoke more method on the {@link Tesseract} OCR api engine. this will
	 * decrease the system performance 4x.
	 * 
	 * @param so - <code>true</code> to draw the original caputred image
	 */
	public void showImage(String type) {
		this.showImage = type;
		// BufferedImage img = images.get(showImage);
		// plus 2 of image border
		Dimension dim = new Dimension(shape.bounds.width + 2, shape.bounds.height + 2);
		// for prepared images
		if (showImage.equals(PREPARED))
			dim = new Dimension(scaledWidth + 2, scaledHeight + 2);
		// if (img != null)
		// dim = new Dimension(img.getWidth() + 2, img.getHeight() + 2);
		imageLabel.setPreferredSize(dim);

	}

	/**
	 * Central method to get OCR operations. This method clear and re sets the ocr and exception variables according to
	 * the succed or failure of the ocr operation.
	 */
	private void doOCR() {
		ocrResult = null;
		try {
			if (isCardArea())
				ocrResult = getImageOCR();
			if (isTextArea() || isNumericArea())
				ocrResult = getTesseractOCR();

		} catch (Exception e) {
			Hero.logger.severe(getName() + ": Fail trying doOCR " + e);
		}
	}

	/**
	 * return the String representation of the card area by comparing the {@link ScreenSensor#getCapturedImage()} image
	 * against the list of card loaded in {@link #cardsTable} static variable. The most probable image file name is
	 * return.
	 * <p>
	 * This method is intendet for card areas. a card area is practicaly equals if the diference is < 3%. This method
	 * will return <code>null</code> if the captured image is less than 50% of the original shape dimensions
	 * 
	 * @return the ocr retrived from the original file name or <code>null</code>
	 */
	private String getImageOCR() throws Exception {
		String ocr = getOCRFromImage(preparedImage, cardsTable);

		// only for visual purpose. the regions of interest was already used
		if (showImage.equals(PREPARED)) {
			Properties parms = new Properties();
			parms.put("rgbToBinaryThreshold", "200");
			parms.put("removeSegmentsWindowSize", "0");
			MarvinImage mi = new MarvinImage(preparedImage);
			TCVUtils.getImageSegments(mi, true, parms);
		}
		//
		// // if the card is the file name is card_facedown, set null for ocr
		// if (ocr != null && ocr.equals("xx")) {
		// ocr = null;
		// Hero.logger.finer(getName() + ": card is face down.");
		// }
		return ocr;
	}
	/**
	 * Perform tesseract ocr operation for generic areas.
	 * 
	 * @return the string recogniyed by tesseract
	 * 
	 * @throws TesseractException
	 */
	private String getTesseractOCR() throws TesseractException {
		String srcocr = iTesseract.doOCR(preparedImage);

		// draw segmented regions (only on prepared image) and ONLY when the prepared image is request to be visible
		if (showImage.equals(PREPARED) && preparedImage != null) {
			int pageIteratorLevel = TessAPI.TessPageIteratorLevel.RIL_WORD;
			// List<Word> wlst = Hero.iTesseract.getWords(preparedImage, pageIteratorLevel);
			List<Rectangle> regions = iTesseract.getSegmentedRegions(preparedImage, pageIteratorLevel);
			Graphics2D g2d = (Graphics2D) preparedImage.getGraphics();
			g2d.setColor(Color.BLUE);
			if (regions != null) {
				for (int i = 0; i < regions.size(); i++) {
					Rectangle region = regions.get(i);
					g2d.drawRect(region.x, region.y, region.width, region.height);
				}
			}
			// Hero.logger.finer(getName() + ": list of words: " + wlst);
			// Hero.logger.finer(getName() + ": Tesseract OCR performed. Regions: " + regions.size() + " OCR=" +
			// srcocr);
		}
		Hero.logger.finer(getName() + ": Tesseract OCR performed. Raw OCR whitout correction=" + srcocr);
		return OCRCorrection(srcocr);
	}
	/**
	 * perform custom corrections accordint of the name or type of sensor. for example, for call sensor is spected this
	 * correction retrive only the numeric value from the second line, ignoring the "call" text
	 * 
	 */
	private String OCRCorrection(String srcocd) {

		// cards sensors
		if (isCardArea()) {
			// srcocd = srcocd.replaceAll("\\s", "");
			// // checq the last character looking for the suit.
			// String tmp = srcocd.substring(srcocd.length() - 1, srcocd.length());
			// String suit = "c";
			// suit = tmp.equals("V") ? "h" : suit;
			// suit = tmp.equals("Q") ? "s" : suit;
			// suit = tmp.equals("0") ? "d" : suit;
			// String rank = srcocd.substring(0, 1);
			// rank = rank.equals("1") ? "10" : rank;
			// srcocd = rank + suit;
		}

		// standar procedure for numeric sensors
		if (isNumericArea()) {
			srcocd = srcocd.replaceAll("[^.1234567890]", "");
			// srcocd = replaceWhitNumbers(srcocd);
		}

		// standar procedure: remove all blanks caracters
		srcocd = srcocd.replaceAll("\\s", "");
		return srcocd;
	}

	/**
	 * perform image operation to set globals variables relatet with the image previous to OCR, color count operations.
	 * acording to the type of area that this sensor represent, the underling image can be transformed in diferent ways.
	 * <p>
	 * This method set the {@link #preparedImage}, {@link #backgroundColor} and {@link #whitePercent} global variables.
	 * 
	 */
	private void prepareImage() {
		// update global variable. this step is mandatory for all areas because maxcolor and white percent affect the
		// flow of the entire class
		BufferedImage bufimg = TColorUtils.convert3(capturedImage);
		Hashtable<String, Integer> histo = TColorUtils.getHistogram(bufimg);
		this.backgroundColor = TColorUtils.getBackgroundColor(histo);
		this.colorPercent = TColorUtils.getColorPercent(histo, shape.enableColor,
				bufimg.getWidth() * bufimg.getHeight());
		images.put(COLORED, bufimg);
		images.put(CAPTURED, capturedImage);

		if (isCardArea()) {
			bufimg = TCVUtils.prepareCard(capturedImage, false);
		}

		// all ocr areas need scaled instance
		if (isTextArea() || isNumericArea()) {
			bufimg = ImageHelper.getScaledInstance(capturedImage, scaledWidth, scaledHeight);
			bufimg = ImageHelper.convertImageToGrayscale(bufimg);

		}
		images.put(PREPARED, bufimg);
		this.preparedImage = bufimg;
	}

	/**
	 * update the asociatet UI components with the internal values from this sensor.
	 */
	private void update() {
		BufferedImage sel = images.get(showImage);
		// at init time, sel image can be null
		if (sel != null)
			imageLabel.setIcon(new ImageIcon(sel));

		String maxc = backgroundColor == null ? "null" : TColorUtils.colorNames.get(backgroundColor);
		String secol = TColorUtils.colorNames.get(shape.enableColor);
		String ecol = isEnabled() ? "GREEN" : "GRAY";
		String etex = isEnabled() ? "Enabled" : "Disabled";
		String elin = "<FONT COLOR=" + ecol + ">" + etex + "</FONT>";

		// + "<br>Max color: <FONT style= \"background-color: #"+maxc +"\"><B>" + maxc + "</B></FONT>" + "<br>OCR: " +
		// ocrResult
		String text = "<html>" + getName() + "  " + elin + "<br>Enable: " + secol + " " + colorPercent
				+ "<br>Background: <B style= \"color: #" + maxc + "\">" + maxc + "</B>" + "<br>OCR: " + ocrResult
				+ "</html>";
		dataLabel.setText(text);
	}
}
