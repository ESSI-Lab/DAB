/**
 * 
 */
package eu.essi_lab.lib.skoss.response.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSConcept;

/**
 * @author Fabrizio
 */
public class SKOSConceptTest {

    @Test
    public void test1() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref");

	Assert.assertEquals("concept", concept.getConcept());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getAlt().isEmpty());
	Assert.assertTrue(concept.getExpanded().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());
    }

    @Test
    public void test2() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", Set.of("a", "x", "y"), Set.of("b", "k"), Set.of("c"));

	Assert.assertEquals("concept", concept.getConcept());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getExpanded().contains("a"));
	Assert.assertTrue(concept.getExpanded().contains("x"));
	Assert.assertTrue(concept.getExpanded().contains("y"));

	Assert.assertTrue(concept.getExpandedFrom().contains("b"));
	Assert.assertTrue(concept.getExpandedFrom().contains("k"));

	Assert.assertEquals("c", concept.getAlt().toArray()[0]);
    }

    @Test
    public void test3() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", "a", "b", "c");

	Assert.assertEquals("concept", concept.getConcept());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertEquals("a", concept.getExpanded().toArray()[0]);
	Assert.assertEquals("b", concept.getExpandedFrom().toArray()[0]);
	Assert.assertEquals("c", concept.getAlt().toArray()[0]);
    }
}
