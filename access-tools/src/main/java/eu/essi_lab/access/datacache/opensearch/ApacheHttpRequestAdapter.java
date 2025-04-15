package eu.essi_lab.access.datacache.opensearch;

import java.util.Arrays;

import org.apache.http.HttpRequest;

import software.amazon.awssdk.http.SdkHttpFullRequest;

public class ApacheHttpRequestAdapter {
    public static void applySignedRequest(SdkHttpFullRequest signed, HttpRequest apacheRequest) {
        // Clear existing headers (optional)
        Arrays.stream(apacheRequest.getAllHeaders()).forEach(header -> 
            apacheRequest.removeHeaders(header.getName()));

        // Add signed headers
        signed.headers().forEach((name, values) -> {
            for (String value : values) {
                apacheRequest.addHeader(name, value);
            }
        });
    }
}
