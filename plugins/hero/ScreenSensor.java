package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import com.alee.utils.*;

import core.*;
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

	public static String IMAGE_CARDS = "plugins/hero/image_cards/";
	private static Hashtable<String, BufferedImage> cardsTable = ScreenSensor.loadImages(IMAGE_CARDS);
	private Shape shape;
	private SensorsArray sensorsArray;
	private int scaledWidth, scaledHeight;
	private boolean showCapturedImage;
	private Color maxColor;
	private double whitePercent;
	private BufferedImage preparedImage, capturedImage;
	private Exception exception;
	private JLabel dataLabel;
	private JLabel imageLabel;
	private String ocrResult;

	private int ocrTime = -1;

	public ScreenSensor(SensorsArray sa, Shape sha) {
		super(new BorderLayout());
		this.sensorsArray = sa;
		this.shape = sha;
		this.imageLabel = new JLabel();
		this.dataLabel = new JLabel();
		dataLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		setName(shape.name);

		Dimension sd = TCVUtils.getScaledDimension(shape.bounds.width, shape.bounds.height);
		this.scaledWidth = sd.width;
		this.scaledHeight = sd.height;

		showCapturedImage(true);

		// standar: image at left, data at center
		// if ratio is > 2. the component aling are vertical (image at top, data at center)
		double ratio = (float) shape.bounds.width / (float) shape.bounds.height;
		add(imageLabel, ratio > 2 ? BorderLayout.NORTH : BorderLayout.WEST);
		add(dataLabel, BorderLayout.CENTER);
		update();
	}

	public static double getImageDiferences2(BufferedImage imagea, BufferedImage imageb) {

		Hashtable<Integer, Integer> ha = TColorUtils.getHistogram(TColorUtils.convert4(imagea));
		Hashtable<Integer, Integer> hb = TColorUtils.getHistogram(TColorUtils.convert4(imageb));

		Set<Integer> keys = ha.size() > hb.size() ? ha.keySet() : hb.keySet();
		double diference = 0.0;
		for (Integer key : keys) {
			int ia = ha.get(key) != null ? ha.get(key).intValue() : 0;
			int ib = hb.get(key) != null ? hb.get(key).intValue() : 0;
			diference += Math.abs(ia - ib);
		}

//		int total_pixel = tot_width * tot_height;
//		double avg_diff = diference / total_pixel;
//		double percent = avg_diff * 100;
//		return percent;
		return diference;
	}

	/**
	 * Compare the images <code>imagea</code> and <code>imageg</code> pixel by pixel returning the percentage of
	 * diference. If the images are equals, return values closer to 0.0, and for complety diferent images, return values
	 * closer to 100 percent.
	 * <p>
	 * this function accept diferent sizes from imagea and b. in this case, this function compare only the common area.
	 * that is, starting from (0,0) until the dimension of the smaller image
	 * 
	 * @see TColorUtils#getRGBColorDistance(Color, Color)
	 * 
	 * @param imagea - firts image
	 * @param imageb - second image
	 * 
	 * @return percentaje of diference
	 */
	public static double getImageDiferences(BufferedImage imagea, BufferedImage imageb) {
		double diference = 0;

		int tot_width = imagea.getWidth() < imageb.getWidth() ? imagea.getWidth() : imageb.getWidth();
		int tot_height = imagea.getHeight() < imageb.getHeight() ? imagea.getHeight() : imageb.getHeight();

		for (int x = 0; x < tot_width; x++) {
			for (int y = 0; y < tot_height; y++) {
				int rgba = imagea.getRGB(x, y);
				int rgbb = imageb.getRGB(x, y);
				diference += TColorUtils.getRGBColorDistance(new Color(rgba), new Color(rgbb));
			}
		}

		// total number of pixels
		int total_pixel = tot_width * tot_height;
		// normaliye the value of diferent pixel
		double avg_diff = diference / total_pixel;
		// percentage
		double percent = avg_diff * 100;
		return percent;
	}

	/**
	 * utilice the {@link ScreenSensor#getImageDiferences2(BufferedImage, BufferedImage)} to compare the
	 * <code>imagea</code> argument against the list of images <code>images</code>. the name of the image with less
	 * diference is returned.
	 * <p>
	 * This method return <code>null</code> if no image are found or the diference bettwen images > diferenceThreshold.
	 * the difference threshold for this method is 30%. The average image similarity (for card areas is 2%)
	 * 
	 * @see ScreenSensor#getImageDiferences2(BufferedImage, BufferedImage)
	 * @param imagea - image to compare
	 * @param images - list of candidates.
	 * @return a {@link TEntry} where the key is the ocr from the image and the dif is the diference betwen the imagea
	 *         argument and the most probable image founded.
	 */
	public static String getOCRFromImage(BufferedImage imagea, Hashtable<String, BufferedImage> images) {
		String ocr = null;
		double dif = 100.0;
		double difThreshold = 30.0;
		ArrayList<String> names = new ArrayList<>(images.keySet());
		for (String name : names) {
			BufferedImage imageb = images.get(name);
			double s = ScreenSensor.getImageDiferences(imagea, imageb);
			Hero.logger.finer("file name: " + name + " Diference: " + s);
			if (s < dif) {
				dif = s;
				ocr = name;
			}
		}
		Hero.logger.finer("getOCRFromImage: image " + ocr + " found. Diference: " + dif);
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
	 * Load all images located in the <code>dir</code> parameter and create a {@link Hashtable} where the key is the
	 * file name (call, Ks, Etc.) and the value parameter is the {@link BufferedImage} loaded from that file
	 * 
	 * @param dir - folder source of the images
	 * 
	 * @return table of filename and image
	 */
	public static Hashtable<String, BufferedImage> loadImages(String dir) {
		File fdir = new File(dir);
		String[] imgs = fdir.list();
		Hero.logger.config("Loading images from " + dir);
		Hashtable<String, BufferedImage> images = new Hashtable<>();
		for (String img : imgs) {
			File f = new File(dir + img);
			BufferedImage imageb;
			try {
				imageb = ImageIO.read(f);
				String inam = f.getName().split("[.]")[0];
				BufferedImage old = images.put(inam, imageb);
				if (old != null) {
					Hero.logger.warning("image file name " + inam + "duplicated.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images;
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
		 * by default an area is enabled if against a dark background, there is some white color. if the white color is
		 * over some %, the action is setted as enabled. use the property enable.when=% to set a diferent percentage
		 */
		setEnabled(false);
		if (!(whitePercent > shape.enableWhen)) {
			update();
			return;
		}
		setEnabled(true);

		if (doOcr) {
			// if ((lastOcrImage == null)
			// || (lastOcrImage != null && getImageDiferences(lastOcrImage, capturedImage, 100) > 0)) {
			doOCR();
			// lastOcrImage = capturedImage;
			// }
		}
		update();
	}

	/**
	 * Return the image captureds by this sensor area. The captured image is the exact image retribed from the
	 * enviorement without any treatment
	 * 
	 * @return the captured image
	 */
	public BufferedImage getCapturedImage() {
		return capturedImage;
	}

	public Exception getException() {
		return exception;
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
	public int getIntOCR() {
		String ocr = getOCR();
		int val = -1;
		try {
			if (ocr != null) {
				val = Integer.parseInt(ocr);
			}
		} catch (Exception e) {
			Hero.logger.severe(getName() + ": Fail getting int value. The OCR is: " + ocr);
		}
		return val;
	}
	/**
	 * Return the string representation of the {@link #maxColor} variable. the format is RRGGBB
	 * 
	 * @return
	 */
	public String getMaxColor() {
		return TColorUtils.getRGBColor(maxColor);
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

	public BufferedImage getPreparedImage() {
		return preparedImage;
	}

	/**
	 * init this sensor variables. use this method to clean for a fresh start
	 * 
	 */
	public void init() {
		exception = null;
		ocrResult = null;
		preparedImage = null;
		capturedImage = null;
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
	public void showCapturedImage(boolean so) {
		this.showCapturedImage = so;
		// show prepared or original
		if (so) {
			// plus 2 of image border
			imageLabel.setPreferredSize(new Dimension(shape.bounds.width + 2, shape.bounds.height + 2));
		} else {
			// plus 2 of image border
			imageLabel.setPreferredSize(new Dimension(scaledWidth + 2, scaledHeight + 2));
		}
	}

	/**
	 * Central method to get OCR operations. This method clear and re sets the ocr and exception variables according to
	 * the succed or failure of the ocr operation.
	 */
	private void doOCR() {
		long t1 = System.currentTimeMillis();
		ocrResult = null;
		exception = null;
		try {
			if (isCardArea()) {
				ocrResult = getImageOCR();
			} else {
				ocrResult = getTesseractOCR();
			}
		} catch (Exception e) {
			Hero.logger.severe(getName() + ": Fail trying doOCR " + e);
		}
		ocrTime = (int) (System.currentTimeMillis() - t1);
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
		BufferedImage imagea = preparedImage;
		// fail safe. if the food function cant detetct enought area is because the card is face down or due noise in
		// the table. return null if the captured area is lest than the half of the original shape area
		int imagearea = imagea.getWidth() * imagea.getHeight();
		int shapearea = shape.bounds.width * shape.bounds.height;
		if (imagearea * 2 < shapearea) {
			Hero.logger.finer(getName() + ": Not enought area. OCR set to null");
			return null;
		}
		String ocr = getOCRFromImage(imagea, cardsTable);

		// if the card is the file name is card_facedown, set null for ocr
		if (ocr != null && ocr.equals("xx")) {
			ocr = null;
			Hero.logger.finer(getName() + ": card is face down.");
		}
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
		String srcocr = Hero.iTesseract.doOCR(preparedImage);

		// draw segmented regions (only on prepared image) and ONLY when the prepared image is request to be visible
		if (!showCapturedImage && preparedImage != null) {
			int pageIteratorLevel = TessAPI.TessPageIteratorLevel.RIL_WORD;
			// List<Word> wlst = Hero.iTesseract.getWords(preparedImage, pageIteratorLevel);
			List<Rectangle> regions = Hero.iTesseract.getSegmentedRegions(preparedImage, pageIteratorLevel);
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
//			srcocd = srcocd.replaceAll("\\s", "");
//			// checq the last character looking for the suit.
//			String tmp = srcocd.substring(srcocd.length() - 1, srcocd.length());
//			String suit = "c";
//			suit = tmp.equals("V") ? "h" : suit;
//			suit = tmp.equals("Q") ? "s" : suit;
//			suit = tmp.equals("0") ? "d" : suit;
//			String rank = srcocd.substring(0, 1);
//			rank = rank.equals("1") ? "10" : rank;
//			srcocd = rank + suit;
		}
		// call sensors
		if (TStringUtils.wildCardMacher(getName(), "*.call")) {
			srcocd = srcocd.replaceAll("\\s", "");
			srcocd = replaceWhitNumbers(srcocd);
		}

		// for call/rise sensors,set the ocr only of the retrive numerical value
		if (getName().equals("call") || getName().equals("raise")) {
			String vals[] = srcocd.split("\\n");
			srcocd = "0";
			if (vals.length > 1) {
				srcocd = replaceWhitNumbers(vals[1]);
			}
		}

		// standar procedure: remove all blanks caracters
		srcocd = srcocd.replaceAll("\\s", "");
		return srcocd;
	}

	/**
	 * perform image operation to set globals variables relatet with the image previous to OCR, color count operations.
	 * acording to the type of area that this sensor represent, the underling image can be transformed in diferent ways.
	 * <p>
	 * This method set the {@link #preparedImage}, {@link #maxColor} and {@link #whitePercent} global variables.
	 * 
	 */
	private void prepareImage() {

		// update global variable. this step is mandatory for all areas because maxcolor and white percent affect the
		// flow of the entire class
		BufferedImage bufimg = TColorUtils.convert4(capturedImage);
		Hashtable<Integer, Integer> histo = TColorUtils.getHistogram(bufimg);
		this.maxColor = TColorUtils.getMaxColor(histo);
		this.whitePercent = TColorUtils.getWhitePercent(histo, bufimg.getWidth(), bufimg.getHeight());

		if (isCardArea()) {
			bufimg = TColorUtils.getImageDataRegion(capturedImage);
//			bufimg = ImageHelper.getScaledInstance(bufimg, scaledWidth, scaledHeight);
		}

		// TODO: TEMPORAL jjust for whitePercent variable
		// TODO: the pot font is so thing that stardar imagen treat don.t detect white pixels
		if (getName().equals("pot")) {
			bufimg = ImageHelper.convertImageToBinary(bufimg);
			this.whitePercent = TColorUtils.getWhitePercent(bufimg);
		}

		// all ocr areas need scaled instance
		if (isTextArea() || isNumericArea()) {
			bufimg = ImageHelper.getScaledInstance(capturedImage, scaledWidth, scaledHeight);
		}

		this.preparedImage = bufimg;
	}

	/**
	 * update the asociatet UI components with the internal values from this sensor.
	 */
	private void update() {
		BufferedImage sel = showCapturedImage ? capturedImage : preparedImage;
		// at init time, sel image can be null
		if (sel != null)
			imageLabel.setIcon(new ImageIcon(sel));
		String maxc = maxColor == null ? "000000" : getMaxColor();

		String ecol = isEnabled() ? "GREEN" : "GRAY";
		String etex = isEnabled() ? "Enabled" : "Disabled";
		String elin = "<FONT COLOR=" + ecol + "\">" + etex + "</FONT>";

		String text = "<html><B>" + getName() + "</B>  " + elin + "<br>White %: " + whitePercent
		// + "<br>Max color: <FONT style= \"background-color: #"+maxc +"\"><B>" + maxc + "</B></FONT>" + "<br>OCR: " +
		// ocrResult
				+ "<br>Max color: <B style= \"color: #" + maxc + "\">" + maxc + "</B>" + "<br>OCR: " + ocrResult
				+ "</html>";
		dataLabel.setText(text);
	}
}
