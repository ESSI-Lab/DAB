package eu.essi_lab.access.datacache.opensearch;

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
