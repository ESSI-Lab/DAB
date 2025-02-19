<%@page import="eu.essi_lab.access.availability.DownloadReport"%>
<%@page import="java.util.concurrent.Future"%>
<%@page import="java.util.AbstractMap.SimpleEntry"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.concurrent.Callable"%>
<%@page import="java.util.concurrent.Executors"%>
<%@page import="java.util.concurrent.ExecutorService"%>
<%@page import="java.util.stream.Stream"%>
<%@page import="eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer"%>
<%@page import="eu.essi_lab.access.availability.DownloadInformation"%>
<%@page import="java.util.Comparator"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="eu.essi_lab.model.resource.GSResource"%>
<%@page import="eu.essi_lab.messages.ResultSet"%>
<%@page import="eu.essi_lab.model.resource.ResourceProperty"%>
<%@page import="eu.essi_lab.messages.bond.BondFactory"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%@page import="eu.essi_lab.messages.RequestMessage.IterationMode"%>
<%@page import="eu.essi_lab.model.StorageInfo"%>
<%@page import="eu.essi_lab.messages.Page"%>
<%@page import="eu.essi_lab.messages.DiscoveryMessage"%>
<%@page import="java.util.ServiceLoader"%>
<%@page import="eu.essi_lab.request.executor.IDiscoveryExecutor"%>
<%@page import="eu.essi_lab.access.availability.AvailabilityMonitor"%>
<%@page import="eu.essi_lab.lib.utils.ISO8601DateTimeUtils"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.UUID"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.pdk.BouncerTool"%>
<%@page import="eu.essi_lab.pdk.BouncerRequest"%>
<%@page import="eu.essi_lab.pdk.RedisTool"%>
<html>
<head>
<title>Dashboard</title>
<style>
</style>
</head>
<body>
	<%
	AvailabilityMonitor.getInstance().getS3TransferManager();

	String viewId = request.getParameter("view");

	if (viewId != null && !viewId.trim().isEmpty()) {

	    View view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getDatabaseURI(), viewId).get();

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setOutputSources(true);
	    message.setPage(new Page(1, 1000));

	    List<GSSource> sources = ConfigurationWrapper.getViewSources(view);
	    out.println("<!DOCTYPE html>\n" + //
	    "<html lang=\"en\">\n" + //
	    "<head>\n" + //
	    "<meta charset=\"UTF-8\">\n" + //
	    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + //
	    "<title>Load Page in Div</title>\n" + //
	    "<script>\n" + //
	    "function loadIframe(sourceId) {\n" + //
	    "document.getElementById(\"details\").innerHTML =\n" + // 
	    "'<iframe src=\"dashboard-details.jsp?source='+sourceId+'&view=" + viewId
	    + "\" width=\"600\" height=\"400\" style=\"border: none;\"></iframe>';\n" + //
	    "}\n" + //
	    "</script>\n" + //
	    "</head>\n" + //
	    "<body>");
	    out.println("<h1>" + viewId.toUpperCase() + " monitoring dashboard</h1>");
	    out.println("<h2>Data availability</h2>");
	    out.println(
	    "<table><tr><th>Source</th><th>Status</th><th>Detailed info</th><th>Last success download date</th><th>Last failed download date</th></tr>");

	    sources.sort(new Comparator<GSSource>() {
		public int compare(GSSource o1, GSSource o2) {
	    return o1.getLabel().compareTo(o2.getLabel());
		}
	    });

	    ExecutorService executor = Executors.newFixedThreadPool(5);

	    // Define 3 tasks
	    List<Callable<DownloadReport>> tasks = new ArrayList<>();

	    for (GSSource source : sources) {

		final GSSource fSource = source;
		Callable<DownloadReport> task = new Callable<DownloadReport>() {
	    public DownloadReport call() throws Exception {
			AvailabilityMonitor monitor = AvailabilityMonitor.getInstance();

			DownloadInformation goodInfo = monitor.getLastDownloadDate(fSource.getUniqueIdentifier());
			DownloadInformation badInfo = monitor.getLastFailedDownloadDate(fSource.getUniqueIdentifier());
			DownloadReport report = new DownloadReport(fSource, goodInfo, badInfo);
			return report;
	    }
		};
		tasks.add(task);
	    }

	    try {
		// Invoke all tasks and wait for completion
		List<Future<DownloadReport>> results = executor.invokeAll(tasks);

		// Retrieve results
		for (Future<DownloadReport> result : results) {
	    DownloadReport report = result.get();
	    GSSource source = report.getSource();
	    String sourceId = source.getUniqueIdentifier();
	    String sourceLabel = source.getLabel();
	    DownloadInformation goodInfo = report.getGoodInfo();
	    Date lastGood = goodInfo == null ? null : goodInfo.getDate();
	    String lastGoodStationID = goodInfo == null ? null : goodInfo.getPlatformId();
	    DownloadInformation badInfo = report.getBadInfo();
	    Date lastBad = badInfo == null ? null : badInfo.getDate();
	    String lastBadStationID = badInfo == null ? null : badInfo.getPlatformId();
	    String sourceString = sourceLabel;
	    String status = "<td bgcolor='blue'><b>Only metadata available</b></td>";
	    if (lastBad != null) {
			status = "<td bgcolor='red'><b>Download issues</b></td>";
	    }
	    if (lastGood != null && (lastBad == null || lastGood.after(lastBad))) {
			status = "<td bgcolor='green'><b>Download available</b></td>";
	    }
	    out.println("<tr><td>" + sourceString + "</td>" + status + "<td><button onclick=\"loadIframe('" + sourceId + "')\">Details</button></td><td>" + lastGood + "</td><td>" + lastBad
			    + "</td></tr>");
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		executor.shutdown();
	    }

	    out.println("</table><p>Details:</p><div id=\"details\">Click the info buttons to load the details.</div></body></html>");

	} else {
	    out.println("A view parameter is needed here");
	}
	%>
</body>
</html>