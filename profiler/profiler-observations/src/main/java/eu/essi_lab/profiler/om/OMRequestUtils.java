package eu.essi_lab.profiler.om;

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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.profiler.om.OMRequest.APIParameters;

/**
 * Helpers for {@link OMRequest} parameter interpretation.
 */
public final class OMRequestUtils {

    public static final int METADATA_SYNC_MAX_RECORDS = 100;

    public static final int METADATA_PAGE_SIZE = 500;

    private static final String RESUMPTION_TOKEN_SEPARATOR = ",";

    private OMRequestUtils() {
    }

    /**
     * Serializes a {@link SearchAfter} cursor into the OM API resumption token format.
     */
    public static String toResumptionToken(SearchAfter searchAfter) {

	if (searchAfter == null || searchAfter.getValues().isEmpty() || searchAfter.getValues().get().isEmpty()) {
	    return "";
	}

	return searchAfter.getValues().get().stream().//
		map(Object::toString).//
		collect(Collectors.joining(RESUMPTION_TOKEN_SEPARATOR));
    }

    /**
     * Parses an OM API resumption token into a {@link SearchAfter} cursor. Values are URL-decoded first so tokens
     * produced by {@link MetadataDownloaderTool} (where commas are encoded as {@code %2C}) are handled correctly.
     */
    public static SearchAfter toSearchAfter(String resumptionToken) {

	if (resumptionToken == null || resumptionToken.isEmpty()) {
	    return null;
	}

	String decoded = URLDecoder.decode(resumptionToken, StandardCharsets.UTF_8);
	List<Object> values = new ArrayList<>();
	for (String part : decoded.split(RESUMPTION_TOKEN_SEPARATOR, -1)) {
	    values.add(part);
	}
	return new SearchAfter(values);
    }

    public static boolean isIncludeData(OMRequest request) {
	String value = request.getParameterValue(APIParameters.INCLUDE_VALUES);
	if (value == null) {
	    return false;
	}
	value = value.trim().toLowerCase();
	return value.equals("true") || value.equals("yes") || value.equals("1");
    }

    public static boolean isMetadataOnly(OMRequest request) {
	String value = request.getParameterValue(APIParameters.INCLUDE_VALUES);
	if (value == null) {
	    return false;
	}
	value = value.trim().toLowerCase();
	return value.equals("false") || value.equals("no") || value.equals("0");
    }

    public static boolean isMetadataOnlyRequestUrl(String requestURL) {
	if (requestURL == null) {
	    return false;
	}
	String lower = requestURL.toLowerCase();
	return lower.contains("includedata=false") || lower.contains("includevalues=false");
    }
}
