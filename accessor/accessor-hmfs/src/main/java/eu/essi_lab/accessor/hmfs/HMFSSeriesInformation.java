package eu.essi_lab.accessor.hmfs;

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

import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONUtils;

/**
 * @author boldrini
 */
public class HMFSSeriesInformation extends HMFSJSON {

    public HMFSSeriesInformation(String string) {
	this(new JSONObject(string));
    }

    public HMFSSeriesInformation(JSONObject json) {
	super(json);
    }

    public Integer getId() {
	return JSONUtils.getInteger(json, "series_id");
    }

    public String getString() {
	return JSONUtils.getString(json, "series_table");
    }

    public Integer getStationId() {
	return JSONUtils.getInteger(json, "estacion_id");
    }

    public Integer getVariableId() {
	return JSONUtils.getInteger(json, "var_id");
    }

    public Integer getRunId() {
	return JSONUtils.getInteger(json, "cor_id");
    }

    public List<String> getQualifiers() {
	List<String> ret = JSONUtils.getStrings(json, "qualifiers");
	if (ret.isEmpty()) {
	    String qualifier = JSONUtils.getString(json, "qualifier");
	    ret.add(qualifier);
	}
	return ret;
    }

    public String getBeginDate() {
	return JSONUtils.getString(json, "begin_date");
    }

    public String getEndDate() {
	return JSONUtils.getString(json, "end_date");
    }

    public String getCount() {
	return JSONUtils.getString(json, "count");
    }

}
