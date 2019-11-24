package plugins.hero;

import java.awt.*;

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
	 * Property: enable.when <br>
	 * Range: 0-100 <br>
	 * Description: indicate the % of white color that must be present to cosider a action area as enabled.
	 */
	public int enableWhen = 0;

	/**
	 * Property: area.type=action <br>
	 * Range: boolean <br>
	 * Description: An action area is an area that is clicable by the aplication. this kind of area is of interest
	 * because it can has enable/disable status and control the game flow.
	 */
	public boolean isActionArea = false;

	public Point getCenterPoint() {
		return center;
	}

	public Shape(Rectangle rec) {
		this.bounds = rec;
		int cx = rec.width / 2;
		int cy = rec.height / 2;
		center = new Point();
		center.x = rec.x + cx;
		center.y = rec.y + cy;
	}
}
