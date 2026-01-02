package eu.essi_lab.accessor.ina;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wof.CUAHSIHISServerConnector;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.datamodel.Site;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesINAResponseDocument;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class INAConnector extends CUAHSIHISServerConnector<INAConnectorSetting> {

    private Downloader downloader;

    // RICHER (e.g. email address) but SLOWER
    private static final boolean ENRICH_TIME_SERIES = false;

    private CUAHSIHISServerClient client = new CUAHSIHISServerClient1_1(getSourceURL());

    private Iterator<SiteInfo> siteInfoIterator = null;

    private SiteInfo siteInfo = null;

    private Integer siteNumber = null;

    private SimpleDateFormat iso8601OutputFormat = null;

    private SimpleDateFormat iso8601InputFormat = null;

    private SitesResponseDocument richerSiteInfoDocument = null;

    private SitesResponseDocument INASiteInfoDocument = null;

    private List<String> sitesWithErrors = new ArrayList<>();

    static final String INA_CONNECTOR_ERROR = "INA_CONNECTOR_ERROR";

    /**
     * 
     */
    public static final String TYPE = "INAConnector";

    private Integer recordsReturned = 0;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    public INAConnector() {
	super();
	this.downloader = new Downloader();
	this.iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	this.iso8601InputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	iso8601InputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();
	if (baseEndpoint == null) {
	    return false;
	}
	String request = baseEndpoint.endsWith("/") ? baseEndpoint + "/GetSites?" : baseEndpoint + "/GetSites?";
	Optional<InputStream> res = getDownloader().downloadOptionalStream(request);
	if (res.isPresent())
	    return true;

	return false;
    }

    protected SimpleEntry<Integer, Integer> decodeDoubleResumptionToken(String id) throws GSException {

	String[] split = id.split(":");

	Integer sites = Integer.parseInt(split[0]);
	Integer series = Integer.parseInt(split[1]);

	SimpleEntry<Integer, Integer> ret = new SimpleEntry<Integer, Integer>(sites, series);
	return ret;

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {
	// TODO Auto-generated method stub

	// getsites
	if (INASiteInfoDocument == null || INASiteInfoDocument.getSites().isEmpty()) {
	    INASiteInfoDocument = getSites();
	    this.siteInfoIterator = INASiteInfoDocument.getSitesInfo().iterator();
	    siteNumber = -1;
	}

	if (INASiteInfoDocument == null)
	    return null;

	String id = listRecords.getResumptionToken();

	TimeSeries timeSeries = null;

	String nextId = null;

	Integer siteNumber;
	Integer seriesNumber;

	String siteName = "";
	String seriesName = "";

	if (id == null) {
	    siteNumber = 0;
	    seriesNumber = 0;
	} else {
	    SimpleEntry<Integer, Integer> decodedResumptionToken = decodeDoubleResumptionToken(id);
	    siteNumber = decodedResumptionToken.getKey();
	    seriesNumber = decodedResumptionToken.getValue();
	}
	SiteInfo siteInfo = null;
	if (this.siteNumber == siteNumber && this.siteInfo != null) {
	    // nothing to do
	} else {
	    // let's scan until the requested site is reached
	    while (this.siteNumber < siteNumber) {
		if (siteInfoIterator.hasNext()) {
		    this.siteInfo = siteInfoIterator.next();
		    this.siteNumber++;
		} else {
		    ErrorInfo ei = new ErrorInfo();

		    ei.setErrorDescription("Unable to resume from resumption token: " + id);
		    ei.setCaller(this.getClass());
		    ei.setErrorId(INA_CONNECTOR_ERROR);
		    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

		    throw GSException.createException(ei);
		}
	    }
	}

	siteInfo = this.siteInfo;

	// this is a cache for a site info
	if (richerSiteInfoDocument == null || richerSiteInfoDocument.getSitesInfo().isEmpty()
		|| !richerSiteInfoDocument.getSitesInfo().get(0).getSiteCodeNetwork().equals(siteInfo.getSiteCodeNetwork())
		|| !richerSiteInfoDocument.getSitesInfo().get(0).getSiteCode().equals(siteInfo.getSiteCode())) {
	    // we perform a get site info to obtain a richer site info document and we put
	    // it in cache to drastically
	    // reduce get site info requests
	    String siteCodeNetwork = siteInfo.getSiteCodeNetwork();
	    String siteCode = siteInfo.getSiteCode();
	    try {
		this.richerSiteInfoDocument = getSiteInfo(siteCode);
	    } catch (Exception e) {
		this.sitesWithErrors.add(siteCodeNetwork + ":" + siteCode);
		// the site info call returned errors. this may happen,
		// e.g. for the service at http://data.envirodiy.org/wofpy/soap/cuahsi_1_1/.wsdl
		// where the site envirodiy:160065_Limno_Crossroads is available, while
		// envirodiy:MM_001 gives error
		ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
		// in this case we skip to next site
		seriesNumber = 0;
		siteNumber++;
		nextId = siteNumber + ":" + seriesNumber;
		if (!siteInfoIterator.hasNext()) {
		    // but if the last site is fully visited no next records are available
		    nextId = null;
		}
		ret.setResumptionToken(nextId);
		return ret;
	    }

	}

	siteInfo = richerSiteInfoDocument.getSitesInfo().get(0);
	List<Site> sites = richerSiteInfoDocument.getSites();
	if (!sites.isEmpty()) {
	    Site site = sites.get(0);
	    site.setSeriesCatalogWSDL(getSourceURL());
	}
	siteName = siteInfo.getSiteName();
	List<TimeSeries> seriesCatalog = siteInfo.getSeries();
	if (seriesNumber == 0 && seriesCatalog.isEmpty()) {
	    seriesNumber = 0;
	    siteNumber++;
	    nextId = siteNumber + ":" + seriesNumber;
	    if (!siteInfoIterator.hasNext()) {
		// but if the last site is fully visited no next records are available
		nextId = null;
	    }
	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	    ret.setResumptionToken(nextId);
	    return ret;
	}
	if (seriesNumber < seriesCatalog.size()) {
	    // we select the time series
	    timeSeries = seriesCatalog.get(seriesNumber);

	    seriesName = timeSeries.getVariableName();
	    // also the time series is enriched at maximum level (included contact
	    // information such as email address)
	    if (ENRICH_TIME_SERIES) {
		String variableCode = timeSeries.getVariableCode();
		String methodId = timeSeries.getMethodId();
		String qualityControlLevelCode = timeSeries.getQualityControlLevelCode();
		String sourceId = timeSeries.getSourceId();
		try {
		    timeSeries = client.getAugmentedTimeSeries(siteInfo, variableCode, methodId, qualityControlLevelCode, sourceId);
		} catch (GSException e) {
		    // in case an exception occurred, no metadata is returned for this series
		    GSLoggerFactory.getLogger(this.getClass()).error("Remote server error augmenting time series. ");
		    GSLoggerFactory.getLogger(this.getClass()).error("URL: {}", getSourceURL());
		    GSLoggerFactory.getLogger(this.getClass()).error("Site: {}", siteInfo);
		    GSLoggerFactory.getLogger(this.getClass()).error("Variable: {}", variableCode);
		}
	    }

	    // the next (not duplicated) time series from current site is selected as next

	    boolean duplicated = false;
	    do {
		seriesNumber++;

		if (seriesNumber < seriesCatalog.size()) {

		    TimeSeries nextSeries = seriesCatalog.get(seriesNumber);
		    String nextSeriesVariableCode = nextSeries.getVariableCode();
		    duplicated = false;
		    for (int i = 0; i < seriesNumber; i++) {
			String visitedVariableCode = seriesCatalog.get(i).getVariableCode();
			// this is the case of a time series with duplicated variable code
			if (nextSeriesVariableCode.equals(visitedVariableCode)) {
			    duplicated = true;
			    GSLoggerFactory.getLogger(this.getClass()).warn("Found duplicated variable code, skipping: {}",
				    visitedVariableCode);
			    break;
			}
		    }
		}

	    } while (duplicated && seriesNumber < seriesCatalog.size());

	    nextId = siteNumber + ":" + seriesNumber;
	    if (seriesNumber >= seriesCatalog.size()) {
		// if we are at the last time series of the site then
		// the next time series is the first series from the next site
		seriesNumber = 0;
		siteNumber++;
		nextId = siteNumber + ":" + seriesNumber;
		if (!siteInfoIterator.hasNext()) {
		    // but if the last site is fully visited no next records are available
		    nextId = null;
		}
	    }
	    if (isFirstSiteOnly() && siteNumber > 0) {
		nextId = null;
	    }

	} else {
	    ErrorInfo ei = new ErrorInfo();

	    ei.setErrorDescription("Unable to resume from resumption token: " + id);
	    ei.setCaller(this.getClass());
	    ei.setErrorId(INA_CONNECTOR_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(ei);
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	OriginalMetadata metadataRecord = new OriginalMetadata();
	metadataRecord.setSchemeURI(listMetadataFormats().get(0));
	try {

	    SitesResponseDocument emptySRD = getEmptyResponseDocument(richerSiteInfoDocument);

	    emptySRD.getSites().get(0).addSeries(timeSeries);

	    String metadata = emptySRD.getReader().asString();
	    metadataRecord.setMetadata(metadata);
	    ret.addRecord(metadataRecord);
	    recordsReturned++;

	    if (!getSetting().isMaxRecordsUnlimited()) {

		Optional<Integer> mr = getSetting().getMaxRecords();

		if (mr.isPresent() && recordsReturned >= mr.get()) {

		    GSLoggerFactory.getLogger(this.getClass()).info("Reached max records of {}", mr.get());

		    ret.setResumptionToken(null);

		    return ret;

		}
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    ErrorInfo ei = new ErrorInfo();

	    ei.setErrorDescription("Error marshalling metadata for site: " + siteName + " variable name: " + seriesName);
	    ei.setCaller(this.getClass());
	    ei.setErrorId(INA_CONNECTOR_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(ei);
	}
	ret.setResumptionToken(nextId);
	return ret;

    }

    public SitesResponseDocument getSiteInfo(String siteCode) {
	SitesResponseDocument sites = null;
	String url = getSourceURL();
	String request = url.endsWith("/") ? url + "GetSiteInfo?site=" + siteCode : url + "/GetSiteInfo?site=" + siteCode;
	GSLoggerFactory.getLogger(this.getClass()).info("Get SitesInfo for INA Accessor {}", request);
	try {
	    Optional<HttpResponse<InputStream>> response = getDownloader().downloadOptionalResponse(request);
	    if (response.isPresent()) {
		sites = new SitesResponseDocument(response.get().body());
	    }
	} catch (SAXException | IOException e) {
	    // in case an exception occurred, no metadata is returned for this series
	    GSLoggerFactory.getLogger(this.getClass()).error("Remote server error getting remote sites.");
	    GSLoggerFactory.getLogger(this.getClass()).error("URL: {}", request);
	    return null;
	}

	return sites;
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.WML1_INA_NS_URI);
	return ret;
    }

    public SitesResponseDocument getSites() {
	SitesResponseDocument sites = null;
	String url = getSourceURL();
	String request = url.endsWith("/") ? url + "GetSites?" : url + "/GetSites?";
	GSLoggerFactory.getLogger(this.getClass()).info("Get Sites for INA Accessor {}", request);
	try {
	    Optional<InputStream> stream = getDownloader().downloadOptionalStream(request);
	    if (stream.isPresent()) {
		sites = new SitesResponseDocument(stream.get());
	    }
	} catch (SAXException | IOException e) {
	    // in case an exception occurred, no metadata is returned for this series
	    GSLoggerFactory.getLogger(this.getClass()).error("Remote server error getting remote sites.");
	    GSLoggerFactory.getLogger(this.getClass()).error("URL: {}", request);
	    return null;

	}

	return sites;

    }

    public TimeSeriesINAResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId, Date begin, Date end) throws GSException {

	// first, we must retrieve timezone of the hydro server
	SitesResponseDocument siteInfo = getSiteInfo(siteCode);

	if (siteInfo == null) {

	    GSLoggerFactory.getLogger(getClass()).error("Siteinfo null for siteCode: {}", siteCode);

	    ErrorInfo ei = new ErrorInfo();
	    ei.setCaller(this.getClass());
	    ei.setErrorDescription("Siteinfo null for siteCode: " + siteCode);
	    ei.setErrorId(INA_CONNECTOR_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(ei);
	}

	TimeSeries variable = siteInfo.getSitesInfo().get(0).getSeries(variableCode, methodId, qualityControlLevelCode, sourceId);
	String t1 = variable.getBeginTimePosition();
	String t1UTC = variable.getBeginTimePositionUTC();

	try {
	    boolean t1Present = true;
	    boolean t1UTCPresent = true;
	    if (t1 == null || t1.equals("") || t1.equals("0000-00-00T00:00:00")) {
		t1 = null;
		t1Present = false;
	    }
	    if (t1UTC == null || t1UTC.equals("") || t1UTC.equals("0000-00-00T00:00:00")) {
		t1UTC = null;
		t1UTCPresent = false;
	    }
	    String timeBeginString = null;
	    String timeEndString = null;
	    if (t1Present && t1UTCPresent) {
		Date beginDate = iso8601InputFormat.parse(t1);
		Date beginDateUTC = iso8601InputFormat.parse(t1UTC);
		long beginMilliseconds = beginDate.getTime();
		long beginMillisecondsUTC = beginDateUTC.getTime();
		long difference = beginMilliseconds - beginMillisecondsUTC;
		long millisecsDifference = difference;
		timeBeginString = getLocalTimeString(begin, millisecsDifference);
		timeEndString = getLocalTimeString(end, millisecsDifference);
	    }
	    if (t1UTCPresent) {
		// then, we redirect to the usual method
		timeBeginString = getUTCTimeString(begin);
		timeEndString = getUTCTimeString(end);
	    }

	    TimeSeriesINAResponseDocument res = null;
	    String url = getSourceURL();
	    // e.g.
	    // https://alerta.ina.gob.ar/wml/GetValues?site=1072&variable=4&startDate=2018-03-19T21:00&endDate=2018-03-21T08:00
	    String request = url.endsWith("/") ? url + "GetValues?site=" + siteCode + "&variable=" + variableCode
		    : url + "/GetValues?site=" + siteCode + "&variable=" + variableCode;

	    if (timeBeginString != null) {
		request += "&startDate=" + timeBeginString;
	    }

	    if (timeEndString != null) {
		request += "&endDate=" + timeEndString;
	    }

	    Optional<InputStream> inputRes = getDownloader().downloadOptionalStream(request);
	    if (inputRes.isPresent()) {
		res = new TimeSeriesINAResponseDocument(inputRes.get());
	    }

	    res.reduceValues(methodId, qualityControlLevelCode, sourceId);

	    return res;

	} catch (Exception e) {

	    ErrorInfo ei = new ErrorInfo();
	    ei.setCaller(this.getClass());
	    ei.setErrorDescription("Remote server fault: Time zone not available");
	    ei.setErrorId(INA_CONNECTOR_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(ei);
	}
    }

    private String getUTCTimeString(Date begin) {
	return iso8601OutputFormat.format(begin.getTime());
    }

    private String getLocalTimeString(Date begin, long millisecsDifference) {

	long newTime = begin.getTime() + millisecsDifference;

	return iso8601OutputFormat.format(new Date(newTime));
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected INAConnectorSetting initSetting() {

	return new INAConnectorSetting();
    }
}
