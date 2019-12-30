package plugins.hero;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;

import core.*;

public class XYBarChartDemo1 extends ApplicationFrame {

	private JPanel chartPanel;
	private JFreeChart chart;
	private DefaultCategoryDataset dataset;

	public XYBarChartDemo1(String paramString) {
		super(paramString);
		this.dataset = new DefaultCategoryDataset();
		this.chart = createChart(dataset);
		this.chartPanel = new ChartPanel(chart);
		Timer t = new Timer(1000, l -> setDataSet(example()));
		t.start();
		chartPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private Vector<TEntry<String, Double>> example() {
		Vector<TEntry<String, Double>> example = new Vector<>();
		example.add(new TEntry<>("fold", Math.random() * 2.0));
		example.add(new TEntry<>("call", Math.random() * 3.0));
		example.add(new TEntry<>("raise", Math.random() * 4.0));
		return example;
	}

	public void setDataSet(Vector<TEntry<String, Double>> example) {
		dataset.clear();
		example.forEach(te -> dataset.addValue(te.getValue(), "", te.getKey()));
		// CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		// categoryPlot.setDataset(dataset);
	}

	private static JFreeChart createChart(CategoryDataset paramCategoryDataset) {
		JFreeChart chart = ChartFactory.createBarChart("Actions", "Category", "EV", paramCategoryDataset);
		CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
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

	public static void main(String[] paramArrayOfString) {
		XYBarChartDemo1 xYBarChartDemo1 = new XYBarChartDemo1("State Executions - USA");
		xYBarChartDemo1.pack();
		RefineryUtilities.centerFrameOnScreen(xYBarChartDemo1);
		xYBarChartDemo1.setVisible(true);
	}
}
