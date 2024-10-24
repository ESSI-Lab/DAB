/**
 * 
 */
package eu.essi_lab.messages.stats;

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

import java.io.InputStream;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public class RegionsManager {

    /**
     * @param bbox
     * @return
     */
    public static Optional<String> getLocation(String bbox) {

	return StreamUtils
		.iteratorToStream(//
			getRegions().iterator())
		.//
		map(o -> (JSONObject) o).//
		filter(o -> {

		    String b = o.get("south").toString();
		    b += "," + o.get("west").toString();
		    b += "," + o.get("north").toString();
		    b += "," + o.get("east").toString();

		    return bbox.equals(b);
		}).map(o -> o.getString("location")).//
		findFirst();
    }

    /**
     * @param bbox
     * @return
     */
    public static Optional<String> getURLEncodedLocation(String bbox) {

	Optional<String> location = getLocation(bbox);

	if (location.isPresent()) {
	    return Optional.of(StringUtils.URLEncodeUTF8(location.get()));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public static JSONArray getRegions() {

	InputStream regionsStream = RegionsManager.class.getClassLoader().getResourceAsStream("regions.json");

	try {
	    return new JSONArray(IOStreamUtils.asUTF8String(regionsStream));
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
}
