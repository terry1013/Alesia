package delete;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;

import core.datasource.*;


public class DualAxisDemo5CopyOf {

	private ChartPanel localChartPanel;
	private Record percentRecord, amountRecord;
	public DualAxisDemo5CopyOf(Record pr, Record ar) {
		this.percentRecord = pr;
		this.amountRecord = pr;
		CategoryDataset localCategoryDataset1 = createPercentDataset(percentRecord);
		CategoryDataset localCategoryDataset2 = createAmountDataset(amountRecord);
		// CategoryDataset localCategoryDataset1 = createDataset1();
		// CategoryDataset localCategoryDataset2 = createDataset2();
		JFreeChart localJFreeChart = createChart(localCategoryDataset1, localCategoryDataset2);
		localChartPanel = new ChartPanel(localJFreeChart);
	}

	public ChartPanel getChartPanel() {
		return localChartPanel;
	}

	private static CategoryDataset createPercentDataset(Record r) {
		String rowstr = "Porcentaje";
		DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
		for (int c = 0; c < r.getFieldCount(); c++) {
			localDefaultCategoryDataset.addValue((Double) r.getFieldValue(c), rowstr, r.getFieldName(c));
		}
		for (int c = 0; c < r.getFieldCount(); c++) {
			localDefaultCategoryDataset.addValue(null, "dummy" + rowstr, r.getFieldName(c));
		}
		return localDefaultCategoryDataset;
	}
	private static CategoryDataset createAmountDataset(Record r) {
		String rowstr = "Monto";
		DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
		for (int c = 0; c < r.getFieldCount(); c++) {
			localDefaultCategoryDataset.addValue(null, "dummy" + rowstr, r.getFieldName(c));
		}
		for (int c = 0; c < r.getFieldCount(); c++) {
			localDefaultCategoryDataset.addValue((Double) r.getFieldValue(c), rowstr, r.getFieldName(c));
		}
		return localDefaultCategoryDataset;
	}

	private static CategoryDataset createDataset1() {
		String str1 = "Series 1";
		String str2 = "Dummy 1";
		String str3 = "Category 1";
		String str4 = "Category 2";
		String str5 = "Category 3";
		String str6 = "Category 4";
		DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
		localDefaultCategoryDataset.addValue(1.0D, str1, str3);
		localDefaultCategoryDataset.addValue(4.0D, str1, str4);
		localDefaultCategoryDataset.addValue(3.0D, str1, str5);
		localDefaultCategoryDataset.addValue(5.0D, str1, str6);
		localDefaultCategoryDataset.addValue(null, str2, str3);
		localDefaultCategoryDataset.addValue(null, str2, str4);
		localDefaultCategoryDataset.addValue(null, str2, str5);
		localDefaultCategoryDataset.addValue(null, str2, str6);
		return localDefaultCategoryDataset;
	}

	private static CategoryDataset createDataset2() {
		String str1 = "Dummy 2";
		String str2 = "Series 2";
		String str3 = "Category 1";
		String str4 = "Category 2";
		String str5 = "Category 3";
		String str6 = "Category 4";
		DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
		localDefaultCategoryDataset.addValue(null, str1, str3);
		localDefaultCategoryDataset.addValue(null, str1, str4);
		localDefaultCategoryDataset.addValue(null, str1, str5);
		localDefaultCategoryDataset.addValue(null, str1, str6);
		localDefaultCategoryDataset.addValue(75.0D, str2, str3);
		localDefaultCategoryDataset.addValue(87.0D, str2, str4);
		localDefaultCategoryDataset.addValue(96.0D, str2, str5);
		localDefaultCategoryDataset.addValue(68.0D, str2, str6);
		return localDefaultCategoryDataset;
	}

	private static JFreeChart createChart(CategoryDataset paramCategoryDataset1, CategoryDataset paramCategoryDataset2) {
		CategoryAxis localCategoryAxis = new CategoryAxis("Category");
		NumberAxis localNumberAxis1 = new NumberAxis("Value");
		BarRenderer localBarRenderer1 = new BarRenderer();
		DualAxisDemo51 local1 = new DualAxisDemo51(paramCategoryDataset1, localCategoryAxis, localNumberAxis1,
				localBarRenderer1);
		JFreeChart localJFreeChart = new JFreeChart(null, local1);
		local1.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		local1.setDataset(1, paramCategoryDataset2);
		local1.mapDatasetToRangeAxis(1, 1);
		NumberAxis localNumberAxis2 = new NumberAxis("Secondary");
		local1.setRangeAxis(1, localNumberAxis2);
		local1.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		BarRenderer localBarRenderer2 = new BarRenderer();
		local1.setRenderer(1, localBarRenderer2);
		ChartUtilities.applyCurrentTheme(localJFreeChart);
		return localJFreeChart;
	}
	/*
	 * private static JFreeChart createChart(CategoryDataset paramCategoryDataset1, CategoryDataset
	 * paramCategoryDataset2) { CategoryAxis localCategoryAxis = new CategoryAxis("Category"); NumberAxis
	 * localNumberAxis1 = new NumberAxis("Value"); BarRenderer localBarRenderer1 = new BarRenderer(); DualAxisDemo51
	 * local1 = new DualAxisDemo51(paramCategoryDataset1, localCategoryAxis, localNumberAxis1, localBarRenderer1);
	 * JFreeChart localJFreeChart = new JFreeChart(null, local1);
	 * local1.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT); local1.setDataset(1, paramCategoryDataset2);
	 * local1.mapDatasetToRangeAxis(1, 1); NumberAxis localNumberAxis2 = new NumberAxis("Secondary");
	 * local1.setRangeAxis(1, localNumberAxis2); local1.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	 * BarRenderer localBarRenderer2 = new BarRenderer(); local1.setRenderer(1, localBarRenderer2);
	 * ChartUtilities.applyCurrentTheme(localJFreeChart); return localJFreeChart; }
	 */
}
