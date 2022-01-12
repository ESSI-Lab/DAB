package eu.essi_lab.api.database.marklogic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.xml.sax.SAXException;

import com.google.common.io.ByteStreams;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.internal.Folder;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.jaxb.common.schemas.BooleanValidationHandler;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.common.schemas.SchemaValidator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.PropertiesAdapter.AdaptPolicy;
public class SourceStorageWorker {

    public static final String META_PREFIX = "-meta";
    public static final String DATA_2_PREFIX = "-data-2";
    public static final String DATA_1_PREFIX = "-data-1";
    public static final String DATA_FOLDER_POSTFIX = "_dataFolder";

    private String sourceId;
    private MarkLogicDatabase markLogicDB;
    private String startTimeStamp;
    private static final String WRITING_FOLDER = "writingFolder";

    private static final String SOURCE_STORAGE_GET_WRITING_FOLDER_ERROR = "SOURCE_STORAGE_GET_WRITING_FOLDER_ERROR";
    private static final String SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR = "SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR";
    private static final String SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR = "SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR";
    private static final String ERRORS_REPORT_FILE_NAME = "errorsReport";
    private static final String WARN_REPORT_FILE_NAME = "warnReport";
    public static double MINIMUM_PERCENT = 90;
    
    private static Boolean ENABLE_SMART_HARVESTING = null;
    
    
    public static void enableSmartHarvestingFinalizationFeature(boolean enable) {
	ENABLE_SMART_HARVESTING = enable;
    }
    
    private MarkLogicReader reader;
    private List<String> report;

    /**
     * @param sourceId
     * @param db
     * @throws Exception
     */
    public SourceStorageWorker(String sourceId, MarkLogicDatabase db) {

	this.sourceId = sourceId;
	this.markLogicDB = db;
	this.reader = new MarkLogicReader();
	this.reader.setDatabase(db);
    }

