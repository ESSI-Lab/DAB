package eu.essi_lab.accessor.nrfa;

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

import java.util.Date;

import javax.xml.datatype.Duration;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class ParameterInfo {

    private JSONObject json;

    public JSONObject getJSON() {
	return json;
    }

    public void setJSON(JSONObject json) {
	this.json = json;
    }

    public ParameterInfo(JSONObject json) {
	this.json = json;
    }

    public String getIdentifier() {
	return json.getString("data-type");

    }

    public String getName() {
	return json.getString("parameter");
    }

    public String getUnits() {

	return json.getString("units");
    }

    public Duration getSamplingPeriod() {

	String period = json.getString("period");
	Duration samplingPeriod = ISO8601DateTimeUtils.getDuration(period);
	return samplingPeriod;
    }

    public Date getBegin() {

	String first = json.getString("first");
	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(first).get();
	return begin;
    }

    public Date getEnd() {
	String last = json.getString("last");
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(last).get();
	return end;
    }

}
