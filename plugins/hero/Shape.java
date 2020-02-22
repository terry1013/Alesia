package plugins.hero;

import java.awt.*;

import org.apache.commons.math3.distribution.*;
import org.apache.poi.hslf.usermodel.*;

/**
 * Encapsulate all information about a shape inside of the ppt file. this class is a bean of the selected information
 * content in the {@link HSLFShape}
 * 
 * @author terry
 *
 */
public class Shape {

	/**
	 * rectangle that represent the ppt figure anchor. This property is retrives from the figure location inside of the
	 * ppt slide
	 */
	public Rectangle bounds = new Rectangle();

	/**
	 * Property: name <br>
	 * Range: <br>
	 * Description: name of the figure area. this name must be unique.
	 */
	public String name;

	private Point center = new Point();

	/**
	 * Property: enaColor<br>
	 * Description: indicate the color name that must be present to indicate a sensor is enabled. If the sensor has.t
	 * this property, the white color is used
	 */
	public Color enableColor;

	/**
	 * An action area is an area that is clicable by the aplication. this kind of area is of interest because it can has
	 * enable/disable status and control the game flow. the property is setted when the name of the shape star with
	 * <code>action.</code> prefix
	 */
	public boolean isActionArea = false;

	/**
	 * set to true during the construction if ths shape is a card area. hero.cards, villans cards and comunity cards all
	 * are card areas. this property is based when the name of the shape int the ppt file
	 */
	public boolean isCardArea = false;

	/**
	 * indicate if this area is a button area. the button area is the place where the button (flag) is placed to
	 * indicate that the player is big blind, small blind or dealer.
	 */
	public boolean isButtonArea = false;
	/**
	 * mark if this shape is required to be readed by tesserac and is a text area. this area can contain any string.
	 * this property is hardcoded in a method nside of {@link ShapeAreas}
	 */
	public boolean isOCRTextArea = false;

	/**
	 * mark if this shape is required to be readed by tesserac and is a numeric area. a numeric area MUST ontain ONLY
	 * numbers. this property is hardcoded in a method inside of {@link ShapeAreas}
	 */
	public boolean isOCRNumericArea = false;

	/**
	 * indicate how menay clicks require this area to be completed. this attribute must be named <code>clicks</code> in
	 * the shape name. by default, an action area nedds only a mouse/keyboard click to complete. this parameter is used
	 * by {@link RobotActuator}
	 */
	public int clicks = 1;

	/**
	 * Return a random point normal distributed allong the x and y axis
	 * 
	 * @return a random point
	 */
	public Point getRandomPoint() {
		int sx = (int) distributionX.sample();
		int sy = (int) distributionY.sample();
		return new Point(sx, sy);
	}

	public Point getCenter() {
		return center;
	}

	private NormalDistribution distributionX;
	private NormalDistribution distributionY;

	public Shape(Rectangle rec) {
		this.bounds = rec;
		int cx = rec.width / 2;
		int cy = rec.height / 2;
		center = new Point();
		center.x = rec.x + cx;
		center.y = rec.y + cy;
		this.distributionX = new NormalDistribution(center.x, 4);
		this.distributionY = new NormalDistribution(center.y, 4);
	}
}
