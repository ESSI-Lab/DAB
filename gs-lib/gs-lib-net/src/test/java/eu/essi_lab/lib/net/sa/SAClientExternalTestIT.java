package eu.essi_lab.lib.net.sa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.net.sa.SAClient.SemanticAnalysisResponse;
import eu.essi_lab.lib.net.sa.SAClient.SemanticAnalysisResponse.TermMatchInfo;

public class SAClientExternalTestIT {

    @Test
    public void test() throws Exception {
	SAClient client = new SAClient();

	// Prepare the request
	List<String> terms = new ArrayList<String>();
	terms = Arrays.asList("DYFAMED", "EuroSITES", "MOORING");

	
//	InputStream termsStream = SAClientExternalTestIT.class.getClassLoader().getResourceAsStream("terms");
//	try (BufferedReader reader = new BufferedReader(new InputStreamReader(termsStream, StandardCharsets.UTF_8))) {
//	    String line;
//	    while ((line = reader.readLine()) != null) {
//		terms.add(line.trim());
//	    }
//	}

	List<String> matchTypes = Arrays.asList("exactMatch");
	List<String> matchProperties = Arrays.asList("altLabel", "prefLabel");

	// Make the request
	SemanticAnalysisResponse response = client.analyzeTerms(terms, matchTypes, matchProperties).get(0);

	// Access the results
	System.out.println("Total terms found: " + response.getStats().getTotalNumberTermsFound());
	System.out.println("Terms not found: " + response.getSearchTermsNotFound());
	for (String term : terms) {
	    TermMatchInfo info = response.findTermMatchInfo(term);
	    if (info != null) {
		System.out.println("term found: " + term + "->" + info.getUri()+" "+info.getAdditionalType());
		
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
