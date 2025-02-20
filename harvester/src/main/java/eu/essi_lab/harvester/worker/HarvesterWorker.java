package eu.essi_lab.harvester.worker;

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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.task.CustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.harvester.Harvester;
import eu.essi_lab.harvester.component.AugmenterComponent;
import eu.essi_lab.harvester.component.DatabaseComponent;
import eu.essi_lab.harvester.component.IdentifierDecoratorComponent;
import eu.essi_lab.harvester.component.IndexedElementsWriterComponent;
import eu.essi_lab.harvester.component.ResourceValidatorComponent;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class HarvesterWorker extends SchedulerWorker<HarvestingSetting> {

    static final String HARVESTER_WORKER_TYPE = "harvesterWorker";

    /**
     * @author Fabrizio
     */
    public static final class RecoveringContext implements JobExecutionContext {

	private boolean isRecovering;

	/**
	 * @param isRecovering
	 * @return
	 */
	public static RecoveringContext create(boolean isRecovering) {

	    return new RecoveringContext(isRecovering);
	}

	/**
	 * @param context
	 * @return
	 */
	public static boolean isRecoveringContext(JobExecutionContext context) {

	    return context.getClass().equals(RecoveringContext.class);
	}

	/**
	 * @param isRecovering
	 */
	private RecoveringContext(boolean isRecovering) {

	    this.isRecovering = isRecovering;
	}

	@Override
	public org.quartz.Scheduler getScheduler() {

	    return null;
	}

	@Override
	public Trigger getTrigger() {

	    return null;
	}

	@Override
	public Calendar getCalendar() {

	    return null;
	}

	@Override
	public boolean isRecovering() {

	    return isRecovering;
	}

	@Override
	public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {

	    return null;
	}

	@Override
	public int getRefireCount() {

	    return 0;
	}

	@Override
	public JobDataMap getMergedJobDataMap() {

	    return null;
	}

	@Override
	public JobDetail getJobDetail() {

	    return null;
	}

	@Override
	public Job getJobInstance() {

	    return null;
	}

	@Override
	public Date getFireTime() {

	    return null;
	}

	@Override
	public Date getScheduledFireTime() {

	    return null;
	}

	@Override
	public Date getPreviousFireTime() {

	    return null;
	}

	@Override
	public Date getNextFireTime() {

	    return null;
	}

	@Override
	public String getFireInstanceId() {

	    return null;
	}

	@Override
	public Object getResult() {

	    return null;
	}

	@Override
	public void setResult(Object result) {
	}

	@Override
	public long getJobRunTime() {

	    return 0;
	}

	@Override
	public void put(Object key, Object value) {
	}

	@Override
	public Object get(Object key) {
	    return null;
	}
    }

    /**
     * @param recovering
     * @throws Exception
     */
    public void startHarvesting(boolean recovering) throws Exception {

	startHarvesting(RecoveringContext.create(recovering), null);
    }

    /**
     * @param context
     * @param status
     * @throws Exception
     */
    public void startHarvesting(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	GSLoggerFactory.getLogger(this.getClass()).info("Starting Harvester Worker");

	Harvester harvester = new Harvester();

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	GSLoggerFactory.getLogger(this.getClass()).debug("Configured Database URI: {}", databaseURI.getUri());

	DatabaseReader dataBaseReader = DatabaseProviderFactory.getReader(databaseURI);
	DatabaseWriter dataBaseWriter = DatabaseProviderFactory.getWriter(databaseURI);
	SourceStorage storage = DatabaseProviderFactory.getSourceStorage(databaseURI);

	// ----------------------------------------------------
	// inserts the mandatory components in the PROPER order
	// ----------------------------------------------------

	HarvestingSetting harvestingSetting = getSetting();

	HarvestedConnectorSetting harvConnSetting = harvestingSetting.getSelectedAccessorSetting().getHarvestedConnectorSetting();

	// --------------------------------
	//
	// 1) IndentifierDecoratorComponent
	//

	Boolean preserveIds = harvConnSetting.preserveIdentifiers();

	IdentifierDecorator identifierDecorator = new IdentifierDecorator(//
		ConfigurationWrapper.getSourcePrioritySetting(), //
		preserveIds, //
		dataBaseReader);

	harvester.getPlan().getComponents().add(new IdentifierDecoratorComponent(identifierDecorator));

	// -----------------------------
	//
	// 2) ResourceValidatorComponent
	//
	ResourceValidatorComponent resValidatorComponent = new ResourceValidatorComponent();

	harvester.getPlan().getComponents().add(resValidatorComponent);

	// --------------------------------------------------------------------------
	//
	// 2.1) Augmenters (if any) - They are ordered by priority ??? (they should!)
	//
	//

	@SuppressWarnings("rawtypes")
	List<Augmenter> augmenters = new HarvestingSettingHelper(harvestingSetting).getSelectedAugmenters();

	augmenters.forEach(a -> harvester.getPlan().getComponents().add(new AugmenterComponent(a)));

	// ----------------------------------
	//
	// 3) IndexedElementsWriterComponent
	//
	IndexedElementsWriterComponent indexerWriterComponent = new IndexedElementsWriterComponent();

	harvester.getPlan().getComponents().add(indexerWriterComponent);

	// ----------------------------------------------------
	//
	// 4) DatabaseComponent (stores the resources, at last)
	//
	DatabaseComponent databaseComponent = new DatabaseComponent(dataBaseWriter, dataBaseReader);

	harvester.getPlan().getComponents().add(databaseComponent);

	// ---------------------------------------
	//
	// Adds the SourceStorage to the harvester
	//
	harvester.setSourceStorage(storage);

	//
	// Adds the Accessor to the harvester
	//
	@SuppressWarnings("rawtypes")
	IHarvestedAccessor accessor = new HarvestingSettingHelper(getSetting()).getSelectedAccessor();

	GSLoggerFactory.getLogger(this.getClass()).debug("Configured accessor: {}", accessor.getClass());

	harvester.setAccessor(accessor);

	//
	// Set the custom task, if enabled
	//

	Optional<CustomTaskSetting> customTaskSetting = getSetting().getCustomTaskSetting();

	if (customTaskSetting.isPresent() && customTaskSetting.get().isEnabled()) {

	    String taskClassName = customTaskSetting.get().getTaskClassName();

	    @SuppressWarnings("unchecked")
	    Class<CustomTask> taskClass = (Class<CustomTask>) Class.forName(taskClassName);

	    CustomTask customTask = taskClass.newInstance();

	    harvester.setCustomTask(customTask);

	} else if (!customTaskSetting.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Custom task setting missing for source: [{}]", //
		    accessor.getSource().getLabel());
	}

	// ------------------------------------------------------------
	//
	// Begins harvesting
	//

	harvester.getPlan().setAccessor(accessor);
	harvester.getPlan().setSourceStorage(storage);

	harvester.harvest(context, status);
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	startHarvesting(context, status);
    }

    @Override
    public String getType() {

	return HARVESTER_WORKER_TYPE;
    }

    @Override
    protected HarvestingSetting initSetting() {

	return new HarvestingSettingImpl();
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
