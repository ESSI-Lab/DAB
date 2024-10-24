package eu.essi_lab.eiffel.api;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public interface EiffelAPI {

    /**
     * @author Fabrizio
     */
    public enum SearchIdentifiersApi {

	/**
	 * Uses the 'filter' API to retrieve with a single non paginated query, a maximum of 10.000 identifiers matching
	 * the given search terms
	 */
	FILTER,

	/**
	 * Uses the 'search' API to retrieve in one or more paginated queries, a non defined maximum of identifiers
	 * matching the given search terms
	 */
	SEARCH;
    }

    /**
     * The default value of the maximum number of identifiers that can be sorted with the 'sort' API
     */
    public static final int DEFAULT_MAX_SORT_IDENTIFIERS = 10000;

    /**
     * This value set the 'threshold' parameter of the filter API
     */
    public static final double DEFAULT_FILTER_TRESHOLD = 0.7;

    /**
     * This value set the 'recordsPerPage' parameter of the search API
     */
    public static final int DEFAULT_MAX_RECORDS_PER_PAGE = 100;

    /**
     * This value set the 'minScore' parameter of the search API
     */
    public static final int DEFAULT_MIN_SCORE = 0;

    /**
     * This value set the 'termsSignificance' parameter of the search API
     */
    public static final String DEFAULT_TERMS_SIGNIFICANCE = "";

    /**
     * This value set the 'queryMethod' parameter of the search API
     */
    public static final String DEFAULT_SERACH_QUERY_METHOD = "exact";

    /**
     * @return
     */
    default double getFilterTreshold() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	double value = DEFAULT_FILTER_TRESHOLD;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty("eiffelAPIFilterTreshold");

	    if (propValue != null) {

		value = Double.valueOf(propValue);
	    }
	}

	return value;
    }

    /**
     * @return
     */
    default double getMinScore() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	double value = DEFAULT_MIN_SCORE;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty("eiffelAPISearchMinScore");

	    if (propValue != null) {

		value = Double.valueOf(propValue);
	    }
	}

	return value;
    }

    /**
     * @return
     */
    default String getSearchQueryMethod() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	String value = DEFAULT_SERACH_QUERY_METHOD;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty("eiffelAPISearchQueryMethod");

	    if (propValue != null) {

		value = propValue;
	    }
	}

	return value;
    }

    /**
     * @return
     */
    default String getTermsSignificance() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	String value = DEFAULT_TERMS_SIGNIFICANCE;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty("eiffelAPISearchTermsSignificance");

	    if (propValue != null && !propValue.equals("none")) {

		value = propValue;
	    }
	}

	return value;
    }

    /**
     * @author Fabrizio
     */
    public class SearchTermsFinderParser implements DiscoveryBondHandler {

	private Optional<String> searchTerms;

	/**
	 * 
	 */
	public SearchTermsFinderParser() {

	    searchTerms = Optional.empty();
	}

	/**
	 * @return the searchTerms
	 */
	public Optional<String> getSearchTerms() {

	    return searchTerms;
	}

	@Override
	public void startLogicalBond(LogicalBond bond) {
	}

	@Override
	public void separator() {
	}

	@Override
	public void nonLogicalBond(Bond bond) {
	}

	@Override
	public void endLogicalBond(LogicalBond bond) {
	}

	@Override
	public void viewBond(ViewBond bond) {
	}

	@Override
	public void spatialBond(SpatialBond bond) {
	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {

	    if (bond.getProperty() == MetadataElement.KEYWORD //
		    || bond.getProperty() == MetadataElement.TITLE //
		    || bond.getProperty() == MetadataElement.ABSTRACT //
	    ) {

		searchTerms = Optional.of(bond.getPropertyValue());
	    }
	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {
	}

	@Override
	public void customBond(QueryableBond<String> bond) {
	}
    }

    /**
     * Sort and filter: sorted by the Eiffel search API according to the given <code>searchTerms</code>, and filtered by
     * DAB according to a merged query
     * 
     * @param searchTerms
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public SimpleEntry<List<String>, Integer> searchIdentifiers(SearchIdentifiersApi mode, String searchTerms, int start, int count)
	    throws GSException;
    
    /**
     * Sort and filter: sorted by the Eiffel search API according to the given <code>searchTerms</code>, and filtered by
     * DAB according to a merged query
     * 
     * @param searchTerms
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public SimpleEntry<List<String>, Integer> searchIdentifiers(SearchIdentifiersApi mode, String searchTerms, String bbox, int start, int count)
	    throws GSException;


    /**
     * Filter and sort: filtered by DAB according to the original user query constraints, sorted by the Eiffel sort API
     * according only to the given <code>searchTerms</code>
     * 
     * @param message
     * @param identifiers
     * @return
     */
    public List<String> sortIdentifiers(String searchTerms, List<String> identifiers) throws GSException;

}
