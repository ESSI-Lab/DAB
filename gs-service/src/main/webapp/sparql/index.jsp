<%@page import="software.amazon.awssdk.services.s3.model.HeadObjectResponse"%>
<%@page import="software.amazon.awssdk.services.s3.model.ListObjectsV2Request"%>
<%@page import="eu.essi_lab.lib.net.s3.S3TransferWrapper"%>
<%@page import="java.util.Comparator"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>

<%!

public String getInfo(S3TransferWrapper wrapper, String key){
    HeadObjectResponse metadata = wrapper.getObjectMetadata("dataset.geodab.eu", key);
	return "Size: "+metadata.contentLength()+" Date: "+metadata.lastModified();
}

%>
<%
String viewId = request.getParameter("view");
if (viewId == null) {

    out.println("<html><head></head><body>");
    out.println("A view parameter should be specified");
    out.println("</body></html>");
    return;
}

out.println("<html><head></head><body>");

Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);

List<GSSource> sources = ConfigurationWrapper.getViewSources(view.get());

sources.sort(new Comparator<GSSource>() {
    public int compare(GSSource o1, GSSource o2) {
	return o1.getLabel().compareTo(o2.getLabel());
    }
});

Optional<S3TransferWrapper> optionalWrapper = ConfigurationWrapper.getS3TransferManager();

S3TransferWrapper wrapper = optionalWrapper.get();



for (GSSource source : sources) {
    out.println("<h2>" + source.getLabel() + "</h2>");
    String id = source.getUniqueIdentifier();
    String objectKey = id + "/" + id + ".ttl";
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + objectKey+"'>Complete set (TTLs)</a> "+getInfo(wrapper, objectKey)+"<br/>\n");
    objectKey = id + "/" + id + "-valid.ttl";
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + objectKey + "'>Valid set (TTLs)</a> "+getInfo(wrapper, objectKey)+"<br/>\n");
    objectKey = id + "/" + id + "-report.txt";
    out.println("<a href='https://s3.us-east-1.amazonaws.com/dataset.geodab.eu/dataset/" + objectKey + "'>Validity report</a> "+getInfo(wrapper, objectKey)+"<br/>\n");
}

out.println("</body></html>");
%>