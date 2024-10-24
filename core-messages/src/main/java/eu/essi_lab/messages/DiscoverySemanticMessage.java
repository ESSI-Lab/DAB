/**
 * 
 */
package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.messages.sem.SemanticSearch;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;

/**
 * @author Fabrizio
 */
public class DiscoverySemanticMessage extends DiscoveryMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -7565049436947423137L;
    private static final String QUERY_EXPANSION_POLICY = "QUERY_EXPANSION_POLICY";
    private static final String EXPANSION_SEARCH_TERMS_QUERYABLES = "EXPANSION_SEARCH_TERMS_QUERYABLES";

    private static final String EXPANSION_POLICY = "EXPANSION_POLICY";
    private static final String DISCOVERY_QUERYABLES = "DISCOVERY_QUERYABLES";
    private static final String TERMS_TO_EXPAND = "TERMS_TO_EXPAND";
    private static final String SCHEME = "SCHEME";

    /**
     * @author Fabrizio
     */
    public enum ExpansionPolicy {

	/**
	 * 
	 */
	SEARCH,

	/**
	 * 
	 */
	SEARCH_AND_EXPAND,

	/**
	 * 
	 */
	SEARCH_AND_COLLAPSE
    }

    /**
     * 
     */
    public DiscoverySemanticMessage() {

	setExpansionPolicy(ExpansionPolicy.SEARCH_AND_EXPAND);
    }

    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = super.provideInfo();

	ExpansionPolicy expansionPolicy = getExpansionPolicy();
	map.put(EXPANSION_POLICY, Arrays.asList(expansionPolicy.toString()));

	List<Queryable> discoveryQueryables = getDiscoveryQueryables();
	map.put(DISCOVERY_QUERYABLES, discoveryQueryables.stream().map(q -> q.getName()).collect(Collectors.toList()));

	List<String> termsToExpand = getTermsToExpand();
	map.put(TERMS_TO_EXPAND, termsToExpand);

	GSKnowledgeScheme scheme = getScheme();
	map.put(SCHEME, Arrays.asList(scheme.getNamespace()));

	return map;
    }

    @Override
    public String getName() {

	return "DISCOVERY_SEMANTIC_MESSAGE";
    }

    /**
     * @param policy
     */
    public void setExpansionPolicy(ExpansionPolicy policy) {

	getHeader().add(new GSProperty<ExpansionPolicy>(QUERY_EXPANSION_POLICY, policy));
    }

    /**
     * @return
     */
    public ExpansionPolicy getExpansionPolicy() {

	return getHeader().get(QUERY_EXPANSION_POLICY, ExpansionPolicy.class);
    }

    /**
     * Set the queryables to use as target in the discovery query for the expanded search terms provided as result of
     * the semantic expansion
     * 
     * @param queryables
     */
    public void setDiscoveryQueryables(List<Queryable> queryables) {

	getHeader().add(new GSProperty<List<Queryable>>(EXPANSION_SEARCH_TERMS_QUERYABLES, queryables));
    }

    /**
     * Get the queryables to use as target in the discovery query for the expanded search terms provided as result of
     * the semantic expansion
     */
    @SuppressWarnings("unchecked")
    public List<Queryable> getDiscoveryQueryables() {

	return getHeader().get(EXPANSION_SEARCH_TERMS_QUERYABLES, List.class);
    }

    /**
     * Set the search terms to expand according to the {@link ExpansionPolicy} set with
     * {@link #setExpansionPolicy(ExpansionPolicy)}
     * 
     * @see SemanticSearch#setSearchTerms(List)
     * @param searchTerms
     */
    public void setTermsToExpand(List<String> searchTerms) {

	getHeader().add(new GSProperty<List<String>>(TERMS_TO_EXPAND, searchTerms));
    }

    /**
     * Get the search fields to expand according to the {@link ExpansionPolicy} set with
     * {@link #setExpansionPolicy(ExpansionPolicy)}
     */
    @SuppressWarnings("unchecked")
    public List<String> getTermsToExpand() {

	return getHeader().get(TERMS_TO_EXPAND, List.class);
    }

    /**
     * @param scheme
     * @return
     */
    public void setScheme(GSKnowledgeScheme scheme) {

	getHeader().add(new GSProperty<GSKnowledgeScheme>(SCHEME, scheme));
    }

    /**
     * @return
     */
    public GSKnowledgeScheme getScheme() {

	return getHeader().get(SCHEME, GSKnowledgeScheme.class);
    }
}
