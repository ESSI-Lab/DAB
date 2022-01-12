package eu.essi_lab.identifierdecorator;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseReader.IdentifierType;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceType;

public class IdentifierDecorator extends AbstractGSconfigurableComposed {

    private static final long serialVersionUID = 5527405836433966422L;
    public static final String PRIORITY_DOC = "PRIORITY_DOC";
    public static final String DATABASE_READER = "DATABASE_READER";
    public static final String DATABASE_WRITER = "DATABASE_WRITER";

    public IdentifierDecorator() {
    }

    public IdentifierDecorator(SourcePriorityDocument sourcePriorityDoc, DatabaseReader dbReader, DatabaseWriter dbWriter) {
	getConfigurableComponents().put(PRIORITY_DOC, sourcePriorityDoc);
	getConfigurableComponents().put(DATABASE_READER, dbReader);
	getConfigurableComponents().put(DATABASE_WRITER, dbWriter);
    }

    public Optional<SourcePriorityDocument> getSourcePriorityDocument() {

	return Optional.ofNullable((SourcePriorityDocument) getConfigurableComponents().get(PRIORITY_DOC));
    }

    public void setSourcePriorityDocument(SourcePriorityDocument sourcePriorityDocument) {
	getConfigurableComponents().put(PRIORITY_DOC, sourcePriorityDocument);
    }

    @JsonIgnore
    public DatabaseReader getDbReader() {
	return (DatabaseReader) getConfigurableComponents().get(DATABASE_READER);
    }

    public void setDbReader(DatabaseReader dbReader) {
	getConfigurableComponents().put(DATABASE_READER, dbReader);
    }

    @JsonIgnore
    public DatabaseWriter getDbWriter() {
	return (DatabaseWriter) getConfigurableComponents().get(DATABASE_WRITER);
    }

    public void setDbWriter(DatabaseWriter dbWriter) {
	getConfigurableComponents().put(DATABASE_WRITER, dbWriter);
    }

    public void decorateDistributedIdentifier(GSResource resource) {
	resource.setPrivateId(generateUniqueIdentifier());
	resource.setPublicId(resource.getPrivateId());
    }
    private int isDuplicate(//
	    GSResource existingResource, //
	    GSResource incomingResource, //
	    HarvestingProperties properties, //
	    boolean firstHarvesting, //
	    boolean isRecovery, //
	    boolean isIncremental) {

	GSLoggerFactory.getLogger(getClass()).debug("Existing resource \"{}\"", existingResource);
	GSLoggerFactory.getLogger(getClass()).debug("Existing resource source \"{}\"", existingResource.getSource().getLabel());

	boolean sameSource = existingResource.getSource().getUniqueIdentifier().equals(//
		incomingResource.getSource().getUniqueIdentifier());

	boolean sameOriginalId = Objects.nonNull(existingResource.getOriginalId()) //
		&& existingResource.getOriginalId().//
			equals(incomingResource.getOriginalId());

	//
	// basic condition
	//
	if (sameSource && sameOriginalId) {

	    GSLoggerFactory.getLogger(getClass()).debug("Basic condition checked");

	    boolean isReharvesting = Objects.nonNull(properties) && properties.getHarvestingCount() >= 1;

	    //
	    // case 1
	    //
	    if (!isRecovery && firstHarvesting) {

		GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource found: case 1");

		return 1;
	    }

	    //
	    // case 2. it should never happen
	    //
	    if (isRecovery && (firstHarvesting || isIncremental)) {

		GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource found: case 2");

		return 2;
	    }

	    //
	    // case 3
	    //
	    if (!firstHarvesting && !isRecovery && isIncremental) {

		GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource found: case 3");

		return 3;
	    }

	    Optional<String> existingResTimeStamp = existingResource.getPropertyHandler().getResourceTimeStamp();
	    String endHarvestingTimestamp = properties.getEndHarvestingTimestamp();

	    //
	    // if this check fails, we are in the case 4
	    //
	    boolean isConsolidated = existingResTimeStamp.isPresent() && existingResTimeStamp.get().compareTo(endHarvestingTimestamp) < 0;

	    GSLoggerFactory.getLogger(getClass()).debug("Existing resource time stamp: " + existingResTimeStamp.orElse("none"));
	    GSLoggerFactory.getLogger(getClass()).debug("End harvesting    time stamp: " + endHarvestingTimestamp);

	    GSLoggerFactory.getLogger(getClass()).debug("Existing resource is consolidated: " + isConsolidated);

	    //
	    // case 5
	    //
	    if (!isRecovery && !isIncremental && isReharvesting && !isConsolidated) {

		GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource found: case 5");

		return 5;
	    }

	    //
	    // case 6. it should never happen
	    //
	    if (isRecovery && !isIncremental && isReharvesting && !isConsolidated) {

		GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource found: case 6");

		return 6;
	    }
	}

	return 0;
    }

