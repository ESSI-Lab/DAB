package eu.essi_lab.api.database.marklogic;

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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.jaxb.ViewFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.serialization.J2RDFSerializer;
import eu.essi_lab.model.resource.GSResource;

public class MarkLogicWriter extends MarkLogicReader implements DatabaseWriter {

    private static final String MARK_LOGIC_RESOURCE_STORAGE_ERROR = "MARK_LOGIC_RESOURCE_STORAGE_ERROR";
    private static final String MARK_LOGIC_RESOURCE_REMOVAL_ERROR = "MARK_LOGIC_RESOURCE_REMOVAL_ERROR";
    private static final String MARK_LOGIC_ONTOLOGY_OBJECT_STORAGE_ERROR = "MARK_LOGIC_ONTOLOGY_OBJECT_STORAGE_ERROR";

    private static final String MARK_LOGIC_WRITER_RDF_STORING_ERROR = "MARK_LOGIC_WRITER_RDF_STORING_ERROR";
    private static final String MARK_LOGIC_WRITER_RESOURCE_UPDATING_ERROR = "MARK_LOGIC_WRITER_RESOURCE_UPDATING_ERROR";
    private static final String MARK_LOGIC_WRITER_VIEW_STORAGE_ERROR = "MARK_LOGIC_WRITER_VIEW_STORAGE_ERROR";
    private static final String MARK_LOGIC_WRITER_NO_DATA_FOLDERS_EXIST_ERROR = "MARK_LOGIC_WRITER_NO_DATA_FOLDERS_EXIST_ERROR";
    private static final String MARK_LOGIC_WRITER_BOTH_DATA_FOLDERS_EXIST_ERROR = "MARK_LOGIC_WRITER_BOTH_DATA_FOLDERS_EXIST_ERROR";
    private static final String MARK_LOGIC_WRITER_DOCUMENT_STORING_ERROR = "MARK_LOGIC_WRITER_DOCUMENT_STORING_ERROR";
    private static final String MARK_LOGIC_WRITER_DOCUMENT_REMOVAL_ERROR = "MARK_LOGIC_WRITER_DOCUMENT_REMOVAL_ERROR";
    private static final String MARK_LOGIC_WRITER_USER_STORAGE_ERROR = "MARK_LOGIC_WRITER_USER_STORAGE_ERROR";

