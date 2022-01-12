package eu.essi_lab.dbupdater;

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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
public class AugmenterWorker {

    private Bond sourcesBond;
    private DatabaseReader reader;
    private DatabaseWriter writer;

    private List<Augmenter> augmenters;
    private int maxRecords;
    private boolean orderingSet;
    private long timeBack;

    public AugmenterWorker() {

	augmenters = new ArrayList<>();
    }

    /**
     * @param dataBaseWriter
     */
    public void setDatabaseWriter(DatabaseWriter dataBaseWriter) {

	this.writer = dataBaseWriter;
    }

    /**
     * @param executor
     */
    public void setDatabaseReader(DatabaseReader executor) {

	this.reader = executor;
    }

    /**
     * @param sourceIds
     */
    public void setSourcesIdsList(List<String> sourceIds) {

	List<ResourcePropertyBond> list = sourceIds.//
		stream().//
		map(id -> BondFactory.createSourceIdentifierBond(id)).//
		collect(Collectors.toList());

	if (list.size() == 1) {

	    sourcesBond = list.get(0);

	} else {

	    LogicalBond orBond = BondFactory.createOrBond();
	    list.forEach(b -> orBond.getOperands().add(b));

	    sourcesBond = orBond;
	}
    }

    /**
     * @param augmenters
     */
    public void setAugmenters(List<Augmenter> augmenters) {

	this.augmenters = augmenters;
    }

    /**
     * @param maxRecords
     */
    public void setMaxRecords(int maxRecords) {

	this.maxRecords = maxRecords;
    }

    /**
     * @param timeBack
     */
    public void setTimeBack(long timeBack) {

	this.timeBack = timeBack;
    }

    /**
     * @param orderingSet
     */
    public void setMostRecentOrdering(boolean orderingSet) {

	this.orderingSet = orderingSet;
    }

    @JsonIgnore
    public void augment() throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Augmentation STARTED");

	if (maxRecords > 0) {

	    GSLoggerFactory.getLogger(getClass()).info("Number of records to augment limited to: " + maxRecords);
	}

	boolean completed = false;

	int start = 1;
	int step = 50;

	DiscoveryMessage message = new DiscoveryMessage();
	
	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());

	if (orderingSet) {

	    message.setOrderingDirection(OrderingDirection.ASCENDING);
	    message.setOrderingProperty(ResourceProperty.RESOURCE_TIME_STAMP);

	    GSLoggerFactory.getLogger(getClass()).info("Less recent ordering set");
	}

	LogicalBond andBond = BondFactory.createAndBond();
	//
	// add the sources bond
	//
	andBond.getOperands().add(sourcesBond);

	if (timeBack > 0) {

	    long timeMillis = System.currentTimeMillis();
	    long startTime = timeMillis - timeBack;

	    String startTimeS = ISO8601DateTimeUtils.getISO8601DateTime(new Date(startTime));

	    GSLoggerFactory.getLogger(getClass()).info("Minimum resource time stamp set to: " + startTimeS);

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

	int count = reader.count(message).getCount();

	if (count == 0) {

	    GSLoggerFactory.getLogger(getClass()).warn("No resource to augment");
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Found " + count + " resources to augment");

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
	augmenters = augmenters.//
		stream().//
		sorted(Comparator.comparing(Augmenter::getPriority)).//
		collect(Collectors.toList());

	while (!completed) {

	    Page page = new Page(start, step);
	    message.setPage(page);

	    ResultSet<GSResource> response = reader.discover(message);
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

			GSLoggerFactory.getLogger(getClass()).warn("Augmentation error occurred");
			GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
		    }
		}

		//
		// if the resource is augmented, it is updated
		//
		if (optional.isPresent()) {

		    GSLoggerFactory.getLogger(getClass()).trace("Updating augmented resource STARTED");

		    this.writer.update(optional.get());

		    GSLoggerFactory.getLogger(getClass()).trace("Updating augmented resource ENDED");
		}

		if (!concepts.isEmpty()) {
		    for (GSKnowledgeResourceDescription concept : concepts) {

			GSLoggerFactory.getLogger(getClass()).trace("Storing concepts STARTED");

			this.writer.store(concept);

			GSLoggerFactory.getLogger(getClass()).trace("Storing concepts ENDED");
		    }
		}
	    }

	    start += step;

	    logger.iterationDone();
	}

	GSLoggerFactory.getLogger(getClass()).info("Augmentation ENDED");
    }
}
