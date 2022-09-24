package eu.floraresearch.drm;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.StandardGradientPaintTransformer;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.floraresearch.drm.report.Report;
import eu.floraresearch.drm.report.ReportsHandler;

public class CapacitiesChart extends ApplicationFrame {

    private static final String ELEMENTS = ConfigReader.getInstance().readCategory_Label(3);
    private static final String GDC_ELEMENTS = ConfigReader.getInstance().readCategory_Label(4);
    private static final String TITLE = ConfigReader.getInstance().readGraphicTitle();

    public CapacitiesChart() {

	super(TITLE);
    }

    public static JFreeChart createChart(List<Report> reports) {

	CapacitiesChart demo = new CapacitiesChart();
	CategoryDataset dataset = demo.createDataset(reports);
	JFreeChart chart = demo.createChart(dataset);

	return chart;
    }

    private CategoryDataset createDataset(List<Report> reports) {

	DefaultCategoryDataset result = new DefaultCategoryDataset();

	int count = 1;
	
	GSLoggerFactory.getLogger(getClass()).info("------------------------\n\n");

	for (Report report : reports) {

	    String completeName = report.getCompleteName();
	    GSLoggerFactory.getLogger(getClass()).info("report [" + count + "/" + reports.size() + "]: "+completeName);

	    int gdcElements = report.refineRecordsCount()[1];
	    int elements = report.refineRecordsCount()[0];

	    GSLoggerFactory.getLogger(getClass()).info("gdc [" + gdcElements+"]");
	    GSLoggerFactory.getLogger(getClass()).info("elm [" + elements+"]");

	    try {

		result.addValue(gdcElements, GDC_ELEMENTS, String.valueOf(count));
		result.addValue(elements, ELEMENTS, String.valueOf(count));

		count++;

		GSLoggerFactory.getLogger(getClass()).info("------------------------\n\n");

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return result;
    }

    private JFreeChart createChart(final CategoryDataset dataset) {

	JFreeChart chart = ChartFactory.createBarChart(TITLE, "Capacity", "Elements", dataset, PlotOrientation.VERTICAL, true, true, false);

	chart.setBackgroundPaint(new Color(249, 231, 236));

	StackedBarRenderer renderer = new StackedBarRenderer();
	// renderer.setItemMargin(1000.5);
	renderer.setBase(1);

	Paint p1 = new GradientPaint(0.0f, 0.0f, new Color(16, 89, 172), 0.0f, 0.0f, new Color(201, 201, 244));
	renderer.setSeriesPaint(0, p1);

	Paint p2 = new GradientPaint(0.0f, 0.0f, new Color(10, 144, 40), 0.0f, 0.0f, new Color(160, 240, 180));
	renderer.setSeriesPaint(1, p2);

	renderer.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL));

	if (ConfigReader.getInstance().readAddGraphMap()) {
	    renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator("", "cat", "src"));
	}

	CategoryPlot plot = (CategoryPlot) chart.getPlot();

	// plot.setForegroundAlpha(0.5F);//
	plot.setRenderer(renderer);
	plot.setFixedLegendItems(createLegendItems());
	plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

	plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
	plot.getDomainAxis().setLowerMargin(0.01);
	plot.getDomainAxis().setUpperMargin(0.01);
	plot.getDomainAxis().setLabelFont(plot.getDomainAxis().getLabelFont().deriveFont(Font.BOLD));

	// Font font = plot.getDomainAxis().getTickLabelFont();
	// Paint paint = plot.getDomainAxis().getTickLabelPaint();

	LogarithmicAxis logaxis = new LogarithmicAxis("Granules");
	// logaxis.setTickLabelFont(font);
	// logaxis.setTickLabelPaint(paint);
	logaxis.setNumberFormatOverride(NumberFormat.getInstance());
	logaxis.setLabelFont(plot.getDomainAxis().getLabelFont().deriveFont(Font.BOLD));

	if (ConfigReader.getInstance().readUseLogScale()) {
	    try {
		plot.setRangeAxis(logaxis);
	    } catch (RuntimeException ex) {
		ex.printStackTrace();
	    }
	}

	return chart;
    }

    private LegendItemCollection createLegendItems() {

	LegendItemCollection result = new LegendItemCollection();

	LegendItem item1 = new LegendItem(GDC_ELEMENTS, GDC_ELEMENTS, GDC_ELEMENTS, GDC_ELEMENTS, new Rectangle(10, 10), new GradientPaint(
		0.0f, 0.0f, new Color(16, 89, 172), 0.0f, 0.0f, new Color(201, 201, 244)));

	LegendItem item2 = new LegendItem(ELEMENTS, ELEMENTS, ELEMENTS, ELEMENTS, new Rectangle(10, 10), new GradientPaint(0.0f, 0.0f,
		new Color(10, 144, 40), 0.0f, 0.0f, new Color(160, 240, 180)));

	result.add(item1);
	result.add(item2);

	return result;
    }

    public static void main(final String[] args) {

	CapacitiesChart demo = new CapacitiesChart();
	List<Report> fakeReports = ReportsHandler.getInstance().createFakeReports();
	CategoryDataset dataset = demo.createDataset(fakeReports);
	JFreeChart chart = demo.createChart(dataset);
	ChartPanel chartPanel = new ChartPanel(chart);

	chartPanel.setPreferredSize(new java.awt.Dimension(ConfigReader.getInstance().readGraphWidth(), ConfigReader.getInstance()
		.readGraphHeight()));

	demo.setContentPane(chartPanel);
	demo.pack();

	RefineryUtilities.centerFrameOnScreen(demo);
	demo.setVisible(true);
    }
}