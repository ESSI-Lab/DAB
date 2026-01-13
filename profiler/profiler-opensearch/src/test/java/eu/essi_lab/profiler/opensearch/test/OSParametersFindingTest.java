package eu.essi_lab.profiler.opensearch.test;

import java.util.*;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;

public class OSParametersFindingTest {

    @Test
    public void testOSParametersFinding() {

	List<OSParameter> params = WebRequestParameter.findParameters(OSParameters.class);
	Assert.assertThat(params, new Matcher<List<OSParameter>>() {

	    @Override
	    public void describeTo(Description description) {
	    }

	    @Override
	    public boolean matches(Object item) {

		@SuppressWarnings("unchecked")
		List<OSParameter> params = (List<OSParameter>) item;
		boolean accept = false;
		for (OSParameter param : params) {
		    accept |= param.getName().equals(OSParameters.COUNT.getName())
			    || param.getName().equals(OSParameters.BBOX.getName())
			    || param.getName().equals(OSParameters.OUTPUT_FORMAT.getName());
		}
		return accept;
	    }

	    @Override
	    public void describeMismatch(Object item, Description mismatchDescription) {
	    }

	    @Override
	    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
	    }
	});

	Optional<OSParameter> count = WebRequestParameter.findParameter("ct", OSParameters.class);
	Assert.assertEquals("ct", count.get().getName());

    }

}
