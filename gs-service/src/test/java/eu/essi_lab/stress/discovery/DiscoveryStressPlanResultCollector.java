package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.utils.IOStreamUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressPlanResultCollector {

    private List<DiscoveryStressTestResult> results = new ArrayList<>();

    public void addResult(DiscoveryStressTestResult result) {
	getResults().add(result);

    }

    public List<DiscoveryStressTestResult> getResults() {
	return results;
    }

    public void printReport(OutputStream out) {
	String summary = String.format("Number of tests: %d\nSuccess: %d",getResults().size(), getResults().stream().filter(r->r.getCode()==200).count());

	OutputStreamWriter writer = new OutputStreamWriter(out);

	try {
	    writer.write(summary);
	    writer.flush();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }
}
