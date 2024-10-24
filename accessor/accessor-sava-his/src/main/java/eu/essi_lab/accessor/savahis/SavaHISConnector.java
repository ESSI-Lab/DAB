package eu.essi_lab.accessor.savahis;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;
import eu.essi_lab.lib.xml.stax.StAXDocumentIterator;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class SavaHISConnector extends HarvestedQueryConnector<SavaHISConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SavaHISConnector";

    public static final String SAVA_HIS_ROOT = "savaHIS";

    private static final String SAVA_HIS_CONNECTOR_RETRIEVE_CAPABILITIES_ERROR = "SAVA_HIS_CONNECTOR_RETRIEVE_CAPABILITIES_ERROR";

    private static final String SAVA_HIS_CONNECTOR_LIST_RECORDS_ERROR = "SAVA_HIS_CONNECTOR_LIST_RECORDS_ERROR";

    private String siteURL;
    private XMLDocumentReader hydroFeatures = null;
    private XMLDocumentReader hydroSingleSeries = null;
    private XMLDocumentReader meteoFeatures = null;
    private XMLDocumentReader meteoSingleSeries = null;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("savahis.org")) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public String getSourceURL() {
	return siteURL;
    }

    @Override
    public void setSourceURL(String sourceURL) {
	// es. http://savahis.org/his/waterml?
	this.siteURL = sourceURL;

    }

    public enum StationType {
	HYDRO, METEO
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	try {
	    retrieveCapabilities();
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SAVA_HIS_CONNECTOR_RETRIEVE_CAPABILITIES_ERROR, //
		    e);
	}

	String token = request.getResumptionToken();

	if (token == null) {
	    token = StationType.HYDRO.name() + "1";
	}

	StationType stationType = null;

	if (token.contains(StationType.HYDRO.name())) {
	    stationType = StationType.HYDRO;
	    token = token.replace(StationType.HYDRO.name(), "");
	} else {
	    stationType = StationType.METEO;
	    token = token.replace(StationType.METEO.name(), "");
	}
	int index = Integer.parseInt(token);
	int count = 0;
	Node seriesNode = null;
	String nextResumptionToken = null;

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	try {
	    switch (stationType) {
	    case HYDRO:
		seriesNode = hydroSingleSeries.evaluateNode("//*:observationMember[*:OM_Observation][" + index + "]");
		count = hydroSingleSeries.evaluateNumber("count(//*:observationMember[*:OM_Observation])").intValue();
		if (index >= count) {
		    nextResumptionToken = StationType.METEO.name() + "1";
		} else {
		    nextResumptionToken = StationType.HYDRO.name() + (index + 1);
		}
		break;
	    case METEO:
		seriesNode = meteoSingleSeries.evaluateNode("//*:observationMember[*:OM_Observation][" + index + "]");
		count = meteoSingleSeries.evaluateNumber("count(//*:observationMember[*:OM_Observation])").intValue();
		if (index >= count) {
		    nextResumptionToken = null;
		} else {
		    nextResumptionToken = StationType.METEO.name() + (index + 1);
		}
		break;
	    default:
		break;
	    }

	    if (!getSetting().isMaxRecordsUnlimited()) {

		Optional<Integer> mr = getSetting().getMaxRecords();

		if (mr.isPresent() && index >= mr.get()) {

		    GSLoggerFactory.getLogger(getClass()).info("Reached max records of {}", mr.get());

		    nextResumptionToken = null;

		    return ret;

		}
	    }

	    ret.setResumptionToken(nextResumptionToken);

	    XMLDocumentReader retReader = new XMLDocumentReader("<" + SAVA_HIS_ROOT + "></" + SAVA_HIS_ROOT + ">");
	    XMLDocumentWriter retWriter = new XMLDocumentWriter(retReader);

	    retWriter.addNode("/*[1]", seriesNode);

	    XMLNodeReader seriesReader = new XMLNodeReader(seriesNode);
	    String station = seriesReader.evaluateString("*:OM_Observation/*:observedProperty/@MonitoringPoint");
	    if (station != null) {
		XMLDocumentReader tmp = null;
		String tag = null;
		switch (stationType) {
		case HYDRO:
		    tmp = hydroFeatures;
		    tag = "european_wgst_code";
		    break;
		case METEO:
		    tmp = meteoFeatures;
		    tag = "european_ptst_code";
		    break;
		}
		Node featureNode = tmp.evaluateNode("//*:featureMember[*/*:" + tag + "='" + station + "']");
		if (featureNode != null) {
		    retWriter.addNode("/*[1]", featureNode);
		}
	    }
	    // XMLNodeReader featureReader = new XMLNodeReader(featureNode);

	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setMetadata(retReader.asString());
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.SAVAHIS_URI);
	    ret.addRecord(metadataRecord);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SAVA_HIS_CONNECTOR_LIST_RECORDS_ERROR, //
		    e);
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(CommonNameSpaceContext.SAVAHIS_URI);
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	return false;
    }

    private void retrieveCapabilities() throws Exception {

	if (hydroFeatures != null && meteoFeatures != null) {
	    return;
	}

	String endpoint = getSourceURL().trim(); // es. http://savahis.org/his/waterml?

	if (!endpoint.endsWith("?")) {
	    endpoint = endpoint + "?";
	}

	// the hydrology station description through WFS
	String hydroWFSEndpoint = endpoint.replace("his/waterml?",
		"wfs?service=WFS&version=1.1.0&request=GetFeature&typeName=monitoring_point_h");
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (1/6): " + hydroWFSEndpoint);
	if (hydroFeatures == null) {
	    hydroFeatures = downloadDom(hydroWFSEndpoint);
	}

	// big WML info file, containing all the correct times for single time series
	String hydroEndpoint = endpoint + "monitoringType=004"; // hydrology stations
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (2/6): " + hydroEndpoint);
	Iterator<XMLDocumentReader> hydroMultipleSeries = downloadStax(hydroEndpoint, "observationMember");

	// smaller WML info file
	hydroEndpoint = hydroEndpoint + "&singleSerie=true"; // single series!
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (3/6): " + hydroEndpoint);
	if (hydroSingleSeries == null) {
	    hydroSingleSeries = downloadDom(hydroEndpoint);
	}

	// adjusting hydro time begin and time end
	augmentFeatures(hydroSingleSeries, hydroMultipleSeries);

	// the meteo station description through WFS
	String meteoWFSEndpoint = endpoint.replace("his/waterml?",
		"wfs?service=WFS&version=1.1.0&request=GetFeature&typeName=monitoring_point_m");
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (4/6): " + meteoWFSEndpoint);
	if (meteoFeatures == null) {
	    meteoFeatures = downloadDom(meteoWFSEndpoint);
	}

	// big WML info file, containing all the correct times for single time series
	String meteoEndpoint = endpoint + "monitoringType=003"; // meteorology stations
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (5/6): " + meteoEndpoint);
	Iterator<XMLDocumentReader> meteoMultipleSeries = downloadStax(meteoEndpoint, "observationMember");

	// smaller WML info file
	meteoEndpoint = meteoEndpoint + "&singleSerie=true"; // single series!
	GSLoggerFactory.getLogger(getClass()).info("Downloading Sava HIS catalogue (6/6): " + hydroEndpoint);
	if (meteoSingleSeries == null) {
	    meteoSingleSeries = downloadDom(meteoEndpoint);
	}

	// adjusting meteo time begin and time end
	augmentFeatures(meteoSingleSeries, meteoMultipleSeries);
    }

    private void augmentFeatures(XMLDocumentReader singleSeries, Iterator<XMLDocumentReader> multipleSeries)
	    throws XPathExpressionException {

	XMLDocumentWriter writer = new XMLDocumentWriter(singleSeries);
	writer.setText("//*:beginPosition", null);
	writer.setText("//*:endPosition", null);
	long i = 0;
	HashMap<String, SimpleEntry<Date, Date>> datesMap = new HashMap<>();

	while (multipleSeries.hasNext()) {
	    i++;
	    if (i % 1000 == 0) {
		GSLoggerFactory.getLogger(getClass()).info("Augmented with " + i + " multiple series");
	    }
	    // if (i % 10000 == 0) {
	    // GSLoggerFactory.getLogger(getClass()).info("Augmented with " + i + " multiple series");
	    // break;
	    // }
	    XMLDocumentReader actualSeries = multipleSeries.next();
	    String stationCode = actualSeries.evaluateString("*:observationMember/*:OM_Observation/*:observedProperty/@MonitoringPoint");
	    String property = actualSeries.evaluateString("*:observationMember/*:OM_Observation/*:observedProperty/@ObservedProperty");
	    String beginPosition = actualSeries
		    .evaluateString("*:observationMember/*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:beginPosition");
	    String endPosition = actualSeries
		    .evaluateString("*:observationMember/*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:endPosition");
	    Date actualBeginDate = parseDate(beginPosition);
	    Date actualEndDate = parseDate(endPosition);

	    String key = stationCode + "-" + property;
	    SimpleEntry<Date, Date> dates = datesMap.get(key);
	    Date currentBeginDate = null;
	    Date currentEndDate = null;
	    if (dates != null) {
		currentBeginDate = dates.getKey();
		currentEndDate = dates.getValue();
	    }

	    if (currentBeginDate == null || currentBeginDate.after(actualBeginDate)) {
		currentBeginDate = actualBeginDate;
	    }
	    if (currentEndDate == null || currentEndDate.before(actualEndDate)) {
		currentEndDate = actualEndDate;
	    }

	    datesMap.put(key, new SimpleEntry<Date, Date>(currentBeginDate, currentEndDate));
	}

	int size = datesMap.size();

	Node[] observations = singleSeries.evaluateNodes("//*:OM_Observation");

	List<Node> toRemove = new ArrayList<>();

	for (int j = 0; j < observations.length; j++) {
	    if (j % 100 == 0) {
		GSLoggerFactory.getLogger(getClass()).info("Augmented " + j + "/" + size + " single series");
	    }
	    XMLNodeReader observationReader = new XMLNodeReader(observations[j]);
	    XMLNodeWriter observationWriter = new XMLNodeWriter(observationReader);
	    String station = observationReader.evaluateString("*:observedProperty/@MonitoringPoint");
	    String property = observationReader.evaluateString("*:observedProperty/@ObservedProperty");
	    String key = station + "-" + property;
	    SimpleEntry<Date, Date> dates = datesMap.get(key);
	    if (dates != null) {
		Date beginDate = dates.getKey();
		String begin = null;
		if (beginDate != null) {
		    begin = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
		}
		Date endDate = dates.getValue();
		String end = null;
		if (endDate != null) {
		    end = ISO8601DateTimeUtils.getISO8601DateTime(endDate);
		}
		if (begin != null && end != null && !begin.equals(end)) {
		    observationWriter.setText("*:phenomenonTime/*:TimePeriod/*:beginPosition", begin);
		    observationWriter.setText("*:phenomenonTime/*:TimePeriod/*:endPosition", end);
		} else {
		    toRemove.add(observations[j]);
		}
	    } else {
		toRemove.add(observations[j]);
	    }
	}

	for (Node node : toRemove) {
	    node.getParentNode().removeChild(node);
	}

    }

    private Date parseDate(String dateString) {

	if (dateString != null && !dateString.equals("")) {
	    Optional<Date> dateOptional = ISO8601DateTimeUtils.parseISO8601ToDate(dateString.replace(" ", "T"));
	    if (dateOptional.isPresent()) {
		Date date = dateOptional.get();
		return date;
	    }
	}
	return null;
    }

    private Iterator<XMLDocumentReader> downloadStax(String url, String elementLocalName) throws Exception {
	File tmpFile = downloadFile(url);

	FileInputStream fis = new FileInputStream(tmpFile);
	StAXDocumentIterator reader = new StAXDocumentIterator(fis, elementLocalName);
	return new Iterator<XMLDocumentReader>() {

	    @Override
	    public XMLDocumentReader next() {
		XMLDocumentReader ret = reader.next();
		return ret;
	    }

	    @Override
	    public boolean hasNext() {
		boolean ret = reader.hasNext();
		if (!ret) {
		    try {
			GSLoggerFactory.getLogger(getClass()).info("Closing list stream");
			fis.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		    GSLoggerFactory.getLogger(getClass()).info("Deleting temporary list file: " + tmpFile);
		    tmpFile.delete();
		}
		return ret;
	    }
	};

    }

    private File downloadFile(String url) throws Exception {
	InputStream output = downloadStreamWithRetry(url);
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	GSLoggerFactory.getLogger(getClass()).info("Downloading XML document to : " + tmpFile.getAbsolutePath());
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(output, fos);
	output.close();
	fos.close();
	GSLoggerFactory.getLogger(getClass()).info("Downloaded XML document. Size: " + tmpFile.length() + " bytes");
	return tmpFile;
    }

    private InputStream downloadStreamWithRetry(String url) throws Exception {
	for (int i = 0; i < 10; i++) {

	    Downloader httpRequestExecutor = new Downloader();
	    httpRequestExecutor.setConnectionTimeout(TimeUnit.MINUTES, 2);

	    HttpResponse<InputStream> response = httpRequestExecutor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url.trim()));
	    if (response.statusCode() == 200) {
		InputStream stream = response.body();
		return stream;
	    }
	}
	return null;
    }

    private XMLDocumentReader downloadDom(String url) throws Exception {
	InputStream output = downloadStreamWithRetry(url);
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	GSLoggerFactory.getLogger(getClass()).info("Downloading XML document to : " + tmpFile.getAbsolutePath());
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(output, fos);
	output.close();
	fos.close();
	GSLoggerFactory.getLogger(getClass()).info("Downloaded XML document. Size: " + tmpFile.length() + " bytes");
	XMLDocumentReader reader = new XMLDocumentReader(tmpFile);
	return reader;

    }

    public static void main(String[] args) throws GSException {
	SavaHISConnector connector = new SavaHISConnector();
	connector.setSourceURL("http://savahis.org/his/waterml?");
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	OriginalMetadata record = response.getRecords().next();
	String metadata = record.getMetadata();
	System.out.println(metadata);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SavaHISConnectorSetting initSetting() {

	return new SavaHISConnectorSetting();
    }

}
