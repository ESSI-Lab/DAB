package eu.essi_lab.pdk.wrt;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.sem.SemanticBrowsing;
import eu.essi_lab.messages.sem.SemanticBrowsing.BrowsingAction;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticOperation;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * @author Fabrizio
 */
public abstract class SemanticRequestTransformer extends WebRequestTransformer<SemanticMessage> {

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract Optional<Bond> getUserBond(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract SemanticOperation getOperation(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract Optional<String> getSubjectId(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract Optional<String> getOntologyId(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract List<String> getSearchTerms(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract Optional<GSKnowledgeScheme> getScheme(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract Optional<BrowsingAction> getBrowsingAction(WebRequest request) throws GSException;

    @Override
    protected SemanticMessage refineMessage(SemanticMessage message) throws GSException {

	SemanticOperation operation = getOperation(message.getWebRequest());
	message.setOperation(operation);

	if (operation instanceof SemanticBrowsing) {

	    SemanticBrowsing browsing = (SemanticBrowsing) operation;

	    Optional<String> subjectId = getSubjectId(message.getWebRequest());
	    if (subjectId.isPresent()) {
		browsing.setSubjectId(subjectId.get());
	    }
	}

	Optional<String> ontologyId = getOntologyId(message.getWebRequest());
	if (ontologyId.isPresent()) {
	    operation.setOntologyId(ontologyId.get());
	}

	List<String> searchTerms = getSearchTerms(message.getWebRequest());
	if (!searchTerms.isEmpty()) {
	    operation.setSearchTerms(searchTerms);
	}

	Optional<GSKnowledgeScheme> scheme = getScheme(message.getWebRequest());
	if (scheme.isPresent()) {
	    operation.setScheme(scheme.get());
	}

	Optional<Bond> bond = getUserBond(message.getWebRequest());
	if (bond.isPresent()) {
	    message.setUserBond(bond.get());
	}

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(message.getWebRequest());

	try {

	    //
	    // logs message info
	    //

	    if (publisher.isPresent()) {

		publisher.get().publish(message);
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	}

	return message;
    }

    @Override
    protected SemanticMessage createMessage() {

	return new SemanticMessage();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
