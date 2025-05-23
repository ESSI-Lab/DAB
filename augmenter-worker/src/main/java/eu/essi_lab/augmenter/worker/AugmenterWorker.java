package eu.essi_lab.augmenter.worker;

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

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.utils.PropertiesUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.AugmenterProperties;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.views.DefaultViewManager;

/**
 * @author Fabrizio
 */
public class AugmenterWorker extends SchedulerWorker<AugmenterWorkerSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "AugmenterWorker";
    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public AugmenterWorker() {

    }

    /**
     * @param setting
     */
    public AugmenterWorker(AugmenterWorkerSetting setting) {

	super(setting);

	this.setting = setting;
    }

    @Override
    protected AugmenterWorkerSetting initSetting() {

	return new AugmenterWorkerSettingImpl();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Augmentation STARTED");

	AugmentationReportsHandler.sendAugmentationEmail(true, context.isRecovering(), getSetting());

	StorageInfo storageURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader reader = DatabaseProviderFactory.getReader(storageURI);
	DatabaseWriter writer = DatabaseProviderFactory.getWriter(storageURI);
	DatabaseFinder finder = DatabaseProviderFactory.getFinder(storageURI);

	DiscoveryMessage message = new DiscoveryMessage();

	//
	// some augmenters can mark as deleted the augmented records
	// and if the deleted are excluded, the process can end before the processing of all the resources
	//
	message.setIncludeDeleted(true);

	message.setRequestId(getClass().getSimpleName() + "-" + getSetting().getIdentifier());

	LogicalBond andBond = BondFactory.createAndBond();
	message.setPermittedBond(andBond);

	// this is to avoid to retrieve records harvested after the beginning of this process
	andBond.getOperands()
		.add(BondFactory.createResourceTimeStampBond(BondOperator.LESS_OR_EQUAL, ISO8601DateTimeUtils.getISO8601DateTime()));

	//
	// ordering set option
	//

	handleSortOption(getSetting(), status, message);

	//
	// view option
	//

	handleViewOption(getSetting(), reader, andBond);

	//
	// max age option
	//

	handleMaxAgeOption(getSetting(), status, andBond);

	//
	// sources bond
	//

	handleSelectedSources(getSetting(), status, message, andBond);

	//
	// count request
	//

	GSLoggerFactory.getLogger(getClass()).debug("Count request STARTED");

	int count = finder.count(message).getCount();

	GSLoggerFactory.getLogger(getClass()).debug("Count request ENDED");

	if (count == 0) {

	    GSLoggerFactory.getLogger(getClass()).warn("No resource to augment, exit");
	    status.addWarningMessage("No resource to augment");

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Found [{}] resources to augment", StringUtils.format((double) count));
	status.addInfoMessage("Found [" + StringUtils.format((double) count) + "] resources to augment");

	//
	// max records
	//

	count = handleMaxRecordsOption(getSetting(), count);

	//
	//
	//

	int start = 1;
	int iterationsCount = 1;
	int pageSize = DEFAULT_PAGE_SIZE;

	GSLoggerFactory.getLogger(getClass()).debug("Get properties file STARTED");

	Optional<AugmenterProperties> optProperties = getProperties(getSetting(), reader.getDatabase());

	GSLoggerFactory.getLogger(getClass()).debug("Get properties file ENDED");

	AugmenterProperties properties = null;

	if (optProperties.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Found properties file");

	    properties = optProperties.get();

	    start = properties.getStart();
	    iterationsCount = properties.getIterationsCount();

	    GSLoggerFactory.getLogger(getClass()).debug("Resuming job from start index [{}]", start);

	    count = count - start;

	    GSLoggerFactory.getLogger(getClass()).debug("Updating number of resources to augment: {} ", StringUtils.format((double) count));
	    status.addInfoMessage("Updating number of resources to augment: " + StringUtils.format((double) count));

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Properties file not found");

	    properties = createProperties(getSetting(), reader.getDatabase());
	}

	IterationLogger logger = new IterationLogger(this, iterationsCount, count, pageSize);
	logger.setMessage("Augmentation status of '" + getSetting().getWorkerName() + "': ");

	// sorts the augmenters according to their priority the lower value, the higher the priority
	List<Augmenter> augmenters = sortAugmenters(getSetting());

	boolean errorOccurred = false;

	do {

	    logger.iterationStarted();

	    Page page = new Page(start, pageSize);
	    message.setPage(page);

	    ResultSet<GSResource> response = finder.discover(message);

	    List<GSResource> resources = response.getResultsList();

	    for (GSResource resource : resources) {

		Optional<GSResource> optResource = Optional.empty();
		List<GSKnowledgeResourceDescription> concepts = new ArrayList<>();

		for (Augmenter augmenter : augmenters) {

		    try {

			optResource = augmenter.augment(resource);

			if (optResource.isPresent()) {

			    // if the resource is augmented, uses the augmented resource to generates the concepts
			    concepts = augmenter.generate(optResource.get());

			} else {

			    concepts = augmenter.generate(resource);
			}

		    } catch (Exception ex) {

			GSLoggerFactory.getLogger(getClass()).warn("Augmentation error occurred: " + ex.getMessage());
			GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);

			errorOccurred = true;
		    }
		}

		// if the resource is augmented, it is updated
		if (optResource.isPresent()) {

		    writer.update(optResource.get());
		}

		if (!concepts.isEmpty()) {

		    for (GSKnowledgeResourceDescription concept : concepts) {

			writer.store(concept);
		    }
		}
	    }

	    //
	    //
	    //

	    start += pageSize;

	    logger.iterationEnded();

	    properties.setStart(start);
	    properties.setTimestamp();
	    properties.setProgress(logger);

	    updateProperties(properties, getSetting(), reader);

	    //
	    //
	    //

	    if (ConfigurationWrapper.isJobCanceled(context)) {

		GSLoggerFactory.getLogger(getClass()).trace("Augmentation canceled");

		status.setPhase(JobPhase.CANCELED);

		break;
	    }
	} while (start < count);

	boolean removed = removeProperties(properties, getSetting(), reader);

	if (!removed) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to remove properties file");
	    status.addErrorMessage("Unable to remove properties file");
	}

	if (errorOccurred) {

	    status.addErrorMessage("One or more augmentation error occurred");
	}

	GSLoggerFactory.getLogger(getClass()).debug("Augmentation ENDED");

	AugmentationReportsHandler.sendAugmentationEmail(false, context.isRecovering(), getSetting());
    }

    /**
     * @param setting
     * @return
     */
    @SuppressWarnings("rawtypes")
    private List<Augmenter> sortAugmenters(AugmenterWorkerSetting setting) {

	return getSetting().//
		getSelectedAugmenterSettings().//
		stream().//
		sorted(Comparator.comparing(AugmenterSetting::getPriority)).//
		map(s -> (Augmenter) s.createConfigurableOrNull()). //
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    /**
     * @param setting
     * @param count
     * @return
     */
    private int handleMaxRecordsOption(AugmenterWorkerSetting setting, int count) {

	int out = count;

	int maxRecords = setting.getMaxRecords();

	if (out > maxRecords && maxRecords > 0) {

	    GSLoggerFactory.getLogger(getClass()).debug("Number of records to augment limited to [{}]",
		    StringUtils.format((double) maxRecords));

	    out = maxRecords;
	}

	return out;
    }

    /**
     * @param setting
     * @param status
     * @param message
     * @param andBond
     */
    private void handleSelectedSources(AugmenterWorkerSetting setting, SchedulerJobStatus status, DiscoveryMessage message,
	    LogicalBond andBond) {

	List<GSSource> selectedSources = getSetting().getSelectedSources();

	message.setSources(selectedSources);

	GSLoggerFactory.getLogger(getClass()).debug("Number of sources to augment: {}", selectedSources.size());
	status.addInfoMessage("Number of sources to augment: " + selectedSources.size());

	andBond.getOperands().add(getSetting().getSourcesBond());
    }

    /**
     * @param setting
     * @param dataBaseReader
     * @param andBond
     * @throws GSException
     */
    private void handleViewOption(AugmenterWorkerSetting setting, DatabaseReader dataBaseReader, LogicalBond andBond) throws GSException {

	Optional<String> viewIdentifier = getSetting().getViewIdentifier();

	if (viewIdentifier.isPresent()) {

	    DefaultViewManager manager = new DefaultViewManager();
	    manager.setDatabaseReader(dataBaseReader);

	    Optional<View> resolvedView = manager.getResolvedView(viewIdentifier.get());

	    if (resolvedView.isPresent()) {

		andBond.getOperands().add(resolvedView.get().getBond());

		GSLoggerFactory.getLogger(getClass()).debug("View " + viewIdentifier.get() + " resolved");

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Unable to resolve view: " + viewIdentifier.get());
	    }
	}
    }

    /**
     * @param setting
     * @param status
     * @param message
     */
    private void handleSortOption(AugmenterWorkerSetting setting, SchedulerJobStatus status, DiscoveryMessage message) {

	boolean sorting = getSetting().isLessRecentSortSet();

	GSLoggerFactory.getLogger(getClass()).debug("Less recent ordering set: {} ", sorting);
	status.addInfoMessage("Less recent ordering set: " + sorting);

	if (sorting) {

	    message.setSortedFields(SortedFields.of(ResourceProperty.RESOURCE_TIME_STAMP, SortOrder.ASCENDING));
	}
    }

    /**
     * @param setting
     * @param status
     * @param andBond
     */
    private void handleMaxAgeOption(AugmenterWorkerSetting setting, SchedulerJobStatus status, LogicalBond andBond) {

	int maxAge = getSetting().getMaxAge();

	if (maxAge > 0) {

	    GSLoggerFactory.getLogger(getClass()).debug("Maximum age: {} ", maxAge);
	    status.addInfoMessage("Maximum age: " + maxAge);

	    long timeMillis = System.currentTimeMillis();
	    long startTime = timeMillis - TimeUnit.MINUTES.toMillis(maxAge);

	    String startTimeS = ISO8601DateTimeUtils.getISO8601DateTime(new Date(startTime));

	    GSLoggerFactory.getLogger(getClass()).debug("Minimum resource time stamp set to: " + startTimeS);
	    status.addInfoMessage("Minimum resource time stamp set to: " + startTimeS);

	    andBond.getOperands().add(//
		    BondFactory.createResourceTimeStampBond(BondOperator.GREATER_OR_EQUAL, startTimeS));
	}
    }

    /**
     * @param setting
     * @param dataBaseWriter
     * @return
     * @throws Exception
     */
    private AugmenterProperties createProperties(AugmenterWorkerSetting setting, Database database) throws Exception {

	DatabaseFolder folder = getPropertiesFolder(getSetting(), database).get();

	String fileName = setting.getIdentifier() + ".properties";
	AugmenterProperties augmenterProperties = new AugmenterProperties();

	folder.store(fileName, FolderEntry.of(augmenterProperties.asStream()), EntryType.AUGMENTER_PROPERTIES);

	return augmenterProperties;
    }

    /**
     * @param setting
     * @return
     * @throws GSException
     */
    private Optional<AugmenterProperties> getProperties(AugmenterWorkerSetting setting, Database database) throws Exception {

	DatabaseFolder folder = getPropertiesFolder(getSetting(), database).get();

	Optional<String> fileName = Arrays.asList(folder.listKeys()).//
		stream().//
		filter(key -> key.startsWith(setting.getIdentifier())).//
		findFirst();

	GSLoggerFactory.getLogger(getClass()).debug("Looking for file {}", setting.getIdentifier() + ".json");

	if (fileName.isPresent()) {

	    InputStream binary = folder.getBinary(fileName.get());

	    AugmenterProperties properties = PropertiesUtils.fromStream(binary, AugmenterProperties.class);

	    return Optional.of(properties);
	}

	return Optional.empty();
    }

    /**
     * @param properties
     * @param setting
     * @param dataBaseWriter
     * @throws Exception
     */
    private void updateProperties(//
	    AugmenterProperties properties, //
	    AugmenterWorkerSetting setting, //
	    DatabaseReader reader) throws Exception {

	DatabaseFolder folder = getPropertiesFolder(getSetting(), reader.getDatabase()).get();

	String fileName = setting.getIdentifier() + ".properties";

	folder.replace(fileName, FolderEntry.of(properties.asStream()), EntryType.AUGMENTER_PROPERTIES);
    }

    /**
     * @param properties
     * @param setting
     * @param reader
     * @return
     * @throws Exception
     */
    private boolean removeProperties(AugmenterProperties properties, AugmenterWorkerSetting setting, DatabaseReader reader)
	    throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Removing properties file STARTED");

	DatabaseFolder folder = getPropertiesFolder(getSetting(), reader.getDatabase()).get();

	String fileName = setting.getIdentifier() + ".properties";

	boolean removed = folder.remove(fileName);

	GSLoggerFactory.getLogger(getClass()).debug("Removing properties file ENDED");

	return removed;
    }

    /**
     * @param setting
     * @param reader
     * @return
     * @throws GSException
     */
    private Optional<DatabaseFolder> getPropertiesFolder(AugmenterWorkerSetting setting, Database database) throws GSException {

	String folderName = null;
	StorageInfo storageInfo = database.getStorageInfo();

	if (storageInfo == null) {
	    // this is in case of VolatileDatabaseReader
	    folderName = "propertiesFolder";

	} else {

	    folderName = storageInfo.getIdentifier() + "_" + Database.AUGMENTERS_FOLDER;
	}

	return database.getFolder(folderName, true);
    }

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }

    @Override
    protected void storeJobStatus(SchedulerJobStatus status) throws GSException {

	SchedulerViewSetting setting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(setting);

	try {
	    scheduler.setJobStatus(status);

	} catch (SQLException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store status");
	    GSLoggerFactory.getLogger(getClass()).error(status.toString());
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
