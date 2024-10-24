package eu.essi_lab.lib.net.executor;

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
import java.util.AbstractMap.SimpleEntry;

public class HTTPExecutorResponse {

    private Integer responseCode;
    private InputStream responseStream;
    private SimpleEntry<String, String>[] responseHeaders;

    public HTTPExecutorResponse(Integer responseCode, SimpleEntry<String, String>[] responseHeaders, InputStream responseStream) {
	super();
	this.responseCode = responseCode;
	this.responseStream = responseStream;
	this.responseHeaders = responseHeaders;
    }

    public Integer getResponseCode() {
	return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
	this.responseCode = responseCode;
    }

    public InputStream getResponseStream() {
	return responseStream;
    }

    public void setResponseStream(InputStream responseStream) {
	this.responseStream = responseStream;
    }

    public SimpleEntry<String, String>[] getResponseHeaders() {
	return responseHeaders;
    }

    public void setResponseHeaders(SimpleEntry<String, String>[] responseHeaders) {
	this.responseHeaders = responseHeaders;
    }
}
