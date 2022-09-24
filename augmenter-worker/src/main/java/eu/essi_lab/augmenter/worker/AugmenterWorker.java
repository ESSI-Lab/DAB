package eu.essi_lab.augmenter.worker;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
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
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class AugmenterWorker extends SchedulerWorker<AugmenterWorkerSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "AugmenterWorker";

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

	AugmentationReportsHandler.sendAugmentationEmail(true, context.isRecovering(), getSetting());

	GSLoggerFactory.getLogger(this.getClass()).info("Augmenter job STARTED");

	int maxRecords = getSetting().getMaxRecords();
	boolean orderingSet = getSetting().isLessRecentOrderingSet();
	int timeBack = getSetting().getTimeBack();

	GSLoggerFactory.getLogger(getClass()).debug("Max records: {} ", maxRecords);
	GSLoggerFactory.getLogger(getClass()).debug("Ordering set: {} ", orderingSet);
	GSLoggerFactory.getLogger(getClass()).debug("Time back: {} ", timeBack);

	status.addInfoMessage("Max records: " + maxRecords);
	status.addInfoMessage("Ordering set: " + orderingSet);
	status.addInfoMessage("Time back: " + timeBack);

	if (maxRecords > 0) {

	    GSLoggerFactory.getLogger(getClass()).debug("Number of records to augment limited to: {}", maxRecords);
	    status.addInfoMessage("Number of records to augment limited to: " + maxRecords);
	}

	StorageUri storageURI = ConfigurationWrapper.getDatabaseURI();

	DatabaseReader dataBaseReader = DatabaseConsumerFactory.createDataBaseReader(storageURI);
	DatabaseWriter dataBaseWriter = DatabaseConsumerFactory.createDataBaseWriter(storageURI);

	boolean completed = false;

	int start = 1;
	int step = 50;

	DiscoveryMessage message = new DiscoveryMessage();

	//
	// some augmenters can mark as deleted the augmented records
	// and if the deleted are excluded, the process can end before the processing
	// of all the resources
	//
	message.setIncludeDeleted(true);

	//
	//
	//

	List<GSSource> selectedSources = getSetting().getSelectedSources();

	message.setSources(getSetting().getSelectedSources());

	GSLoggerFactory.getLogger(getClass()).debug("Number of sources to augment: {}", selectedSources.size());
	status.addInfoMessage("Number of sources to augment: " + selectedSources.size());

	//
	//
	//

	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());

	if (orderingSet) {

	    message.setOrderingDirection(OrderingDirection.ASCENDING);
	    message.setOrderingProperty(ResourceProperty.RESOURCE_TIME_STAMP);

	    GSLoggerFactory.getLogger(getClass()).info("Less recent ordering set");
	    status.addInfoMessage("Less recent ordering set");
	}

	LogicalBond andBond = BondFactory.createAndBond();

	//
	// add the sources bond
	//

	andBond.getOperands().add(getSetting().getSourcesBond());

	if (timeBack > 0) {

	    long timeMillis = System.currentTimeMillis();
	    long startTime = timeMillis - timeBack;

	    String startTimeS = ISO8601DateTimeUtils.getISO8601DateTime(new Date(startTime));

	    GSLoggerFactory.getLogger(getClass()).info("Minimum resource time stamp set to: " + startTimeS);
	    status.addInfoMessage("Minimum resource time stamp set to: " + startTimeS);

	    andBond.getOperands().add(//
		    BondFactory.createResourceTimeStampBond(BondOperator.GREATER_OR_EQUAL, startTimeS));
	}

	//
	// this is to avoid to retrieve records harvested after the beginning of this process
	//
	andBond.getOperands()
		.add(BondFactory.createResourceTimeStampBond(BondOperator.LESS_OR_EQUAL, ISO8601DateTimeUtils.getISO8601DateTime()));

	//
	// set the and bond
	//
	message.setPermittedBond(andBond);

	int count = dataBaseReader.count(message).getCount();

	if (count == 0) {

	    GSLoggerFactory.getLogger(getClass()).warn("No resource to augment");
	    status.addWarningMessage("No resource to augment");

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Found " + count + " resources to augment");
	status.addInfoMessage("Found " + count + " resources to augment");

	if (count > maxRecords && maxRecords > 0) {

	    count = maxRecords;
	}

	IterationLogger logger = new IterationLogger(this, count, step);
	logger.setMessage("Augmentation status: ");

	// -------------------------------------------------
	//
	// sorts the augmenters according to their priority
	// the lower value, the higher the priority
	//

	List<Augmenter> augmenters = getSetting().//
		getSelectedAugmenterSettings().//
		stream().//
		sorted(Comparator.comparing(AugmenterSetting::getPriority)).//
		map(s -> (Augmenter) s.createConfigurableOrNull()). //
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	while (!completed) {

	    Page page = new Page(start, step);
	    message.setPage(page);

	    ResultSet<GSResource> response = dataBaseReader.discover(message);
	    List<GSResource> resources = response.getResultsList();

	    int size = resources.size();
	    if (size < step || (maxRecords > 0 && start + step > maxRecords)) {
		completed = true;
	    }

	    for (GSResource resource : resources) {

		Optional<GSResource> optional = Optional.empty();
		List<GSKnowledgeResourceDescription> concepts = new ArrayList<>();

		for (Augmenter augmenter : augmenters) {

		    try {

			optional = augmenter.augment(resource);

			if (optional.isPresent()) {
			    //
			    // if the resource is augmented, uses the augmented
			    // resource to generates the concepts
			    //
			    concepts = augmenter.generate(optional.get());

			} else {

			    concepts = augmenter.generate(resource);
			}

		    } catch (Exception ex) {

			GSLoggerFactory.getLogger(getClass()).warn("Augmentation error occurred: " + ex.getMessage());
			status.addErrorMessage("Augmentation error occurred: " + ex.getMessage());

			GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
		    }
		}

		//
		// if the resource is augmented, it is updated
		//
		if (optional.isPresent()) {

		    // GSLoggerFactory.getLogger(getClass()).trace("Updating augmented resource STARTED");

		    dataBaseWriter.update(optional.get());

		    // GSLoggerFactory.getLogger(getClass()).trace("Updating augmented resource ENDED");
		}

		if (!concepts.isEmpty()) {
		    for (GSKnowledgeResourceDescription concept : concepts) {

			GSLoggerFactory.getLogger(getClass()).trace("Storing concepts STARTED");

			dataBaseWriter.store(concept);

			GSLoggerFactory.getLogger(getClass()).trace("Storing concepts ENDED");
		    }
		}
	    }

	    start += step;

	    logger.iterationDone();
	}

	GSLoggerFactory.getLogger(getClass()).info("Augmentation ENDED");

	AugmentationReportsHandler.sendAugmentationEmail(false, context.isRecovering(), getSetting());
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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
