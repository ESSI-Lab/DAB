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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONUtils;

public class SIGEDACStation {

    private JSONObject json;

    public SIGEDACStation(JSONObject json) {
	this.json = json;
    }
    
    public String getId() {
	return JSONUtils.getBigDecimal(json, "id").toString();
    }
    
    public String getCode() {
	return JSONUtils.getString(json, "code");
    }
    
    public String getWMOCode() {
	return JSONUtils.getString(json, "wmo_code");
    }
    
    public String getName() {
	return JSONUtils.getString(json, "name");
    }
    
    public BigDecimal getLatitude() {
	return JSONUtils.getBigDecimal(json, "latitude");
    }
    
    public BigDecimal getLongitude() {
	return JSONUtils.getBigDecimal(json, "longitude");
    }
    
    public BigDecimal getElevation() {
	return JSONUtils.getBigDecimal(json, "elevation");
    }
    
    public Boolean isActive() {
	return JSONUtils.getBoolean(json, "active");
    }
    
    public String getCityId() {
	return JSONUtils.getBigDecimal(json, "city_id").toString();
    }
    
    public String getCityName() {
	return JSONUtils.getString(json, "city_name");
    }
    
    public String getStationType() {
	return JSONUtils.getString(json, "station_type_name");
    }

}
