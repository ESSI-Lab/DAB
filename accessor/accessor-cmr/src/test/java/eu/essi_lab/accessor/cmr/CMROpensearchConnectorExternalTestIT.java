package eu.essi_lab.accessor.cmr;

import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.accessor.cmr.cwic.distributed.CWICGranulesTemplate;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchCollectionMapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class CMROpensearchConnectorExternalTestIT {

    private String BASE_URL = "https://cmr.earthdata.nasa.gov/opensearch/collections.atom?clientId=gs-service&";

    private Downloader downloader = new Downloader();

    private static final String TAGKEY_XPATH = "//*:tagKey/text()";
    private static final String IDENTIFIER_XPATH = "//*:identifier";
    private static final String DATACENTER_XPATH = "//*:dataCenter";
    private static final String ARCHIVECENTER_XPATH = "//*:archiveCenter";
    private static final String SHORT_NAME_XPATH = "//*:shortName";
    private static final String VERSION_XPATH = "//*:versionId";

    public final static String CWIC_BASE_TEMPLATE_URL = "https://cwic.wgiss.ceos.org/opensearch/granules.atom?";
    public final static String IDN_GRANULES_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?";
    public final static String IDN_COLLECTION_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/collections.atom?";
    public final static String IDN_OSDD_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules/descriptor_document.xml?";

    public int cwicCount = 0;
    public int childcount = 0;
    public int rightChildCount = 0;
    public boolean isCwic = false;
    public int errorId = 0;

    @Test
    public void testCWICCollectionNumber() {

	int startPosition = 1;

	int pageSize = 50;

	try {
	    String getRequestURLCount = BASE_URL + "numberOfResults=1";

	    String result = downloader.downloadOptionalString(getRequestURLCount).orElse(null);

	    XMLDocumentReader reader = new XMLDocumentReader(result);
	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    String count = reader.evaluateNode("//*:totalResults").getTextContent();

	    int totalRecords = 0;
	    startPosition = startPosition - 1;
	    int maximumIteraction = 20;
	    while (startPosition < Integer.parseInt(count) && startPosition < maximumIteraction) {
		String getRequestURL = BASE_URL + "offset=" + startPosition + "&numberOfResults=" + pageSize;

		String request = downloader.downloadOptionalString(getRequestURL).orElse(null);
		XMLDocumentReader responseDocument = new XMLDocumentReader(request);
		responseDocument.setNamespaceContext(new CommonNameSpaceContext());

		Node[] entries = responseDocument.evaluateNodes("//*:entry");
		int countRecords = 0;
		for (Node entry : entries) {
		    XMLDocumentReader r = new XMLDocumentReader(XMLDocumentReader.asString(entry));
		    r.setNamespaceContext(new CommonNameSpaceContext());

		    Optional<List<String>> tags = readListString(r, TAGKEY_XPATH);

		    tags.ifPresent(t -> {
			int j = 0;
			for (String s : t) {

			    if (s.toLowerCase().contains("org.ceos.wgiss.cwic")) {
				j++;
				isCwic = true;
				break;
			    }
			}
			cwicCount = cwicCount + j;
		    });

		    Optional<String> identifier = read(r, IDENTIFIER_XPATH);

		    Optional<String> shortName = read(r, SHORT_NAME_XPATH);
		    Optional<String> dataCenter = read(r, DATACENTER_XPATH);
		    Optional<String> version = read(r, VERSION_XPATH);

		    if (dataCenter.isPresent() && !isCwic) {

			String osddURL = IDN_OSDD_TEMPLATE_URL + "dataCenter=" + dataCenter.get();

			if (shortName.isPresent()) {
			    osddURL = osddURL + "&shortName=" + shortName.get();
			}

			osddURL = osddURL + "&clientId=gs-service";

			try {
			    String osddResult = downloader.downloadOptionalString(osddURL).orElse(null);

			    // if (osddResult.contains("INVALID_DATASET - Unrecognized dataset")) {
			    // errorId++;
			    // }

			    XMLDocumentReader rr = new XMLDocumentReader(osddResult);

			    Node urlEntry = rr.evaluateNode("//*:Url[1]");
			    String templateURL = null;

			    if (urlEntry != null) {

				Node item = urlEntry.getAttributes().getNamedItem("template");

				templateURL = item.getTextContent();

				GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).info("TEMPLATE URL {} ", templateURL);

				CWICGranulesTemplate template = new CWICGranulesTemplate(templateURL);

				template.setDatasetId(identifier.get());
				template.setStart(1);
				template.setCount(1);

				GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).info("REQUEST TEMPLATE {} ",
					template.getRequestURL());

				String res1 = downloader.downloadOptionalString(template.getRequestURL()).orElse(null);

				XMLDocumentReader rres1 = new XMLDocumentReader(res1);

				String countResString1 = rres1.evaluateNode("//*:totalResults").getTextContent();

				if (Integer.parseInt(countResString1) > 0) {
				    childcount++;
				    GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class)
					    .info("TEMPLATE GRANULES FOUND: {} records for {}", countResString1, template.getRequestURL());
				}

			    }

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error("Get Number of Records Error");
			    GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error(e.getMessage(), e);
			}

		    }

		    if (dataCenter.isPresent() && !isCwic) {
			String urlRequest = IDN_GRANULES_BASE_TEMPLATE_URL + "dataCenter=" + dataCenter.get();

			if (shortName.isPresent()) {
			    urlRequest = urlRequest + "&shortName=" + shortName.get();
			}

			if (version.isPresent()) {
			    if (version.get().contains(" ") || version.get().contains("Not provided")
				    || version.get().contains("Latest Version")) {
				urlRequest = urlRequest + "&versionId=";
			    } else {
				urlRequest = urlRequest + "&versionId=" + version.get();
			    }
			}
			urlRequest = urlRequest + "&startPage=1&count=1&clientId=gs-service";

			try {
			    String res = downloader.downloadOptionalString(urlRequest).orElse(null);

			    XMLDocumentReader rres = new XMLDocumentReader(res);

			    String countResString = rres.evaluateNode("//*:totalResults").getTextContent();

			    if (Integer.parseInt(countResString) > 0) {
				rightChildCount++;
				GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).info("GRANULES FOUND: {} records for {}",
					countResString, urlRequest);
			    }

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error("Get Number of Records Error");
			    GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error(e.getMessage(), e);
			}

		    }

		    countRecords++;
		    isCwic = false;
		}
		startPosition = startPosition + pageSize;
		totalRecords = totalRecords + countRecords;
		GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).info("Records: " + totalRecords);
		GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).info("CWICRecords: " + cwicCount);
		GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).info("CHILD COUNT (IDENTIFIER): " + childcount);
		GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class)
			.info("CHILD COUNT (DATA CENTER+SHORT NAME): " + rightChildCount);
		GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).info("ERROR ID: " + errorId);

	    }

	    // Assert.assertTrue(cwicCount > 3200);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).error("Get request error");
	    GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).error(e.getMessage(), e);
	}

    }

    private Optional<List<String>> readListString(XMLDocumentReader reader, String xpath) {
	try {

	    return Optional.ofNullable(reader.evaluateTextContent(xpath));

	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();
    }

    private Optional<String> read(XMLDocumentReader reader, String xpath) {

	try {

	    return Optional.ofNullable(reader.evaluateString(xpath));

	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(CMROpensearchConnectorExternalTestIT.class).warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();

    }

}
