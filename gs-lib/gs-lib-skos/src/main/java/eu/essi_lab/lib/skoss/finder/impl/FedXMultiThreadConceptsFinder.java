/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.federated.FedXConfig;

import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.FindConceptsQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXMultiThreadConceptsFinder extends AbstractFedXConceptsFinder {

    private FedXConfig config;

    /**
     * 
     */
    public FedXMultiThreadConceptsFinder() {

    }

    /**
     * @param config
     */
    public FedXMultiThreadConceptsFinder(FedXConfig config) {

	setConfiguration(config);
    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	return ontologyUrls.//
		parallelStream().//
		flatMap(url -> {

		    FedXEngine engine = buildEngine(Arrays.asList(url));

		    FindConceptsQueryBuilder queryBuilder = getQueryBuilder();

		    ConceptsQueryExecutor executor = getExecutor();

		    try {

			return executor.execute(queryBuilder, searchTerm, ontologyUrls, sourceLangs).stream();

		    } catch (Exception e) {

			GSLoggerFactory.getLogger(getClass()).error(e);

		    } finally {

			engine.close();
		    }

		    return Stream.of();

		}).collect(Collectors.toList());
    }

    /**
     * @return
     */
    public Optional<FedXConfig> getConfiguration() {

	return Optional.ofNullable(config);
    }

    /**
     * @param config
     */
    public void setConfiguration(FedXConfig config) {

	this.config = config;
    }

    /**
     * @param ontologyUrls
     * @return
     */
    private FedXEngine buildEngine(List<String> ontologyUrls) {

	return getConfiguration().//
		map(config -> FedXEngine.of(ontologyUrls, config)).//
		orElse(FedXEngine.of(ontologyUrls));
    }
}
