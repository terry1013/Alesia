package gui.prueckl.draw;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.lang.reflect.*;
import java.util.*;

import plugins.hero.*;

public abstract class Figure extends Drawable {
	public static final int TRANSFORM_ANCHOR_SIZE = 8;
	private static final int TP_TOPLEFT = 0;
	private static final int TP_TOPRIGHT = 1;
	private static final int TP_BOTTOMRIGHT = 2;
	private static final int TP_BOTTOMLEFT = 3;
	private static final int TP_TOP = 4;
	private static final int TP_BOTTOM = 5;
	private static final int TP_LEFT = 6;
	private static final int TP_RIGHT = 7;
	public Vector<Conn> fromConns = new Vector<Conn>();
	public Vector<Conn> toConns = new Vector<Conn>();
	protected Point pos;
	transient protected MouseListener ml = new MouseListener();
	protected Vector transformPoints = new Vector();
	protected Point currenttp = null;
	protected int height, width;
	public Figure(Component parent, Point pos) {
		super(parent);
		this.pos = pos;
		// parent.addMouseListener(ml);
		// parent.addMouseMotionListener(ml);
	}
	public static Figure createFigure(Component parent, Point pos) throws NoActiveDrawableException {
		if (activeDrawableClass == null)
			throw new NoActiveDrawableException();
		try {
			Constructor ct = activeDrawableClass.getConstructor(new Class[]{Component.class, Point.class});
			return (Figure) ct.newInstance(new Object[]{parent, pos});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	public static boolean isFigureSelected() {
		return activeDrawableClass != null && Figure.class.isAssignableFrom(activeDrawableClass);
	}
	public void delete() {
	}
	public java.awt.Rectangle getAnchorBounds() {
		java.awt.Rectangle bounds = getBounds();
		bounds.grow(TRANSFORM_ANCHOR_SIZE / 2, TRANSFORM_ANCHOR_SIZE / 2);
		return bounds;
	}
	public Point getBottomConnPoint() {
		return new Point(pos.x, pos.y + height / 2);
	}
	public java.awt.Rectangle getBounds() {
		return new java.awt.Rectangle(pos.x - width / 2, pos.y - height / 2, width, height);
	}
	public java.awt.Rectangle getBoundsOfTransformPoint(Point tp) {
		return new java.awt.Rectangle(tp.x - TRANSFORM_ANCHOR_SIZE / 2, tp.y - TRANSFORM_ANCHOR_SIZE / 2,
				TRANSFORM_ANCHOR_SIZE, TRANSFORM_ANCHOR_SIZE);
	}
	public void setBounds(Rectangle rec) {
		int cx = rec.width /2;
		int cy = rec.height /2;
		pos.x = rec.x + cx;
		pos.y = rec.y + cy;
		width = rec.width;
		height = rec.height;
	}
	public Point getCenterPoint() {
		return pos;
	}
	public java.awt.Rectangle getConnBounds() {
		java.awt.Rectangle connbounds = new java.awt.Rectangle();
		for (Conn conn : fromConns) {
			connbounds = connbounds.union(conn.getBounds());
		}
		for (Conn conn : toConns) {
			connbounds = connbounds.union(conn.getBounds());
		}
		return connbounds;
	}
	public Point getConnPoint(Point target) {
		double x;
		double y;
		double d = Double.MAX_VALUE;
		double newd;
		Point connpoint = null;

		x = pos.x;
		y = pos.y - height / 2;
		newd = Math.sqrt(Math.pow(target.x - x, 2) + Math.pow(target.y - y, 2));
		if (newd < d) {
			connpoint = new Point((int) x, (int) y);
			d = newd;
		}

		x = pos.x;
		y = pos.y + height / 2;
		newd = Math.sqrt(Math.pow(target.x - x, 2) + Math.pow(target.y - y, 2));
		if (newd < d) {
			connpoint = new Point((int) x, (int) y);
			d = newd;
		}

		x = pos.x - width / 2;
		y = pos.y;
		newd = Math.sqrt(Math.pow(target.x - x, 2) + Math.pow(target.y - y, 2));
		if (newd < d) {
			connpoint = new Point((int) x, (int) y);
			d = newd;
		}

		x = pos.x + width / 2;
		y = pos.y;
		newd = Math.sqrt(Math.pow(target.x - x, 2) + Math.pow(target.y - y, 2));
		if (newd < d) {
			connpoint = new Point((int) x, (int) y);
			d = newd;
		}

		return connpoint;
	}
	public Point getLeftConnPoint() {
		return new Point(pos.x - width / 2, pos.y);
	}
	public Point getRightConnPoint() {
		return new Point(pos.x + width / 2, pos.y);
	}
	public Point getTopConnPoint() {
		return new Point(pos.x, pos.y - height / 2);
	}
	public boolean isInside(Point p) {
		if (p.x >= pos.x - width / 2 && p.x <= pos.x + width / 2 && p.y >= pos.y - height / 2
				&& p.y <= pos.y + height / 2) {
			return true;
		}
		if (isOnTransformPoint(p) != -1) {
			return true;
		}
		return false;
	}
	public int isOnTransformPoint(Point p) {
		Point tp;
		int i = 0;
		for (Enumeration e = transformPoints.elements(); e.hasMoreElements();) {
			tp = (Point) e.nextElement();
			if (getBoundsOfTransformPoint(tp).contains(p))
				return i;
			i++;
		}
		return -1;
	}
	public void moveBy(int x, int y) {
		java.awt.Rectangle connbounds = getConnBounds();
		java.awt.Rectangle oldbounds = (this.highlighted || this.selected) ? getAnchorBounds() : getBounds();
		pos.x += x;
		pos.y += y;
		java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
				.union(oldbounds);
		parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
		connbounds = connbounds.union(getConnBounds());
		parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
	}

	public void paint(Graphics g1) {
		Graphics2D g2d = (Graphics2D) g1;
		java.awt.Rectangle bound = getBounds();

		// rotation
		AffineTransform olat = g2d.getTransform();
		String deg = getProperties().getProperty("rotate", null);
		if (deg != null) {
			int ax = bound.x + bound.width / 2;
			int ay = bound.y + bound.height / 2;
			double th = Math.toRadians(Integer.parseInt(deg));
			AffineTransform rot = AffineTransform.getRotateInstance(th, ax, ay);
			g2d.setTransform(rot);
		}

		// color
		String col = getProperties().getProperty("color", "#000000");
		g2d.setColor(Color.decode(col));

		// triger point
		Point p = ScreenSensor.getTrigerPoint(this);
		if (p != null) {
			int x = bound.x + p.x;
			int y = bound.y + p.y;
			g2d.drawLine(x - 3, y, x + 3, y);
			g2d.drawLine(x, y - 3, x, y + 3);
		}

		paintFigure(g2d);
		g2d.setTransform(olat);

		paintHighlightSelectPoints(g2d);
	}

	public Shape getShape() {
		// temporaly only for rectangles
		Rectangle r = getBounds();
		r.x = 0;
		r.y = 0;
		Rectangle2D r2 = r.getBounds2D();
		r2.setFrame(r);
		return r2;
	}
	public abstract void paintFigure(Graphics g);
	public void paintHighlightSelectPoints(Graphics g) {
		transformPoints.clear();
		if (highlighted || selected) {
			transformPoints.add(new Point(pos.x - width / 2, pos.y - height / 2));
			transformPoints.add(new Point(pos.x + width / 2, pos.y - height / 2));
			transformPoints.add(new Point(pos.x + width / 2, pos.y + height / 2));
			transformPoints.add(new Point(pos.x - width / 2, pos.y + height / 2));
			transformPoints.add(new Point(pos.x, pos.y - height / 2));
			transformPoints.add(new Point(pos.x, pos.y + height / 2));
			transformPoints.add(new Point(pos.x - width / 2, pos.y));
			transformPoints.add(new Point(pos.x + width / 2, pos.y));
			Point tp;
			for (Enumeration e = transformPoints.elements(); e.hasMoreElements();) {
				tp = (Point) e.nextElement();
				paintHighlightSelectPoint(g, tp.x, tp.y);
			}
		}
	}
	public void paintTransformPoint(Graphics g, int x, int y) {
		g.drawRect(x - TRANSFORM_ANCHOR_SIZE / 2, y - TRANSFORM_ANCHOR_SIZE / 2, TRANSFORM_ANCHOR_SIZE,
				TRANSFORM_ANCHOR_SIZE);
	}
	public void setBoundPoints(Point p1, Point p2) {
		java.awt.Rectangle connbounds = getConnBounds();
		java.awt.Rectangle oldbounds = (this.highlighted || this.selected) ? getAnchorBounds() : getBounds();
		pos = new Point(p1.x + (p2.x - p1.x) / 2, p1.y + (p2.y - p1.y) / 2);
		height = Math.abs(p2.y - p1.y);
		width = Math.abs(p2.x - p1.x);
		java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
				.union(oldbounds);
		parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
		connbounds = connbounds.union(getConnBounds());
		parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
	}
	public boolean transform(int itp, Point lastPoint, Point p) {
		java.awt.Rectangle connbounds = getConnBounds();
		java.awt.Rectangle oldbounds = (this.highlighted || this.selected) ? getAnchorBounds() : getBounds();
		if (itp == TP_TOPLEFT) {
			int top, bottom;
			top = p.y;
			bottom = pos.y + height / 2;
			if ((bottom - top) % 2 != 0)
				return false;
			int left, right;
			left = p.x;
			right = pos.x + width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			return true;
		}
		if (itp == TP_TOPRIGHT) {
			int top, bottom;
			top = p.y;
			bottom = pos.y + height / 2;
			if ((bottom - top) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			int left, right;
			right = p.x;
			left = pos.x - width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			connbounds = connbounds.union(getConnBounds());
			parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
			return true;
		}
		if (itp == TP_BOTTOMLEFT) {
			int top, bottom;
			bottom = p.y;
			top = pos.y - height / 2;
			if ((bottom - top) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			int left, right;
			left = p.x;
			right = pos.x + width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			return true;
		}
		if (itp == TP_BOTTOMRIGHT) {
			int top, bottom;
			bottom = p.y;
			top = pos.y - height / 2;
			if ((bottom - top) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			int left, right;
			right = p.x;
			left = pos.x - width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			connbounds = connbounds.union(getConnBounds());
			parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
			return true;
		}
		/*
		 * height=10 pos.y=20 diffy=-2.5, -4 botom=20+5 new pos.y=25-(5+2.5)/2 (44+22/2)-(22/2+4) 55-11+4=43
		 */
		if (itp == TP_TOP) {
			int top, bottom;
			top = p.y;
			bottom = pos.y + height / 2;
			if ((bottom - top) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			connbounds = connbounds.union(getConnBounds());
			parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
			return true;
		}
		if (itp == TP_BOTTOM) {
			int top, bottom;
			bottom = p.y;
			top = pos.y - height / 2;
			if ((bottom - top) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			height = (bottom - top);
			pos.y = top + height / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			return true;
		}
		if (itp == TP_LEFT) {
			int left, right;
			left = p.x;
			right = pos.x + width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			connbounds = connbounds.union(getConnBounds());
			parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
			return true;
		}
		if (itp == TP_RIGHT) {
			int left, right;
			right = p.x;
			left = pos.x - width / 2;
			if ((right - left) % 2 != 0) {
				java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
						.union(oldbounds);
				parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
				connbounds = connbounds.union(getConnBounds());
				parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
				return false;
			}
			width = (right - left);
			pos.x = left + width / 2;
			java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
					.union(oldbounds);
			parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
			connbounds = connbounds.union(getConnBounds());
			parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
			return true;
		}
		java.awt.Rectangle bounds = ((this.highlighted || this.selected) ? getAnchorBounds() : getBounds())
				.union(oldbounds);
		parent.repaint(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
		connbounds = connbounds.union(getConnBounds());
		parent.repaint(connbounds.x, connbounds.y, connbounds.width + 1, connbounds.height + 1);
		return false;
	}
	protected void finalize() throws Throwable {
		super.finalize();
		// parent.removeMouseListener(ml);
		// parent.removeMouseMotionListener(ml);
	}
}
