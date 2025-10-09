/**
 * 
 */
package eu.essi_lab.lib.skoss;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.lib.skoss.expander.ConceptsExpander;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;

/**
 * @author Fabrizio
 */
public class SKOSClient {

    private static final int DEFAULT_LIMIT = 10;
    private static final ExpansionLevel DEFAULT_EXPANSION_LEVEL = ExpansionLevel.LOW;
    private static final List<SKOSSemanticRelation> DEFAULT_RELATIONS = Arrays.asList(SKOSSemanticRelation.NARROWER,
	    SKOSSemanticRelation.RELATED);

    private static final List<String> DEFAULT_SEARCH_LANGS = Arrays.asList("it", "en");
    private static final List<String> DEFAULT_SOURCE_LANGS = Arrays.asList("it", "en");

    //
    //
    //

    private String searchTerm;
    private List<String> sourceLangs;
    private List<String> searchLangs;
    private List<String> ontologyUrls;
    private ExpansionLevel expansionLevel;
    private List<SKOSSemanticRelation> expansionsRelations;
    private int limit;

    //
    //
    //

    private ConceptsFinder finder;
    private ConceptsExpander expander;

    /**
     * 
     */
    public SKOSClient() {

	setLimit(DEFAULT_LIMIT);
	setExpansionLevel(DEFAULT_EXPANSION_LEVEL);
	setExpansionsRelations(DEFAULT_RELATIONS);
	setSearchLangs(DEFAULT_SEARCH_LANGS);
	setSourceLangs(DEFAULT_SOURCE_LANGS);

	setFinder(new FedXConceptsFinder());
	setExpander(new FedXConceptsExpander());
    }

    /**
     * @return the searchTerm
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
     * @return the sourceLangs
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
     * @return the searchLangs
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
     * @return the ontologyUrls
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
     * @return the expansionLevel
     */
    public ExpansionLevel getExpansionLevel() {
	return expansionLevel;
    }

    /**
     * @param expansionLevel
     */
    public void setExpansionLevel(ExpansionLevel expansionLevel) {
	this.expansionLevel = expansionLevel;
    }

    /**
     * @return the expansionsRelations
     */
    public List<SKOSSemanticRelation> getExpansionsRelations() {
	return expansionsRelations;
    }

    /**
     * @param expansionsRelations
     */
    public void setExpansionsRelations(List<SKOSSemanticRelation> expansionsRelations) {
	this.expansionsRelations = expansionsRelations;
    }

    /**
     * @return the limit
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
     * @return the finder
     */
    public ConceptsFinder getFinder() {
	return finder;
    }

    /**
     * @param finder
     */
    public void setFinder(ConceptsFinder finder) {
	this.finder = finder;
    }

    /**
     * @return the expander
     */
    public ConceptsExpander getExpander() {
	return expander;
    }

    /**
     * @param expander
     */
    public void setExpander(ConceptsExpander expander) {
	this.expander = expander;
    }

    /**
     * @return
     * @throws Exception
     */
    public SKOSResponse search() throws Exception {

	List<String> concepts = getFinder().find(//
		getSearchTerm(), //
		getOntologyUrls(), //
		getSourceLangs());

	SKOSResponse response = getExpander().expand(//
		concepts, //
		getOntologyUrls(), //
		getSourceLangs(), //
		getSearchLangs(), //
		getExpansionsRelations(), //
		getExpansionLevel(), //
		getLimit());//

	return response;
    }

}
