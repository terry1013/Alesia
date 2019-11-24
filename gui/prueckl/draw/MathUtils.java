package gui.prueckl.draw;

import java.awt.geom.*;
import java.util.*;

public class MathUtils {
	public static double round(double value, int postcomma) {
		int i = (int) (value * Math.pow(10, postcomma));
		double d = (double) i / Math.pow(10, postcomma);
		return d;
	}

	public static double between(double from, double to, double value) {
		return Math.max(from, Math.min(to, value));
	}

	public static long factorial(long value) {
		long fact = 1;

		for (long f = 1; f <= value; f++) {
			fact *= f;
		}

		return fact;
	}

	public enum Quadrant {
		TopRight, BottomRight, BottomLeft, TopLeft, CenterNone
	};

	public static Quadrant quadrantOfVector(Point2D.Double vector) {
		if (vector.x > 0 && vector.y > 0)
			return Quadrant.BottomRight;
		if (vector.x > 0 && vector.y < 0)
			return Quadrant.TopRight;
		if (vector.x < 0 && vector.y > 0)
			return Quadrant.BottomLeft;
		if (vector.x < 0 && vector.y < 0)
			return Quadrant.TopLeft;
		return Quadrant.CenterNone;
	}

	public static Quadrant quadrantOfAngle(double angle) {
		if (angle >= 0.0 && angle < 0.5 * Math.PI)
			return Quadrant.TopRight;
		if (angle >= 0.5 * Math.PI && angle < Math.PI)
			return Quadrant.BottomRight;
		if (angle >= Math.PI && angle < 1.5 * Math.PI)
			return Quadrant.BottomLeft;
		if (angle >= 1.5 * Math.PI && angle < 2 * Math.PI)
			return Quadrant.BottomLeft;
		return Quadrant.CenterNone;
	}

	/**
	 * Calculates the angle between x and y in the unit circle
	 * 
	 * @param vectorX
	 * @param vectorY
	 * @return angle in radiants
	 */
	public static double angleOfVector(double vectorX, double vectorY) {
		double radiantAngle;

		if (vectorX != 0)
			radiantAngle = Math.atan(Math.abs(vectorY) / Math.abs(vectorX));
		else
			radiantAngle = Math.PI / 2;

		if (vectorX >= 0 && vectorY >= 0) {
			return Math.PI / 2 - radiantAngle;
		}
		if (vectorX >= 0 && vectorY <= 0) {
			return Math.PI / 2 + radiantAngle;
		}
		if (vectorX <= 0 && vectorY <= 0) {
			return Math.PI + (Math.PI / 2 - radiantAngle);
		}
		if (vectorX <= 0 && vectorY >= 0) {
			return Math.PI + Math.PI / 2 + radiantAngle;
		}
		return Double.NaN;
	}

	public static Point2D.Double funcBezier(double t, Point2D.Double... points) {
		if (points.length < 2)
			return null;
		Point2D.Double ret = new Point2D.Double();
		int i = 0;

		double denominator;
		for (Point2D.Double point : points) {
			denominator = (MathUtils.factorial(i) * MathUtils.factorial(points.length - 1 - i));
			if (denominator != 0.0) {
				ret.x += MathUtils.factorial(points.length - 1) / denominator * Math.pow(t, i)
						* Math.pow(1 - t, points.length - 1 - i) * point.x;
				ret.y += MathUtils.factorial(points.length - 1) / denominator * Math.pow(t, i)
						* Math.pow(1 - t, points.length - 1 - i) * point.y;
			}
			i++;
		}

		return ret;
	}

	public static Point2D.Double funcCubicSpline(double x, Point2D.Double... points) {
		if (points.length < 4)
			return null;

		Point2D.Double ret = new Point2D.Double();
		ret.x = x;

		double splineBase;
		int p = points.length * 2 - points.length - 1;
		int i = 0;
		for (Point2D.Double point : points) {
			if (i > 0 && i < points.length - 2) {
				splineBase = cubicSpline(x, points[i - 1], points[i], points[i + 1], points[i + 2]);
				// ret.x+=point.x*splineBase;
				ret.y += splineBase;
			}
			i++;
		}

		return ret;
	}

	public static double S(double xi, double yi, double xi1, double yi1, double xim1, double yim1) {
		double Si = 6 * ((yi1 - yi) / (xi1 - xi) - (yi - yim1) / (xi - xim1));
		return Si;
	}

	/**
	 * 
	 * @param t time value in the range from 0 to points count minus 2
	 * @param points array of points for NURBS curve
	 * @return
	 */
	public static Point2D.Double simpleNURBS(double t, Point2D.Double... points) {
		int order = 4;

		Vector<Double> T = new Vector<Double>();

		for (int i = 0; i < order; i++) {
			T.add(0.0d);
		}
		for (int i = 1; i <= points.length - 3; i++) {
			T.add((double) i);
		}
		for (int i = 0; i < order; i++) {
			T.add((double) (points.length - 2));
		}

		Vector<Double> w = new Vector<Double>();
		for (int i = 0; i < points.length; i++) {
			w.add(1.d);
		}

		return NURBS(t, order, T, w, points);
	}

