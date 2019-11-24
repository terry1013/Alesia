package gui.prueckl.draw;

import java.awt.*;
import java.awt.geom.*;

public class CurveConn extends Conn {

	public CurveConn(Component parent, Figure from) {
		super(parent, from);
	}

	public void paintConnection(Graphics g, Point pFrom, Point pTo) {
		boolean pFromLeft = false;
		boolean pFromRight = false;
		boolean pFromTop = false;
		boolean pFromBottom = false;
		boolean pToLeft = false;
		boolean pToRight = false;
		boolean pToTop = false;
		boolean pToBottom = false;

		Point cp;
		cp = getFromFigure().getCenterPoint();
		if (Math.abs(pFrom.x - cp.x) > Math.abs(pFrom.y - cp.y))
			if (pFrom.x < cp.x)
				pFromLeft = true;
			else
				pFromRight = true;
		else if (pFrom.y < cp.y)
			pFromTop = true;
		else
			pFromBottom = true;
		if (getToFigure() != null) {
			cp = getToFigure().getCenterPoint();
			if (Math.abs(pTo.x - cp.x) > Math.abs(pTo.y - cp.y))
				if (pTo.x < cp.x)
					pToLeft = true;
				else
					pToRight = true;
			else if (pTo.y < cp.y)
				pToTop = true;
			else
				pToBottom = true;
		} else {
			pToRight = pFromLeft;
			pToLeft = pFromRight;
			pToTop = pFromBottom;
			pToBottom = pFromTop;
		}

		int xdistance = (int) Math.abs(pTo.getX() - pFrom.getX());
		int ydistance = (int) Math.abs(pTo.getY() - pFrom.getY());
		int xstart = (int) Math.min(pFrom.getX(), pTo.getX());
		int xend = (int) Math.max(pFrom.getX(), pTo.getX());
		int ystart = (int) Math.min(pFrom.getY(), pTo.getY());
		int yend = (int) Math.max(pFrom.getY(), pTo.getY());

		Point2D.Double startpoint = new Point2D.Double();
		Point2D.Double endpoint = new Point2D.Double();

		double cpFromx;
		double cpFromy;
		double cpTox;
		double cpToy;

		// if (xdistance>=ydistance) {
		if (pFromLeft || pFromRight) {
			if (pTo.x > pFrom.x) {
				startpoint = new Point2D.Double(pFrom.x, pFrom.y);
				endpoint = new Point2D.Double(pTo.x, pTo.y);
			} else {
				startpoint = new Point2D.Double(pTo.x, pTo.y);
				endpoint = new Point2D.Double(pFrom.x, pFrom.y);
			}

			cpFromx = pFrom.getX();// +(pTo.getX()-pFrom.getX())/3;
			cpFromy = pFrom.getY();
			cpTox = pFrom.getX() + (pTo.getX() - pFrom.getX());// /3*2;
			cpToy = pTo.getY();
		} else if (pFromTop || pFromBottom) {
			if (pTo.y > pFrom.y) {
				startpoint = new Point2D.Double(pFrom.x, pFrom.y);
				endpoint = new Point2D.Double(pTo.x, pTo.y);
			} else {
				startpoint = new Point2D.Double(pTo.x, pTo.y);
				endpoint = new Point2D.Double(pFrom.x, pFrom.y);
			}

			cpFromx = pFrom.getX();
			cpFromy = pFrom.getY();// +(pTo.getY()-pFrom.getY())/3;
			cpTox = pTo.getX();
			cpToy = pFrom.getY() + (pTo.getY() - pFrom.getY());// /3*2;
		}

		// y=ax^3+bx^2+cx+d;
		// pFromy=a*pFromx^3+b*pFromx^2+c*pFromx+d,pToy=a*pTox^3+b*pTox^2+c*pTox+d,cpFromy=a*cpFromx^3+b*cpFromx^2+c*cpFromx+d,cpToy=a*cpTox^3+b*cpTox^2+c*cpTox+d

		/*
		 * pFrom.getY()=a*pFrom.getX()^3+b*pFrom.getX()^2+c*pFrom.getX();
		 * a=(pFrom.getY()-b*pFrom.getX()^2-c*pFrom.getX())/pFrom.getX()^3;
		 * 
		 * pTo.getY()=a*pTo.getX()^3+b*pTo.getX()^2+c*pTo.getX();
		 * pTo.getY()=(pFrom.getY()-b*pFrom.getX()^2-c*pFrom.getX(
		 * ))/pFrom.getX()^3*pTo.getX()^3+b*pTo.getX()^2+c*pTo.getX();
		 * pTo.getY()*pFrom.getX()^3=(pFrom.getY()-b*pFrom.getX
		 * ()^2-c*pFrom.getX())*pTo.getX()^3+b*pTo.getX()^2+c*pTo.getX();
		 * pTo.getY()*pFrom.getX()^3=pFrom.getY()*pTo.getX
		 * ()^3-b*pFrom.getX()^2*pTo.getX()^3-c*pFrom.getX()*pTo.getX()^3+b*pTo.getX()^2+c*pTo.getX();
		 * pTo.getY()*pFrom.getX
		 * ()^3-pFrom.getY()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX()=-b*pFrom.getX()^2
		 * *pTo.getX()^3+b*pTo.getX()^2;
		 * pTo.getY()*pFrom.getX()^3-pFrom.getY()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-
		 * c*pTo.getX()=b*(-pFrom.getX()^2+pTo.getX()^2);
		 * b=(pTo.getY()*pFrom.getX()^3-pFrom.getY()*pTo.getX()^3+c*pFrom.
		 * getX()*pTo.getX()^3-c*pTo.getX())/(-pFrom.getX()^2+pTo.getX()^2);
		 * 
		 * cpy=a*cpx^3+b*cpx^2+c*cpx;
		 * cpy=(pFrom.getY()-b*pFrom.getX()^2-c*pFrom.getX())/pFrom.getX()^3*cpx^3+(pTo.getY()
		 * *pFrom.getX()^3-pFrom.getY(
		 * )*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX())/(-pFrom.getX()^2+pTo.getX()^2)*cpx^2+c*cpx;
		 * cpy=(pFrom
		 * .getY()-(pTo.getY()*pFrom.getX()^3-pFrom.getY()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX(
		 * ))/(-pFrom
		 * .getX()^2+pTo.getX()^2)*pFrom.getX()^2-c*pFrom.getX())/pFrom.getX()^3*cpx^3+(pTo.getY()*pFrom.getX()
		 * ^3-pFrom.getY
		 * ()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX())/(-pFrom.getX()^2+pTo.getX()^2)*cpx^2+c*cpx;
		 * cpy=(pFrom
		 * .getY()-(pTo.getY()*pFrom.getX()^3-pFrom.getY()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX(
		 * ))/(-pFrom
		 * .getX()^2+pTo.getX()^2)*pFrom.getX()^2-c*pFrom.getX())/pFrom.getX()^3*cpx^3+(pTo.getY()*pFrom.getX()
		 * ^3-pFrom.getY
		 * ()*pTo.getX()^3+c*pFrom.getX()*pTo.getX()^3-c*pTo.getX())/(-pFrom.getX()^2+pTo.getX()^2)*cpx^2+c*cpx; cpy=
		 * 
		 * 1/1 -> 4/4 1+(4-1)/2=1+1.5=2.5 4+(1-4)/2=4-1.4=2.5
		 */

		/*
		 * Solved with Maxima
		 * a=(((cpTox-cpFromx)*pFromx^2+(cpFromx^2-cpTox^2)*pFromx+cpFromx*cpTox^2-cpFromx^2*cpTox)*pToy+
		 * ((cpFromx-cpTox)*pFromy+(cpToy-cpFromy)*pFromx-cpFromx*cpToy+cpFromy*cpTox)*pTox^2+
		 * ((cpTox^2-cpFromx^2)*pFromy
		 * +(cpFromy-cpToy)*pFromx^2+cpFromx^2*cpToy-cpFromy*cpTox^2)*pTox+(cpFromx^2*cpTox-cpFromx*cpTox^2)*
		 * pFromy+(cpFromx*cpToy-cpFromy*cpTox)*pFromx^2+(cpFromy*cpTox^2-cpFromx^2*cpToy)*pFromx)/(
		 * ((cpTox-cpFromx)*pFromx^2+(cpFromx^2-cpTox^2)*pFromx+cpFromx*cpTox^2-cpFromx^2*cpTox)*pTox^3+
		 * ((cpFromx-cpTox)*pFromx^3+(cpTox^3-cpFromx^3)*pFromx-cpFromx*cpTox^3+cpFromx^3*cpTox)*pTox^2+
		 * ((cpTox^2-cpFromx^2)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx^2+cpFromx^2*cpTox^3-cpFromx^3*cpTox^2)*pTox+
		 * (cpFromx
		 * ^2*cpTox-cpFromx*cpTox^2)*pFromx^3+(cpFromx*cpTox^3-cpFromx^3*cpTox)*pFromx^2+(cpFromx^3*cpTox^2-cpFromx
		 * ^2*cpTox^3)*pFromx);
		 * 
		 * b=-(((cpTox-cpFromx)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx+cpFromx*cpTox^3-cpFromx^3*cpTox)*pToy+
		 * ((cpFromx-cpTox)*pFromy+(cpToy-cpFromy)*pFromx-cpFromx*cpToy+cpFromy*cpTox)*pTox^3+
		 * ((cpTox^3-cpFromx^3)*pFromy
		 * +(cpFromy-cpToy)*pFromx^3+cpFromx^3*cpToy-cpFromy*cpTox^3)*pTox+(cpFromx^3*cpTox-cpFromx*cpTox^3)*
		 * pFromy+(cpFromx*cpToy-cpFromy*cpTox)*pFromx^3+(cpFromy*cpTox^3-cpFromx^3*cpToy)*pFromx)/(
		 * ((cpTox-cpFromx)*pFromx^2+(cpFromx^2-cpTox^2)*pFromx+cpFromx*cpTox^2-cpFromx^2*cpTox)*pTox^3+
		 * ((cpFromx-cpTox)*pFromx^3+(cpTox^3-cpFromx^3)*pFromx-cpFromx*cpTox^3+cpFromx^3*cpTox)*pTox^2+
		 * ((cpTox^2-cpFromx^2)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx^2+cpFromx^2*cpTox^3-cpFromx^3*cpTox^2)*pTox+
		 * (cpFromx
		 * ^2*cpTox-cpFromx*cpTox^2)*pFromx^3+(cpFromx*cpTox^3-cpFromx^3*cpTox)*pFromx^2+(cpFromx^3*cpTox^2-cpFromx
		 * ^2*cpTox^3)*pFromx);
		 * 
		 * c=(((cpTox^2-cpFromx^2)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx^2+cpFromx^2*cpTox^3-cpFromx^3*cpTox^2)*pToy+
		 * ((cpFromx^2-cpTox^2)*pFromy+(cpToy-cpFromy)*pFromx^2-cpFromx^2*cpToy+cpFromy*cpTox^2)*pTox^3+
		 * ((cpTox^3-cpFromx
		 * ^3)*pFromy+(cpFromy-cpToy)*pFromx^3+cpFromx^3*cpToy-cpFromy*cpTox^3)*pTox^2+(cpFromx^3*cpTox^
		 * 2-cpFromx^2*cpTox^3)*
		 * pFromy+(cpFromx^2*cpToy-cpFromy*cpTox^2)*pFromx^3+(cpFromy*cpTox^3-cpFromx^3*cpToy)*pFromx^2)/(
		 * ((cpTox-cpFromx)*pFromx^2+(cpFromx^2-cpTox^2)*pFromx+cpFromx*cpTox^2-cpFromx^2*cpTox)*pTox^3+
		 * ((cpFromx-cpTox)*pFromx^3+(cpTox^3-cpFromx^3)*pFromx-cpFromx*cpTox^3+cpFromx^3*cpTox)*pTox^2+
		 * ((cpTox^2-cpFromx^2)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx^2+cpFromx^2*cpTox^3-cpFromx^3*cpTox^2)*pTox+
		 * (cpFromx
		 * ^2*cpTox-cpFromx*cpTox^2)*pFromx^3+(cpFromx*cpTox^3-cpFromx^3*cpTox)*pFromx^2+(cpFromx^3*cpTox^2-cpFromx
		 * ^2*cpTox^3)*pFromx);
		 * 
		 * d=-(
		 * ((cpFromx*cpTox^2-cpFromx^2*cpTox)*pFromx^3+(cpFromx^3*cpTox-cpFromx*cpTox^3)*pFromx^2+(cpFromx^2*cpTox^3
		 * -cpFromx^3*cpTox^2)*pFromx)*
		 * pToy+((cpFromx^2*cpTox-cpFromx*cpTox^2)*pFromy+(cpFromx*cpToy-cpFromy*cpTox)*pFromx
		 * ^2+(cpFromy*cpTox^2-cpFromx^2*cpToy)*pFromx)*
		 * pTox^3+((cpFromx*cpTox^3-cpFromx^3*cpTox)*pFromy+(cpFromy*cpTox-
		 * cpFromx*cpToy)*pFromx^3+(cpFromx^3*cpToy-cpFromy*cpTox^3)*pFromx)* pTox^2+
		 * ((cpFromx^3*cpTox^2-cpFromx^2*cpTox
		 * ^3)*pFromy+(cpFromx^2*cpToy-cpFromy*cpTox^2)*pFromx^3+(cpFromy*cpTox^3-cpFromx^3*cpToy)*pFromx^2)*
		 * pTox)/(((cpTox-cpFromx)*pFromx^2+(cpFromx^2-cpTox^2)*pFromx+cpFromx*cpTox^2-cpFromx^2*cpTox)*pTox^3+
		 * ((cpFromx-cpTox)*pFromx^3+(cpTox^3-cpFromx^3)*pFromx-cpFromx*cpTox^3+cpFromx^3*cpTox)*pTox^2+
		 * ((cpTox^2-cpFromx^2)*pFromx^3+(cpFromx^3-cpTox^3)*pFromx^2+cpFromx^2*cpTox^3-cpFromx^3*cpTox^2)*pTox+
		 * (cpFromx
		 * ^2*cpTox-cpFromx*cpTox^2)*pFromx^3+(cpFromx*cpTox^3-cpFromx^3*cpTox)*pFromx^2+(cpFromx^3*cpTox^2-cpFromx
		 * ^2*cpTox^3)*pFromx);
		 */

		// g.drawLine(pFrom.x, pFrom.y, (int)cpFromx, (int)cpFromy);
		// g.drawLine((int)cpTox, (int)cpToy, pTo.x, pTo.y);

		Point2D.Double pStart = new Point2D.Double();
		Point2D.Double pEnd = new Point2D.Double();

		// if (xdistance>=ydistance) {
		if ((pFromLeft || pFromRight) && (pToLeft || pToRight)) {
			// if ((pFromLeft || pFromRight)) {
			int y;
			int lastx = (int) startpoint.x, lasty = -1;
			for (int x = (int) startpoint.x; x <= endpoint.x; x++) {
				y = (int) Math.round(funcSigmoid(x, startpoint, endpoint));
				if (lasty == -1)
					g.drawLine(x, y, x, y);
				else
					g.drawLine(lastx, lasty, x, y);
				lastx = x;
				lasty = y;
			}
		} else if ((pFromTop || pFromBottom) && (pToTop || pToBottom)) {
			// } else if ((pFromTop || pFromBottom)) {
			int x;
			int lasty = (int) startpoint.y, lastx = -1;
			for (int y = (int) startpoint.y; y <= endpoint.y; y++) {
				x = (int) Math.round(funcSigmoid(y, new Point2D.Double(startpoint.y, startpoint.x), new Point2D.Double(
						endpoint.y, endpoint.x)));
				if (lastx == -1)
					g.drawLine(x, y, x, y);
				else
					g.drawLine(lastx, lasty, x, y);
				lastx = x;
				lasty = y;
			}
		} else {
			Point2D.Double[] points = new Point2D.Double[3];

			if (pFromRight && pToTop) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pTo.x, pFrom.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromRight && pToBottom) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pTo.x, pFrom.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromLeft && pToTop) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pTo.x, pFrom.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromLeft && pToBottom) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pTo.x, pFrom.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromTop && pToRight) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pFrom.x, pTo.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromTop && pToLeft) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pFrom.x, pTo.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromBottom && pToRight) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pFrom.x, pTo.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			} else if (pFromBottom && pToLeft) {
				points[0] = new Point2D.Double(pFrom.x, pFrom.y);
				points[1] = new Point2D.Double(pFrom.x, pTo.y);
				points[2] = new Point2D.Double(pTo.x, pTo.y);
			}

			{
				Point2D.Double lastpoint = null;
				Point2D.Double point;
				for (double t = 0.0; t <= 1.0; t += 0.01) {
					point = funcBezier(t, points);
					if (lastpoint == null)
						g.drawLine((int) point.x, (int) point.y, (int) point.x, (int) point.y);
					else
						g.drawLine((int) lastpoint.x, (int) lastpoint.y, (int) point.x, (int) point.y);
					lastpoint = point;
				}
				/*
				 * width=(pTo.x-pFrom.x)*2; height=(pTo.y-pFrom.y)*2; g.drawArc(pFrom.x-width/2, pFrom.y, width, height,
				 * 0, 90);
				 */

				/*
				 * pStart.x=pFrom.x; pStart.y=pFrom.y; pEnd.x=pTo.x+(pTo.x-pFrom.x); pEnd.y=pTo.y+(pTo.y-pFrom.y); int
				 * y; int lastx=(int)pFrom.x, lasty=-1; for (int x=(int)pFrom.x; x<=pTo.x; x++) {
				 * y=(int)Math.round(funcCurve(x, pStart, pEnd)); if (lasty==-1) g.drawLine(x, y, x, y); else
				 * g.drawLine(lastx, lasty, x, y); lastx=x; lasty=y; }
				 */

			}
		}
	}

	protected Point2D.Double funcBezier(double t, Point2D.Double... points) {
		Point2D.Double ret = new Point2D.Double();
		int i = 0;

		for (Point2D.Double point : points) {
			ret.x += MathUtils.factorial(points.length - 1)
					/ (MathUtils.factorial(i) * MathUtils.factorial(points.length - 1 - i)) * Math.pow(t, i)
					* Math.pow(1 - t, points.length - 1 - i) * point.x;
			ret.y += MathUtils.factorial(points.length - 1)
					/ (MathUtils.factorial(i) * MathUtils.factorial(points.length - 1 - i)) * Math.pow(t, i)
					* Math.pow(1 - t, points.length - 1 - i) * point.y;
			i++;
		}

		return ret;
	}

	protected double funcSin(double x, Point2D.Double cpFrom, Point2D.Double cpTo) {
		/*
		 * double y=((((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(), 2)+(Math.pow(cpFrom.getX(),
		 * 2)-Math.pow(cpTo.getX(), 2))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX())*pTo.getY()+
		 * ((cpFrom.getX()-cpTo.getX())*pFrom.getY()+(cpTo.getY()-cpFrom.getY())*pFrom.getX()
		 * -cpFrom.getX()*cpTo.getY()+cpFrom.getY()*cpTo.getX())*Math.pow(pTo.getX(), 2)+ ((Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2))*pFrom.getY()+(cpFrom.getY()-cpTo.getY())*Math.pow(pFrom.getX(),
		 * 2)+Math.pow(cpFrom.getX(), 2)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(),
		 * 2))*pTo.getX()+(Math.pow(cpFrom.getX(), 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 2))*
		 * pFrom.getY()+(cpFrom.getX()*cpTo.getY()-cpFrom.getY()*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 2)+(cpFrom.getY()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*cpTo.getY())*pFrom.getX())/(
		 * ((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(), 2)+(Math.pow(cpFrom.getX(), 2)-Math.pow(cpTo.getX(),
		 * 2))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX())*Math.pow(pTo.getX(), 3)+ ((cpFrom.getX()-cpTo.getX())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3))*pFrom.getX()-cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 3)+Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pTo.getX(), 2)+ ((Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2))*Math.pow(pFrom.getX(), 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(),
		 * 3))*Math.pow(pFrom.getX(), 2)+Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*Math.pow(cpTo.getX(), 2))*pTo.getX()+ (Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 2))*Math.pow(pFrom.getX(),
		 * 3)+(cpFrom.getX()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(),
		 * 3))*pFrom.getX())) * Math.pow(x, 3) + (-(((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(), 3))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getX())*pTo.getY()+
		 * ((cpFrom.getX()-cpTo.getX())*pFrom.getY()+(cpTo.getY()-
		 * cpFrom.getY())*pFrom.getX()-cpFrom.getX()*cpTo.getY()+cpFrom.getY()*cpTo.getX())*Math.pow(pTo.getX(), 3)+
		 * ((Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3))*pFrom.getY()+(cpFrom.getY()-cpTo.getY())*Math.pow(pFrom.getX(), 3)+Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(), 3))*pTo.getX()+(Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 3))*
		 * pFrom.getY()+(cpFrom.getX()*cpTo.getY()-cpFrom.getY()*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 3)+(cpFrom.getY()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getY())*pFrom.getX())/(
		 * ((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(), 2)+(Math.pow(cpFrom.getX(), 2)-Math.pow(cpTo.getX(),
		 * 2))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX())*Math.pow(pTo.getX(), 3)+ ((cpFrom.getX()-cpTo.getX())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3))*pFrom.getX()-cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 3)+Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pTo.getX(), 2)+ ((Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2))*Math.pow(pFrom.getX(), 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(),
		 * 3))*Math.pow(pFrom.getX(), 2)+Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*Math.pow(cpTo.getX(), 2))*pTo.getX()+ (Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 2))*Math.pow(pFrom.getX(),
		 * 3)+(cpFrom.getX()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(),
		 * 3))*pFrom.getX())) * Math.pow(x, 2) + ((((Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2))*Math.pow(pFrom.getX(), 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(), 3))*Math.pow(pFrom.getX(),
		 * 2)+Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(),
		 * 2))*pTo.getY()+ ((Math.pow(cpFrom.getX(), 2)-Math.pow(cpTo.getX(),
		 * 2))*pFrom.getY()+(cpTo.getY()-cpFrom.getY())*Math.pow(pFrom.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getY()+cpFrom.getY()*Math.pow(cpTo.getX(), 2))*Math.pow(pTo.getX(), 3)+ ((Math.pow(cpTo.getX(),
		 * 3)-Math.pow(cpFrom.getX(), 3))*pFrom.getY()+(cpFrom.getY()-cpTo.getY())*Math.pow(pFrom.getX(),
		 * 3)+Math.pow(cpFrom.getX(), 3)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(), 3))*Math.pow(pTo.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3))*
		 * pFrom.getY()+(Math.pow(cpFrom.getX(), 2)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(),
		 * 2))*Math.pow(pFrom.getX(), 3)+(cpFrom.getY()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getY())*Math.pow(pFrom.getX(), 2))/( ((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 2)-Math.pow(cpTo.getX(), 2))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2)*cpTo.getX())*Math.pow(pTo.getX(), 3)+
		 * ((cpFrom.getX()-cpTo.getX())*Math.pow(pFrom.getX(), 3)+(Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3))*pFrom.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 3)+Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getX())*Math.pow(pTo.getX(), 2)+ ((Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2))*Math.pow(pFrom.getX(), 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(), 3))*Math.pow(pFrom.getX(),
		 * 2)+Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(),
		 * 2))*pTo.getX()+ (Math.pow(cpFrom.getX(), 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 2))*Math.pow(pFrom.getX(), 3)+(cpFrom.getX()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getX())*Math.pow(pFrom.getX(), 2)+(Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3))*pFrom.getX())) * x +
		 * (-(((cpFrom.getX()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpFrom.getX(), 3)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 3))*Math.pow(pFrom.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(),
		 * 2))*pFrom.getX())* pTo.getY()+((Math.pow(cpFrom.getX(), 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 2))*pFrom.getY()+(cpFrom.getX()*cpTo.getY()-cpFrom.getY()*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 2)+(cpFrom.getY()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*cpTo.getY())*pFrom.getX())*
		 * Math.pow(pTo.getX(), 3)+((cpFrom.getX()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*cpTo.getX())*pFrom.getY()+(cpFrom.getY()*cpTo.getX()-cpFrom.getX()*cpTo.getY())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpFrom.getX(), 3)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(), 3))*pFrom.getX())*
		 * Math.pow(pTo.getX(), 2)+ ((Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*Math.pow(cpTo.getX(), 3))*pFrom.getY()+(Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getY()-cpFrom.getY()*Math.pow(cpTo.getX(), 2))*Math.pow(pFrom.getX(),
		 * 3)+(cpFrom.getY()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getY())*Math.pow(pFrom.getX(),
		 * 2))* pTo.getX())/(((cpTo.getX()-cpFrom.getX())*Math.pow(pFrom.getX(), 2)+(Math.pow(cpFrom.getX(),
		 * 2)-Math.pow(cpTo.getX(), 2))*pFrom.getX()+cpFrom.getX()*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX())*Math.pow(pTo.getX(), 3)+ ((cpFrom.getX()-cpTo.getX())*Math.pow(pFrom.getX(),
		 * 3)+(Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3))*pFrom.getX()-cpFrom.getX()*Math.pow(cpTo.getX(),
		 * 3)+Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pTo.getX(), 2)+ ((Math.pow(cpTo.getX(),
		 * 2)-Math.pow(cpFrom.getX(), 2))*Math.pow(pFrom.getX(), 3)+(Math.pow(cpFrom.getX(), 3)-Math.pow(cpTo.getX(),
		 * 3))*Math.pow(pFrom.getX(), 2)+Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(),
		 * 3)*Math.pow(cpTo.getX(), 2))*pTo.getX()+ (Math.pow(cpFrom.getX(),
		 * 2)*cpTo.getX()-cpFrom.getX()*Math.pow(cpTo.getX(), 2))*Math.pow(pFrom.getX(),
		 * 3)+(cpFrom.getX()*Math.pow(cpTo.getX(), 3)-Math.pow(cpFrom.getX(), 3)*cpTo.getX())*Math.pow(pFrom.getX(),
		 * 2)+(Math.pow(cpFrom.getX(), 3)*Math.pow(cpTo.getX(), 2)-Math.pow(cpFrom.getX(), 2)*Math.pow(cpTo.getX(),
		 * 3))*pFrom.getX()));
		 */

		double xdist = cpTo.x - cpFrom.x;
		double ydist = cpTo.y - cpFrom.y;
		double y = ydist / 2 - ydist / 2
				* Math.sin(Math.PI / 2 + (xdist != 0.d ? Math.PI * (x - cpFrom.x) / xdist : 0));
		y = cpFrom.y + y;
		return y;
	}

	protected double funcSigmoid(double x, Point2D.Double cpFrom, Point2D.Double cpTo) {
		double xdist = cpTo.x - cpFrom.x;
		double ydist = cpTo.y - cpFrom.y;
		double y = 1.d / (1.d + Math.pow(Math.E, -(x - cpFrom.x - xdist / 2) / (xdist / 10)));
		y = cpFrom.y + ydist * y;
		return y;
	}
}
