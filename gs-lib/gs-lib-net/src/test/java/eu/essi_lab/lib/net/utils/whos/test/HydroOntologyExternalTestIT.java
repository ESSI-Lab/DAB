package eu.essi_lab.lib.net.utils.whos.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;

public class HydroOntologyExternalTestIT {
	String dischargeURI = "http://hydro.geodab.eu/hydro-ontology/concept/76";
	String dischargeStreamURI = "http://hydro.geodab.eu/hydro-ontology/concept/78";

	HydroOntology ontology = new WHOSOntology();

	@Test
	public void testFindAccentedConcepts() {

		List<SKOSConcept> concepts = ontology.findConcepts("precipitación");
		assertTrue(concepts.size() == 1);

	}

	@Test
	public void testFindSpecialCharacters() {

		List<SKOSConcept> concepts = ontology.findConcepts("CL (nuvens baixas)");
		assertTrue(concepts.size() == 1);

	}

	@Test
	public void testFindConceptsItalian() {

		List<SKOSConcept> concepts = ontology.findConcepts("flusso, portata");
		assertTrue(concepts.size() == 1);
		assertDischargeConcept(concepts.get(0));

		concepts = ontology.findConcepts("temperatura");
		for (SKOSConcept concept : concepts) {
			System.out.println(concept.getPreferredLabel());
		}
	}

	@Test
	public void testFindConceptsFinnish() {

		List<SKOSConcept> concepts = ontology.findConcepts("Virtaama");
		assertTrue(concepts.size() == 1);
		assertDischargeConcept(concepts.get(0));

	}

	@Test
	public void testGetConcept() {

		SKOSConcept concept = ontology.getConcept(dischargeURI);
		assertDischargeConcept(concept);

	}

	@Test
	public void testGetCloseConcept() {

		SKOSConcept concept = ontology.getConcept(dischargeStreamURI);
		assertEquals(dischargeStreamURI, concept.getURI());
		assertEquals("Discharge, stream", concept.getPreferredLabel().getKey());
		assertTrue(!concept.getCloseMatches().isEmpty());
	}

	@Test
	public void testFindCloseConcepts() {

		List<SKOSConcept> concepts = ontology.findConcepts(dischargeStreamURI, true, false);
		System.out.println(concepts.size());

		HashSet uris = new HashSet<>();
		for (SKOSConcept concept : concepts) {
			System.out.println(concept.getURI());
			uris.add(concept.getURI());
		}
		assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/78"));
		assertTrue(uris.contains("http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171"));
	}

	private void assertDischargeConcept(SKOSConcept concept) {
		assertEquals(dischargeURI, concept.getURI());
		assertEquals("Flux, discharge", concept.getPreferredLabel().getKey());
		HashSet<SimpleEntry<String, String>> alternateLabels = concept.getAlternateLabels();
		boolean found = false;
		for (SimpleEntry<String, String> simpleEntry : alternateLabels) {
			String label = simpleEntry.getKey();
			String language = simpleEntry.getValue();
			if (label.equals("Flusso, portata") && language.equals("it")) {
				found = true;
			}
		}
		assertTrue(found);

	}

}
