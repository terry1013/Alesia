package plugins.hero;

import static marvin.MarvinPluginCollection.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jfree.chart.*;

import core.*;
import marvin.color.*;
import marvin.image.*;
import marvin.plugin.*;
import marvin.util.*;
import net.sourceforge.tess4j.util.*;

public class TCVUtils {

	private static Hashtable<String, JLabel> labels;
	private JFrame frame;
	private String prpfile = "plugins/hero/marvinproperties.properties";
	/**
	 * store the parameter for the methos inside this class. if this properti
	 */
	private Properties parameteres;
	private TreeMap<String, BufferedImage> images;

	private Hashtable<String, String> images2;

	public TCVUtils() {
		this.frame = new JFrame();
		WindowListener wl = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					parameteres.store(new FileOutputStream(prpfile), "");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(wl);
		parameteres = new Properties();
		try {
			parameteres.load(new FileInputStream(prpfile));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static MarvinImage autoCrop(List<MarvinSegment> segments, MarvinImage image) {
		Rectangle croprec = getJoinSegments(segments);
		MarvinImage clone;
		// if there is no area to crop, return a copy of the original
		if (croprec.getWidth() > 0 &&  croprec.getHeight() > 0)
			clone = image.subimage(croprec.x, croprec.y, croprec.width, croprec.height);
		else
			clone = image.clone();
		clone.update();
		return clone;
	}

	public static JPanel createImagesPanel(TreeMap<String, BufferedImage> images) {
		JPanel panel = new JPanel(new GridLayout(0, 4, 2, 2));
		labels = new Hashtable<>();
		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage image1 = images.get(key);
			JLabel jl = new JLabel();
			jl.setFont(new Font("courier new", Font.PLAIN, 12));
			jl.setOpaque(true);
			jl.setBackground(Color.LIGHT_GRAY);
			jl.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
			jl.setVerticalAlignment(JLabel.TOP);
			jl.setIcon(new ImageIcon(image1));
			String txt = "<html>" + key + "<br>with " + image1.getWidth() + "<br>height " + image1.getHeight()
					+ "</html>";
			jl.setText(txt);
			labels.put(key, jl);
			panel.add(jl);
		}
		return panel;
	}
	/**
	 * test method: draw the segments in the image
	 * 
	 * @param image
	 * @param segments
	 */
	public static void drawSegments(MarvinImage image, List<MarvinSegment> segments) {
		for (MarvinSegment segment : segments)
			image.drawRect(segment.x1, segment.y1, segment.width, segment.height, Color.BLUE);
		image.update();
	}

	public static MarvinImage findText(MarvinImage image) {
		return findText(image, 15, 8, 30, 150);

	}
	public static MarvinImage findText(MarvinImage image, int maxWhiteSpace, int maxFontLineWidth, int minTextWidth,
			int grayScaleThreshold) {
		List<MarvinSegment> segments = findTextRegions(image, maxWhiteSpace, maxFontLineWidth, minTextWidth,
				grayScaleThreshold);

		for (MarvinSegment s : segments) {
			if (s.height >= 5) {
				s.y1 -= 5;
				s.y2 += 5;
				image.drawRect(s.x1, s.y1, s.x2 - s.x1, s.y2 - s.y1, Color.red);
				image.drawRect(s.x1 + 1, s.y1 + 1, (s.x2 - s.x1) - 2, (s.y2 - s.y1) - 2, Color.red);
			}
		}
		return image;
	}

	public static int getHammingDistance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length(); k++) {
			if (s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}

	public static String getHightLight(String key, Object val) {
		String ht = "<br><FONT style= \"background-color: #808080\">" + key + val + "</FONT>";
		return ht;
	}

	/**
	 * Compare the images <code>imagea</code> and <code>imageg</code> pixel by pixel returning the percentage of
	 * diference. If the images are equals, return values closer to 0.0, and for complety diferent images, return values
	 * closer to 100 percent.
	 * <p>
	 * the ajust parameter indicate what todo whie the images are of diferent size.
	 * <li><code>true</code> means the image with mayor area will be rescaled to the dimension of the image of the menor
	 * aresa.
	 * <li><code>false</code>in this case, this function compare only the common area. that is, starting from (0,0)
	 * until the dimension of the smaller image
	 * 
	 * @see TColorUtils#getRGBColorDistance(Color, Color)
	 * 
	 * @param imagea
	 * @param imageb
	 * @param ajust
	 * @return
	 */
	public static double getImageDiferences(BufferedImage imagea, BufferedImage imageb, boolean ajust) {
		double diference = 0;
		BufferedImage buffimg_a = TColorUtils.copy(imagea);
		BufferedImage buffimg_b = TColorUtils.copy(imageb);

		// ajust the mayor image to the size of the minor image
		if (ajust) {
			int areaa = buffimg_a.getWidth() * buffimg_a.getHeight();
			int areab = buffimg_b.getWidth() * buffimg_b.getHeight();
			if (areaa > areab)
				buffimg_a = ImageHelper.getScaledInstance(buffimg_a, buffimg_b.getWidth(), buffimg_b.getHeight());
			else
				buffimg_b = ImageHelper.getScaledInstance(buffimg_b, buffimg_a.getWidth(), buffimg_a.getHeight());
		}

		int width = buffimg_a.getWidth() < buffimg_b.getWidth() ? buffimg_a.getWidth() : buffimg_b.getWidth();
		int height = buffimg_a.getHeight() < buffimg_b.getHeight() ? buffimg_a.getHeight() : buffimg_b.getHeight();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgba = buffimg_a.getRGB(x, y);
				int rgbb = buffimg_b.getRGB(x, y);
				diference += TColorUtils.getRGBColorDistance(new Color(rgba), new Color(rgbb));
			}
		}

