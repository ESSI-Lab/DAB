package eu.essi_lab.profiler.oaipmh.test.request;

import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfiler;

public class ListMetadataFormatsRequestTest {

    @Test
    public void test() {
	
	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());


	Response response = null;

	OAIPMHProfiler osProfiler = new OAIPMHProfiler();

	String queryString = "http://profiler-oaipmh?verb=ListMetadataFormats";
	WebRequest webRequest = WebRequest.createGET(queryString);

	try {
	    response = osProfiler.handle(webRequest);
	} catch (GSException e) {
	    fail("Exception thrown");
	}
	Assert.assertEquals(response.getHeaderString("Content-Type"), "application/xml;charset=UTF-8");

	String entity = (String) response.getEntity();

	Assert.assertThat("Output string contains ListMetadataFormats", entity, new Matcher<String>() {

	    @Override
	    public void describeTo(Description description) {
	    }

	    @Override
	    public boolean matches(Object item) {

		return item.toString().contains("ListMetadataFormats");
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
