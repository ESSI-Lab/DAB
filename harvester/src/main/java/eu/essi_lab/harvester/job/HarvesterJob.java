package eu.essi_lab.harvester.job;

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

import static eu.essi_lab.jobs.scheduler.quartz.QuartzJobBuilderMediator.CONFIGURABLE_KEY;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.harvester.Harvester;
import eu.essi_lab.harvester.component.DatabaseComponent;
import eu.essi_lab.harvester.component.HarvesterPlan;
import eu.essi_lab.harvester.component.IdentifierDecoratorComponent;
import eu.essi_lab.harvester.component.IndexedElementsWriterComponent;
import eu.essi_lab.harvester.component.ResourceValidatorComponent;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.identifierdecorator.SourcePriorityDocument;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.GSJobValidationFailReason;
import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.AbstractGSConfigurableJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.MetadataDataDabaseStoragiURI;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class HarvesterJob extends AbstractGSConfigurableJob {

    /**
     *
     */
    private static final long serialVersionUID = -5198684985077108879L;

    @JsonIgnore
    private static final String IDENTIFIER_DECORATOR_KEY = "IDENTIFIER_DECORATOR_KEY";

    @JsonIgnore
    private static final String INDEXED_ELEMENTS_WRITER_KEY = "INDEXED_ELEMENTS_WRITER_KEY";

    @JsonIgnore
    private static final String DATABASE_COMPONENT_KEY = "DATABASE_COMPONENT_KEY";

    @JsonIgnore
    private static final String RESOURCE_VALIDATOR_COMPONENT_KEY = "RESOURCE_VALIDATOR_COMPONENT_KEY";

    @JsonIgnore
    private transient  Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    public void run(Map<String, Object> jobDataMap, Boolean isRecovering, Optional<GSJobStatus> jobStatus) throws GSException {

	logger.info("Starting HarvesTer Job");

	IGSConfigurationReader reader = getConfigurationReader();

	IGSConfigurable configurable = reader.readComponent(new ConfigurableKey((String) jobDataMap.get(CONFIGURABLE_KEY)));

	Harvester harvester = (Harvester) configurable;

	if (logger.isTraceEnabled())
	    logger.trace("Configurable \n {}", harvester.serialize());

	MetadataDataDabaseStoragiURI storageURI = reader.readInstantiableType(MetadataDataDabaseStoragiURI.class, new Deserializer()).get(
		0);

	logger.debug("Using DB @ {}", storageURI.getUri());

	DatabaseConsumerFactory consumerFactory = new DatabaseConsumerFactory();

	HarvesterPlan plan = harvester.getPlan();

	Map<String, IGSConfigurable> components = plan.getConfigurableComponents();

	// makes a copy of the plan components
	HashSet<Entry<String, IGSConfigurable>> hashSet = new HashSet<>();
	hashSet.addAll(components.entrySet());

	// clears the component
	components.clear();

	DatabaseReader dataBaseReader = consumerFactory.createDataBaseReader(storageURI);
	DatabaseWriter dataBaseWriter = consumerFactory.createDataBaseWriter(storageURI);

	// ----------------------------------------------------
	// inserts the mandatory components in the PROPER order
	// ----------------------------------------------------

	// --------------------------------
	//
	// 1) IndentifierDecoratorComponent
	//
	IdentifierDecorator identifierDecorator = new IdentifierDecorator(new SourcePriorityDocument(), dataBaseReader, dataBaseWriter);

	components.put(IDENTIFIER_DECORATOR_KEY, new IdentifierDecoratorComponent(identifierDecorator));

	// -----------------------------
	//
	// 2) ResourceValidatorComponent
	//
	ResourceValidatorComponent resValidatorComponent = new ResourceValidatorComponent();

	components.put(RESOURCE_VALIDATOR_COMPONENT_KEY, resValidatorComponent);

	// -----------------------------------------------------------
	//
	// inserts the minor components just before the indexed writer (because for example augmenters
	// can modify the resources metadata )
	// components already present are skipped (because it's a map)
	//
	for (Entry<String, IGSConfigurable> entry : hashSet) {

	    components.put(entry.getKey(), entry.getValue());
	}

	// ----------------------------------
	//
	// 3) IndexedElementsWriterComponent
	//
	IndexedElementsWriterComponent indexerWriterComponent = new IndexedElementsWriterComponent();

	components.put(INDEXED_ELEMENTS_WRITER_KEY, indexerWriterComponent);

	// ----------------------------------------------------
	//
	// 4) DatabaseComponent (stores the resources, at last)
	//
	DatabaseComponent databaseComponent = new DatabaseComponent(dataBaseWriter);

	components.put(DATABASE_COMPONENT_KEY, databaseComponent);

	// ---------------------------------------
	//
	// Adds the SourceStorage to the harvester
	//
	SourceStorage sourceStorage = consumerFactory.createSourceStorage(storageURI);
	harvester.setSourceStorage(sourceStorage);

	// ------------------------------------------------------------
	//
	// Begins harvesting
	//
	harvester.harvest(isRecovering);
    }

    ConfigurationSync getConfigurationSync() {
	return ConfigurationSync.getInstance();
    }

    @Override
    public GSJobValidationResult isValid(Map<String, Object> jobDataMap) {

	logger.debug("Validating HarvesterJob {}", getKey());

	GSJobValidationResult result = new GSJobValidationResult();
	try {

	    IGSConfigurationReader reader = getConfigurationReader();

	    Object obj = jobDataMap.get(CONFIGURABLE_KEY);

	    if (obj == null) {
		logger.warn(
			"Found a HarvesterJob with null value for job data map key {}. This is probably an old version scheduling, thus "
				+ "I'm returning false in validation", CONFIGURABLE_KEY);

		result.setValid(false);
		result.setReason(GSJobValidationFailReason.NO_CONFIGURABLE_KEY_FOUND);

		return result;
	    }

	    IGSConfigurable configurable = reader.readComponent(new ConfigurableKey((String) obj));

	    Harvester harvester = (Harvester) configurable;

	    GSSource accSource = harvester.getAccessor().getSource();

	    String sourceIdentifier = accSource.getUniqueIdentifier();

	    String sourceLabel = accSource.getLabel();

	    logger.trace("HarvesterJob is configured with source {} [SourceId: {}]", sourceLabel, sourceIdentifier);

	    GSSource sourceInConf = reader.readInstantiableType(GSSource.class, sourceIdentifier, new Deserializer());

	    if (sourceInConf == null) {
		logger.info("Source {} [SourceId: {}] not found in current configuration, returning false", sourceLabel, sourceIdentifier);

		result.setValid(false);
		result.setReason(GSJobValidationFailReason.TARGET_SOURCE_NOT_FOUND);

		return result;
	    }

	    logger.trace("Found a matching source in configuration, checking identifiers");

	    if (!sourceIdentifier.equals(sourceInConf.getUniqueIdentifier())) {

		logger.info("Detected source in configuration {} [SourceId: {}] has a different id than the configured source {} "
				+ "[SourceId: {}], returning false", sourceInConf.getLabel(), sourceInConf.getUniqueIdentifier(), sourceLabel,
			sourceIdentifier);

		result.setValid(false);
		result.setReason(GSJobValidationFailReason.TARGET_SOURCE_NOT_MATCHING);

		return result;
	    }

	} catch (GSException ex) {

	    logger.warn("Exception during validation of HarvesterJob (will return valid)");

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(ex)));

	}

	logger.info("HarvesterJob {} is valid", getKey());

	result.setValid(true);

	return result;
    }

    IGSConfigurationReader getConfigurationReader() throws GSException {
	return new GSConfigurationManager(getConfigurationSync());
    }
}
