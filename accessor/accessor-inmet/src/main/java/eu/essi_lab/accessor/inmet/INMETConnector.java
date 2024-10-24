package eu.essi_lab.accessor.inmet;

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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class INMETConnector extends HarvestedQueryConnector<INMETConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "INMETConnector";

    private FTPDownloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String user = "broker";

    private static final String psw = "obsolete accessor... for the password check sources document on confluence";

    private static final String INMET_CONNECTOR_PACKAGE_ID_NOT_FOUND_ERROR = "INMET_CONNECTOR_PACKAGE_ID_NOT_FOUND_ERROR";

    private int startIndex;

    /**
     * This is the cached set of INMET urls, used during subsequent list records.
     */

    private List<String> inmetFiles = new ArrayList<>();

    private int partialNumbers = 0;

    public FTPDownloader getDownloader() {
	return downloader == null ? new FTPDownloader() : downloader;
    }

    public void setDownloader(FTPDownloader downloader) {
	this.downloader = downloader;
    }

    public INMETConnector() {
	this.downloader = new FTPDownloader();
	startIndex = 0;
	// this.webConnector = new WebConnector();

	// GSConfOption<Integer> pageSizeOption = new GSConfOptionInteger();
	// pageSizeOption.setLabel("GetRecords page size");
	// pageSizeOption.setKey(INMET_CONNECTOR_PAGESIZE_OPTION_KEY);
	// pageSizeOption.setValue(requestSizeDefault);
	// getSupportedOptions().put(pageSizeOption.getKey(), pageSizeOption);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (inmetFiles == null || inmetFiles.isEmpty()) {

	    logger.info("Start Getting INMET files");

	    getINMETFiles();

	    logger.info("End Getting INMET files");
	    logger.info("INMET files SIZE: {}", inmetFiles.size());
	}

	Iterator<String> iterator = inmetFiles.iterator();

	String id = listRecords.getResumptionToken();
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
	    if (inmetFiles.contains(id)) {
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
			ErrorInfo.SEVERITY_ERROR, //
			INMET_CONNECTOR_PACKAGE_ID_NOT_FOUND_ERROR); //
	    }
	}

	// String token = listRecords.getResumptionToken();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && startIndex > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (startIndex < inmetFiles.size() && !maxNumberReached) {

	    if (id != null) {
		String baseEndpoint = getSourceURL();
		String[] splitted = baseEndpoint.split("ftp://");

		String url = "ftp://" + user + ":" + psw + "@" + splitted[1];

		File file = getDownloader().downloadStream(url, id);
		if (file != null) {
		    String originalMetadata = createCSVMetadataRecord(file, id);
		    if (originalMetadata != null) {
			OriginalMetadata metadata = new OriginalMetadata();
			metadata.setSchemeURI(CommonNameSpaceContext.INMET_CSV_URI);
			metadata.setMetadata(originalMetadata);
			ret.addRecord(metadata);
			partialNumbers++;
			startIndex++;
		    }
		    if (file.exists()) {
			file.delete();
		    }
		}

	    }

	} else {
	    nextId = null;
	}
	ret.setResumptionToken(nextId);
	return ret;
    }

    public String createCSVMetadataRecord(File file, String nameFile) throws GSException {
	String result;
	try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {

	    String[] values = getValues(br);
	    logger.info("READING FILE: {}", nameFile);
	    if (values == null) {
		logger.info("ERROR READING FILE: {}", nameFile);
		return null;
		// throw GSException.createException(this.getClass(), "Error reading inpustream: ",
		// "IOException connecting to: ", ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR,
		// INMET_STREAM_ERROR);
	    }
	    logger.info("SUCCESS READING FILE: {}", nameFile);
	    HashMap<String, String> metadata = new HashMap<>();
	    while (values != null && !containsCaseInsensitive("precipita", values)) {
		String key = values[0];
		key = key.replace(":", "");
		key = key.trim();
		key = key.toUpperCase();
		if (values.length > 1) {
		    metadata.put(key, values[1]);
		} else {
		    logger.info("ERROR XLS FILE: {}", nameFile);
		}
		values = getValues(br);
	    }
	    String region = null;
	    String state = null;
	    String stationName = null;
	    String wmoCode = null;
	    String wigosId = null;
	    String latitude = null;
	    String longitude = null;
	    String height = null;
	    String foundationDate = null;
	    for (String metadataKey : metadata.keySet()) {
		switch (metadataKey) {
		case "REGION":
		    region = metadata.get(metadataKey);
		    break;
		case "FEDERATIVE UNIT (STATE)":
		    state = metadata.get(metadataKey);
		    break;
		case "NAME OF THE STATION":
		    stationName = metadata.get(metadataKey);
		    break;
		case "WMO CODE":
		    wmoCode = metadata.get(metadataKey);
		    break;
		case "WIGOS ID":
		    wigosId = metadata.get(metadataKey);
		    break;
		case "LATITUDE":
		    latitude = metadata.get(metadataKey).replace(",", ".");
		    break;
		case "LONGITUDE":
		    longitude = metadata.get(metadataKey).replace(",", ".");
		    break;
		case "HEIGHT":
		    height = metadata.get(metadataKey);
		    break;
		case "FOUNDATION DATE":
		    foundationDate = metadata.get(metadataKey);
		    break;
		default:
		    System.out.println("Unknown metadata: " + metadataKey);
		    break;
		}
	    }

	    Date beginDate = null;
	    Date endDate = null;
	    String beginStringDate = null;
	    String endStringDate = null;
	    int count = 0;
	    SortedMap<Date, Double> valuesPoint = new TreeMap<Date, Double>();
	    while (br.ready()) {
		values = getValues(br);
		String date = values[0];
		String hour = values[1];
		String value = values[2];
		if (value == null || value.startsWith("-999")) {
		    continue;
		}
		value = value.replace(",", ".");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String tmpString = date + "T" + hour;
		Date tmp = format.parse(tmpString);
		// Date tmp = TimeAndDateHelper.parse(date + hour, "yyyy-MM-ddHHmm");
		// try {
		// double v = Double.parseDouble(value);
		// valuesPoint.put(tmp, v);
		// } catch (Exception e) {
		// continue;
		// }
		if (beginDate == null || beginDate.after(tmp)) {
		    beginDate = tmp;
		    beginStringDate = tmpString;
		}
		if (endDate == null || endDate.before(tmp)) {
		    endDate = tmp;
		    endStringDate = tmpString;
		}
		count++;
	    }

	    // add check on date, if date is null ignore record (return null)
	    if (beginDate == null && endDate == null) {
		return null;
	    }

	    INMETStation station = new INMETStation();
	    if (region != null) {
		station.setRegion(region);
	    }
	    if (state != null) {
		station.setState(state);
	    }
	    if (stationName != null) {
		station.setStationName(stationName);
	    }
	    if (wmoCode != null) {
		station.setWmoCode(wmoCode);
	    }
	    if (wigosId != null) {
		station.setWigosId(wigosId);
	    }
	    if (latitude != null) {
		station.setLatitude(latitude);
	    }
	    if (longitude != null) {
		station.setLongitude(longitude);
	    }
	    if (height != null) {
		station.setHeight(height);
	    }
	    if (foundationDate != null) {
		station.setFoundationDate(foundationDate);
	    }

	    if (beginDate != null) {
		station.setStartDate(beginStringDate);
	    }
	    if (endDate != null) {
		station.setEndDate(endStringDate);
	    }
	    if (nameFile != null) {
		station.setNameFile(nameFile);
	    }
	    if (count != 0) {
		station.setSize(count);
	    }

	    result = createRecord(station, INMETVariable.PRECIPITATION);
	    return result;

	} catch (IOException | ParseException e) {

	    e.printStackTrace();
	} finally {

	    if (file != null) {

		GSLoggerFactory.getLogger(getClass()).trace("Removing TMP file: " + nameFile);

		file.deleteOnExit();
		file.delete();

		GSLoggerFactory.getLogger(getClass()).trace("TMP FILE REMOVED");
	    }
	}

	return null;
    }

    public String[] getValues(BufferedReader reader) throws IOException {
	if (!reader.ready()) {
	    return null;
	}
	String line = reader.readLine();
	return line.split(";");
    }

    private String createRecord(INMETStation station, INMETVariable variable) {
	StringBuilder sb = new StringBuilder();

	sb.append(station.getRegion());
	sb.append(",");
	sb.append(station.getState());
	sb.append(",");
	sb.append(station.getStationName());
	sb.append(",");
	sb.append(station.getWmoCode());
	sb.append(",");
	sb.append(station.getWigosId());
	sb.append(",");
	sb.append(station.getLatitude());
	sb.append(",");
	sb.append(station.getLongitude());
	sb.append(",");
	sb.append(station.getHeight());
	sb.append(",");
	sb.append(station.getFoundationDate());
	sb.append(",");
	sb.append(station.getStartDate());
	sb.append(",");
	sb.append(station.getEndDate());
	sb.append(",");
	sb.append(station.getNameFile());
	sb.append(",");
	sb.append(getSourceURL());
	sb.append(",");
	sb.append(station.getSize());

	return sb.toString();
    }

    public void getINMETFiles() {

	GSLoggerFactory.getLogger(getClass()).trace("Get INMET files STARTED");

	String inmetUrl = getSourceURL();

	GSLoggerFactory.getLogger(getClass()).trace("INMET url  is {}: ", inmetUrl);

	String[] splitted = inmetUrl.split("ftp://");

	String url = "ftp://" + user + ":" + psw + "@" + splitted[1];

	List<String> listNames = getDownloader().downloadFileNames(url);

	if (!listNames.isEmpty()) {
	    this.inmetFiles = listNames;
	}

	GSLoggerFactory.getLogger(getClass()).trace("Get INMET files ENDED");
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.INMET_CSV_URI);
	return toret;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();

	String[] splitted = baseEndpoint.split("ftp://");

	String url = "ftp://" + user + ":" + psw + "@" + splitted[1];

	try {

	    return getDownloader().checkConnection(url);

	} catch (Exception e) {
	    // any exception during download or during XML parsing
	    logger.warn("Exception during connection to FTP remote service", e);
	}
	return false;
    }

    public int getINMETfileSize() {
	return this.inmetFiles.size();
    }

    public List<String> getINMETfileNames() {
	return this.inmetFiles;
    }

    public boolean containsCaseInsensitive(String s, String[] values) {
	for (String string : values) {
	    if (string.toLowerCase().contains(s)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected INMETConnectorSetting initSetting() {

	return new INMETConnectorSetting();
    }
}
