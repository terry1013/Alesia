package core.reporting;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.csv.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class ExportTask implements TRunnable {

	private Vector<Record> rcdList;
	private Hashtable<String, Object> exportParameters;
	private String viewName;
	private Record rmodel;
	private ServiceResponse response;
	private ServiceRequest request;
	private Hashtable<String, String> fldtxt;
	// private DecimalFormat decimalF;

	public ExportTask(Hashtable ht) {
		this.exportParameters = ht;
		// decimalF = new DecimalFormat("##0.00;-##0.00");
	}

	/**
	 * Create a library of cell styles
	 */
	private static Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap();
		CellStyle style;
		Font titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short) 30);
		// titleFont.setBold(true);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(titleFont);
		styles.put("title", style);

		Font monthFont = wb.createFont();
		// monthFont.setFontHeightInPoints((short) 11);
		// monthFont.setBold(true);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		style = wb.createCellStyle();
		// style.setAlignment(HorizontalAlignment.CENTER);
		// style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(monthFont);
		// style.setWrapText(true);
		styles.put("header", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true);
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("cell", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
		styles.put("formula", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		// style.setVerticalAlignment(VerticalAlignment.CENTER);
		// style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		// style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
		styles.put("Double", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		// style.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy hh:mm AM/PM"));
		styles.put("Date", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		// style.setVerticalAlignment(VerticalAlignment.CENTER);
		// style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		// style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// style.setDataFormat(wb.createDataFormat().getFormat("0,00"));
		styles.put("String", style);

		return styles;
	}

	/**
	 * Sort the lists of records inside of the vector pass as argumento acording to the natural order of the object
	 * stored in field name.
	 * 
	 * @param rl - Vector of Record to sort
	 * @param fn - field name those values will be sorted
	 * @return new vector with all elemento sorted
	 */
	public static Vector<Record> sort(Vector<Record> rl, String fn) {
		ArrayList<TEntry> sortl = new ArrayList<TEntry>();
		for (Record r : rl) {
			Object key = r.getFieldValue(fn);
			sortl.add(new TEntry(r, key));
		}
		Collections.sort(sortl);
		Vector<Record> sv = new Vector<Record>(rl.size());
		for (TEntry te : sortl) {
			sv.add((Record) te.getKey());
		}
		return sv;
	}

	@Override
	public void run() {
		try {
			request = (ServiceRequest) exportParameters.get(ExportParameters.SERVICE_REQUEST);
			response = ServiceConnection.sendTransaction(request);
			rmodel = (Record) response.getParameter(ServiceResponse.RECORD_MODEL);
			rcdList = (Vector) response.getData();

			fldtxt = TStringUtils.getFieldsDescriptions(request);

			String ff = (String) exportParameters.get(ExportParameters.FILE_FORMAT);
			String fn = (String) exportParameters.get(ExportParameters.FILE_NAME);
			if (ff.equals(".xlsx")) {
				exportBinaryExcel();
			} else {
				exportCsv();
			}
			Alesia.showNotification("notification.msg19", fn);
		} catch (Exception ex) {
			// notify the user and log exeption
			SystemLog.logException(ex);
			Alesia.showNotification("notification.msg18", ex.getMessage());
		}
	}

	private void exportBinaryExcel() throws Exception {
		// view text are used as sheet name
		String vn = (String) exportParameters.get("viewName");
		viewName = TStringUtils.getString("action." + vn);
		String fileName = (String) exportParameters.get(ExportParameters.FILE_NAME);
		File f = new File(fileName);
		XSSFWorkbook workbook = null;
		Sheet sheet = null;
		boolean headfoot = false;
		// if file exist, append new sheet to it. otherwise, crete new workbook
		if (f.exists()) {
			FileInputStream fis = new FileInputStream(fileName);
			workbook = new XSSFWorkbook(fis);
			fis.close();
			sheet = workbook.getSheet(vn);
			if (sheet != null) {
				workbook.removeSheetAt(workbook.getSheetIndex(sheet));
			}
		} else {
			workbook = new XSSFWorkbook();
			headfoot = true;
		}
		sheet = workbook.createSheet(vn);

		Map<String, CellStyle> styles = createStyles(workbook);
		int rowcnt = 0;
		String[] fldlist = ((String) exportParameters.get(ExportParameters.FIELD_LIST)).split(";");

		// header row
		String he = (String) exportParameters.get(ExportParameters.EXPORT_HEADER);
		if (!he.equals("expnoh")) {
			Row headerRow = sheet.createRow(rowcnt++);
			headerRow.setHeightInPoints(15);
			Cell headerCell;
			for (int co = 0; co < fldlist.length; co++) {
				String fld = fldlist[co];
				headerCell = headerRow.createCell(co);
				// header fld or description
				headerCell.setCellValue(he.equals("expnam") ? fld : fldtxt.get(fld));
				headerCell.setCellStyle(styles.get("header"));
			}
		}

		// data: diferent procedures according to servicerequest
		for (Record r : rcdList) {

			Row row = sheet.createRow(rowcnt++);
			for (int co = 0; co < fldlist.length; co++) {
				String fld = fldlist[co];
				Object val = r.getFieldValue(fld);
				Cell cel = row.createCell(co);
				val = fixFieldValue(val);
				if (val instanceof Double) {
					cel.setCellStyle(styles.get("Double"));
					cel.setCellValue((Double) val);
					continue;
				}
				cel.setCellStyle(styles.get("String"));
				cel.setCellValue(val.toString());
			}
		}

		// column size
		for (int co = 0; co < rmodel.getFieldCount(); co++) {
			sheet.autoSizeColumn(co);
		}

		// head and footer
		if (headfoot) {
			Date d = new Date();
			Sheet sh = workbook.getSheetAt(0);
			Header header = sh.getHeader();
			header.setLeft("&16Planc\n&10Planificación y presupuesto laboral");
			header.setCenter("&14" + viewName);
			header.setRight("&9Exportado por " + Session.getUserName() + "\n" + "Fecha "
					+ SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(d) + "\n" + "Pagina &P de &N");

			Footer foot = sh.getFooter();
			foot.setLeft("Av. principal valle arriba, CC Daymar Piso 2, Local P2-09 Guatire estado Miranda, Venezuela. \n"
					+ "+058 (212) 341-61-18 (212) 429-71-63 (424) 186-53-27 \n" + "info@sle.com.ve");

		}
		FileOutputStream fos = new FileOutputStream(fileName);
		workbook.write(fos);
		fos.close();
	}

	private void exportCsv() throws Exception {
		String ff = (String) exportParameters.get(ExportParameters.FILE_FORMAT);

		// file format
		CSVFormat csvf = CSVFormat.DEFAULT;
		csvf = ff.equals("EXCEL") ? CSVFormat.EXCEL : csvf;
		csvf = ff.equals("MYSQL") ? CSVFormat.MYSQL : csvf;
		csvf = ff.equals("RFC4180") ? CSVFormat.RFC4180 : csvf;
		csvf = ff.equals("TDF") ? CSVFormat.TDF : csvf;

		String[] flds = ((String) exportParameters.get(ExportParameters.FIELD_LIST)).split(";");

		// header
		String hf = (String) exportParameters.get(ExportParameters.EXPORT_HEADER);
		String[] header = null;
		if (!hf.equals("expnoh")) {
			// export field name or field description
			header = new String[flds.length];
			for (int i = 0; i < header.length; i++) {
				header[i] = hf.equals("expnam") ? flds[i] : fldtxt.get(flds[i]);
			}
			csvf = csvf.withHeader(header);
		}

		// target file
		File f = new File((String) exportParameters.get(ExportParameters.FILE_NAME));
		FileWriter out = new FileWriter(f);
		CSVPrinter printer = csvf.print(out);

		// data: diferent procedures according to servicerequest
		for (Record rcd : rcdList) {
			Object[] csvr = new Object[flds.length];
			for (int co = 0; co < flds.length; co++) {
				String fld = flds[co];
				Object val = rcd.getFieldValue(fld);
				val = fixFieldValue(val);
				csvr[co] = val;
			}
			csvf.printRecord(out, csvr);
		}
		printer.close();
	}

	/**
	 * method that take care of some data considerations. This method is invoked before write the <code>val</code>
	 * object to the output file.
	 * 
	 * @param val - object to modify
	 */
	private Object fixFieldValue(Object val) {
		// date consideration: if < zero date, convert to empty string
		if (val instanceof Date) {
			Date d = (Date) val;
			return d.getTime() < 0 ? "" : d;
		}
		// Double: reduce decimal digits to 2
		if (val instanceof Double) {
			double sc = Math.pow(10, 2);
			double dval = Math.round(((Double) val) * sc) / sc;
			val = dval;
			// val = decimalF.format(dval);
		}

		return val;
	}

	@Override
	public Hashtable getTaskParameters() {
		return exportParameters;
	}

	@Override
	public void setTaskParameters(Hashtable parms) {
		exportParameters.putAll(parms);
	}

	@Override
	public void setFuture(Future f, boolean ab) {
		
	}
}
