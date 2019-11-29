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
package plugin.xdoc;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.*;
import javax.swing.*;

import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;
import com.google.zxing.qrcode.decoder.*;

import core.*;
import core.datasource.*;
import core.tasks.*;
import fr.opensagres.xdocreport.document.*;
import fr.opensagres.xdocreport.document.images.*;
import fr.opensagres.xdocreport.document.registry.*;
import fr.opensagres.xdocreport.template.*;
import fr.opensagres.xdocreport.template.formatter.*;

public class XDocReportTask implements TRunnable {

	private IContext iContext;
	private SimpleDateFormat dateFormat;
	private DecimalFormat decimalFormat;
	private String dseparator;
	private Hashtable<String, Object> context;
	private Hashtable parameteres;

	public XDocReportTask() {
		this.dateFormat = new SimpleDateFormat((String) parameteres.get("export.dateformat"));
		this.dseparator = (String) parameteres.get("export.decimal.separator");
		this.decimalFormat = TStringUtils.getDecimalFormat();
	}

	@Override
	public void run() {
		Vector<String> rcdl = (Vector<String>) parameteres.get("xdoc.baseRecords");
		DBAccess dba = ConnectionManager.getAccessTo(PluginManagerIld.getPluginProperty("XDoc",
				"xdoc.config.recipient.file"));
		for (String rcd : rcdl) {
			Record br = dba.exist(rcd);
			if (br != null) {
				generateXDcox(br);
			}
		}
	}

