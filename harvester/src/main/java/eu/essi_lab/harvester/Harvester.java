package eu.essi_lab.harvester;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.harvester.component.HarvesterComponentException;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
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
    @SuppressWarnings("rawtypes")
    private IHarvestedAccessor harvesterAccessor;
    /**
     * 
     */
    private static final String UNEXPECTED_NO_LIST_RECORDS_ERROR_RESPONSE_ERROR = "UNEXPECTED_NO_LIST_RECORDS_ERROR_RESPONSE_ERROR";

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

	harvest(isRecovering, null);
    }

    /**
     * @param isRecovering
     * @param status
     * @throws GSException
     */
    public void harvest(Boolean isRecovering, SchedulerJobStatus status) throws GSException {

	GSException exception = null;

	boolean isIncremental = isIncrementalHarvestingSupported();
	HarvestingStrategy strategy = computeHarvestingStrategy(isIncremental);

	reportsHandler = new HarvestingReportsHandler(getAccessor().getSource(), getSourceStorage());

	reportsHandler.sendHarvestingEmail(//
		true, //
		getAccessor().getSource(), //
		isRecovering, //
		null, //
		getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource()));

	try {

	    getSourceStorage().harvestingStarted(getAccessor().getSource(), strategy, isRecovering, Optional.ofNullable(status));

	    GSLoggerFactory.getLogger(this.getClass()).info("Harvest of source {} [{}] STARTED with recovery flag {}",
		    getAccessor().getSource().getLabel(), getAccessor().getSource().getUniqueIdentifier(), isRecovering);

	    ListRecordsRequest request = new ListRecordsRequest(status);

	    HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource());
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

	    request.setRecovering(isRecovering);

	    doHarvest(request, isRecovering, isIncremental, properties);

	} catch (GSException ex) {

	    exception = ex;
	    reportsHandler.gatherGSException(exception);
	}

	try {

	    getSourceStorage().harvestingEnded(getAccessor().getSource(), strategy, Optional.ofNullable(status));

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(this.getClass()).error("Error occurred: the harvesting end procedure is failed");
	    exception = ex;
	    reportsHandler.gatherGSException(exception);
	}

	GSLoggerFactory.getLogger(this.getClass()).info("Harvest of source {} [{}] ENDED", getAccessor().getSource().getLabel(),
		getAccessor().getSource().getUniqueIdentifier());

	reportsHandler.sendHarvestingEmail(//
		false, //
		getAccessor().getSource(), //
		isRecovering, //
		getSourceStorage().getStorageReport(getAccessor().getSource()), //
		getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource()));

	reportsHandler.sendErrorAndWarnMessageEmail();

	if (exception != null) {

	    if (exception instanceof GSException) {

		((GSException) exception).log();
	    }

	    throw exception;
	}
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
     * @param recordRequest
     * @param isRecovering
     * @param isIncremental
     * @param properties
     * @throws GSException
     */
    @SuppressWarnings("unchecked")
    private void doHarvest(//
	    ListRecordsRequest recordRequest, //
	    Boolean isRecovering, //
	    boolean isIncremental, //
	    HarvestingProperties properties) throws GSException {

	String resumptionToken = null;

	if (recordRequest.getRecovering()) {
	    resumptionToken = findRecoveryToken();

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application STARTED (recovery) - resumption token: {}",
		    resumptionToken);
	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application STARTED (not recovery)");
	}

	recordRequest.setFirst(true);

	do {

	    recordRequest.setResumptionToken(resumptionToken);

	    //
	    // updates the recovery RESUMPTION token before the plan application
	    //
	    updateToken(resumptionToken, false);

	    //
	    // updates the recovery REMOVAL token before the plan application.
	    // the records with this token after a crash, will be removed before the recovery begins
	    //
	    String recoveryRemovalToken = UUID.randomUUID().toString();

	    updateToken(UUID.randomUUID().toString(), true);

	    ListRecordsResponse<GSResource> response;

	    try {

		response = getAccessor().listRecords(recordRequest);

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

	    applyHarvestPlan(//
		    response.getRecords(), //
		    recordRequest.isFirstHarvesting(), //
		    isRecovering, //
		    isIncremental, //
		    resumptionToken, //
		    recoveryRemovalToken, //
		    properties);

	    // now get the response token
	    resumptionToken = response.getResumptionToken();

	    recordRequest.setRecovering(false);
	    recordRequest.setFirst(false);

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application ENDED");

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
    private void applyHarvestPlan(//
	    Iterator<GSResource> records, //
	    boolean firstHarvesting, //
	    boolean isRecovering, //
	    boolean isIncremental, //
	    String resumptionToken, //
	    String recoveryRemovalToken, //
	    HarvestingProperties harvestingProperties) {

	while (records.hasNext()) {

	    GSResource resource = records.next();

	    // GSLoggerFactory.getLogger(getClass()).trace("Plan application on resource {} STARTED", resource);

	    //
	    // set the removal token used for the recovery. in case of recovery, resources
	    // having this token (which corresponds to the token written in the properties file)
	    // will be removed before recovery starts
	    //
	    resource.getPropertyHandler().setRecoveryRemovalToken(recoveryRemovalToken);

	    HarvesterPlan plan = getPlan();

	    plan.setResumptionToken(resumptionToken);
	    plan.setHarvestingProperties(harvestingProperties);
	    plan.setIsRecovering(isRecovering);
	    plan.setIsFirstHarvesting(firstHarvesting);
	    plan.setIsIncrementalHarvesting(isIncremental);

	    try {

		plan.apply(resource);

	    } catch (ConflictingResourceException ex) {

		reportsHandler.gatherConflictingResourceException(ex);

	    } catch (DuplicatedResourceException rex) {

		reportsHandler.gatherDuplicatedResourceException(rex);

	    } catch (HarvesterComponentException hce) {

		reportsHandler.gatherHarvesterComponentException(hce);
	    }

	    // GSLoggerFactory.getLogger(getClass()).trace("Plan application on resource {} ENDED", resource);
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
     * @param token
     * @param removal
     * @throws GSException
     */
    private void updateToken(String token, boolean removal) throws GSException {

	if (token == null) {

	    return;
	}

	if (!removal) {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery resumption token STARTED");

	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery removal token STARTED");
	}

	GSLoggerFactory.getLogger(getClass()).trace("Token {} ", token);

	GSSource source = getAccessor().getSource();

	HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(source);

	if (!removal) {

	    properties.setRecoveryResumptionToken(token);

	} else {

	    properties.setRecoveryRemovalToken(token);
	}

	getSourceStorage().storeHarvestingProperties(source, properties);

	if (!removal) {

	    GSLoggerFactory.getLogger(getClass()).trace("Updating recovery resumption token ENDED");

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
