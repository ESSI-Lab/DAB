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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.predicates.GSPredicateIsDefinedBy;
import eu.essi_lab.model.ontology.d2k.predicates.GSPredicateType;
import eu.essi_lab.model.ontology.d2k.resources.GSKnowledgeResource;

/**
 * This implements the serialization of a {@link GSKnowledgeResourceDescription} to RDF/XML.
 *
 * @author ilsanto
 */
public class J2RDFSerializer {

    private static final String RDFXML_NODE_SERIALIZATION_ERROR = "RDFXML_NODE_SERIALIZATION_ERROR";
    private static final String NO_RESOURCE_FOUND = "NO_RESOURCE_FOUND";

    /**
     * Serializes the provided object to RDF/XML, generating a document which contains:
     * <ul>
     * <li>All the statements present in the provided {@link GSKnowledgeResourceDescription} object</li>
     * <li>For each statement (resource, predicate, value) in the provided {@link GSKnowledgeResourceDescription}
     * object:
     * <ul>
     * <li>Equivalent predicate statements, i.e.: (resource, equivalentPred, value) where equivalentPred is a predicate
     * defined
     * in essid2k and is declared as equivalent to predicate
     * </li>
     * <li>SuperProperty predicate statements, i.e.: (resource, superPred, value) where superPred is a predicate defined
     * in essid2k and is declared as SuperProperty of predicate (technically all superPred such that predicate
     * rdfs:subPropertyOf
     * superPred)
     * </li>
     * <li>InverseProperty predicate statements, i.e.: (value, invPred, resource) where invPred is a predicate defined
     * in essid2k and is declared as inverseOf of predicate (note that the statement actually switches subject and
     * object)
     * </li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param o
     * @return
     * @throws GSException
     */
    public Node toNode(GSKnowledgeResourceDescription o) throws GSException {

	ByteArrayOutputStream out = new ByteArrayOutputStream();

	try {

	    toOutputStream(o, out);

	    return XMLFactories.newDocumentBuilderFactory().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()))
		    .getDocumentElement();

	} catch (SAXException | IOException | ParserConfigurationException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error serializing object to RDF/XML", e);

	    throw GSException.createException(getClass(), "Can't serialize object to RDF/XML", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, RDFXML_NODE_SERIALIZATION_ERROR, e);

	}

    }

    private void toOutputStream(GSKnowledgeResourceDescription resourceDescription, ByteArrayOutputStream os) throws GSException {

	ValueFactory factory = SimpleValueFactory.getInstance();

	Optional<GSKnowledgeResource> r = Optional.ofNullable(resourceDescription.getResource());

	GSKnowledgeResource resource = r.orElseThrow(() -> GSException.createException(J2RDFSerializer.class, "No Resource found", null,
		ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, NO_RESOURCE_FOUND));

	RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML, os);
	writer.startRDF();

	if (resource.getSource() != null && resource.getSource().getId() != null && !"".equals(resource.getSource().getId()))
	    writer.handleStatement(
		    factory.createStatement(resource, new GSPredicateIsDefinedBy(), factory.createIRI(resource.getSource().getId())));

	if (resource.getKnowledgeClass() != null && !"".equals(resource.getKnowledgeClass()))
	    writer.handleStatement(
		    factory.createStatement(resource, new GSPredicateType(), factory.createIRI(resource.getKnowledgeClass())));

	resourceDescription.getPredicates().stream().forEach(predicate -> {

	    List<Statement> statements = new ArrayList<>();

	    resourceDescription.getValues(predicate).forEach(value -> {

		addStatementIfNotPresent(statements, factory.createStatement(resource, predicate, value));

		predicate.equivalentOf().forEach(iri ->

		addStatementIfNotPresent(statements, factory.createStatement(resource, iri, value))

		);

		predicate.subPropertyOf().forEach(iri ->

		addStatementIfNotPresent(statements, factory.createStatement(resource, iri, value))

		);

		predicate.inverseOf().forEach(iri -> {

		    if (Resource.class.isAssignableFrom(value.getClass()))
			addStatementIfNotPresent(statements, factory.createStatement((Resource) value, iri, resource));

		    else
			GSLoggerFactory.getLogger(J2RDFSerializer.class).warn(
				"Found {} {} {} where {} has inverse predicate {} but " + "resource and value can't be switched",
				resource.stringValue(), predicate.toString(), value.stringValue(), predicate.toString(), iri.toString());

		});

	    });

	    statements.forEach(writer::handleStatement);

	});

	writer.endRDF();

	print(os);

    }

    private void addStatementIfNotPresent(List<Statement> statements, Statement statement) {

	if (isInList(statement, statements))
	    return;

	statements.add(statement);

    }

    private boolean isInList(Statement statement, List<Statement> statements) {

	for (Statement st : statements) {

	    if (!statement.getSubject().toString().equals(st.getSubject().toString()))
		continue;

	    if (!statement.getPredicate().toString().equals(st.getPredicate().toString()))
		continue;

	    if (!statement.getObject().toString().equals(st.getObject().toString()))
		continue;

	    return true;

	}

	return false;
    }

    private void print(ByteArrayOutputStream os) {

	if (GSLoggerFactory.getLogger(J2RDFSerializer.class).isTraceEnabled())
	    try {

		GSLoggerFactory.getLogger(J2RDFSerializer.class).trace("Generated RDF " + System.getProperty("line.separator") + "{}",
			os.toString("UTF-8"));

	    } catch (UnsupportedEncodingException e) {
		GSLoggerFactory.getLogger(J2RDFSerializer.class).warn("Error converting output stream to string", e);
	    }
    }

}
