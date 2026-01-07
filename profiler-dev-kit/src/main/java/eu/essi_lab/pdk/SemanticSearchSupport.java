package eu.essi_lab.pdk;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ontology.*;
import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsFinder;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class SemanticSearchSupport {

    public static final String ONTOLOGY_IDS_PARAM = "ontologyIds";
    public static final String ATTRIBUTE_TITLE_PARAM = "attributeTitle";
    public static final String SEMANTIC_SEARCH_PARAM = "semanticSearch";

    private static final String EXPANSION_LEVEL_PARAM = "expansionLevel";
    private static final String EXPANSION_LIMIT_PARAM = "expansionLimit";
    private static final String SEMANTIC_RELATIONS_PARAM = "semanticRelations";
    private static final String SEARCH_LANGS_PARAM = "searchLangs";
    private static final String SOURCE_LANGS_PARAM = "sourceLangs";

    /**
     * @param value
     * @param withObsPropURIs
     * @param searchFields
     * @param semantics
     * @param ontology
     * @return
     */
    public Optional<Bond> getSemanticBond(//
	    KeyValueParser parser, //
	    String value, //
	    String ontologyIds, //
	    String searchFields, //
	    boolean withObsPropURIs) {

	SKOSClient client = new SKOSClient();

	client.setSearchValue(value);

	List<String> ids = Arrays.asList(ontologyIds.split(","));

	client.setOntologyUrls(//
		ConfigurationWrapper.getOntologySettings().//
			stream().//
			filter(set -> ids.contains(set.getOntologyId())).//
			map(OntologySetting::getOntologyEndpoint).//
			toList());

	DefaultSemanticSearchSetting setting = ConfigurationWrapper.getDefaultSemanticSearchSetting();

	ExpansionLevel expansionLevel = parser.getOptionalValue(EXPANSION_LEVEL_PARAM).//
		map(v -> ExpansionLevel.of(Integer.parseInt(v)).orElse(setting.getDefaultExpansionLevel())).//
		orElse(setting.getDefaultExpansionLevel());

	client.setExpansionLevel(expansionLevel);

	ExpansionLimit expansionLimit = parser.getOptionalValue(EXPANSION_LIMIT_PARAM).//
		map(v -> ExpansionLimit.of(v).orElse(setting.getDefaultExpansionLimit())).//
		orElse(setting.getDefaultExpansionLimit());

	client.setExpansionLimit(expansionLimit);

	List<SKOSSemanticRelation> semanticRelations = parser.getOptionalValue(SEMANTIC_RELATIONS_PARAM).//
		stream().//
		flatMap(v -> Arrays.stream(v.split(","))).//
		flatMap(v -> LabeledEnum.valueOf(SKOSSemanticRelation.class, v).stream()).toList();

	client.setExpansionsRelations(semanticRelations.isEmpty() ? setting.getDefaultSemanticRelations() : semanticRelations);

	List<String> searchLangs = parser.getOptionalValue(SEARCH_LANGS_PARAM).stream().//
		flatMap(v -> Arrays.stream(v.split(","))).toList();

	client.setSearchLangs(
		searchLangs.isEmpty() ? setting.getDefaultSearchLanguages().stream().map(EuropeanLanguage::getLabel).toList() : searchLangs);

	List<String> sourceLangs = parser.getOptionalValue(SOURCE_LANGS_PARAM).stream().//
		flatMap(v -> Arrays.stream(v.split(","))).toList();

	client.setSourceLangs(
		sourceLangs.isEmpty() ? setting.getDefaultSourceLanguages().stream().map(EuropeanLanguage::getLabel).toList() : sourceLangs);

	//
	// finder
	//

	DefaultConceptsFinder finder = new DefaultConceptsFinder();
	finder.setTraceQuery(false);
	finder.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));// default
	// default 1 second
	finder.setTaskConsumer((task) -> task.setMaxExecutionTime(setting.getDefaultMaxExecutionTime()));

	client.setFinder(finder);
	
	//
	// expander
	//

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	expander.getQueryBuilder().setIncludeNoLanguageConcepts(false); // default
	expander.setTraceQuery(false);
	// default, 4 threads per level
	expander.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
	// default 1 second
	expander.setTaskConsumer((task) -> task.setMaxExecutionTime(setting.getDefaultMaxExecutionTime()));
	client.setExpander(expander);

	//
	//
	//

	try {

	    SKOSResponse response = client.search();

	    List<String> expandedTerms = response.getLabels();

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} expanded terms: \n{}", expandedTerms.size(),
		    expandedTerms.toString().replace("[", "").replace("]", ""));

	    List<Bond> expandedBonds = new ArrayList<>();

	    if (withObsPropURIs) {

		Set<String> uris = response.getURIs();

		uris.forEach(uri -> expandedBonds
			.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.OBSERVED_PROPERTY_URI, uri)));
	    } else {

		expandedTerms.forEach(term -> BondUtils.createFieldsBond(searchFields, term).ifPresent(expandedBonds::add));
	    }

	    if (client.getSearchTarget().get() == SearchTarget.TERMS && setting.isOriginalTermIncluded()) {

		// adds the initial search term/concept
		expandedBonds.add(BondUtils.createFieldsBond(searchFields, value).get());
	    }

	    return BondFactory.aggregate(expandedBonds, LogicalOperator.OR);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return Optional.empty();
	}
    }

    /**
     * @param request
     * @param value
     * @param ontologyIds
     * @param searchFields
     * @param withObsPropURIs
     * @return
     */
    public Optional<Bond> getSemanticBond(//
	    WebRequest request, //
	    String value, //
	    String ontologyIds, //
	    String searchFields, //
	    boolean withObsPropURIs) {

	return getSemanticBond(new KeyValueParser(request.getOptionalQueryString().get()), value, ontologyIds, searchFields,
		withObsPropURIs);
    }

}
