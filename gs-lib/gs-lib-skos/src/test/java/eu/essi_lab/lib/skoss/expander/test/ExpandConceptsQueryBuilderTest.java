/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.DefaultExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class ExpandConceptsQueryBuilderTest {

    @Test
    public void defaultExpandConceptsQueryBuilderTest() {

	DefaultExpandConceptsQueryBuilder builder = new DefaultExpandConceptsQueryBuilder();

	String query = builder.build(//
		Arrays.asList("CONCEPT"), //
		Arrays.asList("it"), //
		Arrays.asList(SKOSSemanticRelation.BROAD_MATCH), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"SELECT DISTINCT ?pref ?alt ?expanded WHERE {\n" //
		+ "    BIND(<CONCEPT> AS ?concept)\n" //
		+ "\n"//
		+ "    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (\"it\")) }\n"//
		+ "    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (\"it\")) }\n" //
		+ "\n"//
		+ "    OPTIONAL { ?concept skos:closeMatch ?closeMatch }\n"//
		+ "    OPTIONAL { ?concept skos:broadMatch ?expanded } \n"//
		+ "}";//

	Assert.assertEquals(expected, query);
    }

    
}
