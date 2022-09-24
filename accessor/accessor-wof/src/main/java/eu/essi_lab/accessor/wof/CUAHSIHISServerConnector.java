package eu.essi_lab.accessor.wof;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.datamodel.Site;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.setting.CUAHSIHIServerConnectorSetting;
import eu.essi_lab.cdk.harvest.FirstSiteConnectorSetting;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class CUAHSIHISServerConnector<C extends FirstSiteConnectorSetting> extends HarvestedQueryConnector<C> {

    // RICHER (e.g. email address) but SLOWER
    private static final boolean ENRICH_TIME_SERIES = false;

    /**
     * 
     */
    public static final String TYPE = "CUAHSIHISServerConnector";

    private static final String CUAHSI_HIS_SERVER_CONNECTOR_ERROR = "CUAHSI_HIS_SERVER_CONNECTOR_ERROR";

    private CUAHSIHISServerClient client = null;

    private Iterator<SiteInfo> siteInfoIterator = null;

    private SiteInfo cachedSiteInfo = null;

    private Integer cachedSiteNumber = null;

    private SitesResponseDocument richerSiteInfoDocument = null;

    private List<String> sitesWithErrors = new ArrayList<>();

    private Integer recordsReturned = 0;

    public List<String> getSiteWithErrors() {
	return sitesWithErrors;
    }

    /**
     * 
     */
    public CUAHSIHISServerConnector() {

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	if (client == null) {
	    if (getSourceURL().contains("cuahsi_1_0.asmx")) {
		// not supported at this time
		ErrorInfo info = new ErrorInfo();
		info.setErrorDescription("Unsupported HIS server version: 1.0");
		info.setCaller(this.getClass());
		info.setErrorId(CUAHSI_HIS_SERVER_CONNECTOR_ERROR);
		info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		info.setSeverity(ErrorInfo.SEVERITY_ERROR);

		throw GSException.createException(info);
	    } else {
		client = new CUAHSIHISServerClient1_1(getSourceURL());
	    }
	}

	if (siteInfoIterator == null) {
	    try {
		siteInfoIterator = client.getSitesObjectStAX();
	    } catch (Exception e) {
		// here we try to resume with the getSites method, as some HIS server
		// doesn't implement the getSitesObject method, such as
		// https://hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_FORA_002.cgi?WSDL
		GSLoggerFactory.getLogger(this.getClass())
			.warn("GetSitesObject method returned error, trying to resume with the GetSites method");
		siteInfoIterator = client.getSitesSTaX();
	    }
	    cachedSiteNumber = -1;
	}
	String id = listRecords.getResumptionToken();
	/*
	 * The resumption token for HydroServerConnector is in the form:
	 * SITE_NUMBER:SERIES_NUMBER
	 * where:
	 * SITE_NUMBER = the progressive number of the site as it appears on the sites response document
	 * SERIES_NUMBER = the progressive number of the series as it appears on the site info response document
	 */

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
	    SimpleEntry<Integer, Integer> decodedResumptionToken = decodeResumptionToken(id);
	    siteNumber = decodedResumptionToken.getKey();
	    seriesNumber = decodedResumptionToken.getValue();
	}

	// we select the site

	SiteInfo siteInfo = null;
	if (this.cachedSiteNumber.equals(siteNumber) && this.cachedSiteInfo != null) {
	    // nothing to do
	} else {
	    // let's scan until the requested site is reached
	    while (this.cachedSiteNumber < siteNumber) {
		if (siteInfoIterator.hasNext()) {
		    this.cachedSiteInfo = siteInfoIterator.next();
		    this.cachedSiteNumber++;
		} else {
		    ErrorInfo info = new ErrorInfo();
		    info.setErrorDescription(getUnableToResumeError(id));
		    info.setCaller(this.getClass());
		    info.setErrorId(CUAHSI_HIS_SERVER_CONNECTOR_ERROR);
		    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

		    throw GSException.createException(info);
		}
	    }
	}

	siteInfo = this.cachedSiteInfo;

	// this is a cache for a site info
	if (richerSiteInfoDocument == null || richerSiteInfoDocument.getSitesInfo().isEmpty()
		|| !richerSiteInfoDocument.getSitesInfo().get(0).getSiteCodeNetwork().equals(siteInfo.getSiteCodeNetwork())
		|| !richerSiteInfoDocument.getSitesInfo().get(0).getSiteCode().equals(siteInfo.getSiteCode())) {
	    // we perform a get site info to obtain a richer site info document and we put it in cache to drastically
	    // reduce get site info requests
	    String siteCodeNetwork = siteInfo.getSiteCodeNetwork();
	    String siteCode = siteInfo.getSiteCode();
	    try {
		this.richerSiteInfoDocument = client.getSiteInfo(siteCodeNetwork, siteCode);
	    } catch (GSException e) {
		this.sitesWithErrors.add(siteCodeNetwork + ":" + siteCode);
		// the site info call returned errors. this may happen,
		// e.g. for the service at http://data.envirodiy.org/wofpy/soap/cuahsi_1_1/.wsdl
		// where the site envirodiy:160065_Limno_Crossroads is available, while envirodiy:MM_001 gives error
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
	    // also the time series is enriched at maximum level (included contact information such as email address)
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
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription(getUnableToResumeError(id));
	    info.setCaller(this.getClass());
	    info.setErrorId(CUAHSI_HIS_SERVER_CONNECTOR_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(info);
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
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription("Error marshalling metadata for site: " + siteName + " variable name: " + seriesName);
	    info.setCaller(this.getClass());
	    info.setErrorId(CUAHSI_HIS_SERVER_CONNECTOR_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(info);
	}
	ret.setResumptionToken(nextId);
	return ret;

    }

    /**
     * Returns a sites response document without series
     *
     * @param richerSiteInfoDocument
     * @return
     * @throws TransformerException
     * @throws IOException
     * @throws SAXException
     * @throws GSException
     * @throws Exception
     */
    protected SitesResponseDocument getEmptyResponseDocument(SitesResponseDocument richerSiteInfoDocument)
	    throws SAXException, IOException, TransformerException, GSException {
	SitesResponseDocument ret = new SitesResponseDocument(richerSiteInfoDocument.getReader().asStream());
	ret.getSites().get(0).clearSeries();
	return ret;
    }

    protected SimpleEntry<Integer, Integer> decodeResumptionToken(String id) throws GSException {
	if (!id.contains(":")) {
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription(getUnableToResumeError(id));
	    throw GSException.createException(info);
	}
	String[] split = id.split(":");
	try {
	    Integer sites = Integer.parseInt(split[0]);
	    Integer series = Integer.parseInt(split[1]);

	    if (sites < 0 || series < 0) {
		ErrorInfo info = new ErrorInfo();
		info.setErrorDescription(getUnableToResumeError(id));
		info.setCaller(this.getClass());
		info.setErrorId(CUAHSI_HIS_SERVER_CONNECTOR_ERROR);
		info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		info.setSeverity(ErrorInfo.SEVERITY_ERROR);

		throw GSException.createException(info);
	    }

	    SimpleEntry<Integer, Integer> ret = new SimpleEntry<Integer, Integer>(sites, series);
	    return ret;
	} catch (NumberFormatException e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CUAHSI_HIS_SERVER_CONNECTOR_ERROR, //
		    e);
	}
    }

    private String getUnableToResumeError(String id) {
	return "Unable to resume from resumption token: " + id;
    }

    /**
     * @param firstSiteOnly
     */
    public void setFirstSiteOnly(Boolean firstSiteOnly) {

	((FirstSiteConnectorSetting) getSetting()).setHarvestFirstSiteOnly(firstSiteOnly);
    }

    protected boolean isFirstSiteOnly() {

	return ((FirstSiteConnectorSetting) getSetting()).isFirstSiteHarvestOnlySet();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.WML1_NS_URI);
	return ret;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();
	if (baseEndpoint == null) {
	    return false;
	}

	try {
	    CUAHSIHISServerClient testClient;
	    if (baseEndpoint.contains("cuahsi_1_0.asmx")) {
		// unsupported at this time
		return false;
	    } else {
		testClient = new CUAHSIHISServerClient1_1(baseEndpoint);
	    }
	    if (hasSites(testClient)) {
		return true;
	    }

	} catch (Exception e) {
	    // any exception during download or during XML parsing
	    String warn = "Exception during download or during XML parsing: " + e.getMessage();
	    GSLoggerFactory.getLogger(this.getClass()).warn(warn);
	}
	return false;
    }

    private boolean hasSites(CUAHSIHISServerClient testClient) throws GSException {
	try {
	    Iterator<SiteInfo> sites = testClient.getSitesObjectStAX();
	    if (sites.hasNext()) {
		return true;
	    }
	} catch (Exception e) {
	    Iterator<SiteInfo> sites = testClient.getSitesSTaX();
	    if (sites.hasNext()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected C initSetting() {

	return (C) new CUAHSIHIServerConnectorSetting();
    }
}
