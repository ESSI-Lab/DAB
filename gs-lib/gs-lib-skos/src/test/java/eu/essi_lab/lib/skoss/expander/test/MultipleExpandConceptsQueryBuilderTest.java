/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.query.impl.DefaultExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skos.expander.query.impl.MultipleExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class MultipleExpandConceptsQueryBuilderTest {

    @Test
    public void excludeNoLanguageConceptsWithLanguageTest() {

	MultipleExpandConceptsQueryBuilder builder = new MultipleExpandConceptsQueryBuilder();

	builder.setIncludeNoLanguageConcepts(false); // default

	String query = builder.build(//
		Arrays.asList("CONCEPT1", "CONCEPT2"), //
		Arrays.asList("it"), //
		Arrays.asList(SKOSSemanticRelation.BROADER), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + //
		"SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {\n" + //
		"    VALUES ?concept {\n" + //
		"  <CONCEPT1>\n" + //
		"<CONCEPT2>\n\n" + //

		"}\n" + //

		"OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (\"it\")) }\n" + //

		"  OPTIONAL { ?concept ?altProp ?alt\n" + //
		"    FILTER(?altProp IN (skos:altLabel, rdfs:label) )\n" + //
		"    FILTER(LANG(?alt) IN (\"it\") ) }\n" + //

		"      OPTIONAL { { OPTIONAL { ?concept skos:broader ?expanded } } } \n" + //
		" }";//

	System.out.println(query);

	Assert.assertEquals(expected, query);
    }

    @Test
    public void excludeNoLanguageConceptsWithoutLanguageTest() {

	MultipleExpandConceptsQueryBuilder builder = new MultipleExpandConceptsQueryBuilder();

	builder.setIncludeNoLanguageConcepts(false); // default

	String query = builder.build(//
		Arrays.asList("CONCEPT1", "CONCEPT2"), //
		Arrays.asList(), //
		Arrays.asList(SKOSSemanticRelation.BROADER), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + //
		"SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {\n" + //
		"    VALUES ?concept {\n" + //
		"  <CONCEPT1>\n" + //
		"<CONCEPT2>\n\n" + //

		"}\n" + //

		"OPTIONAL { ?concept skos:prefLabel ?pref  }\n" + //

		"  OPTIONAL { ?concept ?altProp ?alt\n" + //
		"    FILTER(?altProp IN (skos:altLabel, rdfs:label) )\n" + //
		"     }\n" + //

		"      OPTIONAL { { OPTIONAL { ?concept skos:broader ?expanded } } } \n" + //
		" }";//

	System.out.println(query);

	Assert.assertEquals(expected, query);
    }

    @Test
    public void includeNoLanguageConceptsWithLanguageTest() {

	MultipleExpandConceptsQueryBuilder builder = new MultipleExpandConceptsQueryBuilder();

	builder.setIncludeNoLanguageConcepts(true);

	String query = builder.build(//
		Arrays.asList("CONCEPT1", "CONCEPT2"), //
		Arrays.asList("it"), //
		Arrays.asList(SKOSSemanticRelation.BROADER), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);///

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + //
		"SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {\n" + //
		"    VALUES ?concept {\n" + //
		"  <CONCEPT1>\n" + //
		"<CONCEPT2>\n\n" + //

		"}\n" + //

		"OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (\"it\")) }\n" + //

		"  OPTIONAL { ?concept ?altProp ?alt\n" + //
		"    FILTER(?altProp IN (skos:altLabel, rdfs:label) ||LANG(?alt)=\"\")\n" + //
		"    FILTER(LANG(?alt) IN (\"it\") || LANG(?alt)=\"\" ) }\n" + //

		"      OPTIONAL { { OPTIONAL { ?concept skos:broader ?expanded } } } \n" + //
		" }";//

	System.out.println(query);

	Assert.assertEquals(expected, query);
    }

    @Test
    public void includeNoLanguageConceptsWithoutLanguageTest() {

	MultipleExpandConceptsQueryBuilder builder = new MultipleExpandConceptsQueryBuilder();

	builder.setIncludeNoLanguageConcepts(true);

	String query = builder.build(//
		Arrays.asList("CONCEPT1","CONCEPT2"), //
		Arrays.asList(), //
		Arrays.asList(SKOSSemanticRelation.BROADER), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + //
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + //
		"SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {\n" + //
		"    VALUES ?concept {\n" + //
		"  <CONCEPT1>\n" + //
		"<CONCEPT2>\n\n" + //

		"}\n" + //

		"OPTIONAL { ?concept skos:prefLabel ?pref  }\n" + //

		"  OPTIONAL { ?concept ?altProp ?alt\n" + //
		"    FILTER(?altProp IN (skos:altLabel, rdfs:label) ||LANG(?alt)=\"\")\n" + //
		"    FILTER(LANG(?alt)=\"\" ) }\n" + //

		"      OPTIONAL { { OPTIONAL { ?concept skos:broader ?expanded } } } \n" + //
		" }";//

	System.out.println(query);

	Assert.assertEquals(expected, query);
    }
}
