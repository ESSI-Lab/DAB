package eu.essi_lab.accessor.inpe;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import eu.essi_lab.accessor.satellite.common.SatelliteConnector;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.RankingStrategy;

/**
 * @author Fabrizio
 */
public class INPEConnector extends SatelliteConnector<INPEConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "INPEConnector";

    private Logger logger;

    private Iterator<XMLNodeReader> currentIt;
    private int maxNumPage = 0;
    private int maxNumScene = 0;

    /**
     * @param maxNumScene 0 for unlimited
     */
    public void setMaxNumScene(int maxNumScene) {
	this.maxNumScene = maxNumScene;
    }

    /**
     * @param maxNumPage 0 for unlimited
     */
    public void setMaxNumPage(int maxNumPage) {
	this.maxNumPage = maxNumPage;
    }

    // private String cookieval;
    private String baseURL = "http://www.dgi.inpe.br/CDSR/";

    private static final int INPE_ROWS_PER_PAGE = 4;
    private static final int INPE_COLUMNS_PER_ROW = 5;
    private static final int INPE_ENTRIES_PER_PAGE = INPE_COLUMNS_PER_ROW * INPE_ROWS_PER_PAGE;

    private static final String INPE_CONNECTOR_NO_SAT_FOUND_ERROR = "INPE_CONNECTOR_NO_SATELLITES_FOUND_ERROR";

    private static final String INPE_CONNETOR_SERVICE_DOWN_ERROR = "INPE_CONNETOR_SERVICE_DOWN_ERROR";

    private static final String INPE_CONNECTOR_DOWNLOAD_ERROR = "INPE_CONNECTOR_DOWNLOAD_ERROR";

    private static final String INPE_CONNECTOR_NO_SCENES_FOUND_ERROR = "INPE_CONNECTOR_NO_SCENES_FOUND_ERROR";

    private static final String INPE_CONNECTOR_TOTAL_NUMBER_PAGES_NOT_FOUND_ERROR = "INPE_CONNECTOR_TOTAL_NUMBER_PAGES_NOT_FOUND_ERROR";

    protected String createKey(XMLNodeReader o) {
	Map<String, String> maop = extractOriginalMD(o);

	return "INPE" + maop.get("sceneid");
    }

    private String cookieval = null;

    private void retrievePanelAndCookie() throws GSException {

	Properties props = new Properties();

	try {
	    InputStream stream = INPEConnector.class.getClassLoader().getResourceAsStream("inpe/collectionNames.properties");
	    props.load(stream);
	    stream.close();
	} catch (IOException e1) {

	    e1.printStackTrace();

	    logger.error("Can not load collection names");
	}

	Iterator<Map.Entry<Object, Object>> it = props.entrySet().iterator();

	while (it.hasNext()) {

	    Map.Entry<Object, Object> entry = it.next();

	    String satelliteId = (String) entry.getKey();
	    knownSatellites.add(satelliteId);
	}

	String cookievalue = "PHPSESSID=" + UUID.randomUUID().toString().replace("-", "");

	Map<String, String> headers = new HashMap<String, String>();
	headers.put("Cookie", cookievalue);

	try {
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	    InputStream stream = downloader.downloadOptionalStream(baseURL + "panel.php?SESSION_LANGUAGE=EN", HttpHeaderUtils.build(headers))
		    .get();
	    INPESatellites.getInstance().init(stream);
	    this.cookieval = cookievalue;

	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}

	if (INPESatellites.getInstance().getSatellites().size() == 0) {
	    logger.error("No satellites found");
	    throw GSException.createException(//
		    getClass(), //
		    "No satellites found", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INPE_CONNECTOR_NO_SAT_FOUND_ERROR);
	}

    }

    private XMLDocumentReader getDocumentFromURL(String url) throws GSException {

	if (cookieval == null) {
	    retrievePanelAndCookie();
	}

	Map<String, String> headers = new HashMap<String, String>();
	headers.put("Cookie", cookieval);
	headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

	InputStream is;
	try {
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MINUTES, 2);

	    Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url, HttpHeaderUtils.build(headers));
	    if (!optionalStream.isPresent()) {

		throw GSException.createException(//
			getClass(), //
			"Download error", //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			INPE_CONNECTOR_DOWNLOAD_ERROR);
	    }

	    is = optionalStream.get();

	    InputStreamReader reader = new InputStreamReader(is, Charset.forName("ISO-8859-1"));
	    InputSource input = new InputSource(reader);

	    XMLReader tagsoupReader = null;

	    try {
		tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
		tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
	    }

	    SAXSource source = new SAXSource(tagsoupReader, input);
	    DOMResult result = new DOMResult();
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.transform(source, result);

	    XMLDocumentReader xdoc = new XMLDocumentReader((Document) result.getNode());

	    return xdoc;

	} catch (Exception e) {

	    e.printStackTrace();

	}

	return null;

    }

    private List<XMLNodeReader> retrieveScenes(String satellite, String sensor, int page, String from, String until)
	    throws Exception, EndPageException {

	if (maxNumPage > 0 && page > maxNumPage) {
	    // maxNumPage is used to limit the harvesting size
	    throw new EndPageException();
	}

	String fromString;
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	if (from == null) {
	    fromString = "1900-01-01'T'00:00:00'Z'";
	} else {
	    TemporalAccessor t = ZonedDateTime.parse(from);
	    fromString = dateTimeFormatter.format(t).replace("T", "'T'").replace("Z", "'Z'");
	}
	String untilString;
	if (until == null) {
	    ZonedDateTime yesterday = ZonedDateTime.now().with(ChronoField.NANO_OF_DAY, 0).minusDays(1);
	    untilString = dateTimeFormatter.format(yesterday).replace("T", "'T'").replace("Z", "'Z'");
	} else {
	    TemporalAccessor t = ZonedDateTime.parse(until);
	    untilString = dateTimeFormatter.format(t).replace("T", "'T'").replace("Z", "'Z'");
	}

	// END DATE (last day at 00)

	// System.out.println(enddatenew);

	String url = baseURL + //
		"mantab.php?" + //
		// SATELLITE:
		// AQUA_1 = A1
		// TERRA_1 = T1
		// LANDSAT_1 = L1
		// LANDSAT_2 = L2
		// LANDSAT_3 = L3
		// LANDSAT_5 = L5
		// LANDSAT_7 = L7
		// GLS_LANDSAT = GLS
		// CBERS = CB2
		// CBERS = CB2B
		// RESOURCE SAT_1 = P6
		"QUICK=&TAM=G" + //

		"&SATELITE=" + satellite + //
		"&SENSOR=" + sensor + //

		// "&Q1=100&Q2=100&Q3=100&Q4=100" +

		// startDate = timeBond.getSince("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// endDate = timeBond.getTo("yyyy-MM-dd'T'HH:mm:ss'Z'");

		"&IDATE=" + fromString + //
		"&FDATE=" + untilString + //
		"&LAT1=" + "-90" + //
		"&LAT2=" + "90" + //
		"&LON1=" + "-180" + //
		"&LON2=" + "180" + //
		"&PAGE=" + page;

	XMLDocumentReader doc = getDocumentFromURL(url);
	int total = 0;

	if (doc != null) {

	    try {
		String docString = doc.asString();

		if (docString.contains("Nenhuma cena encontrada") || docString.contains("No scene found")) {
		    // this satellite has problems, this will be the last page
		    GSLoggerFactory.getLogger(getClass())
			    .error("Problem harvesting INPE satellite: " + satellite + " sensor: " + sensor + " (no scenes found)");

		    throw GSException.createException(//
			    getClass(), //
			    "Problem harvesting INPE satellite: " + satellite + " sensor: " + sensor + " (no scenes found)", //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    INPE_CONNECTOR_NO_SCENES_FOUND_ERROR);
		}

		String[] splitted = docString.split("TOTAL=");

		String s = null;
		if (splitted.length > 1)
		    s = splitted[1];

		if (s != null) {
		    String totString = s.substring(0, s.indexOf("\""));
		    // totalRecords = Integer.valueOf(totString);
		    int intPart = Integer.valueOf(totString) / INPE_ENTRIES_PER_PAGE;
		    int tocheck = Integer.valueOf(totString) % INPE_ENTRIES_PER_PAGE;
		    if (tocheck > 0) {
			intPart++;
		    }
		    total = intPart;
		    if (page > total) {
			throw new EndPageException();
		    }
		} else {

		    if (docString.contains("Current Page : ") || docString.contains("gina Atual : ")) {
			// it means that there is only this page
			// if this is page 1 o.k., else we are at the end
			if (page > 1) {
			    throw new EndPageException();
			}
		    } else {
			// this satellite has problems, this will be the last page
			GSLoggerFactory.getLogger(getClass()).error("Problem harvesting INPE satellite: " + satellite + " sensor: " + sensor
				+ " (total number of pages not found)");

			throw GSException.createException(//
				getClass(), //
				"Problem harvesting INPE satellite: " + satellite + " sensor: " + sensor
					+ " (total number of pages not found)", //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_ERROR, //
				INPE_CONNECTOR_TOTAL_NUMBER_PAGES_NOT_FOUND_ERROR);
		    }

		}

		List<XMLNodeReader> extracted = extractScenesFromPage(doc);
		// officialCount = officialCount + extracted.size();
		return extracted;

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		logger.error(e.getMessage(), e);
	    }

	}
	return null;

    }

    private List<XMLNodeReader> extractScenesFromPage(XMLDocumentReader xdoc) throws GSException {

	List<XMLNodeReader> ret = new ArrayList<XMLNodeReader>();

	try {
	    Node[] linkNodes = xdoc.evaluateNodes("//*:td/*:a/@href");
	    int i = 0;
	    for (Node linkNode : linkNodes) {
		String link = xdoc.evaluateString(linkNode, ".");
		i++;
		if (maxNumScene > 0 && i > maxNumScene) {
		    // maxNumScene is used to limit the harvesting size
		    break;
		}
		if (link == null || !link.contains("manage.php?")) {
		    continue;
		}
		String[] splitted = link.split("INDICE=");

		if (splitted.length > 1) {

		    String[] splitted2 = splitted[1].split("&");

		    if (splitted2.length > 1) {

			String index = splitted2[0];

			if (!index.isEmpty()) {

			    String url = baseURL + "manage.php?INDICE=" + index + "&DONTSHOW=0";
			    // String url = baseURL + index;

			    XMLDocumentReader res = getDocumentFromURL(url);
			    if (res != null) {
				ret.add(res);
			    }

			}
		    }
		}

	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error extracting INPE scenes");
	}

	return ret;

    }

    private Map<String, String> getNewInpeMetadataElementsMap() {

	Map<String, String> ret = new HashMap<String, String>();

	ret.put("satellite", "");//
	ret.put("sensor", "");//
	ret.put("path", "");
	ret.put("row", "");
	ret.put("passage date", "");//
	ret.put("sceneid", "");
	ret.put("orbit", "");
	ret.put("top left lat", "");//
	ret.put("top left lon", "");//
	ret.put("bottom right lon", "");//
	ret.put("bottom right lat", "");//
	ret.put("center time(gmt)", "");
	ret.put("image orientation", "");
	ret.put("sun azimuth", "");
	ret.put("sun elevation", "");
	// TODO Add Q1, etc.

	return ret;

    }

    private Map<String, String> extractOriginalMD(XMLNodeReader doc) {
	Map<String, String> inpe_el_map = getNewInpeMetadataElementsMap();

	if (doc == null)
	    return null;

	Node[] trRes = null;
	try {
	    trRes = doc.evaluateNodes("//*[local-name()='tr']");
	} catch (XPathExpressionException e) {

	    logger.error(e.getMessage(), e);

	    return null;
	}

	for (Node n : trRes) {

	    Node first = n.getFirstChild();
	    if (first == null)
		continue;

	    String first_text = first.getFirstChild() == null ? null : first.getFirstChild().getTextContent();

	    if (first_text != null && !first_text.equalsIgnoreCase("")) {

		first_text = first_text.trim().toLowerCase();

		// This is the tail of inpe response, which is not trimmed
		byte[] bytes = null;
		bytes = first_text.getBytes(StandardCharsets.UTF_8);

		if (bytes.length > 2 && bytes[bytes.length - 1] == -96 && bytes[bytes.length - 2] == -62) {

		    byte[] b = new byte[bytes.length - 2];

		    for (int i = 0; i < bytes.length - 2; i++) {
			b[i] = bytes[i];
		    }

		    first_text = new String(b);

		}

		String inope_el = inpe_el_map.get(first_text);

		if (inope_el == null)
		    continue;

		Node last = n.getLastChild();
		if (last == null)
		    continue;

		String last_text = last.getFirstChild() == null ? null : last.getFirstChild().getTextContent();

		if (last_text != null && !last_text.equalsIgnoreCase("")) {

		    inpe_el_map.put(first_text, last_text);

		}

	    } else {
		continue;
	    }

	}

	return inpe_el_map;

    }

    protected SatelliteSceneMD createScene(XMLNodeReader doc, INPESatellite satellite) {

	SatelliteSceneMD scene = new SatelliteSceneMD();

	String id = createKey(doc);

	scene.setId(id);

	upDateMetadata(scene, satellite, extractOriginalMD(doc));

	String link = null;
	try {
	    // TODO encode link for xml
	    link = doc.evaluateString("//*[local-name()='img']/@src");
	    link = URLEncoder.encode(link, "UTF-8");
	} catch (XPathExpressionException e) {
	    // TODO Auto-generated catch block
	    logger.error(e.getMessage(), e);
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	if (link != null && !"".equalsIgnoreCase(link))
	    scene.setThumbnailURL(baseURL + link);

	return scene;
    }

    // public enum INPESatellite {
    //
    // CBERS2("CB2", "CBERS 2"), CBERS2B("CB2B", "CBERS 2B"), LANDSAT_1("L1", "Landsat 1"), LANDSAT_2("L2", "Landsat
    // 2"), LANDSAT_3("L3",
    // "Landsat 3"), LANDSAT_5("L5", "Landsat 5"), LANDSAT_7("L7", "Landsat 7"), TERRA_1("T1",
    // "Terra 1"), AQUA_1("A1", "Aqua 1"), RESOURCE_SAT_1("P6", "ResourceSat-1"), GLS_LANDSAT("GLS", "GLS-Landsat");
    //
    // private String id;
    // private String title;
    //
    // public String getId() {
    // return id;
    // }
    //
    // public String getTitle() {
    // return title;
    // }
    //
    // INPESatellite(String id, String title) {
    // this.id = id;
    // this.title = title;
    // }
    // }

    private void upDateMetadata(SatelliteSceneMD scene, INPESatellite satellite, Map<String, String> inpe_el_map) {

	// CCD=ChargeCoupledDevice
	String north = inpe_el_map.get("top left lat");

	String south = inpe_el_map.get("bottom right lat");

	String west = inpe_el_map.get("top left lon");

	String east = inpe_el_map.get("bottom right lon");

	if (north != null && !north.equalsIgnoreCase("") && south != null && !south.equalsIgnoreCase("") && west != null
		&& !west.equalsIgnoreCase("") && east != null && !east.equalsIgnoreCase("")) {
	    scene.setEast(Double.valueOf(east));
	    scene.setWest(Double.valueOf(west));
	    scene.setNorth(Double.valueOf(north));
	    scene.setSouth(Double.valueOf(south));
	}

	String satelliteId = satellite.getId();

	scene.setPlatformId(satelliteId);
	scene.setPlatform("INPE-" + satellite.getTitle());

	String time = inpe_el_map.get("passage date");
	String passage = inpe_el_map.get("center time(gmt)");

	if (time != null && !time.equalsIgnoreCase("")) {

	    if (passage != null && !passage.equalsIgnoreCase("")) {

		scene.setTimeStart(time + "T" + passage);
		scene.setTimeEnd(time + "T" + passage);
		// metadata.getDataIdentification().addTemporalExtent(time + "H" + passage + "Z", time + "H" + passage +
		// "Z");
		// mi_Metadata.getDataIdentification().addTemporalExtent(time + "H" + passage + "Z", time + "H" +
		// passage + "Z");

	    } else {
		scene.setTimeStart(time);
		scene.setTimeEnd(time);
		// metadata.getDataIdentification().addTemporalExtent(time, time);
		// mi_Metadata.getDataIdentification().addTemporalExtent(time, time);
	    }

	}

	String sensor = inpe_el_map.get("sensor");
	if (sensor != null && !sensor.equalsIgnoreCase("")) {
	    // MI_Instrument mi_Instrument = new MI_Instrument();
	    // mi_Instrument.setDescription(sensor);
	    scene.setSensor(sensor);
	}

	String path = inpe_el_map.get("path");
	String row = inpe_el_map.get("row");

	if (path != null && !path.isEmpty()) {
	    // if (path.contains("_"))
	    // path = path.split("_")[0];

	    scene.setPath(path);
	}

	if (row != null && !row.isEmpty()) {
	    // row = row.replaceAll("[^\\d]", "");
	    scene.setRow(row);
	}

    }

    public INPEConnector() {
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.INPE_URI);
	return ret;
    }

    /**
     * @return
     * @throws Exception
     */
    public List<GSResource> collections() throws Exception {

	return getCollections();
    }

    @Override
    protected List<GSResource> getCollections() throws Exception {
	List<GSResource> ret = new ArrayList<>();

	logger.trace("HARVESTING STARTED at: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));

	Properties props = new Properties();

	try {
	    InputStream stream = INPEConnector.class.getClassLoader().getResourceAsStream("inpe/collectionNames.properties");
	    props.load(stream);
	    stream.close();
	} catch (IOException e1) {

	    e1.printStackTrace();

	    logger.error("Can not load collection names");
	}

	Iterator<Map.Entry<Object, Object>> it = props.entrySet().iterator();

	while (it.hasNext()) {

	    Map.Entry<Object, Object> entry = it.next();

	    String id = (String) entry.getKey();
	    String name = (String) entry.getValue();

	    try {

		String res = "inpe/" + name + ".xml";
		logger.info("Loading satellite collection: " + id + " (" + res + ")");

		ClonableInputStream clonableInputStream = new ClonableInputStream(
			INPEConnector.class.getClassLoader().getResourceAsStream(res));

		MIMetadata md = new MIMetadata(clonableInputStream.clone());

		DatasetCollection collection = new DatasetCollection();

		if (name.equals("INPE")) {

		    // highest ranking for the root collection
		    collection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
		    collection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);

		} else {

		    // lowest ranking for the others
		    collection.getPropertyHandler().setLowestRanking();
		}

		//
		// set the original metadata
		//
		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(IOStreamUtils.asUTF8String(clonableInputStream.clone()));
		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);

		collection.setOriginalMetadata(originalMetadata);

		//
		//
		//

		collection.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(md);

		ret.add(collection);

	    } catch (Throwable e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return ret;
    }

    @Override
    protected String getMetadataFormat() {
	return CommonNameSpaceContext.INPE_URI;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("inpe.br")) {
	    return true;
	}
	return false;
    }

    private HashSet<String> harvestedSatellites = new HashSet<>();
    private HashSet<String> knownSatellites = new HashSet<>();

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	if (cookieval == null) {
	    retrievePanelAndCookie();
	}
	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	String from = request.getFromDateStamp();
	String until = request.getUntilDateStamp();

	HarvestingProperties properties = request.getHarvestingProperties();

	String resumptionToken = request.getResumptionToken();

	if (true) {

	    GSLoggerFactory.getLogger(getClass()).info("Storing INPE collections STARTED");
	    addCollections(response);
	    GSLoggerFactory.getLogger(getClass()).info("Storing INPE collections ENDED");
	}

	if (resumptionToken == null) {
	    resumptionToken = "1;1;1"; // first satellite, first sensor, first page
	}
	String[] split = resumptionToken.split(";");
	int satelliteInt = Integer.parseInt(split[0]);
	int sensorInt = Integer.parseInt(split[1]);
	int page = Integer.parseInt(split[2]);

	GSLoggerFactory.getLogger(getClass()).info("Serving INPE resumption token: " + resumptionToken + " (satellite, sensor, page)");
	GSLoggerFactory.getLogger(getClass())
		.info("INPE satellite: " + satelliteInt + "/" + INPESatellites.getInstance().getSatellites().size());
	INPESatellite satellite = INPESatellites.getInstance().getSatellites().get(satelliteInt - 1);

	GSLoggerFactory.getLogger(getClass()).info("INPE sensor: " + sensorInt + "/" + satellite.getSensors().size());
	GSLoggerFactory.getLogger(getClass()).info("INPE page: " + page);

	INPESensor sensor = satellite.getSensors().get(sensorInt - 1);

	String nextResumptionToken = satelliteInt + ";" + sensorInt + ";" + (page + 1);

	try {

	    List<XMLNodeReader> scenes = retrieveScenes(satellite.getId(), sensor.getId(), page, from, until);
	    if (scenes == null) {
		response.setResumptionToken(null);
		return response;
	    }

	    for (XMLNodeReader scene : scenes) {
		try {

		    try {
			SatelliteSceneMD tostore = createScene(scene, satellite);
			OriginalMetadata metadataRecord = new OriginalMetadata();
			metadataRecord.setMetadata(tostore.toNode().asString());
			metadataRecord.setSchemeURI(CommonNameSpaceContext.INPE_URI);
			response.addRecord(metadataRecord);
			harvestedSatellites.add(satellite.getId());

		    } catch (Throwable e) {
			e.printStackTrace();
		    }

		} catch (Throwable th) {
		}

	    }
	} catch (EndPageException e) {
	    if (sensorInt == satellite.getSensors().size()) {
		// end of sensors..
		if (satelliteInt == INPESatellites.getInstance().getSatellites().size()) {
		    // end of satellites... end of harvesting!
		    nextResumptionToken = null;
		    // String satellites = "";
		    // for (String harvestedSatellite : harvestedSatellites) {
		    // satellites = satellites + harvestedSatellite + " ";
		    // }
		    logger.info("Harvested from the following satellites: " + harvestedSatellites);
		    HashSet<String> newSatellites = new HashSet<>(harvestedSatellites);
		    newSatellites.removeAll(knownSatellites);
		    if (!newSatellites.isEmpty()) {
			logger.warn("These satellites scenes haven't a correspondent collection, so they won't be shown: " + newSatellites);
		    }
		    HashSet<String> missingSatellites = new HashSet<>(knownSatellites);
		    missingSatellites.removeAll(harvestedSatellites);
		    if (!missingSatellites.isEmpty()) {
			logger.error("These satellites haven't been harvested: " + missingSatellites);
		    }

		} else {
		    // next satellite, first sensor, first page
		    nextResumptionToken = (satelliteInt + 1) + ";1;1";
		}
	    } else {
		// same satellite, next sensor, first page
		nextResumptionToken = (satelliteInt) + ";" + (sensorInt + 1) + ";1";
	    }

	} catch (Exception e) {
	    // the harvesting should resume with the last resumption token
	    GSLoggerFactory.getLogger(getClass())
		    .error("Errors harvesting INPE " + satellite.getTitle() + " page " + page + " probably service down?");

	    throw GSException.createException(getClass(),
		    "Errors harvesting INPE " + satellite.getTitle() + " page " + page + " probably service down?",
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, INPE_CONNETOR_SERVICE_DOWN_ERROR);
	}

	response.setResumptionToken(nextResumptionToken);

	return response;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected INPEConnectorSetting initSetting() {

	return new INPEConnectorSetting();
    }
}
