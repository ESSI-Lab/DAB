package eu.essi_lab.profiler.csw.test.harvester;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CswGetRecordsClient {

    private static final String CSW_ENDPOINT =
	    "http://localhost:9090/gs-service/services/essi/view/argo-stac/harvestingcsw";

    private static final int MAX_RECORDS = 20;

    public static void main(String[] args) throws Exception {

	int startPosition = 1;
	int totalMatched = Integer.MAX_VALUE;

	while (startPosition <= totalMatched) {

	    System.out.println("Requesting records starting at: " + startPosition);

	    String requestXml = buildRequest(startPosition, MAX_RECORDS);
	    Document response = sendRequest(requestXml);

	    Element searchResults = (Element)
		    response.getElementsByTagNameNS(
			    "http://www.opengis.net/cat/csw/2.0.2",
			    "SearchResults"
		    ).item(0);

	    int matched = Integer.parseInt(
		    searchResults.getAttribute("numberOfRecordsMatched")
	    );
	    int returned = Integer.parseInt(
		    searchResults.getAttribute("numberOfRecordsReturned")
	    );

	    totalMatched = matched;

	    // ---- Process records here ----
	    System.out.println("Returned: " + returned + " / Matched: " + matched);

	    if (returned == 0) {
		break;
	    }

	    startPosition += returned;
	}

	System.out.println("All records retrieved.");
    }

    private static String buildRequest(int startPosition, int maxRecords) {
	return "<?xml version='1.0' encoding='UTF-8'?>"
		+ "<csw:GetRecords "
		+ "xmlns:csw='http://www.opengis.net/cat/csw/2.0.2' "
		+ "xmlns:ogc='http://www.opengis.net/ogc' "
		+ "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
		+ "service='CSW' version='2.0.2' "
		+ "resultType='results' "
		+ "outputFormat='application/xml' "
		+ "outputSchema='http://www.isotc211.org/2005/gmd' "
		+ "startPosition='" + startPosition + "' "
		+ "maxRecords='" + maxRecords + "' "
		+ "xsi:schemaLocation='http://www.opengis.net/cat/csw/2.0.2 "
		+ "http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd'>"
		+ "<csw:Query typeNames='csw:Record'>"
		+ "<csw:ElementSetName>brief</csw:ElementSetName>"
		+ "</csw:Query>"
		+ "</csw:GetRecords>";
    }

    private static Document sendRequest(String xml) throws Exception {

	URL url = new URL(CSW_ENDPOINT);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	conn.setRequestMethod("POST");
	conn.setRequestProperty("Content-Type", "application/xml");
	conn.setDoOutput(true);

	try (OutputStream os = conn.getOutputStream()) {
	    os.write(xml.getBytes(StandardCharsets.UTF_8));
	}

	if (conn.getResponseCode() != 200) {
	    throw new RuntimeException(
		    "HTTP error: " + conn.getResponseCode()
	    );
	}

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);

	DocumentBuilder db = dbf.newDocumentBuilder();
	return db.parse(conn.getInputStream());
    }
}
