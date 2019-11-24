package plugins.hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import com.alee.utils.*;

import core.*;
import gui.prueckl.draw.*;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.*;

/**
 * this class represent one area of vision that need to be read from the screen. Goup of instances of this class are
 * controled by {@link SensorsArray} class.
 * <p>
 * for every screen area exist some properties Properties:
 * <ul>
 * <li>bounds: x,y,with,heght: this property is used by {@link DrawingPanel} to set te bounds of the figure. this bound
 * is not stored in the propertys of the figure. is only for fine tunig during the edit proces
 * <li>color: figure line color.
 * </ul>
 * <li>rotate: indicate the rotation for this figure. this area will be rotate back (changin the rotation degree sign)
 * <li>triger.point = x,y: indicate a point that is used for
 * {@link TColorUtils#flood(BufferedImage, int, int, Color, Color, int)} operations. the triger property can be expresed
 * in direct coordenated input or -x,-y. the minus sign means that from the bound.width (right corner) minus x points,
 * or bound.hieght (right lower corner - y points before the reading process
 * 
 * </ul>
 * 
 * 
 * @author terry
 *
 */
public class ScreenSensor extends JPanel {

	public static String SAMPLE_PATH = "plugins/hero/samples/";
	private Shape shape;
	private List<Rectangle> regions;
	private SensorsArray sensorsArray;
	private int pageIteratorLevel = TessAPI.TessPageIteratorLevel.RIL_WORD;
	private int scaledWidth, scaledHeight;
	private boolean showOriginalImage;
	private Color maxColor;
	private double whitePercent;
	private BufferedImage preparedImage, capturedImage, lastOcrImage;
	private Exception exception;
	private String ocrResult;

