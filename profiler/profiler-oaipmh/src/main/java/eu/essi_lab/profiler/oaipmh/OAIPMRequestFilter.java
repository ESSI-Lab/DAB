package eu.essi_lab.profiler.oaipmh;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.google.common.io.ByteStreams;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;

/**
 * A selector which support GET method and POST method with the "Content-Type" set to
 * "application/x-www-form-urlencoded" (see
 * <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#HTTPRequestFormat">3.1.1 HTTP Request Format</a>}
 * 
 * @author Fabrizio
 */
public class OAIPMRequestFilter extends GETRequestFilter {

    private static final String OAI_SELECTOR_INVALID_POST_BODY = "OAI_SELECTOR_INVALID_POST_BODY";

    public OAIPMRequestFilter() {
    }

    /**
     * @param value
     * @param strategy
     */
    public OAIPMRequestFilter(String value, InspectionStrategy strategy) {
	super(value, strategy);
    }
    
    @Override
    public boolean accept(WebRequest webRequest) throws GSException {
	 
	Set<String> keySet = queryConditions.keySet();
	for (String queryString : keySet) {
	    if (accept(getQueryString(webRequest), queryString, queryConditions.get(queryString))) {
		return true;
	    }
	}

	return false;
    }


    /**
     * @param request
     * @return
     */
    protected String getQueryString(WebRequest request) throws GSException {

	String qs = null;

	if (request.isPostRequest()) {

	    InputStream clone = request.getBodyStream().clone();
	    try {
		qs = extractQueryString(clone);

	    } catch (IOException e) {
		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			OAI_SELECTOR_INVALID_POST_BODY, //
			e);
	    }
	} else {
	    qs = request.getQueryString();
	}

	return qs;
    }

    /**
     * Extracts the query string encoded in the POST request <code>body</code> with"Content-Type"
     * "application/x-www-form-urlencoded"
     * 
     * @param body
     * @return
     * @throws IOException
     */
    public static String extractQueryString(InputStream body) throws IOException {

	byte[] byteArray = ByteStreams.toByteArray(body);

	return new String(byteArray);
    }
}
