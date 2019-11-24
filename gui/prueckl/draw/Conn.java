package gui.prueckl.draw;

import java.awt.*;
import java.awt.geom.*;
import java.lang.reflect.*;
import java.util.*;


public abstract class Conn extends Drawable {
    public static class NoActiveConnException extends Exception {
    }
    protected Figure from=null, to=null;
    protected Point dragp=null;
    public Conn(Component parent, Figure from) {
        super(parent);
        this.from=from;
        from.fromConns.add(this);
        invalidate();
    }
    public void delete() {
        if (getFromFigure()!=null)
            getFromFigure().fromConns.remove(this);
        if (getToFigure()!=null)
            getToFigure().toConns.remove(this);
    }
    public Figure getFromFigure() {
      return from;
    }
    public Figure getToFigure() {
      return to;
    }
    public void setDragPoint(Point p) {
        invalidate();
        dragp=p;
        invalidate();
    }
    public void setToFigure(Figure to) {
        invalidate();
        if (this.to!=null) {
            this.to.toConns.remove(this);
        }
        this.to=to;
        if (to!=null) {
            to.toConns.add(this);
        }
        invalidate();
    }
    protected Vector highlightSelectPoints=new Vector();
    public void drawHighlightSelectPoints(Graphics g) {
        highlightSelectPoints.clear();
        if (highlighted || selected) {
            java.awt.Rectangle bounds=getBounds();
            highlightSelectPoints.add(new Point(bounds.x, bounds.y));
            highlightSelectPoints.add(new Point(bounds.x+bounds.width, bounds.y));
            highlightSelectPoints.add(new Point(bounds.x+bounds.width, bounds.y+bounds.height));
            highlightSelectPoints.add(new Point(bounds.x, bounds.y+bounds.height));
            Point tp;
            for (Enumeration e=highlightSelectPoints.elements(); e.hasMoreElements(); ) {
                tp = (Point) e.nextElement();
                this.paintHighlightSelectPoint(g, tp.x, tp.y);
            }
        }
    }
    public void invalidate() {
        java.awt.Rectangle bounds=getBounds();
        parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    protected void getConnPoints(Point pFrom, Point pTo) {
        Point p2=new Point();
        Point cp;
        Point p1=new Point();
        if (to!=null) {
            cp=to.getCenterPoint();
            p1=from.getConnPoint(cp);
            /*if (p1.equals(from.getLeftConnPoint()))
                p2=to.getRightConnPoint();
            else if (p1.equals(from.getRightConnPoint()))
                p2=to.getLeftConnPoint();
            else if (p1.equals(from.getTopConnPoint()))
                p2=to.getBottomConnPoint();
            else if (p1.equals(from.getBottomConnPoint()))
                p2=to.getTopConnPoint();
            else*/
                p2=to.getConnPoint(p1);
        } else if (dragp!=null) {
            p2=dragp;
            cp=p2;
            p1=from.getConnPoint(cp);
        }
        pFrom.x=p1.x;
        pFrom.y=p1.y;
        pTo.x=p2.x;
        pTo.y=p2.y;
    }
    public void paint(Graphics g) {
        Point p2=new Point();
        Point p1=new Point();
        getConnPoints(p1, p2);
        //System.out.println("Conn.paint()");
        paintConnection(g, p1, p2);
        if (highlighted || selected) {
            paintHighlightSelectPoint(g, p1.x, p1.y);
            paintHighlightSelectPoint(g, p2.x, p2.y);
        }        
    }
    public void paintConnection(Graphics g, Point p1, Point p2) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);        
    }
    public Line2D.Double getLine2D() {
        Point p2=new Point();
        Point p1=new Point();
        getConnPoints(p1, p2);
        return new Line2D.Double(p1, p2);
    }
    public boolean isInside(Point p) {
        return getBounds().contains(p);
    }
    public java.awt.Rectangle getBounds() {
        java.awt.Rectangle bounds=getLine2D().getBounds();
        bounds.width++;
        bounds.height++;
        return bounds;
    }
    public static boolean isConnSelected() {
      return activeDrawableClass!=null && Conn.class.isAssignableFrom(activeDrawableClass);
    }
    public static Conn createConn(Component parent, Figure from) throws NoActiveDrawableException {
      if (activeDrawableClass==null) throw new NoActiveDrawableException();
      try {
          Constructor ct = activeDrawableClass.getConstructor(new Class[] {Component.class, Figure.class});
          return (Conn)ct.newInstance(new Object[] {parent, from});
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return null;
  }
}
