package eu.essi_lab.lib.net.executor;

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

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;

import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpRequestExecutor extends HTTPExecutor {



    @Override
    public HTTPExecutorResponse execute(String url, String httpMethod, List<SimpleEntry<String, String>> headers, byte[] body)
	    throws Exception {
	OkHttpClient client = new OkHttpClient();
	Request request = null;
	Builder requestBuilder = new Request.Builder().url(url);
	RequestBody requestBody = null;
	if (body != null) {
	    requestBody = RequestBody.create(body);
	}
	switch (httpMethod.toUpperCase()) {
	case "GET":
	    requestBuilder = requestBuilder.get();
	    break;
	case "POST":
	    requestBuilder = requestBuilder.post(requestBody);
	    break;
	default:
	    break;
	}
	if (headers != null) {
	    for (SimpleEntry<String, String> header : headers) {
		requestBuilder = requestBuilder.addHeader(header.getKey(), header.getValue());
	    }
	}
	request = requestBuilder.build();
	Response response = client.newCall(request).execute();
	Headers responseHeaders = response.headers();
	SimpleEntry<String, String>[] resHeaders = new SimpleEntry[0];
	if (responseHeaders != null) {
	    resHeaders = new SimpleEntry[responseHeaders.size()];
	    Iterator<Pair<String, String>> iterator = responseHeaders.iterator();
	    int i = 0;
	    while (iterator.hasNext()) {
		Pair<java.lang.String, java.lang.String> pair = (Pair<java.lang.String, java.lang.String>) iterator.next();
		resHeaders[i++] = new SimpleEntry<String, String>(pair.getFirst(), pair.getSecond());
	    }
	}
	ResponseBody responseBody = response.body();
	InputStream responseStream = null;
	if (responseBody != null) {
	    responseStream = responseBody.byteStream();
	}
	HTTPExecutorResponse ret = new HTTPExecutorResponse(response.code(), resHeaders, responseStream);
	return ret;
    }

}
