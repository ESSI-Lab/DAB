package eu.essi_lab.accessor.ana;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Connector for National Water Agency of Brazil (Brazil-ANA)
 * 
 * @author roncella
 */
public class ANAConnector extends HarvestedQueryConnector<ANAConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ANAConnector";

    private static final String ANA_SEPARATOR_STRING = "ANA_SEPARATOR_STRING";

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String ANA_CONNECTOR_RESPONSE_STREAM_ERROR = "ANA_CONNECTOR_RESPONSE_STREAM_ERROR";

    private static final String ANA_CONNECTOR_PARSE_ERROR = "ANA_CONNECTOR_PARSE_ERROR";

    private static final String ANA_CONNCECTOR_RESUMPTION_TOKEN_ERROR = "ANA_CONNCECTOR_RESUMPTION_TOKEN_ERROR";

    private BasinDocument basins;

    private StationListDocument stationList;

    public ANAConnector() {
	this.downloader = new Downloader();
    }

    private int partialNumbers = 0;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();
	if (baseEndpoint == null) {
	    return false;
	}
	String request = baseEndpoint.endsWith("/") ? baseEndpoint + "HidroBaciaSubBacia?codBacia=&codSubBacia="
		: baseEndpoint + "/HidroBaciaSubBacia?codBacia=&codSubBacia=";
	try {
	    Optional<InputStream> res = getDownloader().downloadOptionalStream(request);
	    if (res.isPresent()) {

		XMLDocumentReader reader = new XMLDocumentReader(res.get());
		Node[] result = reader.evaluateNodes(("//*:nmBacia"));
		if (result.length > 0)
		    return true;

	    }

	} catch (SAXException | IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (XPathExpressionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return false;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	// retrieve basins
	getBasins();

	// retrieve station list
	getStationsList();

	// basins idetifiers
	LinkedHashSet<String> identifiers = basins.getBasinIdentifiers();

	String id = request.getResumptionToken();

	Iterator<String> iterator = identifiers.iterator();

	String nextId = null;
	if (id == null) {
	    if (iterator.hasNext()) {
		// we start from the first
		id = iterator.next();
		if (iterator.hasNext()) {
		    nextId = iterator.next();
		}
	    } else {
		// empty package list
		// nextId remains null
	    }
	} else {
	    if (identifiers.contains(id)) {
		while (iterator.hasNext()) {
		    String tmp = iterator.next();
		    if (tmp.equals(id) && iterator.hasNext()) {
			nextId = iterator.next();
			break;
		    }
		    // if it is the last element
		    // nextId remains null
		}

	    } else {
		// if the package id is not found in the package list
		throw GSException.createException(//
			getClass(), //
			"Unable to resume from resumption token: " + id, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.ERRORTYPE_SERVICE, ANA_CONNCECTOR_RESUMPTION_TOKEN_ERROR);
	    }
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	Integer siteNumber = Integer.valueOf(id);
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && partialNumbers > mr.get() - 1) {
	    // // max record set
	    maxNumberReached = true;
	}

	if (!maxNumberReached) {

	    if (id != null) {
		LinkedHashSet<String> subBasin = basins.getSubBasinIdentifiers(id);

		for (String sub : subBasin) {
		    List<StationDocument> res = stationList.getStations(id, sub);

		    if (res != null && !res.isEmpty()) {
			// limited numbers
			// if(partialNumbers < 5) {
			for (StationDocument station : res) {
			    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && partialNumbers > mr.get() - 1) {
				break;
			    }
			    try {

				List<String> originalMetadataList = checkVariable(station);

				if (originalMetadataList.isEmpty()) {
				    GSLoggerFactory.getLogger(getClass()).warn("Empty variables list for station: {}", station);
				}

				for (String s : originalMetadataList) {

				    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && partialNumbers > mr.get() - 1) {
					break;
				    }

				    OriginalMetadata metadataRecord = new OriginalMetadata();
				    metadataRecord.setSchemeURI(CommonNameSpaceContext.ANA_URI);
				    String metadata = "";
				    metadata = s;
				    metadataRecord.setMetadata(metadata);
				    ret.addRecord(metadataRecord);
				    partialNumbers++;
				}

			    } catch (Exception e) {
				logger.error(e.getMessage());
			    }
			}
			// }
		    }
		}
	    }

	    if (isFirstSiteOnly()) {
		nextId = null;
	    }

	    ret.setResumptionToken(nextId);
	    return ret;

	} else {
	    ret.setResumptionToken(null);

	    GSLoggerFactory.getLogger(ANAConnector.class).debug("Added all Collection records: " + partialNumbers);
	    partialNumbers = 0;
	    return ret;
	}

    }

    private List<String> checkVariable(StationDocument station) throws UnsupportedEncodingException, TransformerException {

	List<String> ret = new ArrayList<String>();
	for (ANAVariable variable : ANAVariable.values()) {

	    Date endDate = getEndDate(station.getStationCode(), variable);

	    if (endDate == null) {
		GSLoggerFactory.getLogger(getClass()).info("Station {} Variable {} not found", station.getStationCode(),
			variable.toString());
		continue;
	    }

	    Date startDate = getStartDate(station.getStationCode(), variable, ISO8601DateTimeUtils.parseISO8601ToDate("1900-01-01").get(),
		    endDate);

	    GSLoggerFactory.getLogger(getClass()).info("Station {} Variable {} start: {} end: {}", station.getStationCode(),
		    variable.toString(), startDate, endDate);

	    station.setVariable(variable);
	    String originalMetadata = station.asString() + ANA_SEPARATOR_STRING + variable.toString() + ANA_SEPARATOR_STRING
		    + startDate.getTime() + ANA_SEPARATOR_STRING + endDate.getTime();
	    ret.add(originalMetadata);

	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.ANA_URI);
	return toret;
    }

    private void getBasins() throws GSException {
	if (basins == null) {
	    this.basins = getBasinDocument();
	}
    }

    private void getStationsList() throws GSException {
	if (stationList == null) {
	    this.stationList = getStatationsDocument();
	}

    }

    public HydroInventario getHydroInventario() throws GSException {

	HydroInventario doc = null;
	String baseEndpoint = getSourceURL();
	String request = baseEndpoint.endsWith("/") ? baseEndpoint : baseEndpoint + "/";
	request = request + "HidroInventario";

	logger.info("Retrieving HydroInventario. Request: {}", request);

	// HttpPost post = new HttpPost(request.trim());
	// post.addHeader("Content-Type", "application/x-www-form-urlencoded");
	//
	String parameters = "codEstDE=&codEstATE=&tpEst=&nmEst=&nmRio=&codSubBacia=&codBacia=&nmMunicipio=&nmEstado=&sgResp=&sgOper=&telemetrica=1";
	// ByteArrayEntity inputEntity = new ByteArrayEntity(parameters.getBytes(StandardCharsets.UTF_8));
	// inputEntity.setChunked(false);
	// post.setEntity(inputEntity);
	//

	try {

	    HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, request.trim(), parameters.getBytes(StandardCharsets.UTF_8));

	    logger.info("Sending POST Request");

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(postRequest);

	    InputStream output = response.body();

	    File tmpFile = File.createTempFile(ANAConnector.class.getName(), ".xml");
	    logger.info("Downloading document to : " + tmpFile.getAbsolutePath());
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(output, fos);
	    if (output != null)
		output.close();
	    logger.info("Downloaded document. Size: " + tmpFile.length() + " bytes");

	    FileInputStream fis = new FileInputStream(tmpFile);
	    doc = new HydroInventario(fis);

	    logger.info("XML response parsed, removing temporary file: " + tmpFile.getAbsolutePath());
	    if (fos != null)
		fos.close();
	    tmpFile.delete();

	    return doc;
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ANA_CONNECTOR_PARSE_ERROR, //
		    e);
	}
    }

    public StationListDocument getStatationsDocument() throws GSException {
	StationListDocument doc = null;
	String baseEndpoint = getSourceURL();
	String request = baseEndpoint.endsWith("/") ? baseEndpoint + "ListaEstacoesTelemetricas?statusEstacoes=0&origem="
		: baseEndpoint + "/ListaEstacoesTelemetricas?statusEstacoes=0&origem=";
	logger.info("Retrieving Stations List. Request: {}", request);

	try {
	    Optional<InputStream> res = getDownloader().downloadOptionalStream(request);
	    if (res.isPresent()) {
		InputStream originalStream = res.get();
		// to replace an invalid XML character sent by ANA, and prevent SAXException
		ANAReplacingInputStream replacingStream = new ANAReplacingInputStream(originalStream);
		doc = new StationListDocument(replacingStream);
	    }
	} catch (IOException e) {
	    logger.error("IOException connecting to {}", request, e);

	    throw GSException.createException(getClass(), "IOException connecting to " + request, null, "Could not contact Brazil-ANA",
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, ANA_CONNECTOR_RESPONSE_STREAM_ERROR, e);
	} catch (SAXException e) {

	    logger.error("SAXException connecting to {}", request, e);

	    throw GSException.createException(getClass(), "SAXException connecting to " + request, null, "Could not parse stream service",
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ANA_CONNECTOR_PARSE_ERROR, e);
	} catch (ParserConfigurationException e) {
	    logger.error("ParserConfigurationException connecting to {}", request, e);
	    throw GSException.createException(getClass(), "ParserConfigurationException connecting to " + request, null,
		    "Could not parse stream service", ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ANA_CONNECTOR_PARSE_ERROR, e);
	}

	return doc;
    }

    public BasinDocument getBasinDocument() throws GSException {
	BasinDocument doc = null;
	String baseEndpoint = getSourceURL();

	String request = baseEndpoint.endsWith("/") ? baseEndpoint + "HidroBaciaSubBacia?codBacia=&codSubBacia="
		: baseEndpoint + "/HidroBaciaSubBacia?codBacia=&codSubBacia=";
	logger.info("Retrieving Basins. Request: {}", request);

	try {
	    Optional<InputStream> res = getDownloader().downloadOptionalStream(request);
	    if (res.isPresent()) {
		doc = new BasinDocument(res.get());
	    }
	} catch (IOException e) {
	    logger.error("IOException connecting to {}", request, e);

	    throw GSException.createException(getClass(), "IOException connecting to " + request, null, "Could not contact Brazil-ANA",
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, ANA_CONNECTOR_RESPONSE_STREAM_ERROR, e);
	} catch (SAXException e) {

	    logger.error("SAXException connecting to {}", request, e);

	    throw GSException.createException(getClass(), "SAXException connecting to " + request, null, "Could not parse stream service",
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ANA_CONNECTOR_PARSE_ERROR, e);
	}

	return doc;
    }

    long oneDay = 1000l * 60 * 60 * 24;

    public Date getEndDate(String stationCode, ANAVariable variable) {

	GSLoggerFactory.getLogger(getClass()).info("Getting end date for station: {} variable: {}", stationCode, variable.toString());

	Date now = new Date();
	Date date = now;

	TemporalInfo info;

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 2);
	    } else {
		return info.getEndDate();
	    }

	}

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 10);
	    } else {
		return getEndDate(stationCode, variable, info.getEndDate(), now);
	    }

	}

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 100);
	    } else {
		return getEndDate(stationCode, variable, info.getEndDate(), now);
	    }

	}

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 300);
	    } else {
		return getEndDate(stationCode, variable, info.getEndDate(), now);
	    }

	}

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 1000);
	    } else {
		return getEndDate(stationCode, variable, info.getEndDate(), now);
	    }

	}

	for (int i = 0; i < 3; i++) {

	    info = getTemporalInfo(stationCode, variable, date, date);

	    if (info.getEndDate() == null) {
		date = new Date(date.getTime() - oneDay * 10000);
	    } else {
		return getEndDate(stationCode, variable, info.getEndDate(), now);
	    }

	}

	return null;

    }

    public Date getEndDate(String stationCode, ANAVariable variable, Date valueDate, Date zeroDate) {

	GSLoggerFactory.getLogger(getClass()).info("Getting end date using bisection for station: {} variable: {}", stationCode,
		variable.toString());

	long zeroTime = zeroDate.getTime();
	long valueTime = valueDate.getTime();
	long gap = zeroTime - valueTime;
	GregorianCalendar valueCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	valueCalendar.setTime(valueDate);
	GregorianCalendar zeroCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	zeroCalendar.setTime(zeroDate);
	if (equalCalendars(zeroCalendar, valueCalendar)) {
	    return valueDate;
	}
	valueCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
	if (equalCalendars(zeroCalendar, valueCalendar)) {
	    return valueDate;
	}
	Date middle = new Date(valueTime + gap / 2);
	TemporalInfo temporalInfo = getTemporalInfo(stationCode, variable, middle);
	if (temporalInfo.getEndDate() == null) {
	    return getEndDate(stationCode, variable, valueDate, middle);
	} else {
	    return getEndDate(stationCode, variable, temporalInfo.getEndDate(), zeroDate);
	}
    }

    public Date getStartDate(String stationCode, ANAVariable variable, Date zeroDate, Date valueDate) {

	GSLoggerFactory.getLogger(getClass()).info("Getting start date using bisection for station: {} variable: {}", stationCode,
		variable.toString());

	long zeroTime = zeroDate.getTime();
	long valueTime = valueDate.getTime();
	GregorianCalendar zeroCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	zeroCalendar.setTime(zeroDate);
	GregorianCalendar valueCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	valueCalendar.setTime(valueDate);
	if (equalCalendars(zeroCalendar, valueCalendar)) {
	    return valueDate;
	}
	zeroCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
	if (equalCalendars(zeroCalendar, valueCalendar)) {
	    return valueDate;
	}
	long gap = valueTime - zeroTime;
	Date middle = new Date(zeroTime + gap / 2);
	TemporalInfo temporalInfo = getTemporalInfo(stationCode, variable, middle);
	if (temporalInfo.getStartDate() == null) {
	    return getStartDate(stationCode, variable, middle, valueDate);
	} else {
	    return getStartDate(stationCode, variable, zeroDate, temporalInfo.getStartDate());
	}
    }

    private boolean equalCalendars(GregorianCalendar c1, GregorianCalendar c2) {
	return (c1.get(GregorianCalendar.YEAR) == c2.get(GregorianCalendar.YEAR) && //
		c1.get(GregorianCalendar.MONTH) == c2.get(GregorianCalendar.MONTH) && //
		c1.get(GregorianCalendar.DAY_OF_MONTH) == c2.get(GregorianCalendar.DAY_OF_MONTH));
    }

    public TemporalInfo getTemporalInfo(String stationCode, ANAVariable variable, Date date) {
	return getTemporalInfo(stationCode, variable, date, date);
    }

    public TemporalInfo getTemporalInfo(String stationCode, ANAVariable variable, Date startDate, Date endDate) {

	TemporalInfo ret = new TemporalInfo();

	String dateStartString = ISO8601DateTimeUtils.getISO8601Date(startDate);
	String dateEndString = ISO8601DateTimeUtils.getISO8601Date(endDate);
	logger.info("Checking dates: {}/{}", dateStartString, dateEndString);

	HashSet<String> metadata = new HashSet<>();
	metadata.add("CodEstacao");
	metadata.add("DataHora");
	try {
	    XMLDocumentReader xdoc = null;

	    InputStream is = getData(getSourceURL(), stationCode, dateStartString, dateEndString);

	    xdoc = new XMLDocumentReader(is);

	    if (xdoc != null) {

		Number number = xdoc.evaluateNumber("count(//*:DadosHidrometereologicos/" + variable.getXPath() + "[text()])");

		logger.info("{} results", number.longValue());

	    }

	    ret.setParameter(variable.toString());
	    List<Node> nodesDate = new ArrayList<>();
	    nodesDate = xdoc.evaluateOriginalNodesList("//*:DadosHidrometereologicos[" + variable.getXPath() + "[text()]]/*:DataHora");
	    Date minDate = null;
	    Date maxDate = null;

	    for (Node node : nodesDate) {
		String t1 = node.getTextContent();
		DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT-3"));
		Date parsed = iso8601OutputFormat.parse(t1);

		if (minDate == null || parsed.before(minDate)) {
		    minDate = parsed;
		}
		if (maxDate == null || parsed.after(maxDate)) {
		    maxDate = parsed;
		}

	    }

	    ret.setStartDate(minDate);
	    ret.setEndDate(maxDate);

	    if (is != null)
		is.close();

	    return ret;

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return null;
    }

    public InputStream getData(String endpoint, String stationCode, String start, String end) {
	String req = endpoint + "/DadosHidrometeorologicos?codEstacao=" + stationCode + "&dataInicio=" + start + "&dataFim=" + end;

	InputStream body = null;

	try {

	    GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + req + " STARTED");

	    Downloader executor = new Downloader();

	    HttpResponse<InputStream> response = executor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, req));

	    int statusCode = response.statusCode();

	    GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + req + " ENDED");

	    if (statusCode != 200) {

		GSLoggerFactory.getLogger(getClass()).warn("Status code {}: ", statusCode);
		GSLoggerFactory.getLogger(getClass()).warn("Status code not 200, continue");
		return null;
	    }

	    body = response.body();

	    // ret = new XMLDocumentReader(body);

	    return body;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @param firstSiteOnly
     */
    public void setFirstSiteOnly(Boolean firstSiteOnly) {

	getSetting().setHarvestFirstSiteOnly(firstSiteOnly);
    }

    protected boolean isFirstSiteOnly() {

	return getSetting().isFirstSiteHarvestOnlySet();
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ANAConnectorSetting initSetting() {

	return new ANAConnectorSetting();
    }
}
