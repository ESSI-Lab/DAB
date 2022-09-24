package eu.floraresearch.drm.report.impl.legacy;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.floraresearch.drm.report.impl.DefaultReport;

public class FedEOGranulesCounter extends DefaultReport {

    private static final String ENDPOINT = "https://fedeo.esa.int/opensearch/request/?";
    private static final int OFFSET = 50;
    private static final int MAX_COLLECTION_COUNTING_TRIES = 3;
    private static final long COLLECTION_COUNTING_RETRY_TIME = 5000;

    int totalGranulesCount;

    public FedEOGranulesCounter() {
    }

    public void computeGranules() throws Exception {

	int collections = getCollectionsCount();

	try {

	    GSLoggerFactory.getLogger(getClass()).info("FedEO granules computation STARTED");

	    int start = 1;
	    int totalCountedCollections = 0;

	    while (start <= collections) {

		GSLoggerFactory.getLogger(getClass()).info("Opening pages from [" + start + "/" + (start + OFFSET) + "] STARTED");

		int countedCollections = openPages(start);

		GSLoggerFactory.getLogger(getClass()).info("Opening pages from [" + start + "/" + (start + OFFSET) + "] ENDED");

		totalCountedCollections += countedCollections;

		GSLoggerFactory.getLogger(getClass()).info("Counted       [" + countedCollections + "]");
		GSLoggerFactory.getLogger(getClass()).info("Total counted [" + totalCountedCollections + "/" + collections + "]");

		start += OFFSET;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Total collections " + totalCountedCollections);
	    GSLoggerFactory.getLogger(getClass()).info("Total granules - " + totalGranulesCount);

	    GSLoggerFactory.getLogger(getClass()).info("FedEO granules computation ENDED");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

    }

    /**
     * @return
     */
    private int getTotalGranules() {

	return totalGranulesCount;
    }

    /**
     * @return
     * @throws Exception
     */
    private int getCollectionsCount() throws Exception {

	String getCollections = getCollectionsRequest(1);

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

    /**
     * @param start
     * @return
     * @throws Exception
     */
    private int openPages(int start) {

	int countedCollections = 0;

	String getCollections = getCollectionsRequest(start);

	GSLoggerFactory.getLogger(getClass()).info("Collections request STARTED");

	GSLoggerFactory.getLogger(getClass()).info("Collection request [" + getCollections + "]");

	Downloader request = createDownloader();

	Optional<InputStream> resp = request.downloadStream(getCollections);

//	if (!resp.isPresent()) {
//
//	    GSLoggerFactory.getLogger(getClass()).warn("Error occurred, next try after 10 seconds");
//
//	    try {
//		Thread.sleep(10000);
//	    } catch (InterruptedException e) {
//	    }
//
//	    resp = request.downloadStream(getCollections);
//	}

	if (!resp.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to open collection");
	    return 0;
	}

	GSLoggerFactory.getLogger(getClass()).info("Collections request ENDED");

	List<Node> collectionEntries = null;
	XMLDocumentReader doc = null;
	try {

	    doc = new XMLDocumentReader(resp.get());
	    doc.setNamespaceContext(new FEDEONameSpaceContext());

	    collectionEntries = doc.evaluateOriginalNodesList("//*[local-name()='entry']");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to parse request response");
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    return 0;
	}

	for (int i = 0; i < collectionEntries.size(); i++) {

	    try {

		GSLoggerFactory.getLogger(getClass()).info("Handling collection #" + i + " STARTED");

		Node collectionEntry = collectionEntries.get(i);

		String collectionDescDocLink = doc.evaluateString(collectionEntry,
			"*:link[@type='application/opensearchdescription+xml']/@href");

		GSLoggerFactory.getLogger(getClass()).info("Desc.doc. link [" + collectionDescDocLink + "]");

		if (Objects.nonNull(collectionDescDocLink) && collectionDescDocLink.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).warn("OpenSearch description document link missing");
		    GSLoggerFactory.getLogger(getClass()).warn("Skipping collection");

		    continue;
		}

		String collectionId = findCollectionId(collectionDescDocLink);

		GSLoggerFactory.getLogger(getClass()).info("Current collection id [" + collectionId + "]");

		if (Objects.nonNull(collectionId) && !collectionId.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).info("Counting collection granules STARTED");

		    int granules = countCollectionGranulesCount(collectionId);

		    GSLoggerFactory.getLogger(getClass()).info("Counting collection granules ENDED");

		    GSLoggerFactory.getLogger(getClass()).info("Collection granules [" + granules + "]");

		    if (granules > -1) {

			totalGranulesCount += granules;
			countedCollections++;
		    }

		    GSLoggerFactory.getLogger(getClass()).info("Total granules [" + totalGranulesCount + "]");
		}

		GSLoggerFactory.getLogger(getClass()).info("Handling collection #" + i + " ENDED");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return countedCollections;
    }

    /**
     * @param cid
     * @return
     * @throws Exception
     */
    private int countCollectionGranulesCount(String cid) throws Exception {

	String requestURL = "https://fedeo.esa.int/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=" + cid
		+ "&startRecord=1&maximumRecords=1";

	GSLoggerFactory.getLogger(getClass()).info("Counting collection request [" + requestURL + "]");

	Downloader downloader = createDownloader();

	int count = -1;
	int tries = MAX_COLLECTION_COUNTING_TRIES + 1;

	do {
	    try {

		InputStream inputStream = downloader.downloadStream(requestURL).get();

		XMLDocumentReader reader = new XMLDocumentReader(inputStream);

		try {

		    String docString = reader.evaluateString(".//*:totalResults/text()");
		    count = Integer.valueOf(docString);

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		    GSLoggerFactory.getLogger(getClass()).error("Error occurred while counting collection [" + reader.asString() + "]");

		    tries = MAX_COLLECTION_COUNTING_TRIES + 1;
		    break;
		}

		tries = MAX_COLLECTION_COUNTING_TRIES + 1;
		break;

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
//		GSLoggerFactory.getLogger(getClass()).info("New attempt in 5 seconds...");

		tries++;

//		Thread.sleep(COLLECTION_COUNTING_RETRY_TIME);
	    }
	} while (tries < MAX_COLLECTION_COUNTING_TRIES);

	return count;
    }

    /**
     * @param start
     * @return
     */
    private String getCollectionsRequest(int start) {

	String query = "httpAccept=application/atom%2Bxml&type=collection&startRecord=" + start + "&maximumRecords=" + OFFSET;

	if (ENDPOINT.endsWith("?")) {
	    query = ENDPOINT + query;
	} else {
	    query = ENDPOINT + "?" + query;
	}

	return query;
    }

    /**
     * @param collectionDescDocLink
     * @return
     */
    private String findCollectionId(String collectionDescDocLink) {

	String id = null;

	String[] split = collectionDescDocLink.split("&");

	for (int i = 0; i < split.length; i++) {

	    String s = split[i];

	    if (s.contains("parentIdentifier")) {

		id = s.replace("https://fedeo.esa.int/opensearch/description.xml?", "");
		id = id.replace("parentIdentifier=", "");
		try {
		    id = URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		break;
	    }
	}

	return id;
    }

    public static void main(String[] args) {

	try {
	    FedEOGranulesCounter fedEOGranulesCounter = new FedEOGranulesCounter();

	    fedEOGranulesCounter.computeGranules();

	    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");
	    System.out.println(fedEOGranulesCounter.getTotalGranules());
	    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
