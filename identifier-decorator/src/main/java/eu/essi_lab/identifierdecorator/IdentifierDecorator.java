package eu.essi_lab.identifierdecorator;

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

import eu.essi_lab.model.exceptions.ErrorInfo;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceType;

/**
 * @author Fabrizio
 */
public class IdentifierDecorator {

	private DatabaseReader dbReader;
	private SourcePrioritySetting sourcePrioritySetting;
	private Boolean preserveIds;
	private static final String PID_SEPARATOR = "@";

	/**
	 *
	 */
	public IdentifierDecorator() {
	}

	/**
	 * @param sourcePrioritySetting
	 * @param dataBaseReader
	 */
	public IdentifierDecorator(//
			SourcePrioritySetting sourcePrioritySetting, //
			DatabaseReader dataBaseReader) {

		this(sourcePrioritySetting, false, dataBaseReader);
	}

	/**
	 * @param dataBaseReader
	 * @param preserveIds
	 * @param sourcePrioritySetting
	 */
	public IdentifierDecorator(//
			SourcePrioritySetting sourcePrioritySetting, //
			Boolean preserveIds, //
			DatabaseReader dataBaseReader) {

		this.preserveIds = preserveIds;
		this.dbReader = dataBaseReader;
		this.sourcePrioritySetting = sourcePrioritySetting;
	}

	/**
	 * @param resource
	 */
	public void decorateDistributedIdentifier(GSResource resource) {

		decorateIdentifier(resource, resource.getSource());
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

		Optional<String> opOrig = incomingResource.getOriginalId();

		if (!opOrig.isPresent()) {

			throw GSException.createException(getClass(), "No Original Id Found", null, null, ErrorInfo.ERRORTYPE_SERVICE,
					ErrorInfo.SEVERITY_ERROR,
					"NoOriginalId");

		}

		String originalId = opOrig.get();

		//
		// this method searches for resources with the provided original identifier
		// in the whole DB, included the temporary/writing folders in order
		// to apply the isDuplicate check
		//
		List<GSResource> existingResources = getDatabaseReader().getResources(IdentifierType.ORIGINAL, originalId);

		if (!existingResources.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).debug("Found [{}] existing resources with original id [{}]", //
					existingResources.size(), //
					originalId);

			ConflictingResourceException crex = null;

			for (GSResource existingResource : existingResources) {

				GSLoggerFactory.getLogger(getClass()).warn("Existing resource \"{}\"", existingResource);
				GSLoggerFactory.getLogger(getClass()).warn("Existing resource source \"{}\"", existingResource.getSource().getLabel());

				int duplicationCase = isDuplicate(//
						existingResource, //
						incomingResource, //
						harvestingProperties, //
						isFirstHarvesting, //
						isRecovery, //
						isIncremental);

				if (duplicationCase > 0) {
					//
					// the source administrator should be warn since it means that the source
					// provides records with same identifier.
					// the exception is thrown so the HarvesterPlan which catches it, skips this
					// resource
					//
					throw new DuplicatedResourceException(incomingResource, existingResource, originalId, duplicationCase);

				}
			}
		}

