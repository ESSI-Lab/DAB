package eu.essi_lab.accessor.usgswatersrv;

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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class USGSCounties {

    private TreeMap<String, USGSCounty> counties = new TreeMap<String, USGSCounty>();

    public TreeMap<String, USGSCounty> getCounties() {
	return counties;
    }

    public USGSCounty getCounty(String key) {
	return counties.get(key);
    }

    public USGSCounties(List<String> result) {
	for (String string : result) {
	    USGSMetadata metadata = new USGSMetadata(string);
	    String countryCode = metadata.get("country_cd");
	    String stateCode = metadata.get("state_cd");
	    String countyCode = metadata.get("county_cd");
	    String countyName = metadata.get("county_nm");
	    USGSCounty county = new USGSCounty(countryCode, stateCode, countyCode, countyName);
	    counties.put(stateCode + countyCode, county);
	}
    }

    public String getNextKey(String key) {
	String[] keys = counties.keySet().toArray(new String[] {});
	if (key == null) {
	    return keys[0];
	}
	boolean returnNext = false;
	for (int i = 0; i < keys.length; i++) {
	    String countyKey = keys[i];
	    if (returnNext) {
		return countyKey;
	    }
	    if (countyKey.equals(key)) {
		returnNext = true;
	    }
	}
	return null;
    }

    private static boolean ALLOW_REDUCTION = true;

    public void reduceCounties() {
	if (ALLOW_REDUCTION) {
	    List<String> toRemove = new ArrayList<>();
	    String[] keys = counties.keySet().toArray(new String[] {});
	    for (int i = 0; i < counties.size(); i++) {
		Integer keepOneInX = 100; // with value of 5 and limited parameter it takes 4 hours
		// with value of 3 took 3 hours
		if (i % keepOneInX == 0) {
		    // keep it
		} else {
		    toRemove.add(keys[i]);
		}
	    }
	    for (String key : toRemove) {
		counties.remove(key);
	    }
	}
    }

}
