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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Parsed expand option, e.g. Things($top=100) or Observations($orderby=...;$filter=...).
     */
    public static class ExpandOption {
	private final String property;
	private final Integer top;
	private final String options;

	public ExpandOption(String property, Integer top, String options) {
	    this.property = property;
	    this.top = top;
	    this.options = options != null ? options : "";
	}

	public String getProperty() {
	    return property;
	}

	public Integer getTop() {
	    return top;
	}

	/** Raw options string, e.g. "$orderby=phenomenonTime desc;$filter=phenomenonTime ge ..." */
	public String getOptions() {
	    return options;
	}

	/** Extracts $filter value from options. */
	public String getFilter() {
	    return extractOption(options, "\\$filter\\s*=\\s*([^;$]+)", 1);
	}

	/** Extracts $orderby value from options. */
	public String getOrderBy() {
	    return extractOption(options, "\\$orderby\\s*=\\s*([^;$]+)", 1);
	}

	private static String extractOption(String opts, String regex, int group) {
	    if (opts == null || opts.isEmpty()) {
		return null;
	    }
	    Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(opts.trim());
	    return m.find() ? m.group(group).trim() : null;
	}

	/** Parses phenomenonTime ge X and le Y from filter. Returns [begin, end] or null if not found. */
	public static String[] parsePhenomenonTimeRange(String filter) {
	    if (filter == null || filter.isEmpty()) {
		return null;
	    }
	    String decoded = filter;
	    try {
		decoded = java.net.URLDecoder.decode(filter, java.nio.charset.StandardCharsets.UTF_8);
	    } catch (Exception e) {
		/* keep original */
	    }
	    String ge = null;
	    String le = null;
	    Matcher geM = Pattern.compile("phenomenonTime\\s+ge\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE).matcher(decoded);
	    if (geM.find()) {
		ge = geM.group(1).replaceAll("^['\"]|['\"]$", "");
	    }
	    Matcher leM = Pattern.compile("phenomenonTime\\s+le\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE).matcher(decoded);
	    if (leM.find()) {
		le = leM.group(1).replaceAll("^['\"]|['\"]$", "");
	    }
	    if (ge != null && le != null) {
		return new String[] { ge, le };
	    }
	    return null;
	}
    }

    private static final Pattern EXPAND_ITEM_PATTERN = Pattern.compile("([A-Za-z]+)\\s*(?:\\(([^)]*)\\))?");
    private static final Pattern EXPAND_TOP_PATTERN = Pattern.compile("\\$top\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses $expand value into list of options. E.g. "Things($top=100)" -> [ExpandOption(Things, 100)].
     */
    public List<ExpandOption> getExpandOptions() {
	String expand = getExpand();
	if (expand == null || expand.isEmpty()) {
	    return List.of();
	}
	List<ExpandOption> out = new ArrayList<>();
	for (String item : expand.split(",")) {
	    String trimmed = item.trim();
	    if (trimmed.isEmpty()) {
		continue;
	    }
	    Matcher m = EXPAND_ITEM_PATTERN.matcher(trimmed);
	    if (m.matches()) {
		String prop = m.group(1);
		String opts = m.group(2);
		Integer top = null;
		if (opts != null && !opts.isEmpty()) {
		    Matcher topM = EXPAND_TOP_PATTERN.matcher(opts);
		    if (topM.find()) {
			top = Integer.valueOf(topM.group(1));
		    }
		}
		out.add(new ExpandOption(prop, top, opts));
	    }
	}
	return out;
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

    /**
     * Extracts a top-level query parameter. Uses proper parsing (split by &) so that
     * parameters whose values contain the same key (e.g. $expand=Observations($filter=...))
     * do not incorrectly match.
     */
    private String getQueryParam(String key) {
	String queryString = webRequest.getQueryString();
	if (queryString == null && webRequest.getURLDecodedQueryString().isPresent()) {
	    queryString = webRequest.getURLDecodedQueryString().get();
	}
	if (queryString == null || queryString.isEmpty()) {
	    return null;
	}
	String prefix = key + "=";
	for (String param : queryString.split("&")) {
	    if (param.startsWith(prefix)) {
		String value = param.substring(prefix.length());
		try {
		    return URLDecoder.decode(value, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
		    return value;
		}
	    }
	}
	return null;
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
