/**
 * 
 */
package eu.essi_lab.lib.net.sparql.federated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.federated.FedXFactory;
import org.eclipse.rdf4j.federated.repository.FedXRepository;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.net.sparql.SKOSFederatedSearch;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SKOSSFederatedClient {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_EXPANSION_LEVEL = 1;
    private static final List<String> DEFAULT_RELATIONS = Arrays.asList("skos:narrower", "skos:related");
    private static final List<String> DEFAULT_SEARCH_LANGS = Arrays.asList("it", "en");
    private static final List<String> DEFAULT_SOURCE_LANGS = Arrays.asList("it", "en");
    private static final Object LOCK = new Object();

    private String searchTerm;
    private List<String> sourceLangs;
    private List<String> searchLangs;
    private List<String> ontologyUrls;
    private int expansionLevel;
    private List<String> expansionsRelations;
    private int limit;

    /**
     * 
     */
    public SKOSSFederatedClient() {

	setLimit(DEFAULT_LIMIT);
	setExpansionLevel(DEFAULT_EXPANSION_LEVEL);
	setExpansionsRelations(DEFAULT_RELATIONS);
	setSearchLangs(DEFAULT_SEARCH_LANGS);
	setSourceLangs(DEFAULT_SOURCE_LANGS);
    }

    /**
     * @param sourceLangs
     * @param searchTerm
     * @param searchLangs
     * @param ontologyUrls
     * @param expansionLevel
     * @param expansionRelations
     * @param limit
     * @return
     * @throws Exception
     */
    private List<SKOSSResponseItem> searchAndExpand2(//
	    List<String> sourceLangs, //
	    String searchTerm, //
	    List<String> searchLangs, //
	    List<String> ontologyUrls, //
	    int expansionLevel, //
	    List<String> expansionRelations, //
	    int limit) {

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Creating federation STARTED");

	FedXFactory fed = FedXFactory.newFederation();
	fed.withSparqlEndpoints(ontologyUrls);

//	fed.withConfig(new FedXConfig().withEnforceMaxQueryTime(10));

	FedXRepository repo = fed.create();

	FedXRepositoryConnection conn = repo.getConnection();

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Creating federation ENDED");

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Finding matching concepts STARTED");

	Set<String> initConcepts = findMatchingConcepts(conn, sourceLangs, searchTerm);

	// Set<String> initConcepts = ontologyUrls.//
	// parallelStream().//
	// flatMap(url -> findMatchingConcepts(url, sourceLangs, searchTerm).stream()).//
	// collect(Collectors.toSet());

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Finding matching concepts ENDED");

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Matched concepts: {} ", initConcepts.size());

	initConcepts.forEach(con -> GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info(con));

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding matching concepts STARTED");

	List<SKOSSResponseItem> results = Collections.synchronizedList(new ArrayList<>());

	Set<String> stampSet = Collections.synchronizedSet(new HashSet<>());

	Set<String> visited = Collections.synchronizedSet(new HashSet<>());

	ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	initConcepts.forEach(con -> stampSet.add(con + "0"));

	for (String concept : initConcepts) {

	    expandConcept2(//
		    stampSet, //
		    executor, //
		    concept, //
		    conn, //
		    searchLangs, //
		    expansionRelations, //
		    expansionLevel, //
		    visited, //
		    results, //
		    0);//
	}

	while (!stampSet.isEmpty()) {

	    try {
		Thread.sleep(Duration.ofSeconds(1));

	    } catch (InterruptedException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding matching concepts ENDED");

	//
	//
	//

	conn.close();
	repo.shutDown();

	executor.shutdown();

	//
	//
	//

	if (results.size() > limit) {

	    return results.subList(0, limit);

	} else {

	    return results;
	}
    }

    /**
     * @param sourceLangs
     * @param searchTerm
     * @param searchLangs
     * @param ontologyUrls
     * @param expansionLevel
     * @param expansionRelations
     * @param limit
     * @return
     * @throws Exception
     */
    private List<SKOSSResponseItem> searchAndExpand(//
	    List<String> sourceLangs, //
	    String searchTerm, //
	    List<String> searchLangs, //
	    List<String> ontologyUrls, //
	    int expansionLevel, //
	    List<String> expansionRelations, //
	    int limit) {

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Creating federation STARTED");

	FedXFactory fed = FedXFactory.newFederation();
	fed.withSparqlEndpoints(ontologyUrls);

	FedXRepository repo = fed.create();

	FedXRepositoryConnection conn = repo.getConnection();

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Creating federation ENDED");

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Finding matching concepts STARTED");

	Set<String> initConcepts = findMatchingConcepts(conn, sourceLangs, searchTerm);

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Finding matching concepts ENDED");

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Matched concepts: {} ", initConcepts.size());

	initConcepts.forEach(con -> GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info(con));

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding matching concepts STARTED");

	List<SKOSSResponseItem> results = new ArrayList<>();
	Set<String> visited = new HashSet<>();

	for (String concept : initConcepts) {

	    expandConcept(concept, conn, searchLangs, expansionRelations, expansionLevel, visited, results, 0);
	}

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding matching concepts ENDED");

	conn.close();
	repo.shutDown();

	if (results.size() > limit) {
	    return results.subList(0, limit);
	} else {
	    return results;
	}
    }

    /**
     * @param ontologyUrls
     * @return
     */
    private Set<String> findMatchingConcepts(//
	    String ontologyUrl, //
	    List<String> sourceLangs, //
	    String searchTerm) {

	FedXFactory fed = FedXFactory.newFederation();
	fed.withSparqlEndpoints(Arrays.asList(ontologyUrl));

	FedXRepository repo = fed.create();

	FedXRepositoryConnection connection = repo.getConnection();

	Set<String> matchingConcepts = findMatchingConcepts(connection, sourceLangs, searchTerm);

	connection.close();
	repo.shutDown();

	return matchingConcepts;
    }

    /**
     * @param conn
     * @param sourceLangs
     * @param searchTerm
     * @return
     * @throws Exception
     */
    private Set<String> findMatchingConcepts(//
	    RepositoryConnection conn, //
	    List<String> sourceLangs, //
	    String searchTerm) {

	String langFilter = String.join(",", sourceLangs.stream().map(lang -> "\"" + lang + "\"").toArray(String[]::new));

	String match = String.format("FILTER(LANG(?label) IN (%s) && LCASE(STR(?label)) = \"%s\")", langFilter,
		searchTerm.toLowerCase().replace("\"", "\\\""));

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
     * @param stampSet
     * @param executor
     * @param concept
     * @param conn
     * @param searchLangs
     * @param expansionRelations
     * @param expansionLevel
     * @param visited
     * @param results
     * @param currentLevel
     * @return
     * @throws Exception
     */
    private void expandConcept2(//
	    Set<String> stampSet, //
	    ExecutorService executor, //
	    String concept, //
	    RepositoryConnection conn, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    int expansionLevel, //
	    Set<String> visited, //
	    List<SKOSSResponseItem> results, //
	    int currentLevel) {

	executor.submit(() -> {

	    GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding concept {} STARTED", concept);

	    GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Current level: {}", currentLevel);

	    if (visited.contains(concept) || currentLevel > expansionLevel) {

		GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Ending recursive call");

		return;
	    }

	    stampSet.add(concept + currentLevel);

	    visited.add(concept);

	    String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	    String expansionBlock = currentLevel < expansionLevel ? buildExpansionOptionalBlock("concept", expansionRelations) : "";
	    String closeMatchBlock = currentLevel < expansionLevel ? "OPTIONAL { ?concept skos:closeMatch ?closeMatch }" : "";

	    String queryStr = String.format("""
	    	PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
	    	SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {
	    	    BIND(<%s> AS ?concept)

	    	    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
	    	    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

	    	    %s
	    	    %s
	    	}
	    	""", concept, labelsFilter, labelsFilter, closeMatchBlock, expansionBlock).trim().strip();

	    // GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Current query: \n{}", queryStr);

	    TupleQuery query = conn.prepareTupleQuery(queryStr);

	    GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Evaluating STARTED");

	    try {

		TupleQueryResult res = query.evaluate();

		GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Evaluating ENDED");

		while (res.hasNext()) {

		    var bs = res.next();

		    SKOSSResponseItem item = SKOSSResponseItem.of(//
			    concept, //
			    bs.getValue("pref") != null ? bs.getValue("pref").stringValue() : null, //
			    bs.getValue("expanded") != null ? bs.getValue("expanded").stringValue() : null, //
			    bs.getValue("alt") != null ? bs.getValue("alt").stringValue() : null);

		    if (!results.contains(item)) {

			results.add(item);
		    }

		    if (bs.getValue("closeMatch") != null) {

			expandConcept2(//
				stampSet, //
				executor, //
				bs.getValue("closeMatch").stringValue(), //
				conn, //
				searchLangs, //
				expansionRelations, //
				expansionLevel, //
				visited, //
				results, //
				currentLevel + 1);

		    } else if (bs.getValue("expanded") != null) {

			expandConcept2(//
				stampSet, //
				executor, //
				bs.getValue("expanded").stringValue(), //
				conn, //
				searchLangs, //
				expansionRelations, //
				expansionLevel, //
				visited, //
				results, //
				currentLevel + 1);
		    }
		}

		res.close();

	    } catch (QueryEvaluationException ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

	    } finally {

		stampSet.remove(concept + currentLevel);
	    }

	    GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding concept {} ENDED", concept);

	}, executor);

    }

    /**
     * @param concept
     * @param conn
     * @param searchLangs
     * @param expansionRelations
     * @param expansionLevel
     * @param visited
     * @param results
     * @param currentLevel
     * @throws Exception
     */
    private void expandConcept(//
	    String concept, //
	    RepositoryConnection conn, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    int expansionLevel, //
	    Set<String> visited, //
	    List<SKOSSResponseItem> results, //
	    int currentLevel) {

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding concept {} STARTED", concept);

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Current level: {}", currentLevel);

	if (visited.contains(concept) || currentLevel > expansionLevel) {

	    GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Ending recursive call");

	    return;
	}

	visited.add(concept);

	String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	String expansionBlock = currentLevel < expansionLevel ? buildExpansionOptionalBlock("concept", expansionRelations) : "";
	String closeMatchBlock = currentLevel < expansionLevel ? "OPTIONAL { ?concept skos:closeMatch ?closeMatch }" : "";

	String queryStr = String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {
		    BIND(<%s> AS ?concept)

		    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
		    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

		    %s
		    %s
		}
		""", concept, labelsFilter, labelsFilter, closeMatchBlock, expansionBlock).trim().strip();

	// GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Current query: \n{}", queryStr);

	TupleQuery query = conn.prepareTupleQuery(queryStr);

	try (TupleQueryResult res = query.evaluate()) {

	    while (res.hasNext()) {

		var bs = res.next();

		SKOSSResponseItem item = SKOSSResponseItem.of(//
			concept, //
			bs.getValue("pref") != null ? bs.getValue("pref").stringValue() : null, //
			bs.getValue("expanded") != null ? bs.getValue("expanded").stringValue() : null, //
			bs.getValue("alt") != null ? bs.getValue("alt").stringValue() : null);

		if (!results.contains(item)) {

		    results.add(item);
		}

		if (bs.getValue("closeMatch") != null) {

		    expandConcept(//
			    bs.getValue("closeMatch").stringValue(), //
			    conn, //
			    searchLangs, //
			    expansionRelations, //
			    expansionLevel, //
			    visited, //
			    results, //
			    currentLevel + 1);

		} else if (bs.getValue("expanded") != null) {

		    expandConcept(//
			    bs.getValue("expanded").stringValue(), //
			    conn, //
			    searchLangs, //
			    expansionRelations, //
			    expansionLevel, //
			    visited, //
			    results, //
			    currentLevel + 1);
		}
	    }
	}

	GSLoggerFactory.getLogger(SKOSFederatedSearch.class).info("Expanding concept {} ENDED", concept);
    }

    /**
     * @param conceptVar
     * @param relations
     * @return
     */
    private String buildExpansionOptionalBlock(String conceptVar, List<String> relations) {

	StringBuilder sb = new StringBuilder();

	for (String rel : relations) {
	    sb.append("OPTIONAL { ?").append(conceptVar).append(" ").append(rel).append(" ?expanded } ");
	}

	return sb.toString();
    }

    /**
     * @return
     */
    public String getSearchTerm() {

	return searchTerm;
    }

    /**
     * @param searchTerm
     */
    public void setSearchTerm(String searchTerm) {

	this.searchTerm = searchTerm;
    }

    /**
     * @return
     */
    public List<String> getSourceLangs() {

	return sourceLangs;
    }

    /**
     * @param sourceLangs
     */
    public void setSourceLangs(List<String> sourceLangs) {

	this.sourceLangs = sourceLangs;
    }

    /**
     * @return
     */
    public List<String> getSearchLangs() {

	return searchLangs;
    }

    /**
     * @param searchLangs
     */
    public void setSearchLangs(List<String> searchLangs) {

	this.searchLangs = searchLangs;
    }

    /**
     * @return
     */
    public List<String> getOntologyUrls() {

	return ontologyUrls;
    }

    /**
     * @param ontologyUrls
     */
    public void setOntologyUrls(List<String> ontologyUrls) {

	this.ontologyUrls = ontologyUrls;
    }

    /**
     * @return
     */
    public int getExpansionLevel() {

	return expansionLevel;
    }

    /**
     * @param expansionLevel
     */
    public void setExpansionLevel(int expansionLevel) {

	this.expansionLevel = expansionLevel;
    }

    /**
     * @return
     */
    public List<String> getExpansionsRelations() {

	return expansionsRelations;
    }

    /**
     * @param expansionsRelations
     */
    public void setExpansionsRelations(List<String> expansionsRelations) {

	this.expansionsRelations = expansionsRelations;
    }

    /**
     * @return
     */
    public int getLimit() {

	return limit;
    }

    /**
     * @param limit
     */
    public void setLimit(int limit) {

	this.limit = limit;
    }

    /**
     * @return
     * @throws Exception
     */
    public List<SKOSSResponseItem> search() {

	return searchAndExpand(//
		getSourceLangs(), //
		getSearchTerm(), //
		getSearchLangs(), //
		getOntologyUrls(), //
		getExpansionLevel(), //
		getExpansionsRelations(), //
		getLimit());
    }

    /**
     * @return
     * @throws Exception
     */
    public List<SKOSSResponseItem> search2() {

	return searchAndExpand2(//
		getSourceLangs(), //
		getSearchTerm(), //
		getSearchLangs(), //
		getOntologyUrls(), //
		getExpansionLevel(), //
		getExpansionsRelations(), //
		getLimit());
    }

    /**
     * @return
     * @throws Exception
     */
    public List<SKOSSResponseItem> search3() throws Exception {

	return searchAndExpand2(//
		getSourceLangs(), //
		getSearchTerm(), //
		getSearchLangs(), //
		getOntologyUrls(), //
		getExpansionLevel(), //
		getExpansionsRelations(), //
		getLimit());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	SKOSSFederatedClient client = new SKOSSFederatedClient();

	client.setExpansionLevel(2);
	client.setSearchTerm("water");
	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql"));

	client.setLimit(200);

	// Chronometer chronometer1 = new Chronometer(TimeFormat.SEC_MLS);
	// chronometer1.start();
	//
	// List<SKOSSResponseItem> response1 = client.//
	// search().//
	// stream().//
	//
	// sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).//
	//
	// collect(Collectors.toList());
	//
	// String elTime1 = chronometer1.formatElapsedTime();

	Chronometer chronometer2 = new Chronometer(TimeFormat.SEC_MLS);
	chronometer2.start();

	List<SKOSSResponseItem> response2 = client.//
		search3().//
		stream().//

		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).//

		toList();

	String elTime2 = chronometer2.formatElapsedTime();

	System.out.println("\n\n\n---");
	// System.out.println(elTime1);
	System.out.println(elTime2);
	// System.out.println(response1.equals(response2));

	System.out.println("\n\n");

	// response1.forEach(c -> System.out.println(c + "\n---"));
	// response1.stream().filter(r -> r.getPref().isPresent()).map(r -> r.getPref().get()).distinct().//
	// sorted().//
	// forEach(v -> System.out.println(v));

	System.out.println("\n\n");
	// response2.forEach(c -> System.out.println(c + "\n---"));
	response2.stream().filter(r -> r.getPref().isPresent()).map(r -> r.getPref().get()).distinct().//
		sorted().//
		forEach(v -> System.out.println(v));

    }
}
