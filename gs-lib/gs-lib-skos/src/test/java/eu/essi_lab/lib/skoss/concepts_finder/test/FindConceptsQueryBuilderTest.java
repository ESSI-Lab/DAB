/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class FindConceptsQueryBuilderTest {

    @Test
    public void defaultFindConceptsQueryBuilderTest() {

	DefaultFindConceptsQueryBuilder builder = new DefaultFindConceptsQueryBuilder();
	
	String query = builder.build("searchTerm", Arrays.asList("it"));
	
	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" //
		+ "SELECT DISTINCT ?concept WHERE {\n" //
		+ "    { ?concept skos:prefLabel ?label }\n" //
		+ "    UNION { ?concept skos:altLabel ?label }\n" //
		+ "    UNION { ?concept skos:hiddenLabel ?label }\n" //
		+ "    FILTER(LANG(?label) IN (\"it\") && LCASE(STR(?label)) = \"searchterm\")\n" //
		+ "}"; //
	
	Assert.assertEquals(expected, query);
    }
}
