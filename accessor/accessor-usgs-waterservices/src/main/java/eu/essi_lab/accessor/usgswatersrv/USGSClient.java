package eu.essi_lab.accessor.usgswatersrv;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class USGSClient {

    public static final String DEFAULT_SITE_URL = "https://waterservices.usgs.gov/nwis/site?";
    public static final String DEFAULT_IV_URL = "https://waterservices.usgs.gov/nwis/iv?";
    public static final String DEFAULT_DV_URL = "https://waterservices.usgs.gov/nwis/dv?";
    private static final String USGS_CLIENT_DOWNLOAD_ERROR = "USGS_CLIENT_DOWNLOAD_ERROR";
    public static final String COUNTIES_URL = "https://help.waterdata.usgs.gov/code/county_query?fmt=rdb";
    private static final String KEY_START_DT = "&startDT=";
    private static final String KEY_END_DT = "&endDT=";
    private static final String KEY_TS_ID = "&tsId=";
    private static final String SITE_STATUS_ALL = "&siteStatus=all";
    private static final String KEY_SITES = "&sites=";
    private static final String KEY_PARAMETER_CODE = "&parameterCd=";
    private static final String DATE_SUFFIX = "%2b0000";
    private static final String USGS_PREFIX = "USGS:";
    private static final String FORMAT_WATERML = "format=waterml";
    private String siteURL;
    private String ivURL;
    private String dvURL;
    private String parameterCode = null;
    private String dataType = null;
    private String countyCode = null;
    private String stationCode = null;

    public void setCountyCode(String countyCode) {
	this.countyCode = countyCode;
    }

    public void setStationCode(String stationCode) {
	this.stationCode = stationCode;
    }

    //
    // https://waterservices.usgs.gov/nwis/site/?
    //

    public USGSClient() {
	this(DEFAULT_SITE_URL, DEFAULT_IV_URL, DEFAULT_DV_URL);
    }

    public USGSClient(String siteURL) {
	this(siteURL, DEFAULT_IV_URL, DEFAULT_DV_URL);
    }

    public USGSClient(String siteURL, String ivURL, String dvURL) {
	this.siteURL = siteURL;
	this.ivURL = ivURL;
	this.dvURL = dvURL;

    }

    /**
     * Returns information on all the available time series from a given state
     * 
     * @param stateCode
     * @return
     * @throws GSException
     */
    public List<String> getSeriesInfo() throws GSException {
	String request = getSiteRequest() + getCountyParameter() + getStationParameter() + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    private String getDataTypeCode() {
	if (dataType != null) {
	    return "&hasDataTypeCd=" + dataType;
	} else {
	    return "";
	}
    }

    private String getParameterCode() {
	if (parameterCode != null) {
	    return KEY_PARAMETER_CODE + parameterCode;
	} else {
	    return "";
	}
    }

    private String getCountyParameter() {
	if (countyCode != null) {
	    return "&countyCd=" + countyCode;
	}
	return "";
    }

    private String getStationParameter() {
	if (stationCode != null) {
	    return "&site=" + stationCode;
	}
	return "";
    }

    /**
     * Returns information on all the available time series from a given state
     * 
     * @param stateCode
     * @return
     * @throws GSException
     */
    public List<String> getSeriesInfoByState(String stateCode) throws GSException {
	String request = getSiteRequest() + "&stateCd=" + stateCode + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    /**
     * Returns information on all the available time series from a given county
     * 
     * @param stateCode
     * @return
     * @throws GSException
     */
    public List<String> getSitesInfoExpanded() throws GSException {
	String request = getExpandedSiteRequest() + getCountyParameter() + getStationParameter() + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    /**
     * Returns information on all the available time series from a given state
     * 
     * @param stateCode
     * @return
     * @throws GSException
     */
    public List<String> getSeriesInfoByStateExpanded(String stateCode) throws GSException {
	String request = getExpandedSiteRequest() + "&stateCd=" + stateCode + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    /**
     * Returns information on all the time series from a given site
     * 
     * @param siteCode
     * @return
     * @throws GSException
     */
    public List<String> getSeriesInfoBySite(String siteCode) throws GSException {
	String request = getSiteRequest() + KEY_SITES + siteCode + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    /**
     * Returns information on all the time series from a given site and parameter
     * 
     * @param siteCode
     * @param parameterCode
     * @return
     * @throws GSException
     */
    public List<String> getSeriesInfoByParameter(String siteCode, String parameterCode) throws GSException {
	String request = getSiteRequest() + KEY_SITES + siteCode + KEY_PARAMETER_CODE + parameterCode;
	InputStream stream = executeRequest(request);
	return parseStream(stream);
    }

    /**
     * Returns information on a specific site and time series
     * 
     * @param siteCode
     * @param siteCode
     * @param timeSeriesCode
     * @return
     * @throws GSException
     */
    public USGSMetadata getSeriesInfoByTimeSeriesCode(String siteCode, String timeSeriesCode, String parameterCode, String statisticalCode)
	    throws GSException {
	if (timeSeriesCode == null) {
	    timeSeriesCode = parameterCode + ";" + statisticalCode;
	}
	String request = getSiteRequest() + KEY_SITES + siteCode + KEY_TS_ID + timeSeriesCode + getParameterCode() + getDataTypeCode();
	InputStream stream = executeRequest(request);
	List<String> series = parseStream(stream);
	if (series.isEmpty()) {
	    return null;
	}
	for (String serie : series) {
	    USGSMetadata metadata = new USGSMetadata(serie);
	    String tsId = metadata.getTimeSeriesId();
	    if (tsId != null && tsId.equals(timeSeriesCode)) {
		return metadata;
	    }
	    if (parameterCode != null && statisticalCode != null) {
		String pc = metadata.getParameterCode();
		String sc = metadata.getStatisticalCode();
		if (pc != null && sc != null && pc.equals(parameterCode) && sc.equals(statisticalCode)) {
		    return metadata;
		}
	    }
	}

	return null;
    }

    /**
     * Returns expanded information (useful for time zone codes) on a specific site and time series
     * 
     * @param siteCode
     * @param siteCode
     * @param timeSeriesCode
     * @return
     * @throws GSException
     */
    public String getExpandedSeriesInfoByTimeSeriesCode(String siteCode, String timeSeriesCode, String parameterCode,
	    String statisticalCode) throws GSException {
	if (timeSeriesCode == null) {
	    timeSeriesCode = parameterCode + ";" + statisticalCode;
	}
	String request = getExpandedSiteRequest() + KEY_SITES + siteCode + KEY_TS_ID + timeSeriesCode;
	InputStream stream = executeRequest(request);
	List<String> ret = parseStream(stream);
	if (ret.isEmpty()) {
	    return null;
	}
	return ret.get(0);
    }

    private List<String> parseStream(InputStream stream) {

	String line = "";

	int seriesIndex = -2; // the first two lines are the headers and the sizes

	String headers = "";
	String sizes = "";

	List<String> ret = new ArrayList<>();
	try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {

	    while ((line = br.readLine()) != null) {

		if (line.startsWith("#")) {
		    continue;
		} else {
		    if (seriesIndex == -2) {
			headers = line;
		    } else if (seriesIndex == -1) {
			sizes = line;
		    } else {
			ret.add(headers + "\n" + sizes + "\n" + line + "\n");
		    }
		    seriesIndex++;
		}

	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	return ret;
    }

    /**
     * Returns instantaneous values
     * 
     * @param siteCode
     * @param timeSeriesCode
     * @param beginString
     * @param endString
     * @return
     * @throws GSException
     */
    public InputStream getInstantantaneousValues(String timeSeriesCode, Date beginString, Date endString, TimeZone timezone,
	    String parameterCode) throws GSException {
	// https://waterservices.usgs.gov/nwis/iv/?format=waterml,2.0&sites=01646500&startDT=2018-03-01T01:00%2b0000&endDT=2018-03-07T02:00%2b0000&parameterCd=00060&siteStatus=all
	String request = getInstantaneousValuesRequest(timeSeriesCode, beginString, endString, timezone, parameterCode);
	return executeRequest(request);
    }

    /**
     * Returns daily values
     * 
     * @param siteCode
     * @param timeSeriesCode
     * @param beginDate
     * @param endDate
     * @return
     * @throws GSException
     */
    public InputStream getDailyValues(String siteCode, String parameterCode, String statisticalCode, Date beginDate, Date endDate,
	    TimeZone timezone) throws GSException {
	// https://waterservices.usgs.gov/nwis/dv/?format=waterml,2.0&sites=01646500&startDT=2018-03-01&endDT=2018-03-07&parameterCd=00060&siteStatus=all
	String request = getDailyValuesRequest(siteCode, parameterCode, statisticalCode, beginDate, endDate, timezone);
	return executeRequest(request);
    }

    private String getSiteRequest() {
	return getCoreSiteRequest() + "&seriesCatalogOutput=true";
    }

    private String getExpandedSiteRequest() {
	return getCoreSiteRequest() + "&siteOutput=expanded";
    }

    private String getCoreSiteRequest() {
	return siteURL + "format=rdb,1.0";
    }

    /**
     * Returns instantaneous values request
     * 
     * @param timeSeriesCode
     * @param beginDate
     * @param endDate
     * @return
     */
    public String getInstantaneousValuesRequest(String timeSeriesCode, Date beginDate, Date endDate, TimeZone timezone,
	    String parameterCode) {

	timeSeriesCode = timeSeriesCode.replace(USGS_PREFIX, "");
	String begin = getDateTime(beginDate, timezone);
	String end = getDateTime(endDate, timezone);
	return ivURL + //
		FORMAT_WATERML + //
		KEY_START_DT + begin + DATE_SUFFIX + //
		KEY_END_DT + end + DATE_SUFFIX + //
		KEY_TS_ID + timeSeriesCode + //
		KEY_PARAMETER_CODE + parameterCode + //
		SITE_STATUS_ALL;
    }

    /**
     * Returns instantaneous values request
     * 
     * @param parameterCode
     * @param statisticalCode
     * @param beginDate
     * @param endDate
     * @return
     */
    public String getInstantaneousValuesRequest(String siteCode, String parameterCode, Date beginDate, Date endDate, TimeZone timezone) {
	String begin = getDateTime(beginDate, timezone);
	String end = getDateTime(endDate, timezone);
	return ivURL + //
		FORMAT_WATERML + //
		KEY_SITES + siteCode + //
		KEY_START_DT + begin + DATE_SUFFIX + //
		KEY_END_DT + end + DATE_SUFFIX + //
		KEY_PARAMETER_CODE + parameterCode + //
		// "&statCd=" + statisticalCode + // no statistical codes for instantaneous values
		SITE_STATUS_ALL;
    }

    /**
     * Returns daily values request
     * 
     * @param timeSeriesCode
     * @param beginDate
     * @param endDate
     * @return
     */
    public String getDailyValuesRequest(String timeSeriesCode, Date beginDate, Date endDate, TimeZone timezone) {
	String begin = getDate(beginDate, timezone);
	String end = getDate(endDate, timezone);
	return dvURL + //
		FORMAT_WATERML + // also waterml,2.0 is supported
		KEY_START_DT + begin + //
		KEY_END_DT + end + //
		KEY_TS_ID + timeSeriesCode + //
		SITE_STATUS_ALL;
    }

    private String getDate(Date date, TimeZone timezone) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(timezone);
	return sdf.format(date);
    }

    private String getDateTime(Date date, TimeZone timezone) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	sdf.setTimeZone(timezone);
	return sdf.format(date);
    }

    /**
     * Returns daily values request
     * 
     * @param parameterCode
     * @param statisticalCode
     * @param beginDate
     * @param endDate
     * @return
     */
    public String getDailyValuesRequest(String siteCode, String parameterCode, String statisticalCode, Date beginDate, Date endDate,
	    TimeZone timezone) {
	String begin = getDate(beginDate, timezone);
	String end = getDate(endDate, timezone);
	return dvURL + //
		FORMAT_WATERML + // also waterml,2.0 is supported
		KEY_SITES + siteCode + //
		KEY_START_DT + begin + //
		KEY_END_DT + end + //
		KEY_PARAMETER_CODE + parameterCode + //
		"&statCd=" + statisticalCode + //
		SITE_STATUS_ALL;
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    private InputStream executeRequest(String request) throws GSException {

	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(request);

	if (!optional.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable get body of: " + request, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USGS_CLIENT_DOWNLOAD_ERROR);
	}

	InputStream stream = optional.get();

	return stream;
    }

    public USGSCounties getCounties() throws GSException {
	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(COUNTIES_URL);

	if (!optional.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to get county codes: " + COUNTIES_URL, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USGS_CLIENT_DOWNLOAD_ERROR);
	}

	InputStream stream = optional.get();

	List<String> result = parseStream(stream);

	USGSCounties ret = new USGSCounties(result);

	return ret;
    }

    public void setRequiredParameter(String parameterCode) {
	this.parameterCode = parameterCode;

    }

    public void setRequiredDataType(String dataType) {
	this.dataType = dataType;

    }
}
