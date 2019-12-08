package plugins.hero;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.apache.poi.hslf.usermodel.*;

import core.*;

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
		return name.contains(".card") || name.startsWith("flop") || name.equals("turn") || name.equals("river");
	}

	private boolean isOCRArea(String name) {
		return name.equals("pot") || name.equals("call") || name.equals("raise")
				|| TStringUtils.wildCardMacher(name, "*.call") || TStringUtils.wildCardMacher(name, "*.name")
				|| TStringUtils.wildCardMacher(name, "*.chips");
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
			Hero.logger.finer(
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
						Hero.logger.finer("shape found " + name + " Bounds" + "[x=" + sha.bounds.x + ",y="
								+ sha.bounds.y + ",width=" + sha.bounds.width + ",height=" + sha.bounds.height + "]");
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
			Predicate<Shape> pre = (sh -> sh.name.contains(".name"));
			checkDimensions(pre, "villan2.name", "Name areas");
			checkDimensions((sh -> sh.isCardArea), "hero.card1", "Card areas");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkDimensions(Predicate<Shape> predicate, String refName, String msgPrefix) {
		DescriptiveStatistics stat = new DescriptiveStatistics();
		Dimension base = shapes.get(refName).bounds.getSize();
		shapes.values().stream().filter(predicate)
				.forEach(sh -> stat.addValue(sh.bounds.getWidth() * sh.bounds.getHeight()));
		if (stat.getMax() != stat.getMin()) {
			Hero.logger.severe(msgPrefix + " HAS NOT the same dimensions.");
			Hero.logger.severe(msgPrefix + " of reference: " + base.width + " height=" + base.height);
			shapes.values().stream().filter(predicate.and(sh -> !base.equals(sh.bounds.getSize())))
					.forEach(losh -> Hero.logger
							.severe(losh.name + " with=" + losh.bounds.width + " height=" + losh.bounds.height));

		} else {
			Hero.logger.info(msgPrefix + " checked. all areas have width=" + base.width + " height=" + base.height);
		}
	}

	public Hashtable<String, Shape> getShapes() {
		return shapes;
	}
}
