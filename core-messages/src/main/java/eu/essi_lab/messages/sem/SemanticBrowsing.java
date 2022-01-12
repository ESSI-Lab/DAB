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

import java.util.Optional;

import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.d2k.D2KGSKnowledgeScheme;

/**
 * @author Fabrizio
 */
public class SemanticBrowsing extends SemanticOperation {

    /**
     * @author Fabrizio
     */
    public enum BrowsingAction {
	/**
	*
	*/
	EXPAND,
	/**
	 *
	 */
	COLLAPSE;
    }

    private String subjectId;
    private BrowsingAction action;
    
    /**
     * @param ontologyId
     * @param action
     */
    public SemanticBrowsing(BrowsingAction action) {

	this(D2KGSKnowledgeScheme.getInstance(), null, action);
    }

    /**
     * @param ontologyId
     * @param action
     */
    public SemanticBrowsing(String ontologyId, BrowsingAction action) {

	this(D2KGSKnowledgeScheme.getInstance(), ontologyId, action);
    }

    /**
     * @param scheme
     * @param ontologyId
     * @param action
     */
    public SemanticBrowsing(GSKnowledgeScheme scheme, String ontologyId, BrowsingAction action) {

	super(ontologyId, scheme);
	this.action = action;
    }

    /**
     * @param action
     */
    public void setAction(BrowsingAction action) {

	this.action = action;
    }

    /**
     * @return
     */
    public BrowsingAction getAction() {

	return action;
    }

    /**
     * @return
     */
    public Optional<String> getSubjectId() {

	return Optional.ofNullable(subjectId);
    }

    /**
     * @param subjectId
     */
    public void setSubjectId(String subjectId) {

	this.subjectId = subjectId;
    }

}
