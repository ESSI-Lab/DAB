package eu.essi_lab.api.database;

import java.io.ByteArrayInputStream;

import javax.xml.bind.Marshaller;

import org.apache.commons.io.output.ByteArrayOutputStream;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.jaxb.ViewFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.serialization.J2RDFSerializer;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public abstract class DatabaseWriter implements DatabaseProvider {

    /**
     * Stores the given <code>resource</code>
     *
     * @param resource
     * @throws GSException
     * @see DatabaseReader#getResource(String)
     * @see GSResource#getPrivateId()
     */
    public void store(GSResource resource) throws GSException {
    
        GSSource source = resource.getSource();
    
        Database markLogicDB = getDatabase();
    
        try {
    
            SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
    
            DatabaseFolder folder = markLogicDB.findWritingFolder(worker);
    
            Document asDocument = resource.asDocument(true);
    
            String key = resource.getPrivateId();
            folder.store(key, asDocument);
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to store resource {}", resource.getPrivateId(), e);
    
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterStoreResourceError", //
        	    e);
        }
    }

    /**
     * Removes the given <code>resource</code>
     *
     * @param resource
     * @throws GSException if resource is not present or other errors occurred
     */
    public abstract void remove(GSResource resource) throws GSException;

    /**
     * Updates the given resource in a single transaction (e.g. not using the two db operations "remove" + "store"
     * operation, but the single
     * db operation "replace")
     *
     * @param resource
     * @throws GSException
     */
    public void update(GSResource resource) throws GSException {
    
        GSSource source = resource.getSource();
    
        Database markLogicDB = getDatabase();
    
        try {
    
            SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
    
            Document asDocument = resource.asDocument(true);
    
            String key = resource.getPrivateId();
    
            DatabaseFolder folder = markLogicDB.findWritingFolder(worker);
    
            folder.replace(key, asDocument);
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to update resource {}", resource.getPrivateId(), e);
    
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterUpdateResourceError", //
        	    e);
        }
    }

    /**
     * Stores the given <code>document</code> according to the provided <code>identifier</code>
     *
     * @param identifier
     * @param document
     * @throws GSException
     */
    public abstract void store(String identifier, Document document) throws GSException;

    /**
     * Removes the document with the provided <code>identifier</code>
     *
     * @param resource
     * @throws GSException if document is not present or other errors occurred
     */
    public abstract void removeDocument(String identifier) throws GSException;

    /**
     * Stores the given user, overwriting a possible existing user with same identifier
     *
     * @param user
     * @throws GSException
     */
    public void store(GSUser user) throws GSException {
    
        try {
    
            DatabaseFolder folder = getDatabase().getUsersFolder();
    
            Document document = user.asDocument(true);
    
            folder.store(user.getUri(), document);
    
            folder.replace(user.getUri(), document);
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to store user {}", user.getIdentifier(), e);
    
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterStoreUserError", //
        	    e);
        }
    }

    /**
     * Removes the user associated with the given identifier
     *
     * @param userId
     * @throws GSException
     */
    public void removeUser(String userIdentifier) throws GSException {
    
        try {
    
            DatabaseFolder folder = getDatabase().getUsersFolder();
    
            folder.remove(GSUser.toURI(userIdentifier));
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to remove user {}", userIdentifier, e);
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterRemoveUserError", //
        	    e);
        }
    }

    /**
     * Stores the given view, overwriting a possible existing view with the same identifier
     *
     * @param view
     * @throws GSException
     */
    public void store(View view) throws GSException {
    
        try {
    
            String id = view.getId();
    
            DatabaseFolder folder = getDatabase().getViewFolder(true);
    
            ViewFactory factory = new ViewFactory();
    
            Marshaller marshaller = factory.createMarshaller();
    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
            marshaller.marshal(view, baos);
    
            byte[] bytes = baos.toByteArray();
    
            folder.storeBinary(id, new ByteArrayInputStream(bytes));
    
            folder.replaceBinary(id, new ByteArrayInputStream(bytes));
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to store view {}", view.getId(), e);
    
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterStoreViewError", //
        	    e);
        }
    }

    /**
     * Removes the view associated with the given identifier
     *
     * @param viewId
     * @throws GSException
     */
    public void removeView(String id) throws GSException {
    
        try {
    
            DatabaseFolder folder = getDatabase().getViewFolder(true);
    
            folder.remove(id);
    
        } catch (Exception e) {
    
            GSLoggerFactory.getLogger(getClass()).error("Unable to remove view {}", id, e);
            throw GSException.createException(//
        	    getClass(), //
        	    e.getMessage(), //
        	    null, //
        	    ErrorInfo.ERRORTYPE_INTERNAL, //
        	    ErrorInfo.SEVERITY_ERROR, //
        	    "DatabaseWriterRemoveViewError", //
        	    e);
        }
    }

    /**
     * Stores the RDF document in the common semantic folder.
     *
     * @param rdf
     */
    public abstract void storeRDF(Node rdf) throws GSException;

    /**
     * @param recoveryRemovalToken
     * @param count
     * @throws RequestException
     */
    public abstract void removeByRecoveryRemovalToken(String recoveryRemovalToken, int count) throws GSException;

    /**
     * Stores the supplied <code>object</code> in the common semantic folder. The object is serialized utilizing {@link
     * J2RDFSerializer#toNode(GSKnowledgeResourceDescription)}
     *
     * @param object
     */
    public void store(GSKnowledgeResourceDescription object) throws GSException {

	try {

	    Node asNode = new J2RDFSerializer().toNode(object);

	    storeRDF(asNode);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to resource {}", object.getResource().stringValue(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "DatabaseWriterStoreKRDError", e);
	}

    }

}
