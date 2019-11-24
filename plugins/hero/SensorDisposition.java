package plugins.hero;

import java.awt.geom.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.apache.poi.hslf.usermodel.*;

/**
 * this class represent the sensors configured using power point. This class read the powerpoint file and extract all
 * the necesari information about the game sensor disposition.
 * 
 * @author terry
 *
 */
public class SensorDisposition {

	private Hashtable<String, Shape> shapes;
	private File file;
	private ImageIcon backgroundImage;

	public SensorDisposition(File file) {
		this.shapes = new Hashtable<>();
		this.file = file;
	}
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	public void read() {
		try {

			FileInputStream fis = new FileInputStream(file);
			HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(fis));
			// get slides
			for (HSLFSlide slide : ppt.getSlides()) {
				for (HSLFShape sh : slide.getShapes()) {		
					if(sh instanceof HSLFAutoShape) {
						HSLFAutoShape pptshape = (HSLFAutoShape) sh;
						Rectangle2D anchor = pptshape.getAnchor();
						Shape sha = new Shape(anchor.getBounds());
						String name = pptshape.getShapeName();
//						marck action areas
						if(name.startsWith("action.")) {
							sha.isActionArea = true;
							name = name.replace("action.", "");
						}
						sha.name = name;
					}
				}
			}
			ppt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Hashtable<String, Shape> getShapes() {
		return shapes;
	}
}
