package eu.essi_lab.accessor.mch.datamodel;

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

import java.util.Date;

import org.json.JSONObject;

public class MCHAvailability extends MCHObject{

    private String station;
    private String variable;
    private Integer ndata;
    private Date startDate;
    private Date endDate;
    private String codee;
    
    public MCHAvailability(JSONObject json) {
	station = readString(json, "Station");
	variable = readString(json, "Variable");
	ndata = readInteger(json, "Ndata");
	startDate = readDate(json, "StrtDate");
	if (startDate==null) {
	    startDate = readDate(json, "StartDate");
	}
	endDate = readDate(json, "EndDate");
	codee = readString(json, "Codee");

    }

    public String getStation() {
	return station;
    }

    public void setStation(String station) {
	this.station = station;
    }

    public String getVariable() {
	return variable;
    }

    public void setVariable(String variable) {
	this.variable = variable;
    }

    public Integer getNdata() {
	return ndata;
    }

    public void setNdata(Integer ndata) {
	this.ndata = ndata;
    }

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

    public String getCodee() {
	return codee;
    }

    public void setCodee(String codee) {
	this.codee = codee;
    }

}
