package eu.essi_lab.request.executor.discover;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourceType;

/**
 * This object analyzes the message looking for sources having a resource priority set:
 * <ul>
 * <li>{@link ResultsPriority#ALL}: nothing to do</li>
 * <li>{@link ResultsPriority#UNSET}: nothing to do</li>
 * <li>{@link ResultsPriority#DATASET}: adds the resource property bond {@link ResourceProperty#TYPE} with
 * {@link ResourceType#DATASET}</li>
 * <li>{@link ResultsPriority#COLLECTION}:
 * <ol>
 * <li>checks if this is a second level query</li>
 * <li>- if so, the {@link ResourceProperty#TYPE} with {@link ResourceType#DATASET_COLLECTION} is NOT SET in order to
 * allow retrieving of datasets<br>
 * - if not, adds the resource property bond {@link ResourceProperty#TYPE} with
 * {@value ResourceType#DATASET_COLLECTION}</li>
 * </ol>
 * </li>
 * </ul>
 * 
 * @author Fabrizio
 */
public class ResourcePropertyConstraintAdder {

    private List<GSSource> sources;
    private Bond userBond;
    private DiscoveryMessage message;

    /**
     * @param message
     */
    public ResourcePropertyConstraintAdder(DiscoveryMessage message) {

	this.message = message;
	this.userBond = message.getUserBond().orElse(null);

	if (message.getSources() == null) {

	    this.sources = new ArrayList<GSSource>();

	} else {

	    this.sources = message.getSources();
	}
    }

    /**
     * @param requestId
     * @param bond
     * @param queryResultsPriority
     * @return
     * @throws GSException
     */
    public Bond addResourcePropertyConstraints(//
	    String requestId, //
	    Bond bond, //
	    Optional<ResultsPriority> queryResultsPriority) throws GSException {

	if (Objects.isNull(bond)) {

	    GSLoggerFactory.getLogger(getClass()).trace("No bond to analyze, exit");

	    return null;
	}

	if (bond instanceof LogicalBond) {

	    LogicalBond logicalBond = (LogicalBond) bond;

	    List<Bond> originalOperands = logicalBond.getOperands();

	    List<Bond> operands = new ArrayList<Bond>();

	    for (Bond originalOperand : originalOperands) {

		Bond replacedBond = addResourcePropertyConstraints(requestId, originalOperand, queryResultsPriority);

		operands.add(replacedBond);
	    }

	    LogicalBond copyBond = BondFactory.createLogicalBond(logicalBond.getLogicalOperator(), operands);

	    return copyBond;

	} else {

	    if (bond instanceof ResourcePropertyBond) {

		ResourcePropertyBond rpb = (ResourcePropertyBond) bond;

		if (rpb.getProperty() == ResourceProperty.SOURCE_ID) {

		    String currentSourceId = rpb.getPropertyValue();

		    for (GSSource gsSource : sources) {

			String sourceId = gsSource.getUniqueIdentifier();

			if (sourceId.equals(currentSourceId)) {

			    Bond ret = null;

			    LogicalBond andBond = BondFactory.createAndBond();

			    //
			    // default results priority is specified by each source
			    //
			    ResultsPriority resultsPriority = gsSource.getResultsPriority();

			    //
			    // however if user has specified a different results priority, this wins with respect to
			    // default settings
			    //
			    if (queryResultsPriority.isPresent()) {

				resultsPriority = queryResultsPriority.get();
			    }

			    switch (resultsPriority) {
			    case DATASET:

				andBond.getOperands().add(BondFactory.createSourceIdentifierBond(sourceId));
				andBond.getOperands().add(BondFactory.createResourceTypeBond(ResourceType.DATASET));

				ret = andBond;

				break;

			    case COLLECTION:

				List<String> identifiers = getIds(userBond);
				//
				// If the discovery message contains some bonds with
				// MetadataElement.PARENT_IDENTIFIER, 
				// MetadataElement.ONLINE_ID or
				// MetadataElement.IDENTIFIER
				// as property (in this case the query is probably a GetRecordsById),
				// then it is necessary to check whether the current source owns the records with
				// those identifiers.
				// If so, it means that this is a "second level query" or more in general a query which
				// requires to retrieves both collections and datasets, so in order to discover also the
				// datasets, the collection priority must be ignored otherwise datasets cannot be
				// returned
				//
				if (!identifiers.isEmpty()) {

				    String propertyString = gsSource.getSortProperty();

				    //
				    // if a particular collection ordering is required, here it is set
				    // to the discovery message
				    //
				    if (Objects.nonNull(propertyString)) {

					MetadataElement property = MetadataElement.fromName(propertyString);

					SortOrder direction = gsSource.getSortOrder();

					message.setSortProperty(property);
					message.setSortOrder(direction);
				    }

				    //
				    // returning a normal source id bond, no resource type bond added
				    //
				    ret = BondFactory.createSourceIdentifierBond(sourceId);

				} else {

				    //
				    // returning a source id bond with type collection added
				    //
				    andBond.getOperands().add(BondFactory.createSourceIdentifierBond(sourceId));
				    andBond.getOperands().add(BondFactory.createResourceTypeBond(ResourceType.DATASET_COLLECTION));

				    ret = andBond;
				}

				break;

			    case UNSET:
			    case ALL:
			    default:

				//
				// returning a normal source id bond, no resource type bond added
				//
				ret = BondFactory.createSourceIdentifierBond(sourceId);
				break;
			    }

			    return ret;
			}
		    }

		    return bond;

		} else {

		    return bond;
		}
	    } else {

		return bond;
	    }
	}
    }

