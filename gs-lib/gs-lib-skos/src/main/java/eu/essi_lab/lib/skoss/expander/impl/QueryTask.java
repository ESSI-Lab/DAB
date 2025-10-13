package eu.essi_lab.lib.skoss.expander.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class QueryTask implements Callable<List<SKOSConcept>> {

    private String ontologyURL;
    private String query;

    public QueryTask(String ontologyURL, String query, List<SKOSConcept> currentLevelResults) {
	super();
	this.ontologyURL = ontologyURL;
	this.query = query;
	this.currentLevelResults = currentLevelResults;
    }

    private List<SKOSConcept> currentLevelResults;

    @Override
    public List<SKOSConcept> call() throws Exception {
	SKOSResponse ret = querySingleOntology(ontologyURL, query, currentLevelResults);
	return ret.getAggregatedResults();
    }

    private SKOSResponse querySingleOntology(String endpointUrl, String query, List<SKOSConcept> concepts) {
	List<SKOSConcept> tmpResults = new ArrayList<>();
	SKOSResponse tmpResponse = SKOSResponse.of(tmpResults);

	if (concepts.isEmpty()) {
	    return tmpResponse;
	}

	// Create a SPARQLRepository for a single endpoint
	Repository repo = new SPARQLRepository(endpointUrl);
	repo.init();

	try (RepositoryConnection conn = repo.getConnection()) {

	    TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

	    try (TupleQueryResult res = tupleQuery.evaluate()) {
		while (res.hasNext()) {

		    BindingSet bs = res.next();
		    String concept = bs.getValue(QueryBinding.CONCEPT.getLabel()) != null
			    ? bs.getValue(QueryBinding.CONCEPT.getLabel()).stringValue()
			    : null;

		    Set<String> father = null;

		    if (concept != null) {
			for (SKOSConcept fatherConcept : concepts) {
			    Set<String> tmpFathers = fatherConcept.getExpandedFrom();
			    if (fatherConcept.getConcept().equals(concept)) {
				father = tmpFathers;
			    }
			}
		    }

		    SKOSConcept item = SKOSConcept.of(concept,
			    bs.getValue(QueryBinding.PREF.getLabel()) != null ? bs.getValue(QueryBinding.PREF.getLabel()).stringValue()
				    : null,
			    bs.getValue(QueryBinding.EXPANDED.getLabel()) != null
				    ? Set.of(bs.getValue(QueryBinding.EXPANDED.getLabel()).stringValue())
				    : new HashSet<String>(),
			    father,
			    bs.getValue(QueryBinding.ALT.getLabel()) != null
				    ? Set.of(bs.getValue(QueryBinding.ALT.getLabel()).stringValue())
				    : new HashSet<String>());

		    tmpResults.add(item);
		}
	    }

	} catch (QueryEvaluationException ex) {
	    // handle the error gracefully (same as your FedX code)
	    GSLoggerFactory.getLogger(DefaultConceptsExpander.class).error(ex);
	} finally {
	    repo.shutDown();
	}

	return tmpResponse;
    }

}
