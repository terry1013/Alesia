package delete;

import java.awt.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;

import core.datasource.*;


public class DualAxisDemo5 {
	
	private ChartPanel localChartPanel;
	private Record percentRecord, amountRecord;
	public DualAxisDemo5(Record pr, Record ar) {
		this.percentRecord = pr;
		this.amountRecord = ar;
		CategoryDataset localCategoryDataset1 = createPercentDataset(percentRecord);
		CategoryDataset localCategoryDataset2 = createAmountDataset(amountRecord);
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
	private static JFreeChart createChart(CategoryDataset paramCategoryDataset1, CategoryDataset paramCategoryDataset2) {
		CategoryAxis localCategoryAxis = new CategoryAxis(null);
		// percent axis
//		NumberAxis localNumberAxis1 = new NumberAxis("Value");
		NumberAxis localNumberAxis1 = new NumberAxis(null);
		BarRenderer localBarRenderer1 = new BarRenderer();
		
		DualAxisDemo51 local1 = new DualAxisDemo51(paramCategoryDataset1,
				localCategoryAxis, localNumberAxis1, localBarRenderer1);
		
		JFreeChart localJFreeChart = new JFreeChart(null, local1);
		localJFreeChart.getLegend().setVisible(false);
		localJFreeChart.setBackgroundPaint(Color.WHITE);
		
		local1.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		local1.setDataset(1, paramCategoryDataset2);
		local1.mapDatasetToRangeAxis(1, 1);
		
		// amount axis
//		NumberAxis localNumberAxis2 = new NumberAxis("Secondary");
		NumberAxis localNumberAxis2 = new NumberAxis(null);
		local1.setRangeAxis(1, localNumberAxis2);
		local1.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		BarRenderer localBarRenderer2 = new BarRenderer();
		local1.setRenderer(1, localBarRenderer2);
		
		StandardChartTheme sct = new StandardChartTheme("JFree");
		sct.apply(localJFreeChart);
		
//		ChartUtilities.applyCurrentTheme(localJFreeChart);
		return localJFreeChart;
	}
}
