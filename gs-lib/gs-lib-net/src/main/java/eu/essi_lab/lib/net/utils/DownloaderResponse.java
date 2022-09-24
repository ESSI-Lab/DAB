package eu.essi_lab.lib.net.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.apache.http.Header;


public class DownloaderResponse {

    private Integer responseCode;
    private InputStream responseStream;

    public DownloaderResponse(Integer responseCode, Header[] responseHeaders, InputStream responseStream) {
	super();
	this.responseCode = responseCode;
	this.responseStream = responseStream;
	this.responseHeaders = responseHeaders;
    }

    private Header[] responseHeaders;

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

    public Header[] getResponseHeaders() {
	return responseHeaders;
    }

    public void setResponseHeaders(Header[] responseHeaders) {
	this.responseHeaders = responseHeaders;
    }

}
