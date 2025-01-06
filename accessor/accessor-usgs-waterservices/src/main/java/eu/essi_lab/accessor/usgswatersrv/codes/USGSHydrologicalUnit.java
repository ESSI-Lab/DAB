package eu.essi_lab.accessor.usgswatersrv.codes;

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

public class USGSHydrologicalUnit extends USGSCode {

    private static USGSHydrologicalUnit instance = null;

    public static USGSHydrologicalUnit getInstance() {
	if (instance == null) {
	    instance = new USGSHydrologicalUnit();
	}
	return instance;
    }

    private USGSHydrologicalUnit() {
	super("huc_cd");

	// values:
	// * huc_cd huc_class_cd huc_nm

    }

    @Override
    public String getRetrievalURL() {
	return null;
	// TODO: the following online list is outdated! A connector to WBD should be implemneted:
	// https://water.usgs.gov/GIS/huc.html
	// in the meantime the offline version is used
	// return "https://help.waterdata.usgs.gov/code/hucs_query?fmt=rdb";
    }

    public String getRegion(String hydrologicUnit) {
	if (hydrologicUnit == null || hydrologicUnit.isEmpty()) {
	    return "";
	}
	if (hydrologicUnit.length() < 2) {
	    return "";
	}
	return getProperties(hydrologicUnit.substring(0, 2)).get("huc_nm");
    }

    public String getSubregion(String hydrologicUnit) {
	if (hydrologicUnit == null || hydrologicUnit.isEmpty()) {
	    return "";
	}
	if (hydrologicUnit.length() < 4) {
	    return "";
	}
	return getProperties(hydrologicUnit.substring(0, 4)).get("huc_nm");

    }

    public String getAccountingUnit(String hydrologicUnit) {
	if (hydrologicUnit == null || hydrologicUnit.isEmpty()) {
	    return "";
	}
	return getProperties(hydrologicUnit.substring(0, 6)).get("huc_nm");
    }

    public String getCatalogingUnit(String hydrologicUnit) {
	if (hydrologicUnit == null || hydrologicUnit.isEmpty()) {
	    return "";
	}
	return getProperties(hydrologicUnit).get("huc_nm");
    }

    @Override
    public String getLocalResource() {
	return "usgs/huc_cd.txt";
    }

}
