package eu.essi_lab.profiler.csw.test.harvester;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.*;

public class CswGetRecordsClient {

    private static final String CSW_ENDPOINT = "http://localhost:9090/gs-service/services/essi/view/elixir-ena/harvestingcsw";
    // private static final String CSW_ENDPOINT = "http://blue-cloud.geodab.eu/gs-service/services/essi/view/elixir-ena/harvestingcsw";
   // private static final String CSW_ENDPOINT = "http://54.175.100.77:33106/gs-service/services/essi/view/elixir-ena/harvestingcsw";

    private static final int MAX_RECORDS = 20;

    public static void main(String[] args) throws Exception {

	int loops = 0;
	while(true){
	    loops++;
	    gatherAll(loops);
	}

    }

    private static void gatherAll(int loop) throws Exception {
	int startPosition = 1;
	int totalMatched = Integer.MAX_VALUE;

	int record = 0;

	while (startPosition <= totalMatched) {

	    System.out.println(new Date()+ " Sending GET RECORDS at "+startPosition+ " (loop "+loop+")");

	    Document response = sendPost(buildGetRecordsRequest(startPosition, MAX_RECORDS));

	    Element searchResults = (Element) response.getElementsByTagNameNS("http://www.opengis.net/cat/csw/2.0.2", "SearchResults")
		    .item(0);

	    int matched = Integer.parseInt(searchResults.getAttribute("numberOfRecordsMatched"));
	    int returned = Integer.parseInt(searchResults.getAttribute("numberOfRecordsReturned"));

	    totalMatched = matched;

	    List<String> identifiers = extractIdentifiers(response);

	    for (String id : identifiers) {
		record++;
		Document fullRecord = getRecordById(id);
		// ---- process full metadata here ----
		System.out.println(record+" Retrieved FULL record for id: " + id);
	    }

	    if (returned == 0) {
		break;
	    }

	    startPosition += returned;
	}

	System.out.println("All records processed.");
    }

    // ---------------------------------------------------------------------
    // GetRecords
    // ---------------------------------------------------------------------

    private static String buildGetRecordsRequest(int start, int max) {
	return "<?xml version='1.0' encoding='UTF-8'?>" + "<csw:GetRecords " + "xmlns:csw='http://www.opengis.net/cat/csw/2.0.2' " + "xmlns:ogc='http://www.opengis.net/ogc' " + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " + "service='CSW' version='2.0.2' " + "resultType='results' " + "outputFormat='application/xml' " + "outputSchema='http://www.isotc211.org/2005/gmd' " + "startPosition='" + start + "' " + "maxRecords='" + max + "' " + "xsi:schemaLocation='http://www.opengis.net/cat/csw/2.0.2 " + "http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd'>" + "<csw:Query typeNames='csw:Record'>" + "<csw:ElementSetName>brief</csw:ElementSetName>" + "</csw:Query>" + "</csw:GetRecords>";
    }

    // ---------------------------------------------------------------------
    // Identifier extraction
    // ---------------------------------------------------------------------

    private static List<String> extractIdentifiers(Document doc) {

	List<String> ids = new ArrayList<>();

	// ISO 19115 fileIdentifier
	NodeList nodes = doc.getElementsByTagNameNS("http://www.isotc211.org/2005/gmd", "fileIdentifier");

	for (int i = 0; i < nodes.getLength(); i++) {
	    Element el = (Element) nodes.item(i);
	    String id = el.getTextContent().trim();
	    if (!id.isEmpty()) {
		ids.add(id);
	    }
	}

	return ids;
    }

    // ---------------------------------------------------------------------
    // GetRecordById
    // ---------------------------------------------------------------------

    private static Document getRecordById(String id) throws Exception {

	String query = CSW_ENDPOINT + "?service=CSW" + "&version=2.0.2" + "&request=GetRecordById" + "&elementSetName=full" + "&outputSchema=" + URLEncoder.encode(
		"https://www.blue-cloud.org/", "UTF-8") + "&id=" + URLEncoder.encode(id, "UTF-8");

	System.out.println(new Date()+ " Sending GET request to URL : " + query);
	URL url = new URL(query);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setRequestMethod("GET");

	if (conn.getResponseCode() != 200) {
	    System.err.println("HTTP error: " + conn.getResponseCode());
	    System.err.println("HTTP error message: " + conn.getResponseMessage());
	    InputStream stream = conn.getErrorStream();
	    IOUtils.copy(stream,System.err);
	    throw new RuntimeException("GetRecordById failed for id=" + id);
	}

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);

	DocumentBuilder db = dbf.newDocumentBuilder();
	return db.parse(conn.getInputStream());
    }

    // ---------------------------------------------------------------------
    // HTTP POST helper
    // ---------------------------------------------------------------------

    private static Document sendPost(String xml) throws Exception {
	System.out.println("Sending POST request to URL : " + CSW_ENDPOINT);
	System.out.println("POST body : " + xml);
	URL url = new URL(CSW_ENDPOINT);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	conn.setRequestMethod("POST");
	conn.setRequestProperty("Content-Type", "application/xml");
	conn.setDoOutput(true);

	try (OutputStream os = conn.getOutputStream()) {
	    os.write(xml.getBytes(StandardCharsets.UTF_8));
	}

	if (conn.getResponseCode() != 200) {
	    System.err.println("HTTP error: " + conn.getResponseCode());
	    System.err.println("HTTP error message: " + conn.getResponseMessage());
	    InputStream stream = conn.getErrorStream();
	    IOUtils.copy(stream,System.err);
	    throw new RuntimeException("HTTP error: " + conn.getResponseCode());
	}

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);

	DocumentBuilder db = dbf.newDocumentBuilder();
	return db.parse(conn.getInputStream());
    }

}