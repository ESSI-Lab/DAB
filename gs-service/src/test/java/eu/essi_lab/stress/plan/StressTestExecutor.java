package eu.essi_lab.stress.plan;

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
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.commons.io.IOUtils;

/**
 * @author Mattia Santoro
 */
public class StressTestExecutor implements Callable<StressTestResult> {

    private final IStressTest test;
    private final String host;
    private GSLoggerFactory.GSLogger logger = GSLoggerFactory.getLogger(getClass());
    private String rid;

    public StressTestExecutor(IStressTest test, String host) {
	this.test = test;
	this.host = host;
    }

    @Override
    public StressTestResult call() throws Exception {

	rid = "stresstest-" + UUID.randomUUID();
	return executeRequest(test.createRequest(this.host, rid));

    }

    private StressTestResult executeRequest(HttpRequest request) throws URISyntaxException, InterruptedException {

	StressTestResult result = new StressTestResult();

	try {

	    String requestString = test.requestString(this.host, rid);
	    logger.info("Sending {}", requestString);

	    long start = System.currentTimeMillis();
	    HttpResponse<InputStream> response = HttpClient.newBuilder().build().send(request,
		    HttpResponse.BodyHandlers.ofInputStream());

	    long end = System.currentTimeMillis();
	    Integer code = response.statusCode();
	    logger.info("({}) Completed {}", code, requestString);

	    result.setCode(code);
	    result.setRequest(requestString);
	    result.setStart(start);
	    result.setEnd(end);
	    result.setTest(test);
	    result.setResponseFile(saveResponseToFile(response.body()));

	    result.getResponseMetrics().addAll(test.getResponseMetrics());
	    result.getServerMetrics().addAll(test.getServerMetrics());
	    result.setRequestId(rid);

	    return result;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }

    private String saveResponseToFile(InputStream body) throws IOException {
	Path file = Files.createTempFile("stresstest", test.getResponseFileExtension());
	OutputStream outfile = new FileOutputStream(file.toFile());
	IOUtils.copy(body, outfile);

	return file.toFile().getAbsolutePath();

    }
}
