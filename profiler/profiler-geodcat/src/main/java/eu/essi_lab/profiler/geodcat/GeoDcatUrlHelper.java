package eu.essi_lab.profiler.geodcat;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;

/**
 * Builds absolute URIs for catalog and dataset resources, preserving the current request path (view, token, etc.).
 */
public final class GeoDcatUrlHelper {

    private GeoDcatUrlHelper() {
    }

    public static String stripQuery(String absoluteUri) {

	int q = absoluteUri.indexOf('?');
	return q > 0 ? absoluteUri.substring(0, q) : absoluteUri;
    }

    public static String currentResourceUri(WebRequest request) {

	if (request.getUriInfo() == null || request.getUriInfo().getAbsolutePath() == null) {
	    return "";
	}

	return stripQuery(request.getUriInfo().getAbsolutePath().toString());
    }

    public static String geodcatBaseFromCatalogRequest(WebRequest request) {

	String uri = currentResourceUri(request);
	if (uri.endsWith("/catalog")) {
	    return uri.substring(0, uri.length() - "/catalog".length());
	}
	return uri;
    }

    public static String datasetLandingPageUri(WebRequest request, String publicId) {

	String base = geodcatBaseFromCatalogRequest(request);
	String enc = URLEncoder.encode(publicId, StandardCharsets.UTF_8).replace("+", "%20");
	return base + "/dataset/" + enc;
    }

    /**
     * Absolute catalog URL for the given page, preserving other query parameters and setting {@code startIndex} and
     * {@code count}.
     */
    public static String catalogPageUrl(WebRequest request, int startIndex, int count) {

	String base = stripQuery(currentResourceUri(request));
	if (base.isEmpty()) {
	    return "";
	}
	return catalogPageUrl(base, request.getOptionalQueryString().orElse(null), startIndex, count);
    }

    static String catalogPageUrl(String catalogPathWithoutQuery, String queryString, int startIndex, int count) {

	KeyValueParser parser = new KeyValueParser(queryString != null ? queryString : "");
	Map<String, String> map = new LinkedHashMap<>(parser.getParametersMap());
	map.put("startIndex", String.valueOf(startIndex));
	map.put("count", String.valueOf(count));
	StringJoiner joiner = new StringJoiner("&");
	for (Map.Entry<String, String> e : map.entrySet()) {
	    String v = e.getValue() != null ? e.getValue() : "";
	    joiner.add(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
		    + URLEncoder.encode(v, StandardCharsets.UTF_8));
	}
	return catalogPathWithoutQuery + "?" + joiner.toString();
    }
}
