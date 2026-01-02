package eu.essi_lab.accessor.mch.datamodel;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class MCHObject {
    protected String readString(JSONObject json, String key) {
	if (json.has(key) && !json.isNull(key)) {
	    return json.getString(key);
	} else {
	    return null;
	}
    }

    protected Integer readInteger(JSONObject json, String key) {
	if (json.has(key) && !json.isNull(key)) {
	    return json.getInt(key);
	} else {
	    return null;
	}
    }

    protected BigDecimal readDecimal(JSONObject json, String key) {
	if (json.has(key) && !json.isNull(key)) {
	    return json.getBigDecimal(key);
	} else {
	    return null;
	}
    }

    protected Date readDate(JSONObject json, String key) {
	if (json.has(key) && !json.isNull(key)) {
	    String date = json.getString(key);
	    Optional<Date> ret = ISO8601DateTimeUtils.parseISO8601ToDate(date);
	    if (ret.isPresent()) {
		return ret.get();
	    }
	}
	return null;
    }
}
