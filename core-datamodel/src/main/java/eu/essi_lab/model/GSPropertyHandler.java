package eu.essi_lab.model;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class GSPropertyHandler implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2339129380818257424L;
    private Map<String, GSProperty<?>> properties;

    public GSPropertyHandler() {

	properties = new HashMap<String, GSProperty<?>>();
    }

    /**
     * @param name
     * @param type
     * @return
     */
    public <T> T get(String name, Class<T> type) {

	GSProperty<?> prop = properties.get(name);
	if (prop != null) {
	    return type.cast(prop.getValue());
	}

	return null;
    }

    /**
     * @param mp
     * @return
     */
    public boolean add(GSProperty<?> mp) {

	return this.properties.put(mp.getName(), mp) == null;
    }

    /**
     * @param name
     */
    public void remove(String name) {

	properties.remove(name);
    }

    /**
     * @param name
     * @return
     */
    public Class<?> getType(String name) {

	GSProperty<?> prop = properties.get(name);
	if (prop != null) {
	    return prop.getValue().getClass();
	}

	return null;
    }

    /**
     * @return
     */
    public List<String> getNames() {

	return properties.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public String toString() {
	String ret = "[\n";
	if (properties != null)
	    for (String key : properties.keySet()) {
		String value = "";
		if (properties.get(key) != null) {
		    GSProperty<?> k = properties.get(key);
		    Object v = k.getValue();
		    if (v != null) {
			value = v.toString() + ";\n";
		    }
		}

		ret += key + ":" + value;
	    }
	return ret + "]";
    }
}
