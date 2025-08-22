package eu.essi_lab.gssrv.conf.task;

import java.math.BigDecimal;

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

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.opensearch.index.Shape;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.CollectionType;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author boldrini
 */
public class StationsCreatorTask extends AbstractEmbeddedTask {

    /**
     * 
     */
    private static final int PAGE_SIZE = 100;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Stations creator task STARTED");

	GSSource gsSource = getSource().get();

	String sourceId = gsSource.getUniqueIdentifier();

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	SourceStorageWorker worker = database.getWorker(sourceId);

	DatabaseFolder target = worker.getData1Folder() == null ? worker.getData2Folder() : worker.getData1Folder();

	int targetSize = target.size();

	GSLoggerFactory.getLogger(getClass()).debug("Target data folder: {}", target.getName());
	GSLoggerFactory.getLogger(getClass()).debug("Target data folder size: {}", targetSize);

	//
	//
	//

	DiscoveryMessage message = new DiscoveryMessage();

	ResourcePropertyBond sourceIdBond = BondFactory.createSourceIdentifierBond(sourceId);

	message.setPermittedBond(sourceIdBond);
	message.setNormalizedBond(sourceIdBond);
	message.setUserBond(sourceIdBond);

	message.setPage(new Page(PAGE_SIZE));

	ResourceSelector selector = new ResourceSelector();
	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	selector.addIndex(MetadataElement.PLATFORM_TITLE);
	selector.addIndex(MetadataElement.BOUNDING_BOX);
	selector.addIndex(MetadataElement.ORGANISATION_NAME);
	selector.addIndex(MetadataElement.COUNTRY);
	selector.addIndex(MetadataElement.ABSTRACT);
	selector.addIndex(MetadataElement.TITLE);
	selector.addIndex(ResourceProperty.PRIVATE_ID);

	message.setResourceSelector(selector);

	message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	message.setExcludeResourceBinary(true);

	message.setSortedFields(
		SortedFields.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, SortOrder.ASCENDING));

	//
	//
	//

	Optional<SearchAfter> searchAfter = Optional.empty();

	String uniquePlatformId = null;

	int targetIterations = targetSize / PAGE_SIZE;
	int iterations = 1;

	do {

	    GSLoggerFactory.getLogger(getClass()).debug("Iteration {}/{} STARTED", iterations, targetIterations);

	    ResultSet<GSResource> resultSet = finder.discover(message);

	    for (GSResource res : resultSet.getResultsList()) {

		List<String> ids = res.getIndexesMetadata().read(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

		if (!ids.isEmpty()) {

		    String _uniquePlatformId = ids.get(0);

		    if (!_uniquePlatformId.equals(uniquePlatformId)) {

			DatasetCollection collection = new DatasetCollection();

			collection.setSource(gsSource);

			collection.getPropertyHandler().setCollectionType(CollectionType.STATION);

			collection.setPrivateId(res.getPrivateId() + "_station");
			collection.setPublicId(collection.getPrivateId());
			collection.setOriginalId(collection.getPrivateId());

			//
			// country
			//

			res.getExtensionHandler().getCountry().ifPresent(country -> collection.getExtensionHandler().setCountry(country));

			//
			// shape
			//

			Optional<String> shape = res.getExtensionHandler().getShape();

			if (shape.isPresent()) {

			    Optional<Geometry> optGeometry = Shape.of(shape.get());

			    if (optGeometry.isPresent()) {

				Geometry geometry = optGeometry.get();

				if (geometry instanceof Point) {

				    Point point = (Point) geometry;
				    double x = point.getX();
				    double y = point.getY();

				    collection.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
					    new BigDecimal(y), //
					    new BigDecimal(x), //
					    new BigDecimal(y), //
					    new BigDecimal(x));

				} else if (geometry instanceof Polygon) {

				    String bbox = BBOXUtils.toBBOX(shape.get(), false);
				    // south, west, north, east
				    String[] split = bbox.split(" ");

				    collection.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
					    new BigDecimal(split[2]), // north
					    new BigDecimal(split[1]), // west
					    new BigDecimal(split[0]), // south
					    new BigDecimal(split[3]));// east
				}
			    }
			}

			//
			// platform id
			//

			collection.getExtensionHandler().setUniquePlatformIdentifier(_uniquePlatformId);

			//
			// platform title
			//

			List<String> platformTitle = res.getIndexesMetadata().read(MetadataElement.PLATFORM_TITLE);

			if (!platformTitle.isEmpty()) {

			    Citation citation = new Citation();
			    citation.setTitle(platformTitle.get(0));

			    MIPlatform miPlatform = new MIPlatform();
			    miPlatform.setCitation(citation);

			    collection.getHarmonizedMetadata().//
				    getCoreMetadata().//
				    getMIMetadata().//
				    addMIPlatform(miPlatform);

			    collection.getHarmonizedMetadata().getCoreMetadata().setAbstract(platformTitle.get(0));
			    collection.getHarmonizedMetadata().getCoreMetadata().setTitle(platformTitle.get(0));
			}

			//
			// organisation name
			//

			List<String> orgName = res.getIndexesMetadata().read(MetadataElement.ORGANISATION_NAME);

			if (!orgName.isEmpty()) {

			    DataIdentification dataIdentification = collection.getHarmonizedMetadata().//
				    getCoreMetadata().//
				    getMIMetadata().//
				    getDataIdentification();

			    ResponsibleParty responsibleParty = new ResponsibleParty();
			    responsibleParty.setOrganisationName(orgName.get(0));

			    dataIdentification.addPointOfContact(responsibleParty);
			}

			//
			//
			//

			IndexedElementsWriter.write(collection);

			GSLoggerFactory.getLogger(getClass()).debug("Storing collection {} STARTED", _uniquePlatformId);

			target.store(collection.getPrivateId(), FolderEntry.of(collection.asDocument(true)), EntryType.GS_RESOURCE);

			GSLoggerFactory.getLogger(getClass()).debug("Storing collection {} ENDED", _uniquePlatformId);

			//
			//
			//

			uniquePlatformId = _uniquePlatformId;
		    }
		}
	    }

	    searchAfter = resultSet.getSearchAfter();

	    searchAfter.ifPresent(sa -> message.setSearchAfter(sa));

	    GSLoggerFactory.getLogger(getClass()).debug("Iteration {}/{} ENDED", iterations, targetIterations);

	    iterations++;

	} while (searchAfter.isPresent());

	log(status, "Stations creator task ENDED");
    }

    @Override
    public String getName() {

	return "Stations creator task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }
}
