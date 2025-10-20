package eu.essi_lab.model.ontology.d2k;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSRootResource;

/**
 * @author ilsanto
 */
public class GSKnowledgeResourceDescriptionTest {

    @Test
    public void test() {

	GSRootResource resource = new GSRootResource();

	GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

	SimpleValueFactory factory = SimpleValueFactory.getInstance();
	description.add(D2KGSPredicate.LABEL, factory.createLiteral("1"));
	description.add(D2KGSPredicate.LABEL, factory.createLiteral("2"));

	assertEquals(1, description.getPredicates().size());

	assertEquals(2, description.getValues(D2KGSPredicate.LABEL).size());

    }
}