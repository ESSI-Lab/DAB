package eu.essi_lab.pdk.handler.selector.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter.InspectionStrategy;

public class GETRequestFilterTest {

    @Test
    public void basicTest() {

	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = Mockito.mock(WebRequest.class);
	Mockito.when(request.isGetRequest()).thenReturn(false);
	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest1() {

	//
	// view and token cannot be defined in the accepted path
	// comparison:
	// 'view', 'pippo', 'opensearch', '*'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("view/pippo/opensearch/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest2() {

	//
	// view and token cannot be defined in the accepted path
	// comparison:
	// 'view', 'pippo', 'opensearch', 'query'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("view/pippo/opensearch/query");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest3() {

	//
	// this test fails because two path segments are expected after the 'view/pippo' segments
	// see next test
	// comparison:
	// '*', '*', 'opensearch', 'query'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("*/*/opensearch/query");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest3_1() {

	//
	// this test succeed because the two path 'aaa' and 'bbb' are provided, they match with the '*/*' of the
	// comparison:
	// '*', '*', 'opensearch', 'query'
	// 'aaa', 'bbb', 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("*/*/opensearch/query");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/aaa/bbb/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest4() {

	// comparison:
	// 'view', '*', 'opensearch', '*'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("view/*/opensearch/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest5() {

	//
	// - comparison:
	// 'opensearch', 'query'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter();
	filter.setPath("opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest6() {

	//
	// - comparison:
	// 'opensearch', '*'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter();
	filter.setPath("opensearch/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest7() {

	//
	// - comparison:
	// 'opensearch', 'query'
	// 'opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest7_1() {

	//
	// - comparison:
	// 'opensearch', 'query'
	// 'essi','opensearch', 'query'
	//

	GETRequestFilter filter = new GETRequestFilter("opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/"); // this not matches the request services path '/services/essi/'

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewTest8() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewAndTokenTest1() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/token/pluto/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewAndTokenTest2() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/token/pluto/view/pippo/opensearch/query");
	request.setServicesPath("/services/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void viewAndTokenTest3() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/token/pluto/view/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void tokenTest1() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/token/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void tokenTest2() {

	GETRequestFilter filter = new GETRequestFilter("abc/opensearch/query");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/token/pippo/opensearch/query");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void tokenTest3() {

	GETRequestFilter filter = new GETRequestFilter("opensearch/query");

	WebRequest request = WebRequest.createGET(
		" http://localhost:9090/gs-service/services/essi/token/xxx/opensearch/query?si=1&ct=10&outputFormat=application%2Fjson");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest1() {

	// ------------------------------
	//
	// 'path' is compared with 'path'.
	// the services path is not set and it is not used in the request URL
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/path");
	request.setServicesPath("");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest2() {

	// ------------------------------
	//
	// 'path' is compared with 'gs-service/path'
	// because the services path is set but is not present in the request URL
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/path");
	request.setServicesPath("/services/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest2_1() {

	// ------------------------------
	//
	// 'path' is compared with 'path'
	// since the services path is set as default('/services/essi/') but it is overridden
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	filter.overrideServicesPath("");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/path");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest3() {

	// ------------------------------
	//
	// 'path' is compared with 'path'
	// the services path '/services/essi/' is not included in the comparison
	// since it is correctly set
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest4() {

	// ------------------------------
	//
	// 'path' is compared with 'path' (slash of "path/" is excluded)
	// the services path '/services/essi/' is not included in the comparison
	// since it is correctly set
	//
	GETRequestFilter filter = new GETRequestFilter("path/");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest5() {

	// ------------------------------
	//
	// 'path' is compared with 'path' (slash of "path/" is excluded)
	// the services path '/services/essi/' is not included in the comparison
	// since it is correctly set
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest6() {

	// ------------------------------
	//
	// 'path' is compared with 'services/path'
	// the services path '/services/' is included in the comparison
	// since it is NOT correctly set
	//
	GETRequestFilter filter = new GETRequestFilter("path");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest7() {

	// ------------------------------
	//
	// 'path/*' is compared with 'path/abc'
	//
	//
	GETRequestFilter filter = new GETRequestFilter("path/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path/abc");
	request.setServicesPath("/services/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest8() {

	// ------------------------------
	//
	// 'path/*' is compared with 'path'
	//
	//
	GETRequestFilter filter = new GETRequestFilter("path/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path");
	request.setServicesPath("/services/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest9() {

	GETRequestFilter filter = new GETRequestFilter("csw/pubsub/subscription/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/csw/pubsub/subscription/xxx");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest10() {

	GETRequestFilter filter = new GETRequestFilter("csw/*/*/subscription/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/csw/a/b/subscription/c");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest11() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*");
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest11_1() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*");
	filter.overrideServicesPath("/services/");

	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest12() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*");
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    /**
     * This test fails since the default {@link WebRequest} services path is 'services/essi', so we expect a request such 
     * 'http://localhost:9090/gs-service/services/essi/bnhs...' while the 'essi' segment is missing
     */
    @Test
    public void pathTest13() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*/timeseries");
	WebRequest request = WebRequest
		.createGET("http://localhost:9090/gs-service/services/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/timeseries");
 
	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
    
    /**
     * Like the test above but with the {@link WebRequest} services path is 'services/essi' set to 'services', so it passes
     */
    @Test
    public void pathTest13_1() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*/timeseries");
	WebRequest request = WebRequest
		.createGET("http://localhost:9090/gs-service/services/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/timeseries");
	
	//
	//
	//
	request.setServicesPath("/services/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    
    /**
     * Similar to the test above but {@link WebRequest} services path is the default, and instead of setting it to '/services/' in the 
     * request, it is overridden in the filter
     */
    @Test
    public void pathTest13_2() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*/timeseries");
	
	//
	//
	//
	filter.overrideServicesPath("/services/");

	WebRequest request = WebRequest
		.createGET(" http://localhost:9090/gs-service/services/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/timeseries");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathTest15() {

	GETRequestFilter filter = new GETRequestFilter("bnhs/station/*/timeseries");
	WebRequest request = WebRequest
		.createGET(" http://localhost:9090/gs-service/services/essi/bnhs/station/7038DE71E2DF0EB692008B700F7B375737808D52/timeseries");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest1() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identify");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest2() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identifyx");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest3() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?vErb=IdeNtifY");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest4() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=ListRecords&something=true");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest5() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identify&something=true");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest6() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identify");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest7() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.IGNORE_CASE_EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?vErb=IdeNtifY");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest8() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identify&something=false");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest9() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?something=false&verb=Identify");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest10() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.IGNORE_CASE_LIKE_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=IdeNtIfy&something=false");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest11() {

	GETRequestFilter filter = new GETRequestFilter();
	filter.addQueryCondition("verb=Identify", InspectionStrategy.EXACT_MATCH);
	filter.addQueryCondition("verb=ListMetadataFormats", InspectionStrategy.EXACT_MATCH);
	filter.addQueryCondition("verb=ListSets", InspectionStrategy.EXACT_MATCH);

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Identify");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=ListMeta");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=ListMetadataFormats");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=List");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Sets");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=ListSets");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest12() {

	GETRequestFilter filter = new GETRequestFilter();
	filter.addQueryCondition("verb=Identify", InspectionStrategy.EXACT_MATCH);
	filter.addQueryCondition("verb=ListMetadataFormats", InspectionStrategy.EXACT_MATCH);
	filter.addQueryCondition("verb=ListSets", InspectionStrategy.EXACT_MATCH);

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?verb=Unknown");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest13() {

	GETRequestFilter filter = new GETRequestFilter();
	filter.addParameterCondition("A", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("b", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("cde", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?A&BB&CDE");
	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest14() {

	GETRequestFilter filter = new GETRequestFilter();
	filter.addParameterCondition("A", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("b", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("cde", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?A&B&CDE");
	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void conditionsTest15() {

	GETRequestFilter filter = new GETRequestFilter();
	filter.addParameterCondition("A", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("b", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	filter.addParameterCondition("cde", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/path?sd&a&B&gf=pippo&CDE=topolino&n");
	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest1() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=Identify");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest2() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=Identifyx");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest3() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.IGNORE_CASE_EXACT_MATCH);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?vErb=IdeNtifY");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest4() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=ListRecords&something=true");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest5() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=Identify&something=true");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest6() {

	GETRequestFilter filter = new GETRequestFilter("path", "verb=Identify", InspectionStrategy.EXACT_DISCARD);
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=Identify");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest7() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.IGNORE_CASE_EXACT_DISCARD);
	filter.setPath("path");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?vErb=IdeNtifY");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest8() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	filter.setPath("path");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=Identify&something=false");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest9() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	filter.setPath("path");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?something=false&verb=Identify");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest10() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.IGNORE_CASE_LIKE_MATCH);
	filter.setPath("path");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path?verb=IdeNtIfy&something=false");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest11() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	filter.setPath("path/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path/abc?something=false&verb=Identify");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest12() {

	GETRequestFilter filter = new GETRequestFilter("verb=Identify", InspectionStrategy.LIKE_MATCH);
	filter.setPath("path/*");

	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/path/?something=false&verb=Identify");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest13() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest14() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest15() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest16() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest17() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest18() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/access/info/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertFalse(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest19() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/*");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/sem/concepts/abc/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest20() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts/");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/sem/concepts/");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void pathAndConditionsTest21() {

	GETRequestFilter filter = new GETRequestFilter("rest/sem/concepts");
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/rest/sem/concepts");
	request.setServicesPath("/services/essi/");

	try {
	    boolean accept = filter.accept(request);
	    Assert.assertTrue(accept);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
