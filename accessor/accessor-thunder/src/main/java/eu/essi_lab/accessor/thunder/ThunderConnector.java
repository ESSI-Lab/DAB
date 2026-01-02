package eu.essi_lab.accessor.thunder;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class ThunderConnector extends HarvestedQueryConnector<ThunderConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ThunderConnector";

    /**
     * SERVICE ENDPOINT: ftp://18.18.83.11/
     * Thunder Metadata are collected in file named "isd-history.csv"
     * The metadata columns are:
     * USAF,"WBAN","STATION NAME","CTRY","STATE","ICAO","LAT","LON","ELEV(M)","BEGIN","END"
     * METADATA FIELDS USED:
     * - USAF - is the name of the data files in thunder_data_GSOD folder (e.g. USAF=370180 -> 370180.txt file)
     * - STATION NAME - name of the station that collected thunder count
     * - CTRY - is the country of the station
     * - STATE -
     * - ICAO -
     * - LAT -
     * - LON -
     * - ELEV (M) -
     * - BEGIN - start temporal extent
     * - END - end temporal extent
     * Thuder Data are collected in ASCII text format and the (38) colums are:
     * 1: YEAR, 2: STATION CODE, 3-14: Monthly Average Temperature in Degrees C, starting from January
     * 15-26: Standard Deviation of Monthly Temperature in Degrees C, starting from January
     * 27-38: Monthly Thunder Day count, starting from January
     * DATA FILEDS USED
     * - Year
     * - Station Code (if NaN ignore it, it is the name of the data file). E.g. file with name 007026.txt will have
     * 007026 value as station code
     * - Monthly Thunder Day count, starting from January
     */
    private static final String THUNDER_STATION_ERROR = "Unable to find stations URL";
    private static final String THUNDER_PARSING_STATION_ERROR = "THUNDER_PARSING_STATION_ERROR";
    private static final String THUNDER_STATIONS_URL_NOT_FOUND_ERROR = "THUNDER_STATIONS_URL_NOT_FOUND_ERROR";

    private static final String THUNDER_URL_DATA_PATH = "thunder_data_GSOD";

    private static final String THUNDER_URL_METADATA_PATH = "isd-history.csv";

    private Map<String, ThunderStation> metadataStations;

    public Map<String, ThunderStation> getMetadataStations() {
	return metadataStations;
    }

    public List<String> getThunderListFiles() {
	return thunderListFiles;
    }

    private List<String> thunderListFiles;

    private FTPDownloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String USER = "thunder";

    private static final String FTP = "ftp://";

    /**
     * This is the cached set of CANADA urls, used during subsequent list records.
     */

    private Set<String> cachedThunderUrls;

    private int partialNumbers;

    public ThunderConnector() {

	this.downloader = new FTPDownloader();

	this.metadataStations = new HashMap<>();

	this.thunderListFiles = new ArrayList<>();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (metadataStations.isEmpty()) {
	    Optional<File> stationsURL = findStations();

	    if (stationsURL.isPresent()) {

		metadataStations = parseStations(stationsURL.get());

		logger.trace("Number of Thunder stations found: {}", metadataStations.size());

	    } else {

		throw GSException.createException(//
			this.getClass(), //
			THUNDER_STATION_ERROR, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			THUNDER_STATIONS_URL_NOT_FOUND_ERROR);

	    }
	}

	if (thunderListFiles.isEmpty()) {
	    Optional<List<String>> stationsFile = getStationFileList();

	    if (stationsFile.isPresent()) {

		thunderListFiles = stationsFile.get();

		logger.trace("Number of Thunder stations found: {}", thunderListFiles.size());

	    } else {

		throw GSException.createException(//
			this.getClass(), //
			THUNDER_STATION_ERROR, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			THUNDER_STATIONS_URL_NOT_FOUND_ERROR);

	    }
	}

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
	if (start < thunderListFiles.size() && !maxNumberReached) {

	    int end = start + pageSize;
	    if (end > thunderListFiles.size()) {
		end = thunderListFiles.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }

	    int count = 0;

	    for (int i = start; i < end; i++) {

		String txtName = thunderListFiles.get(i);

		String[] splittedName = txtName.split(".txt");

		String name = splittedName[0];
		if (!metadataStations.containsKey(name)) {
		    count++;
		    continue;
		}

		ThunderStation station = metadataStations.get(name);

		OriginalMetadata original = new OriginalMetadata();
		original.setSchemeURI(CommonNameSpaceContext.THUNDER_URI);

		// we consider only one variable - Monthly TD count
		String originalMetadata = createOriginalMetadata(station, txtName);

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(CommonNameSpaceContext.THUNDER_URI);
		metadata.setMetadata(originalMetadata);
		ret.addRecord(metadata);
		partialNumbers++;
		count++;
	    }

	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed stations: {}", partialNumbers, String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, metadataStations.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    public FTPDownloader getDownloader() {
	return downloader == null ? new FTPDownloader() : downloader;
    }

    public void setDownloader(FTPDownloader downloader) {

	this.downloader = downloader;
    }

    private String addCredentialsInRequests(String url) {
	return url.replace(FTP, FTP + USER + ":" + getFtpPassword() + "@");
    }

    private String getFtpPassword() {

	return getSetting().getFTPassword().orElse("");
    }

    private String removeCredentialsInRequests(String url) {
	if (url.contains(getFtpPassword())) {
	    String[] splittedString = url.split("@");
	    if (splittedString.length > 1) {
		url = FTP + splittedString[1];
	    }
	}
	return url;
    }

    /**
     * @return
     * @throws GSException
     */
    public Optional<File> findStations() throws GSException {

	logger.trace("Stations URL finding STARTED");

	String thunderUrl = getSourceURL();

	logger.trace("Thunder URL: {}", thunderUrl);

	String url = addCredentialsInRequests(thunderUrl);

	File metadataFile = null;

	try {
	    metadataFile = downloader.downloadStream(url, THUNDER_URL_METADATA_PATH);
	} catch (Exception e) {
	    throw GSException.createException(//
		    this.getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    THUNDER_PARSING_STATION_ERROR);
	}

	return Optional.of(metadataFile);
    }

    private Optional<List<String>> getStationFileList() throws GSException {
	logger.trace("Stations List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String thunderUrl = getSourceURL();

	logger.trace("Thunder URL: {}", thunderUrl);

	String[] splitted = thunderUrl.split(FTP);

	FTPClient ftpClient = new FTPClient();

	try {

	    ftpClient.connect(splitted[1], 21);
	    ftpClient.login(USER, getFtpPassword());

	    // use local passive mode to pass firewall
	    ftpClient.enterLocalPassiveMode();

	    logger.trace("Connected to remote FTP server: {}", thunderUrl);

	    FTPFile[] subFiles = ftpClient.listFiles("/" + THUNDER_URL_DATA_PATH);

	    for (FTPFile remoteFile : subFiles) {
		if (!remoteFile.getName().equals(".") && !remoteFile.getName().equals("..")) {
		    ret.add(remoteFile.getName());
		}
	    }

	} catch (Exception e) {
	    throw GSException.createException(//
		    this.getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    THUNDER_PARSING_STATION_ERROR);
	}

	logger.trace("Stations List Data finding ENDED");

	return Optional.of(ret);

    }

    /**
     * @param stationsURL
     * @throws GSException
     * @throws IOException
     */
    private Map<String, ThunderStation> parseStations(File stations) throws GSException {

	Map<String, ThunderStation> stationMap = new HashMap<>();

	try (BufferedReader bfReader = new BufferedReader(new FileReader(stations))) {

	    String temp = bfReader.readLine(); // skip header line

	    logger.trace("Reading header line ENDED");

	    logger.trace("Executing while STARTED");

	    while ((temp = bfReader.readLine()) != null) {

		if (!temp.equals("")) {

		    String[] split = temp.split(",", -1);
		    // 0 STATION CODE
		    String stationCode = split[0].replace("\"", "");
		    // 1 WBAN
		    // 2 STATION NAME - name of the station that collected thunder count
		    String stationName = split[2].replace("\"", "");
		    // 3 - CTRY - is the country of the station
		    String stationCountry = split[3].replace("\"", "");
		    // 4 - STATE -
		    String stationState = split[4].replace("\"", "");
		    // 5 - ICAO -
		    String stationIcao = split[5].replace("\"", "");
		    // 6 - LAT -
		    String stationLat = split[6].replace("\"", "");
		    // 7 - LON -
		    String stationLon = split[7].replace("\"", "");
		    // 8 - ELEV (M) -
		    String stationElevation = split[8].replace("\"", "");
		    // 9 - BEGIN -
		    String stationBegin = split[9].replace("\"", "");
		    // 10 - END -
		    String stationEnd = split[10].replace("\"", "");

		    ThunderStation station = new ThunderStation(stationCode, stationName, stationLat, stationLon, stationElevation,
			    stationBegin, stationEnd);
		    if (stationCountry != null && !stationCountry.equals("")) {
			station.setCountry(stationCountry);
		    } else {
			station.setCountry("");
		    }
		    if (stationIcao != null && !stationIcao.equals("")) {
			station.setIcao(stationIcao);
		    } else {
			station.setIcao("");
		    }
		    if (stationState != null && !stationState.equals("")) {
			station.setState(stationState);
		    } else {
			station.setState("");
		    }

		    // add station to the map
		    if (!stationMap.containsKey(stationCode)) {
			stationMap.put(stationCode, station);
		    }
		}
	    }

	    return stationMap;

	} catch (Exception e) {

	    throw GSException.createException(//
		    this.getClass(), //
		    THUNDER_STATION_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    THUNDER_STATIONS_URL_NOT_FOUND_ERROR);
	}
    }

    /**
     * @param station
     * @param name
     * @return
     */
    private String createOriginalMetadata(ThunderStation station, String txtname) {

	logger.trace("Creating metadata results from {} STARTED", station.getName());

	String endpoint = getSourceURL() + "/" + THUNDER_URL_DATA_PATH + "/" + txtname;

	String metadataRecord = createRecord(station, endpoint);

	logger.trace("Creating metadata results from {} ENDED", station.getName());

	return metadataRecord;

    }

    private String createRecord(ThunderStation station, String txtname) {

	StringBuilder sb = new StringBuilder();

	sb.append(station.getStationCode());
	sb.append(",");
	sb.append(station.getName());
	sb.append(",");
	sb.append(station.getLat());
	sb.append(",");
	sb.append(station.getLon());
	sb.append(",");
	sb.append(station.getElevation());
	sb.append(",");
	sb.append(station.getStartDate());
	sb.append(",");
	sb.append(station.getEndDate());
	sb.append(",");
	sb.append(station.getIcao());
	sb.append(",");
	sb.append(station.getState());
	sb.append(",");
	sb.append(station.getCountry());
	sb.append(",");
	sb.append(getSourceURL());
	sb.append(",");

	sb.append(txtname);

	return sb.toString();
    }

    private int getStationCount() {

	return this.thunderListFiles.size();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.THUNDER_URI);
	return toret;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();

	String url = addCredentialsInRequests(baseEndpoint);

	try {

	    return getDownloader().checkConnection(url);

	} catch (Exception e) {
	    // any exception during download or during XML parsing
	    logger.warn("Exception during connection to FTP remote service", e);
	}
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ThunderConnectorSetting initSetting() {

	return new ThunderConnectorSetting();
    }
}
