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
	AvailabilityMonitor monitor = AvailabilityMonitor.getInstance();

	String viewId = request.getParameter("view");

	if (viewId != null && !viewId.trim().isEmpty()) {

		ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
		IDiscoveryExecutor executor = loader.iterator().next();

		DiscoveryMessage discoveryMessage = new DiscoveryMessage();
		discoveryMessage.setRequestId(UUID.randomUUID().toString());
		discoveryMessage.setPage(new Page(1, 1000));
		discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
		discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
		StorageInfo uri = ConfigurationWrapper.getDatabaseURI();
		discoveryMessage.setDataBaseURI(uri);

		Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getDatabaseURI(), viewId);
		WebRequestTransformer.setView(view.get().getId(), ConfigurationWrapper.getDatabaseURI(), discoveryMessage);
		discoveryMessage.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
		ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
		List<GSResource> resources = resultSet.getResultsList();
		out.println("<h1>" + viewId.toUpperCase() + " monitoring dashboard</h1>");
		out.println("<h2>Data availability</h2>");
		out.println(
		"<table><tr><th>Source</th><th>Status</th><th>Last success download date</th><th>Last failed download date</th></tr>");

		resources.sort(new Comparator<GSResource>() {
			public int compare(GSResource o1, GSResource o2) {
		return o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
			}
		});

		for (GSResource resource : resources) {
			GSSource source = resource.getSource();
			String sourceId = source.getUniqueIdentifier();
			String sourceLabel = source.getLabel();
			Date lastGood = monitor.getLastDownloadDate(sourceId);
			Date lastGoodStationID = monitor.getLastDownloadPlatformId(sourceId);
			Date lastBad = monitor.getLastFailedDownloadDate(sourceId);
			Date lastBadStationID = monitor.getLastFailedDownloadPlatformId(sourceId);
			String sourceString = sourceLabel;
			String status = "<td bgcolor='blue'><b>Only metadata available</b></td>";
			if (lastBad != null) {
		status = "<td bgcolor='red'><b>Download issues</b></td>";
			}
			if (lastGood != null && (lastBad == null || lastGood.after(lastBad))) {
		status = "<td bgcolor='green'><b>Download available</b></td>";
			}
			out.println("<tr><td>" + sourceString + "</td>" + status + "<td><a href='" + lastGoodStationID + "'>" + lastGood
			+ "</></td><td><a href='/gs-service/services/view/his-central/bnhs/station/" + lastBadStationID + "/'>"
			+ lastBad + "</a></td></tr>");
		}
		out.println("</table>");

	} else {
		out.println("A view parameter is needed here");
	}
	%>
</body>
</html>