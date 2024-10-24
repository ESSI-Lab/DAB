package eu.essi_lab.profiler.oaipmh.request.test;

import static org.junit.Assert.fail;

import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.oaipmh.handler.srvinfo.OAIPMHListSetsHandler;

public class ListSetsRequestTest {

    @Before
    public void init() {
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Test
    public void test() {

	Response response = null;

	GSSource gsSource = new GSSource();
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	gsSource.setEndpoint("http://");
	gsSource.setLabel("Label");
	String randomId = UUID.randomUUID().toString();
	gsSource.setUniqueIdentifier(randomId);

	OAIPMHListSetsHandler osListSetsHandler = new OAIPMHListSetsHandler();

	//
	//
	//
	DefaultConfiguration configuration = new DefaultConfiguration();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	ConfigurationWrapper.getDistributonSettings().forEach(s -> configuration.remove(s.getIdentifier()));

	ConfigurationWrapper.getHarvestingSettings().forEach(s -> configuration.remove(s.getIdentifier()));

	HarvestingSetting sourceSetting = HarvestingSettingLoader.load();

	sourceSetting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier(gsSource.getUniqueIdentifier());
	sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceLabel(gsSource.getLabel());
	sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceEndpoint(gsSource.getEndpoint());

	configuration.put(sourceSetting);
	configuration.clean();

	//
	//
	//

	String queryString = "http://profiler-oaipmh?verb=ListSets";
	WebRequest webRequest = WebRequest.createGET(queryString);

	try {
	    response = osListSetsHandler.handle(webRequest);
	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
	Assert.assertEquals(response.getHeaderString("Content-Type"), "application/xml;charset=UTF-8");

	String entity = (String) response.getEntity();

	Assert.assertThat("Output string contains ListSets", entity, new Matcher<String>() {

	    @Override
	    public void describeTo(Description description) {
	    }

	    @Override
	    public boolean matches(Object item) {

		return item.toString().contains(randomId) && item.toString().contains("ListSets");
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
