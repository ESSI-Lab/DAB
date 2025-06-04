package eu.essi_lab.gssrv.conf.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Mattia Santoro
 */
public class ParentIdentifierDecoratorTask extends AbstractEmbeddedTask  {

    @Override
    public String getName() {
	return "Parent Identifier Decorator Task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Parent Identifier Decorator Task START");

	Optional<String> taskOptions = readTaskOptions(context);

	if (!taskOptions.isPresent()) {

	    log(status, "Custom task options missing, unable to perform task");

	    return;
	}

	List<String> targetSourceIds = Arrays.asList(taskOptions.get().split(","));

	List<GSSource> collectedSources = readConfiguredSources().stream()
		.filter(source -> targetSourceIds.contains(source.getUniqueIdentifier())).collect(Collectors.toList());

	if (targetSourceIds.size() - collectedSources.size() != 0) {
	    GSLoggerFactory.getLogger(getClass()).warn(
		    "Size of configured target sources ids ({}) is different from the size of " + "collected sources ({})",
		    targetSourceIds.size(), collectedSources.size());
	    log(status, "Warning: size of configured target sources ids is different from the size of collected sources", false);
	}

	for (GSSource source : collectedSources) {
	    try {
		decorateParentIdentifiers(source, status);
	    } catch (GSException gsException) {

		log(status, "Exception running parent identifier decorator on source " + source.getLabel());
	    }
	}

    }

    private void decorateParentIdentifiers(GSSource source, SchedulerJobStatus status) throws GSException {

	log(status,
		String.format("Starting parent identifier decorator for source %s (%s)", source.getLabel(), source.getUniqueIdentifier()));

	DatabaseReader reader = getDataBaseReader();
	DatabaseWriter writer = getDataBaseWriter();
	DatabaseFinder finder = getDataBaseFinder();

	Map<String, String> alreadyCheckedParentIds = new HashMap<>();

	int count = countResourcesWithParentId(finder, source);

	log(status, String.format("Found %d resources with parent identifier in source %s (%s)", count, source.getLabel(),
		source.getUniqueIdentifier()));

	int start = 1;

	while (start <= count) {

	    log(status, String.format("Decorating resources from %d to %d in source %s (%s)", start, count, source.getLabel(),
		    source.getUniqueIdentifier()));

	    List<GSResource> resources = getResourcesWithParentId(finder, source, start, 10);

	    for (GSResource resource : resources) {

		decorateResource(resource, source, alreadyCheckedParentIds, status, reader, writer);

	    }

	    start = start + 10;
	}

	log(status, "Completed parent identifier decorator for source " + source.getLabel());

    }

    int countResourcesWithParentId(DatabaseFinder finder, GSSource source) throws GSException {

	DiscoveryMessage message = createDiscoveryMessage(source);

	return finder.count(message).getCount();
    }

    private DiscoveryMessage createDiscoveryMessage(GSSource source) {
	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(UUID.randomUUID().toString());

	ResourcePropertyBond sourceIdBond = BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.SOURCE_ID,
		source.getUniqueIdentifier());
	SimpleValueBond parentIdExistsBond = BondFactory.createExistsSimpleValueBond(MetadataElement.PARENT_IDENTIFIER);

	LogicalBond andBond = BondFactory.createAndBond(sourceIdBond, parentIdExistsBond);

	message.setPermittedBond(andBond);
	message.setNormalizedBond(andBond);
	message.setUserBond(andBond);

	return message;
    }

    List<GSResource> getResourcesWithParentId(DatabaseFinder finder, GSSource source, Integer start, Integer count) throws GSException {

	DiscoveryMessage message = createDiscoveryMessage(source);

	message.setPage(new Page(start, count));

	ResultSet<GSResource> resultSet = finder.discover(message);
	return resultSet.getResultsList();

    }

    List<GSSource> readConfiguredSources() {
	return ConfigurationWrapper.getAllSources();
    }

    DatabaseReader getDataBaseReader() throws GSException {

	StorageInfo dataBaseURI = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(getClass()).debug("Configured Database URI: {}", dataBaseURI.getUri());

	return DatabaseProviderFactory.getReader(dataBaseURI);

    }

    DatabaseFinder getDataBaseFinder() throws GSException {

	StorageInfo dataBaseURI = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(getClass()).debug("Configured Database URI: {}", dataBaseURI.getUri());

	return DatabaseProviderFactory.getFinder(dataBaseURI);

    }

    DatabaseWriter getDataBaseWriter() throws GSException {

	StorageInfo dataBaseURI = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(getClass()).debug("Configured Database URI: {}", dataBaseURI.getUri());

	return DatabaseProviderFactory.getWriter(dataBaseURI);

    }

    private void decorateResource(GSResource resource, GSSource source, Map<String, String> alreadyCheckedParentIds,
	    SchedulerJobStatus status, DatabaseReader reader, DatabaseWriter writer) throws GSException {

	String parentid = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier();

	log(status, String.format("Resource %s has parent id %s", resource.getPublicId(), parentid));

	if (alreadyCheckedParentIds.get(parentid) != null) {
	    String workingparentid = alreadyCheckedParentIds.get(parentid);

	    log(status, String.format("Parent id %s already checked, correct parent id is %s", parentid, workingparentid));

	    if (!parentid.equalsIgnoreCase(workingparentid)) {
		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(workingparentid);

		writeResource(resource, writer, status);
	    }

	    return;
	}

	log(status, String.format("Reading by public id %s", parentid));

	List<GSResource> collections = reader.getResources(Database.IdentifierType.PUBLIC, parentid);
	log(status, String.format("Found %d resources with public id %s", collections.size(), parentid));

	if (!collections.isEmpty()) {

	    alreadyCheckedParentIds.put(parentid, parentid);

	} else {

	    IdentifierDecorator identifierDec = new IdentifierDecorator();
	    String totestParentid;

	    if (identifierDec.isDecoratedId(parentid, source)) {

		totestParentid = identifierDec.retrieveOriginalId(parentid, source);

	    } else {
		totestParentid = identifierDec.generatePersistentIdentifier(parentid, source.getUniqueIdentifier());
	    }

	    log(status, String.format("Reading by public id %s", totestParentid));

	    collections = reader.getResources(Database.IdentifierType.PUBLIC, totestParentid);
	    log(status, String.format("Found %d resources with public id %s", collections.size(), totestParentid));

	    if (!collections.isEmpty()) {

		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(totestParentid);
		writeResource(resource, writer, status);
		alreadyCheckedParentIds.put(parentid, totestParentid);
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Can not find parent resources of {} with parent id {} from {} ({})",
			resource.getPublicId(), resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier(),
			source.getLabel(), source.getUniqueIdentifier());

		log(status,
			String.format("WARNING: Can not find parent resources of %s with parent id %s from %s (%s)", resource.getPublicId(),
				resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier(), source.getLabel(),
				source.getUniqueIdentifier()),
			false);

	    }
	}
    }

    void writeResource(GSResource resource, DatabaseWriter databaseWriter, SchedulerJobStatus status) throws GSException {
	log(status, String.format("Writing resource %s with new parent id %s", resource.getPublicId(),
		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier()));
	databaseWriter.remove(resource);
	databaseWriter.store(resource);

    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }
}
