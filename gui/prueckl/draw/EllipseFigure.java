package gui.prueckl.draw;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;

public class EllipseFigure extends Figure {
    public EllipseFigure(Component parent, Point pos) {
        super(parent, pos);
    }
    public void paintFigure(Graphics g) {
        g.drawArc(pos.x-width/2, pos.y-height/2, width, height, 0, 360);
    }
}
