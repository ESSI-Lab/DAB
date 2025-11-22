package eu.essi_lab.model.ontology.d2k.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeOntology;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSBusinessProcessResource;
import eu.essi_lab.model.ontology.d2k.resources.GSIndicatorResource;
import eu.essi_lab.model.ontology.d2k.resources.GSPolicyGoalResource;
import eu.essi_lab.model.ontology.d2k.resources.GSRootResource;

/**
 * @author ilsanto
 */
public class J2RDFSerializerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test9() throws GSException, XPathExpressionException {
	GSRootResource root = new GSRootResource();

	root.setId("id");

	GSKnowledgeResourceDescription rootDescription = new GSKnowledgeResourceDescription(root);

	SimpleValueFactory facory = SimpleValueFactory.getInstance();
	rootDescription.add(D2KGSPredicate.LABEL, facory.createLiteral("prova"));

	Node node = new J2RDFSerializer().toNode(rootDescription);

	assertNotNull(node);

	XMLNodeReader reader = new XMLNodeReader(node);

	assertEquals("id", reader.evaluateNode("//*[local-name()='Description']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals("prova", reader.evaluateNode("//*[local-name()='label']").getTextContent());
    }

    @Test
    public void test10() throws GSException, XPathExpressionException {
	GSIndicatorResource indicator = new GSIndicatorResource();

	indicator.setId("rootid");

	GSBusinessProcessResource model = new GSBusinessProcessResource();

	model.setId("id");

	GSKnowledgeOntology source = new GSKnowledgeOntology();
	source.setId("http://sid#id");
	model.setSource(source);
	GSKnowledgeResourceDescription rootDescription = new GSKnowledgeResourceDescription(model);

	SimpleValueFactory facory = SimpleValueFactory.getInstance();
	rootDescription.add(D2KGSPredicate.LABEL, facory.createLiteral("prova"));

	rootDescription.add(D2KGSPredicate.GENERATES_OUTPUT, indicator);

	Node node = new J2RDFSerializer().toNode(rootDescription);

	assertNotNull(node);

	XMLNodeReader reader = new XMLNodeReader(node);

	assertEquals("id", reader.evaluateNode("//*[local-name()='Description']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals("prova", reader.evaluateNode("//*[local-name()='label']").getTextContent());

	assertEquals(indicator.stringValue(), reader.evaluateNode("//*[local-name()='generates_output']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(indicator.stringValue(), reader.evaluateNodes("//*[local-name()='Description']")[1].getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals(model.getKnowledgeClass(), reader.evaluateNode("//*[local-name()='type']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(model.getSource().getId(), reader.evaluateNode("//*[local-name()='isDefinedBy']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());
    }

    @Test
    public void test11() throws GSException, XPathExpressionException {
	GSRootResource root = new GSRootResource();

	root.setId("rootid");

	GSPolicyGoalResource policy = new GSPolicyGoalResource();

	policy.setId("id");

	GSKnowledgeOntology source = new GSKnowledgeOntology();
	source.setId("http://sid#id");
	policy.setSource(source);
	GSKnowledgeResourceDescription policyDescription = new GSKnowledgeResourceDescription(policy);

	SimpleValueFactory facory = SimpleValueFactory.getInstance();
	policyDescription.add(D2KGSPredicate.LABEL, facory.createLiteral("prova"));

	policyDescription.add(D2KGSPredicate.ADDRESSES, root);

	Node node = new J2RDFSerializer().toNode(policyDescription);

	assertNotNull(node);

	XMLNodeReader reader = new XMLNodeReader(node);

	assertEquals("id", reader.evaluateNode("//*[local-name()='Description']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals("prova", reader.evaluateNode("//*[local-name()='label']").getTextContent());

	assertEquals("id", reader.evaluateNode("//*[local-name()='addressed_by']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(root.stringValue(), reader.evaluateNodes("//*[local-name()='Description']")[1].getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals(policy.getKnowledgeClass(), reader.evaluateNode("//*[local-name()='type']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(policy.getSource().getId(), reader.evaluateNode("//*[local-name()='isDefinedBy']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());
    }

    @Test
    public void test12() throws GSException, XPathExpressionException {

	GSBusinessProcessResource model = new GSBusinessProcessResource();

	model.setId("modelid");

	GSIndicatorResource output = new GSIndicatorResource();

	output.setId("outputid");

	GSKnowledgeOntology source = new GSKnowledgeOntology();
	source.setId("http://sid#id");
	output.setSource(source);

	GSKnowledgeResourceDescription outputDescription = new GSKnowledgeResourceDescription(output);

	SimpleValueFactory facory = SimpleValueFactory.getInstance();
	outputDescription.add(D2KGSPredicate.LABEL, facory.createLiteral("prova"));

	outputDescription.add(D2KGSPredicate.OUTPUT_OF_SCIENTIFIC_BP, model);

	Node node = new J2RDFSerializer().toNode(outputDescription);

	assertNotNull(node);

	XMLNodeReader reader = new XMLNodeReader(node);

	assertEquals(output.stringValue(), reader.evaluateNode("//*[local-name()='Description']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());

	assertEquals("prova", reader.evaluateNode("//*[local-name()='label']").getTextContent());

	assertEquals(model.stringValue(), reader.evaluateNode("//*[local-name()='output_of']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(model.stringValue(), reader.evaluateNode("//*[local-name()='output_of_scientific_bp']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	//	assertEquals(model.stringValue(), reader.evaluateNode("//*[local-name()='isParentOf']").getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	//	assertEquals(output.stringValue(), reader.evaluateNode("//*[local-name()='isChildOf']").getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(output.stringValue(), reader.evaluateNode("//*[local-name()='generates_output']").getAttributes()
		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	//
	//	assertEquals("id", reader.evaluateNode("//*[local-name()='addressed_by']").getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());
	//
	//	assertEquals(root.stringValue(), reader.evaluateNodes("//*[local-name()='Description']")[1].getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").getTextContent());
	//
	//	assertEquals(policy.getKnowledgeClass(), reader.evaluateNode("//*[local-name()='type']").getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());
	//
	//	assertEquals(policy.getSource().getId(), reader.evaluateNode("//*[local-name()='isDefinedBy']").getAttributes()
	//		.getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getTextContent());

	assertEquals(1, reader.evaluateNodes("//*[local-name()='output_of_scientific_bp']").length);

	assertEquals(0, reader.evaluateNodes("//*[local-name()='output_of_knowledge_bp']").length);

	assertEquals(0, reader.evaluateNodes("//*[local-name()='addresses']").length);

	assertEquals(0, reader.evaluateNodes("//*[local-name()='addressed_by']").length);
    }
}