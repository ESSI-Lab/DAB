package eu.essi_lab.accessor.whos.automatic;

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

/**
 * @author boldrini
 */
public class AutomaticSystemVariable extends AutomaticSystemJSON {

    public AutomaticSystemVariable(JSONObject json) {
	super(json);
    }

    public BigDecimal getId() {
	return getBigDecimal("id");
    }

    public String getStationId() {
	return json.getString("station_id");
    }

    public String getVariableName() {
	return json.getString("name");
    }

    public Date getObservationsStart() {
	return getDate("observations_start");
    }

    public Date getObservationsEnd() {
	Date ret = getDate("observations_end");
	//Date may2020 = ISO8601DateTimeUtils.parseISO8601ToDate("2022-05-01").get();
	//if (ret.after(may2020)) {
	//    return new Date();
	//}
	return ret;
    }

    public String getUnitOfMeasure() {
	return json.getString("measure_unit_name");
    }
    
    public String getUnitOfMeasureCode() {
	return json.getString("measure_unit_code");
    }
    
    public String getUnitOfMeasureSymbol() {
	return json.getString("measure_unit_symbol");
    }

//    public BigDecimal getAggregationPeriod() {
//	return getBigDecimal("aggregation_period");
//    }
//
//    public String getAggregationPeriodUnits() {
//	return json.getString("aggregation_period_units");
//    }

    public String getStringValue(String key) {
	return json.getString(key);
    }

    public void set(String key, String value) {
	json.put(key, value);

    }

}
