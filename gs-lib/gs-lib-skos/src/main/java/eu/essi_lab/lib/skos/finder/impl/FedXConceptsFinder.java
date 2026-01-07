/**
 * 
 */
package eu.essi_lab.lib.skos.finder.impl;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.essi_lab.lib.skos.QueryTask;
import eu.essi_lab.lib.skos.finder.ConceptsQueryBuilder;
import eu.essi_lab.lib.skos.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ThreadMode;
import eu.essi_lab.lib.utils.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.utils.ThreadMode.SingleThreadMode;

/**
 * @author Fabrizio
 */
public class FedXConceptsFinder extends AbstractConceptsFinder<QueryTask> {

    private ConceptsQueryExecutor executor;

    /**
     *  
     */
    public FedXConceptsFinder() {

	setExecutor(new FedXConceptsQueryExecutor());
	setThreadMode(ThreadMode.SINGLE());
    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts STARTED");

	List<String> out = switch (getThreadMode()) {

	case SingleThreadMode single -> find_(searchTerm, ontologyUrls, sourceLangs).collect(Collectors.toList());

	case MultiThreadMode multi -> {

	    ExecutorService executor = multi.getExecutor();

	    List<CompletableFuture<Stream<String>>> futures = ontologyUrls.//
		    stream().//
		    map(url -> CompletableFuture.supplyAsync(() -> find_(searchTerm, Arrays.asList(url), sourceLangs), executor)).//
		    toList();

	    yield futures.stream().flatMap(CompletableFuture::join).toList();
	}

	default -> null;// no way
	};

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Concepts found: {}", out.size());

	out.forEach(con -> GSLoggerFactory.getLogger(getClass()).debug("{}", con));

	return out;
    }

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     */
    private Stream<String> find_(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) {

	ConceptsQueryBuilder queryBuilder = getQueryBuilder();

	ConceptsQueryExecutor executor = getExecutor();

	try {

	    return executor.execute(queryBuilder, searchTerm, ontologyUrls, sourceLangs).stream();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return Stream.of();
    }

    /**
     * @return the executor
     */
    public ConceptsQueryExecutor getExecutor() {

	return executor;
    }

    /**
     * @param executor
     */
    public void setExecutor(ConceptsQueryExecutor executor) {

	this.executor = executor;
    }

}
