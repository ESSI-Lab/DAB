package eu.essi_lab.stress.plan;

import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface IStressTest {
    String createTestKey();

    HttpRequest createRequest(String host) throws URISyntaxException;

    String requestString(String host);

    String getResponseFileExtension();

    List<String> getResponseMetrics();
}
