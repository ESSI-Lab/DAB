/**
 * 
 */
package eu.essi_lab.lib.skoss.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;

/**
 * @author Fabrizio
 */
public class SKOSConceptTest {

    @Test
    public void test1() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref");

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getAlt().isEmpty());
	Assert.assertTrue(concept.getExpanded().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());

	Assert.assertTrue(concept.getLevel().isEmpty());

	SKOSConcept concept2 = SKOSConcept.of("concept", "pref");

	Assert.assertEquals(concept, concept2);
    }

    @Test
    public void test2() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", Set.of("a", "x", "y"), Set.of("b", "k"), Set.of("c"));

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getExpanded().contains("a"));
	Assert.assertTrue(concept.getExpanded().contains("x"));
	Assert.assertTrue(concept.getExpanded().contains("y"));

	Assert.assertTrue(concept.getExpandedFrom().contains("b"));
	Assert.assertTrue(concept.getExpandedFrom().contains("k"));

	Assert.assertEquals("c", concept.getAlt().toArray()[0]);

	Assert.assertTrue(concept.getLevel().isEmpty());

	SKOSConcept concept2 = SKOSConcept.of("concept", "pref", Set.of("a", "x", "y"), Set.of("b", "k"), Set.of("c"));

	Assert.assertEquals(concept, concept2);
    }

    @Test
    public void test3() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", "a", "b", "c");

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertEquals("a", concept.getExpanded().toArray()[0]);
	Assert.assertEquals("b", concept.getExpandedFrom().toArray()[0]);
	Assert.assertEquals("c", concept.getAlt().toArray()[0]);

	Assert.assertTrue(concept.getLevel().isEmpty());
    }

    @Test
    public void test4() {

	SKOSConcept concept = SKOSConcept.of("concept");

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertFalse("pref", concept.getPref().isPresent());

	Assert.assertTrue(concept.getAlt().isEmpty());
	Assert.assertTrue(concept.getExpanded().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());

	Assert.assertTrue(concept.getLevel().isEmpty());

    }

    @Test
    public void test5() {

	SKOSConcept concept = SKOSConcept.of(Set.of("a", "x", "y"));

	Assert.assertNull("concept", concept.getConceptURI());
	Assert.assertFalse("pref", concept.getPref().isPresent());

	Assert.assertFalse(concept.getExpanded().isEmpty());

	Assert.assertTrue(concept.getAlt().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());

	Assert.assertTrue(concept.getLevel().isEmpty());

    }

    @Test
    public void test6() {

	SKOSConcept concept = SKOSConcept.of(Set.of("a", "x", "y"));

	concept.setLevel(ExpansionLevel.HIGH);

	Assert.assertNull("concept", concept.getConceptURI());
	Assert.assertFalse("pref", concept.getPref().isPresent());

	Assert.assertFalse(concept.getExpanded().isEmpty());

	Assert.assertTrue(concept.getAlt().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());

	Assert.assertEquals(ExpansionLevel.HIGH, concept.getLevel().get());
    }

    @Test
    public void test7() {

	SKOSConcept concept = SKOSConcept.of("concept");

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertFalse(concept.getPref().isPresent());
	Assert.assertTrue(concept.getAlt().isEmpty());

	Assert.assertTrue(concept.getExpanded().isEmpty());
	Assert.assertTrue(concept.getExpandedFrom().isEmpty());
	Assert.assertTrue(concept.getLevel().isEmpty());

	concept.setAlt(Set.of("alt"));
	concept.setPref("pref");

	Assert.assertEquals("alt", concept.getAlt().iterator().next());
	Assert.assertEquals("pref", concept.getPref().get());
    }

    @Test
    public void test8() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", Set.of("a", "x", "y"), Set.of("b", "k"), Set.of("c"), ExpansionLevel.HIGH);

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getExpanded().contains("a"));
	Assert.assertTrue(concept.getExpanded().contains("x"));
	Assert.assertTrue(concept.getExpanded().contains("y"));

	Assert.assertTrue(concept.getExpandedFrom().contains("b"));
	Assert.assertTrue(concept.getExpandedFrom().contains("k"));

	Assert.assertEquals("c", concept.getAlt().toArray()[0]);

	Assert.assertEquals(ExpansionLevel.HIGH, concept.getLevel().get());

	SKOSConcept concept2 = SKOSConcept.of("concept", "pref", Set.of("a", "x", "y"), Set.of("b", "k"), Set.of("c"), ExpansionLevel.HIGH);

	Assert.assertEquals(concept, concept2);
    }
    
    @Test
    public void test9() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", "y", "k", "c", ExpansionLevel.HIGH);

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getExpanded().contains("y"));

	Assert.assertTrue(concept.getExpandedFrom().contains("k"));

	Assert.assertEquals("c", concept.getAlt().toArray()[0]);

	Assert.assertEquals(ExpansionLevel.HIGH, concept.getLevel().get());

	SKOSConcept concept2 = SKOSConcept.of("concept", "pref", "y", "k", "c", ExpansionLevel.HIGH);

	Assert.assertEquals(concept, concept2);
    }
    
    @Test
    public void test10() {

	SKOSConcept concept = SKOSConcept.of("concept", "pref", "y", "k", "c");

	Assert.assertEquals("concept", concept.getConceptURI());
	Assert.assertEquals("pref", concept.getPref().get());

	Assert.assertTrue(concept.getExpanded().contains("y"));

	Assert.assertTrue(concept.getExpandedFrom().contains("k"));

	Assert.assertEquals("c", concept.getAlt().toArray()[0]);

	Assert.assertTrue(concept.getLevel().isEmpty());

	SKOSConcept concept2 = SKOSConcept.of("concept", "pref", "y", "k", "c");

	Assert.assertEquals(concept, concept2);
    }
    
     
}
