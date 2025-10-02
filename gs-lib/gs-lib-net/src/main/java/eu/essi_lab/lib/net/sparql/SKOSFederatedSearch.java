package eu.essi_lab.lib.net.sparql;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.federated.FedXFactory;
import org.eclipse.rdf4j.federated.repository.FedXRepository;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SKOSFederatedSearch {
	/**
	 * Search a term in sourceLang, expand narrower/related relations up to
	 * expansionLevel, follow closeMatch links, and return labels in searchLangs.
	 */
	public static List<Map<String, String>> searchAndExpand(List<String> sourceLangs, String searchTerm,
			List<String> searchLangs, List<String> ontologyUrls, int expansionLevel, List<String> expansionRelations,
			int limit) throws Exception {

		// Create FedX federation
		FedXFactory fed = FedXFactory.newFederation();
		fed.withSparqlEndpoints(ontologyUrls);
		FedXRepository repo = fed.create();
		List<Map<String, String>> results = new ArrayList<>();
		Set<String> visited = new HashSet<>();

		FedXRepositoryConnection conn = repo.getConnection();

		// 1. Find initial matching concepts
		Set<String> initialConcepts = findMatchingConcepts(conn, sourceLangs, searchTerm);

		System.out.println("Matched concepts: " + initialConcepts.size());
		for (String initialConcept : initialConcepts) {
			System.out.println(initialConcept);
		}

		// 2. Recursively expand each concept
		for (String concept : initialConcepts) {
			expandConcept(concept, conn, searchLangs, expansionRelations, expansionLevel, visited, results, 1);
		}

		conn.close();

		repo.shutDown();

		// Limit results
		if (results.size() > limit) {
			return results.subList(0, limit);
		} else {
			return results;
		}
	}

	/**
	 * Find concepts matching the search term in prefLabel, altLabel, hiddenLabel
	 */
	private static Set<String> findMatchingConcepts(RepositoryConnection conn, List<String> sourceLangs,
			String searchTerm) throws Exception {

		String langFilter = String.join(",",
				sourceLangs.stream().map(lang -> "\"" + lang + "\"").toArray(String[]::new));

//		exact match, case sensitive
//		String match = String.format(
//			    "FILTER(LANG(?label) = \"%s\" && STR(?label) = \"%s\")",
//			    sourceLang,
//			    searchTerm.toLowerCase().replace("\"", "\\\"")
//			);
//		exact match, case unsensitive
		String match = String.format("FILTER(LANG(?label) IN (%s) && LCASE(STR(?label)) = \"%s\")", langFilter,
				searchTerm.toLowerCase().replace("\"", "\\\""));

		// Insert into the query
		String queryStr = String.format("""
				PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
				SELECT DISTINCT ?concept WHERE {
				    { ?concept skos:prefLabel ?label }
				    UNION { ?concept skos:altLabel ?label }
				    UNION { ?concept skos:hiddenLabel ?label }
				    %s
				}
				""", match);

		TupleQuery query = conn.prepareTupleQuery(queryStr);
		Set<String> concepts = new HashSet<>();
		try (TupleQueryResult res = query.evaluate()) {
			while (res.hasNext()) {
				concepts.add(res.next().getValue("concept").stringValue());
			}
		}
		return concepts;
	}

	/**
	 * Recursive expansion of narrower/related concepts and closeMatch equivalents
	 */
	private static void expandConcept(String concept, RepositoryConnection conn, List<String> searchLangs,
			List<String> expansionRelations, int expansionLevel, Set<String> visited, List<Map<String, String>> results,
			int currentLevel) throws Exception {

		if (visited.contains(concept) || currentLevel > expansionLevel)
			return;
		visited.add(concept);

		// Fetch labels in desired languages
		String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
		String queryStr = String.format("""
				PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
				SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {
				    BIND(<%s> AS ?concept)

				    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
				    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

				    OPTIONAL { ?concept skos:closeMatch ?closeMatch }

				    OPTIONAL { %s }
				}
				""", concept, labelsFilter, labelsFilter, buildExpansionOptionalBlock("concept", expansionRelations));

		System.out.println(queryStr);
		TupleQuery query = conn.prepareTupleQuery(queryStr);

		try (TupleQueryResult res = query.evaluate()) {
			while (res.hasNext()) {
				var bs = res.next();

				// Save result
				Map<String, String> row = new HashMap<>();
				row.put("concept", concept);
				row.put("expanded", bs.getValue("expanded") != null ? bs.getValue("expanded").stringValue() : null);
				row.put("pref", bs.getValue("pref") != null ? bs.getValue("pref").stringValue() : null);
				row.put("alt", bs.getValue("alt") != null ? bs.getValue("alt").stringValue() : null);
				results.add(row);

				// Recurse on closeMatch
				if (bs.getValue("closeMatch") != null) {
					expandConcept(bs.getValue("closeMatch").stringValue(), conn, searchLangs, expansionRelations,
							expansionLevel, visited, results, currentLevel + 1);
				}

				// Recurse on expanded narrower/related
				if (bs.getValue("expanded") != null) {
					expandConcept(bs.getValue("expanded").stringValue(), conn, searchLangs, expansionRelations,
							expansionLevel, visited, results, currentLevel + 1);
				}
			}
		}
	}

	/**
	 * Build OPTIONAL { ?concept skos:narrower ?expanded } blocks for expansion
	 * relations
	 */
	private static String buildExpansionOptionalBlock(String conceptVar, List<String> relations) {
		StringBuilder sb = new StringBuilder();
		for (String rel : relations) {
			sb.append("OPTIONAL { ?").append(conceptVar).append(" ").append(rel).append(" ?expanded } ");
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		// Parameters
		List<String> sourceLangs = Arrays.asList("en", "it");
		String searchTerm = "livello";
//		String searchTerm = "level"; // works as well
		List<String> searchLangs = Arrays.asList("en", "it");
		List<String> ontologyUrls = Arrays.asList(
				//
				"http://hydro.geodab.eu/hydro-ontology/sparql"
//				"https://vocabularies.unesco.org/sparql"
		//
//				"http://www.eionet.europa.eu/gemet/sparql"
		);
		int expansionLevel = 1;
		List<String> expansionsRelations = Arrays.asList("skos:narrower", "skos:related");
		int limit = 50;

		List<Map<String, String>> results = SKOSFederatedSearch.searchAndExpand(sourceLangs, searchTerm, searchLangs,
				ontologyUrls, expansionLevel, expansionsRelations, limit);

		for (Map<String, String> result : results) {
			for (String key : result.keySet()) {
				System.out.println(key + ": " + result.get(key));
			}
			System.out.println("");
		}
	}
}
