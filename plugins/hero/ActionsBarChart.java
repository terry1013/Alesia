package plugins.hero;

import java.awt.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.ui.*;
import org.jfree.data.category.*;

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

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public void setCategoryMarker(String category) {
		CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		categoryPlot.clearDomainMarkers();
		if (category == null)
			return;

		CategoryMarker categoryMarker = new CategoryMarker(category);
		categoryMarker.setPaint(Color.BLUE);
		categoryMarker.setAlpha(0.3F);
		categoryPlot.addDomainMarker(categoryMarker, Layer.FOREGROUND);
	}

	public void setTitle(String title) {
		chart.getTitle().setText(title);
	}

	public void setDataSet(Vector<TEntry<String, Double>> example) {
		dataset.clear();

		// fill the dataset with empty sloot to keep the shape of the graphics
		if (example == null)
			example = new Vector<>();
		
		example.forEach(te -> dataset.addValue(te.getValue(), "", te.getKey()));
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
		CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
		categoryAxis
				.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.5235987755982988D));

		chart.getLegend().setVisible(false);
		chart.setBackgroundPaint(Color.WHITE);

		StandardChartTheme sct = new StandardChartTheme("Legacy");
		sct.apply(chart);
		// ChartUtils.applyCurrentTheme(jFreeChart);
		return chart;
	}

}
