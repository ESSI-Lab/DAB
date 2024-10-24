package eu.essi_lab.authentication;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static eu.essi_lab.authentication.OAuth2Authenticator.ERR_WITH_TWITTER_MSG;
import static eu.essi_lab.authentication.OAuth2Authenticator.INVALID_OAUTH_PARAM_VALUE_ERR_ID;
import static eu.essi_lab.authentication.OAuth2Authenticator.MISSING_OATH_CONF_FILE_ERR_ID;
import static eu.essi_lab.authentication.OAuth2Authenticator.NULL_HTTP_REQUEST_PROVIDED_ERR_ID;
import static eu.essi_lab.authentication.OAuth2Authenticator.NULL_HTTP_RESPONSE_PROVIDED_ERR_ID;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.authentication.util.RFC3986Encoder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class TwitterOAuthAuthenticator extends OAuthAuthenticator {

    public static final String TWITTER = "twitter";
    private static final String HMAC_SHA1 = "HMAC-SHA1";
    private static final String UTF8 = "UTF-8";
    private String clientId;
    private String clientSecret;
    private URI redirectUri;
    private String requestTokenUrl;
    private String loginUrl;
    private String tokenUrl;
    private String userInfoUrl;
    private CloseableHttpClient httpClient;
    private Logger log = GSLoggerFactory.getLogger(getClass());
    private static final String SIGNATURE_EXCEPTION_ERR_ID = "SIGNATURE_EXCEPTION_ERR_ID";
    private static final String ENCODING_EXCEPTION_ERR_ID = "ENCODING_EXCEPTION_ERR_ID";
    private static final String BAD_CODE_TOKEN_ERR_ID = "BAD_CODE_TOKEN_ERR_ID";
    private static final String IOEXCEPTION_TWITTER_ERR_ID = "IOEXCEPTION_TWITTER_ERR_ID";
    private static final String NO_OAUTH_TOKEN_ERR_ID = "NO_OAUTH_TOKEN_ERR_ID";
    private static final String NO_OAUTH_VERIFIER_ERR_ID = "NO_OAUTH_VERIFIER_ERR_ID";
    private static final String OAUTH_TOKEN_MISMATCH_VERIFIER_ERR_ID = "OAUTH_TOKEN_MISMATCH_VERIFIER_ERR_ID";

    @Override
    public void initialize(JsonNode conf) throws GSException {

	if (conf == null)
	    throw GSException.createException(this.getClass(), "Missing configuraiton file", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, MISSING_OATH_CONF_FILE_ERR_ID);

	try {

	    redirectUri = new URI(OAuthAuthenticator.getConfigurationValue("redirect-uri", conf));

	} catch (URISyntaxException e) {

	    log.error("Invalid redirect URI found {}", OAuthAuthenticator.getConfigurationValue("redirect-uri", conf));

	    throw GSException.createException(this.getClass(), "Invalid redirect URI found", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, INVALID_OAUTH_PARAM_VALUE_ERR_ID, e);
	}

	requestTokenUrl = OAuthAuthenticator.getConfigurationValue("request-token-url", conf);
	loginUrl = OAuthAuthenticator.getConfigurationValue("login-url", conf);
	tokenUrl = OAuthAuthenticator.getConfigurationValue("token-url", conf);
	userInfoUrl = OAuthAuthenticator.getConfigurationValue("userinfo-url", conf);
    }

    @Override
    public void setRedirectURI(URI uri) {
	redirectUri = uri;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
	this.httpClient = httpClient;
    }

    /**
     * Twitter implements its login service with an OAuth1.0 protocol. This mean
     * you have to ask for a peculiar oauth_token before redirect the user,
     * store the token's values you get back in a session, then redirect user.
     * The oauth_token you store must match with the oauth_token twitter will
     * give you by the callback. Let's summarize:<br>
     * - client wants to login by twitter, so call webapp's login endpoint,<br>
     * - webapp asks twitter about a token to perform a 'safe' redirect,<br>
     * - twitter returns webapp an oauth_token, webapp store it in a session and
     * redirect user giving the oauth_token as queryparam.
     */
    @Override
    public void handleLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String clienturl) throws GSException {
	if (httpRequest == null) {

	    log.error("Found null HttpServletRequest");

	    throw GSException.createException(this.getClass(), "Found null HttpServletRequest", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_REQUEST_PROVIDED_ERR_ID);
	}

	if (httpResponse == null) {

	    log.error("Found null HttpServletResponse");

	    throw GSException.createException(this.getClass(), "Found null HttpServletResponse", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_RESPONSE_PROVIDED_ERR_ID);
	}

	log.info("Handling login request.");

	String base64Clienturl = "";
	try {
	    base64Clienturl = RFC3986Encoder.encode(clienturl, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    log.warn("Can't RFC3986 encode {}", clienturl, e);
	}

	String ruri = (redirectUri + "/" + base64Clienturl).toString();

	log.debug("Redirect URI: {}", ruri);

	List<String[]> headers = buildNameValueCollection(
		new String[] { "oauth_callback", "oauth_consumer_key", "oauth_nonce", "oauth_signature_method", "oauth_timestamp",
			"oauth_version", "oauth_token" },
		new String[] { ruri, clientId, UUID.randomUUID().toString().replaceAll("-", ""), HMAC_SHA1,
			Long.toString(System.currentTimeMillis() / 1000), "1.0", "" });

	String oauthSignature = "";

	HttpPost postTokenReq = new HttpPost(requestTokenUrl);

	oauthSignature = createSignature("POST", requestTokenUrl, headers, new ArrayList<String[]>(), clientSecret, "");
	headers.add(new String[] { "oauth_signature", oauthSignature });

	postTokenReq.addHeader("Authorization", buildAuthorizationHeader(headers));

	CloseableHttpResponse postTokenResp = null;

	try {
	    postTokenResp = httpClient.execute(postTokenReq);

	    if (postTokenResp.getStatusLine().getStatusCode() != 200) {

		log.error("Requesting request_token fails with code {}", postTokenResp.getStatusLine().getStatusCode());

		throw GSException.createException(this.getClass(), "IOException requesting token", null, null, ErrorInfo.ERRORTYPE_SERVICE,
			ErrorInfo.SEVERITY_ERROR, BAD_CODE_TOKEN_ERR_ID);

	    }

	    String responseBody = null;

	    responseBody = readBody(postTokenResp.getEntity().getContent());

	    String oAuthToken = retreiveFromResponseBody("oauth_token", responseBody);
	    HttpSession oAuthSession = httpRequest.getSession();
	    oAuthSession.setAttribute(oAuthToken, oAuthToken);
	    log.info("Succesfully obtained a request token.");

	    httpResponse.sendRedirect(String.format(loginUrl, oAuthToken));

	} catch (IOException e) {

	    log.error("IOException sending redirect during login handling");

	    throw GSException.createException(this.getClass(), "IOException sending redirect during login handling", null, null,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_TWITTER_ERR_ID, e);
	}

    }

    private String readBody(InputStream body) {
	String responseBody = null;
	try (Scanner s = new Scanner(body)) {
	    responseBody = s.useDelimiter("\\A").hasNext() ? s.next() : "";
	}
	return responseBody;
    }

    private String retreiveFromResponseBody(String attribute, String body) {
	String[] attributes = body.split("&");
	HashMap<String, String> attributeSet = new HashMap<>();
	for (String attrb : attributes) {
	    String[] keyvalue = attrb.split("=");
	    if (keyvalue.length > 1) {
		attributeSet.put(keyvalue[0], keyvalue[1]);
	    }
	}
	if (attributeSet.containsKey(attribute)) {
	    return attributeSet.get(attribute);
	}
	return null;
    }

    private String buildAuthorizationHeader(List<String[]> headers) throws GSException {
	StringBuffer authorizationHeader = new StringBuffer();
	authorizationHeader.append("OAuth ");

	for (String[] header : headers) {

	    try {

		authorizationHeader.append(RFC3986Encoder.encode(header[0], UTF8)).append("=\"")
			.append(RFC3986Encoder.encode(header[1], UTF8)).append("\",");

	    } catch (UnsupportedEncodingException e) {

		throw GSException.createException(this.getClass(), "Error in executing RFC3986 Encoding", null, null,
			ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ENCODING_EXCEPTION_ERR_ID);

	    }
	}

	authorizationHeader.setLength(authorizationHeader.length() - 1);
	return authorizationHeader.toString();

    }

    /**
     * OAuth protocol expect this step to check given oauth_token parameter with
     * the previously stored oauth_token, obtained before redirection (that's
     * why we can't handle oauth without sessions).
     */
    @Override
    public Token handleCallback(HttpServletRequest httpRequest) throws GSException {
	if (httpRequest == null) {

	    log.error("Found null HttpServletRequest");

	    throw GSException.createException(this.getClass(), "Found null HttpServletRequest", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_REQUEST_PROVIDED_ERR_ID);
	}

	String oAuthToken = httpRequest.getParameter("oauth_token");

	if (oAuthToken == null || oAuthToken.isEmpty()) {
	    log.error("Service provider callback has no oauth_token");

	    throw GSException.createException(this.getClass(), "Service provider callback has no oauth_token", null, ERR_WITH_TWITTER_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, NO_OAUTH_TOKEN_ERR_ID);
	}

	String oAuthVerifier = httpRequest.getParameter("oauth_verifier");

	if (oAuthVerifier == null || oAuthVerifier.isEmpty()) {
	    log.error("Service provider callback has no oauth_verifier");

	    throw GSException.createException(this.getClass(), "Service provider callback has no oauth_verifier", null,
		    ERR_WITH_TWITTER_MSG, ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, NO_OAUTH_VERIFIER_ERR_ID);
	}

	String previouslyStoredOAuthToken = (String) httpRequest.getSession(false).getAttribute(oAuthToken);

	if (previouslyStoredOAuthToken == null || previouslyStoredOAuthToken.isEmpty()) {

	    log.error("Token mismatch");

	    throw GSException.createException(this.getClass(), "Token mismatch", null, ERR_WITH_TWITTER_MSG, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, OAUTH_TOKEN_MISMATCH_VERIFIER_ERR_ID);

	}

	log.info("Handling callback request.");

	HttpPost postTokenReq = new HttpPost(String.format(tokenUrl + "?oauth_verifier=%s", oAuthVerifier));

	List<String[]> headers = buildNameValueCollection(
		new String[] { "oauth_callback", "oauth_consumer_key", "oauth_nonce", "oauth_signature_method", "oauth_timestamp",
			"oauth_version", "oauth_token" },
		new String[] { redirectUri.toString(), clientId, UUID.randomUUID().toString().replaceAll("-", ""), HMAC_SHA1,
			Long.toString(System.currentTimeMillis() / 1000), "1.0", oAuthToken });
	List<String[]> parameters = buildNameValueCollection(new String[] { "oauth_verifier" }, new String[] { oAuthVerifier });

	String oauthSignature = createSignature("POST", tokenUrl, headers, parameters, clientSecret, "");
	headers.add(new String[] { "oauth_signature", oauthSignature });
	postTokenReq.addHeader("Authorization",

		buildAuthorizationHeader(headers));

	CloseableHttpResponse postTokenResp;
	try {
	    postTokenResp = httpClient.execute(postTokenReq);

	    String requestTokenBodyResp = null;
	    try (Scanner s = new Scanner(postTokenResp.getEntity().getContent())) {
		requestTokenBodyResp = s.useDelimiter("\\A").hasNext() ? s.next() : "";
	    }
	    String twitterOAuthToken = retreiveFromResponseBody("oauth_token", requestTokenBodyResp);
	    String twitterOAuthTokenSecret = retreiveFromResponseBody("oauth_token_secret", requestTokenBodyResp);

	    headers = buildNameValueCollection(
		    new String[] { "oauth_consumer_key", "oauth_nonce", "oauth_signature_method", "oauth_timestamp", "oauth_version",
			    "oauth_token" },
		    new String[] { clientId, UUID.randomUUID().toString().replaceAll("-", ""), HMAC_SHA1,
			    Long.toString(System.currentTimeMillis() / 1000), "1.0", twitterOAuthToken });
	    parameters = buildNameValueCollection(new String[] { "include_email" }, new String[] { "true" });

	    HttpGet getUserEmailReq = new HttpGet(userInfoUrl + "?include_email=true");
	    String getUserEmailSignature = createSignature("GET", userInfoUrl, headers, parameters, clientSecret, twitterOAuthTokenSecret);
	    headers.add(new String[] { "oauth_signature", getUserEmailSignature });
	    getUserEmailReq.addHeader("Authorization", buildAuthorizationHeader(headers));

	    CloseableHttpResponse getUserEmailResp = httpClient.execute(getUserEmailReq);
	    String userEmail = new ObjectMapper().readValue(getUserEmailResp.getEntity().getContent(), JsonNode.class).get("email")
		    .asText();
	    log.info(String.format("Succesfull login about user %s. Token is in session.", userEmail));
	    Token token = new Token();
	    token.setEmail(userEmail);
	    token.setToken(twitterOAuthToken);
	    token.setTokenSecret(twitterOAuthTokenSecret);
	    token.setType(null);
	    token.setServiceProvider(TWITTER);
	    return token;

	} catch (IOException e) {

	    log.error("Exception from twitter service");

	    throw GSException.createException(this.getClass(), "Exception from twitter service", null, ERR_WITH_TWITTER_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_TWITTER_ERR_ID, e);
	}
    }

    private List<String[]> buildNameValueCollection(String[] name, String[] value) {
	List<String[]> nameValueCollection = new ArrayList<>();
	for (int i = 0; i < name.length; i++) {
	    nameValueCollection.add(new String[] { name[i], value[i] });
	}
	return nameValueCollection;
    }

    public String createSignature(String http_method, String nonEncodedBaseURL, List<String[]> oauth_params,
	    List<String[]> additional_params, String clientSecret, String accessTokenSecret) throws GSException {
	try {
	    String upperCaseMethod = http_method.toUpperCase();
	    String[][] tosort_params = new String[oauth_params.size() + additional_params.size()][2];
	    int count = 0;
	    for (String[] p : oauth_params) {
		tosort_params[count] = new String[] { RFC3986Encoder.encode(p[0], UTF8), RFC3986Encoder.encode(p[1], UTF8) };
		count++;
	    }
	    for (String[] p : additional_params) {
		tosort_params[count] = new String[] { RFC3986Encoder.encode(p[0], UTF8), RFC3986Encoder.encode(p[1], UTF8) };
		count++;
	    }
	    List<String[]> sortedList = Arrays.asList(tosort_params);
	    Collections.sort(sortedList, new Comparator<String[]>() {
		@Override
		public int compare(String[] o1, String[] o2) {
		    return o1[0].compareTo(o2[0]);
		}
	    });
	    String paramsSign = "";
	    for (int i = 0; i < sortedList.size(); i++) {
		String[] p = sortedList.get(i);
		paramsSign += p[0];
		paramsSign += "=";
		paramsSign += p[1];
		if (i < sortedList.size() - 1) {
		    paramsSign += "&";
		}
	    }
	    String signBaseString = upperCaseMethod;
	    signBaseString += "&";
	    signBaseString += RFC3986Encoder.encode(nonEncodedBaseURL, UTF8);
	    signBaseString += "&";
	    signBaseString += RFC3986Encoder.encode(paramsSign, UTF8);
	    String signingKey = RFC3986Encoder.encode(clientSecret, UTF8) + "&";
	    if (accessTokenSecret != null)
		signingKey += RFC3986Encoder.encode(accessTokenSecret, UTF8);
	    return calculateRFC2104HMAC(signBaseString, signingKey);

	} catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException e) {

	    log.error("Can't create signture");

	    throw GSException.createException(this.getClass(), "Error creating signature", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, SIGNATURE_EXCEPTION_ERR_ID, e);

	}
    }

    private static String calculateRFC2104HMAC(String data, String key) throws InvalidKeyException, NoSuchAlgorithmException {
	SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
	Mac mac = Mac.getInstance("HmacSHA1");
	mac.init(signingKey);
	byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
	return Base64.getEncoder().encodeToString(rawHmac);
    }
}
