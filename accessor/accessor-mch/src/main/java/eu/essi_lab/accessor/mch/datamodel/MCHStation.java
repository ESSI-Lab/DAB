package eu.essi_lab.accessor.mch.datamodel;

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

import org.json.JSONObject;

public class MCHStation extends MCHObject {

    private String stationId;
    private String stationName;
    private String stationName2;
    private String timeZone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal altitude;
    private String state;
    private String regManagement;
    private String catchment;
    private String subCatchment;
    private String operationalReagion;
    private String hydroRegion;
    private String rh;
    private String municipality;
    private String infrastructure;
    private String type;
    private String use;

    public MCHStation(JSONObject json) {
	stationId = readString(json, "Station");
	stationName = readString(json, "StationName");
	stationName2 = readString(json, "StationName2");
	timeZone = readString(json, "TimeZone");
	longitude = readDecimal(json, "Longitude2");
	latitude = readDecimal(json, "Latitude2");
	altitude = readDecimal(json, "Altitude");
	// "Latitude": 19389,
	// "Altitude": null,
	// "DMSlongitude": "-70\u00c2\u00b035'47''",
	// "DMSLatitude": "19\u00c2\u00b038'9''",
	state = readString(json, "Statee");
	regManagement = readString(json, "RegManagmt");
	catchment = readString(json, "Catchment");
	subCatchment = readString(json, "Subcatchment");
	operationalReagion = readString(json, "OperatnlRegion");
	hydroRegion = readString(json, "HydroReg");
	rh = readString(json, "RH");
	municipality = readString(json, "Municipality");
	// codeB=readString(json, "CodeB");
	// codeG=readString(json, "CodeG");
	// codeCB=readString(json, "CodeCB");
	// codePB=readString(json, "CodePB");
	// codeE=readString(json, "CodeE");
	// codeCL=readString(json, "CodeCL");
	// codeHG=readString(json, "CodeHG");
	// codePG=readString(json, "CodePG");
	// codeNW=readString(json, "CodeNw");
	// code1=readString(json, "Code1");
	// code2=readString(json, "Code2");
	// code3=readString(json, "Code3");
	// maxO=readString(json, "MaxOrdStrgLvl");
	// =readString(json, "MaxOrdStrgVol");
	// =readString(json, "MaxExtStrgLvl");
	// =readString(json, "MaxExtStrgVol");
	// =readString(json, "SpillwayLevel");
	// =readString(json, "SpillwayStorage");
	// =readString(json, "FreeSpillwayLevel");
	// =readString(json, "FreeSpillwayStorage");
	// =readString(json, "DeadStrgLevel");
	// =readString(json, "DeadStrgCapac");
	// =readString(json, "UsableStorageCapLev");
	// =readString(json, "UsableStorage");
	// =readString(json, "HoldingStorage");
	// =readString(json, "Key1fil");
	// =readString(json, "Key2fil");
	// =readString(json, "Key3fil");
	// =readString(json, "CritLevelSta");
	// =readString(json, "MinLevelSta");
	// =readString(json, "MaxLevelSta");
	// =readString(json, "CritFlow");
	// =readString(json, "MinDischarge");
	// =readString(json, "MaxDischarge");
	// =readString(json, "Stream");
	// =readString(json, "Distance");
	infrastructure = readString(json, "Infrastructure");
	type = readString(json, "Type");
	use = readString(json, "Usee");
    }

    public String getStationId() {
	return stationId;
    }

    public String getStationName() {
	return stationName;
    }

    public String getStationName2() {
	return stationName2;
    }

    public String getTimeZone() {
	return timeZone;
    }

    public BigDecimal getLongitude() {
	return longitude;
    }

    public BigDecimal getLatitude() {
	return latitude;
    }

    public BigDecimal getAltitude() {
	return altitude;
    }

    public String getState() {
	return state;
    }

    public String getRegManagement() {
	return regManagement;
    }

    public String getCatchment() {
	return catchment;
    }

    public String getSubCatchment() {
	return subCatchment;
    }

    public String getOperationalReagion() {
	return operationalReagion;
    }

    public String getHydroRegion() {
	return hydroRegion;
    }

    public String getRh() {
	return rh;
    }

    public String getMunicipality() {
	return municipality;
    }

    public String getInfrastructure() {
	return infrastructure;
    }

    public String getType() {
	return type;
    }

    public String getUse() {
	return use;
    }
}
