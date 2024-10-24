<%@page import="eu.essi_lab.lib.utils.ISO8601DateTimeUtils"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.pdk.BouncerTool"%>
<%@page import="eu.essi_lab.pdk.BouncerRequest"%>
<%@page import="eu.essi_lab.pdk.RedisTool"%>
<html>
<head>
<title>Request monitor</title>
<style>
</style>
</head>
<body>
	<%!public String print(BouncerTool bouncer, String hash) {
	List<String> executingRequests = bouncer.getExecutingRequests(hash);
	List<String> requests = bouncer.getRequests(hash);

	String ret = "";
	ret += "<p>" + hash + " requests</p>";
	ret += "<ul>";
	requests.sort(null);
	for (String req : requests) {
	    String execution = "";
	    String bs = "";
	    String be = "";
	    if (executingRequests.contains(req)) {
// 		BouncerRequest r = bouncer.getRequest(hash, req);
		bs = "<b>";
		be = "</b>";
		String minutes = "unknown";
		
		String dateString = bouncer.getRequestDatestamp(hash,req);
		if (dateString!=null){
		Date date = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();
		if (date != null) {
		    minutes = "" + (System.currentTimeMillis() - date.getTime()) / TimeUnit.MINUTES.toMillis(1);
		}		
		}
		String ip = bouncer.getRequestIp(hash,req);
		String h = bouncer.getRequestHostname(hash,req);
		execution = " (in execution from " + minutes + " minutes, ip: "+ip+", on host: "+h+")";
		executingRequests.remove(req);
	    }	    
	    ret += "<li>" + bs + req + execution + be + "</li>";
	}
	for(String req:executingRequests){
	    String bs = "<b>";
	    String be = "</b>";
		String minutes = "unknown";
		
		String dateString = bouncer.getRequestDatestamp(hash,req);
		if (dateString!=null){
		Date date = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();
		if (date != null) {
		    minutes = "" + (System.currentTimeMillis() - date.getTime()) / TimeUnit.MINUTES.toMillis(1);
		}		
		}
		String ip = bouncer.getRequestIp(hash,req);
		String h = bouncer.getRequestHostname(hash,req);
		String execution = " TO CHECK, NOT IN REQUESTS (in execution from " + minutes + " minutes, ip: "+ip+", on host: "+h+")";
	    ret += "<li>" + bs + req + execution + be + "</li>";

	}
	ret += "</ul>";
	return ret;
    }%>
	<%
	RedisTool tool = new RedisTool("essi-lab.eu", 6379);
	BouncerTool bouncer = new BouncerTool(tool);

	Set<String> hosts = bouncer.getHosts();
	out.println("<p>Active hosts</p>");
	out.println("<ul>");
	List<String> sortedHosts = new ArrayList(hosts);
	sortedHosts.sort(null);
	for (String host : sortedHosts) {
	    Date date = bouncer.getHostInformation(host);
	    String info;
	    if (date == null) {
		info = "unknown";
	    } else {
		long gap = System.currentTimeMillis() - date.getTime();
		long minutes = gap / TimeUnit.MINUTES.toMillis(1);
		info = "last ping " + minutes + " minutes ago "+date;
	    }
	    out.println("<li>" + host + " (" + info + ")</li>");
	}
	out.println("</ul>");

	out.println(print(bouncer, "{prod-frontend}"));
	
	out.println(print(bouncer, "{prod-access}"));

	out.println(print(bouncer, "{prod-intensive}"));
	
	

	bouncer.close();
	%>
</body>
</html>