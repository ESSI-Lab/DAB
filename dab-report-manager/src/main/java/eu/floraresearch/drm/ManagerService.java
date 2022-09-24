package eu.floraresearch.drm;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.servlet.ServletUtilities;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.floraresearch.drm.headermng.HeaderManager;
import eu.floraresearch.drm.headermng.HeaderManagerFactory;
import eu.floraresearch.drm.report.Report;
import eu.floraresearch.drm.report.ReportsHandler;

public class ManagerService extends HttpServlet {

    private static final long serialVersionUID = 8831516148615887116L;
    private String table;
    private JFreeChart chart;
    private ChartRenderingInfo info;
    private TableManager tableManager;

    public ManagerService() {

	info = new ChartRenderingInfo(new StandardEntityCollection());
	tableManager = new TableManager();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	String start = req.getParameter("action");

	if (start != null && start.equals("start")) {

	    try {
		List<Report> reports = ReportsHandler.getInstance().getReports();

		// List<Report> reports = ReportManager.getInstance().createFakeReports();

		chart = CapacitiesChart.createChart(reports);
		table = tableManager.createTable(reports);

		serve(req, resp);
		
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	} else {

	    if (chart == null) {
		resp.setContentType(MediaType.TEXT_HTML.toString());
		resp.getWriter().append("<HTML><BODY>");
		resp.getWriter().append("<h2>Start the computation with \"action=start\"</h2>");
		resp.getWriter().append("</HTML></BODY>");
	    } else {
		serve(req, resp);
	    }
	}
    }

    private void serve(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	resp.setContentType(MediaType.TEXT_HTML.toString());

	resp.getWriter().append("<HTML>");
	resp.getWriter().append("<HEAD><TITLE>" + ConfigReader.getInstance().readWindowTitle()
		+ ISO8601DateTimeUtils.getISO8601DateTime(Calendar.getInstance().getTime()) + "</TITLE></HEAD>");

	resp.getWriter().append(tableManager.getTableStyle());

	HeaderManager hm = HeaderManagerFactory.createHeaderManager();

	resp.getWriter().append(hm.createHeader());

	if (ConfigReader.getInstance().readShowGraph()) {

	    String fileName = ServletUtilities.saveChartAsPNG(chart, ConfigReader.getInstance().readGraphWidth(),
		    ConfigReader.getInstance().readGraphHeight(), info, req.getSession(true));

	    ChartUtilities.writeImageMap(resp.getWriter(), fileName, info, true);

	    String graphURL = req.getContextPath() + "/servlet/DisplayChart?filename=" + fileName;

	    String useMap = ConfigReader.getInstance().readAddGraphMap() ? "usemap=\"#" + fileName + "\"" : "";
	    resp.getWriter().append("<img src=\"" + graphURL + "\" " + useMap + " border=\"1\" />");
	}

	resp.getWriter().append(table);

	// resp.getWriter()
	// .append("<label class=\"rotation\">Date " + TimeAndDateHelper.toISO8601GMTDate(Calendar.getInstance()) +
	// "</label>");

	if (ConfigReader.getInstance().readShowUOSPublishedTag()) {
	    resp.getWriter().append(tableManager.getUOSPublishedTag());
	}

	resp.getWriter().append("</BODY>");
	resp.getWriter().append("</HTML>");

	resp.getWriter().flush();
	resp.getWriter().close();
    }
}
