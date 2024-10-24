package eu.essi_lab.lib.net.utils.whos.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.List;

import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;

public class HydroOntologyPrinter {

    public HydroOntologyPrinter() throws Exception {
	BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/hydro-ontology.csv"));
	writer.write("Concept URI" + "\t" + "Preferred English label" + "\t" + "Synonym" + "\t" + "Close match concept URIs\n");
	HydroOntology ontology = new WHOSOntology();
	List<SKOSConcept> concepts = ontology.findConcepts("http://hydro.geodab.eu/hydro-ontology/concept/1", true, true);	
	for (SKOSConcept concept : concepts) {
	    HashSet<SimpleEntry<String, String>> labels = concept.getAlternateLabels();

	    String close = "";
	    HashSet<String> matches = ontology.getConcept(concept.getURI()).getCloseMatches();
	    for (String match : matches) {
		close += match + ";";
	    }
	    String pref = "";
	    SimpleEntry<String, String> pl = concept.getPreferredLabel();
	    if (pl != null) {
		pref = pl.getKey();
	    }

	    if (labels == null || labels.isEmpty()) {
		writer.write(concept.getURI() + "\t" + pref.replace("\t", "") + "\t" + "" + "\t" + close.replace("\t", "") + "\n");
	    } else {
		for (SimpleEntry<String, String> label : labels) {
		    String term = label.getKey();
		    String lang = label.getValue();
		    writer.write(concept.getURI() + "\t" + pref.replace("\t", "") + "\t" + term + " (" + lang + ")" + "\t"
			    + close.replace("\t", "") + "\n");
		}
	    }

	}
	writer.close();
    }

    public static void main(String[] args) throws Exception {
	HydroOntologyPrinter p = new HydroOntologyPrinter();
    }
}