	/**
	 * 
	 * @param t time in values of the knotvector
	 * @param k order = degree of NURBS plus one (degree is usually 1, 2, 3 or 5)
	 * @param T knotvector (degree plus number of control points minus one) (eg 0,0,0,1,2,2,2,3,7,7,9,9,9)
	 * @param w weightvector of (control)points (a weight of 1 is non-rational, otherwise rational)
	 * @param points controlpoints (at least degree plus one points)
	 * @return
	 */
	public static Point2D.Double NURBS(double t, int k, Vector<Double> T, Vector<Double> w, Point2D.Double... points) {
		Point2D.Double ret = new Point2D.Double();

		double ratBSpline;
		int i = 0;
		for (Point2D.Double point : points) {
			ratBSpline = rationalBSpline(t, i, k, T, w);
			ret.x += point.x * ratBSpline;
			ret.y += point.y * ratBSpline;
			i++;
		}

		return ret;
	}

	public static double rationalBSpline(double t, int i, int k, Vector<Double> T, Vector<Double> w) {
		double ret;

		double denominator = 0.d;
		int j = 0;
		for (double wj : w) {
			denominator += BSpline(j, k, T, t) * wj;
			j++;
		}

		ret = (BSpline(i, k, T, t) * w.get(i)) / denominator;

		return ret;
	}

	/**
	 * 
	 * @param t value between T[p] and T[n-p+1]
	 * @param T knot vector
	 * @param p
	 * @param points
	 * @return
	 */
	public static Point2D.Double BSplineCurve(double t, Vector<Double> T, int k, Point2D.Double... points) {
		Point2D.Double ret = new Point2D.Double();
		double bSpline;

		for (int i = 0; i < points.length; i++) {
			bSpline = BSpline(i, k, T, t);
			ret.x += points[i].x * bSpline;
			ret.y += points[i].y * bSpline;
		}

		return ret;
	}

	public static double BSpline(int i, int k, Vector<Double> T, double t) {
		double ret = 0.d;

		try {
			/*
			 * if (i<0 || i>=T.size()) return 0.d;
			 */
			if (k == 1) {
				if (T.get(i) <= t && t <= T.get(i + 1))
					return 1.d;
				else
					return 0.d;
			}
			/*
			 * if (k<=0) return 1.d;
			 */
			double factor1, factor2;
			if (T.get(i + k - 1).doubleValue() - T.get(i).doubleValue() == 0.d)
				factor1 = 0.d;
			else
				factor1 = (t - T.get(i).doubleValue()) / (T.get(i + k - 1).doubleValue() - T.get(i).doubleValue())
						* BSpline(i, k - 1, T, t);
			if (T.get(i + k).doubleValue() - T.get(i + 1).doubleValue() == 0.d)
				factor2 = 0.d;
			else
				factor2 = (T.get(i + k).doubleValue() - t) / (T.get(i + k).doubleValue() - T.get(i + 1).doubleValue())
						* BSpline(i + 1, k - 1, T, t);
			ret = factor1 + factor2;
			// System.out.println("BSpline is "+ret);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// ex.printStackTrace();
		}

		if (Double.isNaN(ret) || Double.isInfinite(ret))
			return 0.d;

		return ret;
	}

	public static double cubicSpline(double x, Point2D.Double pminus1, Point2D.Double p, Point2D.Double pplus1,
			Point2D.Double pplus2) {
		double ret;

		// ret=(t-ti)/(tij-ti)*splineBase(t, i, p-1)+(tip1-t)/(tip1-ti1))*splineBase(t, i+1, p-1);

		double ai = (S(pplus1.x, pplus1.y, pplus2.x, pplus2.y, p.x, p.y) - S(p.x, p.y, pplus1.x, pplus1.y, pminus1.x,
				pminus1.y)) / (6 * (pplus1.x - p.x));
		double bi = S(p.x, p.y, pplus1.x, pplus1.y, pminus1.x, pminus1.y) / 2;
		double ci = (pplus1.y - p.y)
				/ (pplus1.x - p.x)
				- (2 * (pplus1.x - p.x) * S(p.x, p.y, pplus1.x, pplus1.y, pminus1.x, pminus1.y) + ((pplus1.x - p.x) * S(
						pplus1.x, pplus1.y, pplus2.x, pplus2.y, p.x, p.y))) / 6;
		ci = (pplus1.y - p.y) / (pplus1.x - p.x) - S(pplus1.x, pplus1.y, pplus2.x, pplus2.y, p.x, p.y)
				* (pplus1.x - p.x) / 6 - S(p.x, p.y, pplus1.x, pplus1.y, pminus1.x, pminus1.y) * (pplus1.x - p.x) / 3;
		double di = p.y;

		ret = di + ci * (x - p.x) + bi * Math.pow(x - p.x, 2) + ai * Math.pow(x - p.x, 3);

		return ret;
	}

