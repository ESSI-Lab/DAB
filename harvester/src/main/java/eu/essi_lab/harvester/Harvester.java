package eu.essi_lab.harvester;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.harvester.component.HarvesterComponentException;
import eu.essi_lab.harvester.component.HarvesterPlan;
import eu.essi_lab.harvester.job.HarvesterJob;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
public class Harvester extends AbstractGSconfigurableComposed {

    @JsonIgnore
    public static final String HARVESTER_ACCESSOR_KEY = "HARVESTER_ACCESSOR_KEY";

    @JsonIgnore
    public static final String HARVESTER_PLAN_KEY = "HARVESTER_PLAN_KEY";

    @JsonIgnore
    private static final String AUGMENTER_KEY = "AUGMENTER_KEY";

    @JsonIgnore
    private transient SourceStorage sourceStorage;

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    @JsonIgnore
    private List<HarvesterJob> job;

    @JsonIgnore
    private HarvestingReportsHandler reportsHandler;

    private static final long serialVersionUID = -7071691875248551546L;

    public Harvester() {

	setLabel("Harvester");
	job = new ArrayList<>();

	// --------------------------------
	//
	// Set the harvesting options
	//
	//
	GSConfOptionBoolean deletedOption = SourceStorage.createMarkDeletedOption();
	getSupportedOptions().put(deletedOption.getKey(), deletedOption);
	
	GSConfOptionBoolean smartHarvestingOption = SourceStorage.createForceOverwriteOption();
	getSupportedOptions().put(smartHarvestingOption.getKey(), smartHarvestingOption);

	GSConfOptionBoolean isoOption = SourceStorage.createISOComplianceOption();
	getSupportedOptions().put(isoOption.getKey(), isoOption);

	GSConfOptionBoolean recoverOptions = SourceStorage.createRecoverTagsOption();
	getSupportedOptions().put(recoverOptions.getKey(), recoverOptions);
    }

    /**
     * @return
     */
    @JsonIgnore
    public HarvestingReportsHandler getReportsHandler() {

	return reportsHandler;
    }

    @JsonIgnore
    public void setAccessor(IHarvestedAccessor harvesterAccessor) {

	harvesterAccessor.setKey(HARVESTER_ACCESSOR_KEY);

	getConfigurableComponents().put(harvesterAccessor.getKey(), harvesterAccessor);
    }

    @JsonIgnore
    public IHarvestedAccessor getAccessor() {

	return (IHarvestedAccessor) getConfigurableComponents().get(HARVESTER_ACCESSOR_KEY);
    }

    @JsonIgnore
    public void setPlan(HarvesterPlan harvesterPlan) {

	harvesterPlan.setKey(HARVESTER_PLAN_KEY);

	getConfigurableComponents().put(HARVESTER_PLAN_KEY, harvesterPlan);
    }

    @JsonIgnore
    public HarvesterPlan getPlan() {

	return (HarvesterPlan) getConfigurableComponents().get(HARVESTER_PLAN_KEY);
    }

