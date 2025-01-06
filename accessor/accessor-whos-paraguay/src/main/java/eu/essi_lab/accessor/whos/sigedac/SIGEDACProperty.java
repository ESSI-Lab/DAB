package eu.essi_lab.accessor.whos.sigedac;

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
import eu.essi_lab.lib.utils.JSONUtils;

public class SIGEDACProperty {

    private JSONObject json;

    public SIGEDACProperty(JSONObject json) {
	this.json = json;
    }

    public String getId() {
	return JSONUtils.getBigDecimal(json, "id").toString();
    }

    public String getName() {
	return JSONUtils.getString(json, "name");
    }

    public String getShortName() {
	return JSONUtils.getString(json, "short_name");
    }

    public String getUnitId() {
	BigDecimal bd = JSONUtils.getBigDecimal(json, "measure_unit_id");
	if (bd != null) {
	    return bd.toString();
	} else {
	    return null;
	}
    }

    public String getUnitCode() {
	return JSONUtils.getString(json, "measure_unit_code");
    }

    public String getUnitName() {
	return JSONUtils.getString(json, "measure_unit_name");
    }

    public String getUnitSymbol() {
	return JSONUtils.getString(json, "measure_unit_symbol");
    }

    public String getMagnitude() {
	return JSONUtils.getString(json, "measure_unit_magnitude");
    }

    public Date getObservationsStart() {
	return getDate("observations_start");
    }

    public Date getObservationsEnd() {
	Date ret = getDate("observations_end");
	// Date may2020 = ISO8601DateTimeUtils.parseISO8601ToDate("2022-05-01").get();
	// if (ret.after(may2020)) {
	// return new Date();
	// }
	return ret;
    }

    public Date getDate(String key) {
	if (!json.has(key)) {
	    return null;
	}
	if (json.isNull(key)) {
	    return null;
	}
	String dateString = json.getString(key);
	if (dateString.contains("+")) {
	    dateString = dateString.split("\\+")[0];
	}
	dateString = json.getString(key).replace(" ", "T");

	Date ret = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();

	// GSLoggerFactory.getLogger(getClass()).debug("from {} to {}", dateString,
	// ISO8601DateTimeUtils.getISO8601DateTime(ret));

	return ret;
    }

    public String getStringValue(String key) {
	return json.getString(key);
    }

    public void set(String key, String value) {
	json.put(key, value);

    }

}
