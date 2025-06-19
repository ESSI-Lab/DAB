package eu.essi_lab.lib.net.sa;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.net.sa.SAClient.SearchAction;
import eu.essi_lab.lib.net.sa.SAClient.SearchResult;
import eu.essi_lab.lib.net.sa.SAClient.SemanticAnalysisResponse;

public class SAClientExternalTestIT {

    @Test
    public void test() throws Exception {
	SAClient client = new SAClient();

	// Prepare the request
	List<String> terms = Arrays.asList("DYFAMED", "EuroSITES", "MOORING");
	List<String> matchTypes = Arrays.asList("exactMatch");
	List<String> matchProperties = Arrays.asList("altLabel", "prefLabel");

	// Make the request
	SemanticAnalysisResponse response = client.analyzeTerms(terms, matchTypes, matchProperties);

	// Access the results
	System.out.println("Total terms found: " + response.getStats().getTotalNumberTermsFound());
	System.out.println("Terms not found: " + response.getSearchTermsNotFound());
	for (String term : terms) {
	    String uri = response.findTermUri(term);
	    if (uri != null) {
		System.out.println("term found: " + term + "->" + uri);		
	    } else {
		System.out.println("term not found: " + term);
	    }
	}

	// Access individual search results
	// for (SearchAction action : response.getGraph()) {
	// for (SearchResult result : action.getResult()) {
	// System.out.println("Found term: " + result.getName());
	// System.out.println("URL: " + result.getUrl());
	// }
	// }
    }

}