    /**
     * @param incomingResource
     * @param harvestingProperties
     * @param isFirstHarvesting
     * @param isRecovery
     * @throws DuplicatedResourceException
     * @throws ConflictingResourceException
     * @throws GSException
     */
    public void decorateHarvestedIdentifier(//
	    GSResource incomingResource, //
	    HarvestingProperties harvestingProperties, //
	    SourceStorage storage, //
	    boolean isFirstHarvesting, //
	    boolean isRecovery, //
	    boolean isIncremental) throws DuplicatedResourceException, ConflictingResourceException, GSException {

	if (useOriginalId(incomingResource)) {

	    GSLoggerFactory.getLogger(getClass()).debug("Using original identifier");

	    String originalId = incomingResource.getOriginalId().get();
	    //
	    // this method searches for resources with the provided original identifier
	    // in the whole DB, included the temporary folders in order
	    // to apply the isDuplicate check since
	    //
	    List<GSResource> existingResources = getDbReader().getResources(IdentifierType.ORIGINAL, originalId);

	    if (!existingResources.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).debug("Found [{}] existing resources with original id [{}]", //
			existingResources.size(), //
			originalId);

		ConflictingResourceException crex = null;

		for (GSResource existingResource : existingResources) {

		    GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource test STARTED");

		    int duplicationCase = isDuplicate(//
			    existingResource, //
			    incomingResource, //
			    harvestingProperties, //
			    isFirstHarvesting, //
			    isRecovery, //
			    isIncremental);

		    GSLoggerFactory.getLogger(getClass()).debug("Duplicated resource test ENDED");

		    if (duplicationCase > 0) {
			//
			// the source administrator should be warn since it means that the source
			// provides records with same identifier.
			// the exception is thrown so the HarvesterPlan which catches it, skips this
			// resource
			//
			throw new DuplicatedResourceException(incomingResource, existingResource, originalId, duplicationCase);

		    } else if (isConflictingResource(storage, existingResource, incomingResource.getSource())) {
			//
			// in the consolidated folders of the whole DB several records with same original id can exist.
			// this exception gathers all the triples (originalId, source, existing source)
			// before to be thrown
			//
			if (Objects.isNull(crex)) {

			    crex = new ConflictingResourceException();
			}

			crex.add(originalId, incomingResource.getSource(), existingResource.getSource());

		    } else {
			//
			// using original identifier
			//
			incomingResource.setPrivateId(originalId);
			incomingResource.setPublicId(originalId);
		    }
		}

		if (Objects.nonNull(crex)) {
		    //
		    // skip and contact data provider
		    // PROBLEM: when Data Source identifier is changed and harvested data aren't deleted.
		    //
		    throw crex;
		}

	    } else {
		//
		// using original identifier
		//
		incomingResource.setPrivateId(originalId);
		incomingResource.setPublicId(originalId);
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Using random identifier");

	    decorateDistributedIdentifier(incomingResource);
	}
    }

    /**
     * @param storage no longer used
     * @param existingResource
     * @param incomingSource
     * @return
     * @throws GSException
     */
    private boolean isConflictingResource(SourceStorage storage, GSResource existingResource, GSSource incomingSource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Conflicting resource test STARTED");

	GSLoggerFactory.getLogger(getClass()).debug("Existing resource \"{}\"", existingResource);
	GSLoggerFactory.getLogger(getClass()).debug("Existing resource source \"{}\"", existingResource.getSource().getLabel());

	boolean sameSource = existingResource.getSource().getUniqueIdentifier().//
		equals(incomingSource.getUniqueIdentifier());

	GSLoggerFactory.getLogger(getClass()).debug("Existing resource is conflicting: {}", !sameSource);

	GSLoggerFactory.getLogger(getClass()).debug("Conflicting resource test ENDED");

	return !sameSource;

	// HarvestingProperties properties = storage.getProperties(existingResource.getSource());
	//
	// if (Objects.isNull(properties)) {
	//
	// return false;
	// }
	//
	// String endHarvestingTimestamp = properties.getEndHarvestingTimestamp();
	//
	// if (Objects.isNull(endHarvestingTimestamp)) {
	//
	// return false;
	// }
	//
	// boolean isConflicting = existingResource.getPropertyHandler().//
	// getResourceTimeStamp().get().compareTo(endHarvestingTimestamp) < 0;
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Existing resource is conflicting: {}", isConflicting);
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Conflicting resource test ENDED");
	//
	// return isConflicting;
    }

    /**
     * The original identifier is used in 3 cases:
     * <ol>
     * <li>The resource is a collection and the related source has the collection priority</li>
     * <li>The source is in the list of the priority source document</li>
     * <li>The original id is an UUID</li>
     * </ol>
     * 
     * @param resource
     * @param isRecovering
     * @return
     */
    private boolean useOriginalId(GSResource resource) {

	Optional<String> originalId = resource.getOriginalId();

	//
	// if the source has a collection priority set and the current resource is a collection,
	// its original identifier must be preserved otherwise the children datasets would lost
	// the "link to the parent" and the second level query would failed
	//
	boolean hasCollectionPriority = resource.getSource().getResultsPriority() == ResultsPriority.COLLECTION;
	boolean collection = resource.getResourceType() == ResourceType.DATASET_COLLECTION;

	boolean hasDocumentPriority = getSourcePriorityDocument().isPresent() && //
		getSourcePriorityDocument().get().isPrioritySource(resource.getSource());

	return originalId.isPresent() && (//
	(collection && hasCollectionPriority) || //
		isUUID(originalId.get()) || //
		hasDocumentPriority//
	);
    }

    /**
     * @param id
     * @return
     */
    private boolean isUUID(String id) {
	// REGULAR EXPRESSION
	// ^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
	try {
	    UUID.fromString(id);
	    return true;
	} catch (IllegalArgumentException e) {
	    return false;
	}
    }

    String generateUniqueIdentifier() {
	return UUID.randomUUID().toString();
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return new HashMap<>();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	return;
    }

    @Override
    public void onFlush() throws GSException {
	return;
    }
}
