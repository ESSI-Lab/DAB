package eu.essi_lab.accessor.whos;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class MWRIStation extends MWRIJSON {

    public MWRIStation(JSONObject json) {
	super(json);
    }

    public String getName() {
	return json.getString("sitename");
    }

    public BigDecimal getLongitude() {
	return new BigDecimal(json.getString("longitude"));
    }

    public BigDecimal getLatitude() {
	return new BigDecimal(json.getString("latitude"));
    }

    public Date getDate() {
	String hour = "" + json.getInt("hour");
	if (hour.length() == 1) {
	    hour = "0" + hour;
	}
	String iso = json.getString("date") + "T" + hour + ":00:00Z";
	return ISO8601DateTimeUtils.parseISO8601ToDate(iso).get();
    }
    
    public BigDecimal getWaterLevelUpstream() {
	return getBigDecimal("water_level_upstream");
    }
    
    public BigDecimal getWaterLevelDownstream() {
	return getBigDecimal("water_level_downstream");
    }

  

}
