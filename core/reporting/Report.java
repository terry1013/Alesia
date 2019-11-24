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
package core.reporting;

import gui.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.table.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.*;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.engine.util.*;
import net.sf.jasperreports.swing.*;
import core.*;
import core.tasks.*;

/**
 * Base class for report generation. This class work in conjuntion with {@link ReportParameters} to obtain all necesary
 * data to correct generation and or exportation of a {@link JasperReport} and {@link TAbstractPrintAction} to initiate
 * the correct secuence of step ans report submit to executors services.
 * <p>
 * Subclass only needs override the desired protected menthod to custom the desire report. the {@link #call()} method is
 * responsible of the secuence of generation steps.
 * <p>
 * this class has a associated {@link TProgressMonitor}. if the {@link #setFuture(Future)} method is not called, the
 * monitor will be displayed whitout the cancel option.
 * 
 * @author terry
 * 
 */
public class Report implements TCallable<JasperPrint> {

	protected Hashtable reportParameters;
	protected Map jasperParameters = new HashMap();
	protected JasperReport report;
	protected JasperPrint jasperPrint;
	protected TProgressMonitor monitor;
	protected Future<JasperPrint> future;
	protected String exportFileName;
	protected TaskListener taskListener;

	public Report(Hashtable rp) {
		this.reportParameters = new Hashtable();
		// ensure don't "deadlook" from another report generation
		reportParameters.putAll(rp);
		this.monitor = new TProgressMonitor("printer2", "Impresion", null, true);
	}

