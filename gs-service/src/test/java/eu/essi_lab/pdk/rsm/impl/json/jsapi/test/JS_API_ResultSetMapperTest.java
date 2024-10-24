package eu.essi_lab.pdk.rsm.impl.json.jsapi.test;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;
 
public class JS_API_ResultSetMapperTest {

    @Before
    public void init() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void test() {

	InputStream stream = JS_API_ResultSetMapperTest.class.getClassLoader().getResourceAsStream("dataset.xml");
	try {

	    Dataset dataset = Dataset.create(stream);
	    JS_API_ResultSetMapper mapper = new JS_API_ResultSetMapper();

	    ResultSet<GSResource> resultSet = new ResultSet<GSResource>();
	    resultSet.setResultsList(Arrays.asList(new GSResource[] { dataset }));

	    DiscoveryMessage message = new DiscoveryMessage();

	    ResultSet<String> rsString = mapper.map(message, resultSet);

	    List<String> resultsList = rsString.getResultsList();

	    for (String string : resultsList) {
		try {
		    new JSONObject(string);
		} catch (Exception ex) {
		    ex.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	} catch (Exception e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}

    }
}
