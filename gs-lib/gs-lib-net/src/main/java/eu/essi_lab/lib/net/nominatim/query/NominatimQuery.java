/**
 * 
 */
package eu.essi_lab.lib.net.nominatim.query;

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

import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public abstract class NominatimQuery {

    /**
     * 
     */
    private static final int DEFAULT_LIMIT = 10;

    private int limit;
    private StringBuilder builder;

    /**
     * 
     */
    public NominatimQuery() {

	setLimit(DEFAULT_LIMIT);
	builder = new StringBuilder();
    }

    /**
     * @return
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
     * @param param
     * @param value
     */
    protected void addParam(String param, String value) {

	if (StringUtils.isReadable(value)) {

	    builder.append(param);
	    builder.append("=");
	    builder.append(value);
	    builder.append("&");
	}
    }

    /**
     * @return
     */
    public String compose() {

	return builder.toString().substring(0, builder.toString().length() - 1);
    }

}
