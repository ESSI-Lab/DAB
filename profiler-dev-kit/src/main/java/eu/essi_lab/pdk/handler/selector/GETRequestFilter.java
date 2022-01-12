package eu.essi_lab.pdk.handler.selector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
public class GETRequestFilter extends PathRequestFilter {

    /**
     * Enumeration of strategies to compare the given value with the supplied query string
     * 
     * @author Fabrizio
     */
    public enum InspectionStrategy {
	/**
	 * The request is accepted if the given value is equals to the request query string or path
	 */
	EXACT_MATCH,
	/**
	 * The request is accepted if the given value is not equals ignoring the case to the request query string or
	 * path
	 */
	IGNORE_CASE_EXACT_MATCH,
	/**
	 * The request is accepted if the given value is not equals to the request query string or path
	 */
	EXACT_DISCARD,
	/**
	 * The request is accepted if the given value is not equals ignoring the case to the request query string or
	 * path
	 */
	IGNORE_CASE_EXACT_DISCARD,
	/**
	 * The request is accepted if the given value is contained in the request query string or path
	 */
	LIKE_MATCH,
	/**
	 * The request is accepted if the given value is not contained in the request query string or path
	 */
	LIKE_DISCARD,
	/**
	 * The request is accepted if the given value is contained in the request query string or path, ignoring case
	 */
	IGNORE_CASE_LIKE_MATCH,
	/**
	 * The request is accepted if the given value is not contained in the request query string or path, ignoring
	 * case
	 */
	IGNORE_CASE_LIKE_DISCARD
    }

    protected HashMap<String, InspectionStrategy> queryConditions;

    protected HashMap<String, InspectionStrategy> parameterConditions;

    /**
     * Creates an undefined filter
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #addParameterCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @see #GETRequestFilter(String)
     */
    public GETRequestFilter() {

	queryConditions = new HashMap<>();
	parameterConditions = new HashMap<>();
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @param queryString a non <code>null</code> string representing the query part of a GET request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public GETRequestFilter(String queryString, InspectionStrategy strategy) {

	this();
	addQueryCondition(queryString, strategy);
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     */
    public GETRequestFilter(String path) {

	this();
	setPath(path);
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     * @param view if <code>true</code> the provided path will be accepted also if preceded by the
     *        'view/viewId/' path
     */
    public GETRequestFilter(String path, boolean view) {

	this();
	setPath(path, view);
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>. The filter accepts only
     * requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     * @param queryString a non <code>null</code> string representing the query part of a GET request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public GETRequestFilter(String path, String queryString, InspectionStrategy strategy) {

	this();
	addQueryCondition(queryString, strategy);
	setPath(path);
    }

    /**
     * Adds a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>
     * 
     * @see #accept(WebRequest)
     * @see #setPath(String)
     * @param queryString a non <code>null</code> string representing the query part of a GET request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public void addQueryCondition(String queryString, InspectionStrategy strategy) {

	queryConditions.put(queryString, strategy);
    }

    /**
     * Adds a new condition defined by the supplied <code>parameterKey</code> to compare with
     * {@link WebRequest#getQueryString()} parameters according to the given <code>strategy</code>
     * 
     * @see #accept(WebRequest)
     * @see #setPath(String)
     * @param parameterKey a non <code>null</code> string representing the mandatory key parameter of a GET request
     *        which is compared
     *        against {@link WebRequest#getQueryString()} parameters
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public void addParameterCondition(String parameterKey, InspectionStrategy strategy) {

	parameterConditions.put(parameterKey, strategy);
    }

    /**
     * Accepts or rejects the supplied <code>webRequest</code> query string according to the query conditions and/or to
     * the path
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #setPath(String)
     */
    @Override
    public boolean accept(WebRequest webRequest) throws GSException {

	if (!isSupported(webRequest)) {
	    return false;
	}

	Optional<Boolean> acceptPath = acceptPath(webRequest);

	if (acceptPath.isPresent()) {

	    if (!acceptPath.get()) {

		return false;
	    }

	    if (queryConditions.isEmpty() && acceptPath.get()) {

		return true;
	    }
	}

	Set<String> querySet = queryConditions.keySet();
	boolean validQuery = querySet.isEmpty() ? true : false;
	for (String query : querySet) {
	    if (accept(getQueryString(webRequest), query, queryConditions.get(query))) {
		validQuery = true;
		break;
	    }
	}
	if (!validQuery) {
	    return false;
	}

	Set<String> expectedKeys = parameterConditions.keySet();
	String queryString = getQueryString(webRequest);
	KeyValueParser parser = new KeyValueParser(queryString);
	for (String expectedKey : expectedKeys) {
	    boolean validKey = false;
	    for (String actualKey : parser.getParametersMap().keySet()) {

		if (accept(actualKey, expectedKey, parameterConditions.get(expectedKey))) {
		    validKey = true;
		    break;
		}
	    }
	    if (!validKey) {
		return false;
	    }
	}

	return true;
    }

    /**
     * @param request
     * @return
     */
    protected boolean isSupported(WebRequest request) {

	return request.isGetRequest();
    }

    /**
     * @param request
     * @return
     */
    protected String getQueryString(WebRequest request) throws GSException {

	return request.getQueryString();
    }

    /**
     * @param expectedString
     * @return
     */
    protected boolean accept(String actualString, String expectedString, InspectionStrategy strategy) {

	if (actualString == null) {
	    return false;
	}

	switch (strategy) {

	case EXACT_MATCH:

	    return actualString.equals(expectedString);

	case IGNORE_CASE_EXACT_MATCH:

	    return actualString.equalsIgnoreCase(expectedString);

	case EXACT_DISCARD:

	    return !actualString.equals(expectedString);

	case IGNORE_CASE_LIKE_DISCARD:

	    return !actualString.toLowerCase().contains(expectedString.toLowerCase());

	case LIKE_DISCARD:

	    return !actualString.contains(expectedString);

	case IGNORE_CASE_EXACT_DISCARD:

	    return !actualString.equalsIgnoreCase(expectedString);

	case LIKE_MATCH:

	    return actualString.contains(expectedString);

	case IGNORE_CASE_LIKE_MATCH:
	default:
	    return actualString.toLowerCase().contains(expectedString.toLowerCase());
	}
    }
}