    // /**
    // * Tests whether the {@link GSSource} with the provided <code>sourceId</code> owns at least one of the records
    // * having the identifier provided by <code>identifiers</code>
    // *
    // * @param identifiers
    // * @return
    // * @throws GSException
    // */
    // private boolean findRecords(String requestId, String sourceId, List<String> identifiers, StorageUri uri) throws
    // GSException {
    //
    // GSLoggerFactory.getLogger(getClass()).trace("Finding records STARTED");
    //
    // GSLoggerFactory.getLogger(getClass()).trace("Source id [{}]", sourceId);
    // GSLoggerFactory.getLogger(getClass()).trace("Identifiers [{}]", identifiers);
    //
    // DiscoveryMessage message = new DiscoveryMessage();
    //
    // message.setRequestId(requestId);
    // LogicalBond bond = BondFactory.createAndBond();
    //
    // LogicalBond orBond = BondFactory.createOrBond();
    //
    // for (String id : identifiers) {
    //
    // orBond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id));
    // orBond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER,
    // id));
    // orBond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, id));
    // }
    //
    // ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);
    //
    // bond.getOperands().add(sourceBond);
    // bond.getOperands().add(orBond);
    //
    // message.setNormalizedBond(bond);
    // message.setPermittedBond(bond);
    //
    // DatabaseReader reader = DatabaseConsumerFactory.createDataBaseReader(uri);
    //
    // DiscoveryCountResponse countResult = reader.count(message);
    //
    // boolean found = countResult.getCount() > 0;
    //
    // GSLoggerFactory.getLogger(getClass()).trace("Records own to source: {}", found);
    //
    // GSLoggerFactory.getLogger(getClass()).trace("Finding records ENDED");
    //
    // return found;
    // }

    /**
     * Extracts the values of the {@link Bond} having the following properties:
     * <ol>
     * <li>{@link MetadataElement#PARENT_IDENTIFIER}</li>
     * <li>{@link MetadataElement#IDENTIFIER}</li>
     * <li>{@link MetadataElement#ONLINE_ID}</li>
     * </ol>
     * 
     * @param userBond
     * @return
     */
    private List<String> getIds(Bond userBond) {

	final List<String> ids = new ArrayList<>();

	DiscoveryBondHandler handler = new DiscoveryBondHandler() {

	    @Override
	    public void startLogicalBond(LogicalBond bond) {
	    }

	    @Override
	    public void separator() {
	    }

	    @Override
	    public void endLogicalBond(LogicalBond bond) {
	    }

	    @Override
	    public void viewBond(ViewBond bond) {
	    }

	    @Override
	    public void spatialBond(SpatialBond bond) {
	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond bond) {

		if (bond.getProperty() == MetadataElement.PARENT_IDENTIFIER || //
		bond.getProperty() == MetadataElement.IDENTIFIER || //
		bond.getProperty() == MetadataElement.ONLINE_ID) {

		    ids.add(bond.getPropertyValue());
		}
	    }

	    @Override
	    public void resourcePropertyBond(ResourcePropertyBond bond) {
	    }

	    @Override
	    public void customBond(QueryableBond<String> bond) {
	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {
	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    }
	};

	new DiscoveryBondParser(userBond).parse(handler);

	return ids;
    }
}
