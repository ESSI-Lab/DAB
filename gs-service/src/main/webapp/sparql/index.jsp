<%@page import="java.util.Comparator"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%
String viewId = request.getParameter("view");
if (viewId == null) {

    out.println("<html><head></head><body>");
    out.println("A view parameter should be specified");
    out.println("</body></html>");
}

out.println("<html><head></head><body>");

Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);

List<GSSource> sources = ConfigurationWrapper.getViewSources(view.get());

sources.sort(new Comparator<GSSource>() {
    public int compare(GSSource o1, GSSource o2) {
	return o1.getLabel().compareTo(o2.getLabel());
    }
});

for (GSSource source : sources) {
    out.println("<h2>" + source.getLabel() + "</h2>");
    String id = source.getUniqueIdentifier();
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + id + "/" + id + ".ttl'>Complete set (TTLs)</a><br/>\n");
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + id + "/" + id + "-valid.ttl'>Valid set (TTLs)</a><br/>\n");
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + id + "/" + id + "-report.txt'>Validity report</a><br/>\n");
}

out.println("</body></html>");
%>