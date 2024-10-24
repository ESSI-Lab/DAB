package eu.essi_lab.accessor.hmfs;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONUtils;

/**
 * @author boldrini
 */
public class HMFSVariable extends HMFSJSON {

    public HMFSVariable(JSONObject json) {
	super(json);
    }

    public Integer getId() {
	return JSONUtils.getInteger(json, "id");
    }

    public String getVariable() {
	return JSONUtils.getString(json, "var");
    }

    public String getName() {
	return JSONUtils.getString(json, "nombre");
    }

    public String getAbbreviation() {
	return JSONUtils.getString(json, "abrev");
    }

    public String getType() {
	return JSONUtils.getString(json, "type");
    }

    public String getDataType() {
	return JSONUtils.getString(json, "datatype");
    }

    public String getValueType() {
	return JSONUtils.getString(json, "valuetype");
    }

    public String getGeneralCategory() {
	return JSONUtils.getString(json, "GeneralCategory");
    }

    public String getVariableName() {
	return JSONUtils.getString(json, "VariableName");
    }

    public String getSampleMedium() {
	return JSONUtils.getString(json, "SampleMedium");
    }

    public String getDefUnitId() {
	return JSONUtils.getString(json, "def_unit_id");
    }

    public String getTimeSupport() {
	return JSONUtils.getString(json, "timeSupport");
    }
//    public Integer getTimeSupportYears() {
//	return JSONUtils.getInteger(json, "timeSupport/years");
//    }
//
//    public Integer getTimeSupportMonths() {
//	return JSONUtils.getInteger(json, "timeSupport/months");
//    }
//
//    public Integer getTimeSupportDays() {
//	return JSONUtils.getInteger(json, "timeSupport/days");
//    }
//
//    public Integer getTimeSupportHours() {
//	return JSONUtils.getInteger(json, "timeSupport/hours");
//    }
//
//    public Integer getTimeSupportMinutes() {
//	return JSONUtils.getInteger(json, "timeSupport/minutes");
//    }
//
//    public Integer getTimeSupportSeconds() {
//	return JSONUtils.getInteger(json, "timeSupport/seconds");
//    }
//
//    public Integer getTimeSupportMilliseconds() {
//	return JSONUtils.getInteger(json, "timeSupport/milliseconds");
//    }

}