	/**
	 * Export a JasperPrint instance to a temporaly file in csv format for future use
	 * 
	 * @param prt - filled JasperPrint
	 * @param ef - original file name enter by user
	 * 
	 * @return temporal file with data exported
	 * @throws Exception - for any error found during file export
	 */
	@SuppressWarnings("deprecation")
	public static File temporalExport(JasperPrint prt, String ef) throws Exception {
		File tf = null;
		File f = new File(ef);
		String nn = f.getName();
		String[] nn1 = nn.split("[.]");
		tf = File.createTempFile(nn1[0], nn1[1]);
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, prt);
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, tf);
		exporter.exportReport();
		return tf;
	}

	/**
	 * view the report in standar jasperView component display in a jdialog
	 * 
	 * @param prt - JasperPrint repor to view
	 */
	public static void viewJasperPrint(JasperPrint prt) {
		Dimension siz = Alesia.mainFrame.getBoundByFactor(.5, .7).getSize();
		if (prt.getPageFormat().getOrientation() == OrientationEnum.LANDSCAPE) {
			siz = Alesia.mainFrame.getBoundByFactor(.7, .5).getSize();
		}
		JRViewer cnt = new JRViewer(prt);
		cnt.setPreferredSize(siz);
		JDialog dlg = new JDialog(Alesia.mainFrame, true);
		dlg.setContentPane(cnt);
		dlg.setResizable(true);
		dlg.pack();
		dlg.setLocationRelativeTo(Alesia.mainFrame);
		dlg.setVisible(true);
	}

	@Override
	public JasperPrint call() throws Exception {
		try {
			long t1 = System.currentTimeMillis();
			Thread.sleep(250);
			monitor.setProgress(0, "printing.load");
			loadReport();
			Thread.sleep(250);
			monitor.setProgress(0, "printing.set");
			setReportParameters();
			Thread.sleep(250);
			monitor.setProgress(0, "printing.fill");
			fillReport();
			// why i need put this fuck %&%&%&% here?? future.cancel(true) dont't destroy the task ????
			if (future != null && future.isCancelled()) {
				return null;
				// 180130: vergaaa gracias a los suministros que me proporciona mi madre que este proyecto se esta
				// llevando a cabo !! sin ella, no pudiera ni pensar del HAMBRE
			}
			// only generate the jasperprint?
			Boolean onlygen = (Boolean) reportParameters.get(ReportParameters.REPORT_GENERATE_ONLY);
			if (!onlygen) {
				// correct posible missing or erroneous file extension // null = vew report
				exportFileName = (String) reportParameters.get(ReportParameters.FILE_NAME);
				String ext = (String) reportParameters.get(ReportParameters.FILE_FORMAT);
				if (ext != null) {
					// 180313: as in export task, the user is responsable for expecify the file extension
				//	exportFileName = exportFileName.endsWith(ext) ? exportFileName : exportFileName + ext;
				}
				monitor.setProgress(0, "printing.print");
				printReport(jasperPrint);

				// notify the user of report generation is done acording to out type
				String rd = (String) reportParameters.get(ReportParameters.REPORT_DESCRIPTION);
				if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.PRINTER)) {
					Alesia.showNotification("notification.msg22", rd);
				}
				if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.EXPORT)) {
					Alesia.showNotification("notification.msg23", rd, exportFileName);
				}
			}

			monitor.dispose();
			System.out.println("Report.run " + ((String) reportParameters.get(ReportParameters.REPORT_NAME)));
			System.out.println("  Total Execution Time: " + (System.currentTimeMillis() - t1));
			return jasperPrint;
		} catch (Exception ex) {
			// notify the user, log exeption and throw execpion for future use
			monitor.dispose();
			SystemLog.logException(ex);
			Alesia.showNotification("notification.msg00", ex.getMessage());
			throw ex;
		}
	}

	public Hashtable getTaskParameters() {
		return reportParameters;
	}

	/**
	 * Final step of report generation. Out the {@link JasperPrint} report to the destination setted by user in export
	 * option panel.
	 * 
	 * @param prt - JasperPrint to view, print or export
	 * @throws JRException
	 */
	@SuppressWarnings("deprecation")
	public void printReport(JasperPrint prt) throws Exception {
		if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.WINDOW)) {
			Report.viewJasperPrint(prt);
		}
		if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.PRINTER)) {
			JasperPrintManager.printReport(prt, false);
		}
		if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.EXPORT)) {
			String ff = (String) reportParameters.get(ReportParameters.FILE_FORMAT);
			if (ff.equals(".pdf")) {
				JasperExportManager.exportReportToPdfFile(prt, exportFileName);
			}
			if (ff.equals(".xml")) {
				JasperExportManager.exportReportToXmlFile(prt, exportFileName, false);
			}
			if (ff.equals(".xmlEmbed")) {
				JasperExportManager.exportReportToXmlFile(prt, exportFileName, true);
			}
			if (ff.equals(".html")) {
				JasperExportManager.exportReportToHtmlFile(prt, exportFileName);
			}
			if (ff.equals(".xls")) {
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, prt);
				exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, exportFileName);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.exportReport();
			}
			if (ff.equals(".csv")) {
				JRCsvExporter exporter = new JRCsvExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, prt);
				exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, exportFileName);
				exporter.exportReport();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * this method update the {@link #future} and {@link #monitor} variable
	 */
	@Override
	public void setFuture(Future f, boolean ab) {
		this.future = f;
		this.monitor = new TProgressMonitor("printer2", "Impresion", future, ab);
		// avoid thread look in this method invocation
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				monitor.show(Alesia.mainFrame);
			}
		});
	}

	/**
	 * 3th step of report generation. Fill the report with data.
	 * <p>
	 * This method update the{@link #jasperPrint} variable
	 * 
	 * @throws Exception
	 */
	protected void fillReport() throws Exception {
		/*
		 * jasperPrint = null; TableModel org = getTableModel(); JRTableModelDataSource jrtmds = new
		 * JRTableModelDataSource(org); jasperPrint = JasperFillManager.fillReport(report, jasperParameters, jrtmds);
		 */
	}

	/**
	 * this method is called by {@link #fillReport()} to allow subclass generate its own {@link TableModel} instance.
	 * this tablemodel will be used to create a {@link JRTableModelDataSource}
	 * 
	 * @return - TableModel with data
	 */
	protected TableModel getTableModel() {
		return null;
	}

	/**
	 * first report generation step. Create the {@link JasperReport} instance from the compiled .jasper file located on
	 * resource folder. the file name is taked from {@link ReportParameters#REPORT_NAME}
	 * <p>
	 * this method update the {@link #report} variable
	 * 
	 * @throws Exception
	 */
	protected void loadReport() throws Exception {
		this.report = null;
		String rn = ((String) reportParameters.get(ReportParameters.REPORT_NAME)) + ".jasper";
		this.report = (JasperReport) JRLoader.loadObject(new FileInputStream(TResources.getFile(rn)));
	}

	/**
	 * Second report generation step. Set the JasperReport parameters.
	 * <p>
	 * this method update the {@link #jasperParameters} content.
	 */
	protected void setReportParameters() {
		jasperParameters.put("report.date", new Date());
		jasperParameters.put("report.logo", TResources.getFile("images/Report_logo.gif").getAbsolutePath());

		// todos los campos de la compañia seleccionada se establecen como parametros
		// Record p = Session.getPartner();
		// for (int i = 0; i < p.getFieldCount(); i++) {
		// parameters.put("report.partner." + p.getFieldName(i), p.getFieldValue(i));
		// }
		// Record addr = DataBaseUtilities.getFromSerializedForm((String) p.getFieldValue("paaddress"));
		// parameters.put("report.partner.paaddress", TResources.formatAddress(addr));
	}

	@Override
	public void setTaskParameters(Hashtable parms) {
		reportParameters.putAll(parms);
	}
}
