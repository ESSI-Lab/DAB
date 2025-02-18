<%@page import="tech.units.indriya.AbstractSystemOfUnits"%>
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
<title>Sources</title>
<style>
</style>
</head>
<body>
	<%

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
	    out.println("<h1>" + viewId.toUpperCase() + " sources</h1>");
	    out.println("<h2>Sources</h2>");

	    resources.sort(new Comparator<GSResource>() {
		public int compare(GSResource o1, GSResource o2) {
	    return o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
		}
	    });

	    out.println("<ul>");
	    for (GSResource resource : resources) {
		GSSource source = resource.getSource();
		String sourceId = source.getUniqueIdentifier();
		String sourceLabel = source.getLabel();
		out.println("<li><p>" + sourceId + "</p><p>" + sourceLabel + "</p></li>");
	    }
	    out.println("</ul>");

	    int i = 0;
	    out.println("List<Source>sources = new ArrayList();");
	    for (GSResource resource : resources) {
		GSSource source = resource.getSource();
		String sourceId = source.getUniqueIdentifier();
		String sourceLabel = source.getLabel();
		out.println("sources.add(new Source(\"" + sourceId + "\", \"" + sourceLabel + "\"));");
		i++;
	    }
	    out.println("addDeployment(\"" + viewId + "\", sources);");

	} else {
	    out.println("A view parameter is needed here");
	}
	%>
</body>
</html>