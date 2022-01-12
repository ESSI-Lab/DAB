package eu.essi_lab.messages.sem;

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
import java.util.List;
import java.util.Optional;

import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.d2k.D2KGSKnowledgeScheme;

/**
 * @author Fabrizio
 */
public abstract class SemanticOperation {

    private List<String> searchTerms;
    private String ontologyId;
    private GSKnowledgeScheme scheme;

    /**
     * 
     */
    public SemanticOperation() {

	this(null, D2KGSKnowledgeScheme.getInstance());
    }

    /**
     * @param ontologyId
     */
    public SemanticOperation(String ontologyId) {

	this(ontologyId, D2KGSKnowledgeScheme.getInstance());
    }

    /**
     * @param scheme
     */
    public SemanticOperation(GSKnowledgeScheme scheme) {

	this(null, scheme);
    }

    /**
     * @param ontologyId
     * @param scheme
     */
    public SemanticOperation(String ontologyId, GSKnowledgeScheme scheme) {

	this.ontologyId = ontologyId;
	setScheme(scheme);
	setSearchTerms(new ArrayList<>());
    }

    /**
     * @param scheme
     */
    public void setScheme(GSKnowledgeScheme scheme) {

	this.scheme = scheme;
    }

    /**
     * @param ontologyId
     */
    public void setOntologyId(String ontologyId) {

	this.ontologyId = ontologyId;
    }

    /**
     * @param searchTerms
     */
    public void setSearchTerms(List<String> searchTerms) {

	this.searchTerms = searchTerms;
    }

    /**
     * @return
     */
    public GSKnowledgeScheme getScheme() {

	return scheme;
    }

    /**
     * @return
     */
    public Optional<String> getOntologyId() {

	return Optional.ofNullable(ontologyId);
    }

    /**
     * @return
     */
    public List<String> getSearchTerms() {

	return searchTerms;
    }
}
