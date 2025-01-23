package eu.essi_lab.tool;

import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.accessor.eurobis.EurOBISClient;
import eu.essi_lab.gssrv.conf.task.bluecloud.DocumentReport;
import eu.essi_lab.gssrv.conf.task.bluecloud.BlueCloudMetadataElement;
import eu.essi_lab.gssrv.conf.task.bluecloud.MetadataManager;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class EurOBISClientExternalTestIT {
    @Test
    public void test() throws Exception {
	String endpoint = "http://ipt.vliz.be/eurobis/dcat";
	EurOBISClient client = new EurOBISClient(endpoint);
	System.out.println(client.getSize());
	int emlErrors = 0;
	int downloadErrors = 0;
	Map<String, Integer> contents = new java.util.HashMap<>();
	int size = client.getSize();
	// size = 10;
	for (int i = 0; i < size; i++) {
	    XMLDocumentReader metadata = client.getMetadata(i);

	    String metadataURL = client.getMetadataURL(i);
	    Node[] nodes = metadata.evaluateNodes("/*:eml/*:dataset");
	    Node[] children = metadata.evaluateNodes("//*:online/*:url");
	    String id = "";
	    for (Node child : children) {
		id = metadata.evaluateString(child, ".");
		if (id.contains("?dasid=")) {
		    id = id.substring(id.indexOf("?dasid="));
		    id = id.replace("?dasid=", "");
		    System.out.println(id);
		    String imis = "http://www.vliz.be/imis?dasid=" + id + "&show=json";
		    Downloader dw = new Downloader();
		    Optional<String> str = dw.downloadOptionalString(imis);
		    if (str.isPresent()) {
			String s = str.get();
			try {

			    JSONObject obj = new JSONObject(s);
			    if (obj.has("meastypes") && !obj.isNull("meastypes")) {
				JSONArray measArray = obj.getJSONArray("meastypes");
				for (int j = 0; j < measArray.length(); j++) {
				    JSONObject jo = measArray.getJSONObject(j);
				    if (jo.has("Instrument") && !jo.isNull("Instrument")) {
					String instr = jo.get("Instrument").toString();
					if (instr != null && !instr.equals("")) {
					    System.out.println("DASID " + id + " metadata " + metadataURL);
					}
				    }
				}
			    }
			} catch (Exception e) {
			    System.out.println("parsing " + s);
			    e.printStackTrace();
			}
		    }

		}
	    }

	    if (nodes == null || nodes.length == 0) {
		System.out.println("Error: NOT EML at: " + metadataURL);
		emlErrors++;
		continue;
	    }

	    DocumentReport bcm = new DocumentReport(metadata);
	    bcm.addMetadata(BlueCloudMetadataElement.IDENTIFIER, id);
	    bcm.addMetadata(BlueCloudMetadataElement.TITLE, metadata.evaluateString("/*:eml/*:dataset/*:title"));
	    bcm.addMetadata(BlueCloudMetadataElement.KEYWORD, metadata.evaluateString("/*:eml/*:dataset/*:keywordSet/*:keyword"));
	    String desc = metadata.evaluateString("/*:eml/*:dataset/*:coverage/*:geographicCoverage/*:geographicDescription");
	    if (desc != null) {
		if (!desc.contains("http://marineregions.org/mrgid")) {
		    desc = null;
		}
	    }
	    // bcm.addMetadata(BlueCloudElement.GEOGRAPHIC_DESCRIPTION, desc);
	    bcm.addMetadata(BlueCloudMetadataElement.BOUNDING_BOX, metadata
		    .evaluateString("/*:eml/*:dataset/*:coverage/*:geographicCoverage/*:boundingCoordinates/*:westBoundingCoordinate"));
	    bcm.addMetadata(BlueCloudMetadataElement.TEMPORAL_EXTENT,
		    metadata.evaluateString("/*:eml/*:dataset/*:coverage/*:temporalCoverage/*:rangeOfDates/*:beginDate/*:calendarDate"));
	    // bcm.addMetadata(BlueCloudElement.PARAMETER, metadata.evaluateString("/*:eml/*:dataset"));
	    // bcm.addMetadata(BlueCloudElement.PLATFORM, metadata.evaluateString("/*:eml/*:dataset/"));
	    bcm.addMetadata(BlueCloudMetadataElement.ORGANIZATION, metadata.evaluateString("/*:eml/*:dataset/*:creator/*:organizationName"));

	    MetadataManager.getInstance().addMetadata(metadataURL, bcm);

	    String url = client.getDownloadURL(i);

	    Downloader executor = new Downloader();

	    HttpRequest getRequest = HttpRequestUtils.build(MethodNoBody.GET, url);

	    HttpResponse<InputStream> response = executor.downloadResponse(getRequest);

	    int statusCode = response.statusCode();

	    if (statusCode != 200) {
		System.out.println("Error: status code " + statusCode + " for " + url);
		downloadErrors++;
	    }

	    HttpHeaders responseHeaders = response.headers();

	    Map<String, List<String>> map = responseHeaders.map();

	    for (String header : map.keySet()) {
		if (header.contains("Content-Type")) {
		    String content = map.get(header).get(0);
		    Integer v = contents.get(content);
		    if (v == null) {
			v = 1;
		    } else {
			v++;
		    }
		    contents.put(content, v);
		}
	    }
	}

	MetadataManager.getInstance().printStatistics();

	System.out.println("Total EML erros: " + emlErrors);
	System.out.println("Total download erros: " + downloadErrors);
	Set<Entry<String, Integer>> entries = contents.entrySet();
	System.out.println("Content statistics:");
	for (Entry<String, Integer> entry : entries) {
	    System.out.println(entry.getKey() + ": " + entry.getValue() + " datasets");
	}

    }
}