	public ScreenSensor(SensorsArray sa, Shape sha) {
		super(new FlowLayout());
		this.sensorsArray = sa;
		this.shape = sha;
		setName(shape.name);
		this.scaledWidth = (int) (shape.bounds.width * 2.5);
		this.scaledHeight = (int) (shape.bounds.height * 2.5);

		showOriginal(true);
	}
	/**
	 * Compare the images <code>imagea</code> and <code>imageg</code> pixel by pixel returning the percentage of
	 * diference. If the images are equals, return values closer to 0.0, and for complety diferent images, return values
	 * closer to 100 percent
	 * <p>
	 * The <code>per</code> argument idicate the number of pixes to compare (expresed in percentage of the image data).
	 * e.g: per=50 idicate to this method compare the images using only 50% of the pixes in the image data. Those pixel
	 * are random selected.
	 * 
	 * @param imagea - firts image
	 * @param imageb - second image
	 * @param per - Percentages of pixel to compare.
	 * 
	 * @return percentaje of diference
	 */
	public static double getImageDiferences(BufferedImage imagea, BufferedImage imageb, int per) {
		long diference = 0;
		// long t1 = System.currentTimeMillis();
		if (imagea.getWidth() != imageb.getWidth() || imagea.getHeight() != imageb.getHeight()) {
			throw new IllegalArgumentException("images dimensions are not the same.");
		}

		int tot_width = per == 100 ? imagea.getWidth() : (int) (imagea.getWidth() * per / 100);
		int tot_height = per == 100 ? imagea.getHeight() : (int) (imagea.getHeight() * per / 100);

		for (int i = 0; i < tot_width; i++) {
			for (int j = 0; j < tot_height; j++) {
				int x = i;
				int y = j;
				if (per != 100) {
					Point rc = getRandCoordenates(imagea.getWidth(), imagea.getHeight());
					x = rc.x;
					y = rc.y;
				}
				int rgba = imagea.getRGB(x, y);
				int rgbb = imageb.getRGB(x, y);
				int reda = (rgba >> 16) & 0xff;
				int greena = (rgba >> 8) & 0xff;
				int bluea = (rgba) & 0xff;
				int redb = (rgbb >> 16) & 0xff;
				int greenb = (rgbb >> 8) & 0xff;
				int blueb = (rgbb) & 0xff;
				diference += Math.abs(reda - redb);
				diference += Math.abs(greena - greenb);
				diference += Math.abs(bluea - blueb);
			}
		}

		// total number of pixels (all 3 chanels)
		int total_pixel = tot_width * tot_height * 3;

		// normaliye the value of diferent pixel
		double avg_diff = diference / total_pixel;

		// percentage
		double percent = avg_diff / 255 * 100;
		// performanceLog("for total pixel = " + total_pixel + " at %=" + per, t1);
		return percent;
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
	 * Return the triger point coordenates setted for the figure asociated whiht this screen sensor. If no
	 * <code>triger.point</code> property is setted inthe figure, this method return <code>null</code>
	 * 
	 * @return coordinates or null
	 */
	// public static Point getTrigerPoint(Shape fig) {
	// String tpnt = fig.getProperties().getProperty("triger.point", "");
	// Rectangle r = fig.getBounds();
	// Point po = null;
	// if (!tpnt.equals("")) {
	// String[] xy = tpnt.split("[,]");
	// int x = Integer.parseInt(xy[0]);
	// int y = Integer.parseInt(xy[1]);
	// x = (x < 0) ? r.width + x : x;
	// y = (y < 0) ? r.height + y : y;
	// po = new Point(x, y);
	// }
	// return po;
	// }

	public static void logInfo(String txt) {
		// System.out.println("info:" + txt);
	}

	/**
	 * perform custom corrections. this metod is called during the OCR operation. the result returned from this method
	 * will be setted as final OCR of the {@link ScreenSensor} instance
	 * 
	 * @param ss - instance of {@link ScreenSensor} to fix the ocr
	 * 
	 * @return - new text
	 */
	public static String OCRCorrection(ScreenSensor ss) {
		String ocr = ss.getOCR();

		// for call/rise sensors,set the ocr only of the retrive numerical value
		if (ss.getName().equals("call") || ss.getName().equals("raise")) {
			String vals[] = ocr.split("\\n");
			ocr = "0";
			if (vals.length > 1) {
				ocr = replaceWhitNumbers(vals[1]);
			}
		}

		// standar procedure: remove all blanks caracters
		ocr = ocr.replaceAll("\\s", "");
		return ocr;
	}

	public static String replaceWhitNumbers(String srcs) {
		String rstr = srcs.replace("z", "2");
		rstr = rstr.replace("Z", "2");
		rstr = rstr.replace("o", "0");
		rstr = rstr.replace("O", "0");
		rstr = rstr.replace("s", "8");
		rstr = rstr.replace("S", "8");
		rstr = rstr.replace("U", "0");
		rstr = rstr.replace("u", "0");
		return rstr;
	}
	/**
	 * Capture the region of the screen specified by this sensor. this method generate and prepare the image for future
	 * operations.
	 * 
	 * @see #getCapturedImage()
	 * @se {@link #getPreparedImage()}
	 * 
	 */
	public void capture(boolean doocr) {
		Rectangle bou = shape.bounds;

		// capture the image
		if (Hero.trooper.isTestMode()) {
			// from the drawin panel background
			ImageIcon ii = sensorsArray.getSensorDisposition().getBackgroundImage();
			BufferedImage bgimage = ImageUtils.getBufferedImage(ii);
			capturedImage = bgimage.getSubimage(bou.x, bou.y, bou.width, bou.height);
		} else {
			// from the screen
			capturedImage = sensorsArray.getRobot().createScreenCapture(bou);
		}
		logInfo(getName() + ": Imgen captured.");

		/*
		 * color reducction or image treatment before OCR operation or enable/disable action
		 */
		prepareImage();

		/*
		 * by default an area is enabled if against a dark background, there is some white color. if the white color is
		 * over some %, the action is setted as enabled. use the property enable.when=% to set a diferent percentage
		 */
		setEnabled(false);
		// TODO: test performance changin prepared image for captured image because is smaller
		if (!(whitePercent > shape.enableWhen)) {
			logInfo(getName() + ": is disabled.");
			return;
		}
		setEnabled(true);

		/*
		 * Perform ocr operation. the ocr operation is performed only if the current captured image is diferent to the
		 * last imagen used for ocr operation. This avoid multiple ocr operations over the same image.
		 */
		if (doocr) {
			if ((lastOcrImage == null)
					|| (lastOcrImage != null && getImageDiferences(lastOcrImage, capturedImage, 100) > 0)) {
				doOCR();
				lastOcrImage = capturedImage;
			}
		}

		String ex = exception == null ? "" : exception.getMessage();
		String ch = getMaxColor();
		String st = "<html>Name: " + getName() + "<br>Enabled: " + isEnabled() + "<br>Exception: " + ex + "<br>W%: "
				+ whitePercent + "<br>maxC: <FONT COLOR=\"#" + ch + "\"><B>" + ch + "</B></FONT>" + "<br>isOCRArea: "
				+ isOCRArea() + "<br>OCR: " + ocrResult + "</html>";
		setToolTipText(st);
		repaint();
	}
	/**
	 * Return the image captureds by this sensor area.
	 * 
	 * @return the image
	 */
	public BufferedImage getCapturedImage() {
		return capturedImage;
	}

	public Exception getException() {
		return exception;
	}

	/**
	 * Retrun the optical caracter recognition extracted from the asociated area
	 * 
	 * @return OCR result
	 */
	public String getOCR() {
		return ocrResult;
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
		setToolTipText("");
		setEnabled(false);
		repaint();
	}

	/**
	 * return if this sensor is an action area
	 * 
	 * @return <code>true</code> or <code>false</code>
	 * @since 2.3
	 * @see Shape#isActionArea
	 */
	public boolean isActionArea() {
		return shape.isActionArea;
	}
	/**
	 * Return <code>true</code> if this area is a button area. the button area is the place where the button (flag) is
	 * placed to indicate that the player is big blind, small blind or dealer
	 * 
	 * @return true or false
	 */
	public boolean isButtonArea() {
		String sn = getName();
		return sn.contains("button");
	}
	/**
	 * Retrun <code>true</code> if this area represent a card area
	 * 
	 * @return true for card area
	 */
	public boolean isCardArea() {
		String sn = getName();
		return sn.startsWith("hero.card") || sn.startsWith("flop") || sn.equals("turn") || sn.equals("river")
				|| (sn.startsWith("villan") && sn.contains("card"));
	}
	public boolean isComunityCard() {
		String sn = getName();
		return sn.startsWith("flop") || sn.equals("turn") || sn.equals("river");
	}

	public boolean isHoleCard() {
		String sn = getName();
		return sn.startsWith("hero.card");
	}

	public boolean isOCRArea() {
		String sn = getName();
		return sn.equals("pot") || sn.equals("call") || sn.equals("raise")
				|| (sn.startsWith("villan") && sn.contains("name")) || (sn.startsWith("villan") && sn.contains("call"));
	}

	public boolean isVillanCard() {
		String sn = getName();
		return (sn.startsWith("villan") && sn.contains("card"));
	}

	/**
	 * Central method to get OCR operations. This method clear and re sets the ocr and exception variables according to
	 * the succed or failure of the ocr operation.
	 */
	private void doOCR() {
		long t1 = System.currentTimeMillis();
		regions = null;
		ocrResult = null;
		exception = null;
		try {
			if (isCardArea()) {
				ocrResult = getStringForCard();
			} else {
				ocrResult = getStringForAreas();
				Hero.logPerformance("For Tesseract areas", t1);
			}
		} catch (Exception e) {
			System.err.println("Exception on " + getName());
			e.printStackTrace();
		}
	}
	/**
	 * Perform tesseract ocr operation for generic areas.
	 * 
	 * @return the string recogniyed by tesseract
	 * 
	 * @throws TesseractException
	 */
	private String getStringForAreas() throws TesseractException {

		// is this an OCR area ?
		boolean doo = isOCRArea();
		if (!doo) {
			logInfo(getName() + ": no ocr performed. Porperty isOCRArea()=" + doo);
			return null;
		}

		logInfo(getName() + ": performing OCR...");
		regions = Hero.iTesseract.getSegmentedRegions(preparedImage, pageIteratorLevel);
		ocrResult = Hero.iTesseract.doOCR(preparedImage);
		List<Word> wlst = Hero.iTesseract.getWords(preparedImage, pageIteratorLevel);
		return OCRCorrection(this);
	}

	/**
	 * return the String representation of the card area by comparing the {@link ScreenSensor#getCapturedImage()} image
	 * against the list of card founded in the {@link #SAMPLE_PATH} enviorement variables. The most probable image file
	 * name is return.
	 * <p>
	 * This method is intendet for card areas. If the images diferences are hier than {@link #CARD_AREA_DIFERENCE}, the
	 * card area is treated as empty or face down card area and the returned value is <code>null</code>
	 * 
	 * @return the file name of the most probable card.
	 */
	private String getStringForCard() throws Exception {

		// must be a card area
		if (!isCardArea()) {
			throw new IllegalArgumentException("The screen sensor must be a card area sensor.");
		}

		// image comparation
		String ocr = null;
		double dif = 100.0;
		BufferedImage imagea = getCapturedImage();
		File dir = new File(SAMPLE_PATH);
		String[] imgs = dir.list();
		logInfo(getName() + ": Comparing images ...");
		for (String img : imgs) {
			File f = new File(SAMPLE_PATH + img);
			BufferedImage imageb = ImageIO.read(f);
			double s = ScreenSensor.getImageDiferences(imagea, imageb, 100);
			// System.out.println(ss.getSensorName() + "\\t" + f.getName() + "\\t" + s);
			if (s < dif) {
				dif = s;
				ocr = f.getName().split("[.]")[0];
			}
		}

		// if the card is the file name is card_facedown, set null for ocr
		if (ocr.equals("card_facedown")) {
			ocr = null;
			logInfo(getName() + ": card id face down.");
		}

		// if the card diference is most than 30%, its posible than some garbage is interfiring whit the screen capture.
		// in this case, the card area is marked as empty area. the average percentage of card similaryty is < 2%, so a
		// image diference over 30% is a complete diferent image.
		if (dif > 30) {
			ocr = null;
			logInfo(getName() + ": card diference of " + dif + "% dectected. setting this card area as null.");
		}

		return ocr;
	}
	//
	// private Color getTrigerPointColor(BufferedImage image) {
	// Point tp = ScreenSensor.getTrigerPoint(figure);
	// Color c = null;
	// if (tp != null) {
	// c = new Color(image.getRGB(tp.x, tp.y));
	// }
	// return c;
	// }
	//
	// private boolean isTrigerPointWhite(BufferedImage image) {
	// Color c = getTrigerPointColor(image);
	// boolean iw = false;
	// if (c != null) {
	// double de = TColorUtils.getColorDifference(c, Color.white);
	// iw = de <= 30;
	// }
	// return iw;
	// }

	/**
	 * perform image operation to set globals variables relatet whit the image previous to OCR, color count operations.
	 * acording to the tipe of area that this sensor represent, the underling image can be transformed in diferent ways.
	 * <p>
	 * This method set the {@link #preparedImage}
	 * 
	 */
	private void prepareImage() {

		BufferedImage bufimg = TColorUtils.convert4(capturedImage);
		this.maxColor = TColorUtils.getMaxColor(bufimg);

		// TODO: TEMPORAL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// TODO: TEMPORAL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (getName().equals("pot")) {
			bufimg = ImageHelper.convertImageToBinary(bufimg);
		}
		// TODO: TEMPORAL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		this.whitePercent = TColorUtils.getWhitePercent(bufimg);
		if (isButtonArea()) {

		}

		if (isOCRArea()) {
			BufferedImage escaled = ImageHelper.getScaledInstance(capturedImage, scaledWidth, scaledHeight);
			bufimg = ImageHelper.convertImageToGrayscale(escaled);
		}

		this.preparedImage = bufimg;
	}

