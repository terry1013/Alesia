package plugins.hero;

import static marvin.MarvinPluginCollection.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

import marvin.color.*;
import marvin.image.*;
import marvin.io.*;

public class TCVUtils {

	private JFrame frame;
	private JLabel imageLabel;

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

	private void showResult(MarvinImage image) {
		image.update();
		BufferedImage im = getScaledBufferedImage(image.getBufferedImage());
		imageLabel.setIcon(new ImageIcon(im));
		imageLabel.setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static MarvinImage findText(MarvinImage image) {
		return findText(image, 15, 8, 30, 150);

	}
	public TCVUtils() {
		this.frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.imageLabel = new JLabel();
		JPanel jp = new JPanel(new BorderLayout());
		frame.setContentPane(jp);
		frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
	}

	public static MarvinImage segment(MarvinImage imagea) {
		MarvinImage image = imagea.clone();
		MarvinImage binImage = MarvinColorModelConverter.rgbToBinary(image, 200);
		image = MarvinColorModelConverter.binaryToRgb(binImage);
		MarvinSegment[] segments = floodfillSegmentation(image);

		for (int i = 1; i < segments.length; i++) {
			MarvinSegment seg = segments[i];
			imagea.drawRect(seg.x1, seg.y1, seg.width, seg.height, Color.red);
		}
		return imagea;
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

	public static void main(String[] args) {
		TCVUtils demo = new TCVUtils();
		String dir = "plugins/hero/image_cards/";
		File f = new File(dir + "Kc.png");
		MarvinImage image = MarvinImageIO.loadImage(f.getAbsolutePath());
		long t1 = System.currentTimeMillis();
		image = segment(image);
		System.out.println(System.currentTimeMillis() - t1);

		demo.showResult(image);
	}
}