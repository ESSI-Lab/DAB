package eu.essi_lab.accessor.hmfs;

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

import java.math.BigDecimal;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONUtils;

/**
 * @author boldrini
 */
public class HMFSStation extends HMFSJSON {

    public HMFSStation(JSONObject json) {
	super(json);
    }

    public Integer getId() {
	return JSONUtils.getInteger(json, "id");
    }

    public BigDecimal getLatitude() {
	if (json.has("geom")) {
	    return JSONUtils.getBigDecimal(json, "geom/coordinates[1]");
	} else if (json.has("exutorio")) {
	    return JSONUtils.getBigDecimal(json, "exutorio/coordinates[1]");
	} else {
	    return null;
	}
    }

    public BigDecimal getLongitude() {
	if (json.has("geom")) {
	    return JSONUtils.getBigDecimal(json, "geom/coordinates[0]");
	} else if (json.has("exutorio")) {
	    return JSONUtils.getBigDecimal(json, "exutorio/coordinates[0]");
	} else {
	    return null;
	}
    }

}