    @Override
    public void store(GSUser user) throws GSException {

	try {

	    DatabaseFolder folder = getUsersFolder();

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
		    MARK_LOGIC_WRITER_USER_STORAGE_ERROR, //
		    e);
	}
    }

    @Override
    public void removeUser(String userIdentifier) throws GSException {

	try {

	    DatabaseFolder folder = getUsersFolder();

	    folder.remove(GSUser.toURI(userIdentifier));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to remove user {}", userIdentifier, e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_RESOURCE_REMOVAL_ERROR, //
		    e);
	}

    }

    @Override
    public void store(View view) throws GSException {

	try {

	    String id = view.getId();

	    DatabaseFolder folder = getViewFolder();

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
		    MARK_LOGIC_WRITER_VIEW_STORAGE_ERROR, //
		    e);
	}

    }

    @Override
    public void store(GSResource resource) throws GSException {

	GSSource source = resource.getSource();

	MarkLogicDatabase markLogicDB = getDatabase();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());

	    DatabaseFolder folder = findWritingFolder(worker);

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
		    MARK_LOGIC_RESOURCE_STORAGE_ERROR, //
		    e);
	}
    }

    @Override
    public void remove(GSResource resource) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();

	try {

	    markLogicDB.getWrapper().remove(resource.getPrivateId());

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to remove resource  {}", resource.getPrivateId(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_RESOURCE_REMOVAL_ERROR, //
		    e);
	}
    }

    @Override
    public void store(String identifier, Document document) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();

	try {

	    markLogicDB.getWrapper().store(identifier, document);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store document {}", identifier, e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_WRITER_DOCUMENT_STORING_ERROR, //
		    e);
	}

    }

    @Override
    public void removeDocument(String identifier) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();

	try {

	    markLogicDB.getWrapper().remove(identifier);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to remove document {}", identifier, e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_WRITER_DOCUMENT_REMOVAL_ERROR, //
		    e);
	}
    }

    @Override
    public void update(GSResource resource) throws GSException {
	GSSource source = resource.getSource();

	MarkLogicDatabase markLogicDB = getDatabase();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());

	    Document asDocument = resource.asDocument(true);

	    String key = resource.getPrivateId();

	    DatabaseFolder folder = findWritingFolder(worker);

	    folder.replace(key, asDocument);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to update resource {}", resource.getPrivateId(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_WRITER_RESOURCE_UPDATING_ERROR, //
		    e);
	}
    }

    @Override
    public void storeRDF(Node rdf) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();

	String dirURI = encodeSemanticURI(MarkLogicDatabase.SEMANTIC_FOLDER);
	dirURI = markLogicDB.normalizeName(dirURI);

	String query = "xquery version \"1.0-ml\"; \n";
	query += "import module namespace sem = \"http://marklogic.com/semantics\"  at \"/MarkLogic/semantics.xqy\"; \n";
	query += "declare namespace rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"; \n";
	query += "declare namespace owl=\"http://www.w3.org/2002/07/owl#\"; \n";
	query += "declare namespace dc = \"http://purl.org/dc/elements/1.1/\"; \n";
	query += "declare namespace dct = \"http://purl.org/dc/terms/\"; \n";
	query += "declare namespace skos = \"http://www.w3.org/2004/02/skos/core#\"; \n";
	query += "declare namespace rdfs = \"http://www.w3.org/2000/01/rdf-schema#\"; \n";
	query += "declare namespace d2k =\"http://eu.essi_lab.core/2018/06/d2k#\"; \n";

	query += "let $options := 'directory=" + dirURI + "' \n";

	query += "let $parseOptions := 'rdfxml' \n";

	query += "let $triples := sem:rdf-parse( \n";

	try {
	    String rdfString = XMLNodeReader.asString(rdf, true);
	    query += rdfString;

	    query += ",$parseOptions) \n";
	    query += "return sem:rdf-insert($triples, $options) \n";

	    markLogicDB.execXQuery(query);

	} catch (UnsupportedEncodingException | TransformerException | RequestException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store rdf node", e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_WRITER_RDF_STORING_ERROR, //
		    e);
	}
    }

    @Override
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
		    MARK_LOGIC_ONTOLOGY_OBJECT_STORAGE_ERROR, //
		    e);
	}

    }

    @Override
    public void removeView(String id) throws GSException {

	try {

	    DatabaseFolder folder = getViewFolder();

	    folder.remove(id);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to remove view {}", id, e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_RESOURCE_REMOVAL_ERROR, //
		    e);
	}

    }

    /**
     * 
     */
    @Override
    protected DatabaseFolder getProtectedFolder(String dirURI) throws RequestException {
	MarkLogicDatabase markLogicDB = getDatabase();
	DatabaseFolder ret = markLogicDB.getFolder(dirURI);
	if (ret == null) {
	    markLogicDB.addFolder(dirURI);
	    ret = markLogicDB.getFolder(dirURI);
	}
	return ret;
    }

    /**
     * @param uri
     * @return
     */
    private String encodeSemanticURI(String uri) {

	uri = uri.replace("/", "-");
	uri = uri.replace(":", "@");

	return uri;
    }

    /**
     * The folder tagged ad writing folder exists only during harvesting.
     * in this case no harvesting is in progress, so we need to find the
     * current data folder with max. 2 attempts
     * If both folder exist or none, there is some kind of issue and an exception is thrown
     * since the resource can not be stored/updated.
     * See GIP-288
     * 
     * @param worker
     * @return
     * @throws GSException
     * @throws RequestException
     */
    public DatabaseFolder findWritingFolder(SourceStorageWorker worker) throws GSException, RequestException {

	DatabaseFolder folder = worker.getWritingFolder(Optional.empty());

	if (folder == null) {

	    if (worker.existsData1Folder() && worker.existsData2Folder()) {

		throw GSException.createException(//
			getClass(), //
			"Both data-1 and data-2 folders exist", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			MARK_LOGIC_WRITER_BOTH_DATA_FOLDERS_EXIST_ERROR //
		);
	    }

	    if (worker.existsData1Folder()) {

		return worker.getData1Folder();
	    }

	    if (worker.existsData2Folder()) {

		return worker.getData2Folder();
	    }

	    throw GSException.createException(//
		    getClass(), //
		    "No data folder found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_WRITER_NO_DATA_FOLDERS_EXIST_ERROR //
	    );
	}

	return folder;
    }
}
