package eu.essi_lab.model.ontology.d2k.serialization;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;

/**
 * @author ilsanto
 */
public class J2RDFDeserializer {

    private static final String RDF_PARSE_ERROR = "RDF_PARSE_ERROR";

    public List<GSKnowledgeResourceDescription> deserialize(InputStream serialized) throws GSException {

	RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

	GSStatementHandler handler = new GSStatementHandler();

	rdfParser.setRDFHandler(handler);

	try {

	    rdfParser.parse(serialized, "");

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(J2RDFDeserializer.class).error("IO Exception parsing stream", e);

	    throw GSException.createException(J2RDFSerializer.class, "Can't parse rdf input stream", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, RDF_PARSE_ERROR, e);

	}

	return handler.getParsedObjects();

    }
}
