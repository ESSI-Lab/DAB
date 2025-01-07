package eu.essi_lab.lib.utils;

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

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The KVP Mangler is used to mangle a set of (Key-Value pairs) parameters to form a single string in the form:
 * KEY1 sep VALUE1 sep ... sep KEYN sep VALUEN, where sep is a custom defined separator
 * 
 * @author boldrini
 */
public class KVPMangler {

    private SortedMap<String, String> map = new TreeMap<>();

    private String separator;

    public String getSeparator() {
        return separator;
    }

    /**
     * Initializes with the given separator
     * 
     * @param mangling
     * @param separator
     */
    public KVPMangler(String separator) {
	if (separator == null || separator.isEmpty()) {
	    throw new RuntimeException("Not empty separator is needed");
	}
	this.separator = separator;

    }

    /**
     * Sets the given mangling
     * 
     * @param mangling
     */
    public void setMangling(String mangling) {
	String[] split = mangling.split(separator);
	for (int i = 0; (i + 1) < split.length; i += 2) {
	    String key = split[i];
	    String value = split[i + 1];
	    map.put(key, value);
	}
    }

    /**
     * Gets a mangling string out of the state of this mangler
     * 
     * @return
     */
    public String getMangling() {
	Set<String> keySet = map.keySet();
	String ret = "";
	for (String key : keySet) {
	    String value = map.get(key);
	    ret += key + separator + value + separator;
	}
	if (!ret.isEmpty()) {
	    ret = ret.substring(0, ret.lastIndexOf(separator));
	}
	return ret;
    }

    /**
     * Sets the given parameter, identified by its key and value
     * 
     * @param key
     * @param value
     */
    public void setParameter(String key, String value) {
	map.put(key, value);
    }

    /**
     * Get the value of the parameter identified by the key
     * 
     * @param key
     * @return
     */
    public String getParameterValue(String key) {
	return map.get(key);
    }

}
