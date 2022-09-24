package eu.floraresearch.drm.report.impl.legacy;
//package eu.floraresearch.drm.report;
//
//import java.io.InputStream;
//import java.util.List;
//import java.util.UUID;
//
//import org.w3c.dom.Node;
//
//import eu.floraresearch.lablib.net.GetRequest;
//import eu.floraresearch.lablib.net.Response;
//import eu.floraresearch.lablib.xml.XMLDocument;
//
//public class SatelliteReport extends DefaultReport {
//
//    private int granules;
//
//    public SatelliteReport() {
//	granules = -1;
//    }
//
//    @Override
//    public int getCategory_3_Value() throws Exception {
//
//	if (granules == -1) {
//	    String reqID = UUID.randomUUID().toString();
//
//	    String dabEndpoint = cleanDABEndpoint();
//
//	    String id = source.getUniqueIdentifier();
//
//	    String requestURL = createQuery(dabEndpoint, reqID, id, "");
//
//	    requestURL = requestURL.replace("&ct=1", "&ct=10");
//
//	    GetRequest request = new GetRequest(requestURL);
//
//	    Response resp = request.execRequest();
//
//	    InputStream is = resp.getResponseBodyAsStream();
//
//	    XMLDocument doc = new XMLDocument(is);
//
//	    List<Node> entries = doc.evaluateXPath("//*[local-name()='entry']").asNodesList();
//
//	    for (Node n : entries) {
//
//		String cid = new XMLDocument(n).evaluateXPath("//*[local-name()='id']").asString();
//
//		requestURL = dabEndpoint + "services/opensearch?&reqID=" + reqID + "&parents=" + cid
//			+ "&ct=1&outputFormat=application%2Fatom%2Bxml";
//
//		request = new GetRequest(requestURL);
//
//		granules += Integer.valueOf(new XMLDocument(request.execRequest().getResponseBodyAsStream())
//			.evaluateXPath("//*[local-name()='totalResults']").asString());
//
//	    }
//	}
//	return granules == -1 ? 0 : granules;
//
//    }
//
//    @Override
//    public int getCategory_4_Value() throws Exception {
//
//	String reqID = UUID.randomUUID().toString();
//
//	String dabEndpoint = cleanDABEndpoint();
//
//	String id = source.getId();
//
//	String requestURL = createQuery(dabEndpoint, reqID, id, "&gdc=true");
//
//	requestURL = requestURL.replace("&ct=1", "&ct=10");
//
//	GetRequest request = new GetRequest(requestURL);
//
//	Response resp = request.execRequest();
//
//	InputStream is = resp.getResponseBodyAsStream();
//
//	XMLDocument doc = new XMLDocument(is);
//
//	List<Node> entries = doc.evaluateXPath("//*[local-name()='entry']").asNodesList();
//
//	if (entries.size() == 0)
//	    return 0;
//
//	if (granules == -1) {
//
//	    for (Node n : entries) {
//
//		String cid = new XMLDocument(n).evaluateXPath("//*[local-name()='id']").asString();
//
//		requestURL = dabEndpoint + "services/opensearch?&reqID=" + reqID + "&parents=" + cid
//			+ "&ct=1&outputFormat=application%2Fatom%2Bxml";
//
//		request = new GetRequest(requestURL);
//
//		granules += Integer.valueOf(new XMLDocument(request.execRequest().getResponseBodyAsStream())
//			.evaluateXPath("//*[local-name()='totalResults']").asString());
//
//	    }
//	}
//
//	return granules == -1 ? 0 : granules;
//    }
//
//    @Override
//    public String getComments() {
//
//	return "The number of granules is obtained by issuing a request with no constraint to each collection, and summing the total number of records";
//    }
//
//}