	private void generateXDcox(Record baseRcd) {

		DateFormat timeF = DateFormat.getTimeInstance();
		java.util.Date now = new java.util.Date();
		try {

			String docid = (String) parameteres.get("xdoc.pattern.document");
			Record docrcd = ConnectionManager.getAccessTo("xd_config").exist("xd_coid = '" + docid + "'");

			byte[] docdta = (byte[]) docrcd.getFieldValue("xd_codata");
			ByteArrayInputStream bais = new ByteArrayInputStream(docdta);
			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(bais, TemplateEngineKind.Velocity);

			this.iContext = report.createContext();
			this.context = new Hashtable<String, Object>();

			FieldsMetadata metadata = report.createFieldsMetadata();
			metadata.addFieldAsImage("digitalSign");

			// system
			putInContext("document.date", dateFormat.format(now));
			putInContext("document.time", timeF.format(now));
			putInContext("document.user_id", Session.getUserFieldValue("t_ususer_id"));
			putInContext("document.user_name", Session.getUserFieldValue("t_usname"));

			SimpleDateFormat simpledf = new SimpleDateFormat("yyyy/MM/dd;MMMM;EEEE;hh:mm:ss");
			String datetxt[] = simpledf.format(now).split(";");
			putInContext("document.yyyy", datetxt[0].split("[/]")[0]);
			putInContext("document.MM", datetxt[0].split("[/]")[1]);
			putInContext("document.dd", datetxt[0].split("[/]")[2]);
			putInContext("document.month", datetxt[1]);
			putInContext("document.day", datetxt[2]);
			putInContext("document.hh", datetxt[3].split("[:]")[0]);
			putInContext("document.mm", datetxt[3].split("[:]")[1]);
			putInContext("document.ss", datetxt[3].split("[:]")[2]);

			// fecha inicial/ final mes en curso
			int stryyyy = Integer.valueOf((String) context.get("document.yyyy"));
			int stryMM = Integer.valueOf((String) context.get("document.MM"));

			GregorianCalendar gcal = new GregorianCalendar(TimeZone.getTimeZone("00:00"));
			gcal.set(GregorianCalendar.YEAR, stryyyy);
			gcal.set(GregorianCalendar.MONTH, stryMM - 1);
			gcal.add(GregorianCalendar.YEAR, -1);
			gcal.set(GregorianCalendar.DAY_OF_MONTH, 1);
			stryyyy = gcal.get(GregorianCalendar.YEAR);
			putInContext("document.startYear", formatFieldValue(gcal.getTime()));

			// putInContext("document.startYear", 01 + "/" + ("0" + stryMM) +
			// "/" + stryyyy);

			// stryyyy = 2005;
			// stryMM = 8;

			gcal.set(GregorianCalendar.YEAR, Integer.valueOf((String) context.get("document.yyyy")));
			gcal.set(GregorianCalendar.MONTH, Integer.valueOf((String) context.get("document.MM")) - 1);
			gcal.set(GregorianCalendar.DAY_OF_MONTH, 1);
			gcal.add(GregorianCalendar.DAY_OF_MONTH, -1);
			putInContext("document.endYear", formatFieldValue(gcal.getTime()));
			int endyMM = gcal.get(GregorianCalendar.MONTH) + 1;
			int endyyyy = gcal.get(GregorianCalendar.YEAR);

			// endyyyy = 2006;
			// endyMM = 7;
			putInContext("document.endYearMMyyyy", TStringUtils.getStringDate(gcal.getTime(), "MM/yyyy"));

			// remitent
			putInContext("document.remitent", (String) parameteres.get("xdoc.recipient"));

			// extended document properties
			String prpl = (String) parameteres.get("xdoc.out.docs.properties");
			Vector<TEntry> v = TStringUtils.getPropertys(prpl);
			char scnt = 96;
			for (TEntry te : v) {
				scnt++;
				putInContext("property." + te.getKey().toString().toLowerCase() + "." + String.valueOf(scnt), te
						.getValue().toString());
			}

			// record base
			for (int i = 0; i < baseRcd.getFieldCount(); i++) {
				putInContext("recipient." + baseRcd.getFieldName(i).toLowerCase(),
						formatFieldValue(baseRcd.getFieldValue(i)));
			}

			// TODO: put a call to external method to complete aditional information for custormer implementation

			// certificate
			String cert = TStringUtils.getUniqueID();
			putInContext("document.certificate", cert);

			// digital seal COLOCAR DE ULTIMO YA QUE EL EL REGISTRO DEL
			// DOCUMENTO, SE COLOCAN LAS VARIABLES Y
			// ESTE METODO NECESITA CONOCERLAS TODAS
			FileImageProvider fip = generateQRCode(docrcd);
			iContext.put("digitalSign", fip);

			// output
			/*
			 * File tmpf = File.createTempFile("kea", ".docx"); tmpf.deleteOnExit(); OutputStream out = new
			 * FileOutputStream(tmpf); report.process(iContext, out);
			 */
			// output
			File tmpf = File.createTempFile("tmp", ".docx");
			tmpf.deleteOnExit();
			OutputStream out = new FileOutputStream(tmpf);
			report.process(iContext, out);

			// Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
			// report.convert(iContext, options, out);

			Desktop dsk = Desktop.getDesktop();
			if (parameteres.get("xdoc.out.print").equals(Boolean.TRUE)) {
				dsk.print(tmpf);
			}
			if (parameteres.get("xdoc.out.window").equals(Boolean.TRUE)) {
				dsk.open(tmpf);
			}
			if (parameteres.get("xdoc.out.file").equals(Boolean.TRUE)) {
				String tfd = (String) parameteres.get("xdoc.out.targetdirectory");
				String tfname = (String) parameteres.get("xdoc.out.targetfile");

				Vector<String> conk = new Vector(context.keySet());
				for (String sk : conk) {
					tfname = tfname.replace("$" + sk, context.get(sk).toString());
				}
				// replace variables in filename
				File f = new File(tfd + "/" + tfname);
				f.delete();
				tmpf.renameTo(f);
			}

			// to mergedocs
			Record mr = ConnectionManager.getAccessTo("xd_merged_doc").getModel();
			mr.setFieldValue("xd_mdid", "" + System.currentTimeMillis());
			mr.setFieldValue("xd_mdcertificate", cert);
			mr.setFieldValue("xd_mddate", new Date());
			mr.setFieldValue("xd_mdremittent", XDoc.getRecipientFieldsValue(baseRcd));
			mr.setFieldValue("xd_mddoc_name", docrcd.getFieldValue("xd_coname"));

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tmpf));
			byte[] docData = new byte[(int) tmpf.length()];
			bis.read(docData);
			bis.close();
			mr.setFieldValue("xd_mdmerged_doc", docData);
			ConnectionManager.getAccessTo("xd_merged_doc").add(mr);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * da formato al argumento de entrada segun el tipo de instanica y retorna una cadena de caracteres con el resultado
	 * 
	 * @param val - valor a formatear
	 * @return string con valor formateado
	 */
	private String formatFieldValue(Object val) {
		String rtns = "";
		// double
		if (val instanceof Double || val instanceof Float) {
			rtns = decimalFormat.format(val);
			rtns = rtns.replaceAll("[.]", dseparator);
			rtns = rtns.replace("x", "");
		}
		// date
		if (val instanceof Date) {
			rtns = dateFormat.format((Date) val);
		}
		// todo lo demas
		if (rtns.equals("")) {
			rtns = val.toString();
		}
		return rtns;
	}

