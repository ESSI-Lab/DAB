package eu.essi_lab.harvester;

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

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.quartz.JobExecutionContext;

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.HarvestingEmbeddedTask;
import eu.essi_lab.cfga.gs.task.HarvestingEmbeddedTask.ExecutionStage;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.harvester.worker.HarvesterWorker.RecoveringContext;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class Harvester {

    private HarvesterPlan harvesterPlan;
    private SourceStorage sourceStorage;
    private HarvestingReportsHandler reportsHandler;
    private HarvestingEmbeddedTask customTask;
    @SuppressWarnings("rawtypes")
    private IHarvestedAccessor harvesterAccessor;

    /**
     * 
     */
    private static final String UNEXPECTED_NO_LIST_RECORDS_ERROR_RESPONSE_ERROR = "UNEXPECTED_NO_LIST_RECORDS_ERROR_RESPONSE_ERROR";
    private static final String HARVESTER_CUSTOM_TASK_ERROR = "HARVESTER_CUSTOM_TASK_ERROR";

    /**
     * 
     */
    public Harvester() {

	harvesterPlan = new HarvesterPlan();
    }

    /**
     * @param isRecovering
     * @throws GSException
     */
    public void harvest(Boolean isRecovering) throws GSException {

	harvest(RecoveringContext.create(isRecovering), null);
    }

    /**
     * @param context
     * @param status
     * @throws GSException
     */
    public void harvest(JobExecutionContext context, SchedulerJobStatus status) throws GSException {

	GSException exception = null;

	HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource());

	//
	// incremental harvesting
	//

	boolean isIncremental = isIncrementalHarvestingSupported();
	HarvestingStrategy strategy = computeHarvestingStrategy(isIncremental);

	//
	// resumed harvesting
	//

	boolean resumed = properties.isResumed(context.isRecovering()) && getAccessor().supportsResumedHarvesting();
	if (resumed) {
	    GSLoggerFactory.getLogger(this.getClass()).debug("Resumed harvesting enabled");
	}

	//
	// recovery
	//

	boolean recovery = context.isRecovering() && getAccessor().supportsRecovery();

	// always reset the completed state
	properties.setCompleted(false);

	//
	// sends the harvesting started email
	//

	GSLoggerFactory.getLogger(this.getClass()).info("Harvest of source {} [{}] STARTED with recovery flag {} / resumed flag {}",

		getAccessor().getSource().getLabel(), //
		getAccessor().getSource().getUniqueIdentifier(), //
		recovery, //
		resumed);

	reportsHandler = new HarvestingReportsHandler(getAccessor().getSource(), getSourceStorage());

	reportsHandler.sendHarvestingEmail(//
		true, //
		getAccessor().getSource(), //
		recovery, //
		resumed, //
		null, //
		properties);

	//
	// starts the harvesting procedure
	//

	ListRecordsRequest request = new ListRecordsRequest(status);

	//
	// set the source storage worker to the request
	//
	SourceStorageWorker worker = getSourceStorage().getDatabase().getWorker(getAccessor().getSource().getUniqueIdentifier());
	request.setAdditionalInfo(
		GSPropertyHandler.of(new GSProperty<SourceStorageWorker>(ListRecordsRequest.SOURCE_STORAGE_WORKER_PROPERTY, worker)));

	try {

	    getSourceStorage().harvestingStarted(//
		    getAccessor().getSource(), //
		    strategy, //
		    recovery, //
		    resumed, //
		    Optional.ofNullable(status), //
		    Optional.of(request));

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(this.getClass()).//
		    error("Fatal error occurred: the harvesting START procedure is failed: " + ex.getMessage(), ex);

	    reportsHandler.gatherGSException(ex);

	    reportsHandler.sendErrorAndWarnMessageEmail();

	    throw ex;
	}

	//
	// executes the harvesting procedure
	//

	boolean harvestingInterrupted = false;

	try {

	    request.setHarvestingProperties(properties);

	    if (isIncremental) {

		GSLoggerFactory.getLogger(this.getClass()).debug("Incremental harvesting enabled");

		String fromDateStamp = properties.getStartHarvestingTimestamp();
		String untilDateStamp = ISO8601DateTimeUtils.getISO8601DateTime();

		request.setFromDateStamp(fromDateStamp);
		request.setUntilDateStamp(untilDateStamp);

	    } else {

		GSLoggerFactory.getLogger(this.getClass()).debug("Incremental harvesting not enabled");
	    }

	    // set the request flags
	    request.setRecovered(recovery);
	    request.setResumed(resumed);

	    doHarvest(request, context, recovery, resumed, isIncremental, properties, status);

	} catch (GSException ex) {

	    harvestingInterrupted = true;

	    exception = ex;
	    reportsHandler.gatherGSException(ex);
	}

	//
	// finalizes the harvesting procedure
	//

	try {

	    // the harvesting procedure is completed only when the procedure ends without errors
	    if (!harvestingInterrupted) {

		properties.setCompleted(true);
	    }

	    ExecutionStage executionStage = null;

	    if (customTask != null) {

		executionStage = customTask.getExecutionStage();
	    }

	    if (executionStage != null && executionStage == ExecutionStage.BEFORE_HARVESTING_END) {

		handleCustomTask(getAccessor().getSource(), context, status);
	    }

	    getSourceStorage().harvestingEnded(//
		    getAccessor().getSource(), //
		    Optional.of(properties), //
		    strategy, //
		    Optional.ofNullable(status));

	    if (executionStage != null && executionStage == ExecutionStage.AFTER_HARVESTING_END) {

		handleCustomTask(getAccessor().getSource(), context, status);
	    }

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(this.getClass()).//
		    error("Fatal error occurred: the harvesting END procedure is failed: " + ex.getMessage(), ex);

	    if (exception != null) {

		exception.getErrorInfoList().addAll(ex.getErrorInfoList());

	    } else {

		exception = ex;
	    }

	    reportsHandler.gatherGSException(exception);
	}

	//
	//
	//

	GSLoggerFactory.getLogger(this.getClass()).info("Harvest of source {} [{}] ENDED", //
		getAccessor().getSource().getLabel(), //
		getAccessor().getSource().getUniqueIdentifier());

	reportsHandler.sendHarvestingEmail(//
		false, //
		getAccessor().getSource(), //
		recovery, //
		resumed, //
		getSourceStorage().getStorageReport(getAccessor().getSource()), //
		properties);

	reportsHandler.sendErrorAndWarnMessageEmail();

	//
	//
	//

	if (exception != null) {

	    throw exception;
	}
    }

    /**
     * @param customTask
     */
    public void setCustomTask(HarvestingEmbeddedTask customTask) {

	this.customTask = customTask;
    }

    /**
     * @return
     */
    public HarvestingReportsHandler getReportsHandler() {

	return reportsHandler;
    }

    @SuppressWarnings("rawtypes")
    public void setAccessor(IHarvestedAccessor harvesterAccessor) {

	this.harvesterAccessor = harvesterAccessor;
    }

    /**
     * @param sourceStorage
     */
    public void setSourceStorage(SourceStorage sourceStorage) {

	this.sourceStorage = sourceStorage;
    }

    /**
     * @return
     */
    public boolean isIncrementalHarvestingSupported() {

	try {

	    HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource());

	    String startHarvestTimestamp = properties.getStartHarvestingTimestamp();

	    return getAccessor().supportsIncrementalHarvesting() && startHarvestTimestamp != null && !startHarvestTimestamp.isEmpty();

	} catch (Exception t) {

	    GSLoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
	}

	// in case of error, returning false is the most
	// conservative solution
	return false;
    }

    /**
     * @return
     */
    public HarvesterPlan getPlan() {

	return this.harvesterPlan;
    }

    /**
     * @param gsSource
     * @param context
     * @param status
     */
    private void handleCustomTask(GSSource gsSource, JobExecutionContext context, SchedulerJobStatus status) {

	if (customTask != null) {

	    try {

		GSLoggerFactory.getLogger(getClass()).info("Execution of custom task {} STARTED", customTask.getName());

		customTask.setSource(gsSource);
		customTask.doJob(context, status);

		GSLoggerFactory.getLogger(getClass()).info("Execution of custom task {} ENDED", customTask.getName());

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		reportsHandler.gatherGSException(GSException.createException(getClass(), HARVESTER_CUSTOM_TASK_ERROR, e));
	    }
	}
    }

    /**
     * @param request
     * @param context
     * @param isIncremental
     * @param resumed
     * @param properties
     * @param status
     * @throws GSException
     */
    @SuppressWarnings("unchecked")
    private void doHarvest(//
	    ListRecordsRequest request, //
	    JobExecutionContext context, //
	    boolean recovery, //
	    boolean resumed, //
	    boolean isIncremental, //
	    HarvestingProperties properties, //
	    SchedulerJobStatus status) throws GSException {

	String resumptionToken = null;

	if (request.isRecovered() || resumed) {

	    resumptionToken = findRecoveryToken();

	    String info = request.isRecovered() ? "recovery" : "resume";

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application STARTED (" + info + ") - resumption token: {}",
		    resumptionToken);

	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application STARTED (not recovery nor resume)");
	}

	//
	//
	//

	do {

	    request.setResumptionToken(resumptionToken);

	    //
	    // updates the recovery/resuming RESUMPTION token before the plan application
	    //
	    updateToken(properties, resumptionToken, false);

	    //
	    // updates the recovery REMOVAL token before the plan application.
	    // the records with this token after a crash, will be removed before the recovery begins
	    //
	    String recoveryRemovalToken = UUID.randomUUID().toString();

	    updateToken(properties, UUID.randomUUID().toString(), true);

	    ListRecordsResponse<GSResource> response;

	    try {

		response = getAccessor().listRecords(request);

	    } catch (Throwable t) {

		if (t instanceof GSException) {

		    throw t;
		}

		throw GSException.createException(//
			getClass(), //
			t.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			UNEXPECTED_NO_LIST_RECORDS_ERROR_RESPONSE_ERROR);
	    }

	    applyHarvestingPlan(//
		    response.getRecords(), //
		    request.isFirstHarvesting(), //
		    recovery, //
		    isIncremental, //
		    resumptionToken, //
		    recoveryRemovalToken, //
		    properties);

	    // now get the response token
	    resumptionToken = response.getResumptionToken();

	    // set the request flags
	    request.setRecovered(false);
	    request.setFirst(false);

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application ENDED");

	    if (!RecoveringContext.isRecoveringContext(context) && ConfigurationWrapper.isJobCanceled(context)) {

		GSLoggerFactory.getLogger(getClass()).trace("Harvesting canceled");

		status.setPhase(JobPhase.CANCELED);

		break;
	    }

	} while (resumptionToken != null);

    }

    /**
     * @param records
     * @param firstHarvesting
     * @param isRecovering
     * @param isIncremental
     * @param resumptionToken
     * @param recoveryRemovalToken
     * @param harvestingProperties
     */
    private void applyHarvestingPlan(//
	    Iterator<GSResource> records, //
	    boolean firstHarvesting, //
	    boolean isRecovering, //
	    boolean isIncremental, //
	    String resumptionToken, //
	    String recoveryRemovalToken, //
	    HarvestingProperties harvestingProperties) {

	while (records.hasNext()) {

	    GSResource resource = records.next();

	    //
	    // set the removal token used for the recovery. in case of recovery, resources
	    // having this token (which corresponds to the token written in the properties file)
	    // will be removed before recovery starts
	    //
	    resource.getPropertyHandler().setRecoveryRemovalToken(recoveryRemovalToken);

	    HarvesterPlan plan = getPlan();

	    plan.setResumptionToken(resumptionToken);
	    plan.setHarvestingProperties(harvestingProperties);
	    plan.setIsFirstHarvesting(firstHarvesting);

	    try {

		plan.apply(resource);

	    } catch (ConflictingResourceException ex) {

		reportsHandler.gatherConflictingResourceException(ex);

	    } catch (DuplicatedResourceException rex) {

		reportsHandler.gatherDuplicatedResourceException(rex);

	    } catch (HarvestingComponentException hce) {

		reportsHandler.gatherHarvesterComponentException(hce);
	    }
	}
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    private IHarvestedAccessor getAccessor() {

	return this.harvesterAccessor;
    }

    /**
     * @return
     */
    private SourceStorage getSourceStorage() {

	return sourceStorage;
    }

    /**
     * @param properties
     * @param token
     * @param removal
     * @throws GSException
     */
    private void updateToken(HarvestingProperties properties, String token, boolean removal) throws GSException {

	if (token == null) {

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).trace("Token: {} ", token);

	if (!removal) {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery/resuming resumption token STARTED");

	    properties.setRecoveryResumptionToken(token);

	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery removal token STARTED");

	    properties.setRecoveryRemovalToken(token);
	}

	GSSource source = getAccessor().getSource();

	getSourceStorage().storeHarvestingProperties(source, properties);

	if (!removal) {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery/resuming resumption token ENDED");

	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery removal token ENDED");
	}
    }

    /**
     * @param doIncrementalHarvesting
     * @return
     */
    private HarvestingStrategy computeHarvestingStrategy(boolean doIncrementalHarvesting) {

	if (doIncrementalHarvesting) {
	    return HarvestingStrategy.SELECTIVE;
	}

	return HarvestingStrategy.FULL;
    }

    /**
     * @return
     * @throws GSException
     */
    private String findRecoveryToken() throws GSException {

	GSLoggerFactory.getLogger(Harvester.class).trace("Finding recovery token STARTED");

	GSSource source = getAccessor().getSource();

	HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(source);

	String rt = properties.getRecoveryResumptionToken();

	GSLoggerFactory.getLogger(Harvester.class).trace("Finding recovery token ENDED");
	GSLoggerFactory.getLogger(Harvester.class).trace("Recovery token: {}", rt);

	return rt;
    }

}