	public static class EquationPart implements Cloneable {
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}

	public static class Value extends EquationPart {

	}

	public static class Variable extends Value {
		public String name;
		public String toString() {
			return name;
		}
		public boolean equals(Object o) {
			if (!(o instanceof Variable))
				return false;
			else
				return name.equals(((Variable) o).name);
		}
	}

	public static class Rational extends Value {
		public double value;
		public String toString() {
			return String.valueOf(value);
		}
	}

	public static abstract class Operation extends EquationPart {
		public abstract Rational perform(Rational value1, Rational value2);
	}

	public static abstract class LineOperation extends Operation {
	}

	public static abstract class DotOperation extends Operation {
		public Parenthesis perform(Parenthesis value1, Rational value2) {
			Vector<EquationPart> term, term2;
			Rational rat;
			boolean found;
			int i = 0;
			while ((term = value1.getTerm(i)) != null) {
				found = false;
				if (term.size() == 1 && term.get(0) instanceof Parenthesis) {
					term2 = ((Parenthesis) term.get(0)).parts;
				} else {
					term2 = term;
				}
				for (EquationPart part : term2) {
					if (part instanceof Rational) {
						rat = perform((Rational) part, value2);
						((Rational) part).value = rat.value;
						found = true;
						break;
					}
				}
				if (!found) {
					Parenthesis par = new Parenthesis();
					rat = new Rational();
					rat.value = 1;
					rat = perform(rat, value2);
					par.parts.add(rat);
					par.parts.add(new Multiply());
					par.parts.addAll(term);
					value1.removeTerm(i);
					value1.insertTerm(i, par.parts);
				}
				i++;
			}
			return value1;
		}
		public Parenthesis perform(Parenthesis par, Vector<EquationPart> term) {
			Vector<EquationPart> term2;
			Parenthesis par3 = null;
			Rational rat;
			boolean found;
			int i = 0;
			while ((term2 = par.getTerm(i)) != null) {
				found = false;
				if (Parenthesis.areTermsCompatible(term2, term)) {
					par3.parts = Parenthesis.combineTerms(term2, term, this);
					par.removeTerm(i);
					par.insertTerm(i, par3.parts);
				} else {
					try {
						term.add((DotOperation) clone());
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					term2.addAll(term);
					par.removeTerm(i);
					par.insertTerm(i, term);
				}
				i++;
			}
			return par;
		}
		public Parenthesis perform(Parenthesis par, Variable var) {
			Vector<EquationPart> term;
			Parenthesis par2 = null;
			par2 = new Parenthesis();
			par2.parts.add(var);
			Parenthesis par3 = null;
			Rational rat;
			boolean found;
			int i = 0;
			while ((term = par.getTerm(i)) != null) {
				found = false;
				if (Parenthesis.areTermsCompatible(term, par2.parts)) {
					par3.parts = Parenthesis.combineTerms(term, par2.parts, this);
					par.removeTerm(i);
					par.insertTerm(i, par3.parts);
				} else {
					try {
						term.add((DotOperation) clone());
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					term.add(var);
					par.removeTerm(i);
					par.insertTerm(i, term);
				}
				i++;
			}
			return par;
		}
	}

	public static class Multiply extends DotOperation {
		public Rational perform(Rational value1, Rational value2) {
			Rational rat = new Rational();
			rat.value = value1.value * value2.value;
			return rat;
		}
		public String toString() {
			return "*";
		}
	}

	public static class Divide extends DotOperation {
		public Rational perform(Rational value1, Rational value2) {
			Rational rat = new Rational();
			rat.value = value1.value / value2.value;
			return rat;
		}
		public String toString() {
			return "/";
		}
	}

	public static class Plus extends LineOperation {
		public Rational perform(Rational value1, Rational value2) {
			Rational rat = new Rational();
			rat.value = value1.value + value2.value;
			return rat;
		}
		public String toString() {
			return "+";
		}
	}

	public static class Minus extends LineOperation {
		public Rational perform(Rational value1, Rational value2) {
			Rational rat = new Rational();
			rat.value = value1.value - value2.value;
			return rat;
		}
		public String toString() {
			return "-";
		}
	}

	public static class Power extends DotOperation {
		public Rational perform(Rational value1, Rational value2) {
			Rational rat = new Rational();
			rat.value = Math.pow(value1.value, value2.value);
			return rat;
		}
		public String toString() {
			return "^";
		}
	}

	public static class Parenthesis extends EquationPart {
		public Vector<EquationPart> parts = new Vector<EquationPart>();

		public int getTermCount() {
			int i = 0;
			int partindex = 0;
			for (EquationPart part : parts) {
				if (part instanceof Plus || part instanceof Minus) {
					i++;
				}
				partindex++;
			}
			return i;
		}

		public void removeTerm(int index) {
			int i = 0;
			for (EquationPart part : parts) {
				if (part instanceof Plus || part instanceof Minus) {
					i++;
				}
				if (i == index) {
					int startindex = parts.indexOf(part);
					parts.removeElement(part);
					part = parts.elementAt(startindex);
					try {
						while (!(part instanceof Plus) && !(part instanceof Minus)) {
							parts.remove(startindex);
							part = parts.elementAt(startindex);
						}
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
					break;
				}
			}
		}

		public void insertTerm(int index, Vector<EquationPart> term) {
			Parenthesis par = null;
			if (term.size() > 1) {
				par = new Parenthesis();
				par.parts.addAll(term);
			}
			int i = 0;
			int partindex = 0;
			for (EquationPart part : parts) {
				if (i == index) {
					break;
				}
				if (part instanceof Plus || part instanceof Minus) {
					i++;
				}
				partindex++;
			}
			if (par != null)
				parts.insertElementAt(par, partindex);
			else
				parts.addAll(partindex, term);
		}

		public Vector<EquationPart> getTerm(int index) {
			Vector<EquationPart> term = new Vector<EquationPart>();
			int i = 0;
			for (EquationPart part : parts) {
				if (part instanceof Plus || part instanceof Minus) {
					if (i == index) {
						break;
					}
					i++;
				} else {
					if (i == index) {
						term.add(part);
					}
				}
			}
			if (term.isEmpty())
				return null;
			else
				return term;
		}

		public static Hashtable<Variable, Double> getVariablesOfTerm(Vector<EquationPart> term) {
			Hashtable<Variable, Double> vars2Degree = new Hashtable<Variable, Double>();
			for (EquationPart part : term) {
				if (part instanceof Variable)
					vars2Degree.put((Variable) part, 1.d);
				else if ((part instanceof Parenthesis || part instanceof Equation)) {
					Parenthesis par = (Parenthesis) part;
					Variable var = null;
					boolean power = false;
					Rational rat = null;
					for (EquationPart part2 : par.parts) {
						if (part2 instanceof Variable)
							var = (Variable) part2;
						else if (part2 instanceof Power) {
							power = true;
						} else if (part2 instanceof Rational) {
							rat = (Rational) part2;
						}
					}
					if (var != null)
						vars2Degree.put((Variable) var, rat.value);
				}
			}
			return vars2Degree;
		}

		public static boolean areTermsCompatible(Vector<EquationPart> term1, Vector<EquationPart> term2) {
			Hashtable<Variable, Double> term1Vars = getVariablesOfTerm(term1);
			Hashtable<Variable, Double> term2Vars = getVariablesOfTerm(term2);
			if (term1Vars.size() == term2Vars.size()) {
				boolean notfound = false;
				for (Variable var : term2Vars.keySet()) {
					if (!term1Vars.contains(var) || !term1Vars.get(var).equals(term2Vars.get(var))) {
						notfound = true;
						break;
					}
				}
				if (notfound)
					return false;
			}
			return true;
		}

		public static Vector<EquationPart> combineTerms(Vector<EquationPart> term1, Vector<EquationPart> term2,
				Operation op) {
			Rational rat1 = Parenthesis.getRationalOfTerm(term1);
			Rational rat2 = Parenthesis.getRationalOfTerm(term2);
			Rational rat = op.perform(rat1, rat2);
			Rational ratPower;
			Vector<EquationPart> term = new Vector<EquationPart>();
			term.add(rat);
			Hashtable<Variable, Double> vars = Parenthesis.getVariablesOfTerm(term1);
			Parenthesis par;
			for (Variable var : vars.keySet()) {
				term.add(new Multiply());
				par = new Parenthesis();
				par.parts.add(var);
				par.parts.add(new Power());
				ratPower = new Rational();
				ratPower.value = vars.get(var);
				par.parts.add(ratPower);
				term.add(par);
			}
			return term;
		}

		public static Rational getRationalOfTerm(Vector<EquationPart> term) {
			Rational rat = new Rational();
			rat.value = 1.d;
			int i = 0;
			for (EquationPart part : term) {
				if (part instanceof Rational) {
					rat.value = ((Rational) part).value;
					break;
				}
				i++;
			}
			return rat;
		}

		public boolean replace(EquationPart oldpart, EquationPart newpart) {
			int i = 0;
			boolean found = false;
			Vector<EquationPart> foundParts = new Vector<EquationPart>();
			for (EquationPart part : parts) {
				if (part.equals(oldpart)) {
					foundParts.add(part);
					found = true;
				} else if (part instanceof Parenthesis || part instanceof Equation) {
					found = ((Parenthesis) part).replace(oldpart, newpart) || found;
				}
				i++;
			}
			for (EquationPart part : foundParts) {
				i = parts.indexOf(part);
				parts.removeElementAt(i);
				try {
					parts.insertElementAt((EquationPart) (newpart.clone()), i);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return found;
		}
		public String toString() {
			String s = "";
			for (EquationPart part : parts) {
				s += part.toString();
			}
			return "(" + s + ")";
		}
		public void simplify() {
			boolean nothingChanged = true;
			do {
				nothingChanged = true;
				Parenthesis par = null, lastPar = null;
				Rational lastRat = null;
				Value lastValue = null;
				Variable lastVariable = null;
				Operation op = null;
				DotOperation dotop = null;
				Rational rat;
				int index;
				for (EquationPart part : parts) {
					if (part instanceof Equation || part instanceof Parenthesis) {
						lastPar = par;
						par = (Parenthesis) part;
						par.simplify();
						if (par.parts.size() == 1) {
							index = parts.indexOf(par);
							parts.removeElement(par);
							parts.insertElementAt(par.parts.get(0), index);
							nothingChanged = false;
							break;
						}
						if (lastRat != null && dotop != null) {
							dotop.perform(par, lastRat);
							parts.remove(lastRat);
							parts.remove(dotop);
							nothingChanged = false;
							break;
						} else if (lastVariable != null && dotop != null) {
							dotop.perform(par, lastVariable);
							parts.remove(lastVariable);
							parts.remove(dotop);
							nothingChanged = false;
							break;
						} else if (lastPar != null) {
							if (lastPar.getTermCount() == 1) {
								dotop.perform(par, lastPar.getTerm(0));
							} else if (par.getTermCount() == 1) {
								dotop.perform(lastPar, par.getTerm(0));
							}
							nothingChanged = false;
							break;
						}
					} else if (part instanceof Rational) {
						if (lastRat != null && dotop != null) {
							rat = dotop.perform(lastRat, (Rational) part);
							index = parts.indexOf(lastRat);
							parts.removeElement(lastRat);
							parts.removeElement(dotop);
							parts.removeElement(part);
							parts.insertElementAt(rat, index);
							nothingChanged = false;
							break;
						} else if (par != null && dotop != null) {
							dotop.perform(par, (Rational) part);
							parts.remove(part);
							parts.remove(dotop);
							nothingChanged = false;
							break;
						} else if (lastRat == null) {
							lastRat = (Rational) part;
						}
					} else if (part instanceof Variable) {
						lastVariable = (Variable) part;
						if (par != null && dotop != null) {
							dotop.perform(par, lastVariable);
							parts.remove(part);
							parts.remove(dotop);
							nothingChanged = false;
							break;
						}
					} else if (part instanceof DotOperation) {
						dotop = (DotOperation) part;
					} else if (part instanceof LineOperation) {
						par = null;
						lastPar = null;
						lastRat = null;
						lastValue = null;
						lastVariable = null;
						op = null;
						dotop = null;
					}
				}
			} while (!nothingChanged);

			do {
				nothingChanged = true;
				Parenthesis par = null;
				Rational lastRat = null;
				Value lastValue = null;
				Variable lastVariable = null;
				Hashtable<Variable, Double> lastTermVars = null;
				Operation op = null;
				LineOperation lineop = null;
				Rational rat;
				int index;
				for (EquationPart part : parts) {
					if (part instanceof Rational) {
						if (lastRat == null)
							lastRat = (Rational) part;
						else if (lineop != null) {
							rat = lineop.perform(lastRat, (Rational) part);
							index = parts.indexOf(lastRat);
							parts.remove(lastRat);
							parts.remove(part);
							parts.remove(lineop);
							if (index == -1)
								index = 0;
							parts.insertElementAt(rat, index);
							nothingChanged = false;
							break;
						}
					} else if (part instanceof Equation || part instanceof Parenthesis) {
						if (par == null) {
							par = (Parenthesis) part;
							// There should only be one term left
							Vector<EquationPart> term = par.getTerm(0);
							lastTermVars = Parenthesis.getVariablesOfTerm(term);
							// There should only be one rational for this term left
							lastRat = Parenthesis.getRationalOfTerm(term);
						} else if (lineop != null) {
							Parenthesis par2 = (Parenthesis) part;
							if (par.getTermCount() == 1 && par2.getTermCount() == 1
									&& Parenthesis.areTermsCompatible(par.parts, par2.parts)) {
								Vector<EquationPart> term = combineTerms(par.parts, par2.parts, lineop);
								index = parts.indexOf(par);
								parts.remove(par);
								parts.remove(lineop);
								parts.remove(par2);
								Parenthesis parResult = new Parenthesis();
								parResult.parts.addAll(term);
								parts.insertElementAt(parResult, index);
								nothingChanged = false;
								break;
							}

							/*
							 * // There should only be one term left Vector<EquationPart> term2=par2.getTerm(0);
							 * Hashtable<Variable, Double> term2Vars=Parenthesis.getVariablesOfTerm(term2); // There
							 * should only be one rational for this term left Rational
							 * term2Rat=Parenthesis.getRationalOfTerm(term2); boolean notfound=false; if
							 * (lastTermVars.size()==term2Vars.size()) { for (Variable var : term2Vars.keySet()) { if
							 * (!lastTermVars.contains(var)) { notfound=true; break; } } if (!notfound) {
							 * rat=lineop.perform(lastRat, term2Rat); index=parts.indexOf(par); parts.remove(par);
							 * parts.remove(par2); Parenthesis parResult=new Parenthesis(); parResult.parts.add(rat);
							 * parResult.parts.addAll(term2Vars); parts.insertElementAt(parResult, index);
							 * nothingChanged=false; break; } }
							 */
						}
					} else if (part instanceof LineOperation) {
						lineop = (LineOperation) part;
					} else if (part instanceof DotOperation) {
						par = null;
						lastRat = null;
						lastValue = null;
						lastVariable = null;
						op = null;
						lineop = null;
					}
				}
			} while (!nothingChanged);
		}

		public double calc() {
			double value = 0.d;
			EquationPart lastPart = null;
			for (EquationPart part : parts) {
				if (part instanceof Rational) {
					if (lastPart instanceof Plus)
						value += ((Rational) part).value;
					else if (lastPart instanceof Minus)
						value -= ((Rational) part).value;
					else if (lastPart instanceof Multiply)
						value *= ((Rational) part).value;
					else if (lastPart instanceof Divide)
						value /= ((Rational) part).value;
					else if (lastPart == null)
						value = ((Rational) part).value;
					lastPart = null;
				} else if (part instanceof Parenthesis || part instanceof Equation) {
					if (lastPart instanceof Plus)
						value += ((Parenthesis) part).calc();
					else if (lastPart instanceof Minus)
						value -= ((Parenthesis) part).calc();
					else if (lastPart instanceof Multiply)
						value *= ((Parenthesis) part).calc();
					else if (lastPart instanceof Divide)
						value /= ((Parenthesis) part).calc();
					else if (lastPart == null)
						value = ((Parenthesis) part).calc();
					lastPart = null;
				} else {
					lastPart = part;
				}
			}
			return value;
		}
		public Object clone() throws CloneNotSupportedException {
			Parenthesis p = null;
			try {
				p = this.getClass().newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (EquationPart part : parts) {
				if (part instanceof Plus)
					p.parts.add(new Plus());
				else if (part instanceof Minus)
					p.parts.add(new Minus());
				else if (part instanceof Multiply)
					p.parts.add(new Multiply());
				else if (part instanceof Divide)
					p.parts.add(new Divide());
				else if (part instanceof Power)
					p.parts.add(new Power());
				else if (part instanceof Rational) {
					Rational rat = new Rational();
					rat.value = ((Rational) part).value;
					p.parts.add(rat);
				} else if (part instanceof Variable) {
					Variable var = new Variable();
					var.name = ((Variable) part).name;
					p.parts.add(var);
				} else if (part instanceof Parenthesis || part instanceof Equation)
					p.parts.add((Parenthesis) ((Parenthesis) part).clone());
			}
			return p;
		}
	}

	public static class Equation extends Parenthesis {
	}

	public static class SplineCoefficients {
		public double[] coefficients;
		public Variable varx;
		public Equation polynom;
	}

	/*
	 * public static class Poly<R extends Ring<R>> { public Polynomial<R> poly; public Rational evaluate() { return
	 * null; } }
	 */

	/*
	 * public static <R extends Ring<R>> Polynomial<R> compose(Polynomial<R> thiss, Polynomial<R> that, Variable<R>
	 * thatvar) { List<Variable<R>> variables = thiss.getVariables(); Polynomial<R> result = null; for (Variable<R> v :
	 * variables) { //for (Map.Entry<Term, R> entry : thiss._termToCoef.entrySet()) { for (Term entry : that.getTerms())
	 * { Term term = entry; if (term.size()>0 && term.getVariable(0)==thatvar) { Constant<R> cst =
	 * Constant.valueOf(thiss.getCoefficient(term)); int power = term.getPower(v); if (power > 0) { Polynomial<R> fn =
	 * that.pow(power); result = (result != null) ? result.plus(cst.times(fn)) : cst .times(fn); } else { // power = 0
	 * result = (result != null) ? result.plus(cst) : cst; } } } } return result; }
	 */

	/*
	 * public static <R extends Ring<R>> Polynomial<R> divide(Polynomial<R> thiss, Polynomial<R> that) {
	 * Polynomial<Rational> poly=Polynomial.valueOf(Rational.ZERO, (Variable<Rational>)null); }
	 */

	public static Point2D.Double funcSpline(double x, SplineCoefficients sc) {
		// Rational raty=sc.polynom.evaluate(Rational.valueOf((long)x*1000, 1000));

		Point2D.Double p = new Point2D.Double();
		p.x = x;
		Rational rat = new Rational();
		rat.value = x;
		Equation polynom = null;
		try {
			polynom = (Equation) sc.polynom.clone();
			polynom.replace(sc.varx, rat);
			p.y = sc.polynom.calc();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}

	public static SplineCoefficients calcSplineCoefficients(Point2D.Double... points) {
		SplineCoefficients sc = new SplineCoefficients();

		Vector<Equation> polys = new Vector<Equation>();
		Vector<Variable> vars = new Vector<Variable>();

		Equation eq = new Equation();
		Variable varx = new Variable();
		varx.name = "x";
		sc.varx = varx;
		Variable vary = new Variable();
		vary.name = "y";
		int i = 0;
		for (Point2D.Double point : points) {
			Parenthesis par = new Parenthesis();

			Variable var = new Variable();
			var.name = String.valueOf(i);
			vars.add(var);
			par.parts.add(var);
			par.parts.add(new Multiply());
			Parenthesis par2 = new Parenthesis();
			par2.parts.add(varx);
			par2.parts.add(new Power());
			Rational rat = new Rational();
			rat.value = i;
			par2.parts.add(rat);
			par.parts.add(par2);
			if (i > 0)
				eq.parts.add(new Plus());
			eq.parts.add(par);
			i++;
		}
		for (Point2D.Double point : points) {
			try {
				polys.add((Equation) eq.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Vector<Equation> varpolys = new Vector<Equation>();
		i = 0;
		Vector<EquationPart> term = new Vector<EquationPart>();
		for (Equation eqq : polys) {
			Equation eqcoefficient = new Equation();
			Parenthesis par = new Parenthesis();
			par.parts.add(vary);
			term.clear();
			int j = 0;
			for (EquationPart part : eqq.parts) {
				if (part instanceof Plus) {
					if (i != j) {
						par.parts.add(new Minus());
						par.parts.addAll(term);
					}
					term.clear();
					j++;
				} else {
					term.add(part);
				}
			}
			if (!term.isEmpty())
				par.parts.add(new Minus());
			par.parts.addAll(term);
			eqcoefficient.parts.add(par);
			eqcoefficient.parts.add(new Divide());
			par = new Parenthesis();
			par.parts.add(varx);
			par.parts.add(new Power());
			Rational rat = new Rational();
			rat.value = i;
			par.parts.add(rat);
			eqcoefficient.parts.add(par);
			varpolys.add(eqcoefficient);
			i++;
		}

		Vector<Equation> polyresults = new Vector<Equation>();
		Equation polyresult = new Equation();
		i = 0;
		for (Equation eqq : varpolys) {
			try {
				polyresult = (Equation) eqq.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// composeloop: while (true) {
			int j = 0;
			for (Equation eqqq : varpolys) {
				if (i != j)
					if (!polyresult.replace(vars.get(j), eqqq))
						;// break composeloop;
				j++;
			}
			// }
			polyresults.add(polyresult);
			i++;
		}

		Vector<Double> results = new Vector<Double>();
		double result;
		i = 0;
		for (Point2D.Double point : points) {
			Rational rat = new Rational();
			rat.value = point.x;
			polyresults.get(i).replace(varx, rat);
			rat = new Rational();
			rat.value = point.y;
			polyresults.get(i).replace(vary, rat);
			polyresults.get(i).simplify();
			result = polyresults.get(i).calc();
			results.add(result);
			i++;
		}

		sc.coefficients = new double[results.size()];
		sc.polynom = new Equation();
		i = 0;
		for (double d : results) {
			sc.coefficients[i] = d;
			Parenthesis par1 = new Parenthesis();
			Rational rat = new Rational();
			rat.value = d;
			par1.parts.add(rat);
			par1.parts.add(new Multiply());
			Parenthesis par = new Parenthesis();
			par.parts.add(varx);
			par.parts.add(new Power());
			rat = new Rational();
			rat.value = i;
			par.parts.add(rat);
			par1.parts.add(par);
			if (i > 0)
				sc.polynom.parts.add(new Plus());
			sc.polynom.parts.add(par1);
			i++;
		}

		/*
		 * Polynomial<Rational> poly=Polynomial.valueOf(Rational.ZERO, (Variable<Rational>)null);
		 * Variable.Local<Rational> varx = new Variable.Local<Rational>("x"); Polynomial<Rational> polyvarx =
		 * Polynomial.valueOf(Rational.ONE, varx);
		 * 
		 * Vector<Variable.Local<Rational>> vars=new Vector<Variable.Local<Rational>>(); Vector<Polynomial<Rational>>
		 * polys=new Vector<Polynomial<Rational>>(); int i=0; for (Point2D.Double point : points) {
		 * Variable.Local<Rational> var = new Variable.Local<Rational>(""+('a'+i)); vars.add(var); Polynomial<Rational>
		 * polyvar = Polynomial.valueOf(Rational.ONE, var); if (i>0) polyvar=(polyvar=polyvar.times(polyvarx)).pow(i);
		 * poly=poly.plus(polyvar); i++; } for (Point2D.Double point : points) { polys.add(poly.copy()); }
		 * Variable.Local<Rational> vary = new Variable.Local<Rational>("y"); Polynomial<Rational> polyvary =
		 * Polynomial.valueOf(Rational.ONE, vary); Polynomial<Rational> p=Polynomial.valueOf(Rational.ZERO,
		 * (Variable<Rational>)null); Vector<Polynomial<Rational>> varpolysDividend=new Vector<Polynomial<Rational>>();
		 * Vector<Polynomial<Rational>> varpolysDivisor=new Vector<Polynomial<Rational>>(); Variable.Local<Rational>
		 * var1 = new Variable.Local<Rational>("1"); Polynomial<Rational> varpoly1=Polynomial.valueOf(Rational.ONE,
		 * var1); i=0; for (Polynomial<Rational> p1 : polys) { Polynomial<Rational> p4=Polynomial.valueOf(Rational.ZERO,
		 * (Variable<Rational>)null); Polynomial<Rational> p3=Polynomial.valueOf(Rational.ZERO,
		 * (Variable<Rational>)null); int j=0; for (Term term : p1.getTerms()) { if (i!=j-1 &&
		 * p1.getCoefficient(term)!=Rational.ZERO) { Polynomial<Rational> p5=Polynomial.valueOf(p1.getCoefficient(term),
		 * (Variable<Rational>)term.getVariable(0)); if (term.getVariable(1)!=null)
		 * p5=p5.times(Polynomial.valueOf(Rational.ONE, (Variable<Rational>)term.getVariable(1)));
		 * p5=p5.pow(term.getPower(0)); p3=p3.plus(p5); } j++; } p3=(p3=p3.times(Rational.valueOf(-1,
		 * 1))).plus(polyvary); p4=p4.plus(p3); if (i>0) { varpolysDivisor.add(polyvarx.pow(i)); } else {
		 * varpolysDivisor.add(varpoly1); } varpolysDividend.add(p4); i++; }
		 * 
		 * Vector<Polynomial<Rational>> polyresultsDividend=new Vector<Polynomial<Rational>>(); Polynomial<Rational>
		 * polyresult; i=0; for (Polynomial<Rational> p1 : varpolysDividend) { polyresult=p1.copy(); composeloop: while
		 * (true) { int j=0; for (Polynomial<Rational> p2 : varpolysDividend) { //if (p2!=p1) {
		 * p=MathUtils.compose(polyresult, p2, vars.get(j)); if (p!=null) polyresult=p; else break composeloop; //} j++;
		 * } } polyresultsDividend.add(polyresult); i++; } Vector<Polynomial<Rational>> polyresultsDivisor=new
		 * Vector<Polynomial<Rational>>(); i=0; for (Polynomial<Rational> p1 : varpolysDivisor) { polyresult=p1.copy();
		 * composeloop: while (true) { int j=0; for (Polynomial<Rational> p2 : varpolysDivisor) { //if (p2!=p1) {
		 * p=MathUtils.compose(polyresult, p2, vars.get(j)); if (p!=null) polyresult=p; else break composeloop; //} j++;
		 * } } polyresultsDivisor.add(polyresult); i++; }
		 * 
		 * Vector<Double> results=new Vector<Double>(); Rational resultDividend, resultDivisor; i=0; for (Point2D.Double
		 * point : points) { resultDividend=polyresultsDividend.get(i).evaluate( new Rational[] {
		 * Rational.valueOf((long)(point.x*1000), 1000), Rational.valueOf((long)(point.y*1000), 1000)}); if
		 * (polyresultsDivisor.get(i)!=varpoly1) { resultDivisor=polyresultsDivisor.get(i).evaluate( new Rational[] {
		 * Rational.valueOf((long)(point.x*1000), 1000), Rational.valueOf((long)(point.y*1000), 1000)});
		 * results.add(resultDividend.doubleValue()/resultDivisor.doubleValue()); } else {
		 * results.add(resultDividend.doubleValue()); } i++; }
		 * 
		 * sc.coefficients=new double[results.size()]; sc.polynom=Polynomial.valueOf(Rational.ZERO,
		 * (Variable<Rational>)null); i=0; for (double d : results) { sc.coefficients[i]=d; //Variable.Local<Rational>
		 * var = new Variable.Local<Rational>(""+('a'+i)); Polynomial<Rational> polyvar =
		 * Polynomial.valueOf(Rational.ZERO, (Variable<Rational>)null);
		 * sc.polynom.plus(polyvar.plus(Rational.valueOf((long)(d*1000), 1000)).times(polyvarx).pow(i)); i++; }
		 */

		return sc;
	}
}
