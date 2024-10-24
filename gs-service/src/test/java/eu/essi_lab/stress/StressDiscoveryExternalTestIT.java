package eu.essi_lab.stress;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.net.utils.HttpConnectionUtils;

public class StressDiscoveryExternalTestIT extends StressExternalTestIT {

    private List<Integer> myit = Arrays.asList(1, 8); // 2, 4, 8, 16, 32, 64);

    private Integer currentThread = null;

    @Override
    protected int getParallel() {
	return 200;
    }

    @Override
    protected int getNumofTests() {
	return myit.size();
    }

    @Override
    protected Integer getNumberOfRequestForTest(int it) {
	currentThread = myit.get(it);
	return currentThread;
    }

    @Override
    protected Integer getIterationsPerTest() {
	return 10;
    }

    public boolean testOperation() throws URISyntaxException, InterruptedException {

	String hostname = "https://gs-service-preproduction.geodab.eu/";

	// hostname = "http://localhost:9090";

	// hostname = "http://santoro.essi-lab.eu:8080";

	String view = "geoss";

	String sources = ""; // UUID-e8f7704a-bcd1-42c4-853c-bbb9a6caadce for SeaDataNet

	sources = "";// ""UUID-e8f7704a-bcd1-42c4-853c-bbb9a6caadce";

	if (view != null && !view.equals("")) {
	    view = "view/" + view + "/";
	} else {
	    view = "";
	}

	if (sources != null && !sources.equals("")) {
	    sources = "&sources=" + sources;
	}

	String rid = "requestid-" + currentThread;
	String url;

	// url = hostname + "/gs-service/services/" + view + "opensearch/query?searchFields=title," + "keywords&reqID="
	// + rid
	// + "&si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,"
	// +
	// "sscScore&rel=OVERLAPS&viewid=&st=temperature&bbox=-7.946211717841426,46.28789664955397,9.511565902023353,53.996589749888244";

	url = hostname + "/gs-service/services/" + view + "opensearch/query?searchFields=title,keywords&reqID=" + rid
		+ "&si=1&ct=12&tf=keyword,format,protocol,providerID," + "organisationName,sscScore&rel=OVERLAPS&viewid=&st=temperature"
		+ sources;

	return testSearch(url);
    }

    private boolean testSearch(String url) throws URISyntaxException, InterruptedException {

	Optional<Integer> optionalCode = HttpConnectionUtils.getOptionalResponseCode(url);

	if (optionalCode.isPresent()) {
	    Integer code = optionalCode.get();
	    if (code.equals(200)) {
		return true;
	    }
	}
	return false;
    }

}
