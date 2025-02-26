package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTestExecutor implements Callable<DiscoveryStressTestResult> {

    private final DiscoveryStressTest test;
    private final String host;
    private GSLoggerFactory.GSLogger logger = GSLoggerFactory.getLogger(getClass());

    public DiscoveryStressTestExecutor(DiscoveryStressTest test, String host) {
	this.test = test;
	this.host = host;
    }

    @Override
    public DiscoveryStressTestResult call() throws Exception {

	String params = test.createRequestParameters();

	String requestUrl = this.host + "/gs-service/services/opensearch/query?" + params;

	return testSearch(requestUrl);
    }

    private DiscoveryStressTestResult testSearch(String url) throws URISyntaxException, InterruptedException {

	DiscoveryStressTestResult result = new DiscoveryStressTestResult();

	HttpRequest request = HttpRequestUtils.build(HttpRequestUtils.MethodNoBody.GET, url);

	try {

	    logger.info("Sending {}", url);

	    long start = System.currentTimeMillis();
	    HttpResponse<InputStream> response = HttpClient.newBuilder().build().send(request,
		    HttpResponse.BodyHandlers.ofInputStream());

	    logger.info("Completed {}", url);
	    long end = System.currentTimeMillis();
	    long gap = end - start;

	    Integer code = response.statusCode();

	    result.setCode(code);
	    result.setRequest(url);
	    result.setExecTime(gap);

	    return result;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }
}
