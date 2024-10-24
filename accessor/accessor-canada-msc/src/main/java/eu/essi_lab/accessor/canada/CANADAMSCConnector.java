package eu.essi_lab.accessor.canada;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.utils.WebConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class CANADAMSCConnector extends StationConnector<CANADAMSCConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CANADAMSCConnector";

    /**
     *
     */
    private static final String CANADA_URL_CONNECTION_ERROR = "CANADA_MSC_CONNECTOR_URL_CONNECTION_ERROR";
    private static final String CANADA_PARSING_STATION_ERROR = "CANADA_MSC_CONNECTOR_URL_CONNECTION_ERROR";
    private static final String CANADA_STATIONS_URL_NOT_FOUND_ERROR = "CANADA_MSC_CONNECTOR_	STATIONS_URL_NOT_FOUND_ERROR";

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String CANADAMSC_CONNECTOR_PAGESIZE_OPTION_KEY = "CANADAMSC_CONNECTOR_PAGESIZE_OPTION_KEY";

    private static final int DEFAULT_PAGE_SIZE = 50;

    private WebConnector webConnector;

    /**
     * This is the cached set of CANADA urls, used during subsequent list records.
     */

    private Set<String> cachedCANADAUrls;

    private List<ECStation> canadaStations;

    private int partialNumbers;

    private int defaultRequestSize;

    public List<ECStation> getCanadaStations() {
	return canadaStations;
    }

    public CANADAMSCConnector() {

	this.defaultRequestSize = DEFAULT_PAGE_SIZE;

	this.downloader = new Downloader();
	this.webConnector = new WebConnector();

	this.canadaStations = new ArrayList<>();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	pupulateStations();

	String token = listRecords.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();
 
	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (start < canadaStations.size() && !maxNumberReached) {

	    int end = start + pageSize;
	    if (end > canadaStations.size()) {
		end = canadaStations.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }

	    int count = 0;

	    for (int i = start; i < end; i++) {

		ECStation station = canadaStations.get(i);

		List<OriginalMetadata> timeseriesMetadata = getTimeseriesMetadata(station);

		for (OriginalMetadata metadata : timeseriesMetadata) {
		    ret.addRecord(metadata);
		    partialNumbers++;
		}

		count++;
	    }

	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED " + partialNumbers + " records. Number of stations: " + String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);

	    GSLoggerFactory.getLogger(CANADAMSCConnector.class).debug("Added all Collection records: " + partialNumbers,
		    canadaStations.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private void pupulateStations() throws GSException {
	if (canadaStations.isEmpty()) {

	    Optional<String> stationsURL = findStationsURL();

	    if (stationsURL.isPresent()) {

		canadaStations = parseStations(stationsURL.get());

		GSLoggerFactory.getLogger(getClass()).trace("Number of Canada stations found: {}", canadaStations.size());

	    } else {

		throw GSException.createException(//
			this.getClass(), //
			"Unable to find stations URL", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			CANADA_STATIONS_URL_NOT_FOUND_ERROR);

	    }
	}

    }

    private List<OriginalMetadata> getTimeseriesMetadata(ECStation station) {
	//
	// add granules record
	//
	OriginalMetadata original = new OriginalMetadata();
	original.setSchemeURI(CommonNameSpaceContext.ENVIRONMENT_CANADA_URI);

	List<String> links = station.getValues();

	List<String> originalMetadataList = createOriginalMetadata(station, links);

	List<OriginalMetadata> ret = new ArrayList<>();
	for (String s : originalMetadataList) {

	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setSchemeURI(CommonNameSpaceContext.ENVIRONMENT_CANADA_URI);
	    metadata.setMetadata(s);
	    ret.add(metadata);
	}

	return ret;
    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {

	this.downloader = downloader;
    }

    @Override
    public String getSourceURL() {
	String ret = super.getSourceURL();
	if (ret.endsWith("csv/")) {
	    ret = ret.replace("csv/", "");
	}
	return ret;
    }

    /**
     * @return
     * @throws GSException
     */
    private Optional<String> findStationsURL() throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Stations URL finding STARTED");

	String canadaUrl = getSourceURL();

	GSLoggerFactory.getLogger(getClass()).trace("Canada URL: ", canadaUrl);

	List<String> urls = getWebConnector().getHrefs(canadaUrl, null);

	GSLoggerFactory.getLogger(getClass()).trace("Hrefs found {}: ", urls);

	for (String url : urls) {

	    if (url.contains("/doc/")) {

		GSLoggerFactory.getLogger(getClass()).trace("Url contains doc");

		List<String> docChildren = getWebConnector().getHrefs(url, null);

		GSLoggerFactory.getLogger(getClass()).trace("Doc childrens {}: ", docChildren);

		for (String stationsURL : docChildren) {

		    if (stationsURL.contains(".csv")) {

			GSLoggerFactory.getLogger(getClass()).trace("Stations URL found: " + stationsURL);

			return Optional.of(stationsURL);
		    }
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).error("Stations URL not found");

	return Optional.empty();
    }

    /**
     * @param stationsURL
     * @throws GSException
     * @throws IOException
     */
    private List<ECStation> parseStations(String stationsURL) throws GSException {

	List<ECStation> list = new ArrayList<>();

	InputStream body = null;

	GSLoggerFactory.getLogger(getClass()).trace("Parsing station with url {} STARTED", stationsURL);

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + stationsURL + " STARTED");

	Downloader executor = new Downloader();

	executor.setConnectionTimeout(TimeUnit.MINUTES, 1);

	try {

	    HttpResponse<InputStream> response = executor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, stationsURL));

	    GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + stationsURL + " ENDED");

	    int statusCode = response.statusCode();

	    if (statusCode != 200) {

		GSLoggerFactory.getLogger(getClass()).warn("Status code: {}", statusCode);
		GSLoggerFactory.getLogger(getClass()).warn("Status code not 200, exit method");

		throw GSException.createException(//
			this.getClass(), //
			"Unable to access " + stationsURL + ". Status code: " + statusCode, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			CANADA_URL_CONNECTION_ERROR);
	    }

	    GSLoggerFactory.getLogger(getClass()).trace("Getting content STARTED");

	    body = response.body();

	    GSLoggerFactory.getLogger(getClass()).trace("Getting content ENDED");

	    if (body != null) {

		GSLoggerFactory.getLogger(getClass()).trace("Reading header line STARTED");

		BufferedReader bfReader = null;

		bfReader = new BufferedReader(new InputStreamReader(body));
		String temp = null;
		bfReader.readLine(); // skip header line

		GSLoggerFactory.getLogger(getClass()).trace("Reading header line ENDED");

		GSLoggerFactory.getLogger(getClass()).trace("Executing while body STARTED");

		while ((temp = bfReader.readLine()) != null) {

		    if (temp != null && !temp.equals("")) {

			String[] split = temp.split(",");
			String stationCode = split[0];
			String name = split[1];

			if (name.startsWith("\"")) {
			    name = name.substring(1);
			}
			if (name.endsWith("\"")) {
			    name = name.substring(0, name.length() - 1);
			}
			String lat = split[2];
			String lon = split[3];
			String prov = split[4];
			String timezone = split[5];

			ECStation station = new ECStation();

			station.setStationCode(stationCode);
			station.setName(name);
			station.setLat(lat);
			station.setLon(lon);
			station.setProv(prov);
			station.setTimezone(timezone);

			String csvURL = stationsURL.substring(0, stationsURL.indexOf("/doc")) + "/csv/" + prov + "/";
			String csvURLDaily = csvURL + "daily/" + prov + "_" + stationCode + "_daily_hydrometric.csv";
			String csvURLHourly = csvURL + "hourly/" + prov + "_" + stationCode + "_hourly_hydrometric.csv";

			station.getValues().add(csvURLDaily);
			station.getValues().add(csvURLHourly);

			list.add(station);
		    }
		}

		GSLoggerFactory.getLogger(getClass()).trace("Executing while body ENDED");

		if (bfReader != null) {

		    bfReader.close();
		}
	    }

	    if (body != null) {

		body.close();
	    }

	} catch (Exception ex) {

	    throw GSException.createException(//
		    this.getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CANADA_PARSING_STATION_ERROR);

	}

	GSLoggerFactory.getLogger(getClass()).trace("Parsing station with url {} ENDED", stationsURL);

	return list;
    }

    private String createRecord(ECStation station, ECVariable variable, String link) {

	StringBuilder sb = new StringBuilder();

	sb.append(station.getStationCode());
	sb.append(",");
	sb.append(station.getName());
	sb.append(",");
	sb.append(station.getLat());
	sb.append(",");
	sb.append(station.getLon());
	sb.append(",");
	sb.append(station.getProvince());
	sb.append(",");
	sb.append(station.getTimezone());
	sb.append(",");

	sb.append(link);
	sb.append(",");

	sb.append(getSourceURL());
	sb.append(",");

	sb.append(variable);
	sb.append(",");

	sb.append(station.getStartDate());
	sb.append(",");

	sb.append(station.getResolutionMs());

	return sb.toString();
    }

    /**
     * @param station
     * @param links
     * @return
     */
    private List<String> createOriginalMetadata(ECStation station, List<String> links) {

	GSLoggerFactory.getLogger(getClass()).trace("Creating metadata results from " + station.getName() + " STARTED");

	List<String> res = new ArrayList<String>();

	String date = null;
	String date2 = null;

	InputStream body = null;

	int j = 0;

	try {

	    for (String l : links) {

		GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + l + " STARTED");

		Downloader executor = new Downloader();

		executor.setConnectionTimeout(TimeUnit.MINUTES, 1);

		HttpResponse<InputStream> response = executor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, l));

		int statusCode = response.statusCode();

		GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + l + " ENDED");

		if (statusCode != 200) {

		    GSLoggerFactory.getLogger(getClass()).warn("Status code {}: ", statusCode);
		    GSLoggerFactory.getLogger(getClass()).warn("Status code not 200, continue");

		    continue;
		}

		GSLoggerFactory.getLogger(getClass()).trace("Getting content STARTED");

		body = response.body();

		GSLoggerFactory.getLogger(getClass()).trace("Getting content ENDED");

		if (body != null) {

		    GSLoggerFactory.getLogger(getClass()).trace("Reading header line STARTED");

		    BufferedReader bfReader = null;

		    bfReader = new BufferedReader(new InputStreamReader(body));

		    String temp = null;
		    bfReader.readLine(); // skip header line

		    int i = 0;
		    boolean waterVar = false;
		    boolean dischargeVar = false;

		    GSLoggerFactory.getLogger(getClass()).trace("Reading header line ENDED");

		    GSLoggerFactory.getLogger(getClass()).trace("Executing while body STARTED");

		    while ((temp = bfReader.readLine()) != null && i < 5) {

			String[] split = temp.split(",", -1);
			// String id = split[0];
			if (i == 0) {
			    date = split[1];
			}
			if (i == 1) {
			    date2 = split[1];
			}

			String waterVariable = split[2];
			// String waterGrade = split[3];
			// String waterSymbol = split[4];
			// String waterQA_QC = split[5];
			String dischargeVariable = split[6];
			// String dischargeGrade = split[7];
			// String dischargeSymbol = split[7];
			// String dischargeQA_QC = split[8];

			if (waterVariable != null && !waterVariable.isEmpty()) {
			    waterVar = true;

			}

			if (dischargeVariable != null && !dischargeVariable.isEmpty()) {
			    dischargeVar = true;

			}

			i++;

		    }

		    GSLoggerFactory.getLogger(getClass()).trace("Executing while body ENDED");

		    if (bfReader != null) {

			GSLoggerFactory.getLogger(getClass()).trace("Closing buffer STARTED");

			bfReader.close();

			GSLoggerFactory.getLogger(getClass()).trace("Closing buffer ENDED");
		    }

		    if (date != null && date2 != null) {
			Optional<Date> dateTime1 = ISO8601DateTimeUtils.parseISO8601ToDate(date);
			Optional<Date> dateTime2 = ISO8601DateTimeUtils.parseISO8601ToDate(date2);
			if (dateTime1.isPresent() && dateTime2.isPresent()) {
			    long resolution = Math.abs(dateTime2.get().getTime() - dateTime1.get().getTime());
			    station.setResolutionMs(resolution);
			}
		    }

		    if (waterVar) {
			logger.info("Found Water Level Variable values for: " + station.getStationCode() + ": " + station.getName());
			// logger.info("Found " + hourlyVariables.size() + " variable for HOURLY station: " +
			// station.getStationCode()
			// + ": " + station.getName());
			station.setStartDate(date);
			String waterRecord = createRecord(station, ECVariable.WATER_LEVEL, l);
			res.add(waterRecord);
			j++;
			// ret.put(date + "_" + ECVariable.WATER_LEVEL, ECVariable.WATER_LEVEL);
		    }

		    if (dischargeVar) {
			logger.info("Found Discharge Variable values for : " + station.getStationCode() + ": " + station.getName());
			station.setStartDate(date);
			String dischargeRecord = createRecord(station, ECVariable.DISCHARGE, l);
			res.add(dischargeRecord);
			j++;
			// ret.put(date + "_" + ECVariable.DISCHARGE, ECVariable.DISCHARGE);
		    }
		}

		if (body != null) {

		    GSLoggerFactory.getLogger(getClass()).trace("Closing body STARTED");

		    body.close();

		    GSLoggerFactory.getLogger(getClass()).trace("Closing body ENDED");
		}

		logger.info("FOUND " + j + " variables for  " + station.getStationCode() + ": " + station.getName());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    return res;
	} finally {
	    try {
		if (body != null)
		    body.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Creating metadata results from " + station.getName() + " ENDED");

	return res;
    }

    private int getStationCount() {

	return this.canadaStations.size();
    }

    private WebConnector getWebConnector() {

	return webConnector == null ? new WebConnector() : webConnector;
    }

    /**
     * @param stationCode
     * @return
     */
    private ECStation getStation(String stationCode) {

	return this.canadaStations.stream().//
		filter(s -> s.getStationCode().equals(stationCode)).//
		findFirst().//
		get();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.ENVIRONMENT_CANADA_URI);
	return toret;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();

	try {

	    List<String> urls = getWebConnector().getHrefs(baseEndpoint, null);

	    if (urls.size() > 0 && urls.contains("http://dd.weather.gc.ca/hydrometric/doc/"))
		return true;

	} catch (Exception e) {
	    // any exception during download or during XML parsing
	    logger.warn("Exception during download or during XML parsing", e);
	}
	return false;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {

	pupulateStations();

	ECStation station = getStation(stationId);
	List<OriginalMetadata> timeseriesMetadata = getTimeseriesMetadata(station);
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	for (OriginalMetadata metadata : timeseriesMetadata) {
	    ret.addRecord(metadata);
	}
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CANADAMSCConnectorSetting initSetting() {

	return new CANADAMSCConnectorSetting();
    }
}
