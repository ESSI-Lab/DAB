package eu.essi_lab.pdk;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ontology.DefaultSemanticSearchSetting;
import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsFinder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.lib.utils.ThreadMode;
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

    private String expansionLevelParam;
    private String expansionLimitParam;
    private String relationsParam;
    private String searchLangsParam;
    private String sourceLangsParam;

    /**
     * @return
     */
    public String getExpansionLevelParam() {

	return expansionLevelParam;
    }

    /**
     * @param expansionLevelParam
     */
    public void setExpansionLevelParam(String expansionLevelParam) {

	this.expansionLevelParam = expansionLevelParam;
    }

    /**
     * @return
     */
    public String getExpansionLimitParam() {

	return expansionLimitParam;
    }

    /**
     * @param expansionLimitParam
     */
    public void setExpansionLimitParam(String expansionLimitParam) {

	this.expansionLimitParam = expansionLimitParam;
    }

    /**
     * @return
     */
    public String getRelationsParam() {

	return relationsParam;
    }

    /**
     * @param relationsParam
     */
    public void setRelationsParam(String relationsParam) {

	this.relationsParam = relationsParam;
    }

    /**
     * @return
     */
    public String getSearchLangsParam() {

	return searchLangsParam;
    }

    /**
     * @param searchLangsParam
     */
    public void setSearchLangsParam(String searchLangsParam) {

	this.searchLangsParam = searchLangsParam;
    }

    /**
     * @return
     */
    public String getSourceLangsParam() {

	return sourceLangsParam;
    }

    /**
     * @param sourceLangsParam
     */
    public void setSourceLangsParam(String sourceLangsParam) {

	this.sourceLangsParam = sourceLangsParam;
    }

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
			map(set -> set.getOntologyEndpoint()).//
			toList());

	DefaultSemanticSearchSetting setting = ConfigurationWrapper.getSystemSettings().getDefaultSemanticSearchSetting();

	ExpansionLevel expansionLevel = parser.getOptionalValue(getExpansionLevelParam()).//
		map(v -> ExpansionLevel.of(Integer.valueOf(v)).orElse(setting.getDefaultExpansionLevel())).//
		orElse(setting.getDefaultExpansionLevel());

	client.setExpansionLevel(expansionLevel);

	ExpansionLimit expansionLimit = parser.getOptionalValue(getExpansionLimitParam()).//
		map(v -> ExpansionLimit.of(v).orElse(setting.getDefaultExpansionLimit())).//
		orElse(setting.getDefaultExpansionLimit());

	client.setExpansionLimit(expansionLimit);

	List<SKOSSemanticRelation> semanticRelations = parser.getOptionalValue(getRelationsParam()).//
		stream().//
		flatMap(v -> Arrays.asList(v.split(",")).stream()).//
		flatMap(v -> LabeledEnum.valueOf(SKOSSemanticRelation.class, v).stream()).toList();

	client.setExpansionsRelations(semanticRelations.isEmpty() ? setting.getDefaultSemanticRelations() : semanticRelations);

	List<String> searchLangs = parser.getOptionalValue(getSearchLangsParam()).stream().//
		flatMap(v -> Arrays.asList(v.split(",")).stream()).toList();

	client.setSearchLangs(
		searchLangs.isEmpty() ? setting.getDefaultSearchLanguages().stream().map(l -> l.getLabel()).toList() : searchLangs);

	List<String> sourceLangs = parser.getOptionalValue(getSourceLangsParam()).stream().//
		flatMap(v -> Arrays.asList(v.split(",")).stream()).toList();

	client.setSourceLangs(
		sourceLangs.isEmpty() ? setting.getDefaultSourceLanguages().stream().map(l -> l.getLabel()).toList() : sourceLangs);

	//
	//
	//

	DefaultConceptsFinder finder = new DefaultConceptsFinder();
	finder.setTraceQuery(false);
	finder.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
	// finder.setTaskConsumer((task) -> System.out.println(task));

	client.setFinder(finder);

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	expander.getQueryBuilder().setIncludeNoLanguageConcepts(false); // default
	expander.setTraceQuery(false);
	expander.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4))); // 4 threads per level
	// expander.setTaskConsumer((task) -> System.out.println(task));

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

		expandedTerms.forEach(term -> BondUtils.createFieldsBond(searchFields, term).ifPresent(bond -> expandedBonds.add(bond)));
	    }

	    if (client.getSearchTarget().get() == SearchTarget.TERMS) {

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
