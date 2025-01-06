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
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;

public class SIGEDACData {

    private JSONObject json;

    public SIGEDACData(JSONObject json) {
	this.json = json;
    }

    public String getStationName() {
	return JSONUtils.getString(json, "name");
    }

    public Integer getTotalPages() {

	Integer ret = JSONUtils.getInteger(json, "station/last_page");
	if (ret == null) {
	    return JSONUtils.getInteger(json, "meta/last_page");
	} else {
	    return ret;
	}

    }

    public List<SimpleEntry<Date, BigDecimal>> getData() {
	if (json.has("station")) {
	    JSONObject station = json.getJSONObject("station");
	    if (station.has("data")) {
		List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();
		JSONArray array = station.getJSONArray("data");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		for (int i = 0; i < array.length(); i++) {
		    JSONObject dv = array.getJSONObject(i);
		    try {
			String fecha = dv.getString("fecha");
			Date date = sdf.parse(fecha);
			String valor = dv.getString("valor");
			BigDecimal value = null;
			if (valor != null && !valor.isEmpty()) {
			    value = new BigDecimal(valor);
			}
			SimpleEntry<Date, BigDecimal> e = new SimpleEntry<>(date, value);
			ret.add(e);
		    } catch (Exception e1) {
			e1.printStackTrace();
			GSLoggerFactory.getLogger(getClass()).error(e1);
		    }
		}
		return ret;
	    }
	}
	if (json.has("data")) {
	    List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();
	    JSONArray array = json.getJSONArray("data");
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	    for (int i = 0; i < array.length(); i++) {
		JSONObject dv = array.getJSONObject(i);
		try {
		    String fecha = dv.getString("observation_date");
		    Date date = sdf.parse(fecha);
		    BigDecimal value = dv.getBigDecimal("observation_value");
		    SimpleEntry<Date, BigDecimal> e = new SimpleEntry<>(date, value);
		    ret.add(e);
		} catch (Exception e1) {
		    e1.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e1);
		}
	    }
	    return ret;

	}
	return new ArrayList<>();
    }

}
