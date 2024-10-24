package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.SimpleConfiguration;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.os.OSProfiler;

public class OSGetDescriptionDocumentRequestTest {

    @Before
    public void init() {
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Test
    public void test() {
	
	SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
	ConfigurationWrapper.setConfiguration(simpleConfiguration);

	Response response = null;

	OSProfiler osProfiler = new OSProfiler();

	String queryString = "http://localhost/gs-service/services/essi/opensearch/description";
	WebRequest webRequest = WebRequest.createGET(queryString);

	try {
	    response = osProfiler.handle(webRequest);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
	Assert.assertEquals("text/xml;charset=UTF-8", response.getHeaderString("Content-Type"));

	String doc = (String) response.getEntity();

	System.out.println(doc);

	Assert.assertThat("String contains OpenSearchDescription", doc, new Matcher<String>() {

	    @Override
	    public void describeTo(Description description) {
	    }

	    @Override
	    public boolean matches(Object item) {

		return item.toString().contains("OpenSearchDescription");
	    }

	    @Override
	    public void describeMismatch(Object item, Description mismatchDescription) {
	    }

	    @Override
	    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
	    }

	});
    }
}
