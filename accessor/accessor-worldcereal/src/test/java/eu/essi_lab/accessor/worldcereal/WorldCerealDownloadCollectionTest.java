//package eu.essi_lab.accessor.worldcereal;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.xml.namespace.NamespaceContext;
//import javax.xml.transform.TransformerException;
//import javax.xml.xpath.XPathExpressionException;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.w3c.dom.Node;
//import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
//
//import eu.essi_lab.accessor.worldcereal.distributed.WorldCerealGranulesBondHandler;
//import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCollectionMapper;
//import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealConnector;
//import eu.essi_lab.lib.net.downloader.Downloader;
//import eu.essi_lab.lib.xml.XMLDocumentReader;
//
///**
// * @author roncella
// */
//public class WorldCerealDownloadCollectionTest {
//
//    WorldCerealConnector connector;
//    WorldCerealCollectionMapper mapper;
//
//    WorldCerealGranulesBondHandler bondHandler;
//
//    Downloader d;
//
//    public static final String datasetId = "urn:uuid:acc6dd27-4cab-419b-a2b4-01403679e439";
//    private static final String START_KEY = "SkipCount";
//    private static final String COUNT_KEY = "MaxResultCount";
//
//    @Before
//    public void init() {
//
//	this.connector = new WorldCerealConnector();
//	this.mapper = new WorldCerealCollectionMapper();
//	this.bondHandler = new WorldCerealGranulesBondHandler(datasetId);
//	this.d = new Downloader();
//
//    }
//
//    @Test
//    public void downloadCollectionMainTest() throws Exception {
//
//	String request = "http://localhost:9090/gs-service/services/essi/gwps/dataset/urn:uuid:acc6dd27-4cab-419b-a2b4-01403679e439?service=WPS&request=execute&identifier=gi-axe-transform&storeexecuteresponse=true&DataInputs=";
//
//	String openCollectionRequest = "http://localhost:9090/gs-service/services/essi/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=&rel=&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&sources=worldcereal&parents=3a13663f-5e7c-4c57-402f-eced8744ee5e&subj=&rela=&";
//
//	String baseAccessURL = "https://ewoc-rdm-api.iiasa.ac.at/collections/2020brlemaugpoly110/items";
//
//	List<JSONObject> featureCollectionList = new ArrayList<JSONObject>();
//
//	boolean finished = false;
//	int start = 0;
//	int count = 100;
//	while (!finished) {
//	    String worldCerealRequest = baseAccessURL + "?" + START_KEY + "=" + start + COUNT_KEY + "=" + count;
//
//	    Integer code = null;
//	    int tries = 3;
//
//	    while ((code == null || code > 400) && tries > 0) {
//
//		HttpResponse<InputStream> response = d.downloadResponse(worldCerealRequest);
//		code = response.statusCode();
//		if (code > 400) {
//		    //try again
//		    tries--;
//		    Thread.sleep(10000);
//		} else {
//
//
//		}
//	    }
//	}
//
//    }
//
//    @Test
//    public void downloadCollectionMainTest2() throws Exception {
//
//    }
//
//    @Test
//    public void downloadCollectionMainTest3() throws Exception {
//
//    }
//
//}
