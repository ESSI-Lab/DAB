package eu.essi_lab.model.ontology.d2k.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSPolicyGoalResource;
import eu.essi_lab.model.ontology.d2k.resources.GSRootResource;
import org.junit.rules.ExpectedException;

/**
 * @author ilsanto
 */
public class J2RDFDeserializerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() throws GSException {

	InputStream is = getClass().getClassLoader().getResourceAsStream("serialization/test.xml");

	GSKnowledgeResourceDescription resourceDescription = new J2RDFDeserializer().deserialize(is).get(0);

	assertEquals("http://eu.essi_lab.core/test/unontology", resourceDescription.getResource().stringValue());

	assertTrue(GSRootResource.class.isAssignableFrom(resourceDescription.getResource().getClass()));

	assertEquals("Sustainable Development - Knowledge Platform Description", resourceDescription.getValues(D2KGSPredicate.DEFINITION)
		.get(0).stringValue());

	assertEquals("Sustainable Development - Knowledge Platform", resourceDescription.getValues(D2KGSPredicate.LABEL).get(0)
		.stringValue());

    }

    @Test
    public void test2() throws GSException {

	InputStream is = getClass().getClassLoader().getResourceAsStream("serialization/test3.xml");

	List<GSKnowledgeResourceDescription> descriptions = new J2RDFDeserializer().deserialize(is);

	descriptions = descriptions.//
		stream().//
		sorted((o1, o2) -> o1.getResource().stringValue().compareTo(o2.getResource().stringValue())).//
		collect(Collectors.toList());

	GSKnowledgeResourceDescription resourceDescription = descriptions.get(0);

	assertEquals("http://eu.essi_lab.core/test/goal1", resourceDescription.getResource().stringValue());

	assertTrue(GSPolicyGoalResource.class.isAssignableFrom(resourceDescription.getResource().getClass()));

	assertEquals("SDG 1 Description", resourceDescription.getValues(D2KGSPredicate.DEFINITION).get(0).stringValue());

	assertEquals("SDG 1", resourceDescription.getValues(D2KGSPredicate.LABEL).get(0).stringValue());

	assertEquals("http://eu.essi_lab.core/test/unontology", resourceDescription.getValues(D2KGSPredicate.IS_DEFINED_BY).get(0)
		.stringValue());

	//	assertEquals("http://eu.essi_lab.essi.core/test/unontology", resourceDescription.getValues(GS_PREDICATES.IS_CHILD_OF).get(0)
	//		.stringValue());

	resourceDescription = descriptions.get(1);

	assertEquals("http://eu.essi_lab.core/test/unontology", resourceDescription.getResource().stringValue());

	assertTrue(GSRootResource.class.isAssignableFrom(resourceDescription.getResource().getClass()));

	assertEquals("Sustainable Development - Knowledge Platform Description", resourceDescription.getValues(D2KGSPredicate.DEFINITION)
		.get(0).stringValue());

	assertEquals("Sustainable Development - Knowledge Platform", resourceDescription.getValues(D2KGSPredicate.LABEL).get(0)
		.stringValue());

    }

    @Test
    public void test4() throws GSException {

	expectedException.expect(GSException.class);

	InputStream is = getClass().getClassLoader().getResourceAsStream("serialization/test4.xml");
	new J2RDFDeserializer().deserialize(is);
    }

}