    @JsonIgnore
    public void harvest(Boolean isRecovering) throws GSException {

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

	    getSourceStorage().harvestingStarted(getAccessor().getSource(), strategy, isRecovering);

	    GSLoggerFactory.getLogger(this.getClass()).info("Harvest of source {} [{}] STARTED with recovery flag {}",
		    getAccessor().getSource().getLabel(), getAccessor().getSource().getUniqueIdentifier(), isRecovering);

	    ListRecordsRequest request = new ListRecordsRequest();

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

	    getSourceStorage().harvestingEnded(getAccessor().getSource(), strategy);

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

	    throw exception;
	}
    }

    @JsonIgnore
    public boolean isIncrementalHarvestingSupported() {

	try {

	    String startHarvestTimestamp = getSourceStorage().retrieveHarvestingProperties(getAccessor().getSource())
		    .getStartHarvestingTimestamp();

	    return getAccessor().supportsIncrementalHarvesting() && startHarvestTimestamp != null && !startHarvestTimestamp.isEmpty();

	} catch (Exception t) {

	    GSLoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
	}

	// in case of error, returning false is the most
	// conservative solution
	return false;
    }

    @JsonIgnore
    public SourceStorage getSourceStorage() {
	return sourceStorage;
    }

    @JsonIgnore
    public void setSourceStorage(SourceStorage sourceStorage) {

	this.sourceStorage = sourceStorage;

	// ------------------------------------------------------------------
	//
	// passes the source storage options
	// to the source storage
	//
	Collection<GSConfOption<?>> opts = getSupportedOptions().values();
	opts.forEach(option -> {

	    String key = option.getKey();

	    switch (key) {
	    case SourceStorage.MARK_DELETED_RECORDS_KEY:
	    case SourceStorage.TEST_ISO_COMPLIANCE_KEY:
	    case SourceStorage.RECOVER_TAGS_KEY:
	    case SourceStorage.FORCE_OVERWRITE_TAGS_KEY:

		this.sourceStorage.getSupportedOptions().put(key, option);

		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).warn("Unrecognized option key {} for Harvester in setSourceStorage method", key);
	    }
	});
    }

    public void addJob(HarvesterJob harvesterJob) {

	job.add(harvesterJob);
	getConfigurableComponents().put(harvesterJob.getKey(), harvesterJob);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (opt instanceof GSConfOptionBoolean) {

	    GSConfOptionBoolean option = (GSConfOptionBoolean) opt;

	    String key = option.getKey();

	    switch (key) {
	    case SourceStorage.MARK_DELETED_RECORDS_KEY:
	    case SourceStorage.TEST_ISO_COMPLIANCE_KEY:
	    case SourceStorage.RECOVER_TAGS_KEY:
	    case SourceStorage.FORCE_OVERWRITE_TAGS_KEY:

		// replaces the default option set in the init phase
		// with the current option
		getSupportedOptions().put(key, option);
		return;
	    default:
		GSLoggerFactory.getLogger(getClass()).warn("Unrecognized option key {} for Harvester", key);
	    }
	}
    }

    @Override
    public void onFlush() throws GSException {
	List<HarvesterJob> myJobs = getJobs();

	Harvester h = this;

	myJobs.stream().forEach(harvesterJob -> harvesterJob.setConfigurable(h));

    }

    @JsonIgnore
    void updateToken(String token, boolean removal) throws GSException {

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

    @JsonIgnore
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

	    GSLoggerFactory.getLogger(getClass()).trace("Plan application on resource {} STARTED", //
		    resource);

	    //
	    // set the removal token used for the recovery. in case of recovery, resources
	    // having this token (which corresponds to the token written in the properties file)
	    // will be removed before recovery starts
	    //
	    resource.getPropertyHandler().setRecoveryRemovalToken(recoveryRemovalToken);

	    HarvesterPlan plan = getPlan();

	    plan.setAccessor(getAccessor());
	    plan.setSourceStorage(getSourceStorage());
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

	    GSLoggerFactory.getLogger(getClass()).trace("Plan application on resource {} ENDED", //
		    resource);
	}
    }

    private HarvestingStrategy computeHarvestingStrategy(boolean doIncrementalHarvesting) {

	if (doIncrementalHarvesting) {
	    return HarvestingStrategy.SELECTIVE;
	}

	return HarvestingStrategy.FULL;
    }

    @JsonIgnore
    private void doHarvest(//
	    ListRecordsRequest recordRequest, //
	    Boolean isRecovering, //
	    boolean isIncremental, //
	    HarvestingProperties properties) throws GSException {

	String resumptionToken = null;

	if (recordRequest.getRecovering()) {
	    resumptionToken = findRecoveryToken();
	}

	recordRequest.setFirst(true);

	do {

	    GSLoggerFactory.getLogger(getClass()).trace("Harvesting plan application STARTED - resumption token: {}", resumptionToken);
	    
	    recordRequest.setResumptionToken(resumptionToken);

	    //
	    // updates the recovery token before the plan application
	    //
	    updateToken(resumptionToken, false);

	    //
	    // updates the recovery token before the plan application. the records with this
	    // token after a crash, will be removed before the recovery begins
	    //

	    String recoveryRemovalToken = UUID.randomUUID().toString();

	    updateToken(recoveryRemovalToken, true);

	    ListRecordsResponse<GSResource> response = getAccessor().listRecords(recordRequest);

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

    private String findRecoveryToken() throws GSException {

	GSLoggerFactory.getLogger(Harvester.class).trace("Finding recovery token STARTED");

	GSSource source = getAccessor().getSource();

	HarvestingProperties properties = getSourceStorage().retrieveHarvestingProperties(source);

	String rt = properties.getRecoveryResumptionToken();

	GSLoggerFactory.getLogger(Harvester.class).trace("Finding recovery token ENDED");
	GSLoggerFactory.getLogger(Harvester.class).trace("Revoery token: {}", rt);

	return rt;
    }

    @JsonIgnore
    private List<HarvesterJob> getJobs() {

	Iterator<IGSConfigurable> it = getConfigurableComponents().values().iterator();
	List<HarvesterJob> foundJobs = new ArrayList<>();

	while (it.hasNext()) {
	    IGSConfigurable next = it.next();
	    if (HarvesterJob.class.isAssignableFrom(next.getClass()))
		foundJobs.add((HarvesterJob) next);
	}

	return foundJobs;
    }
}
