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

	public static String IMAGE_CARDS = "plugins/hero/image_cards/";
	private static Hashtable<String, BufferedImage> cardsTable = ScreenSensor.loadImages(IMAGE_CARDS);
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

	private int ocrTime = -1;

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
		// compare image sizes. if the images are not the same dimension. create a scaled instance of the biggert
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
	 * utilice the {@link ScreenSensor#getImageDiferences(BufferedImage, BufferedImage, int)} to compare the
	 * <code>imagea</code> argument against the list of images <code>images</code>. the name of the image with less
	 * diference is returned.
	 * <p>
	 * This method return <code>null</code> if no image are found or the diference bettwen images > diferenceThreshold.
	 * the difference threshold for this method is 30%. The average image similarity (for card areas is 2%)
	 * 
	 * @see ScreenSensor#getImageDiferences(BufferedImage, BufferedImage, int)
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
			double s = ScreenSensor.getImageDiferences(imagea, imageb, 100);
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
	 * perform custom corrections. this metod is called during the OCR operation. the result returned from this method
	 * will be setted as final OCR of the {@link ScreenSensor} instance
	 * 
	 * @param ss - instance of {@link ScreenSensor} to fix the ocr
	 * @see #replaceWhitNumbers(String)
	 * @return - new text
	 */
	public static String OCRCorrection(ScreenSensor ss) {
		String ocr = ss.getOCR();

		// for call/rise sensors,set the ocr only of the retrive numerical value
		// for villanX.call perform corrections too
		if (ss.getName().equals("call") || ss.getName().equals("raise")
				|| (ss.getName().startsWith("villan") && ss.getName().contains(".call"))) {
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

	/**
	 * Replage the incomming argumento, which is spected to be only number, with the know replacemente due to tesseract
	 * caracter bad recognition. for example, is know that ocr operation detect 800 as a00 or 80o. this method will
	 * return 800 for thath incommin value
	 * 
	 * @param ocrString - posible nomeric value with leters
	 * @return string only numeric
	 */
	public static String replaceWhitNumbers(String ocrString) {
		String rstr = ocrString.replace("z", "2");
		rstr = rstr.replace("Z", "2");
		rstr = rstr.replace("o", "0");
		rstr = rstr.replace("O", "0");
		rstr = rstr.replace("a", "8");
		rstr = rstr.replace("s", "8");
		rstr = rstr.replace("S", "8");
		rstr = rstr.replace("U", "0");
		rstr = rstr.replace("u", "0");
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

		Hero.logger.finer(getName() + ": Imgen captured.");

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
			Hero.logger.finer(getName() + ": is disabled.");
			return;
		}
		setEnabled(true);

		/*
		 * Perform ocr operation. the ocr operation is performed only if the current captured image is diferent to the
		 * last imagen used for ocr operation. This avoid multiple ocr operations over the same image.
		 */
		if (doOcr) {
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
				+ shape.isOCRArea + "<br>OCR: " + ocrResult + "</html>";
		setToolTipText(st);
		repaint();
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
			Hero.logger.severe(getName() + "Error getting int value. The OCR is: " + ocr);
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
	public boolean isCardCard() {
		return shape.isCardArea;
	}

	public boolean isComunityCard() {
		String sn = getName();
		return sn.startsWith("flop") || sn.equals("turn") || sn.equals("river");
	}

	public boolean isHoleCard() {
		String sn = getName();
		return sn.startsWith("hero.card");
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
			if (shape.isCardArea) {
				ocrResult = getImageDifferenceOCR();
			} else {
				ocrResult = getTesseractOCR();
			}
		} catch (Exception e) {
			Hero.logger.severe(getName() + " twrow an exception " + e);
		}
		ocrTime = (int) (System.currentTimeMillis() - t1);
	}
	/**
	 * Perform tesseract ocr operation for generic areas.
	 * 
	 * @return the string recogniyed by tesseract
	 * 
	 * @throws TesseractException
	 */
	private String getTesseractOCR() throws TesseractException {
		// is this an OCR area ?
		boolean doo = shape.isOCRArea;
		if (!doo) {
			Hero.logger.finer(getName() + ": no ocr performed. Porperty shape.isOCRArea=" + doo);
			return null;
		}

		Hero.logger.finer(getName() + ": performing OCR...");
		regions = Hero.iTesseract.getSegmentedRegions(preparedImage, pageIteratorLevel);
		ocrResult = Hero.iTesseract.doOCR(preparedImage);
		List<Word> wlst = Hero.iTesseract.getWords(preparedImage, pageIteratorLevel);
		return OCRCorrection(this);
	}
	/**
	 * return the String representation of the card area by comparing the {@link ScreenSensor#getCapturedImage()} image
	 * against the list of card loaded in {@link #cardsTable} static variable. The most probable image file name is
	 * return.
	 * <p>
	 * This method is intendet for card areas. If the diference is more than 30% or the special image
	 * <code>card_facedown</code> is founded, this metod will return <code>null</code>
	 * 
	 * @return th ocr retrived from the original file name
	 */
	private String getImageDifferenceOCR() throws Exception {

		// ensure is an card area
		if (!shape.isCardArea) {
			throw new IllegalArgumentException("The screen sensor must be a card area sensor.");
		}
		BufferedImage imagea = getCapturedImage();
		String ocr = getOCRFromImage(imagea, cardsTable);

		// if the card is the file name is card_facedown, set null for ocr
		if (ocr != null && ocr.equals("card_facedown")) {
			ocr = null;
			Hero.logger.finer(getName() + ": card id face down.");
		}

		// at this point, if the ocr=null, the image diference is > difference threshold. that means than some garbage
		// is interfiring
		// whit the screen capture. this card area is marked as empty area.

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
		// TODO: the pot font is so thing that stardar imagen treat don.t detect white pixels
		if (getName().equals("pot")) {
			bufimg = ImageHelper.convertImageToBinary(bufimg);
		}

		// update global variable
		this.whitePercent = TColorUtils.getWhitePercent(bufimg);

		if (shape.isOCRArea) {
			BufferedImage escaled = ImageHelper.getScaledInstance(capturedImage, scaledWidth, scaledHeight);
			bufimg = ImageHelper.convertImageToGrayscale(escaled);
		}

		this.preparedImage = bufimg;
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
