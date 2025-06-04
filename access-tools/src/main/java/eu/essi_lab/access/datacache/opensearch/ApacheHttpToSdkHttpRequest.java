package eu.essi_lab.access.datacache.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;

public class ApacheHttpToSdkHttpRequest {
    public static SdkHttpFullRequest convert(HttpRequest apacheRequest) throws IOException {
    	
    	String uriPath = apacheRequest.getRequestLine().getUri();
        URI parsedUri = URI.create(uriPath);
        SdkHttpFullRequest.Builder builder = SdkHttpFullRequest.builder()
            .method(SdkHttpMethod.valueOf(apacheRequest.getRequestLine().getMethod()))
            .uri(parsedUri);

        for (Header header : apacheRequest.getAllHeaders()) {
            builder.putHeader(header.getName(), Collections.singletonList(header.getValue()));
        }

        // Parse the path and query string
        
        builder.encodedPath(parsedUri.getPath());

        if (parsedUri.getQuery() != null) {
            for (String pair : parsedUri.getQuery().split("&")) {
                String[] parts = pair.split("=");
                String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                builder.putRawQueryParameter(name, value);
            }
        }

        return builder.build();
    }
}
