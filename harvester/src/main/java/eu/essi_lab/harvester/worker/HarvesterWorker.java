package eu.essi_lab.harvester.worker;

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
import java.util.List;

import org.quartz.JobExecutionContext;

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
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
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class HarvesterWorker extends SchedulerWorker<HarvestingSetting> {

    static final String HARVESTER_WORKER_TYPE = "harvesterWorker";

    /**
     * @param recovering
     * @throws Exception
     */
    public void startHarvesting(boolean recovering) throws Exception {

	startHarvesting(recovering, null);
    }

    /**
     * @param recovering
     * @param status
     * @throws Exception
     */
    public void startHarvesting(boolean recovering, SchedulerJobStatus status) throws Exception {

	GSLoggerFactory.getLogger(this.getClass()).info("Starting Harvester Worker");

	Harvester harvester = new Harvester();

	StorageUri databaseURI = ConfigurationWrapper.getDatabaseURI();

	GSLoggerFactory.getLogger(this.getClass()).debug("Configured Database URI: {}", databaseURI.getUri());

	DatabaseReader dataBaseReader = DatabaseConsumerFactory.createDataBaseReader(databaseURI);
	DatabaseWriter dataBaseWriter = DatabaseConsumerFactory.createDataBaseWriter(databaseURI);
	SourceStorage storage = DatabaseConsumerFactory.createSourceStorage(databaseURI);

	// ----------------------------------------------------
	// inserts the mandatory components in the PROPER order
	// ----------------------------------------------------

	// --------------------------------
	//
	// 1) IndentifierDecoratorComponent
	//
	IdentifierDecorator identifierDecorator = new IdentifierDecorator(ConfigurationWrapper.getSourcePrioritySetting(), dataBaseReader);

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
	List<Augmenter> augmenters = new HarvestingSettingHelper(getSetting()).getSelectedAugmenters();

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
	DatabaseComponent databaseComponent = new DatabaseComponent(dataBaseWriter);

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

	// ------------------------------------------------------------
	//
	// Begins harvesting
	//

	harvester.getPlan().setAccessor(accessor);
	harvester.getPlan().setSourceStorage(storage);

	harvester.harvest(recovering, status);
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	startHarvesting(context.isRecovering(), status);
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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
