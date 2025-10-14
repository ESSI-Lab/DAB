/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

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
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.rdf4j.FedXEngine;
import eu.essi_lab.lib.skoss.rdf4j.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutor implements ConceptsQueryExecutor {

    private boolean traceQuery;
    private FedXConfig engineConfig;

    /**
     * 
     */
    public FedXConceptsQueryExecutor() {
    }

    /**
     * @return
     */
    public boolean traceQuery() {

	return traceQuery;
    }

    /**
     * @param traceQuery
     */
    public void setTraceQuery(boolean traceQuery) {

	this.traceQuery = traceQuery;
    }

    /**
     * @return
     */
    public Optional<FedXConfig> getEngineConfig() {

	return Optional.ofNullable(engineConfig);
    }

    /**
     * @param config
     */
    public void setEngineConfig(FedXConfig engine) {

	this.engineConfig = engine;
    }

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    @Override
    public List<String> execute(//
	    ConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception {

	String query = queryBuilder.build(searchTerm, sourceLangs);

	if (traceQuery()) {

	    GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	}

	TupleQuery tupleQuery = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig())).//
		getConnection().//
		prepareTupleQuery(query);

	List<String> concepts = new ArrayList<>();

	try (TupleQueryResult res = tupleQuery.evaluate()) {

	    while (res.hasNext()) {

		String next = res.next().getValue(QueryBinding.CONCEPT.getLabel()).stringValue();

		if (!concepts.contains(next)) {

		    concepts.add(next);
		}
	    }

	} catch (QueryEvaluationException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	return concepts;
    }
}
