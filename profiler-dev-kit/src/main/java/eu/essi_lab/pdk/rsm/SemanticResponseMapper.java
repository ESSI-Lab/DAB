package eu.essi_lab.pdk.rsm;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;

/**
 * Implementation specific to map a <code>SemanticResponse&ltOntologyObject&gt</code> in to a <code>SemanticResponse&ltT&gt</code>
 *
 * @param <T> the type to which to map the {@link eu.essi_lab.model.ontology.d2k.GSKnowledgeConcept}s of the response  (e.g.: String,
 * JSON, XML, etc.)
 * @author Fabrizio
 */
public abstract class SemanticResponseMapper<T> implements
	MessageResponseMapper<SemanticMessage, GSKnowledgeResourceDescription, T, SemanticCountResponse, SemanticResponse<GSKnowledgeResourceDescription>, SemanticResponse<T>> {

    private Logger logger = GSLoggerFactory.getLogger(SemanticResponseMapper.class);

    @Override
    public SemanticResponse<T> map(//
	    SemanticMessage message, //
	    SemanticResponse<GSKnowledgeResourceDescription> response) throws GSException {

	//
	//
	// converts the incoming SemanticRespons<OntologyObject> in a SemanticResponse<T>
	//
	//
	SemanticResponse<T> out = new SemanticResponse<>(response);

	List<T> list = new ArrayList<>();
	out.setResultsList(list);

	for (GSKnowledgeResourceDescription res : response.getResultsList()) {
	    try {

		T value = map(message, res);
		list.add(value);

	    } catch (Exception e) {

		logger.warn("Exception mapping concept {}", res.getResource().stringValue(), e);

	    }
	}

	// map the parent, if present
	Optional<GSKnowledgeResourceDescription> parentObject = response.getParentObject();
	if (parentObject.isPresent()) {

	    GSKnowledgeResourceDescription object = parentObject.get();
	    T parent = mapParentConcept(object);
	    out.setParentObject((T) parent);
	}

	return out;
    }

    /**
     * This method is called when a "concept/{id}" request is performed and the supplied <code>childConcept</code> is one of the resulting
     * children objects
     *
     * @param childConcept the object to map
     * @return the mapped child concept
     */
    protected abstract T mapChildConcept(GSKnowledgeResourceDescription childConcept);

    /**
     * This method is called when the {@link SemanticResponse#getParentObject()} is not empty. The supplied
     * <code>parentConcept</code>
     * can be mapped like a child concept (see {@link #mapChildConcept(GSKnowledgeResourceDescription)} or in a different way, according to the
     * particular implementation
     *
     * @param parentConcept
     * @return the mapped parent concept
     */
    protected abstract T mapParentConcept(GSKnowledgeResourceDescription parentConcept);

    /**
     * This method is called when the root {@link GSKnowledgeConcept}s (called "entry points") are requested
     *
     * @param entryPoint the entry point to map
     * @return the mapped entry pointS
     */
    protected abstract T mapEntryPoint(GSKnowledgeResourceDescription entryPoint);

    /**
     * At the moment only the following requests are supported:
     * <ul>
     * <li>/concept</li>
     * <li>/concept/{id}</li>
     * </ul>
     *
     * @param message
     * @param ontologyObj
     * @return
     * @throws GSException
     */
    private T map(SemanticMessage message, GSKnowledgeResourceDescription ontologyObj) {

	UriInfo uriInfo = message.getWebRequest().getUriInfo();

	List<PathSegment> pathSegments = uriInfo.getPathSegments();

	PathSegment lastSegment = pathSegments.get(pathSegments.size() - 1);

	T object = null;

	if (lastSegment.getPath().equals("concepts")) {
	    //
	    // /concept (entry point) request
	    //

	    object = mapEntryPoint(ontologyObj);

	} else {
	    //
	    // /concept/{id} (children) request
	    //

	    object = mapChildConcept(ontologyObj);
	}

	return object;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
