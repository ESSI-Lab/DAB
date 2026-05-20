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

import eu.essi_lab.profiler.om.OMRequest.APIParameters;

/**
 * Helpers for {@link OMRequest} parameter interpretation.
 */
public final class OMRequestUtils {

    public static final int METADATA_SYNC_MAX_RECORDS = 100;

    public static final int METADATA_PAGE_SIZE = 500;

    private OMRequestUtils() {
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
