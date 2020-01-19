package plugins.ratass;
import java.awt.*;
import java.util.*;
import java.util.List;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.statistics.*;

import core.*;

public class HistogramBarChart2 {

	private ChartPanel chartPanel;
	private JFreeChart chart;
	private HistogramDataset dataset;

	public HistogramBarChart2() {
		this.dataset = new HistogramDataset();
		this.chart = createChart(dataset);
		this.chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 200));
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}


	public void setDataset(Hashtable<Integer, Integer> histogram, String name) {
		
		dataset = new HistogramDataset();
		ArrayList<Integer> values = new ArrayList<>(histogram.values());
		int binds = histogram.size();
		double[] arrayOfDouble = new double[binds];
		double max = 0;
		for (int i = 0; i < arrayOfDouble.length; i++) {
			double val = values.get(i).doubleValue();
			arrayOfDouble[i] = val;
			max = val > max ? val : max;
		}
		dataset.addSeries(name, arrayOfDouble, binds);
		XYPlot xYPlot = (XYPlot) chart.getPlot();
		xYPlot.setDataset(dataset);

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
