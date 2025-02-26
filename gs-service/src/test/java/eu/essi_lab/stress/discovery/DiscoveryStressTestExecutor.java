package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.commons.io.IOUtils;

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

	    long end = System.currentTimeMillis();
	    logger.info("Completed {}", url);

	    Integer code = response.statusCode();

	    result.setCode(code);
	    result.setRequest(url);

	    result.setStart(start);
	    result.setEnd(end);

	    result.setTest(test);

	    result.setResponseFile(saveResponseToFile(response.body()));

	    return result;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }

    private String saveResponseToFile(InputStream body) throws IOException {
	Path file = Files.createTempFile("stresstest", ".xml");
	OutputStream outfile = new FileOutputStream(file.toFile());
	IOUtils.copy(body, outfile);

	return file.toFile().getAbsolutePath();

    }
}
