package eu.essi_lab.lib.odip.rosetta;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class RosettaStoneConnector extends RosettaStone {

    private static final String PREFIXES = "prefix skos:<http://www.w3.org/2004/02/skos/core#>\n" + //
	    "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + // "
	    "prefix owl:<http://www.w3.org/2002/07/owl#>\n" + //
	    "prefix dc:<http://purl.org/dc/terms/>\n";

    public RosettaStoneConnector() {
    }

    @Override
    public Set<String> getTranslations(String term) {
	Set<String> ret = new TreeSet<>();
	if (!term.contains("SDN:EDMO")) {
	    if (term.contains("http://www.seadatanet.org/urnurl/")) {
		ret.add(translateSDNtoNERC(term));
	    } else if (term.contains("http://vocab.nerc.ac.uk/collection")) {
		ret.add(translateNERCtoSDN(term));
	    }
	}
	term = normalizeTermToNERC(term);
	String q = getSameQuery(term);
	ret.addAll(query(q));
	ret = augmentWithSDNterms(ret);
	return ret;
    }

    private String getSameQuery(String term) {
	boolean transitive = true;
	String relation = "owl:sameAs";
	String transitivePart = "";
	if (transitive) {
	    transitivePart = "UNION \n" + //
		    " { ?obj " + relation + " <" + term + "> }  \n" + //
		    " UNION \n " + //
		    " { ?sdn " + relation + " <" + term + "> . \n" + //
		    "  ?sdn " + relation + " ?obj } \n";// ;
	}
	String q = PREFIXES + //
		"select distinct ?obj where {\n" + //
		"{<" + term + "> " + relation + " ?obj} \n" + //
		transitivePart + " }";
	return q;
    }

    private String getNarrowerQuery(String term) {
	boolean transitive = true;
	String relation = "skos:narrower";
	String inverseRelation = "skos:broader";
	String transitivePart = "";
	if (transitive) {
	    transitivePart = "UNION \n" + //
		    " { ?obj " + inverseRelation + " <" + term + "> } \n";
	}
	String q = PREFIXES + //
		"select distinct ?obj where {\n" + //
		"{<" + term + "> " + relation + " ?obj} \n" + //
		transitivePart + " }";
	return q;
    }

    private String getBroaderQuery(String term) {
	boolean transitive = true;
	String relation = "skos:broader";
	String inverseRelation = "skos:narrower";
	String transitivePart = "";
	if (transitive) {
	    transitivePart = "UNION \n" + //
		    " { ?obj " + inverseRelation + " <" + term + "> } \n";
	}
	String q = PREFIXES + //
		"select distinct ?obj where {\n" + //
		"{<" + term + "> " + relation + " ?obj} \n" + //
		transitivePart + " }";
	return q;
    }

    @Override
    public Set<String> getNarrower(String term) {
	term = normalizeTermToNERC(term);
	String q = getNarrowerQuery(term);
	Set<String> ret = query(q);
	ret = augmentWithSDNterms(ret);
	return ret;
    }

    @Override
    public Set<String> getBroader(String term) {
	term = normalizeTermToNERC(term);
	String q = getBroaderQuery(term);
	Set<String> ret = query(q);
	ret = augmentWithSDNterms(ret);
	return ret;
    }

    private Set<String> augmentWithSDNterms(Set<String> ret) {
	List<String> sdnTerms = new ArrayList<>();
	for (String term : ret) {
	    if (term.contains("http://vocab.nerc.ac.uk/collection")) {
		sdnTerms.add(translateNERCtoSDN(term));
	    }
	}
	ret.addAll(sdnTerms);
	return ret;
    }

    private String normalizeTermToNERC(String term) {
	if (!term.contains("SDN:EDMO")) {
	    if (term.contains("http://www.seadatanet.org/urnurl/")) {
		term = translateSDNtoNERC(term);
	    }
	}
	return term;
    }

    public Set<String> query(String queryString) {
	Set<String> ret = new TreeSet<String>();

	Query query = QueryFactory.create(queryString);
	QueryExecution qexec = QueryExecutionFactory.sparqlService("http://vocab.nerc.ac.uk/sparql/sparql", query);
	// QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

	ResultSet results = null;
	while (results == null) {
	    try {
		results = qexec.execSelect();
		while (results.hasNext()) {
		    GSLoggerFactory.getLogger(getClass()).info("Getting next result");
		    QuerySolution next = results.next();
		    GSLoggerFactory.getLogger(getClass()).info("Getting var names");
		    List<String> variables = Lists.newArrayList(next.varNames());
		    for (String var : variables) {
			String representation = next.get(var).toString().trim();
			ret.add(representation);
			GSLoggerFactory.getLogger(getClass()).info("Adding result: " + representation);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	qexec.close();
	return ret;
    }

}
