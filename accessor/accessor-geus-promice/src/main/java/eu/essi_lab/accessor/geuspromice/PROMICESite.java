package eu.essi_lab.accessor.geuspromice;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * The content of a PROMICE / GC-Net site NetCDF file relevant for harvesting: global attributes and valid variables
 *
 * @author boldrini
 */
public class PROMICESite {

    public static final String VARIABLE_NAME = "name";
    public static final String VARIABLE_BEGIN = "begin";
    public static final String VARIABLE_END = "end";

    private final Map<String, String> globalAttributes = new LinkedHashMap<>();
    private final List<JSONObject> variables = new ArrayList<>();

    /**
     * @return
     */
    public Map<String, String> getGlobalAttributes() {

	return globalAttributes;
    }

    /**
     * @return the variable descriptions (name, attributes, begin and end of the valid values in milliseconds)
     */
    public List<JSONObject> getVariables() {

	return variables;
    }
}