		if (useOriginalId(incomingResource)) {

			if (!existingResources.isEmpty()) {

				ConflictingResourceException crex = null;

				for (GSResource existingResource : existingResources) {

					GSLoggerFactory.getLogger(getClass()).warn("Existing resource \"{}\"", existingResource);
					GSLoggerFactory.getLogger(getClass()).warn("Existing resource source \"{}\"", existingResource.getSource().getLabel());

					if (isConflictingResource(existingResource, incomingResource.getSource())) {
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
						incomingResource.setPrivateId(StringUtils.URLEncodeUTF8(originalId));
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
				incomingResource.setPrivateId(StringUtils.URLEncodeUTF8(originalId));
				incomingResource.setPublicId(originalId);
			}
		} else if (preserveIds) {

			GSResource existingResource = getDatabaseReader().getResource(originalId, incomingResource.getSource());

			if (existingResource != null) {

				// successive harvesting
				incomingResource.setPrivateId(existingResource.getPrivateId());
				incomingResource.setPublicId(existingResource.getPublicId());

			} else {

				// first harvesting
				decorateIdentifier(incomingResource, incomingResource.getSource());
			}

		} else {

			decorateIdentifier(incomingResource, incomingResource.getSource());
		}
	}

	/**
	 * @return
	 */
	public DatabaseReader getDatabaseReader() {

		return this.dbReader;
	}

	/**
	 * @param resource
	 */
	private void decorateIdentifier(GSResource resource, GSSource source) {

		StringBuilder builder = new StringBuilder();

		builder.append(resource.getOriginalId().get());
		builder.append(PID_SEPARATOR);
		builder.append(source.getUniqueIdentifier());

		String pid = builder.toString();

		GSLoggerFactory.getLogger(getClass()).debug("Created persistent identifier {} for resource with original id {} from source {}({})",
				pid, resource.getOriginalId().get(), source.getUniqueIdentifier(), source.getLabel());

		resource.setPrivateId(pid);
		resource.setPublicId(pid);
	}

	/**
	 * See diagram https://confluence.geodab.eu/display/GPD/Identifier+Decorator
	 *
	 * @param existingResource
	 * @param incomingResource
	 * @param properties
	 * @param firstHarvesting
	 * @param isRecovery
	 * @param isIncremental
	 * @return
	 */
	private int isDuplicate(//
			GSResource existingResource, //
			GSResource incomingResource, //
			HarvestingProperties properties, //
			boolean firstHarvesting, //
			boolean isRecovery, //
			boolean isIncremental) {

		boolean sameSource = existingResource.getSource().getUniqueIdentifier().equals(//
				incomingResource.getSource().getUniqueIdentifier());

		boolean sameOriginalId = Objects.nonNull(existingResource.getOriginalId()) //
				&& existingResource.getOriginalId().//
				equals(incomingResource.getOriginalId());

		//
		// basic condition
		//
		if (sameSource && sameOriginalId) {

			boolean isReharvesting = Objects.nonNull(properties) && properties.getHarvestingCount() >= 1;

			//
			// case 1
			//
			if (!isRecovery && firstHarvesting) {

				GSLoggerFactory.getLogger(getClass()).warn("Duplicated resource found: case 1");

				return 1;
			}

			//
			// case 2. it should never happen
			//
			if (isRecovery && (firstHarvesting || isIncremental)) {

				GSLoggerFactory.getLogger(getClass()).warn("Duplicated resource found: case 2");

				return 2;
			}

			//
			// case 3
			//
			if (!firstHarvesting && !isRecovery && isIncremental) {

				GSLoggerFactory.getLogger(getClass()).warn("Duplicated resource found: case 3");

				return 3;
			}

			Optional<String> existingResTimeStamp = existingResource.getPropertyHandler().getResourceTimeStamp();
			String endHarvestingTimestamp = properties.getEndHarvestingTimestamp();

			//
			// if this check fails, we are in the case 4
			//
			boolean isConsolidated = existingResTimeStamp.isPresent() && existingResTimeStamp.get().compareTo(endHarvestingTimestamp) < 0;

			GSLoggerFactory.getLogger(getClass()).warn("Existing resource time stamp: " + existingResTimeStamp.orElse("none"));
			GSLoggerFactory.getLogger(getClass()).warn("End harvesting    time stamp: " + endHarvestingTimestamp);

			GSLoggerFactory.getLogger(getClass()).warn("Existing resource is consolidated: " + isConsolidated);

			//
			// case 5
			//
			if (!isRecovery && !isIncremental && isReharvesting && !isConsolidated) {

				GSLoggerFactory.getLogger(getClass()).warn("Duplicated resource found: case 5");

				return 5;
			}

			//
			// case 6. it should never happen
			//
			if (isRecovery && !isIncremental && isReharvesting && !isConsolidated) {

				GSLoggerFactory.getLogger(getClass()).warn("Duplicated resource found: case 6");

				return 6;
			}
		}

		return 0;
	}

	/**
	 * Two datasets (existing and incoming) conflict if all the following conditions apply:<ol>
	 * <li>they have the same original id</li>
	 * <li>they come from different sources</li>
	 * <li>the existing resource uses the original id as public id</li>
	 * </ul>
	 *
	 * @param existingResource
	 * @param incomingSource
	 * @return
	 * @throws GSException
	 */
	private boolean isConflictingResource(GSResource existingResource, GSSource incomingSource) throws GSException {

		boolean originalAsPublic = existingResource.getPublicId().equals(existingResource.getOriginalId().get());

		boolean differentSource = !existingResource.getSource().getUniqueIdentifier().//
				equals(incomingSource.getUniqueIdentifier());

		boolean conflicting = differentSource && originalAsPublic;

		GSLoggerFactory.getLogger(getClass()).warn("Existing resource is conflicting: {}", conflicting);

		return conflicting;
	}

	/**
	 * The original identifier is used if:
	 * <ol>
	 * <li>The source is configured as priority source</li>
	 * </ol>
	 *
	 * @param resource
	 * @return
	 */
	private boolean useOriginalId(GSResource resource) {

		return sourcePrioritySetting.isPrioritySource(resource.getSource());

		//		Optional<String> originalId = resource.getOriginalId();
		//
		//		//
		//		// if the source has a collection priority set and the current resource is a collection,
		//		// its original identifier must be preserved otherwise the children datasets would lost
		//		// the "link to the parent" and the second level query would failed
		//		//
		//		boolean hasCollectionPriority = resource.getSource().getResultsPriority() == ResultsPriority.COLLECTION;
		//		boolean collection = resource.getResourceType() == ResourceType.DATASET_COLLECTION;
		//
		//		boolean isPrioritySource = sourcePrioritySetting.isPrioritySource(resource.getSource());
		//
		//		return originalId.isPresent() && (//
		//				(collection && hasCollectionPriority) || //
		//						StringUtils.isUUID(originalId.get()) || //
		//						isPrioritySource//
		//		);
	}

}
