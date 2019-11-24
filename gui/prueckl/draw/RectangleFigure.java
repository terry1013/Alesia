package gui.prueckl.draw;

import java.awt.*;

public class RectangleFigure extends Figure {
	
	public RectangleFigure(Component parent, Point pos) {
		super(parent, pos);
	}

	public void paintFigure(Graphics g) {
		g.drawRect(pos.x - width / 2, pos.y - height / 2, width, height);
	}
}
