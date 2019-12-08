/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package core;

import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.datasource.*;

/**
 * Utils class for String and {@link TEntry} Manipulations
 * 
 */
public class TStringUtils {

	public final static java.sql.Date ZERODATE = java.sql.Date.valueOf("1899-12-31");

	private static SimpleDateFormat dateFormat = new SimpleDateFormat();
	private static Random random = new Random();
	private static NumberFormat formatter;

	/**
	 * init enviorement
	 * 
	 * @throws Exception
	 */
	public static void init() {
		Vector<File> files = TResources.findFiles(new File(System.getProperty("user.dir") + "/"), ".properties");
		for (File f : files) {
			try {
				// TODO: complete implementation.
				PropertyResourceBundle prb = new PropertyResourceBundle(new FileInputStream(f));
				Alesia.logger.info("property file " + f.getName() + " found.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		formatter = getDecimalFormat();
	}

	/**
	 * Return a formatted string representing the speed or time of the input argument. The incomming argument represent
	 * an elapsed amound of time.
	 * <p>
	 * This method is not a presition method. is intended only for rougly format an amount of time. e.g. millis=1456
	 * will be 1456 Ms but millis=2456 will be formatted as 2 Sec
	 * 
	 * @param millis
	 * @return
	 */
	public static String formatSpeed(long millis) {
		String rs = millis + " Ms";
		millis = millis / 1000;
		rs = millis > 1 ? millis + " Sec" : rs;
		millis = millis / 60;
		rs = millis > 1 ? millis + " Min" : rs;
		millis = millis / 60;
		rs = millis > 1 ? millis + " Hour" : rs;
		millis = millis / 24;
		rs = millis > 1 ? millis + " Day" : rs;
		millis = millis / 365;
		rs = millis > 1 ? millis + " Year" : rs;
		return rs;
	}
	/**
	 * remueve de la cadena de caracateres pasada como argumento todas los tag html
	 * 
	 * @param sht - String con tag html
	 * @return String sin tags
	 */
	public static String removeHTMLtags(String sht) {
		sht = sht.replaceAll("</*\\w*>", "");
		sht = sht.replaceAll("&lt", "<");
		sht = sht.replaceAll("&gt", ">");
		return sht;
	}

	/**
	 * inserta el atributo<br>
	 * dentro de una secuencia de caracteres cada vez que se consiga un espacio en blanco despues de una longitud
	 * determinada Ej.: getInsertedBR("Arnaldo Fuentes", 3) retornara "Arnaldo<br>
	 * Fuentes"
	 * 
	 * NOTA: este metodo adiciona <html></html> si estas no se encuentran al inicio/final de la cadena original
	 * 
	 * @param str - secuancia
	 * @param len - longitud
	 * @return secoencia con atributo insertado
	 */
	public static String getInsertedBR(String str, int len) {
		StringBuffer stt1 = new StringBuffer(str);
		int c = 0;
		boolean f = false;
		for (int x = 0; x < stt1.length(); x++) {
			f = (c > len && stt1.charAt(x) == ' ');
			if (f) {
				stt1.insert(x, "<br>");
				c = 0;
			} else {
				c++;
			}
		}
		String bris = stt1.toString();
		bris = bris.startsWith("<html>") ? bris : "<html>" + bris;
		bris = bris.endsWith("</html>") ? bris : bris + "</html>";
		return bris;
	}

	/**
	 * busca y retorna el grupo de constantes que comienzen con el parametro <code>ty</code>. este metodo intenta
	 * primero localizar la lista de constantes en los archivos <code>.properties</code> cargados durante la
	 * inicializacion. si no se encuentran alli, intenta en el archive de referencias en la base de datos principal
	 * 
	 * @param ty - tipo de constantes
	 * @return arreglo de constantes
	 */
	public static TEntry[] getTEntryGroup(String ty) {
		ArrayList<String> keys = new ArrayList(Alesia.getResourceMap().keySet());
		Vector lst = new Vector();
		for (String key : keys) {
			if (key.startsWith(ty)) {
				String[] kv = Alesia.getResourceMap().getString(key).split(";");
				if (kv.length > 1) {
					lst.add(new TEntry(kv[0], kv[1]));
				}
			}
		}
		TEntry[] lte = (TEntry[]) lst.toArray(new TEntry[lst.size()]);
		return lte;
	}

	/**
	 * retorna instancia de <code>DecimalFormat</code> con valores basicos establecidos
	 * 
	 * @return instancia de DecimalFormat
	 */
	public static DecimalFormat getDecimalFormat() {
		DecimalFormat decF = new DecimalFormat("#0.0#;-#0.0#");
		decF.setMinimumFractionDigits(2);
		decF.setMaximumFractionDigits(2);
		return decF;
	}

	/**
	 * return a hex representation of digested string that has been cypher by MD5 algorithm.
	 * 
	 * @param srcs - source string to digest
	 * @return digested string in hex
	 */
	public static String getDigestString(String srcs) {
		String dmsg = "";
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// terry in numeric keys 53446
			messageDigest.update((srcs + "Terry").getBytes());
			byte[] bytes = messageDigest.digest();
			BigInteger bi = new BigInteger(1, bytes);
			dmsg = String.format("%0" + (bytes.length << 1) + "X", bi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dmsg;
	}

	/**
	 * crea una instanica de <code>Date</code> que representa el monemto actual y lo retorna como una cadena de
	 * caracteres segun el formato pasado como argumento
	 * 
	 * @param f formato <code>(ver SimpleDateFormat)</code>
	 * @return
	 */
	public static String getStringDate(String f) {
		return getStringDate(new Date(), f);
	}

	/**
	 * return a {@link Vector} of {@link TEntry} converted form string of properies
	 * 
	 * @param prpl - String of properties in standar format: key;value;key;value...
	 * 
	 * @return vector of tentry
	 */
	public static Vector<TEntry> getPropertys(String prpl) {
		Vector<TEntry> prpv = new Vector();
		if (!prpl.equals("")) {
			String[] kv = prpl.split(";");
			for (int i = 0; i < kv.length; i = i + 2) {
				// 171218: try to correct error in incoming string when key;value pair are not even
				String v = (i + 1) > kv.length ? "" : kv[i + 1];
				prpv.add(new TEntry(kv[i], v));
			}
		}
		return prpv;
	}

	/**
	 * retorna la instancia de <code>Date</code> pasada como argumento en una String con el formato
	 * <code>f (ver SimpleDateFormat)</code>
	 * 
	 * @param d - instancia de <code>Date</code>
	 * @param f - formato
	 * @return String
	 */
	public static String getStringDate(Date d, String f) {
		dateFormat.applyPattern(f);
		return dateFormat.format(d);
	}

	/**
	 * retorna <code>String</code> designada dentro de <code>ResourceBundle</code> cliente para el identificador pasado
	 * como argumento. si no se encuetra una cadena de caracteres asociadas al identificador se debuelve el
	 * identificador
	 * 
	 * @param id - id
	 * @return String
	 */
	public static String getString(String id) {
		ResourceMap rm = Alesia.getResourceMap();
		// intenta cadena original, si no existe, retorna el mismo identificador
		String text = rm.getString(id);
		return (text == null) ? id : text;
	}

	/**
	 * compara las 2 instancias de <code>Date</code> pasadas como argumento y retorna un valor < 0 si d1 < d2, 0 si d1 =
	 * d2 o un valor > 0 si d1 > d2. La comparacion es establecida con la presicion del argumento <code>fmt</code>
	 * 
	 * @param d1 - instancia de <code>Date</code> menor
	 * @param d2 - instancia de <code>Date</code> mayor
	 * @param fmt - precision o formato (ver SimpleDateFormat)
	 * @return valor segun comparacion
	 */
	public static int compare(Date d1, Date d2, String fmt) {
		dateFormat.applyPattern(fmt);
		String d_1 = dateFormat.format(d1);
		String d_2 = dateFormat.format(d2);
		return d_1.compareTo(d_2);
	}

	/**
	 * retorna un indentificador unico en formato estandar xx-xxx-xxx
	 * 
	 * @return <code>String</code> con identificador unico
	 */
	public static String getUniqueID() {
		String cern = "000";
		String f1 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f2 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f3 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String cern1 = f1.substring(f1.length() - 2) + "-" + f2.substring(f2.length() - 3) + "-"
				+ f3.substring(f3.length() - 3);
		// System.out.println(cern1);
		return cern1;
	}

	/**
	 * generate a OTP for the given user. the password lenght MUST BE 6 char long (this is necesary because this system
	 * use the length of store passgord to determine if that field value is a OTP.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #verifyOneTimePassword(String, String)
	 * @param usid - user to generate the otp
	 * 
	 * @return password to store
	 */
	public static String getOneTimePassword(String uid) {
		String nextp = null;
		try {
			nextp = getPasscodeGenerator(uid).generateTimeoutCode();
		} catch (Exception e) {

		}
		return nextp;
	}
	/**
	 * create a standar {@link PasscodeGenerator}
	 * 
	 * @param uid - uid to generate/verfy OTP
	 * 
	 * @return PasscodeGenerator
	 */
	private static PasscodeGenerator getPasscodeGenerator(String uid) {
		try {
			int to = SystemVariables.getintVar("OTPTimeout");
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(new SecretKeySpec(uid.toLowerCase().getBytes(), ""));
			PasscodeGenerator pcg = new PasscodeGenerator(mac, 6, to * 60);
			return pcg;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * verify if the given OTP is still valid for the user pass as argument.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #getOneTimePassword(String)
	 * 
	 * @param uid - user to verify
	 * @param p - OTP to check
	 * 
	 * @return <code>true</code> if otp still valid
	 */
	public static boolean verifyOneTimePassword(String uid, String p) {
		boolean ok = false;
		try {
			ok = getPasscodeGenerator(uid).verifyTimeoutCode(p, 0, 0);
		} catch (Exception e) {

		}
		return ok;
	}

	/**
	 * Parse the properties stored in String <code>prplis</code> and append in the <code>prps</code> properti objet
	 * 
	 * @param pl - String of properties stores in standar format (key;value;key;value...)
	 * @param prps - {@code Properties} to append the parsed string
	 */
	public static void parseProperties(String pl, Properties prps) {
		if (!pl.equals("")) {
			String[] kv = pl.split(";");
			for (int j = 0; j < kv.length; j = j + 2) {
				prps.put(kv[j], kv[j + 1]);
			}
		}
	}

	/**
	 * retorna texto identificador de aplicacion y version. generalmente usado par grabar en archivos externos
	 * 
	 * @return String en formato <b>Clio Version: 1.36 Update: 0</b> o similar
	 */
	public static String getAboutAppShort() {
		return TStringUtils.getString("name") + " " + TStringUtils.getString("version") + " "
				+ TStringUtils.getString("vendor");

		// return TStringUtils.getString("about.app.name") + " " + TStringUtils.getString("about.version") + " "
		// + SystemVariables.getStringVar("versionID") + " " + TStringUtils.getString("about.update") + " "
		// + SystemVariables.getStringVar("updateID");
	}

	public static String getRecordId() {
		long mill = System.currentTimeMillis();
		// long nanos = System.nanoTime();
		// until better solution, ensure unleast 1 mill from previous method call
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		return Long.toHexString(mill);
	}

	/**
	 * retorna una cadna de caracteres que convierte el tama;o en bytes pasado como argumento a Kb o Mb
	 * 
	 * @param s - tama;o en bytes
	 * @return tama;o en Kb o Mb
	 */
	public static String formatSize(double s) {
		double siz = 0;
		String suf = " Kb";
		if (s < 1024000) {
			siz = s / 1024;
			suf = " Kb";
		} else {
			siz = s / 1024000;
			suf = " Mb";
		}
		return formatter.format(siz) + suf;
	}

	/**
	 * este metodo verifica que el texto txt este escrito segun el patron pasado como argumento
	 * 
	 * @param patt - plantilla que indentifica el formato en que el texto debe estar descrito
	 * @param txt - valor a compara contra la plantiall
	 * 
	 * @return id de mensaje si el valor txt no esta escrito segun el patron
	 */
	public static TError validatePatt(String patt, String txt) {
		if (txt.length() > 0) {
			TError ape = new TError("ui.msg21");
			ape.setMessage(MessageFormat.format(ape.getMessage(), patt));

			// datos > que patron
			if (txt.length() > patt.length()) {
				return ape;
			}
			// verifica patron: mientras el texto escrito este correcto, no muestra mensaje
			for (int i = 0; i < txt.length(); i++) {
				if (patt.charAt(i) == '.' && !(txt.charAt(i) == '.')) {
					return ape;
				}
				if (patt.charAt(i) == '#' && !(Character.isDigit(txt.charAt(i)))) {
					return ape;
				}
			}
			// verifica longitud solo cuando el patron esta correcto
			if (txt.length() == patt.length()) {
				// showMessage("ui.msg21");
				// return;
			}
		}
		return null;
	}

	/**
	 * Given a text and a wildcard pattern, implement wildcard pattern matching algorithm that finds if wildcard pattern
	 * is matched with text. The matching should cover the entire text (not partial text).
	 * <p>
	 * The wildcard pattern can include the characters ‘?’ and ‘*’
	 * <li>‘?’ – matches any single character
	 * <li>‘*’ – Matches any sequence of characters (including the empty sequence)
	 * <p>
	 * examples
	 * 
	 * <br>
	 * String pattern = "ba*****ab"; <br>
	 * String pattern = "ba*ab"; <br>
	 * String pattern = "a*ab"; <br>
	 * String pattern = "a*****ab"; <br>
	 * String pattern = "*a*****ab"; <br>
	 * String pattern = "ba*ab****"; <br>
	 * String pattern = "****"; <br>
	 * String pattern = "*"; <br>
	 * String pattern = "aa?ab"; <br>
	 * String pattern = "b*b"; <br>
	 * String pattern = "a*a"; <br>
	 * String pattern = "baaabab"; <br>
	 * String pattern = "?baaabab"; <br>
	 * String pattern = "*baaaba*";
	 * 
	 * @param string - string to compare
	 * @param pattern - pattern with wildcard caracters
	 * @since 2.3
	 * @return <code>true</code> if the string pass the pattern
	 */
	public static boolean wildCardMacher(String string, String pattern) {

		// static boolean wlidCardFilter(String str, String pattern, int n, int m) {
		// terry: get m and n from the length of the strings
		String str = string == null ? "" : string;
		String patt = pattern == null ? "" : pattern;

		int n = str.length();
		int m = patt.length();

		// empty pattern can only match with
		// empty string
		if (m == 0)
			return (n == 0);

		// lookup table for storing results of
		// subproblems
		boolean[][] lookup = new boolean[n + 1][m + 1];

		// initailze lookup table to false
		for (int i = 0; i < n + 1; i++)
			Arrays.fill(lookup[i], false);

		// empty pattern can match with empty string
		lookup[0][0] = true;

		// Only '*' can match with empty string
		for (int j = 1; j <= m; j++)
			if (patt.charAt(j - 1) == '*')
				lookup[0][j] = lookup[0][j - 1];

		// fill the table in bottom-up fashion
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				// Two cases if we see a '*'
				// a) We ignore '*'' character and move
				// to next character in the pattern,
				// i.e., '*' indicates an empty sequence.
				// b) '*' character matches with ith
				// character in input
				if (patt.charAt(j - 1) == '*')
					lookup[i][j] = lookup[i][j - 1] || lookup[i - 1][j];

				// Current characters are considered as
				// matching in two cases
				// (a) current character of pattern is '?'
				// (b) characters actually match
				else if (patt.charAt(j - 1) == '?' || str.charAt(i - 1) == patt.charAt(j - 1))
					lookup[i][j] = lookup[i - 1][j - 1];

				// If characters don't match
				else
					lookup[i][j] = false;
			}
		}

		return lookup[n][m];
	}
	/**
	 * replace the given variables in patt with the corresponding field value content in <code>rcd</code>.
	 * 
	 * @param patt - string with standar variables in it
	 * @param rcd - Record with values for replacement.
	 * 
	 * @return Formatted string
	 */
	public static String format(String patt, Record rcd) {
		return format(patt, "", rcd);
	}

	/**
	 * replace the given variables inside <code>patt</code> with the corresponding field value content in
	 * <code>rcd</code>.
	 * 
	 * @param patt - string with standar variables in it
	 * @param prefix - prefix to complete the variable inside the <code>patt</code> argument
	 * @param rcd - Record with values for replacement.
	 * 
	 * @return
	 */
	public static String format(String patt, String prefix, Record rcd) {
		String upatt = patt.toUpperCase();
		Vector<String> vars = new Vector<String>();
		String res = patt;
		// extract all variables
		for (int c = 0; c < rcd.getFieldCount(); c++) {
			String ufld = ("${" + prefix + rcd.getFieldName(c) + "}").toUpperCase();
			int ix = upatt.indexOf(ufld);
			if (ix > -1) {
				int le = ufld.length();
				vars.add(patt.substring(ix, ix + le));
			}
		}
		// for detected vars, replace values
		for (String var : vars) {
			String fld = var.substring(2, var.length() - 1);
			fld = fld.replace(prefix, "");
			res = res.replace(var, rcd.getFieldValue(fld).toString());
		}
		return res;
	}

	/**
	 * Utility method to return all fields id, name pairs in a Hashtable acording to the service request. Sometimes is
	 * hard to deside whether to look for field despription. this method build the list acording to the servicerequest
	 * type.
	 * 
	 * @param sr - servicereques
	 * 
	 * @return Hashtable with the id, description pair
	 */
	public static Hashtable<String, String> getFieldsDescriptions(ServiceRequest sr) {
		Hashtable<String, String> fldstxt = new Hashtable<String, String>();
		if (sr.getName().equals(ServiceRequest.CLIENT_GENERATED_LIST)) {
			fldstxt = (Hashtable<String, String>) sr.getParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION);
		} else {
			Record m = ConnectionManager.getAccessTo(sr.getTableName()).getModel();
			for (int c = 0; c < m.getFieldCount(); c++) {
				fldstxt.put(m.getFieldName(c), (String) getString(m.getFieldName(c)));
			}
		}
		return fldstxt;
	}

	/**
	 * Retrive a group of {@link TEntry} from the especific database table according to the selection parameters. this
	 * method accept an aditional TEntry usefull for list that allow espetial entrys like <code>*none</code> or
	 * <code>*all</code>
	 * 
	 * @param tn - table name
	 * @param kf - key field
	 * @param vf - value field
	 * @param wc - where clause
	 * @param ite - aditional {@link TEntry} denoting spetial value like like <code>*none</code> or <code>*all</code>
	 * 
	 * @return TEntry array
	 */
	public static TEntry[] getTEntryGroupFrom(String tn, String kf, String vf, String wc, TEntry ite) {
		Vector lst = new Vector();
		// initial tentry
		if (ite != null) {
			lst.add(ite);
		}
		Vector kls = ConnectionManager.getAccessTo(tn).search(wc, null);
		for (int i = 0; i < kls.size(); i++) {
			Record rcd = (Record) kls.elementAt(i);
			lst.add(new TEntry(rcd.getFieldValue(kf), rcd.getFieldValue(vf)));
		}
		TEntry[] lte = (TEntry[]) lst.toArray(new TEntry[lst.size()]);
		return lte;
	}

	/**
	 * Return a standar decimal format pattern when is necesary display all decimal places. this implementation show
	 * until 10 decimal places. this method return the standar 2 places decimal digits if dp < 3.
	 * 
	 * @param dp - decimal pattern that show until 10 decimal places
	 * 
	 * @return format
	 */
	public static String getFormattForDecimalPlaces(int dp) {
		String fmt = "#,###,###,##0.00;-#,###,###,##0.00";
		if (dp > 2) {
			// String doublefmt = "#,###,###,##0.?;-#,###,###,##0.?";
			String doublefmt = "#,###,###,##0.?;-#,###,###,##0.?";
			String zeros = "0000000000";
			String t = zeros.substring(0, Math.min(10, dp));
			fmt = doublefmt.replace("?", t);
		}
		return fmt;
	}

	/**
	 * retun a list of {@link TEntry} constructed with the fields values passes as argument.
	 * 
	 * @param list - list of row from database
	 * @param key - field name for key
	 * @param value - field name for value
	 * @return list of {@link TEntry}
	 * @since 2.3
	 */
	public static ArrayList<TEntry> getTEntryGroupFrom(LazyList<Model> list, String key, String value) {
		ArrayList<TEntry> entrys = new ArrayList<>();
		for (Model ele : list) {
			entrys.add(new TEntry(ele.get(key), ele.get(value)));
		}
		return entrys;
	}
}