		// total number of pixels
		int pixels = width * height;
		// normaliye the value of diferent pixel
		double avg_diff = diference / pixels;
		// percentage
		double percent = avg_diff * 100;
		return percent;
	}

	public static double getImageDiferences2(BufferedImage imagea, BufferedImage imageb, boolean ajust) {
		double diference = 0;
		BufferedImage buffimg_a = TColorUtils.convert3(imagea);
		BufferedImage buffimg_b = TColorUtils.convert3(imageb);

		// ajust the mayor image to the size of the minor image
		if (ajust) {
			int areaa = buffimg_a.getWidth() * buffimg_a.getHeight();
			int areab = buffimg_b.getWidth() * buffimg_b.getHeight();
			if (areaa > areab)
				buffimg_a = ImageHelper.getScaledInstance(buffimg_a, buffimg_b.getWidth(), buffimg_b.getHeight());
			else
				buffimg_b = ImageHelper.getScaledInstance(buffimg_b, buffimg_a.getWidth(), buffimg_a.getHeight());
		}
		Hashtable<String, Integer> ha = TColorUtils.getHistogram(buffimg_a);
		Hashtable<String, Integer> hb = TColorUtils.getHistogram(buffimg_b);

		int width = buffimg_a.getWidth() < buffimg_b.getWidth() ? buffimg_a.getWidth() : buffimg_b.getWidth();
		int height = buffimg_a.getHeight() < buffimg_b.getHeight() ? buffimg_a.getHeight() : buffimg_b.getHeight();
		Set<String> keys = ha.size() > hb.size() ? ha.keySet() : hb.keySet();

		for (String key : keys) {
			int ia = ha.get(key) != null ? ha.get(key).intValue() : 0;
			int ib = hb.get(key) != null ? hb.get(key).intValue() : 0;
			diference += Math.abs(ia - ib);
		}

		// total number of pixels
		int pixels = width * height;
		// normaliye the value of diferent pixel
		double avg_diff = diference / pixels;
		// percentage
		double percent = avg_diff * 100;
		return percent;
	}

	public static List<MarvinSegment> getImageSegments(MarvinImage mImage, boolean drawSegments, Properties parms) {
		if (parms == null) {
			parms = new Properties();
			parms.put("rgbToBinaryThreshold", "200");
			parms.put("removeSegmentsWindowSize", "1");
		}
		int rgbToBinaryThreshold = Integer.parseInt(parms.getProperty("rgbToBinaryThreshold"));
		int removeSegmentsWindowSize = Integer.parseInt(parms.getProperty("removeSegmentsWindowSize"));
		// MarvinImage mImage = new MarvinImage(image);
		List<MarvinSegment> segments = segment(mImage, rgbToBinaryThreshold);
		removeSegments(segments, mImage, removeSegmentsWindowSize);
		if (drawSegments) {
			drawSegments(mImage, segments);
		}
		return segments;
	}
	public static Rectangle getJoinSegments(List<MarvinSegment> segments) {
		int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
		int bottomY = -1, bottomX = -1;
		for (MarvinSegment segment : segments) {
			if (segment.x1 < topX)
				topX = segment.x1;
			if (segment.y1 < topY)
				topY = segment.y1;
			if (segment.x2 > bottomX)
				bottomX = segment.x2;
			if (segment.y2 > bottomY)
				bottomY = segment.y2;
		}
		Rectangle croprec = new Rectangle(topX, topY, bottomX - topX + 1, bottomY - topY + 1);
		return croprec;
	}

	public static BufferedImage getScaledBufferedImage(BufferedImage image) {
		Dimension ndim = getScaledDimension(image.getWidth(), image.getHeight());
		int type = (image.getTransparency() == Transparency.OPAQUE)
				? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage tmp = new BufferedImage(ndim.width, ndim.height, type);
		Graphics2D g2 = tmp.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.drawImage(image, 0, 0, ndim.width, ndim.height, null);
		g2.dispose();
		return tmp;
	}

	/**
	 * the scale factor form a image taked from the screen at 96dpi to the optimal image resolution 300dpi. Teste but no
	 * visible acuracy detected against the 2.5 factor. leave only because look like the correct procedure.
	 * 
	 * @param width - original width
	 * @param height - original height
	 * @return dimension with the scale size
	 */
	public static Dimension getScaledDimension(int width, int height) {
		int dpi = 300;
		float scale = dpi / 96;
		int scaledWidth = (int) (width * scale);
		int scaledHeight = (int) (height * scale);
		return new Dimension(scaledWidth, scaledHeight);
	}

	public static String imagePHash(BufferedImage image, Properties parms) {
		if (parms == null) {
			parms = new Properties();
			parms.put("imagePhash.size", "32");
			parms.put("imagePhash.smallSize", "16");
		}
		int size = Integer.parseInt(parms.getProperty("imagePhash.size"));
		int smallSize = Integer.parseInt(parms.getProperty("imagePhash.smallSize"));
		ImagePHash ih = new ImagePHash(size, smallSize);
		return ih.getHash(image);
	}

	/**
	 * load and prepare the list of cards form the source directory.
	 * 
	 * @see #prepareCard(BufferedImage)
	 * 
	 * @param dir - source directory
	 * 
	 * @return list for filename & images
	 */
	public static TreeMap<String, BufferedImage> loadCards(String dir) {
		File fdir = new File(dir);
		String[] imgs = fdir.list();
		TreeMap<String, BufferedImage> images = new TreeMap<>();
		for (String img : imgs) {
			File f = new File(dir + img);
			BufferedImage imageb;
			try {
				imageb = ImageIO.read(f);

				imageb = prepareCard(imageb, true);

				String inam = f.getName().split("[.]")[0];
				images.put(inam, imageb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images;
	}

	/**
	 * Load all images located in the <code>dir</code> parameter and create a {@link Hashtable} where the key is the
	 * file name (call, Ks, Etc.) and the value parameter is the {@link BufferedImage} loaded from that file
	 * 
	 * @param dir - folder source of the images
	 * 
	 * @return table of filename and image
	 */
	public static TreeMap<String, BufferedImage> loadImages(String dir) {
		File fdir = new File(dir);
		String[] imgs = fdir.list();
		TreeMap<String, BufferedImage> images = new TreeMap<>();
		for (String img : imgs) {
			File f = new File(dir + img);
			BufferedImage imageb;
			try {
				imageb = ImageIO.read(f);
				String inam = f.getName().split("[.]")[0];
				images.put(inam, imageb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images;
	}

	/**
	 * Load all images located in the <code>dir</code> parameter and create a {@link TreeMap} where the key is the file
	 * name (call, Ks, Etc.) and the value parameter is the image image hash
	 * 
	 * @see #imagePHash(BufferedImage, int)
	 * @param dir - folder source of the images
	 * @return table of filename and image
	 */
	public static TreeMap<String, String> loadPHashImages(String dir) {
		File fdir = new File(dir);
		String[] imgs = fdir.list();
		TreeMap<String, String> images = new TreeMap<>();
		for (String img : imgs) {
			File f = new File(dir + img);
			BufferedImage imageb;
			try {
				imageb = ImageIO.read(f);
				String inam = f.getName().split("[.]")[0];
				images.put(inam, imagePHash(imageb, null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images;
	}

	public static void main(String[] args) {
		TCVUtils demo = new TCVUtils();
		demo.parameteres.put("imagesDir", "plugins/hero/image_cards/");
		demo.showFrame();
	}
	public static BufferedImage MoravecCorners(BufferedImage image, boolean drawCorners, Properties parms) {
		if (parms == null) {
			parms = new Properties();
			parms.put("moravecCorners.threshold", "2000");
			parms.put("moravecCorners.matrixSize", "7");
		}
		int threshold = Integer.parseInt(parms.getProperty("moravecCorners.threshold"));
		int matrixSize = Integer.parseInt(parms.getProperty("moravecCorners.matrixSize"));

		MarvinImagePlugin moravec = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.corner.moravec");
		moravec.load();
		MarvinImage mImage = new MarvinImage(image);
		moravec.setAttribute("threshold", threshold);
		moravec.setAttribute("matrixSize", matrixSize);
		MarvinAttributes attr = new MarvinAttributes();
		moravec.process(mImage, null, attr);
		int[][] cornernessMap = (int[][]) attr.get("cornernessMap");
		int cor = 0;
		for (int x = 0; x < cornernessMap.length; x++)
			for (int y = 0; y < cornernessMap[0].length; y++)
				if (cornernessMap[x][y] > 0)
					cor++;

		parms.setProperty("moravecCorners.corners", "" + cor);
		if (drawCorners)
			mImage = showCorners(mImage, attr, 2);
		mImage.update();
		return mImage.getBufferedImage();
	}

	public static BufferedImage paintBorder(BufferedImage image, Properties parms) {
		if (parms == null) {
			parms = new Properties();
			parms.put("size", "8");
			parms.put("color", "FFFFFF");
		}
		int size = Integer.parseInt(parms.getProperty("size"));
		Color color = TColorUtils.getRGBColor(parms.getProperty("color"));

		BufferedImage newimagea = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g2d = newimagea.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		BasicStroke bs = new BasicStroke(size);
		g2d.setStroke(bs);
		g2d.setColor(color);
		g2d.drawRect(0, 0, image.getWidth(), image.getHeight());
		g2d.dispose();
		return newimagea;
	}

	/**
	 * perform standar algorithm for card. the incoming argument bust be the image whitout any treatment
	 * 
	 * @param image source image
	 * 
	 * @return prepared image
	 */
	public static BufferedImage prepareCard(BufferedImage image, boolean src) {
		BufferedImage bufimg = TColorUtils.convert8(image);
		Hashtable<String, Integer> histo = TColorUtils.getHistogram(bufimg);
		Color backgroundColor = TColorUtils.getBackgroundColor(histo);

		Properties parms = new Properties();
		parms.setProperty("color", TColorUtils.getRGBColor(backgroundColor));
		parms.setProperty("size", "8");
		bufimg = TCVUtils.paintBorder(bufimg, parms);
		if (src)
			bufimg = bufimg.getSubimage(0, 0, 28, 35);
		MarvinImage mi = new MarvinImage(bufimg);
		List<MarvinSegment> segments = TCVUtils.getImageSegments(mi, false, null);
		mi = TCVUtils.autoCrop(segments, mi);
		bufimg = mi.getBufferedImage();
		return bufimg;
	}

	/**
	 * 
	 * @param segments
	 * @param windowSize - the size of the windon for segment average area comparation (values close to 0 keep all
	 *        areas, values coles to 100 remove all areas)
	 */
	public static void removeSegments(List<MarvinSegment> segments, MarvinImage image, int windowSize) {
		int sum = 0;
		int area = image.getWidth() * image.getHeight();
		// for (MarvinSegment ms : segments)
		// sum += ms.area;
		// int average = sum / segments.size();
		// example: if windowSize = 80, minarea is 20% of the image area and maxarea = 80%
		double minarea = area * (windowSize / 100.0);
		double maxarea = area - minarea;
		segments.removeIf(s -> s.width * s.height < minarea || s.width * s.height > maxarea);
	}

	public static List<MarvinSegment> segment(MarvinImage image, int threshold) {
		MarvinImage image1 = image.clone();
		MarvinImage binImage = MarvinColorModelConverter.rgbToBinary(image1, threshold);
		image1 = MarvinColorModelConverter.binaryToRgb(binImage);
		MarvinSegment[] segments = floodfillSegmentation(image1);
		ArrayList<MarvinSegment> list = new ArrayList<>(segments.length);
		for (MarvinSegment ms : segments)
			list.add(ms);
		return list;
	}

	public static MarvinImage uperLeftAutoCrop(List<MarvinSegment> segments, MarvinImage image) {
		// no segments? retrun the same image
		if (segments.size() == 0)
			return image;

		Point upLeft = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		double dist = Integer.MAX_VALUE;
		for (MarvinSegment segment : segments) {
			Point p = new Point(segment.x1, segment.y1);
			double d = Point.distance(0, 0, segment.x1 * 1.0, segment.y1 * 1.0);
			if (d < dist) {
				upLeft = p;
				dist = d;
			}
		}

		Rectangle croprec = getJoinSegments(segments);
		Rectangle ulrec = new Rectangle(upLeft.x, upLeft.y, croprec.width, croprec.height);
		croprec = croprec.intersection(ulrec);

		MarvinImage clone = image.subimage(croprec.x, croprec.y, croprec.width, croprec.height);
		clone.update();
		return clone;
	}
	private static MarvinImage showCorners(MarvinImage image, MarvinAttributes attr, int rectSize) {
		MarvinImage ret = image.clone();
		int[][] cornernessMap = (int[][]) attr.get("cornernessMap");
		int rsize = 0;
		for (int x = 0; x < cornernessMap.length; x++) {
			for (int y = 0; y < cornernessMap[0].length; y++) {
				// Is it a corner?
				if (cornernessMap[x][y] > 0) {
					rsize = Math.min(Math.min(Math.min(x, rectSize), Math.min(cornernessMap.length - x, rectSize)),
							Math.min(Math.min(y, rectSize), Math.min(cornernessMap[0].length - y, rectSize)));
					ret.fillRect(x, y, rsize, rsize, Color.red);
				}
			}
		}

		return ret;
	}

	public void showHistogram(Hashtable<String, BufferedImage> images) {
		ActionsBarChart chart = new ActionsBarChart();
		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage tmpimg = TColorUtils.convert4(images.get(key));
			Hashtable<String, Integer> histo = TColorUtils.getHistogram(tmpimg);
			// chart.addDataset(histo, key);
		}
		ChartPanel cp = chart.getChartPanel();
		cp.setPreferredSize(new Dimension(600, 200));
		frame.setContentPane(cp);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	private JSlider getJSlider(String parameter, int max) {
		String pval = parameteres.getProperty(parameter);
		// if not exist, set the parameter on the center of the slider
		pval = pval == null ? "" + (max / 2) : pval;
		parameteres.setProperty(parameter, pval);
		int th = Integer.parseInt(pval);

		JSlider slider = new JSlider(0, max, th);
		slider.setToolTipText(parameter + ": " + th);
		slider.setName(parameter);
		ChangeListener cl = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int fps = (int) source.getValue();
					String par = source.getName();
					parameteres.setProperty(par, "" + fps);
					slider.setToolTipText(par + ": " + fps);
					processImages();
				}
			}
		};
		slider.addChangeListener(cl);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		return slider;
	}

	private void processImages() {
		// start from the source
		images = loadImages(ScreenSensor.IMAGE_CARDS);

		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage image = images.get(key);

			// the method i what to test
			image = prepareCard(image, true);

			images.put(key, image);
			JLabel jl = labels.get(key);
			String txt = "<html>" + key + "<br>with " + image.getWidth() + "<br>height " + image.getHeight()
					+ "</html>";
			jl.setText(txt);
			jl.setIcon(new ImageIcon(image));
			jl.repaint();
		}
	}

	private void saveResult() {
		try {
			String ext = "png";
			Set<String> keys = images.keySet();
			for (String key : keys) {
				BufferedImage image = images.get(key);
				File f = new File(ScreenSensor.IMAGE_CARDS + key + "." + ext);
				f.delete();
				f.createNewFile();
				ImageIO.write(image, ext, f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveSegments() {
		try {
			images = loadImages(ScreenSensor.IMAGE_CARDS);
			String ext = "png";
			Set<String> keys = images.keySet();
			for (String key : keys) {
				BufferedImage image = images.get(key);
				MarvinImage miB = new MarvinImage(image);
				List<MarvinSegment> segments = TCVUtils.getImageSegments(miB, true, null);

				for (int i = 0; i < segments.size(); i++) {
					MarvinSegment segA = segments.get(i);
					BufferedImage subA = image.getSubimage(segA.x1, segA.y1, segA.width, segA.height);
					File f = new File("----------" + System.currentTimeMillis() + "." + ext);
					f.delete();
					f.createNewFile();
					ImageIO.write(subA, ext, f);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showFrame() {
		// image panel
		images = loadImages(ScreenSensor.IMAGE_CARDS);
		JPanel imagesPanel = createImagesPanel(images);

		// controls
		// JSlider slider = getJSlider("size", 10);
		JSlider slider = getJSlider("rgbToBinaryThreshold", 255);
		slider.setMajorTickSpacing(25);
		JSlider slider2 = getJSlider("removeSegmentsWindowSize", 100);
		slider2.setMajorTickSpacing(10);

		JButton save = new JButton("Save");
		save.addActionListener(l -> saveResult());

		JPanel controlsPanel = new JPanel(new FlowLayout());
		controlsPanel.add(slider);
		controlsPanel.add(slider2);
		controlsPanel.add(save);

		// precess the image after the controls and the properties are set
		processImages();

		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(imagesPanel, BorderLayout.CENTER);
		mainpanel.add(controlsPanel, BorderLayout.SOUTH);
		frame.setContentPane(mainpanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void testImagePhash() {
		images = loadImages(ScreenSensor.IMAGE_CARDS);
		images2 = new Hashtable<>();

		Set<String> keys = images.keySet();
		for (String key : keys) {
			images2.put(key, imagePHash(images.get(key), parameteres));
		}

		for (String key : keys) {
			BufferedImage image = images.get(key);
			int mindis = Integer.MAX_VALUE;
			String who = "";
			String s1 = images2.get(key);
			for (String key2 : keys) {
				int dis = getHammingDistance(s1, images2.get(key2));
				if (!key.equals(key2) && dis < mindis) {
					who = key2;
					mindis = dis;
				}
			}
			// the method i what to test
			// image = processCard(image, true, parameteres);

			images.put(key, image);
			JLabel jl = labels.get(key);

			// min distance highlight
			String distxt = mindis < 3
					? "<br><FONT style= \"background-color: #808080\">minDis: " + mindis + "</FONT>"
					: "<br>minDis: " + mindis;

			String txt = "<html>" + key + "<br>to who: " + who + distxt + "</html>";
			System.out.println(key + "\t" + who + "\t" + mindis);
			jl.setText(txt);
			jl.setIcon(new ImageIcon(image));
			jl.repaint();
		}
	}
}