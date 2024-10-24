package eu.essi_lab.accessor.saeon;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Roberto
 */
public class SAEONAccessorExternalTestIT {

    @Test
    public void getPOSTHTTPTokenTest() throws Exception {
	String CLIENT_ID = System.getProperty("saeon.client.id");
	String CLIENT_SECRET = System.getProperty("saeon.client.secret");
	String TOKEN_REQUEST_URL = "https://proto.saeon.ac.za/auth/oauth2/token";
	String SCOPE = "odp.catalog:read";

	String base64Credentials = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

	String grant_type = "client_credentials";

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Content-Type", "application/x-www-form-urlencoded");
	headers.put("Authorization", "Basic " + base64Credentials);

	HttpRequest httpPost = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_REQUEST_URL, //
		"grant_type=" + grant_type + "&scope=" + SCOPE, headers);

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

    @Test
    public void getHTTPTokenTest() throws Exception {
	String CLIENT_ID = System.getProperty("saeon.client.id");
	String CLIENT_SECRET = System.getProperty("saeon.client.secret");
	String TOKEN_REQUEST_URL = "https://proto.saeon.ac.za/auth/oauth2/token";
	String SCOPE = "odp.catalog:read";

	String command = "curl -u " + CLIENT_ID + ":" + CLIENT_SECRET + " " + TOKEN_REQUEST_URL + " -X POST -d " + "&scope=" + SCOPE
		+ "&grant_type=client_credentials";
	Process process = Runtime.getRuntime().exec(command);
	System.out.println(command);
	InputStream is = process.getInputStream();
	String result = null;
	String result2 = null;
	try {
	    result = IOUtils.toString(is, "UTF-8");
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

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    // @Test
    public void listRecordsTest() throws Exception {

	// String CLIENT_ID = System.getProperty("saeon.client.id");
	// String CLIENT_SECRET = System.getProperty("saeon.client.secret");
	// String TOKEN_REQUEST_URL = "https://proto.saeon.ac.za/auth/oauth2/token";
	// String SCOPE = "odp.catalog:read";
	// String REDIRECT_URI = "https://proto.saeon.ac.za/api";
	// OAuthClient client = new OAuthClient(new URLConnectionClient());
	// OAuthClientRequest request =
	// OAuthClientRequest.tokenLocation(TOKEN_REQUEST_URL).setGrantType(GrantType.CLIENT_CREDENTIALS)
	// .setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET).setScope(org.apache.commons.lang3.StringUtils.join(SCOPE,
	// " "))
	// .buildBodyMessage();
	//
	// // OAuthJSONAccessTokenResponse reeeee = client.accessToken(request);
	// // OAuthJSONAccessTokenResponse ress = client.accessToken(request, OAuth.HttpMethod.POST);
	//
	// // String token = client.accessToken(request, OAuth.HttpMethod.POST,
	// // OAuthJSONAccessTokenResponse.class).getAccessToken();
	// // System.out.println(token);
	//
	// String token = client.accessToken(request, "POST", OAuthJSONAccessTokenResponse.class).getAccessToken();
	//
	// System.out.println(token);
	// assertTrue(token != null);
    }

}
