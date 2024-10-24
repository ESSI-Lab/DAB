package eu.essi_lab.accessor.acronet;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;

public class ACRONETAccessorExternalTestIT {

    @Test
    public void getPOSTHTTPTokenTest() throws Exception {
	String CLIENT_ID = "webdrops_api";
	String CLIENT_SECRET = System.getProperty("acronet.client.secret");
	String TOKEN_REQUEST_URL = "https://testauth.cimafoundation.org/auth/realms/webdrops/protocol/openid-connect/token";
	String username = System.getProperty("acronet.username");
	String password = System.getProperty("acronet.password");

	// String base64Credentials = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("grant_type", "password");
	params.put("username", username);
	params.put("password", password);
	params.put("client_id", CLIENT_ID);
	params.put("client_secret", CLIENT_SECRET);

	String grant_type = "password_credentials";

	// HashMap<String, String> headers = new HashMap<>();
	// headers.put("Content-Type", "application/x-www-form-urlencoded");
	// headers.put("Authorization", "Basic " + base64Credentials);

	HttpRequest httpPost = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_REQUEST_URL, //
		 params);

	HttpResponse<InputStream> response = null;

	try {
	    response = new Downloader().downloadResponse(httpPost);

	    String result = IOUtils.toString(response.body(), "UTF-8");

	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		String token = obj.optString("access_token");
		System.out.println(token);
		assertTrue(token != null);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
