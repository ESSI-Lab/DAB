package eu.essi_lab.lib.sensorthings._1_1.client.request.options;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

import eu.essi_lab.lib.sensorthings._1_1.client.request.Composable;

/**
 * @author Fabrizio
 */
public class SystemQueryOptions implements Composable {

    /**
    * 
    */
    private ExpandOption expandOption;
    /**
    * 
    */
    private SelectOption selectOption;
    /**
     * 
     */
    private Integer top;
    /**
     * 
     */
    private Integer skip;
    /**
     * 
     */
    private boolean count;
    /**
     * 
     */
    private String filter;

    /**
     * 
     */
    private String orderBy;

    /**
     * 
     */
    private SystemQueryOptions() {

    }

    /**
     * @return
     */
    public static SystemQueryOptions get() {

	return new SystemQueryOptions();
    }

    /**
     * @param option
     * @return
     */
    public SystemQueryOptions expand(ExpandOption option) {

	this.expandOption = option;

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#select4
     * @param properties
     */
    public SystemQueryOptions select(SelectOption option) {

	this.selectOption = option;

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#top
     * @param properties
     */
    public SystemQueryOptions top(int top) {

	if (top < 0) {

	    throw new IllegalArgumentException("Top value must be non negative");
	}

	this.top = top;

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#skip
     * @param properties
     */
    public SystemQueryOptions skip(int skip) {

	if (skip < 0) {

	    throw new IllegalArgumentException("Skip value must be non negative");
	}

	this.skip = skip;

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#orderby
     * @param orderBy
     */
    public SystemQueryOptions orderBy(String orderBy) {

	this.orderBy = orderBy;

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#count
     * @param properties
     */
    public SystemQueryOptions count() {

	this.count = true;

	return this;
    }

    /**
     * Only applicable to a collection of entities
     * 
     * @see https://docs.ogc.org/is/18-088/18-088.html#filter
     * @param filter
     */
    public SystemQueryOptions filter(String filter) {

	this.filter = filter;

	return this;
    }

    /**
     * @return the expandOption
     */
    public Optional<ExpandOption> getExpandOptions() {

	return Optional.ofNullable(expandOption);
    }

    /**
     * @return
     */
    public Optional<SelectOption> getSelectOption() {

	return Optional.ofNullable(selectOption);
    }

    /**
     * @return
     */
    public Optional<Integer> getTop() {

	return Optional.ofNullable(top);
    }

    /**
     * @return
     */
    public Optional<Integer> getSkip() {

	return Optional.ofNullable(skip);
    }

    /**
     * @return
     */
    public boolean isCountSet() {

	return count;
    }

    /**
     * @return
     */
    public Optional<String> getFilter() {

	return Optional.ofNullable(filter);
    }

    /**
     * @return
     */
    public Optional<String> getOrderBy() {

	return Optional.ofNullable(orderBy);
    }

    @Override
    public String compose() throws IllegalArgumentException {

	StringBuilder builder = new StringBuilder();

	if (filter != null) {

	    builder.append("&$filter=");
	    try {
		builder.append(URLEncoder.encode(filter, "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	    }
	}

	if (selectOption != null) {

	    builder.append(selectOption.compose());
	}

	if (expandOption != null) {

	    builder.append(expandOption.compose());
	}

	if (top != null) {

	    builder.append("&$top=");
	    builder.append(top);
	}

	if (skip != null) {

	    builder.append("&$skip=");
	    builder.append(skip);
	}

	if (orderBy != null) {

	    builder.append("&$orderby=");
	    try {
		builder.append(URLEncoder.encode(orderBy, "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	    }
	}

	if (count) {

	    builder.append("&$count=true");
	}

	return builder.toString().substring(1, builder.toString().length());
    }
    
    @Override
    public String toString(){
	
	return compose();
    }
}
