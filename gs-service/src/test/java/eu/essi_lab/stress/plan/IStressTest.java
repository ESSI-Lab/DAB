package eu.essi_lab.stress.plan;

import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface IStressTest {
    String createTestKey();

    HttpRequest createRequest(String host, String requestId) throws URISyntaxException;

    String requestString(String host, String requestId);

    String getResponseFileExtension();

    List<String> getResponseMetrics();

    Long readMetric(String metric, String filePath);

    List<String> getServerMetrics();

    Long readServerMetric(String serverMetric, String requestId, String logGroup, String logNamePrefix);

}
