
package eu.essi_lab.accessor.wof.client;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;

import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.stax.StAXDocumentIterator;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public abstract class CUAHSIHISServerClient {

    Logger logger = GSLoggerFactory.getLogger(this.getClass());

    static final String REMOTE_SERVER_ERROR = null;

    protected String endpoint;

    private SimpleDateFormat iso8601OutputFormat;

    /*
     * CONSTRUCTOR
     */

    protected CUAHSIHISServerClient(String endpoint) {
	this.endpoint = endpoint;
	this.iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /*
     * MAIN METHODS
     */

    /**
     * Asks the server for all the sites, replying with a site response document containing all of them
     * 
     * @return
     * @throws GSException
     */
    public Iterator<SiteInfo> getSites() throws GSException {

	SOAPExecutorDOM executor = new SOAPExecutorDOM(endpoint);

	executor.setSOAPAction(getGetSitesSOAPAction());

	InputStream input = null;
	try {
	    input = loadGetSitesTemplate();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	executor.setResultPath(getGetSitesResultXPath());

	XMLDocumentReader reader = executor.execute();

	SitesResponseDocument ret = new SitesResponseDocument(reader.getDocument());

	return ret.getSitesInfo().iterator();

    }

    /**
     * Asks the server for all the sites, replying with a site response document containing all of them, to be read
     * using StAX parser (useful for very large site lists).
     * 
     * @return
     * @throws GSException
     */
    public Iterator<SiteInfo> getSitesSTaX() throws GSException {

	SOAPExecutorStAX executor = new SOAPExecutorStAX(endpoint);

	executor.setSOAPAction(getGetSitesSOAPAction());

	InputStream input = null;
	try {
	    input = loadGetSitesTemplate();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	File tmpFile = executor.execute();
	tmpFile.deleteOnExit();

	try {
	    FileInputStream fis = new FileInputStream(tmpFile);
	    StAXDocumentIterator reader = new StAXDocumentIterator(fis, "siteInfo");

	    return new Iterator<SiteInfo>() {

		@Override
		public SiteInfo next() {
		    XMLDocumentReader ret = reader.next();
		    return new SiteInfo(ret.getDocument().getDocumentElement());
		}

		@Override
		public boolean hasNext() {
		    boolean ret = reader.hasNext();
		    if (!ret) {
			try {
			    logger.info("Closing site list stream");
			    fis.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
			logger.info("Deleting temporary site list file: " + tmpFile);
			tmpFile.delete();
		    }
		    return ret;
		}
	    };

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    "Unable to complete XML file download", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CUAHSIHISServerClient.REMOTE_SERVER_ERROR, //
		    e);
	}
    }

    /**
     * Asks the server for all the sites, replying with a site response document containing all of them, to be parsed
     * with DOM. Note: for very large site lists use the StAX implementation
     * 
     * @return
     * @throws GSException
     */
    public Iterator<SiteInfo> getSitesObject() throws GSException {

	SOAPExecutorDOM executor = new SOAPExecutorDOM(endpoint);

	executor.setSOAPAction(getGetSitesObjectSOAPAction());

	InputStream input = null;
	try {
	    input = loadGetSitesObjectTemplate();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	executor.setResultPath(getGetSitesObjectResultXPath());

	XMLDocumentReader reader = executor.execute();

	SitesResponseDocument ret = new SitesResponseDocument(reader.getDocument());

	return ret.getSitesInfo().iterator();

    }

    /**
     * Asks the server for all the sites, replying with a site response document containing all of them, to be read
     * using StAX parser (useful for very large site lists).
     * 
     * @return
     * @throws GSException
     */
    public Iterator<SiteInfo> getSitesObjectStAX() throws GSException {

	SOAPExecutorStAX executor = new SOAPExecutorStAX(endpoint);

	executor.setSOAPAction(getGetSitesObjectSOAPAction());

	InputStream input = null;
	try {
	    input = loadGetSitesObjectTemplate();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	File tmpFile = executor.execute();
	tmpFile.deleteOnExit();

	try {
	    FileInputStream fis = new FileInputStream(tmpFile);
	    StAXDocumentIterator reader = new StAXDocumentIterator(fis, "siteInfo");

	    return new Iterator<SiteInfo>() {

		@Override
		public SiteInfo next() {
		    XMLDocumentReader ret = reader.next();
		    return new SiteInfo(ret.getDocument().getDocumentElement());
		}

		@Override
		public boolean hasNext() {
		    boolean ret = reader.hasNext();
		    if (!ret) {
			try {
			    logger.info("Closing site list stream");
			    fis.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
			logger.info("Deleting temporary site list file: " + tmpFile);
			tmpFile.delete();
		    }
		    return ret;
		}
	    };

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    "Unable to complete XML file download", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CUAHSIHISServerClient.REMOTE_SERVER_ERROR, //
		    e);
	}
    }

    /**
     * Asks the server for a site info, replying with a Site Info
     * 
     * @return
     * @throws GSException
     */
    public SitesResponseDocument getSiteInfo(String networkName, String siteCode) throws GSException {

	String fullSiteCode = networkName + ":" + siteCode;
	synchronized (siteInfoCache) {
	    SitesResponseDocument ret = siteInfoCache.get(fullSiteCode);
	    if (ret != null) {
		return ret;
	    }
	}

	SOAPExecutorDOM executor = new SOAPExecutorDOM(endpoint);

	executor.setSOAPAction(getGetSiteInfoSOAPAction());

	InputStream input = null;
	try {
	    input = loadGetSiteInfoTemplate();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// sets the site parameter
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(input);
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    writer.setText("//*:site[1]", networkName + ":" + siteCode);
	    input = reader.asStream();
	} catch (Exception e1) {
	    // this shouldn't happen
	    e1.printStackTrace();
	}
	executor.setBody(input);

	executor.setResultPath(getGetSiteInfoResultXPath());

	XMLDocumentReader reader = executor.execute();

	SitesResponseDocument srd = new SitesResponseDocument(reader.getDocument());
	synchronized (siteInfoCache) {
	    siteInfoCache.put(fullSiteCode, srd);
	}
	return srd;

    }

    private static ExpiringCache<SitesResponseDocument> siteInfoCache;
    static {
	siteInfoCache = new ExpiringCache<SitesResponseDocument>();
	siteInfoCache.setMaxSize(50);
	siteInfoCache.setDuration(TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * Gets all the time series values
     * 
     * @param networkName
     * @param siteCode
     * @param variableCode
     * @return
     * @throws GSException
     */
    public TimeSeriesResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId) throws GSException {
	return getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId, (String) null, (String) null);
    }

    public TimeSeriesResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId, Date timeBegin, Date timeEnd) throws GSException {

	// first, we must retrieve timezone of the hydro server
	SitesResponseDocument siteInfo = getSiteInfo(networkName, siteCode);
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
	    if (t1Present && t1UTCPresent) {
		Date beginDate = iso8601OutputFormat.parse(t1);
		Date beginDateUTC = iso8601OutputFormat.parse(t1UTC);
		long beginMilliseconds = beginDate.getTime();
		long beginMillisecondsUTC = beginDateUTC.getTime();
		long difference = beginMilliseconds - beginMillisecondsUTC;
		long millisecsDifference = difference;
		String timeBeginString = getLocalTimeString(timeBegin, millisecsDifference);
		String timeEndString = getLocalTimeString(timeEnd, millisecsDifference);
		// then, we redirect to the usual method
		return getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId, timeBeginString,
			timeEndString);
	    }
	    if (t1UTCPresent) {
		// then, we redirect to the usual method
		String timeBeginString = getUTCTimeString(timeBegin);
		String timeEndString = getUTCTimeString(timeEnd);
		return getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId, timeBeginString,
			timeEndString);
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	throw GSException.createException(//
		getClass(), //
		"Remote server fault: Time zone not available", //
		"Contact remote server administrator", //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		CUAHSIHISServerClient.REMOTE_SERVER_ERROR //
	);
    }

    /**
     * The time request as UTC
     * 
     * @param time
     * @return
     */
    private String getUTCTimeString(Date time) {
	String ret = iso8601OutputFormat.format(time.getTime());
	return ret;
    }

    /**
     * The time request as expressed in the local system time
     * 
     * @param time
     * @return
     */
    private String getLocalTimeString(Date time, Long millisecsDifference) {
	int totalSeconds = (int) Math.round(millisecsDifference / 1000.0);
	int hours = totalSeconds / 3600;
	int minutes = (totalSeconds % 3600) / 60;
	int seconds = (totalSeconds % 3600) % 60;
	ZoneOffset offset = ZoneOffset.ofHoursMinutesSeconds(hours, minutes, seconds);
	String stamp = offset.getId(); // something like -03:00

	long newTime = time.getTime() + millisecsDifference;

	/**
	 * The time stamp string is commented, as test CUAHSIHISServerDownloaderTestIT stopped working after summer time
	 * begin (march 2018). It seems CUAHSI HIS Server depends on summer time. This should be tested again in
	 * November 2018!
	 */
	String ret = iso8601OutputFormat.format(new Date(newTime)); // + stamp;
	return ret;
    }

    public static void main(String[] args) throws Exception {
	String endpoint = "http://hydrolite.ddns.net/aral/hsl-kaza/index.php/default/services/cuahsi_1_1.asmx?WSDL";
	CUAHSIHISServerClient1_1 client = new CUAHSIHISServerClient1_1(endpoint);
	// client.getsites
	// TimeSeriesResponseDocument tsrd = client.getValues("LBR", "USU-LBR-Mendon", "USU3", d, d);
	// List<Value> values = tsrd.getTimeSeries().get(0).getValues();
	// Value v = values.get(0);
	// System.out.println(v.getDateTime() + " " + v.getDateTimeUTC() + " " + v.getValue());
    }

    /**
     * Retrieves the richer information possible about a time series. (no values present, only metadata)
     * 
     * @param siteInfo a previously obtained site info containing a timeseries with the specified variablecode
     * @param variableCode the variable code identifying the time series to be augmented
     * @return
     * @throws GSException
     */
    public TimeSeries getAugmentedTimeSeries(SiteInfo siteInfo, String variableCode, String methodId, String qualityControlLevelCode,
	    String sourceId) throws GSException {
	// first, we must retrieve the start and end dates
	TimeSeries timeSeries = siteInfo.getSeries(variableCode, methodId, qualityControlLevelCode, sourceId);
	String startDate = timeSeries.getBeginTimePosition();
	String startDateUTC = timeSeries.getBeginTimePositionUTC();
	String endDate = timeSeries.getEndTimePosition();
	String endDateUTC = timeSeries.getEndTimePositionUTC();
	Long count = timeSeries.getValueCount();
	if (count > 0) {
	    String networkName = siteInfo.getSiteCodeNetwork();
	    String siteCode = siteInfo.getSiteCode();
	    TimeSeriesResponseDocument timeSeriesResponse = getValues(networkName, siteCode, variableCode, methodId,
		    qualityControlLevelCode, sourceId, startDate, startDate);
	    // the overall information present in the series from site info is propagated
	    timeSeriesResponse.setValueCount(networkName, variableCode, count);
	    timeSeriesResponse.setVariableTimeInterval(networkName, variableCode, startDate, endDate, startDateUTC, endDateUTC);
	    // the time series from site info is finally overwritten
	    timeSeries = timeSeriesResponse.getTimeSeries().get(0);
	    // a check/report on site properties is also done in this case for mapping optimization purposes
	    SiteInfo site = timeSeries.getSiteInfo();
	    site.checkProperties();
	}
	return timeSeries;
    }

    public TimeSeriesResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId, String timeBegin, String timeEnd) throws GSException {

	SOAPExecutorDOM executor = new SOAPExecutorDOM(endpoint);

	executor.setSOAPAction(getGetValuesSOAPAction());

	// The input request
	GetValuesRequest gvr = getGetValuesRequest();
	gvr.setLocation(networkName + ":" + siteCode);
	gvr.setVariable(networkName + ":" + variableCode);
	gvr.setStartDate(timeBegin);
	gvr.setEndDate(timeEnd);

	InputStream input = null;
	try {
	    input = gvr.getReader().asStream();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	executor.setResultPath(getGetValuesResultXPath());

	XMLDocumentReader reader = executor.execute();
	NamespaceContext nc = new CUAHSINamespaceContext();
	reader.setNamespaceContext(nc);
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	try {
	    // Some HIS server (e.g.https://hydroportal.cuahsi.org/nwisuv/cuahsi_1_1.asmx?WSDL )
	    // produces invalid wml, we try to fix it
	    writer.rename("//wml:units", "wml:unit");
	    writer.rename("//wml:unitsAbbreviation", "wml:unitAbbreviation");
	    writer.remove("//wml:unitIDSpecified");
	    writer.remove("//wml:oid");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

	TimeSeriesResponseDocument ret = new TimeSeriesResponseDocument(reader.getDocument());

	// Some HIS server (e.g. http://hydrolite.ddns.net/italia/hsl-emr/index.php/default/services/cuahsi_1_1.asmx?)
	// put a space instead of a 'T' in the time string, making the document not valid according to the schema
	ret.fixTimes();

	ret.reduceValues(methodId, qualityControlLevelCode, sourceId);

	return ret;

    }

    /*
     * XPath Results
     */

    protected String getGetSitesResultXPath() {
	return "//*:GetSitesResponse/*:GetSitesResult";
    }

    protected String getGetSitesObjectResultXPath() {
	return "//*:GetSitesObjectResponse/*:sitesResponse";
    }

    protected String getGetSiteInfoResultXPath() {
	return "//*:GetSiteInfoResponse/*:GetSiteInfoResult";
    }

    protected String getGetValuesResultXPath() {
	return "//*:timeSeriesResponse";
    }

    /*
     * SOAP Actions
     */

    protected abstract String getGetSitesSOAPAction();

    protected abstract String getGetSitesObjectSOAPAction();

    protected abstract String getGetVariableInfoSOAPAction();

    protected abstract String getGetSiteInfoSOAPAction();

    protected abstract String getGetValuesSOAPAction();

    /*
     * Templates
     */

    protected abstract InputStream loadGetSitesTemplate() throws Exception;

    protected abstract InputStream loadGetSitesObjectTemplate() throws Exception;

    protected abstract InputStream loadGetSiteInfoTemplate() throws Exception;

    protected abstract InputStream loadGetSiteInfoObjectTemplate() throws Exception;

    protected abstract GetValuesRequest getGetValuesRequest();

}
