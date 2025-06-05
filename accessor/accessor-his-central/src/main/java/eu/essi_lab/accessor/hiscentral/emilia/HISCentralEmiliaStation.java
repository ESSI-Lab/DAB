package eu.essi_lab.accessor.hiscentral.emilia;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class HISCentralEmiliaStation {

    
    /**
     *  Emilia JSON example: main fields:
     *  B01019: station name
     *  B01194: station type
     *  B05001: latitude
     *  B06001: longitude
     *  B07030: HEIGHT OF STATION GROUND ABOVE MEAN SEA LEVEL
     * 	B07031: HEIGHT OF BAROMETER ABOVE MEAN SEA LEVEL
         
         {
         	"B01019": {"v": "Castelfranco Emilia"},
                "B01194": {"v": "agrmet"},
                "B04001": {"v": 2023},
                "B04002": {"v": 11},
                "B04003": {"v": 9},
                "B04004": {"v": 3},
                "B04005": {"v": 45},
                "B04006": {"v": 0},
                "B05001": {"v": 44.63005},
                "B06001": {"v": 11.02746},
                "B07030": {"v": 32.0},
                "B07031": {"v": 33.0}
          }
     */
    
    
    protected JSONObject json;
    protected List<HISCentralEmiliaVariable> variables;
    
    protected Date startDate;
    protected Date endDate;
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public HISCentralEmiliaStation(JSONObject json) {
	this.json = json;
	variables = new ArrayList<>();
    }
    
    public void addVariable(HISCentralEmiliaVariable var) {
	this.variables.add(var);
        //this.variables.add(var);
    }

    
    public String getName() {
	String name = null;
	JSONObject objName = json.optJSONObject("B01019");
	if(objName != null) {
	    name = objName.optString("v");
	}
	    
	return name;
    }
    
    public String getType() {
	String type = null;
	JSONObject objName = json.optJSONObject("B01194");
	if(objName != null) {
	    type = objName.optString("v");
	}
	    
	return type;
    }

    public BigDecimal getAltitude() {
	BigDecimal alt = null;
	JSONObject objName = json.optJSONObject("B07030");
	if(objName != null) {
	    alt = objName.getBigDecimal("v");
	}
	return alt;
    }

    public BigDecimal getLatitude() {
	BigDecimal lat = null; 
	JSONObject objName = json.optJSONObject("B05001");
	if(objName != null) {
	    lat = objName.getBigDecimal("v");
	}
	return lat;
    }

    public BigDecimal getLongitude() {
	BigDecimal lon = null; 
	JSONObject objName = json.optJSONObject("B06001");
	if(objName != null) {
	    lon = objName.getBigDecimal("v");
	}
	return lon;
    }
    
    public List<HISCentralEmiliaVariable> getVariables(){
	return variables;
    }
    
    public JSONObject getJSON() {
	return json;
    }
    
    public BigDecimal getBigDecimal(String key) {
	if (!json.has(key)) {
	    return null;
	}
	if (json.isNull(key)) {
	    return null;
	}
	return json.getBigDecimal(key);
    }
    
}
