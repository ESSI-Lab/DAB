package eu.essi_lab.api.database.marklogic;

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

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public class MarkLogicWriter extends DatabaseWriter {

    private static final String MARK_LOGIC_RESOURCE_REMOVAL_ERROR = "MARK_LOGIC_RESOURCE_REMOVAL_ERROR";
    private static final String MARK_LOGIC_ONTOLOGY_OBJECT_STORAGE_ERROR = "MARK_LOGIC_ONTOLOGY_OBJECT_STORAGE_ERROR";

    private static final String MARK_LOGIC_WRITER_RDF_STORING_ERROR = "MARK_LOGIC_WRITER_RDF_STORING_ERROR";
    private static final String MARK_LOGIC_WRITER_DOCUMENT_STORING_ERROR = "MARK_LOGIC_WRITER_DOCUMENT_STORING_ERROR";
    private static final String MARK_LOGIC_WRITER_DOCUMENT_REMOVAL_ERROR = "MARK_LOGIC_WRITER_DOCUMENT_REMOVAL_ERROR";

    private MarkLogicDatabase markLogicDB;
    private StorageInfo dbUri;

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
    public void remove(String propertyName, String propertyValue) throws GSException {

	String xQuery = "xquery version \"1.0-ml\";\n"
		+ "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n"
		+ getDatabase().xdmpQueryTrace() + ",\n" +

		"for $x in \n"
		+ "cts:search(doc()[gs:Dataset or gs:DatasetCollection or gs:Document or gs:Ontology or gs:Service or gs:Observation],\n"
		+ "cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','" + propertyName + "'),'=','"
		+ propertyValue + "',(\"score-function=linear\"),0.0),\n" + "(\"unfiltered\",\"score-simple\"),0)\n" +

		"return xdmp:document-delete(fn:document-uri($x))\n" +

		",xdmp:query-trace(false());";

	try {
	    getDatabase().execXQuery(xQuery);

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_REOVE_BY_RECOVERY_REMOVAL_TOKEN_ERROR", ex);
	}
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

    @Override
    public void setDatabase(Database dataBase) {

	this.markLogicDB = (MarkLogicDatabase) dataBase;
    }

    @Override
    public MarkLogicDatabase getDatabase() {

	return this.markLogicDB;
    }

    @Override
    public boolean supports(StorageInfo dbUri) {

	if (MarkLogicDatabase.isSupported(dbUri)) {

	    this.dbUri = dbUri;
	    return true;
	}

	return false;
    }
}
