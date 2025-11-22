package eu.essi_lab.model.ontology.d2k;

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
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ContextStatementCollector;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.OntologyURIs;
import eu.essi_lab.model.ontology.d2k.resources.GSRootResource;
import eu.essi_lab.model.ontology.d2k.serialization.J2RDFSerializer;

/**
 * @author ilsanto
 */
public class D2KGSOntologyLoader {

    private static final String ONTOLOGY_FILE = "ontology/essid2k.owl";
    private static final String ONTOLOGY_PARSE_ERROR = "ONTOLOGY_PARSE_ERROR";
    private final Set<Triple> inferred;
    private InfModel infModel;

    public D2KGSOntologyLoader() throws GSException {

	// GSLoggerFactory.getLogger(D2KGSOntologyLoader.class).trace("Loading statements");

	InputStream is = getOntologyStream();
	RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

	SimpleValueFactory factory = SimpleValueFactory.getInstance();
	ContextStatementCollector handler = new ContextStatementCollector(factory);

	rdfParser.setRDFHandler(handler);

	try {

	    rdfParser.parse(is, "");

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(D2KGSOntologyLoader.class).error(e);

	    throw GSException.createException(J2RDFSerializer.class, e.getMessage(), null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_FATAL, ONTOLOGY_PARSE_ERROR, e);

	}

	inferred = getInferProperties();

	// GSLoggerFactory.getLogger(D2KGSOntologyLoader.class).trace("Loading statements completed");
    }

    public URI getRootOWLClass() {

	return URI.create(new GSRootResource().getKnowledgeClass());
    }

    /**
     * Retrieves all predicates (object properties) from ontology file, which are related to sourcePredicate by related.
     * I.e. suppose in
     * onotology file you have
     * <owl:ObjectProperty rdf:about="http://eu.essi_lab.core/2018/06/d2k#input_of_knowledge_bp">
     * <rdfs:subPropertyOf rdf:resource="http://eu.essi_lab.core/2018/06/d2k#input_of"/> </owl:ObjectProperty> and you
     * request
     * getLinkedPredicates(http://eu.essi_lab.core/2018/06/d2k#input_of_knowledge_bp, rdfs:subPropertyOf) then you get
     * http://eu.essi_lab.core/2018/06/d2k#input_of
     *
     * @param sourcePredicate
     * @param related
     * @return
     */
    public List<String> getLinkedPredicates(IRI sourcePredicate, IRI related) {

	Query query = QueryFactory
		.create("SELECT DISTINCT ?a WHERE { <" + sourcePredicate.stringValue() + "> <" + related.stringValue() + "> ?a }");

	List<String> matched = new ArrayList<>();

	try (QueryExecution qexec = QueryExecutionFactory.create(query, infModel)) {
	    ResultSet results = qexec.execSelect();
	    for (; results.hasNext();) {
		QuerySolution soln = results.nextSolution();
		RDFNode x = soln.get("a");

		matched.add(x.toString());
	    }
	}

	return matched;
    }

    public Set<Triple> inferredProperties() {
	return inferred;
    }

    /**
     * @return
     */
    public static InputStream getOntologyStream() {

	return D2KGSOntologyLoader.class.getClassLoader().getResourceAsStream(ONTOLOGY_FILE);
    }

    private Set<Triple> getInferProperties() {

	InputStream is = getOntologyStream();

	InputStreamReader reader = new InputStreamReader(is);

	OntModel model = ModelFactory.createOntologyModel();

	model.read(reader, null);

	List<Rule> rules1 = Rule.parseRules("[rule1: (?a " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME
		+ " ?b) -> (?b " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?a)]");

	List<Rule> rules3 = Rule.parseRules("[rule3: "//
		+ "(?a " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?b) " //
		+ "(?b " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c) "//
		+ "notEqual(?b ?c) " //
		+ "(?c " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?d) "//
		+ "-> "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?d)]");

	List<Rule> rules4 = Rule.parseRules("[rule4: "//
		+ "(?a " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_EQUIVALENT_OF_LOCALNAME + " ?b) " //
		+ "(?b " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c) "//
		+ "notEqual(?b ?c) " //
		+ "-> "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c)]");

	List<Rule> rules5 = Rule.parseRules("[rule5: "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?b) " //
		+ "notEqual(?a ?b) " //
		+ "(?b " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c) "//
		+ "notEqual(?b ?c) " //
		+ "-> "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c)]");

	List<Rule> rules6 = Rule.parseRules("[rule6: "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?b) " //
		+ "notEqual(?a ?b) " //
		+ "(?b " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?c) "//

		+ "-> "//
		+ "(?a " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?c)]");

	List<Rule> rules7 = Rule.parseRules("[rule7: "//
		+ "(?a " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?b) " //
		+ "(?b " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?c) "//
		+ "notEqual(?b ?c) " //
		+ "(?d " + OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF + " ?c) "//
		+ "-> "//
		+ "(?a " + OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME + " ?d)]");

	rules1.addAll(rules3);

	rules1.addAll(rules4);

	rules1.addAll(rules5);

	rules1.addAll(rules6);
	rules1.addAll(rules7);

	GenericRuleReasoner reasoner = new GenericRuleReasoner(rules1);

	reasoner.setDerivationLogging(true);

	infModel = ModelFactory.createInfModel(reasoner, model);

	List<Triple> list = infModel.getDeductionsModel().getGraph().find().toList();
	// PrintWriter out = new PrintWriter(System.out);
	// for (StmtIterator i = infModel.listStatements(); i.hasNext(); ) {
	// org.apache.jena.rdf.model.Statement st = i.nextStatement();
	//
	// for (Iterator id = infModel.getDerivation(st); id.hasNext(); ) {
	//
	// Derivation deriv = (Derivation) id.next();
	//
	// deriv.printTrace(out, true);
	//
	// }
	// }
	//
	// out.flush();

	return prune(list);
    }

    private Set<Triple> prune(List<Triple> list) {

	Set<Triple> set = new HashSet<>();

	list.stream().filter(triple -> !toBeDiscarded(triple, set)).forEach(triple -> set.add(triple));

	return set;

    }

    private boolean toBeDiscarded(Triple triple, Set<Triple> set) {

	// not reflexive
	if (triple.getSubject().toString().equals(triple.getObject().toString()))
	    return true;

	if (set == null)
	    return false;

	Iterator<Triple> it = set.iterator();

	while (it.hasNext()) {

	    Triple statement = it.next();

	    // not already present
	    if (!statement.getSubject().toString().equals(triple.getSubject().toString()))
		continue;

	    if (!statement.getPredicate().toString().equals(triple.getPredicate().toString()))
		continue;

	    if (!statement.getObject().toString().equals(triple.getObject().toString()))
		continue;

	    return true;

	}

	return false;

    }

}
