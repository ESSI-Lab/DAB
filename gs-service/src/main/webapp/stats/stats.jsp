<%@ page language="java" contentType="text/csv; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@page import="eu.essi_lab.iso.datamodel.classes.VerticalExtent"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.MIMetadata"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox"%>
<%@page import="eu.essi_lab.model.resource.GSResource"%>
<%@page import="eu.essi_lab.messages.ResultSet"%>
<%@page import="eu.essi_lab.messages.RequestMessage.IterationMode"%>
<%@page import="java.util.UUID"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Arrays"%>
<%@page import="eu.essi_lab.model.StorageInfo"%>
<%@page import="net.sf.saxon.expr.instruct.ForEach"%>
<%@page import="java.util.HashSet"%>
<%@page import="eu.essi_lab.model.resource.ResourceProperty"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="eu.essi_lab.model.Queryable"%>
<%@page import="java.util.ArrayList"%>
<%@page import="eu.essi_lab.messages.termfrequency.TermFrequencyItem"%>
<%@page import="eu.essi_lab.messages.stats.ComputationResult"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.messages.stats.ResponseItem"%>
<%@page import="eu.essi_lab.messages.stats.StatisticsResponse"%>
<%@page import="eu.essi_lab.messages.Page"%>
<%@page import="eu.essi_lab.request.executor.IStatisticsExecutor"%>
<%@page import="eu.essi_lab.messages.stats.StatisticsMessage"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.messages.bond.ResourcePropertyBond"%>
<%@page import="eu.essi_lab.messages.DiscoveryMessage"%>
<%@page import="java.util.ServiceLoader"%>
<%@page import="eu.essi_lab.request.executor.IDiscoveryExecutor"%>
<%@page import="eu.essi_lab.messages.ResourceSelector.IndexesPolicy"%>
<%@page import="eu.essi_lab.messages.ResourceSelector.ResourceSubset"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%@page import="eu.essi_lab.messages.bond.BondFactory"%>
<%@page import="eu.essi_lab.model.resource.MetadataElement"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%
String viewId = request.getParameter("view");
if (viewId == null || viewId.isEmpty()) {
    out.println("Unexpected: view parameter missing");
    return;
}
String format = request.getParameter("format");
boolean csv = false;
if (format != null && format.equals("CSV")) {
    csv = true;
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");

}
if (!csv) {
	out.println("<html><head><title>Stations report</title><style></style></head><body>");
	out.println("<h1>" + viewId + " information per source</h1>");
}
String sourceId = request.getParameter("source");
if (sourceId == null || sourceId.isEmpty()) {

    Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
    List<GSSource> sources = ConfigurationWrapper.getViewSources(view.get());
    sources.sort(new Comparator<GSSource>() {
	public int compare(GSSource o1, GSSource o2) {
    return o1.getLabel().compareTo(o2.getLabel());
	}
    });

    for (GSSource source : sources) {

	out.println("<h2>" + source.getLabel() + "</h2>");

	out.println("<a href=\"stats.jsp?view=" + viewId + "&source=" + source.getUniqueIdentifier() + "\">Station list</a>");

    }

} else {

    String startString = request.getParameter("start");
    int start = 1;
    if (startString != null && !startString.isEmpty()) {
	start = Integer.parseInt(startString);
    }

    int size = 50;
    String sizeString = request.getParameter("size");
    if (sizeString != null && !sizeString.isEmpty()) {
	size = Integer.parseInt(sizeString);
    }

    ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
    IDiscoveryExecutor executor = loader.iterator().next();

    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
    discoveryMessage.setRequestId(UUID.randomUUID().toString());
    discoveryMessage.setPage(new Page(start, size));
    	    discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
    GSSource source = ConfigurationWrapper.getSource(sourceId);
    List<GSSource> sources = new ArrayList();
    sources.add(source);
    discoveryMessage.setSources(sources);
    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
    discoveryMessage.setDataBaseURI(uri);
    Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
    WebRequestTransformer.setView(view.get().getId(), ConfigurationWrapper.getStorageInfo(), discoveryMessage);
    discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
    ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
    List<GSResource> resources = resultSet.getResultsList();

    if (csv) {
	out.println("Monitoring point\tLatitude\tLongitude\tElevation");
    } else {
	out.println("<h1>" + source.getLabel() + " stations</h1>");
	out.println("<table>");
	out.println("<tr><th>Number</th><th>Monitoring point</th><th>Latitude</th><th>Longitude</th><th>Elevation</th></tr>");
    }
    int tmp = start;
    for (GSResource resource : resources) {
	String title = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle();
	GeographicBoundingBox bb = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	BigDecimal north = bb.getBigDecimalNorth();
	BigDecimal east = bb.getBigDecimalEast();
	String ele = "";
	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	VerticalExtent vertical = miMetadata.getDataIdentification().getVerticalExtent();
	if (vertical != null) {
    Double minimum = vertical.getMinimumValue();
    ele = "" + minimum;
	}

	if (csv) {
    out.println(title + "\t" + north + "\t" + east + "\t" + ele);
	} else {
    out.println(
		    "<tr><td>" + tmp++ + "</td><td>" + title + "</td><td>" + north + "</td><td>" + east + "</td><td>" + ele + "</td></tr>");
	}
    }
    if (!csv) {
	out.println("</table>");
	out.println("<p>Next records</p>");
	if (start > 1) {
    int prev = start - size;
    if (prev < 1) {
		prev = 1;
    }
    out.println("<a href=\"stats.jsp?view=" + viewId + "&source=" + source.getUniqueIdentifier() + "&start=" + (prev) + "\">"
		    + (prev) + "</a>");
	}
	out.println("<a href=\"stats.jsp?view=" + viewId + "&source=" + source.getUniqueIdentifier() + "&start=" + (start + size) + "\">"
		+ (start + size) + "</a>");

	out.println("<p>Records per page</p>");
	int[] sizes = new int[] { 10, 50, 100, 1000, 10000, 100000 };
	for (int i = 0; i < sizes.length; i++) {
    out.println("<a href=\"stats.jsp?view=" + viewId + "&source=" + source.getUniqueIdentifier() + "&start=" + start + "&size="
		    + sizes[i] + "\">" + sizes[i] + "</a>");
	}

    }
    
}

if (!csv){
    out.println("</body></html>");
}
out.flush();

%>
