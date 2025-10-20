package eu.essi_lab.pdk.rsm.impl.json.sem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONObject;
import org.junit.Test;
 
import eu.essi_lab.model.ontology.GSKnowledgeOntology;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.RelationToParent;
import eu.essi_lab.model.ontology.d2k.D2KGSKnowledgeScheme;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSRootResource;

/**
 * @author ilsanto
 */
public class JSONSemanticResponseMapperTest {

    @Test
    public void test() {
	JSONSemanticResponseMapper mapper = new JSONSemanticResponseMapper();

	GSRootResource resource = new GSRootResource();

	GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

	String id = D2KGSKnowledgeScheme.getInstance().getNamespace();
	resource.setId(id);

	JSONObject json = mapper.mapChildConcept(description);

	System.out.println(json);

	assertEquals(id, json.getJSONObject("concept").getString("id"));

	// TODO
	// here I check the attributes exist even if null, this is to be discussed:
	// better exists with null or not exists?
	assertTrue(json.getJSONObject("concept").has("source"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("id"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("name"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("description"));

	assertTrue(json.getJSONObject("concept").has("linkedKnwoledgeResourceTypes"));
	assertTrue(json.getJSONObject("concept").has("geo"));
	assertTrue(json.getJSONObject("concept").has("image_url"));
	assertTrue(json.has("relationToParent"));

    }

    @Test
    public void test2() {
	JSONSemanticResponseMapper mapper = new JSONSemanticResponseMapper();

	GSRootResource resource = new GSRootResource();

	GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

	String id = D2KGSKnowledgeScheme.getInstance().getNamespace() + "-root";
	resource.setId(id);

	String desc = "desc";
	SimpleValueFactory factory = SimpleValueFactory.getInstance();
	description.add(D2KGSPredicate.DEFINITION, factory.createLiteral(desc));

	GSKnowledgeOntology source = new GSKnowledgeOntology();

	String ontid = D2KGSKnowledgeScheme.getInstance().getNamespace();
	source.setId(ontid);

	String ontname = "ontname";

	source.setName(ontname);

	String ontdesc = "ontdesc";
	source.setDescription(ontdesc);

	resource.setSource(source);

	JSONObject json = mapper.mapChildConcept(description);

	System.out.println(json);

	assertEquals(id, json.getJSONObject("concept").getString("id"));

	assertEquals(desc, json.getJSONObject("concept").getJSONArray("description").getJSONObject(0).getString("label"));

	assertEquals(ontid, json.getJSONObject("concept").getJSONObject("source").getString("id"));
	assertEquals(ontname, json.getJSONObject("concept").getJSONObject("source").getString("name"));
	assertEquals(ontdesc, json.getJSONObject("concept").getJSONObject("source").getString("description"));

    }

    @Test
    public void test3() {
	JSONSemanticResponseMapper mapper = new JSONSemanticResponseMapper();

	GSRootResource resource = new GSRootResource();

	GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

	String id = D2KGSKnowledgeScheme.getInstance().getNamespace() + "-root";
	resource.setId(id);

	RelationToParent rtp = new RelationToParent();

	String rtpname = "rtpname";
	rtp.setRelationName(rtpname);

	D2KGSPredicate r = D2KGSPredicate.ADDRESSES;
	rtp.setRelation(r.getGSPredicate());

	description.setRelationToParent(rtp);

	GSKnowledgeOntology source = new GSKnowledgeOntology();

	String ontid = D2KGSKnowledgeScheme.getInstance().getNamespace();
	source.setId(ontid);

	String ontname = "ontname";

	source.setName(ontname);

	String ontdesc = "ontdesc";
	source.setDescription(ontdesc);

	rtp.setOntology(source);

	JSONObject json = mapper.mapChildConcept(description);

	System.out.println(json);

	assertEquals(id, json.getJSONObject("concept").getString("id"));

	// TODO
	// here I check the attributes exist even if null, this is to be discussed:
	// better exists with null or not exists?
	assertTrue(json.getJSONObject("concept").has("source"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("id"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("name"));
	assertTrue(json.getJSONObject("concept").getJSONObject("source").has("description"));

	assertTrue(json.has("relationToParent"));

	assertEquals(ontid, json.getJSONObject("relationToParent").getJSONObject("source").getString("id"));
	assertEquals(ontname, json.getJSONObject("relationToParent").getJSONObject("source").getString("name"));
	assertEquals(ontdesc, json.getJSONObject("relationToParent").getJSONObject("source").getString("description"));
	assertEquals(r.getGSPredicate().stringValue(), json.getJSONObject("relationToParent").getString("id"));
	assertEquals(rtpname, json.getJSONObject("relationToParent").getString("name"));

	assertTrue(json.getJSONObject("concept").has("linkedKnwoledgeResourceTypes"));
	assertTrue(json.getJSONObject("concept").has("geo"));
	assertTrue(json.getJSONObject("concept").has("image_url"));

    }

}