	/**
	 * set the documento context and store varables for internal use.
	 * 
	 * @param id - id
	 * @param val - values
	 */
	private void putInContext(String id, Object val) {
		iContext.put(id, val);
		context.put(id, val);
		System.out.println("$" + id + "=" + val.toString());
	}

	private FileImageProvider generateQRCode(Record docrcd) {

		String myCodeText = ((String) docrcd.getFieldValue("xd_coqrcode")).replace(";", "\n");
		// REPLACE VARS
		Enumeration<String> enume = context.keys();
		while (enume.hasMoreElements()) {
			String k = enume.nextElement();
			// String varn = "<$" + k + ">";
			String varn = "$" + k;
			if (myCodeText.contains(k)) {
				myCodeText = myCodeText.replace(varn, context.get(k).toString());
			}

		}
		System.out.println(myCodeText);

		ImageIcon ii = TResources.getIcon("/plugin/xdoc/seal_bg");
		String fileType = "png";
		File myFile = null;
		try {
			myFile = File.createTempFile("digitalSign", ".png");
			myFile.deleteOnExit();
			// myFile = new File("c:/keaSeal.png");
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, ii.getIconWidth(),
					ii.getIconHeight(), hintMap);
			int CrunchifyWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(ii.getIconWidth(), ii.getIconHeight(), BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			// graphics.drawImage(ii.getImage(), 0, 0, null);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, ii.getIconWidth(), ii.getIconHeight());
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < ii.getIconWidth(); i++) {
				for (int j = 0; j < ii.getIconHeight(); j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ImageIO.write(image, fileType, myFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new FileImageProvider(myFile);
	}
	/*
	 * private Image generateQRCode() { String myCodeText = "http://Crunchify.com/"; String filePath =
	 * "/Users/arpitshah/Documents/eclipsewp/CrunchifyQR.png"; int size = 125; String fileType = "png"; File myFile =
	 * new File(filePath); try { Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType,
	 * ErrorCorrectionLevel>(); hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); QRCodeWriter
	 * qrCodeWriter = new QRCodeWriter(); BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE,
	 * size, size, hintMap); int CrunchifyWidth = byteMatrix.getWidth(); BufferedImage image = new
	 * BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB); image.createGraphics();
	 * 
	 * Graphics2D graphics = (Graphics2D) image.getGraphics(); graphics.setColor(Color.WHITE); graphics.fillRect(0, 0,
	 * CrunchifyWidth, CrunchifyWidth); graphics.setColor(Color.BLACK);
	 * 
	 * for (int i = 0; i < CrunchifyWidth; i++) { for (int j = 0; j < CrunchifyWidth; j++) { if (byteMatrix.get(i, j)) {
	 * graphics.fillRect(i, j, 1, 1); } } } ImageIO.write(image, fileType, myFile); } catch (WriterException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } return null; }
	 */

	@Override
	public Hashtable getTaskParameters() {
		return parameteres;
	}

	@Override
	public void setTaskParameters(Hashtable parms) {
		this.parameteres = (Hashtable) parms;
	}

	@Override
	public void setFuture(Future f, boolean ab) {
		
	}
}
