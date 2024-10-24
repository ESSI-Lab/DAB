package eu.essi_lab.lib.net.utils.whos.test.hiscentral.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;

public class HydroOntologyExternalTestIT {

    HydroOntology ontology = new HISCentralOntology();

    @Test
    public void testFindConceptsItalian() {

	// search of a specific concept in the ontology
	List<SKOSConcept> concepts = ontology.findConcepts("temperatura", false, true);
	assertEquals(1, concepts.size());
	assertEquals("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40", concepts.get(0).getURI());

	// search of a specific concept in the ontology, with spaces in the search term
	concepts = ontology.findConcepts("temperatura del mare", false, true);
	assertEquals(1, concepts.size());
	assertEquals("http://ontology.his-central.geodab.eu/hydro-ontology/concept/51b", concepts.get(0).getURI());

	// search of a specific concept in the ontology, by URI
	concepts = ontology.findConcepts("http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224", false, true);
	assertEquals(1, concepts.size());
	assertEquals("http://ontology.his-central.geodab.eu/hydro-ontology/concept/49", concepts.get(0).getURI());

	// search of a specific concept in the ontology, by URI (itself)
	concepts = ontology.findConcepts("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40", false, true);
	assertEquals(1, concepts.size());
	assertEquals("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40", concepts.get(0).getURI());
	// search for children and equivalent concepts
	concepts = ontology.findConcepts("temperatura", true, false);
	HashSet<String> uris = new HashSet<>();
	for (SKOSConcept concept : concepts) {
	    System.out.println(concept.getURI());
	    uris.add(concept.getURI());
	}
	assertTrue(uris.contains("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40"));
	assertTrue(uris.contains("http://ontology.his-central.geodab.eu/hydro-ontology/concept/49"));
	assertTrue(uris.contains("http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224"));
	assertTrue(!uris.contains("http://ontology.his-central.geodab.eu/hydro-ontology/concept/65"));
	// search for children and equivalent concepts by URI
	concepts = ontology.findConcepts("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40", true, false);
	uris = new HashSet<>();
	for (SKOSConcept concept : concepts) {
	    System.out.println(concept.getURI());
	    uris.add(concept.getURI());
	}
	assertTrue(uris.contains("http://ontology.his-central.geodab.eu/hydro-ontology/concept/40"));
	assertTrue(uris.contains("http://ontology.his-central.geodab.eu/hydro-ontology/concept/49"));
	assertTrue(uris.contains("http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224"));

	// GET CONCEPT
	String c1 = "http://ontology.his-central.geodab.eu/hydro-ontology/concept/49";
	String c2 = "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224";
	SKOSConcept concept = ontology.getConcept(c1);
	assertEquals(concept.getURI(), c1);
	concept = ontology.getConcept(c2);
	assertEquals(concept.getURI(), c1);
    }

}
