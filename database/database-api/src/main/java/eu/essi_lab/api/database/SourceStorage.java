package eu.essi_lab.api.database;

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

import com.google.common.io.*;
import com.marklogic.xcc.exceptions.*;
import eu.essi_lab.api.database.Database.*;
import eu.essi_lab.api.database.DatabaseFolder.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.lib.xml.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.JobStatus.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.listrecords.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class SourceStorage {

    public static final String META_POSTFIX = "-meta";
    public static final String DATA_1_SHORT_POSTFIX = "data-1";
    public static final String DATA_2_SHORT_POSTFIX = "data-2";
    public static final String DATA_2_POSTFIX = "-data-2";
    public static final String DATA_1_POSTFIX = "-data-1";
    public static final String DATA_FOLDER_POSTFIX = "_dataFolder";
    public static final String WRITING_FOLDER_TAG = "writingFolder";

    private final String sourceId;
    private String startTimeStamp;

    private static final String SOURCE_STORAGE_GET_WRITING_FOLDER_ERROR = "SOURCE_STORAGE_GET_WRITING_FOLDER_ERROR";
    private static final String SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR = "SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR";
    private static final String SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR = "SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR";

    public static final String ERRORS_REPORT_FILE_NAME = "errorsReport";
    public static final String WARN_REPORT_FILE_NAME = "warnReport";

    /**
     *
     */
    private static final double DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD = 60;

    private final Database database;
    private DatabaseReader reader;
    private DatabaseFinder finder;
    private List<String> report;
    private HarvestingStrategy strategy;
    private Optional<ListRecordsRequest> request;
    private SourceStorageProvider storage;

    /**
     * @param sourceId
     * @param database
     */
    public SourceStorage(String sourceId, Database database) {

	this.sourceId = sourceId;
	this.database = database;
    }

    /**
     * @return
     */
    public static InputStream createWritingFolderTag() {

	return new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param sourceId
     * @return
     */
    public static String createDataFolderIndexName(String sourceId) {

	return URLEncoder.encode(sourceId + DATA_FOLDER_POSTFIX, StandardCharsets.UTF_8);
    }

    /**
     * @author Fabrizio
     */
    public static class DataFolderIndexDocument extends XMLDocumentReader {

	public DataFolderIndexDocument(String indexName, String dataFolderPostfix) throws SAXException, IOException {
	    super("<" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":" + //
		    indexName + //
		    " xmlns:" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + "=\"" + NameSpace.GI_SUITE_DATA_MODEL + "\">"
		    + dataFolderPostfix + "</gs:" + indexName + ">");
	}

	/**
	 * @param stream
	 * @throws IOException
	 * @throws SAXException
	 */
	public DataFolderIndexDocument(InputStream stream) throws SAXException, IOException {

	    super(stream);
	}

	/**
	 * @param doc
	 */
	public DataFolderIndexDocument(Document doc) {

	    super(doc);
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public String getShortDataFolderPostfix() {

	    return getDataFolderPrefix().substring(1);
	}

	/**
	 * @return
	 */
	public String getDataFolderPrefix() {

	    try {
		return evaluateTextContent(".//text()").getFirst();
	    } catch (XPathExpressionException e) {
	    }

	    return null;
	}
    }

    /**
     * @return
     * @throws GSException
     */
    public DatabaseFolder getWritingFolder() throws GSException {

	return getWritingFolder(Optional.empty());
    }

    /**
     * @param status
     * @return
     * @throws GSException
     */
    public DatabaseFolder getWritingFolder(Optional<SchedulerJobStatus> status) throws GSException {

	try {

	    if (isData1WritingFolder()) {

		return getData1Folder();
	    }

	    return getData2Folder();

	} catch (Exception ex) {

	    error(ex, status);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOURCE_STORAGE_GET_WRITING_FOLDER_ERROR);
	}
    }

    /**
     * @return
     * @throws Exception
     */
    public HarvestingProperties getHarvestingProperties() throws Exception {

	// -------------------------------------------------------------------------------------------------
	//
	// the first approach was to load the file once at start up, but it must be reloaded at every query,
	// since it can be changed (by an harvester) outside the current instance of SourceStorage
	// it should be not affect performances, otherwise another solution must be found
	//
	DatabaseFolder sourceFolder = getMetaFolder();

	if (sourceFolder != null && sourceFolder.exists(HarvestingProperties.getFileName())) {

	    InputStream binary = sourceFolder.getBinary(HarvestingProperties.getFileName());

	    return HarvestingProperties.fromStream(binary);
	}

	return new HarvestingProperties();
    }

    /**
     * @param suiteId
     * @param folderName
     * @return
     */
    public static String retrieveSourceName(String suiteId, String folderName) {

	if (folderName.endsWith(SourceStorage.META_POSTFIX)) {

	    folderName = folderName.replace(suiteId + "_", "");
	    folderName = folderName.replace(META_POSTFIX, "");

	    return folderName;
	}

	if (folderName.endsWith(SourceStorage.DATA_1_POSTFIX)) {

	    folderName = folderName.replace(suiteId + "_", "");
	    folderName = folderName.replace(DATA_1_POSTFIX, "");

	    return folderName;
	}

	if (folderName.endsWith(SourceStorage.DATA_2_POSTFIX)) {

	    folderName = folderName.replace(suiteId + "_", "");
	    folderName = folderName.replace(DATA_2_POSTFIX, "");

	    return folderName;
	}

	return null;
    }

    /**
     * @return
     * @throws RequestException
     */
    public DatabaseFolder getData2Folder() throws GSException {

	return database.getFolder(sourceId + DATA_2_POSTFIX);
    }

    /**
     * @return
     * @throws RequestException
     */
    public DatabaseFolder getData1Folder() throws GSException {

	return database.getFolder(sourceId + DATA_1_POSTFIX);
    }

    /**
     * @return
     * @throws RequestException
     */
    public boolean existsData1Folder() throws GSException {

	return database.existsFolder(sourceId + DATA_1_POSTFIX);
    }

    /**
     * @return
     * @throws RequestException
     */
    public boolean existsData2Folder() throws GSException {

	return database.existsFolder(sourceId + DATA_2_POSTFIX);
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean isData1WritingFolder() throws Exception {

	if (existsData1Folder()) {

	    return getData1Folder().exists(WRITING_FOLDER_TAG);
	}

	return false;
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean isData2WritingFolder() throws Exception {

	if (existsData2Folder()) {

	    return getData2Folder().exists(WRITING_FOLDER_TAG);
	}

	return false;
    }

    /**
     * If there is only a data folder (for example in case of a first harvesting or in case of selective harvesting), or smart storage is
     * disabled for this source, this method returns {@link Optional#empty()}.<br> In all the other cases, this method returns
     * <code>true</code> if the size of the writing folder is less than the treshold defined as the smart storage treshold or as the
     * {@link ListRecordsRequest#getExpectedRecords()}, if present
     */
    public Optional<Boolean> consolidatedFolderSurvives() throws Exception {

	Boolean smartStorageDisabled = storage.getSetting().isSmartStorageDisabled(this.sourceId);

	if (!smartStorageDisabled) {

	    boolean writingData1 = isData1WritingFolder();

	    DatabaseFolder writingFolder = writingData1 ? getData1Folder() : getData2Folder();

	    int writingFolderSize = writingFolder.size() - 1;

	    double treshold;

	    boolean dataYexists = writingData1 ? existsData2Folder() : existsData1Folder();

	    if (dataYexists) {

		if (request.isPresent() && request.get().getExpectedRecords().isPresent()) {

		    treshold = request.get().getExpectedRecords().get();

		} else {

		    int consolidatedFolderSize = writingData1 ? getData2Folder().size() : getData1Folder().size();
		    treshold = ((double) consolidatedFolderSize / 100) * DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD;
		}

		return Optional.of(writingFolderSize < treshold);
	    }
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public List<String> getStorageReport() {

	return report;
    }

    /**
     * @return
     */
    public String getSourceId() {

	return sourceId;
    }

    /**
     * @return
     */
    public String getStartTimeStamp() {

	return startTimeStamp;
    }

    /**
     * @return
     */
    public HarvestingStrategy getStrategy() {

	return strategy;
    }

    /**
     * @param strategy
     * @param recovery
     * @param resumed
     * @param status
     * @throws Exception
     */
    void harvestingStarted(//
	    HarvestingStrategy strategy, //
	    SourceStorageProvider storage, //
	    boolean recovery, //
	    boolean resumed, //
	    Optional<SchedulerJobStatus> status, //
	    Optional<ListRecordsRequest> request) throws Exception {

	this.strategy = strategy;
	this.request = request;
	this.storage = storage;

	report = new LinkedList<>();

	startTimeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();

	DatabaseFolder writingFolder = null;

	if (recovery) {

	    removeInterruptedHarvestingRecords(status);
	}

	switch (strategy) {
	case FULL:

	    debug("Selecting writing folder for FULL harvest with recovery flag " + recovery + " / resumed flag " + resumed, status);

	    // ----------------------
	    //
	    // first full harvesting
	    //
	    if (!database.existsFolder(getMetaFolderName())) {

		debug("First harvesting detected", status);

		// adds the meta folder
		addMetaFolder();

		// adds the source data folder
		addData1Folder();

		// in the first harvesting the data folder is the current folder
		writingFolder = getData1Folder();

		// --------------------------
		//
		// successive full harvesting
		//
	    } else {

		// -------------------------
		//
		// particular case: the end method has not been called during
		// last harvesting and now there are 2 folders (data-1 and data-2)
		// in order to preserve the more consolidated folder (that is the last NON writing folder)
		// we look for the folder having the marker resource WRITING_FOLDER
		//
		if (existsData1Folder() && existsData2Folder()) {

		    debug("Found data-1 and data-2 folders", status);

		    if (getData1Folder().exists(WRITING_FOLDER_TAG)) {
			// the last writing folder was the data-1, so it is overridden
			debug("Data-1 folder has WRITING_FOLDER tag", status);
			writingFolder = getData1Folder();
		    } else {
			// the last writing folder was the data-2, so it is overridden
			debug("Data-2 folder has WRITING_FOLDER tag", status);
			writingFolder = getData2Folder();
		    }

		    if (!recovery && !resumed) {
			// clears the writing folder
			debug("Cleaning " + writingFolder.getName(), writingFolder.getName(), status);
			writingFolder.clear();
		    }
		}

		// --------------------------------------
		//
		// the current folder is the data 1 folder (*)
		//
		else if (existsData1Folder()) {

		    debug("Only data-1 exists", status);

		    // in case of recovery or resuming continues from the last writing folder
		    if (recovery || resumed) {

			debug("Selecting data-1 because recovery or resumed flag is true", status);

			writingFolder = getData1Folder();

		    } else {

			debug("Creating data-2", status);

			// adds the data 2 folder
			addData2Folder();

			// the writing folder is the data 2 folder
			debug("Selecting data-2", status);
			writingFolder = getData2Folder();

			debug("Removing WRITING_FOLDER tag from data-1", status);
			getData1Folder().remove(WRITING_FOLDER_TAG);
		    }

		    // ----------------------------------------
		    //
		    // the current folder is the data 2 folder (*1)
		    //
		} else {
		    debug("Only data-2 exists", status);

		    // in case of recovery or resuming continues from the last writing folder
		    if (recovery || resumed) {

			debug("Selecting data-2 because recovery or resumed flag is true", status);

			writingFolder = getData2Folder();

		    } else {

			debug("Creating data-1", status);
			// adds the data 1 folder
			addData1Folder();

			// the writing folder is the data 1 folder
			debug("Selecting data-1", status);
			writingFolder = getData1Folder();

			debug("Removing WRITING_FOLDER tag from data-2", status);
			getData2Folder().remove(WRITING_FOLDER_TAG);
		    }
		}
	    }

	    break;

	case SELECTIVE:

	    debug("Selecting writing folder for SELECTIVE harvest with recovery flag " + recovery + " / resumed flag " + resumed, status);

	    if (!database.existsFolder(getMetaFolderName())) {

		debug("First harvesting detected", status);

		// adds the meta folder
		addMetaFolder();

		// adds the source data folder
		addData1Folder();

		writingFolder = getData1Folder();
	    }

	    // for selective harvesting the writing folder should always be the data-1 folder (*3)
	    // but there is the particular "reset time stamp case". if we want to perform a new FULL
	    // re-harvesting, we need to remove the time stamp from the harvesting-properties file.
	    //
	    // after that, there is the data-1 folder with last harvesting records and the writing-folder marker.
	    // when the successive harvesting starts, since there is no time stamp, it is started as FULL
	    // harvesting and the flow enters the (*) case (data-2 becomes the writing folder).
	    // at the end, the data-1 folder is removed.
	    // if the time stamp is removed again, the flow enters the (*2) case, and so on.
	    //
	    // the SELECTIVE harvesting after a time stamp reset goes in the second branch (*4)
	    //
	    else if (existsData1Folder()) {

		debug("Only data-1 exists", status);

		// (*3) normal case
		writingFolder = getData1Folder();

	    } else {

		debug("Data-1 does not exists", status);

		// (*4) after a time stamp reset
		writingFolder = getData2Folder();
	    }

	    break;
	}

	if (writingFolder == null) {

	    error("Unable to detect writing folder at harvesting START", status);

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to detect writing folder at harvesting START", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR);
	}

	debug("Selected writing folder " + writingFolder.getName() + " for " + strategy.name() + " harvesting with recovery flag "
		+ recovery + " / resumed flag " + resumed, status);

	// -------------------------------------------------------------------------
	//
	// this marker can be used to find the folder to preserve in case
	// the end method is not called for some reason
	//
	debug("Storing WRITING_FOLDER tag to folder " + writingFolder.getName() + " STARTED", writingFolder.getName(), status);

	writingFolder.store(//
		WRITING_FOLDER_TAG, //
		FolderEntry.of(createWritingFolderTag()), //
		EntryType.WRITING_FOLDER_TAG);

	debug("Storing WRITING_FOLDER tag to folder " + writingFolder.getName() + " ENDED", writingFolder.getName(), status);

	if (getMetaFolder().exists(ERRORS_REPORT_FILE_NAME)) {

	    debug("Removing errors report file STARTED", status);

	    getMetaFolder().remove(ERRORS_REPORT_FILE_NAME);

	    debug("Removing errors report file ENDED", status);
	}

	if (getMetaFolder().exists(WARN_REPORT_FILE_NAME)) {

	    debug("Removing warning report file STARTED", status);

	    getMetaFolder().remove(WARN_REPORT_FILE_NAME);

	    debug("Removing warning report file ENDED", status);
	}
    }

    /**
     * @param strategy
     * @param properties
     * @param status
     * @throws Exception
     */
    void harvestingEnded(//
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status) throws Exception {

	DatabaseFolder writingFolder = null;

	switch (strategy) {
	case FULL:

	    boolean deletedOption = storage.getSetting().isMarkDeleted(this.sourceId);

	    // -----------------------------------------------------
	    //
	    // tagging is enabled with a configured property
	    //
	    if (deletedOption) {

		debug("Tagging deleted records STARTED", status);
		markDeletedRecords(status);
		debug("Tagging deleted records ENDED", status);
	    }

	    boolean smartStorageDisabled = storage.getSetting().isSmartStorageDisabled(this.sourceId);

	    if (!smartStorageDisabled) {

		writingFolder = smartStorageFinalization(status);

	    } else {

		debug("Classic storage finalization STARTED", status);

		if (isData1WritingFolder()) {

		    writingFolder = getData1Folder();

		    getData1Folder().remove(WRITING_FOLDER_TAG);

		    if (existsData2Folder()) {
			removeData2Folder();
		    }

		} else if (isData2WritingFolder()) {

		    writingFolder = getData2Folder();

		    getData2Folder().remove(WRITING_FOLDER_TAG);

		    if (existsData1Folder()) {
			removeData1Folder();
		    }
		}

		debug("Classic storage finalization ENDED", status);
	    }

	    break;

	case SELECTIVE:

	    debug("Selective harvesting finalization STARTED", status);

	    if (isData1WritingFolder()) {

		writingFolder = getData1Folder();

		getData1Folder().remove(WRITING_FOLDER_TAG);

		if (existsData2Folder()) {
		    removeData2Folder();
		}

	    } else if (isData2WritingFolder()) {

		writingFolder = getData2Folder();

		getData2Folder().remove(WRITING_FOLDER_TAG);

		if (existsData1Folder()) {
		    removeData1Folder();
		}
	    }

	    debug("Selective harvesting finalization ENDED", status);
	}

	debug("Updating harvesting properties STARTED", status);

	updateHarvestingProperties(properties, storage.getDatabase(), writingFolder, status);

	debug("Updating harvesting properties ENDED", status);

	debug("Updating meta folder STARTED", status);

	updateMetaFolder(status);

	debug("Updating meta folder ENDED", status);

	if (writingFolder == null) {

	    error("Unable to detect writing folder at harvesting END", status);

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to detect writing folder at harvesting END", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR);
	}
    }

    /**
     * @param properties
     * @throws Exception
     */
    void storeHarvestingProperties(HarvestingProperties properties) throws Exception {

	DatabaseFolder metaFolder = getMetaFolder();

	metaFolder.remove(HarvestingProperties.getFileName());

	metaFolder.store(//
		HarvestingProperties.getFileName(), //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES);
    }

    /**
     * @param report
     * @return
     * @throws Exception
     * @throws RequestException
     */
    void updateErrorsAndWarnHarvestingReport(String report, boolean error) throws RequestException, Exception {

	// GSLoggerFactory.getLogger(getClass()).debug("Updating of " + (error ? "error" : "warn") + " harvesting report
	// STARTED");

	byte[] byteArray = null;
	boolean exists = false;

	String fileName = error ? ERRORS_REPORT_FILE_NAME : WARN_REPORT_FILE_NAME;

	if (getMetaFolder().exists(fileName)) {

	    byteArray = ByteStreams.toByteArray(getMetaFolder().getBinary(fileName));
	    exists = true;
	}

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	if (Objects.nonNull(byteArray)) {

	    outputStream.write(byteArray);
	}

	String[] messages = report.split("\n");

	for (String message : messages) {

	    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
	    outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
	}

	byteArray = outputStream.toByteArray();
	ByteArrayInputStream document = new ByteArrayInputStream(byteArray);

	EntryType entryType = fileName.equals(ERRORS_REPORT_FILE_NAME)
		? EntryType.HARVESTING_ERROR_REPORT
		: EntryType.HARVESTING_WARN_REPORT;

	if (exists) {
	    getMetaFolder().replace(fileName, FolderEntry.of(document), entryType);

	} else {
	    getMetaFolder().store(fileName, FolderEntry.of(document), entryType);
	}

	// GSLoggerFactory.getLogger(getClass()).debug("Updating of " + (error ? "error" : "warn") + " harvesting report
	// ENDED");
    }

    /**
     * @return
     * @throws Exception
     */
    List<String> retrieveErrorsReport() throws Exception {

	if (getMetaFolder().exists(ERRORS_REPORT_FILE_NAME)) {

	    byte[] byteArray = ByteStreams.toByteArray(getMetaFolder().getBinary(ERRORS_REPORT_FILE_NAME));

	    return Arrays.asList(new String(byteArray));
	}

	return new ArrayList<>();
    }

    /**
     * @return
     * @throws Exception
     */
    List<String> retrieveWarnReport() throws Exception {

	if (getMetaFolder().exists(WARN_REPORT_FILE_NAME)) {

	    byte[] byteArray = ByteStreams.toByteArray(getMetaFolder().getBinary(WARN_REPORT_FILE_NAME));

	    return Arrays.asList(new String(byteArray));
	}

	return new ArrayList<>();
    }

    /**
     * @param indexName
     * @param status
     */
    protected void checkDataFolderIndex(String indexName, Optional<SchedulerJobStatus> status) throws Exception {

    }

    /**
     * @param message
     * @param status
     */
    protected void debug(String message, Optional<SchedulerJobStatus> status) {

	status.ifPresent(s -> s.addInfoMessage(message));

	GSLoggerFactory.getLogger(getClass()).debug(message);
	report.add(message);
    }

    /**
     * @param folder
     * @return
     */
    private String findSourceIdentifier(DatabaseFolder folder) {

	return DatabaseFolder.computeSourceId(database, folder);
    }

    /**
     * @param status
     * @throws Exception
     */
    private void removeInterruptedHarvestingRecords(Optional<SchedulerJobStatus> status) throws Exception {

	debug("Removig interrupted harvesting records STARTED", status);

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID());

	DatabaseFolder metaFolder = getMetaFolder();

	if (metaFolder.exists(HarvestingProperties.getFileName())) {

	    HarvestingProperties properties = retrieveHarvestingProperties();

	    String recoveryRemovalToken = properties.getRecoveryRemovalToken();

	    if (Objects.nonNull(recoveryRemovalToken)) {

		debug("Recovery removal token found: " + recoveryRemovalToken, status);

		message.setNormalizedBond(BondFactory.createRecoveryRemovalTokenBond(recoveryRemovalToken));
		message.setPermittedBond(BondFactory.createRecoveryRemovalTokenBond(recoveryRemovalToken));
		message.setUserBond(BondFactory.createRecoveryRemovalTokenBond(recoveryRemovalToken));

	    } else {
		//
		// this should never happen since a removal token should be present
		// since the first harvester iteration on the list records
		//
		throw new Exception("No recovery removal token found");
	    }

	    int count = getFinder().count(message).getCount();
	    debug("Found " + count + " records to remove", status);

	    if (count > 0) {

		debug("Records removal STARTED", status);

		DatabaseWriter writer = DatabaseProviderFactory.getWriter(database.getStorageInfo());

		writer.remove(ResourceProperty.RECOVERY_REMOVAL_TOKEN.getName(), recoveryRemovalToken);

		debug("Records removal ENDED", status);
	    }
	}

	debug("Removig interrupted harvesting records ENDED", status);
    }

    /**
     * @param status
     * @return
     * @throws Exception
     */
    private DatabaseFolder smartStorageFinalization(Optional<SchedulerJobStatus> status) throws Exception {

	boolean writingData1 = isData1WritingFolder();

	debug("Smart storage finalization STARTED", status);

	String dataX = writingData1 ? "data-1" : "data-2";

	debug(dataX + " writing folder", status);

	DatabaseFolder writingFolder = writingData1 ? getData1Folder() : getData2Folder();

	DatabaseFolder survivedFolder = writingFolder;

	int writingFolderSize = writingFolder.size() - 1;

	debug("Writing folder size: " + StringUtils.format(writingFolderSize), StringUtils.format(writingFolderSize), status);

	double treshold = -1;

	boolean dataYexists = writingData1 ? existsData2Folder() : existsData1Folder();

	if (dataYexists) {

	    if (request.isPresent() && request.get().getExpectedRecords().isPresent()) {

		treshold = request.get().getExpectedRecords().get();

		debug("Setting treshold according to the expected number of records: " + StringUtils.format(treshold), status);

		GSLoggerFactory.getLogger(getClass())
			.info("Setting treshold according to the expected number of records: {}", StringUtils.format(treshold));

	    } else {

		int consolidatedFolderSize = writingData1 ? getData2Folder().size() : getData1Folder().size();
		treshold = ((double) consolidatedFolderSize / 100) * DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD;

		debug("Consolidated folder size: " + StringUtils.format(consolidatedFolderSize), StringUtils.format(consolidatedFolderSize),
			status);

		GSLoggerFactory.getLogger(getClass())
			.info("Setting treshold according to the consolidated folder size: {}", StringUtils.format(treshold));
	    }
	}

	if (writingFolderSize >= treshold) {

	    if (treshold > -1) {

		if (request.isPresent() && request.get().getExpectedRecords().isPresent()) {

		    debug("Writing folder size >= of the expected number of records (" + StringUtils.format((int) treshold) + ")", status);

		} else {

		    debug("Writing folder size >= " + DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD + "% (" + StringUtils.format((int) treshold)
			    + ") consolidated size", status);
		}
	    }

	    if (writingData1) {

		debug("Removing data-1 writing folder tag", status);

		getData1Folder().remove(WRITING_FOLDER_TAG);

	    } else {

		debug("Removing data-2 writing folder tag", status);

		getData2Folder().remove(WRITING_FOLDER_TAG);
	    }

	    if (dataYexists) {

		if (writingData1) {

		    debug("Removing data-2 folder STARTED", status);

		    removeData2Folder();

		    debug("Removing data-2 folder ENDED", status);

		} else {

		    debug("Removing data-1 folder STARTED", status);

		    removeData1Folder();

		    debug("Removing data-1 folder ENDED", status);
		}
	    } else {

		debug("There is no data-Y folder to remove", status);
	    }

	} else {

	    if (request.isPresent() && request.get().getExpectedRecords().isPresent()) {

		error("Writing folder size " + writingFolderSize + " <= of expected number of records " + treshold
			+ ", consolidated folder survives", status);

		updateErrorsAndWarnHarvestingReport(
			"Writing folder size " + writingFolderSize + " <= of expected number of records " + treshold
				+ ", consolidated folder survives", false);

	    } else {

		error("Writing folder size " + writingFolderSize + " <= " + DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD
			+ "% consolidated size, consolidated folder survives", status);

		updateErrorsAndWarnHarvestingReport(
			"Writing folder size " + writingFolderSize + " <= " + DEFAULT_SMART_STORAGE_PERCENTAGE_TRESHOLD
				+ "% consolidated size, consolidated folder survives", false);
	    }

	    if (dataYexists) {

		if (writingData1) {

		    debug("Removing data-1 folder STARTED", status);

		    removeData1Folder();

		    survivedFolder = getData2Folder();

		    debug("Removing data-1 folder ENDED", status);

		} else {

		    debug("Removing data-2 folder STARTED", status);

		    removeData2Folder();

		    survivedFolder = getData1Folder();

		    debug("Removing data-2 folder ENDED", status);
		}
	    } else {

		debug("There is no data-Y folder to remove", status);
	    }
	}

	debug("Smart storage finalization ENDED", status);

	return survivedFolder;
    }

    /**
     * @return
     * @throws Exception
     */
    private HarvestingProperties retrieveHarvestingProperties() throws Exception {

	DatabaseFolder sourceFolder = getMetaFolder();

	InputStream binary = sourceFolder.getBinary(HarvestingProperties.getFileName());
	return HarvestingProperties.fromStream(binary);
    }

    /**
     * @param status
     * @throws Exception
     */
    private void updateMetaFolder(Optional<SchedulerJobStatus> status) throws Exception {

	DatabaseFolder metaFolder = getMetaFolder();

	String sourceId = findSourceIdentifier(metaFolder);
	String indexName = createDataFolderIndexName(sourceId);

	debug("Handling data folder index file STARTED", status);

	checkDataFolderIndex(indexName, status);

	debug("Suite  id [" + database.getIdentifier() + "]", status);
	debug("Source id [" + sourceId + "]", status);

	//
	DatabaseFolder dataFolder = Arrays.stream(database.getFolders()).//

		filter(f -> f.getName().equals(database.getIdentifier() + "_" + sourceId + DATA_1_POSTFIX) || //
		f.getName().equals(database.getIdentifier() + "_" + sourceId + DATA_2_POSTFIX)).

		findFirst().//
		get();

	String dataFolderPostFix = dataFolder.getName().endsWith(DATA_1_POSTFIX) ? DATA_1_POSTFIX : DATA_2_POSTFIX;

	debug("Data folder postfix [" + dataFolderPostFix + "]", status);

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		dataFolderPostFix);

	debug("Index name [" + indexName + "]", status);

	if (metaFolder.exists(indexName)) {

	    debug("Replacing index doc STARTED", status);

	    boolean replaced = metaFolder.replace(indexName, FolderEntry.of(doc.getDocument()), EntryType.DATA_FOLDER_INDEX_DOC);

	    debug("Replacing index doc " + (replaced ? "SUCCEEDED" : "FAILED"), status);

	    debug("Replacing index doc ENDED", status);

	} else {

	    debug("Storing index doc STARTED", status);

	    boolean stored = metaFolder.store(indexName, FolderEntry.of(doc.getDocument()), EntryType.DATA_FOLDER_INDEX_DOC);

	    debug("Storing index doc " + (stored ? "SUCCEEDED" : "FAILED"), status);

	    debug("Storing index doc ENDED", status);
	}

	debug("Handling data folder index file ENDED", status);
    }

    /**
     * @param markLogicDB
     * @param writingFolder
     * @param status
     * @throws Exception
     */
    private void updateHarvestingProperties(//
	    Optional<HarvestingProperties> optProperties, //
	    Database markLogicDB, //
	    DatabaseFolder writingFolder, //
	    Optional<SchedulerJobStatus> status) throws Exception {

	HarvestingProperties properties = optProperties.orElse(new HarvestingProperties());

	properties.incrementHarvestingCount();

	final int[] size = new int[] { -1 };

	if (writingFolder == null) {

	    warn("Unable to detect harvesting folder, setting negative count", status);

	    updateErrorsAndWarnHarvestingReport("Unable to detect harvesting folder, setting negative count", false);

	} else {

	    size[0] = writingFolder.size();

	    status.ifPresent(s -> s.setSize(size[0]));
	}

	properties.setResourcesCount(size[0]);

	if (startTimeStamp != null) {
	    properties.setStartHarvestingTimestamp(startTimeStamp);
	} else {
	    warn("Start time stamp field is lost", status);
	}

	properties.setEndHarvestingTimestamp();

	storeHarvestingProperties(properties);
    }

    /**
     * @param message
     * @param target
     * @param status
     */
    private void debug(String message, Object target, Optional<SchedulerJobStatus> status) {

	status.ifPresent(s -> s.addInfoMessage(message));

	GSLoggerFactory.getLogger(getClass()).debug(message, target);
	String reportMessage = message.replace("{}", String.valueOf(target));
	report.add(reportMessage);
    }

    /**
     * @param message
     * @param status
     */
    private void error(String message, Optional<SchedulerJobStatus> status) {

	status.ifPresent(s -> s.addErrorMessage(message));
	status.ifPresent(s -> s.setPhase(JobPhase.ERROR));

	GSLoggerFactory.getLogger(getClass()).error(message);
	report.add(message);
    }

    /**
     * @param ex
     * @param status
     */
    private void error(Exception ex, Optional<SchedulerJobStatus> status) {

	status.ifPresent(s -> s.addErrorMessage(ex.getMessage()));
	status.ifPresent(s -> s.setPhase(JobPhase.ERROR));

	GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	report.add(ex.getMessage());
    }

    /**
     * @param message
     * @param status
     */
    private void warn(String message, Optional<SchedulerJobStatus> status) {

	status.ifPresent(s -> s.addWarningMessage(message));

	GSLoggerFactory.getLogger(getClass()).warn(message);
	report.add(message);
    }

    /**
     * @param status
     * @throws RequestException
     * @throws Exception
     */
    private void markDeletedRecords(Optional<SchedulerJobStatus> status) throws RequestException, Exception {

	if (existsData1Folder() && existsData2Folder()) {

	    DatabaseFolder newFolder = getWritingFolder(status);
	    DatabaseFolder oldFolder;

	    if (isData1WritingFolder()) {
		oldFolder = getData2Folder();
	    } else {
		oldFolder = getData1Folder();
	    }

	    // 1) get the original ids of the old folder and the original ids from the new folder
	    List<String> newIds = database.getIdentifiers(IdentifierType.ORIGINAL, newFolder.getName(), false);
	    List<String> oldIds = database.getIdentifiers(IdentifierType.ORIGINAL, oldFolder.getName(), false);

	    // 2) removes all the ids of the new folder from the ids of the old folder.
	    // the remaining are the original ids of the removed resources (they are in the old folder
	    // but no longer in the new one)
	    oldIds.removeAll(newIds);

	    debug("Found " + oldIds.size() + " deleted records", status);

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier(findSourceIdentifier(oldFolder));

	    IterationLogger logger = new IterationLogger(this, oldIds.size());
	    logger.setMessage("Total progress: ");

	    for (String string : oldIds) {
		// 3) get the resource with the removed original id from the old folder
		GSResource resource = getReader().getResource(string, gsSource, true);
		// 4) marks it as deleted (some resources can be already be tagged from previous harvesting)
		if (!resource.getPropertyHandler().isDeleted()) {
		    resource.getPropertyHandler().setIsDeleted(true);
		}
		// 5) copies the resource in the new folder
		newFolder.store(//
			resource.getPrivateId(), //
			FolderEntry.of(resource.asDocument(false)), //
			EntryType.GS_RESOURCE);

		logger.iterationEnded();
	    }
	}
    }

    /**
     * @throws RequestException
     */
    private void addMetaFolder() throws GSException {

	database.addFolder(sourceId + META_POSTFIX);
    }

    /**
     * @return
     * @throws RequestException
     */
    private DatabaseFolder getMetaFolder() throws GSException {

	return database.getFolder(sourceId + META_POSTFIX);
    }

    /**
     * @return
     */
    private String getMetaFolderName() {

	return sourceId + META_POSTFIX;
    }

    /**
     * @throws RequestException
     */
    private void addData2Folder() throws GSException {

	database.addFolder(sourceId + DATA_2_POSTFIX);
    }

    /**
     * @throws RequestException
     */
    private void addData1Folder() throws GSException {

	database.addFolder(sourceId + DATA_1_POSTFIX);
    }

    /**
     * @throws RequestException
     */
    private void removeData1Folder() throws GSException {

	database.removeFolder(sourceId + DATA_1_POSTFIX);
    }

    /**
     * @throws RequestException
     */
    private void removeData2Folder() throws GSException {

	database.removeFolder(sourceId + DATA_2_POSTFIX);
    }

    /**
     * @return
     * @throws GSException
     */
    private DatabaseReader getReader() throws GSException {

	if (this.reader == null) {

	    this.reader = DatabaseProviderFactory.getReader(database.getStorageInfo());
	}

	return reader;
    }

    /**
     * @return
     * @throws GSException
     */
    private DatabaseFinder getFinder() throws GSException {

	if (this.reader == null) {

	    this.finder = DatabaseProviderFactory.getFinder(database.getStorageInfo());
	}

	return finder;
    }

    /**
     * @return
     * @throws GSException
     */
    protected Database getDatabase() {

	return database;
    }
}
