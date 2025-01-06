package eu.essi_lab.accessor.whos;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public abstract class DMHJSON {

    protected JSONObject json;

    public DMHJSON(JSONObject json) {
	this.json = json;
    }

    public BigDecimal getBigDecimal(String key) {
	if (!json.has(key)) {
	    return null;
	}
	if (json.isNull(key)) {
	    return null;
	}
	return json.getBigDecimal(key);
    }

    public Date getDate(String key) {
	if (!json.has(key)) {
	    return null;
	}
	if (json.isNull(key)) {
	    return null;
	}
	String dateString = json.getString(key).replace(" ", "T");

	Date ret = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();

	// GSLoggerFactory.getLogger(getClass()).debug("from {} to {}", dateString,
	// ISO8601DateTimeUtils.getISO8601DateTime(ret));

	return ret;
    }
}
