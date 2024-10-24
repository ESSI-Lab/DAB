/**
 * 
 */
package eu.essi_lab.request.executor.discover.semantic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoverySemanticMessage;
import eu.essi_lab.messages.DiscoverySemanticMessage.ExpansionPolicy;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.sem.SemanticBrowsing;
import eu.essi_lab.messages.sem.SemanticBrowsing.BrowsingAction;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.messages.sem.SemanticSearch;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IDiscoverySemanticExecutor;
import eu.essi_lab.request.executor.ISemanticExecutor;

/**
 * @author Fabrizio
 */
public class DiscoverySemanticExecutor implements IDiscoverySemanticExecutor {

    @Override
    public CountSet count(DiscoverySemanticMessage message) throws GSException {

	message = augmentMessage(message);

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	return executor.count(message);
    }

    @Override
    public ResultSet<GSResource> retrieve(DiscoverySemanticMessage message) throws GSException {

	message = augmentMessage(message);

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	return executor.retrieve(message);
    }

    @Override
    public boolean isAuthorized(DiscoverySemanticMessage message) throws GSException {
	// TODO Auto-generated method stub
	return true;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private DiscoverySemanticMessage augmentMessage(DiscoverySemanticMessage message) throws GSException {

	ServiceLoader<ISemanticExecutor> loader = ServiceLoader.load(ISemanticExecutor.class);
	ISemanticExecutor executor = loader.iterator().next();

	// the search terms to expand
	List<String> searchTerms = message.getTermsToExpand();

	if (searchTerms.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("No search terms to expand");

	    return message;
	}

	// the policy to use in order to expand the above search terms
	ExpansionPolicy expansionPolicy = message.getExpansionPolicy();

	// the queryables to use as target of the discovery query
	List<Queryable> discoveryQueryables = message.getDiscoveryQueryables();

	// the scheme to handle browsing and labels
	GSKnowledgeScheme scheme = message.getScheme();

	//
	// executes the semantic search
	//
	GSLoggerFactory.getLogger(getClass()).debug("Semantic search STARTED");

	GSLoggerFactory.getLogger(getClass()).debug("Selected search terms: {}", searchTerms);

	List<GSKnowledgeResourceDescription> searchResult = execSearch(executor, searchTerms, message);

	GSLoggerFactory.getLogger(getClass()).debug("Semantic search ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Found {} concepts from semantic search", searchResult.size());

	Set<String> labels = getLabels(scheme.getLabelPredicate(), searchResult);

	GSLoggerFactory.getLogger(getClass()).debug("Found {} labels from semantic search: {}", labels.size(), labels);

	BrowsingAction action = null;

	switch (expansionPolicy) {
	case SEARCH:
	    // in this case there is nothing else to do
	    GSLoggerFactory.getLogger(getClass()).debug("No browsing action required");
	    break;
	case SEARCH_AND_EXPAND:

	    action = BrowsingAction.EXPAND;
	    break;

	case SEARCH_AND_COLLAPSE:
	    action = BrowsingAction.COLLAPSE;
	    break;
	}

	//
	// exec the browsing action on the concepts resulting from the search
	//
	if (action != null) {

	    GSLoggerFactory.getLogger(getClass()).debug("Required browsing action: {}", action);

	    for (int i = 0; i < searchResult.size(); i++) {

		GSKnowledgeResourceDescription description = searchResult.get(i);

		GSLoggerFactory.getLogger(getClass()).debug("Browsing of concept [{}/{}] STARTED", i + 1, searchResult.size());

		String subjectId = description.getResource().stringValue();

		GSLoggerFactory.getLogger(getClass()).debug("Current subject id: {}", subjectId);

		Set<String> expandedLabels = execBrowsing(executor, subjectId, action, message);

		GSLoggerFactory.getLogger(getClass()).debug("Found {} labels from semantic expansion: {}", expandedLabels.size(),
			expandedLabels);

		int labelsSize = labels.size();

		labels.addAll(expandedLabels);

		if (labels.size() > labelsSize) {

		    int additional = labels.size() - labelsSize;
		    GSLoggerFactory.getLogger(getClass()).debug("Added additional {} labels due to semantic browsing", additional);
		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("No additional labels added after the semantic browsing");
		}

		GSLoggerFactory.getLogger(getClass()).debug("Browsing of concept [{}/{}] ENDED", i + 1, searchResult.size());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Found a total of {} labels for the discovery: {}", labels.size(), labels);

	if (labels.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("No labels found after semantic expansion, original message need no changes");

	    return message;
	}

	//
	// creates the expanded bond which will replace each user bond with
	// the original discovery search terms to expand
	//
	Bond expandedDiscoveryBond = null;

	if (discoveryQueryables.size() == 1 && labels.size() == 1) {

	    Queryable queryable = discoveryQueryables.get(0);

	    expandedDiscoveryBond = createBond(queryable, labels.toArray(new String[] {})[0]);

	} else {

	    LogicalBond orBond = BondFactory.createOrBond();

	    for (Queryable queryable : discoveryQueryables) {
		for (String label : labels) {

		    orBond.getOperands().add(createBond(queryable, label));
		}
	    }

	    expandedDiscoveryBond = orBond;
	}

	//
	// replaces the original user bond (if present) with a bond where in place of the
	// bond with the provided expansion search terms there is a copy of
	// the expanded discovery bond
	//
	Optional<Bond> userBond = message.getUserBond();

	if (!userBond.isPresent() || userBond.get() instanceof QueryableBond) {

	    // in this case there is a single user bond with a single expansion search term
	    // so the user bond can be entirely replaced with the expanded discovery bond
	    //
	    message.setUserBond(expandedDiscoveryBond);

	} else {

	    LogicalBond logical = (LogicalBond) userBond.get();

	    ArrayList<Bond> bondList = new ArrayList<Bond>();
	    for (int i = 0; i < searchTerms.size(); i++) {
		bondList.add(expandedDiscoveryBond.clone());
	    }

	    // replaces each bond with the provided search terms
	    // with a copy of the expanded discovery bond
	    LogicalBond replaced = logical.replaceWithBonds(searchTerms, bondList);

	    message.setUserBond(replaced);
	}

	return message;
    }

    /**
     * @param queryable
     * @param value
     * @return
     */
    private Bond createBond(Queryable queryable, String value) {

	if (queryable instanceof MetadataElement) {

	    return BondFactory.createSimpleValueBond(BondOperator.LIKE, (MetadataElement) queryable, value);
	}

	return BondFactory.createResourcePropertyBond(BondOperator.LIKE, (ResourceProperty) queryable, value);
    }

    /**
     * @param executor
     * @param searchTerms
     * @param message
     * @return
     * @throws GSException
     */
    private List<GSKnowledgeResourceDescription> execSearch(ISemanticExecutor executor, List<String> searchTerms,
	    DiscoverySemanticMessage message) throws GSException {

	//
	// creates the semantic message with the search operation, which must be
	// always executed as first operation
	//
	SemanticMessage semanticMessage = new SemanticMessage();
	semanticMessage.setDataBaseURI(message.getDataBaseURI());

	SemanticSearch semanticSearch = new SemanticSearch();
	semanticMessage.setOperation(semanticSearch);

	// set the search terms provided by the DiscoverySemanticMessage
	semanticSearch.setSearchTerms(searchTerms);

	// set the scheme provided by the DiscoverySemanticMessage
	semanticSearch.setScheme(message.getScheme());

	//
	// executes the semantic search
	//
	SemanticResponse<GSKnowledgeResourceDescription> semanticResponse = executor.retrieve(semanticMessage);
	return semanticResponse.getResultsList();
    }

    /**
     * @param subjectId
     * @param executor
     * @return
     * @throws GSException
     */
    private Set<String> execBrowsing(//
	    ISemanticExecutor executor, //
	    String subjectId, //
	    BrowsingAction action, //
	    DiscoverySemanticMessage message) throws GSException {

	SemanticBrowsing browsing = new SemanticBrowsing(action);
	browsing.setSubjectId(subjectId);

	GSKnowledgeScheme scheme = message.getScheme();

	// set the scheme provided by the DiscoverySemanticMessage
	browsing.setScheme(scheme);

	SemanticMessage semanticMessage = new SemanticMessage();
	semanticMessage.setOperation(browsing);
	semanticMessage.setDataBaseURI(message.getDataBaseURI());

	SemanticResponse<GSKnowledgeResourceDescription> semanticResponse = executor.retrieve(semanticMessage);
	List<GSKnowledgeResourceDescription> descriptions = semanticResponse.getResultsList();

	return getLabels(scheme.getLabelPredicate(), descriptions);
    }

    /**
     * @param languagePredicate
     * @param descriptions
     * @return
     */
    private Set<String> getLabels(GSPredicate languagePredicate, List<GSKnowledgeResourceDescription> descriptions) {

	Set<String> out = new HashSet<>();

	for (GSKnowledgeResourceDescription desc : descriptions) {

	    List<String> labels = desc.//
		    getLabels(languagePredicate).//
		    stream().map(l -> l.stringValue()).//
		    collect(Collectors.toList());

	    out.addAll(labels);
	}

	return out;
    }
}
