/**
 * 
 */
package eu.essi_lab.lib.skoss.client.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSClient;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSResponseItem;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;

/**
 * @author Fabrizio
 */
public class ClientTest {

    @Test
    public void defaultTest() throws Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	));

	client.setSearchTerm("water");

	SKOSResponse response = client.search();

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }

    @Test
    public void mediumExpansionLimit200Test() throws Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	));

	client.setExpansionLevel(ExpansionLevel.MEDIUM);

	client.setLimit(200);

	client.setSearchTerm("water");

	SKOSResponse response = client.search();

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }

    @Test
    public void mediumExpansionFinderMultiThreadVSFinderSingleThreadLimit200Test() throws Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	));

	client.setExpansionLevel(ExpansionLevel.MEDIUM);
	client.setLimit(200);
	client.setSearchTerm("water");

	SKOSResponse response = client.search();

	List<SKOSResponseItem> singleThreadFinderResults = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	//
	//
	//

	client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	));

	client.setExpansionLevel(ExpansionLevel.MEDIUM);
	client.setLimit(200);
	client.setSearchTerm("water");

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());
	client.setFinder(finder);

	response = client.search();

	List<SKOSResponseItem> multiThreadFinderResults = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	//
	//
	//

	Assert.assertEquals(singleThreadFinderResults, multiThreadFinderResults);
    }
}
