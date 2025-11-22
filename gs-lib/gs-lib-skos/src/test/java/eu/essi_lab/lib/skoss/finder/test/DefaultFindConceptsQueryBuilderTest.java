/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultFindConceptsQueryBuilderTest {

    @Test
    public void withLanguageTest() {

	DefaultConceptsQueryBuilder builder = new DefaultConceptsQueryBuilder();

	String query = builder.build("searchTerm", Arrays.asList("it"));

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" //
		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"

		+ "SELECT DISTINCT ?concept WHERE {\n" //
		+ "    { ?concept skos:prefLabel ?label }\n" //
		+ "    UNION { ?concept skos:altLabel ?label }\n" //
		+ "    UNION { ?concept rdfs:label ?label }\n" //
		+ "    UNION { ?concept skos:hiddenLabel ?label }\n" //
		+ "    FILTER(LANG(?label) IN (\"it\") && LCASE(STR(?label)) = \"searchterm\")\n" //
		+ "}"; //

	Assert.assertEquals(expected, query);
    }
    
    @Test
    public void withoutLanguageTest() {

	DefaultConceptsQueryBuilder builder = new DefaultConceptsQueryBuilder();

	String query = builder.build("searchTerm", Arrays.asList());

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" //
		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"

		+ "SELECT DISTINCT ?concept WHERE {\n" //
		+ "    { ?concept skos:prefLabel ?label }\n" //
		+ "    UNION { ?concept skos:altLabel ?label }\n" //
		+ "    UNION { ?concept rdfs:label ?label }\n" //
		+ "    UNION { ?concept skos:hiddenLabel ?label }\n" //
		+ "    FILTER(LCASE(STR(?label)) = \"searchterm\")\n" //
		+ "}"; //

	Assert.assertEquals(expected, query);
    }
}
