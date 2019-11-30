package plugins.hero;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.apache.poi.hslf.usermodel.*;

/**
 * this class represent the sensors configured using power point. This class read the powerpoint file and extract all
 * the necesari information about the game sensor disposition.
 * 
 * @author terry
 *
 */
public class ScreenAreas {

	private Hashtable<String, Shape> shapes;
	private File file;
	private ImageIcon backgroundImage;

	public ScreenAreas(File file) {
		this.shapes = new Hashtable<>();
		this.file = file;
	}
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	private boolean isCardArea(String name) {
		return name.startsWith("hero.card") || name.startsWith("flop") || name.equals("turn") || name.equals("river")
				|| (name.startsWith("villan") && name.contains("card"));
	}

	private boolean isOCRArea(String name) {
		return name.equals("hero.chips") || name.equals("pot") || name.equals("call") || name.equals("raise")
				|| (name.startsWith("villan") && name.contains("name"))
				|| (name.startsWith("villan") && name.contains("call"));
	}

	public void read() {
		try {

			FileInputStream fis = new FileInputStream(file);
			HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(fis));

			// background. paste the image from clipboard genera an PNG image
			HSLFSlideMaster master = ppt.getSlideMasters().get(0);
			HSLFFill fill = master.getBackground().getFill();
			HSLFPictureData pic = fill.getPictureData();
			byte[] data = pic.getData();
			Hero.logger.fine(
					"background detected type=" + pic.getType() + " Dimesions " + pic.getImageDimensionInPixels());
			backgroundImage = new ImageIcon(data);
			for (HSLFSlide slide : ppt.getSlides()) {
				for (HSLFShape sh : slide.getShapes()) {
					// if (sh instanceof HSLFPictureShape) {
					// HSLFPictureShape pict = (HSLFPictureShape) sh;
					// // System.out.println(pict.getPictureName());
					// // System.out.println(pict.getShapeName());
					// HSLFPictureData pictData = pict.getPictureData();
					// byte[] data = pictData.getData();
					// backgroundImage = new ImageIcon(data);
					// // PictureData.PictureType type = pictData.getType();
					// // FileOutputStream out = new FileOutputStream("C:/Users/terry/Desktop/slide0_" + idx +
					// // type.extension);
					// // out.write(data);
					// // out.close();
					// // idx++;
					// }
					if (sh instanceof HSLFAutoShape) {
						HSLFAutoShape pptshape = (HSLFAutoShape) sh;
						Rectangle2D anchor = pptshape.getAnchor();
						String name = pptshape.getShapeName();
						// TODO: temporal: from 72dpi to 96
						anchor.setRect(anchor.getX() * 1.3333, anchor.getY() * 1.3333, anchor.getWidth() * 1.3333,
								anchor.getHeight() * 1.3333);
						Shape sha = new Shape(anchor.getBounds());
						Hero.logger.fine("shape found " + name + " Bounds" + "[x=" + sha.bounds.x + ",y=" + sha.bounds.y
								+ ",width=" + sha.bounds.width + ",height=" + sha.bounds.height + "]");
						// marck action areas
						if (name.startsWith("action.")) {
							sha.isActionArea = true;
							name = name.replace("action.", "");
						}
						sha.name = name;
						sha.isCardArea = isCardArea(name);
						sha.isOCRArea = isOCRArea(name);
						sha.isButtonArea = name.contains(".button");

						shapes.put(name, sha);
					}
				}
			}
			ppt.close();
			checkCardsDimensions();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkCardsDimensions() {
		DescriptiveStatistics stat = new DescriptiveStatistics();
		Dimension base = shapes.get("hero.card1").bounds.getSize();
		shapes.values().stream().filter(sh -> sh.isCardArea)
				.forEach(sh -> stat.addValue(sh.bounds.getWidth() * sh.bounds.getHeight()));
		if (stat.getMax() != stat.getMin()) {
			Hero.logger.severe("Card areas HAS NOT the same dimensions.");
			Hero.logger.severe("Base card: " + base.width + " height=" + base.height);
			shapes.values().stream().filter(sh -> sh.isCardArea && !base.equals(sh.bounds.getSize()))
					.forEach(losh -> Hero.logger
							.severe(losh.name + " with=" + losh.bounds.width + " height=" + losh.bounds.height));

		} else {
			Hero.logger.info("Card areas checked. all card have width=" + base.width + " height=" + base.height);
		}
	}

	public Hashtable<String, Shape> getShapes() {
		return shapes;
	}
}
