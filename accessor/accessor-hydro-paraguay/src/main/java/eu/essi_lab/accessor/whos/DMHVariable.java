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
import java.util.Date;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class DMHVariable extends DMHJSON {

    public DMHVariable(JSONObject json) {
	super(json);
    }

    public String getId() {
	return json.getString("id");
    }

    public String getStationId() {
	return json.getString("station_id");
    }

    public String getVariableName() {
	return json.getString("variable_name");
    }

    public Date getObservationsStart() {
	return getDate("observations_start");
    }

    public Date getObservationsEnd() {
	Date ret = getDate("observations_end");
	Date may2020 = ISO8601DateTimeUtils.parseISO8601ToDate("2022-05-01").get();
	if (ret.after(may2020)) {
	    return new Date();
	}
	return ret;
    }

    public String getUnitOfMeasure() {
	return json.getString("unit_of_measure");
    }

    public BigDecimal getAggregationPeriod() {
	return getBigDecimal("aggregation_period");
    }

    public String getAggregationPeriodUnits() {
	return json.getString("aggregation_period_units");
    }

    public String getStringValue(String key) {
	return json.getString(key);
    }

    public void set(String key, String value) {
	json.put(key, value);

    }

}
