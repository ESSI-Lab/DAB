package eu.essi_lab.api.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.serialization.J2RDFSerializer;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public interface DatabaseWriter extends DatabaseConsumer {

    /**
     * Stores the given user, overwriting a possible existing user with the same email
     *
     * @param user
     * @throws GSException
     */
    void store(GSUser user) throws GSException;

    /**
     * Stores the given view, overwriting a possible existing view with the same identifier
     *
     * @param view
     * @throws GSException
     */
    void store(View view) throws GSException;

    /**
     * Removes the view associated with the given identifier
     *
     * @param viewId
     * @throws GSException
     */
    void removeView(String viewId) throws GSException;

    /**
     * Stores the given <code>resource</code>
     *
     * @param resource
     * @throws GSException
     * @see DatabaseReader#getResource(String)
     * @see GSResource#getPrivateId()
     */
    void store(GSResource resource) throws GSException;

    /**
     * Removes the given <code>resource</code>
     *
     * @param resource
     * @throws GSException if resource is not present or other errors occurred
     */
    void remove(GSResource resource) throws GSException;

    /**
     * Stores the given <code>document</code> according to the provided <code>identifier</code>
     *
     * @param identifier
     * @param document
     * @throws GSException
     */
    void store(String identifier, Document document) throws GSException;

    /**
     * Removes the document with the provided <code>identifier</code>
     *
     * @param resource
     * @throws GSException if document is not present or other errors occurred
     */
    void removeDocument(String identifier) throws GSException;

    /**
     * Updates the given resource in a single transaction (e.g. not using the two db operations "remove" + "store"
     * operation, but the single
     * db operation "replace")
     *
     * @param resource
     * @throws GSException
     */
    void update(GSResource resource) throws GSException;

    /**
     * Stores the supplied <code>object</code> in the common semantic folder. The object is serialized utilizing {@link
     * J2RDFSerializer#toNode(GSKnowledgeResourceDescription)}
     *
     * @param object
     */
    void store(GSKnowledgeResourceDescription object) throws GSException;

    /**
     * Stores the RDF document in the common semantic folder.
     *
     * @param rdf
     */
    void storeRDF(Node rdf) throws GSException;

}
