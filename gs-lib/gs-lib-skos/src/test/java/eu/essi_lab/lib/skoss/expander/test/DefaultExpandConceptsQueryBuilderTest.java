/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.impl.DefaultExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultExpandConceptsQueryBuilderTest {

    @Test
    public void test() {

	DefaultExpandConceptsQueryBuilder builder = new DefaultExpandConceptsQueryBuilder();

	String query = builder.build(//
		Arrays.asList("CONCEPT"), //
		Arrays.asList("it"), //
		false,
		Arrays.asList(SKOSSemanticRelation.BROAD_MATCH), //
		ExpansionLevel.HIGH, //
		ExpansionLevel.NONE);//

	
	String expected = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"
		+ "SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {\n"
		+ "    BIND(<CONCEPT> AS ?concept)\n"
		+ "\n"
		+ "    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (\"it\") ) }\n"
		+ "     OPTIONAL {\n"
		+ "                      ?concept ?altProp ?alt\n"
		+ "                      FILTER(?altProp IN (skos:altLabel, rdfs:label))\n"
		+ "                      FILTER(LANG(?alt) IN (\"it\") )\n"
		+ "                    }\n"
		+ "\n"
		+ "     OPTIONAL { { OPTIONAL { ?concept skos:broadMatch ?expanded } } } \n"
		+ "}";
	;//
	
	System.out.println(expected);
	
	System.out.println(query);

	Assert.assertEquals(expected, query);
    }
}
