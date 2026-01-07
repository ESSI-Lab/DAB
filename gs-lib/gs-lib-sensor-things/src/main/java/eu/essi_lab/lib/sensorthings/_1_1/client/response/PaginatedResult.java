package eu.essi_lab.lib.sensorthings._1_1.client.response;

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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * @author Fabrizio
 */
public abstract class PaginatedResult extends JSONObjectWrapper {

    /**
     * @param nextLink
     * @return
     */
    public static Optional<Integer> getSkip(String nextLink) {

	return extractParam(nextLink, "skip").map(v -> Integer.valueOf(v));

    }

    /**
     * @param nextLink
     * @return
     */
    public static Optional<Integer> getTop(String nextLink) {

	return extractParam(nextLink, "top").map(v -> Integer.valueOf(v));
    }

    /**
     * @param url
     * @param param
     * @return
     */
    private static Optional<String> extractParam(String url, String param) {

	Pattern pattern = Pattern.compile("(?:[?&]\\$" + param + "=)([^&]+)");
	Matcher matcher = pattern.matcher(url);
	return Optional.ofNullable(matcher.find() ? matcher.group(1) : null);
    }

    public static void main(String[] args) {

	String s1 = "http://request&$param1&$skip=100&$top=100";
	String s2 = "http://request&$skip=90&$param2&$param3&$top=10";
	String s3 = "http://request&$param2&$param3";

	System.out.println(getSkip(s1));
	System.out.println(getSkip(s2));
	System.out.println(getSkip(s3));

	System.out.println(getTop(s1));
	System.out.println(getTop(s2));
	System.out.println(getTop(s3));

    }

    /**
     * @param object
     */
    protected PaginatedResult(JSONObject object) {
	super(object);
    }

    /**
     * @return
     */
    public Optional<Integer> getCount() {

	return getObject().has("@iot.count") ? Optional.of(getObject().getInt("@iot.count")) : Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getNextLink() {

	return getObject().has("@iot.nextLink") ? Optional.of(getObject().getString("@iot.nextLink")) : Optional.empty();
    }

}
