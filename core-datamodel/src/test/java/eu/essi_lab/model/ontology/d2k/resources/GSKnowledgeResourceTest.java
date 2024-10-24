package eu.essi_lab.model.ontology.d2k.resources;

import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.junit.Test;

/**
 * @author ilsanto
 */
public class GSKnowledgeResourceTest {


    @Test
    public void test() {

	assertTrue(ServiceLoader.load(GSKnowledgeResource.class).iterator().hasNext());

    }
}