	public String getMaxColor() {
		return TColorUtils.getRGBColor(maxColor);
	}
	/**
	 * set for this sensor that draw the original caputured image or the prepared image. this method affect only the
	 * visual representation of the component.
	 * 
	 * @param so - <code>true</code> to draw the original caputred image
	 */
	private void showOriginal(boolean so) {
		this.showOriginalImage = so;
		// show prepared or original
		if (so) {
			// plus 2 of image border
			setPreferredSize(new Dimension(shape.bounds.width + 2, shape.bounds.height + 2));
		} else {
			// plus 2 of image border
			setPreferredSize(new Dimension(scaledWidth + 2, scaledHeight + 2));
		}
	}

	/**
	 * Return the int value from this sensor. This method return <code>-1</code> if any error is found during the
	 * parsing operation.
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
			System.err.println("Error getting int value from " + getName() + ". The OCR is: " + ocr);
		}
		return val;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.gray.darker());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.black);
		g2d.drawLine(getWidth(), 0, 0, getHeight());
		g2d.drawLine(0, 0, getWidth(), getHeight());

		// original or prepared
		BufferedImage image = showOriginalImage ? capturedImage : preparedImage;
		g2d.drawImage(image, null, 0, 0);

		// draw segmented regions (only on prepared image)
		if (!showOriginalImage && preparedImage != null) {
			g2d.setColor(Color.BLUE);
			if (regions != null) {
				for (int i = 0; i < regions.size(); i++) {
					Rectangle region = regions.get(i);
					g2d.drawRect(region.x, region.y, region.width, region.height);
				}
			}
		}

		// simple light to represent the sensor status
		int or = getWidth() > 20 ? 12 : 6;
		Color ovlc = isEnabled() ? Color.GREEN : Color.GRAY;
		ovlc = exception == null ? ovlc : Color.RED;
		g2d.setColor(ovlc);
		g2d.fillOval(1, 1, or, or);
		g2d.setColor(Color.DARK_GRAY);
		g2d.drawOval(1, 1, or, or);

	}

}
