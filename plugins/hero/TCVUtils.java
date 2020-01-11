package plugins.hero;

import static marvin.MarvinPluginCollection.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jfree.chart.*;

import core.*;
import marvin.color.*;
import marvin.image.*;
import marvin.io.*;

public class TCVUtils {

	private static double REMOVE_SEGMENTS = 0.10;
	private static int SEGMENTS_MIN_DISTANCE = 2;

	private JFrame frame;

	private JLabel imageLabel;

	public TCVUtils() {
		this.frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.imageLabel = new JLabel();
		JPanel jp = new JPanel(new BorderLayout());
		frame.setContentPane(jp);
		frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
	}

	public static MarvinImage autoCrop(List<MarvinSegment> segments, MarvinImage image) {
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
		MarvinImage clone = image.subimage(croprec.x, croprec.y, croprec.width, croprec.height);
		clone.update();
		return clone;
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
		// Hero.logger.config("Loading images from " + dir);
		Hashtable<String, BufferedImage> images = new Hashtable<>();
		for (String img : imgs) {
			File f = new File(dir + img);
			BufferedImage imageb;
			try {
				imageb = ImageIO.read(f);
				String inam = f.getName().split("[.]")[0];
				BufferedImage old = images.put(inam, imageb);
				if (old != null) {
					// Hero.logger.warning("image file name " + inam + "duplicated.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images;
	}

	public static void main(String[] args) {
		TCVUtils demo = new TCVUtils();
		String dir = "plugins/hero/image_cards/";
		File f = new File(dir + "Kc.png");
		MarvinImage image = MarvinImageIO.loadImage(f.getAbsolutePath());
		long t1 = System.currentTimeMillis();

		demo.showFrame();
		// image = segment(image);

		System.out.println(System.currentTimeMillis() - t1);
		demo.showResult(image);
	}

	public static void removeSegments(List<MarvinSegment> segments, double factor) {
		int sum = 0;
		for (MarvinSegment ms : segments)
			sum += ms.area;
		int average = sum / segments.size();
		double minarea = average - average * factor;
		double maxarea = average + average * factor;
		segments.removeIf(s -> s.area < minarea || s.area > maxarea);
	}

	public static List<MarvinSegment> segment(MarvinImage image) {
		MarvinImage image1 = image.clone();
		MarvinImage binImage = MarvinColorModelConverter.rgbToBinary(image1, 200);
		image1 = MarvinColorModelConverter.binaryToRgb(binImage);
		MarvinSegment[] segments = floodfillSegmentation(image1);
		ArrayList<MarvinSegment> list = new ArrayList<>(segments.length);
		for (MarvinSegment ms : segments)
			list.add(ms);
		return list;
	}

	public void showHistogram(Hashtable<String, BufferedImage> images) {
		ActionsBarChart chart = new ActionsBarChart();
		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage tmpimg = TColorUtils.convert4(images.get(key));
			Hashtable<Integer, Integer> histo = TColorUtils.getHistogram(tmpimg);
			chart.addDataset(histo, key);
		}
		ChartPanel cp = chart.getChartPanel();
		cp.setPreferredSize(new Dimension(600, 200));
		frame.setContentPane(cp);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	private Hashtable<String, BufferedImage> images;
	private Hashtable<String, JLabel> labels;

	private JPanel createImagesPanel() {
		JPanel panel = new JPanel(new GridLayout(4, 14, 2, 2));
		images = loadImages(ScreenSensor.IMAGE_CARDS);
		labels = new Hashtable<>();
		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage image1 = images.get(key);
			JLabel jl = new JLabel();
			jl.setText(key);
			jl.setIcon(new ImageIcon(image1));
			labels.put(key, jl);
			panel.add(jl);
		}
		return panel;
	}

	public static BufferedImage processCard(BufferedImage image) {
		double removeFactor = 0.9;
		MarvinImage mImage = new MarvinImage(image);
		List<MarvinSegment> segments = segment(mImage);
		removeSegments(segments, removeFactor);
		// if no segment remains, is because the area is a empty area. return the same input image
		if (segments.size() == 0)
			return image;
		// drawSegments(mImage, segments);
		mImage = autoCrop(segments, mImage);
		return mImage.getBufferedImage();
	}
	private void processImages(double removeFactor) {
		images = loadImages(ScreenSensor.IMAGE_CARDS);
		Set<String> keys = images.keySet();
		for (String key : keys) {
			BufferedImage image = images.get(key);
			image = processCard(image);
			images.put(key, image);
			JLabel jl = labels.get(key);
			jl.setIcon(new ImageIcon(image));
			jl.repaint();
		}
	}

	private void showFrame() {
		// know parameters
		double removefactor = 0.9;
		// image panel
		JPanel imagesPanel = createImagesPanel();

		// controls
		JSlider slider = new JSlider(0, 100, (int) (removefactor * 100));
		ChangeListener cl = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int fps = (int) source.getValue();
					processImages(fps / 100.0);
				}
			}
		};
		slider.addChangeListener(cl);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		JButton save = new JButton("Save");
		save.addActionListener(l -> saveResult());

		JPanel controlsPanel = new JPanel(new FlowLayout());
		controlsPanel.add(slider);
		controlsPanel.add(save);

		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(imagesPanel, BorderLayout.CENTER);
		mainpanel.add(controlsPanel, BorderLayout.SOUTH);
		frame.setContentPane(mainpanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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

	private void showResult(MarvinImage image) {
		image.update();
		BufferedImage im = getScaledBufferedImage(image.getBufferedImage());
		imageLabel.setIcon(new ImageIcon(im));
		imageLabel.setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}