package plugins.hero;

import java.awt.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.ui.*;
import org.jfree.data.category.*;
import org.jfree.data.statistics.*;

import core.*;

public class ActionsBarChart {

	private ChartPanel chartPanel;
	private JFreeChart chart;
	private DefaultCategoryDataset dataset;

	public ActionsBarChart() {
		this.dataset = new DefaultCategoryDataset();
		this.chart = createChart(dataset);
		this.chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 200));
	}

	public void addDataset(Hashtable<Integer, Integer> histogram, String name) {
		Set<Integer> keys = histogram.keySet();
		for (Integer key : keys) {
			Color col = new Color(key);
			String colval = TColorUtils.getRGBColor(col);
			dataset.addValue(histogram.get(key), name, colval);
		}
		// change the color of the bars
		// XYPlot xYPlot = (XYPlot) chart.getPlot();
		// XYBarRenderer xYBarRenderer = (XYBarRenderer) xYPlot.getRenderer();
		// xYBarRenderer.setDrawBarOutline(false);
		// xYBarRenderer.setBarPainter(new StandardXYBarPainter());
		// xYBarRenderer.setShadowVisible(false);
	}

	/**
	 * set the dataset for histogram
	 * 
	 * @param histogram
	 * @param name
	 */
	public void setDataset(Hashtable<Integer, Integer> histogram, String name) {
		dataset.clear();
		addDataset(histogram, name);
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	private String removePrefix(String action) {
		return action.replace("raise.", "");
	}
	public void setCategoryMarker(String category) {
		CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		categoryPlot.clearDomainMarkers();
		if (category == null)
			return;
		String cat = removePrefix(category);
		CategoryMarker categoryMarker = new CategoryMarker(cat);
		categoryMarker.setPaint(Color.BLUE);
		categoryMarker.setAlpha(0.3F);
		categoryPlot.addDomainMarker(categoryMarker, Layer.FOREGROUND);
	}

	public void setTitle(String title) {
		chart.getTitle().setText(title);
	}

	public void setDataSet(Vector<TEntry<String, Double>> example) {
		dataset.clear();

		// fill the dataset with empty slots to keep the shape of the graphics
		if (example == null)
			example = new Vector<>();

		example.forEach(te -> dataset.addValue(te.getValue(), "", removePrefix(te.getKey())));
		int morec = 15 - dataset.getColumnCount();
		if (morec < 1)
			return;

		for (int i = 0; i < morec; i++) {
			dataset.addValue(0, "", "Empty slot " + i);
		}
		// CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		// categoryPlot.setDataset(dataset);
	}

	private static JFreeChart createChart(CategoryDataset paramCategoryDataset) {
		JFreeChart chart = ChartFactory.createBarChart(null, null, null, paramCategoryDataset);
		CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		categoryPlot.setNoDataMessage("NO DATA!");
		categoryPlot.setDomainGridlinesVisible(true);
		categoryPlot.setRangeCrosshairVisible(true);
		categoryPlot.setRangeCrosshairPaint(Color.blue);

		Plot plot = chart.getPlot();
		// xYPlot.setDomainPannable(true);
		// xYPlot.setRangePannable(true);
		plot.setBackgroundPaint(Color.white);
		plot.setForegroundAlpha(0.85F);

		CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
		categoryAxis
				.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.5235987755982988D));

		chart.getLegend().setVisible(false);
		chart.setBackgroundPaint(Color.WHITE);

		// StandardChartTheme sct = new StandardChartTheme("Legacy");
		// sct.apply(chart);
		// ChartUtils.applyCurrentTheme(jFreeChart);
		return chart;
	}

	private static JFreeChart createChart(HistogramDataset histogramDataset) {
		JFreeChart jFreeChart = ChartFactory.createHistogram(null, null, null, histogramDataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot xYPlot = (XYPlot) jFreeChart.getPlot();
		xYPlot.setDomainPannable(true);
		xYPlot.setRangePannable(true);
		xYPlot.setBackgroundPaint(Color.white);
		xYPlot.setForegroundAlpha(0.85F);
		// NumberAxis numberAxis;
		// numberAxis.setStandardTickUnits((numberAxis = (NumberAxis) xYPlot.getRangeAxis()).createIntegerTickUnits());
		XYBarRenderer xYBarRenderer = (XYBarRenderer) xYPlot.getRenderer();
		xYBarRenderer.setDrawBarOutline(false);
		xYBarRenderer.setBarPainter(new StandardXYBarPainter());
		xYBarRenderer.setShadowVisible(false);
		return jFreeChart;
	}
}
