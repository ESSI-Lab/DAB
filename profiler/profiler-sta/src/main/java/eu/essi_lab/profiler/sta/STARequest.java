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

package eu.essi_lab.profiler.sta;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.essi_lab.messages.web.WebRequest;

/**
 * Parses OGC SensorThings API request path and query options.
 *
 * @author boldrini
 */
public class STARequest {

    public enum EntitySet {
	Things, Locations, HistoricalLocations, Datastreams, Sensors, ObservedProperties, Observations, FeaturesOfInterest
    }

    public enum QueryOption {
	FILTER("$filter"), EXPAND("$expand"), SELECT("$select"), ORDERBY("$orderby"), TOP("$top"), SKIP("$skip"), COUNT("$count");

	private final String key;

	QueryOption(String key) {
	    this.key = key;
	}

	public String getKey() {
	    return key;
	}
    }

    private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("([A-Za-z]+)\\(([^)]+)\\)");

    private final WebRequest webRequest;
    private final String path;
    private EntitySet entitySet;
    private String entityId;
    private String navigationProperty;

    public STARequest(WebRequest webRequest) {
	this.webRequest = webRequest;
	this.path = webRequest.getRequestPath() != null ? webRequest.getRequestPath() : "";
	parsePath();
    }

    private void parsePath() {
	String normalized = path.replaceAll("/+", "/").replaceAll("^/|/$", "");
	String[] segments = normalized.isEmpty() ? new String[0] : normalized.split("/");

	for (int i = 0; i < segments.length; i++) {
	    String seg = segments[i];
	    Matcher m = ENTITY_ID_PATTERN.matcher(seg);
	    if (m.matches()) {
		String name = m.group(1);
		entitySet = parseEntitySet(name);
		entityId = m.group(2);
		if (i + 1 < segments.length) {
		    navigationProperty = segments[i + 1];
		}
		return;
	    }
	    EntitySet es = parseEntitySet(seg);
	    if (es != null) {
		entitySet = es;
		return;
	    }
	}
    }

    private static EntitySet parseEntitySet(String name) {
	try {
	    return EntitySet.valueOf(name);
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    public Optional<EntitySet> getEntitySet() {
	return Optional.ofNullable(entitySet);
    }

    public Optional<String> getEntityId() {
	return Optional.ofNullable(entityId);
    }

    /**
     * Returns the entity ID with surrounding quotes stripped (e.g. '1000022519' -> 1000022519).
     */
    public Optional<String> getEntityIdNormalized() {
	if (entityId == null || entityId.isEmpty()) {
	    return Optional.empty();
	}
	String normalized = entityId.replaceAll("^['\"]|['\"]$", "").trim();
	return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    public Optional<String> getNavigationProperty() {
	return Optional.ofNullable(navigationProperty);
    }

    public String getFilter() {
	return getQueryParam(QueryOption.FILTER.getKey());
    }

    public String getExpand() {
	return getQueryParam(QueryOption.EXPAND.getKey());
    }

    public String getSelect() {
	return getQueryParam(QueryOption.SELECT.getKey());
    }

    public String getOrderBy() {
	return getQueryParam(QueryOption.ORDERBY.getKey());
    }

    public Integer getTop() {
	String v = getQueryParam(QueryOption.TOP.getKey());
	return v != null ? parseInt(v) : null;
    }

    public Integer getSkip() {
	String v = getQueryParam(QueryOption.SKIP.getKey());
	return v != null ? parseInt(v) : null;
    }

    /**
     * Resumption token for pagination (next page offset).
     */
    public String getResumptionToken() {
	return getQueryParam("resumptionToken");
    }

    /**
     * Effective skip for pagination: resumptionToken if present, else $skip, else 0.
     */
    public int getEffectiveSkip() {
	String rt = getResumptionToken();
	if (rt != null) {
	    Integer v = parseInt(rt);
	    if (v != null && v >= 0) {
		return v;
	    }
	}
	Integer s = getSkip();
	return s != null ? s : 0;
    }

    public Boolean getCount() {
	String v = getQueryParam(QueryOption.COUNT.getKey());
	return "true".equalsIgnoreCase(v);
    }

    private String getQueryParam(String key) {
	return webRequest.extractQueryParameter(key).orElse(null);
    }

    private static Integer parseInt(String s) {
	try {
	    return Integer.valueOf(s);
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    public WebRequest getWebRequest() {
	return webRequest;
    }

    public String getPath() {
	return path;
    }
}
