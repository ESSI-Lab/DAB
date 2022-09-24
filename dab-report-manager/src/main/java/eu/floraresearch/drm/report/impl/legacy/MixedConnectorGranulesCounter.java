package eu.floraresearch.drm.report.impl.legacy;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.jena.base.Sys;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.floraresearch.drm.ConfigReader;
import eu.floraresearch.drm.report.impl.DefaultReport;

/**
 * This tool counts the number or granules of mixed connectors, having harvested collections in the first level and
 * distributed granules at second level
 * 
 * @author Fabrizio
 */
public class MixedConnectorGranulesCounter extends DefaultReport {

    /**
     * @param sourceId
     * @return
     * @throws Exception
     */
    public int computeGranules(String sourceId) throws Exception {

	String reqID = UUID.randomUUID().toString();

	int granules = 0;
	int step = 50;

	String dabEndpoint = ConfigReader.getInstance().readDABEndpoint();

	int collectionsCount = getCollectionsCount(sourceId);

	System.out.println("********************************");
	System.out.println("Collections count [" + collectionsCount + "]");
	System.out.println("********************************");

	for (int i = 0; i <= collectionsCount; i += step) {

	    try {

		System.out.println("********************************");
		System.out.println("Current collections [" + (i + 1) + "/" + collectionsCount + "]");
		System.out.println("********************************");

		String requestURL = createQuery(dabEndpoint, reqID, sourceId, "");
		requestURL = requestURL.replace("&ct=1", "&ct=" + step);
		requestURL += "&si=" + (i + 1);

		System.out.println("Outer query: " + requestURL);
		System.out.println("\n----------------------------------------------\n");

		Downloader downloader = createDownloader();
		InputStream is = downloader.downloadStream(requestURL).get();

		XMLDocumentReader doc = new XMLDocumentReader(is);

		List<Node> entries = doc.evaluateOriginalNodesList("//*[local-name()='entry']");

		for (Node n : entries) {

		    String cid = doc.evaluateString(n, "./*:id/text()");

		    System.out.println("Current CID: " + cid);

		    requestURL = dabEndpoint + "services/opensearch?&reqID=" + reqID + "&parents=" + cid
			    + "&ct=1&outputFormat=application%2Fatom%2Bxml";

		    downloader = createDownloader();

		    String docString = null;
		    int attempts = 0;
		    do {
			try {

			    System.out.println("Inner query: " + requestURL);

			    InputStream inputStream = downloader.downloadStream(requestURL).get();

			    docString = new XMLDocumentReader(inputStream).evaluateString(".//*:totalResults/text()");

			    attempts = 5;
			    break;

			} catch (Exception ex) {
			    System.out.println("New attempt in 5 seconds...");
			    attempts++;
			    Thread.sleep(5000);
			}
		    } while (attempts < 3);

		    int currentGranules = Integer.valueOf(docString);
		    System.out.println("Current granules: " + currentGranules);

		    granules += Integer.valueOf(docString);
		    System.out.println("Total granules: " + granules);

		    System.out.println("\n----------------------------------------------\n");
		}
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	    }
	}

	return granules;
    }

    /**
     * @return
     * @throws Exception
     */
    private int getCollectionsCount(String sourceId) throws Exception {
    
        String dabEndpoint = ConfigReader.getInstance().readDABEndpoint();
    
        String reqID = UUID.randomUUID().toString();
    
        String getCollections = createQuery(dabEndpoint, reqID, sourceId, "");
        getCollections = getCollections.replace("&ct=1", "&ct=1");
        getCollections += "&si=1";
    
        GSLoggerFactory.getLogger(getClass()).info("Collections counting STARTED");
    
        Downloader request = createDownloader();
    
        InputStream resp = request.downloadStream(getCollections).get();
        XMLDocumentReader doc = new XMLDocumentReader(resp);
        doc.setNamespaceContext(new FEDEONameSpaceContext());
    
        List<Node> totres = doc.evaluateOriginalNodesList("//os:totalResults");
    
        String totString = totres.get(0).getTextContent();
    
        GSLoggerFactory.getLogger(getClass()).info("Collections counting ENDED");
        GSLoggerFactory.getLogger(getClass()).info("Found " + totString + " collections");
    
        return Integer.valueOf(totString);
    }

    public static void main(String[] args) {

	try {
	    int granules = new MixedConnectorGranulesCounter().computeGranules("inpesat");
	    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");
	    System.out.println(granules);
	    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
