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

	String sourceId = request.getParameter("source");
	String viewId = request.getParameter("view");

	if (sourceId == null || sourceId.trim().isEmpty()) {
	    out.println("A source parameter is needed here");
	}else if (viewId == null || viewId.trim().isEmpty()) {
	    out.println("A view parameter is needed here");
	}else{

	   
			AvailabilityMonitor monitor = AvailabilityMonitor.getInstance();

			DownloadInformation goodInfo = monitor.getLastDownloadInformation(sourceId);
			DownloadInformation badInfo = monitor.getLastFailedDownloadInformation(sourceId);
			Date goodDate = goodInfo ==  null?null:goodInfo.getDate();
			Date badDate = badInfo ==  null?null:badInfo.getDate();
			String goodStation = goodInfo ==  null?null:goodInfo.getPlatformId();
			String badStation = badInfo ==  null?null:badInfo.getPlatformId();
			out.println("<p>Good test</p>");
			out.println("<ul>");
			out.println("<li>Date: "+goodDate+"</li>");
			out.println("<li>Platform: <a target='_blank' href='https://gs-service-production.geodab.eu/gs-service/services/view/"+viewId+"/bnhs/station/"+goodStation+"/  '>"+goodStation+"</a></li>");
			out.println("</ul>");
			
			out.println("<p>Bad test</p>");
			out.println("<ul>");
			out.println("<li>Date: "+badDate+"</li>");
			out.println("<li>Platform: <a target='_blank' href='https://gs-service-production.geodab.eu/gs-service/services/view/"+viewId+"/bnhs/station/"+badStation+"/  '>"+badStation+"</a></li>");
			out.println("</ul>");
		
		out.println("<table>");
		out.println("</table>");
	}

	
	%>
</body>
</html>