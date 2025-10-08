/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.ExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.concepts_expander.impl.CloseMatchExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_expander.impl.DefaultExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class ExpandConceptsQueryBuilderTest {

    @Test
    public void defaultExpandConceptsQueryBuilderTest() {

	ExpandConceptsQueryBuilder builder = new DefaultExpandConceptsQueryBuilder();

	String query = builder.build(//
		"CONCEPT", //
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

    @Test
    public void closeMatchExpandConceptsQueryBuilderTest() {

	ExpandConceptsQueryBuilder builder = new CloseMatchExpandConceptsQueryBuilder();

	String query = builder.build(//
		"CONCEPT", //
		Arrays.asList("it"), //
		Arrays.asList(SKOSSemanticRelation.BROAD_MATCH), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {\n"//
		+ "    BIND(<CONCEPT> AS ?concept)\n" + "\n"//
		+ "    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (\"it\")) }\n"//
		+ "    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (\"it\")) }\n" //
		+ "\n"//
		+ "    OPTIONAL { ?concept skos:closeMatch ?closeMatch }\n" //
		+ "    OPTIONAL { ?concept skos:broadMatch ?expanded } \n"//
		+ "}";//

	Assert.assertEquals(expected, query);
    }
}
