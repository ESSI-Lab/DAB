package eu.essi_lab.model.ontology.d2k.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.OntologyURIs;
import eu.essi_lab.model.ontology.d2k.D2KGSOntologyLoader;

/**
 * @author ilsanto
 */
public class GSD2KOntologyLoaderTest {

    @Test
    public void test() throws GSException {

	D2KGSOntologyLoader loader = new D2KGSOntologyLoader();

	URI rootURI = loader.getRootOWLClass();

	assertNotNull(rootURI);

	assertEquals(OntologyURIs.ESSI_D2K_NAMESPACE + "Root", rootURI.toString());

    }

    @Test
    public void test2() throws GSException {

	D2KGSOntologyLoader loader = Mockito.spy(new D2KGSOntologyLoader());

	SimpleValueFactory factory = SimpleValueFactory.getInstance();

	List<String> equivalents = loader.getLinkedPredicates(
		factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of_knowledge_bp"),
		factory.createIRI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"));

	assertFalse(equivalents.isEmpty());

	assertTrue(equivalents.contains(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of"));

	equivalents = loader.getLinkedPredicates(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "isChildOf"),
		factory.createIRI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"));

	assertTrue(equivalents.isEmpty());

    }

    @Test
    public void test3() throws GSException {

	D2KGSOntologyLoader loader = new D2KGSOntologyLoader();

	Set<Triple> inferred = loader.inferredProperties();

	inferred.forEach(System.out::println);

	Set<Statement> expectedStatements = new HashSet<>();

	SimpleValueFactory factory = SimpleValueFactory.getInstance();

	expectedStatements
		.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of_knowledge_bp"),
			factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
			factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "generates_output")));

	expectedStatements
		.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "generates_output"),
			factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
			factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of_knowledge_bp")));

	expectedStatements.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "isParentOf"),
		factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
		factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "isChildOf")));

	expectedStatements
		.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "generates_output"),
			factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
			factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of_scientific_bp")));

	expectedStatements.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of"),
		factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
		factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "generates_output")));

	expectedStatements
		.add(factory.createStatement(factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "output_of_scientific_bp"),
			factory.createIRI(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME),
			factory.createIRI(OntologyURIs.ESSI_D2K_NAMESPACE + "generates_output")));

	inferred.forEach(triple -> {

	    compare(triple, expectedStatements);

	});

	assertTrue("Fond " + expectedStatements.size() + " unexpected elements", expectedStatements.isEmpty());

	assertEquals(6, inferred.size());

    }

    private void compare(Triple triple, Set<Statement> expectedStatements) {

	Iterator<Statement> it = expectedStatements.iterator();

	while (it.hasNext()) {

	    Statement statement = it.next();

	    if (!statement.getSubject().stringValue().equals(triple.getSubject().toString()))
		continue;

	    if (!statement.getPredicate().stringValue().equals(triple.getPredicate().toString()))
		continue;

	    if (!statement.getObject().stringValue().equals(triple.getObject().toString()))
		continue;

	    expectedStatements.remove(statement);

	    return;

	}

    }
}