package eu.essi_lab.lib.skos.rdf4j;

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

import eu.essi_lab.lib.skos.QueryTask;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class RDF4JQueryTask implements Callable<List<SKOSConcept>>, QueryTask {

    static {

	System.setProperty("rdf4j.sparql.url.maxlength", String.valueOf(Integer.MAX_VALUE));
    }

    private String ontologyURL;
    private String query;
    private List<SKOSConcept> currentLevelResults;

    /**
     * @param ontologyURL
     * @param query
     * @param currentLevelResults
     */
    public RDF4JQueryTask(String ontologyURL, String query, List<SKOSConcept> currentLevelResults) {
	super();
	this.ontologyURL = ontologyURL;
	this.query = query;
	this.currentLevelResults = currentLevelResults;
    }

    @Override
    public List<SKOSConcept> call() throws Exception {

	SKOSResponse ret = query(ontologyURL, query, currentLevelResults);
	return ret.getAggregatedResults();
    }

    /**
     * @param endpointUrl
     * @param query
     * @param concepts
     * @return
     */
    @Override
    public SKOSResponse query(String endpointUrl, String query, List<SKOSConcept> concepts) {

	List<SKOSConcept> tmpResults = new ArrayList<>();
	SKOSResponse tmpResponse = SKOSResponse.of(tmpResults);

	if (concepts != null && concepts.isEmpty()) {
	    return tmpResponse;
	}

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

		    Set<String> father = new HashSet<String>();

		    if (concept != null && concepts != null) {
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

	    GSLoggerFactory.getLogger(DefaultConceptsExpander.class).error("Error occurred with ontology: {}", endpointUrl);
	    GSLoggerFactory.getLogger(DefaultConceptsExpander.class).error(ex);

	} finally {

	    repo.shutDown();
	}

	return tmpResponse;
    }

}
