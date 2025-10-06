/**
 * 
 */
package eu.essi_lab.lib.net.sa.skossfedclient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.lib.net.sparql.federated.SKOSSFederatedClient;
import eu.essi_lab.lib.net.sparql.federated.SKOSSResponseItem;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;

/**
 * @author Fabrizio
 */
public class Test {

    @org.junit.Test
    public void test1() {

	SKOSSFederatedClient client = new SKOSSFederatedClient();

	client.setExpansionLevel(2);
	client.setSearchTerm("water");
	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// "https://dbpedia.org/sparql"
	));

	client.setLimit(200);

	Chronometer chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	List<SKOSSResponseItem> response = client.//
		search().//
		stream().//

		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).//

		collect(Collectors.toList());

	String elTime = chronometer.formatElapsedTime();

	System.out.println("\n\n\n---");
	System.out.println(elTime);

	System.out.println("\n\n");

	response.forEach(c -> System.out.println(c + "\n---"));
	response.stream().filter(r -> r.getPref().isPresent()).map(r -> r.getPref().get()).distinct().//
		sorted().//
		forEach(v -> System.out.println(v));

    }
    
    @org.junit.Test
    public void test2() {

	SKOSSFederatedClient client = new SKOSSFederatedClient();

	client.setExpansionLevel(2);
	client.setSearchTerm("water");
	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// "https://dbpedia.org/sparql"
	));

	client.setLimit(200);

	Chronometer chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	List<SKOSSResponseItem> response = client.//
		search2().//
		stream().//

		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).//

		collect(Collectors.toList());

	String elTime = chronometer.formatElapsedTime();

	System.out.println("\n\n\n---");
	System.out.println(elTime);

	System.out.println("\n\n");

	response.forEach(c -> System.out.println(c + "\n---"));
	response.stream().filter(r -> r.getPref().isPresent()).map(r -> r.getPref().get()).distinct().//
		sorted().//
		forEach(v -> System.out.println(v));

    }
}
