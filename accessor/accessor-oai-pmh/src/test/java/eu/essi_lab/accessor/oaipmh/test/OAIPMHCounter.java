package eu.essi_lab.accessor.oaipmh.test;

import java.io.InputStream;

import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class OAIPMHCounter {

    public OAIPMHCounter(String endpoint) throws Exception {
	Downloader d = new Downloader();
	String resumptionToken = null;
	int count = 0;
	do {
	    String url = endpoint;
	    if (resumptionToken != null) {
		url = url + "&resumptionToken=" + resumptionToken;
	    }
	    InputStream stream = d.downloadOptionalStream(url).get();
	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    count += reader.evaluateNumber("count(//*:metadata)").intValue();
	    
	    Node[] idNodes = reader.evaluateNodes("//*:Entry_ID");
	    for (Node node : idNodes) {
		String id = reader.evaluateString(node, ".");
		System.out.println(id);
	    }
	    resumptionToken = reader.evaluateString("//*:resumptionToken");
	    System.out.println("Counting (temp): " + count + " at: " + resumptionToken);
	} while (resumptionToken != null);
	System.out.println("Count: " + count);

    }

    public static void main(String[] args) throws Exception {
	new OAIPMHCounter("https://bluecloud-sios.csw.met.no/csw.py?mode=oaipmh&verb=ListRecords&metadataPrefix=dif");
    }

}