    /**
     * @param sourceId
     * @return
     */
    public static String createDataFolderIndexName(String sourceId) {

	try {
	    return URLEncoder.encode(sourceId + DATA_FOLDER_POSTFIX, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException(e.getMessage());
	}
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
    }

    /**
     * @return
     * @throws GSException
     */
    public Folder getWritingFolder() throws GSException {

	try {

	    if (isWritingData1Folder()) {

		return getData1Folder();
	    }

	    return getData2Folder();

	} catch (Exception ex) {

	    error(ex);

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
    public HarvestingProperties getSourceHarvestingProperties() throws Exception {

	// -------------------------------------------------------------------------------------------------
	//
	// the first approach was to load the file once at start up, but it must be reloaded at every query,
	// since it can be changed (by an harvester) outside the current instance of SourceStorageWorker
	// it should be not affect performances, otherwise another solution must be found
	//
	Folder sourceFolder = getMetaFolder();

	if (sourceFolder != null && sourceFolder.exists(HarvestingProperties.getFileName())) {

	    InputStream binary = sourceFolder.getBinary(HarvestingProperties.getFileName());

	    return HarvestingProperties.fromStream(binary);
	}

	return new HarvestingProperties();
    }

    /**
     * @param strategy
     * @param recovery
     * @throws Exception
     */
    void harvestingStarted(HarvestingStrategy strategy, boolean recovery) throws Exception {

	report = new LinkedList<String>();

	startTimeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();

	Folder writingFolder = null;

	if (recovery) {

	    removeInterruptedHarvestingRecords();
	}

	switch (strategy) {
	case FULL:

	    debug("Selecting writing folder for FULL harvest with recovery flag {}", recovery);

	    // ----------------------
	    //
	    // first full harvesting
	    //
	    if (!markLogicDB.existsFolder(getMetaFolderName())) {

		debug("First harvesting detected");

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

		    debug("Found data-1 and data-2 folders");

		    if (getData1Folder().exists(WRITING_FOLDER)) {
			// the last writing folder was the data-1, so it is overridden
			debug("Data-1 folder has WRITING_FOLDER tag");
			writingFolder = getData1Folder();
		    } else {
			// the last writing folder was the data-2, so it is overridden
			debug("Data-2 folder has WRITING_FOLDER tag");
			writingFolder = getData2Folder();
		    }

		    if (!recovery) {
			// clears the writing folder
			debug("Cleaning {}", writingFolder.getURI());
			writingFolder.clear();
		    }
		}

		// --------------------------------------
		//
		// the current folder is the data 1 folder (*)
		//
		else if (existsData1Folder()) {

		    debug("Only data-1 exists");

		    // in case of recovery continues from the last writing folder
		    if (recovery) {

			debug("Selecting data-1 because recovery flag is true");

			writingFolder = getData1Folder();

		    } else {

			debug("Creating data-2");

			// adds the data 2 folder
			addData2Folder();

			// the writing folder is the data 2 folder
			debug("Selecting data-2");
			writingFolder = getData2Folder();

			debug("Removing WRITING_FOLDER tag from data-1");
			getData1Folder().remove(WRITING_FOLDER);
		    }

		    // ----------------------------------------
		    //
		    // the current folder is the data 2 folder (*1)
		    //
		} else {
		    debug("Only data-2 exists");

		    // in case of recovery continues from the last writing folder
		    if (recovery) {

			debug("Selecting data-2 because recovery flag is true");

			writingFolder = getData2Folder();

		    } else {

			debug("Creating data-1");
			// adds the data 1 folder
			addData1Folder();

			// the writing folder is the data 1 folder
			debug("Selecting data-1");
			writingFolder = getData1Folder();

			debug("Removing WRITING_FOLDER tag from data-2");
			getData2Folder().remove(WRITING_FOLDER);
		    }
		}
	    }

	    break;

	case SELECTIVE:

	    debug("Selecting writing folder for SELECTIVE harvest with recovery flag {}", recovery);

	    if (!markLogicDB.existsFolder(getMetaFolderName())) {

		debug("First harvesting detected");

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

		debug("Only data-1 exists");

		// (*3) normal case
		writingFolder = getData1Folder();

	    } else {

		debug("Data-1 does not exists");

		// (*4) after a time stamp reset
		writingFolder = getData2Folder();
	    }

	    break;
	}

	if (writingFolder == null) {

	    error("Unable to detect writing folder at harvesting START");

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to detect writing folder at harvesting START", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOURCE_STORAGE_GET_WRITING_FOLDER_HARVESTING_STARTED_ERROR);
	}

	debug("Selected writing folder " + writingFolder.getURI() + " for " + strategy.name() + " harvesting with recovery flag "
		+ recovery);

	// -------------------------------------------------------------------------
	//
	// this marker can be used to find the folder to preserve in case
	// the end method is not called for some reason
	//
	debug("Storing WRITING_FOLDER tag to folder {} STARTED", writingFolder.getURI());

	writingFolder.storeBinary(WRITING_FOLDER, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

	debug("Storing WRITING_FOLDER tag to folder {} ENDED", writingFolder.getURI());

	if (!recovery && getMetaFolder().exists(ERRORS_REPORT_FILE_NAME)) {

	    debug("Removing errors report file STARTED");

	    getMetaFolder().remove(ERRORS_REPORT_FILE_NAME);

	    debug("Removing errors report file ENDED");
	}

	if (!recovery && getMetaFolder().exists(WARN_REPORT_FILE_NAME)) {

	    debug("Removing warning report file STARTED");

	    getMetaFolder().remove(WARN_REPORT_FILE_NAME);

	    debug("Removing warning report file ENDED");
	}
    }

    /**
     * @param strategy
     * @param storage
     * @throws Exception
     */
    void harvestingEnded(MarkLogicSourceStorage storage, HarvestingStrategy strategy) throws Exception {

	// -----------------------------------------------------
	//
	// validation is enabled with a configured property
	//
	GSConfOption<?> isoOption = storage.getSupportedOptions().get(SourceStorage.TEST_ISO_COMPLIANCE_KEY);

	if (isoOption != null && isoOption.getValue().equals(Boolean.TRUE)) {

	    debug("ISO compliance test STARTED");
	    testISOCompliance();
	    debug("ISO compliance test ENDED");
	}

	// -----------------------------------------------------
	//
	// recovering is enabled with a configured property
	//
	GSConfOption<?> tagsOption = storage.getSupportedOptions().get(SourceStorage.RECOVER_TAGS_KEY);

	if (tagsOption != null && tagsOption.getValue().equals(Boolean.TRUE)) {

	    debug("Recovering tags STARTED");
	    recoverTags();
	    debug("Recovering tags ENDED");
	}

	Folder writingFolder = null;

	switch (strategy) {
	case FULL:

	    GSConfOption<?> deletedOption = storage.getSupportedOptions().get(SourceStorage.MARK_DELETED_RECORDS_KEY);

	    // -----------------------------------------------------
	    //
	    // tagging is enabled with a configured property
	    //
	    if (deletedOption != null && deletedOption.getValue().equals(Boolean.TRUE)) {

		debug("Tagging deleted records STARTED");
		markDeletedRecords();
		debug("Tagging deleted records ENDED");
	    }

	    GSConfOption<?> forceOverwriteOption = storage.getSupportedOptions().get(SourceStorage.FORCE_OVERWRITE_TAGS_KEY);

	    boolean smartHarvestingFeature = true;
	    if (forceOverwriteOption != null && forceOverwriteOption.getValue().equals(Boolean.TRUE)) {
		smartHarvestingFeature = false;
	    }
	    if (ENABLE_SMART_HARVESTING!=null) { // this is only used by the tests. At runtime the option is used
		smartHarvestingFeature = ENABLE_SMART_HARVESTING;
	    }

	    if (smartHarvestingFeature) {

		writingFolder = smartHarvestingFinalization(isWritingData1Folder());

	    } else {

		if (isWritingData1Folder()) {

		    writingFolder = getData1Folder();

		    getData1Folder().remove(WRITING_FOLDER);

		    if (existsData2Folder()) {
			removeData2Folder();
		    }

		} else if (isWritingData2Folder()) {

		    writingFolder = getData2Folder();

		    getData2Folder().remove(WRITING_FOLDER);

		    if (existsData1Folder()) {
			removeData1Folder();
		    }
		}
	    }

	    break;

	case SELECTIVE:

	    writingFolder = getData1Folder();

	    getData1Folder().remove(WRITING_FOLDER);
	}

	// updates the harvesting properties at last
	updateHarvestingProperties(storage.getDatabase(), writingFolder);

	if (writingFolder == null) {

	    error("Unable to detect writing folder at harvesting END");

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to detect writing folder at harvesting END", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOURCE_STORAGE_DECTECT_WRITING_FOLDER_HARVESTING_END_ERROR);
	}
    }

    static String retrieveSourceName(String suiteId, String folderName) {

	if (folderName.endsWith(SourceStorageWorker.META_PREFIX)) {

	    folderName = folderName.replace(suiteId + "_", "");
	    folderName = folderName.replace(META_PREFIX, "");

	    return folderName;
	}

	return null;
    }

    void storeHarvestingProperties(HarvestingProperties properties) throws Exception {

	Folder sourceFolder = getMetaFolder();

	sourceFolder.remove(HarvestingProperties.getFileName());

	sourceFolder.storeBinary(HarvestingProperties.getFileName(), properties.asStream());
    }

    Folder getData2Folder() throws RequestException {

	return markLogicDB.getFolder(sourceId + DATA_2_PREFIX);
    }

    Folder getData1Folder() throws RequestException {

	return markLogicDB.getFolder(sourceId + DATA_1_PREFIX);
    }

    boolean existsData1Folder() throws RequestException {

	return markLogicDB.existsFolder(sourceId + DATA_1_PREFIX);
    }

    boolean existsData2Folder() throws RequestException {

	return markLogicDB.existsFolder(sourceId + DATA_2_PREFIX);
    }

    /**
     * @return
     */
    List<String> getStorageReport() {

	return report;
    }

    /**
     * @param report
     * @return
     * @throws Exception
     * @throws RequestException
     */
    void updateErrorsAndWarnHarvestingReport(String report, boolean error) throws RequestException, Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Updating of " + (error ? "error" : "warn") + " harvesting report STARTED");

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

	    outputStream.write(message.getBytes("UTF-8"));
	    outputStream.write("\n".getBytes("UTF-8"));
	}

	byteArray = outputStream.toByteArray();
	ByteArrayInputStream document = new ByteArrayInputStream(byteArray);

	if (exists) {
	    getMetaFolder().replaceBinary(fileName, document);

	} else {
	    getMetaFolder().storeBinary(fileName, document);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Updating of " + (error ? "error" : "warn") + " harvesting report ENDED");
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

	return new ArrayList<String>();
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

	return new ArrayList<String>();
    }

    /**
     * @param writingFolder
     */
    private void removeInterruptedHarvestingRecords() throws Exception {

	debug("Removig interrupted harvesting records STARTED");

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());

	Folder metaFolder = getMetaFolder();

	if (metaFolder.exists(HarvestingProperties.getFileName())) {

	    HarvestingProperties properties = retrieveHarvestingProperties();

	    String recoveryRemovalToken = properties.getRecoveryRemovalToken();

	    if (Objects.nonNull(recoveryRemovalToken)) {

		debug("Recovery removal token found: " + recoveryRemovalToken);

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

	    int count = reader.count(message).getCount();
	    debug("Found " + count + " records to remove");

	    if (count > 0) {

		debug("Records removal STARTED");

		markLogicDB.removeResourcesByRemovalResumptionToken(recoveryRemovalToken, count);

		debug("Records removal ENDED");
	    }
	}

	debug("Removig interrupted harvesting records ENDED");
    }

    /**
     * @param writingData1
     * @throws Exception
     */
    private Folder smartHarvestingFinalization(boolean writingData1) throws Exception {

	debug("Full harvesting finalization STARTED");

	String dataX = writingData1 ? "data-1" : "data-2";

	debug(dataX + " writing folder");

	Folder writingFolder = writingData1 ? getData1Folder() : getData2Folder();

	Folder survivedFolder = writingFolder;

	int writingFolderSize = writingFolder.size() - 1;

	debug("Writing folder size: {}", writingFolderSize);

	double percent = -1;

	boolean dataYexists = writingData1 ? existsData2Folder() : existsData1Folder();

	if (dataYexists) {
	    int consolidatedFolderSize = writingData1 ? getData2Folder().size() : getData1Folder().size();
	    percent = ((double) consolidatedFolderSize / 100) * MINIMUM_PERCENT;

	    debug("Consolidated folder size: {}", consolidatedFolderSize);
	}

	if (writingFolderSize >= percent) {

	    debug("Writing folder size >= 90% consolidated size");

	    if (writingData1) {

		debug("Removing data-1 writing folder tag");

		getData1Folder().remove(WRITING_FOLDER);

	    } else {

		debug("Removing data-2 writing folder tag");

		getData2Folder().remove(WRITING_FOLDER);
	    }

	    if (dataYexists) {

		if (writingData1) {

		    debug("Removing data-2 folder STARTED");

		    removeData2Folder();

		    debug("Removing data-2 folder ENDED");

		} else {

		    debug("Removing data-1 folder STARTED");

		    removeData1Folder();

		    debug("Removing data-1 folder ENDED");
		}
	    }
	} else {

	    warn("Writing folder size <= 90% consolidated size, consolidated folder survives");

	    updateErrorsAndWarnHarvestingReport("Writing folder size <= 90% consolidated size, consolidated folder survives", false);

	    if (writingData1) {

		debug("Removing data-1 folder STARTED");

		removeData1Folder();

		survivedFolder = getData2Folder();

		debug("Removing data-1 folder ENDED");

	    } else {

		debug("Removing data-2 folder STARTED");

		removeData2Folder();

		survivedFolder = getData1Folder();

		debug("Removing data-2 folder ENDED");
	    }
	}

	debug("Full harvesting finalization ENDED");

	return survivedFolder;
    }

    /**
     * @return
     * @throws Exception
     */
    private HarvestingProperties retrieveHarvestingProperties() throws Exception {

	Folder sourceFolder = getMetaFolder();

	InputStream binary = sourceFolder.getBinary(HarvestingProperties.getFileName());
	return HarvestingProperties.fromStream(binary);
    }

    /**
     * @param markLogicDB
     * @param writingFolder
     * @throws Exception
     */
    private void updateHarvestingProperties(MarkLogicDatabase markLogicDB, Folder writingFolder) throws Exception {

	Folder sourceFolder = getMetaFolder();
	HarvestingProperties properties = new HarvestingProperties();
	int harvCount = 1;

	if (sourceFolder.exists(HarvestingProperties.getFileName())) {

	    properties = retrieveHarvestingProperties();

	    harvCount = properties.getHarvestingCount();
	    if (harvCount == -1) {
		harvCount = 1;
	    } else {
		harvCount += 1;
	    }
	}

	properties.setHarvestingCount(harvCount);

	int size = -1;

	if (writingFolder == null) {

	    warn("Unable to detect harvesting folder, setting negative count");

	    updateErrorsAndWarnHarvestingReport("Unable to detect harvesting folder, setting negative count", false);

	} else {

	    size = writingFolder.size();
	}

	properties.setResourcesCount(size);

	if (startTimeStamp != null) {
	    properties.setStartHarvestingTimestamp(startTimeStamp);
	} else {
	    warn("Start time stamp field is lost");
	}

	properties.setEndHarvestingTimestamp();

	storeHarvestingProperties(properties);

	//
	//
	//
	MarkLogicIndexesManager idxManager = new MarkLogicIndexesManager(markLogicDB, false);

	String sourceId = sourceFolder.getSimpleName().replace(META_PREFIX, "");
	String indexName = createDataFolderIndexName(sourceId);

	debug("Handling data folder index file STARTED");

	if (!idxManager.rangeIndexExists(//
		indexName, //
		MarkLogicScalarType.STRING)) {

	    debug("Adding data folder range index STARTED");

	    idxManager.addRangeIndex(indexName, //
		    MarkLogicScalarType.STRING.getType());

	    debug("Adding data folder range index ENDED");
	}

	Folder[] folders = markLogicDB.getFolders();
	for (Folder folder : folders) {
	    debug("Available folder [" + folder.getCompleteName() + "]");
	}

	debug("Suite  id [" + markLogicDB.getSuiteIdentifier() + "]");
	debug("Source id [" + sourceId + "]");

	Folder dataFolder = Arrays.asList(markLogicDB.getFolders()).//
		stream().//

		filter(f -> f.getCompleteName().equals(markLogicDB.getSuiteIdentifier() + "_" + sourceId + DATA_1_PREFIX) || //
			f.getCompleteName().equals(markLogicDB.getSuiteIdentifier() + "_" + sourceId + DATA_2_PREFIX))
		.

		findFirst().//
		get();

	String dataFolderPostFix = dataFolder.getCompleteName().endsWith(DATA_1_PREFIX) ? DATA_1_PREFIX : DATA_2_PREFIX;

	debug("Data folder postfix [" + dataFolderPostFix + "]");

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		dataFolderPostFix);

	debug("Index name [" + indexName + "]");

	if (sourceFolder.exists(indexName)) {

	    debug("Replacing index STARTED");

	    boolean replaced = sourceFolder.replace(indexName, doc.getDocument());

	    debug("Replacing index " + (replaced ? "SUCCEEDED" : "FAILED"));

	    debug("Replacing index ENDED");

	} else {

	    debug("Storing index STARTED");

	    boolean stored = sourceFolder.store(indexName, doc.getDocument());

	    debug("Storing index " + (stored ? "SUCCEEDED" : "FAILED"));

	    debug("Storing index ENDED");

	}

	debug("Handling data folder index file ENDED");
    }

    /**
     * @param message
     */
    private void debug(String message) {

	GSLoggerFactory.getLogger(getClass()).debug(message);
	report.add(message);
    }

    /**
     * @param message
     * @param target
     */
    private void debug(String message, Object target) {

	GSLoggerFactory.getLogger(getClass()).debug(message, target);
	String reportMessage = message.replace("{}", String.valueOf(target));
	report.add(reportMessage);
    }

    /**
     * @param message
     */
    private void error(String message) {

	GSLoggerFactory.getLogger(getClass()).error(message);
	report.add(message);
    }

    /**
     * @param message
     * @param ex
     */
    private void error(Exception ex) {

	GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	report.add(ex.getMessage());
    }

    /**
     * @param message
     */
    private void warn(String message) {

	GSLoggerFactory.getLogger(getClass()).warn(message);
	report.add(message);
    }

    private boolean isWritingData1Folder() throws Exception {

	if (existsData1Folder()) {

	    return getData1Folder().exists(WRITING_FOLDER);
	}

	return false;
    }

    private boolean isWritingData2Folder() throws Exception {

	if (existsData2Folder()) {

	    return getData2Folder().exists(WRITING_FOLDER);
	}

	return false;
    }

    private void markDeletedRecords() throws RequestException, Exception {

	if (existsData1Folder() && existsData2Folder()) {

	    Folder newFolder = getWritingFolder();
	    Folder oldFolder = null;

	    if (isWritingData1Folder()) {
		oldFolder = getData2Folder();
	    } else {
		oldFolder = getData1Folder();
	    }

	    // 1) get the original ids of the old folder and the original ids from the new folder
	    List<String> newIds = markLogicDB.getOriginalIDs(newFolder.getCompleteName(), false);
	    List<String> oldIds = markLogicDB.getOriginalIDs(oldFolder.getCompleteName(), false);

	    // 2) removes all the ids of the new folder from the ids of the old folder.
	    // the remaining are the original ids of the removed resources (they are in the old folder
	    // but no longer in the new one)
	    oldIds.removeAll(newIds);

	    debug("Found " + oldIds.size() + " deleted records");

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier(oldFolder.getSimpleName());

	    IterationLogger logger = new IterationLogger(this, oldIds.size());
	    logger.setMessage("Total progress: ");

	    for (String string : oldIds) {
		// 3) get the resource with the removed original id from the old folder
		GSResource resource = reader.getResource(string, gsSource, true);
		// 4) marks it as deleted (some resources can be already be tagged from previous harvesting)
		if (!resource.getPropertyHandler().isDeleted()) {
		    resource.getPropertyHandler().setIsDeleted(true);
		}
		// 5) copies the resource in the new folder
		newFolder.store(resource.getPrivateId(), resource.asDocument(false));

		logger.iterationDone();
	    }
	}
    }

    private void testISOCompliance() throws Exception {

	Folder newFolder = getWritingFolder();

	// 1) get the original ids of the old folder and the original ids from the new folder
	List<String> newIds = markLogicDB.getOriginalIDs(newFolder.getCompleteName(), false);

	Folder oldFolder = null;

	if (isWritingData1Folder() && existsData2Folder()) {
	    oldFolder = getData2Folder();
	} else if (isWritingData2Folder() && existsData1Folder()) {
	    oldFolder = getData1Folder();
	}

	if (oldFolder != null) {
	    List<String> oldIds = markLogicDB.getOriginalIDs(oldFolder.getCompleteName(), false);

	    // 2) removes all the old ids from the new folder ids
	    // the remaining are the ids of the new resources
	    newIds.removeAll(oldIds);
	}

	debug("Found " + newIds.size() + " new records");

	GSSource gsSource = new GSSource();
	gsSource.setUniqueIdentifier(newFolder.getSimpleName());

	IterationLogger logger = new IterationLogger(this, newIds.size());
	logger.setMessage("Total progress: ");

	for (String originalId : newIds) {

	    // 3) get the resource with the original id from the new folder
	    GSResource resource = reader.getResource(originalId, gsSource, true);

	    SchemaValidator sv = new SchemaValidator();

	    try {

		MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

		BooleanValidationHandler handler = sv.validate( //
			miMetadata.asStream(), //
			CommonSchemas.GMI()); //

		// 4) set the validation tag
		resource.getPropertyHandler().setIsISOCompliant(handler.isValid());

	    } catch (JAXBException e) {

		error("Error occurred during validation: " + e.getMessage());
		error(e);
	    }

	    // 5) replaces the resource in the new folder
	    newFolder.replace(resource.getPrivateId(), resource.asDocument(false));

	    logger.iterationDone();
	}
    }

    //
    //
    // from GIP-423 this phase is no longer required, since resources with same original id of the same source
    // are no longer replaced (this was the reason why this phase was necessary, because the new resource with same
    // original id lacked the tags possibly present in the previous copy of the resource)
    //
    //
    private void recoverTags() throws Exception {

	if (existsData1Folder() && existsData2Folder()) {

	    Folder newFolder = getWritingFolder();
	    Folder oldFolder = null;

	    if (isWritingData1Folder()) {
		oldFolder = getData2Folder();
	    } else {
		oldFolder = getData1Folder();
	    }

	    List<String> newIds = markLogicDB.getOriginalIDs(newFolder.getCompleteName(), false);
	    List<String> oldIds = markLogicDB.getOriginalIDs(oldFolder.getCompleteName(), false);

	    @SuppressWarnings("unchecked")
	    Collection<String> intersection = CollectionUtils.intersection(newIds, oldIds);

	    if (!intersection.isEmpty()) {

		debug("Found " + intersection.size() + " common records to check");

		GSSource gsSource = new GSSource();
		gsSource.setUniqueIdentifier(oldFolder.getSimpleName());

		IterationLogger logger = new IterationLogger(this, intersection.size());
		logger.setMessage("Total progress: ");

		for (String id : intersection) {

		    // get the two resources with same original id (the old one and the new one)
		    List<GSResource> resources = reader.getResources(id, gsSource, true);

		    debug("Found {} resources with same identifier", resources.size());

		    // sorts the two resources according to their time stamp
		    List<GSResource> sortedResources = resources.//
			    stream().//
			    sorted((r1, r2) -> //
			    r1.getPropertyHandler().getResourceTimeStamp().get().//
				    compareTo(//
					    r2.getPropertyHandler().getResourceTimeStamp().get()))
			    .//
			    collect(Collectors.toList());

		    GSResource oldResource = sortedResources.get(0);
		    GSResource newResource = sortedResources.get(1);

		    recoverAugmentedElements(oldResource, newResource);

		    recoverExtendedElements(oldResource, newResource);

		    // replaces the resource
		    newFolder.replace(newResource.getPrivateId(), newResource.asDocument(false));

		    logger.iterationDone();
		}
	    }
	}
    }

    private void recoverAugmentedElements(GSResource oldResource, GSResource newResource) {

	List<AugmentedMetadataElement> oldElements = oldResource.getHarmonizedMetadata().getAugmentedMetadataElements();
	List<AugmentedMetadataElement> newElements = newResource.getHarmonizedMetadata().getAugmentedMetadataElements();

	Map<String, List<AugmentedMetadataElement>> collect = //
		Stream.concat(oldElements.stream(), newElements.stream()).//
			collect(Collectors.groupingBy(AugmentedMetadataElement::getName));

	Set<String> elNames = collect.keySet();
	for (String name : elNames) {

	    List<AugmentedMetadataElement> distinct = collect.get(name).//
		    stream().//
		    distinct().//
		    collect(Collectors.toList());

	    newElements.removeIf(e -> e.getName().equals(name));
	    newElements.addAll(distinct);
	}
    }

    private void recoverExtendedElements(GSResource oldResource, GSResource newResource) {

	{
	    ExtensionHandler oldHandler = oldResource.getExtensionHandler();
	    ExtensionHandler newHandler = newResource.getExtensionHandler();

	    // at the moment, the properties currently handled by the ExtendedMetadataHandler are set
	    // at harvesting time by the mappers, so there is no need to adapt....
	    // this is done just in case for some reason, the mappers fail to add the extensions
	    oldHandler.adapt(newHandler, AdaptPolicy.ON_EMPTY);
	}

	{
	    ReportsMetadataHandler oldHandler = new ReportsMetadataHandler(oldResource);
	    ReportsMetadataHandler newHandler = new ReportsMetadataHandler(newResource);

	    // the old reports are copied only if the new resource has no reports at all
	    // (that is, the AccessAugmenter was disabled)
	    // this way more recent properties are preserved
	    oldHandler.adapt(newHandler, AdaptPolicy.ON_EMPTY);
	}
    }

    private void addMetaFolder() throws RequestException {

	markLogicDB.addFolder(sourceId + META_PREFIX);
    }

    private Folder getMetaFolder() throws RequestException {

	return markLogicDB.getFolder(sourceId + META_PREFIX);
    }

    private String getMetaFolderName() {

	return sourceId + META_PREFIX;
    }

    private void addData2Folder() throws RequestException {

	markLogicDB.addFolder(sourceId + DATA_2_PREFIX);
    }

    private void addData1Folder() throws RequestException {

	markLogicDB.addFolder(sourceId + DATA_1_PREFIX);
    }

    private void removeData1Folder() throws RequestException {

	markLogicDB.removeFolder(sourceId + DATA_1_PREFIX);
    }

    private void removeData2Folder() throws RequestException {

	markLogicDB.removeFolder(sourceId + DATA_2_PREFIX);
    }
}
