package eu.essi_lab.accessor.hiscentral.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliAccessor;
import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliConnector;
import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliConnectorSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class HISCentralFriuliAccessorExternalTestIT {

    @Test
    public void getHTTPTokenTest() throws Exception {

	String user = System.getProperty("friuli.user");
	String psw = System.getProperty("friuli.password");

	// HttpClient httpClient = HttpClientBuilder.create().build();
	// HttpPost httpPost = new HttpPost("https://omnia-develop.osmer.fvg.it/api/ws/login");
	//
	// List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	// params.add(new BasicNameValuePair("email", user));
	// params.add(new BasicNameValuePair("password", psw));
	// httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	// Execute and get the response

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("email", user);
	params.put("password", psw);

	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		"https://omnia-develop.osmer.fvg.it/api/ws/login", //
		params);

	try {

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    String result = IOUtils.toString(response.body(), "UTF-8");
	    if (result != null && !result.isEmpty()) {
		String token = result.startsWith("\"") ? result.substring(1, result.length() - 1) : result;
		assertTrue(token != null);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	// HttpResponse response = null;
	// try {
	// response = httpClient.execute(httpPost);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// String result = null;
	// try {
	// result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
	// if (result != null && !result.isEmpty()) {
	// JSONObject obj = new JSONObject(result);
	// String token = obj.optString("access_token");
	// System.out.println(token);
	// assertTrue(token != null);
	// }
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }

    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	HISCentralFriuliAccessor accessor = new HISCentralFriuliAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("SIR_FRIULI", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(HISCentralFriuliConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint(HISCentralFriuliConnector.DEFAULT_BASE_URL);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	HISCentralFriuliConnector connector = accessor.getConnector();

	Assert.assertEquals(HISCentralFriuliConnector.class, connector.getClass());

	HISCentralFriuliConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	oaiConnectorSetting.setMaxRecords(1); // max 1 records

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(64, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	JSONObject object = new JSONObject(metadata);

	Assert.assertTrue(object.has("dataset-info"));
	Assert.assertTrue(object.has("sensor-info"));
    }
}
