package eu.essi_lab.lib.net.downloader;

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

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabrizio
 */
public class HttpHeaderUtils {

    /**
     * @param header
     * @param value
     * @return
     */
    public static HttpHeaders build(String header, String value) {

	HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
	hashMap.put(header, Arrays.asList(value));

	return buildMultiValue(hashMap);
    }

    /**
     * @param map
     * @return
     */
    public static HttpHeaders build(Map<String, String> map) {

	HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
	map.keySet().forEach(k -> hashMap.put(k, Arrays.asList(map.get(k))));

	return buildMultiValue(hashMap);
    }

    /**
     * @param map
     * @return
     */
    public static HttpHeaders buildMultiValue(Map<String, List<String>> map) {

	return HttpHeaders.of(map, (h1, h2) -> true);
    }

    /**
     * @return
     */
    public static HttpHeaders buildEmpty() {

	return HttpHeaders.of(new HashMap<String, List<String>>(), (h1, h2) -> true);
    }

    /**
     * @param header
     * @param value
     * @return
     */
    public static String toString(HttpHeaders headers) {

	return headers.map().toString().replace("{", "").replace("}", "");
    }

}
