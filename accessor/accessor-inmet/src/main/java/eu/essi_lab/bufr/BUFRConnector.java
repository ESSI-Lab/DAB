package eu.essi_lab.bufr;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.bufr.datamodel.BUFRRecord;
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

public class BUFRConnector extends HarvestedQueryConnector<BUFRConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "BUFRConnector";

    private FTPDownloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String user = "broker";

    private static final String psw = "obsolete accessor... for the password check sources document on confluence";

    private static final String BUFR_CONNECTOR_LIST_RECORDS_ERROR = "BUFR_CONNECTOR_LIST_RECORDS_ERROR";

    protected File localFolder = null;

    private Integer remoteSize = null;

    /**
     * Map country(folder) to station to collections
     */
    static Map<String, Map<String, BUFRCollection>> collections = new HashMap<>();

    protected List<String> stationIdentifiers = new ArrayList<>();

    public FTPDownloader getDownloader() {
	return downloader == null ? new FTPDownloader() : downloader;
    }

    public void setDownloader(FTPDownloader downloader) {
	this.downloader = downloader;
    }

    public BUFRConnector() {
	this.downloader = new FTPDownloader();
    }

    /**
     * Downloads the BUFR files to the local temporary folder called bufrFolder. Moreover initializes stationIdentifiers
     * field and update collections field
     * 
     * @throws IOException
     * @throws SocketException
     */
    public void getBUFRFiles() throws SocketException, IOException {

	GSLoggerFactory.getLogger(getClass()).trace("Get BUFR files STARTED");

	this.localFolder = getTemporaryFolder();

	if (remoteSize == null) {
	    this.remoteSize = getRemoteFolderSize();
	}

	logger.info("Remote size: {}", remoteSize);

	String[] list = localFolder.list();

	logger.info("Local size: {}", list.length);

	Optional<Integer> optionalMaxrecords = getSetting().getMaxRecords();

	if (optionalMaxrecords.isPresent() && optionalMaxrecords.get() != null && optionalMaxrecords.get() != 0) {
	    // the full list is reduced
	    remoteSize = optionalMaxrecords.get();
	    logger.info("Updated remote size because of max records limit: {}", remoteSize);

	}

	if (list.length < remoteSize) {
	    downloadFilesFromFTP(optionalMaxrecords);
	}

	Map<String, BUFRCollection> stationToCollections = null;
	synchronized (collections) {
	    String countryFolder = getCountryFolderName();
	    stationToCollections = collections.get(countryFolder);
	    if (stationToCollections == null || stationToCollections.isEmpty()) {
		stationToCollections = aggregateRecordsByStation(localFolder);
		collections.put(countryFolder, stationToCollections);
	    }
	}
	this.stationIdentifiers = new ArrayList<>(stationToCollections.keySet());

	GSLoggerFactory.getLogger(getClass()).trace("Get BUFR files ENDED");
    }

    private void downloadFilesFromFTP(Optional<Integer> optionalMaxrecords) {
	String authorizedURL = getAuthorizedURL();

	List<String> remoteFiles = getDownloader().downloadFileNames(authorizedURL);

	if (!remoteFiles.isEmpty()) {

	    int i = 0;
	    for (String remoteFile : remoteFiles) {
		if (optionalMaxrecords.isPresent()) {
		    i++;
		    Integer maxRecords = optionalMaxrecords.get();
		    if (maxRecords != null && maxRecords != 0 && i > maxRecords) {
			break;
		    }
		}
		File localFile = new File(this.localFolder, remoteFile);
		getDownloader().downloadToFile(authorizedURL, remoteFile, localFile);
		logger.info("Downloaded files: {}", i);
	    }
	}
    }

    /**
     * Returns the temporary folder where to download all the BUFR files from the remote FTP folder
     * 
     * @return
     */
    private File getTemporaryFolder() {
	// remote country folder
	String countryFolder = getCountryFolderName();

	String path = System.getProperty("java.io.tmpdir");
	File tmpFolder = new File(path);

	File ret = new File(tmpFolder, countryFolder);

	String info = "Designated tmp BUFR folder: " + ret.getAbsolutePath();

	logger.info(info);

	if (!ret.exists()) {
	    logger.info("Folder did not exist: will create.");
	    boolean created = ret.mkdir();
	    if (created) {
		logger.info("Folder created");
	    } else {
		logger.error("Folder not created");
	    }
	} else {
	    logger.info("Folder already existed");
	}

	return ret;
    }

    /**
     * Returns the last part of the URL, relative to the folder
     * 
     * @return
     */
    private String getCountryFolderName() {
	String sourceURL = getSourceURL();
	return sourceURL.substring(sourceURL.lastIndexOf('/') + 1);
    }

    private Integer getRemoteFolderSize() throws SocketException, IOException {

	String authorizedURL = getAuthorizedURL();

	return getDownloader().getFolderSize(authorizedURL);
    }

    /**
     * Returns the authorized URL, that is the source URL + hard coded credentials
     * 
     * @return
     */
    private String getAuthorizedURL() {

	String sourceURL = getSourceURL();

	GSLoggerFactory.getLogger(getClass()).trace("INMET url  is {}: ", sourceURL);

	String[] splitted = sourceURL.split("ftp://");
	return "ftp://" + user + ":" + psw + "@" + splitted[1];
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	logger.info("Start Getting INMET BUFR files");

	try {
	    getBUFRFiles();

	} catch (Exception eX) {

	    throw GSException.createException(//
		    getClass(), //
		    eX.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BUFR_CONNECTOR_LIST_RECORDS_ERROR);
	}

	logger.info("End Getting INMET BUFR files");

	String id = listRecords.getResumptionToken();
	int index;
	String nextId = null;
	if (id == null) {
	    id = stationIdentifiers.get(0);
	    index = 0;
	} else {
	    index = stationIdentifiers.indexOf(id);
	    logger.info("Retrieving records for station: {} ({}/{})", id, index, stationIdentifiers.size());
	    if (index == -1) {

		throw GSException.createException(//
			getClass(), //
			"Unable to resume from resumption token: " + id, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			BUFR_CONNECTOR_LIST_RECORDS_ERROR);
	    }
	}

	Map<String, BUFRCollection> stationToCollections = null;

	String countryFolderName = getCountryFolderName();

	synchronized (collections) {
	    stationToCollections = collections.get(countryFolderName);
	}

	logger.info("Station list size: {}", stationToCollections.keySet().size());

	BUFRCollection collection = stationToCollections.get(id);

	Map<String, BUFRCollection> variableCollections = collection.getVariableCollections();

	logger.info("Variable collections size (station {}): {}", id, variableCollections.keySet().size());

	for (BUFRCollection varCollection : variableCollections.values()) {

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    varCollection.marshal(baos);
	    try {
		baos.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    String originalMetadata = new String(baos.toByteArray(), StandardCharsets.UTF_8);

	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setSchemeURI(CommonNameSpaceContext.BUFR_URI);
	    metadata.setMetadata(originalMetadata);
	    ret.addRecord(metadata);

	}
	if (index == stationIdentifiers.size() - 1) {
	    nextId = null;
	} else {
	    nextId = stationIdentifiers.get(index + 1);
	}
	ret.setResumptionToken(nextId);
	return ret;
    }

    public void cleanDownloadFolder() {
	File[] files = this.localFolder.listFiles();
	for (File file : files) {
	    file.delete();
	}
	localFolder.delete();
	collections.clear();
	stationIdentifiers.clear();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.BUFR_URI);
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

    public Map<String, BUFRCollection> aggregateRecordsByStation(File folder) {
	HashMap<String, BUFRCollection> ret = new HashMap<>();
	File[] inputs = folder.listFiles();
	for (File input : inputs) {

	    if (!input.getAbsolutePath().toLowerCase().endsWith(".bfr")) {
		continue;
	    }

	    try {

		BUFRReader reader = new BUFRReader();
		List<BUFRRecord> records = reader.extractRecords(input.getAbsolutePath());
		for (BUFRRecord record : records) {
		    String stationId = record.getWMOStationNumber();
		    if (stationId != null) {
			stationId = "WMO:" + stationId;
			BUFRCollection collection = ret.get(stationId);
			if (collection == null) {
			    collection = new BUFRCollection();
			    ret.put(stationId, collection);
			}
			collection.addRecord(record);
		    } else {
			logger.debug("Skipped BUFR record without station (probably header record)");
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected BUFRConnectorSetting initSetting() {

	return new BUFRConnectorSetting();
    }

}
