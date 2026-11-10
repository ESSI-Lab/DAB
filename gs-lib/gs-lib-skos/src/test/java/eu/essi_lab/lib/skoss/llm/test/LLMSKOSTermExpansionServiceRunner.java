package eu.essi_lab.lib.skoss.llm.test;

import eu.essi_lab.lib.skos.llm.client.*;
import eu.essi_lab.lib.skos.llm.model.*;
import eu.essi_lab.lib.skos.llm.service.*;

/**
 * @author Fabrizio
 */
public class LLMSKOSTermExpansionServiceRunner {

    /**
     * @param args
     */
    public static void main(String[] args) {

	OpenRouterConfig config = OpenRouterConfig.fromEnv();

	try (SKOSTermExpansionService service = new SKOSTermExpansionService(config)) {

	    //	    // Simple case
	    //	    TermExpansionResult result = service.expand("photosynthesis");
	    //
	    //	    print(result);
	    //
	    //	    // Richer case
	    //	    TermExpansionRequest scoped = new TermExpansionRequest(
	    //		    "cell wall", // search term
	    //		    "plant biology, botanical taxonomy vocabulary", // domain context
	    //		    null,// custom relation
	    //		    "en",
	    //		    List.of(SKOSRelationType.BROADER, SKOSRelationType.NARROWER, SKOSRelationType.RELATED),
	    //		    10);

	    // Custom case
	    TermExpansionRequest custom = TermExpansionRequest.custom( //
		    "darkness", // search term
		    "only terms that are opposite to the search term",// custom relation
		    "italian and german", //
		    30);

	    print(service.expand(custom));
	}
    }

    /**
     * @param result
     */
    private static void print(TermExpansionResult result) {

	System.out.println("=== " + result.searchTerm() + " (model: " + result.modelUsed() + ") ===");

	for (int i = 0; i < result.relatedTerms().size(); i++) {

	    RelatedTerm rt = result.relatedTerms().get(i);

	    String relation =
		    rt.relationType() == SKOSRelationType.OTHER ? "OTHER(" + rt.otherRelationLabel() + ")" : rt.relationType().name();

	    System.out.printf("%s. [%s] %-25s conf=%.2f  %s%n", i, relation, rt.term(), rt.confidence(), rt.rationale());

	}

	System.out.println();
    }
}
