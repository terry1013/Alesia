package plugin.dbtweezer;

import java.math.*;

import core.*;

public class SleUtilities {

	public static void main(String[] args) {

		double iAmount = 3.14159265358979323846;
		int iScale = 3;
		int iRoundDec = 1;
		int iTopScale = 9;
		System.out.println("roundDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundDecimal(iAmount, iScale, iRoundDec, iTopScale));
		System.out.println("roundBigDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundBigDecimal("3.14159265358979323846", iScale, iRoundDec, iTopScale));

		iAmount = 5555779992355.7684;
		System.out.println("roundDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundDecimal(iAmount, iScale, iRoundDec, iTopScale));
		System.out.println("roundBigDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundBigDecimal("5555779992355.7684", iScale, iRoundDec, iTopScale));

		iScale = -2;
		System.out.println("roundDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundDecimal(iAmount, iScale, iRoundDec, iTopScale));
		System.out.println("roundBigDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundBigDecimal("5555779992355.7684", iScale, iRoundDec, iTopScale));

		iAmount = 992355.7684;
		System.out.println("roundDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundDecimal(iAmount, iScale, iRoundDec, iTopScale));
		System.out.println("roundBigDecimal(" + iAmount + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ roundBigDecimal("992355.7684", iScale, iRoundDec, iTopScale));
		
		int divisor = 1000;
		System.out.println("divideAndRoundDecimal(" + iAmount + ", " + divisor + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ divideAndRoundDecimal(iAmount, divisor, iScale, iRoundDec, iTopScale));
		
		iAmount = 5555779992355.7684;
		System.out.println("divideAndRoundDecimal(" + iAmount + ", " + divisor + ", " + iScale + ", " + iRoundDec + ", " + iTopScale + ")\t"
				+ divideAndRoundDecimal(iAmount, divisor, iScale, iRoundDec, iTopScale));
	}
	
	public static double divide(double amount, int divisor) {
		BigDecimal bd = new BigDecimal(amount, MathContext.DECIMAL64);
		BigDecimal ret = bd.divide(BigDecimal.valueOf(divisor));
		return ret.doubleValue();
	}

	public static double divideAndRoundDecimal(double iAmount, int divisor, int iScale, int iRoundDec, int iTopScale) {
		BigDecimal bd = new BigDecimal(iAmount, MathContext.DECIMAL64);
		BigDecimal ret = bd.divide(BigDecimal.valueOf(divisor));
		String nd = ret.toPlainString();
		return roundBigDecimal(nd, iScale, iRoundDec, iTopScale).doubleValue();
	}

	public static double roundDecimal(double iAmount, int iScale, int iRoundDec, int iTopScale) {
		String sd = BigDecimal.valueOf(iAmount).toPlainString();
		return roundBigDecimal(sd, iScale, iRoundDec, iTopScale).doubleValue();
	}
	
	/**
	 * UPrint messages from inside of script. This message is printed on the internal log file 
	 * 
	 * @param obj - objet to print
	 */
	public static void print(Object obj) {
		SystemLog.log("dbt.msg01", "DBTweezer", "", obj.toString());
	}
	/**
	 * 
	 * @param iAmount: Cantidad o monto a redondear
	 * @param iScale: Cantidad de decimales que se desea en el número resultante. Si el valor indicado es negativo
	 *        (-1,-2,-3), se truncaran los decimales y se redondea la parte entera a multiplos de 10, 100 ó 500
	 *        respectivamente
	 * @param iRoundDec: Indica si se desea que se redondee los decimales (0=No, 1=Sí)
	 * @param iTopScale: Tope de escala a redondear. El valor indicado se considera para comparar el digito en la
	 *        posición scale + 1 para verficar si se aplica el redondeo. Colocar 10 (diez) cuando no se desee redondear
	 * @return
	 */
	private static BigDecimal roundBigDecimal(String iAmount, int iScale, int iRoundDec, int iTopScale) {
		boolean negative = false;
		double wAmount;
		BigDecimal bdAmount;
		double factor;
		long exponential;
		BigDecimal bdExponential;
		BigDecimal minusONE = new BigDecimal(-1L);
		long precision;
		int topScale;
		MathContext mc = new MathContext(38, RoundingMode.UNNECESSARY);

		//

		bdAmount = new BigDecimal(iAmount);
		switch (iRoundDec) {
			case 0 :
				topScale = 10;
				break;
			case 1 :
				topScale = iTopScale;
				break;
			default :
				topScale = 5;
				break;
		}
		if (iScale >= 0) {
			exponential = Math.round(Math.pow(10, iScale));
			bdExponential = new BigDecimal(exponential);
			if (bdAmount.compareTo(BigDecimal.ZERO) <= 0) {
				bdAmount = new BigDecimal(-1).multiply(bdAmount);
				negative = true;
			}
			// precision = bdAmount.multiply(bdExponential).precision(); //Math.round(Math.floor(wAmount*exponential));
			BigDecimal prec = bdAmount.multiply(bdExponential);
			prec = prec.setScale(0, BigDecimal.ROUND_FLOOR);
			BigInteger precBI = bdAmount.multiply(bdExponential).unscaledValue();
			factor = bdAmount.multiply(bdExponential).subtract(prec).doubleValue();// - precision;
			// double checkScale = (topScale/10d);
			if (factor != 0 && factor >= topScale / 10d) { // t=5 by defaul --> t/10 = 0.5
				bdAmount = prec.add(new BigDecimal("1")); // precision + 1);
			} else {
				bdAmount = prec;
			}
			bdAmount = bdAmount.divide(bdExponential);
			if (negative) {
				bdAmount = bdAmount.multiply(minusONE);
			}
		} else {
			wAmount = Double.parseDouble(iAmount);
			if (iRoundDec == 1) {
				wAmount = roundDecimal(wAmount, 0, 1, 5); // Round 2 decimal and trunc
			}
			wAmount = roundPrecision(wAmount, -1 * iScale);
			bdAmount = new BigDecimal(wAmount);
		}
		return bdAmount;
	}
	// */
	private static double roundPrecision(double iAmount, int iDecimal) {

		// -- Orcle make implicit conversion is necesary separte presicion from scale (Ex. 123456.4566)
		// -- Variables
		boolean negative = false;
		double wAmount, wmount;
		long exponential;
		long precision;
		int topScale;
		int rounding = 0;
		double roundNumber;
		long modNumber;

		//
		wmount = Math.round(iAmount);
		if (iDecimal > 0) {
			exponential = Math.round(Math.pow(10, iDecimal));
			roundNumber = exponential / 2;
			if (wmount < 0) {
				wmount = -1 * wmount;
				negative = true;
			}

			precision = Math.round(wmount / exponential);
			modNumber = Math.floorMod((long) wmount, exponential);
			wmount = wmount - modNumber;
			if (modNumber >= roundNumber) {
				wmount = wmount + exponential;
			}
			if (negative) {
				wmount = -1 * wmount;
			}
		}
		return wmount;
	} // roundPrecision;

}