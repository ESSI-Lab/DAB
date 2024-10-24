package eu.essi_lab.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Twitter authenticator test check requests made by users who wants to login
 * with twitter by oauth protocol to get an access token back.
 * 
 * @author pezzati
 */
@Ignore
public class TwitterAuthenticatorTest {

    @Rule
    public ExpectedException exEx = ExpectedException.none();
    private TwitterOAuthAuthenticator tAuth;
    private JsonNode invalidConf;
    private JsonNode validConf;
    private CloseableHttpClient httpClient;

    @Before
    public void setUpTest() throws URISyntaxException, IOException {
	tAuth = new TwitterOAuthAuthenticator();
	setUpInvalidConf();
	setUpValidConf();
	httpClient = Mockito.mock(CloseableHttpClient.class);
    }

    private void setUpInvalidConf() {
	invalidConf = Mockito.mock(JsonNode.class);
	JsonNode clientIdValue = Mockito.mock(JsonNode.class);
	Mockito.when(clientIdValue.asText()).thenReturn("XXXXX");
	JsonNode clientSecretValue = Mockito.mock(JsonNode.class);
	Mockito.when(clientSecretValue.asText()).thenReturn("XXXXX");
	JsonNode redirectUriValue = Mockito.mock(JsonNode.class);
	Mockito.when(redirectUriValue.asText()).thenReturn("https://myhost.org/redirect");
	JsonNode tokenUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(tokenUrlValue.asText()).thenReturn(null);
	JsonNode loginUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(loginUrlValue.asText()).thenReturn("https://noservice.org/login");
	JsonNode userinfoUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(userinfoUrlValue.asText()).thenReturn("https://noservice.org/userinfo");
	JsonNode requestTokenUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(requestTokenUrlValue.asText()).thenReturn("https://noservice.org/token/request");
	Mockito.when(invalidConf.get("client-id")).thenReturn(clientIdValue);
	Mockito.when(invalidConf.get("client-secret")).thenReturn(clientSecretValue);
	Mockito.when(invalidConf.get("redirect-uri")).thenReturn(redirectUriValue);
	Mockito.when(invalidConf.get("login-url")).thenReturn(loginUrlValue);
	Mockito.when(invalidConf.get("token-url")).thenReturn(tokenUrlValue);
	Mockito.when(invalidConf.get("userinfo-url")).thenReturn(userinfoUrlValue);
	Mockito.when(invalidConf.get("request-token-url")).thenReturn(requestTokenUrlValue);
    }

    private void setUpValidConf() {
	validConf = Mockito.mock(JsonNode.class);
	JsonNode clientIdValue = Mockito.mock(JsonNode.class);
	Mockito.when(clientIdValue.asText()).thenReturn("XXXXX");
	JsonNode clientSecretValue = Mockito.mock(JsonNode.class);
	Mockito.when(clientSecretValue.asText()).thenReturn("XXXXX");
	JsonNode redirectUriValue = Mockito.mock(JsonNode.class);
	Mockito.when(redirectUriValue.asText()).thenReturn("https://myhost.org/redirect");
	JsonNode tokenUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(tokenUrlValue.asText()).thenReturn("https://noservice.org/token");
	JsonNode loginUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(loginUrlValue.asText()).thenReturn("https://noservice.org/login");
	JsonNode userinfoUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(userinfoUrlValue.asText()).thenReturn("https://noservice.org/userinfo");
	JsonNode requestTokenUrlValue = Mockito.mock(JsonNode.class);
	Mockito.when(requestTokenUrlValue.asText()).thenReturn("https://noservice.org/token/request");
	Mockito.when(validConf.get("client-id")).thenReturn(clientIdValue);
	Mockito.when(validConf.get("client-secret")).thenReturn(clientSecretValue);
	Mockito.when(validConf.get("redirect-uri")).thenReturn(redirectUriValue);
	Mockito.when(validConf.get("login-url")).thenReturn(loginUrlValue);
	Mockito.when(validConf.get("token-url")).thenReturn(tokenUrlValue);
	Mockito.when(validConf.get("userinfo-url")).thenReturn(userinfoUrlValue);
	Mockito.when(validConf.get("request-token-url")).thenReturn(requestTokenUrlValue);
    }

    @Test
    public void initializeAuthenticatorByANullJsonConfiguration() throws GSException {
	exEx.expect(GSException.class);
//	exEx.expectMessage("Configuration is null or empty.");
	tAuth.initialize(null);
	Assert.fail();
    }

    @Test
    public void jsonConfigurationLacksOneProperty() throws GSException {
	exEx.expect(GSException.class);
	tAuth.initialize(invalidConf);
	Assert.fail();
    }

    @Test
    public void jsonConfigurationHasAnInvalidRedirectUrl() throws GSException {
	Mockito.when(validConf.get("redirect-uri").asText()).thenReturn(" http://noservice.org/login");
	exEx.expect(GSException.class);
	tAuth.initialize(validConf);
	Assert.fail();
    }

    @Test
    public void initializeAuthenticatorByAGoodJsonConfiguration() throws GSException {
	tAuth.initialize(validConf);
    }

    @Test
    public void handleNullLoginRequest() throws GSException {
	exEx.expect(GSException.class);
	tAuth.handleLogin(null, null,null);
	Assert.fail();
    }

    @Test
    public void handleNullLoginResponse() throws GSException {
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	exEx.expect(GSException.class);
	tAuth.handleLogin(httpRequest, null,null);
	Assert.fail();
    }

    @Test
    public void handleLoginRequestTokenRaiseAIOException() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
	Mockito.when(httpClient.execute(Mockito.any())).thenThrow(IOException.class);
	exEx.expect(GSException.class);
	tAuth.handleLogin(httpRequest, httpResponse,null);
	Assert.fail();
    }

    @Test
    public void handleLoginRequestTokenReturnsAnErrorStatus() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
	CloseableHttpResponse requestTokenResp = Mockito.mock(CloseableHttpResponse.class);
	StatusLine requestTokenRespStatus = Mockito.mock(StatusLine.class);
	Mockito.when(requestTokenRespStatus.getStatusCode()).thenReturn(500);
	Mockito.when(requestTokenResp.getStatusLine()).thenReturn(requestTokenRespStatus);
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(requestTokenResp);
	exEx.expect(GSException.class);
//	exEx.expectMessage("Requesting request_token fails.");
	tAuth.handleLogin(httpRequest, httpResponse,null);
	Assert.fail();
    }

    @Test
    public void redirectRaisesAnIOException() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpSession httpSession = Mockito.mock(HttpSession.class);
	Mockito.when(httpRequest.getSession()).thenReturn(httpSession);
	HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
	CloseableHttpResponse requestTokenResp = Mockito.mock(CloseableHttpResponse.class);
	StatusLine requestTokenRespStatus = Mockito.mock(StatusLine.class);
	Mockito.when(requestTokenRespStatus.getStatusCode()).thenReturn(200);
	Mockito.when(requestTokenResp.getStatusLine()).thenReturn(requestTokenRespStatus);
	HttpEntity requestTokenEntity = Mockito.mock(HttpEntity.class);
	Mockito.when(requestTokenEntity.getContent())
		.thenReturn(new ByteArrayInputStream("value1=1&value2=2&oauth_token=request_token&value3=3".getBytes(StandardCharsets.UTF_8)));
	Mockito.when(requestTokenResp.getEntity()).thenReturn(requestTokenEntity);
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(requestTokenResp);
	Mockito.doThrow(new IOException()).when(httpResponse).sendRedirect(Mockito.anyString());
	exEx.expect(GSException.class);
	tAuth.handleLogin(httpRequest, httpResponse,null);
	Assert.fail();
    }

    @Test
    public void handleNullCallbackRequest() throws GSException {
	exEx.expect(GSException.class);
	tAuth.handleCallback(null);
	Assert.fail();
    }

    @Test
    public void handleCallbackRequestWithoutToken() throws GSException {
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	exEx.expect(GSException.class);
//	exEx.expectMessage("Service provider callback has no oauth_token.");
	tAuth.handleCallback(httpRequest);
	Assert.fail();
    }

    @Test
    public void handleCallbackRequestWithoutVerifier() throws GSException {
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	Mockito.when(httpRequest.getParameter("oauth_token")).thenReturn("mock.token");
	exEx.expect(GSException.class);
//	exEx.expectMessage("Service provider callback has no oauth_verifier.");
	tAuth.handleCallback(httpRequest);
	Assert.fail();
    }

    @Test
    public void handleCallbackRequestWithUnexpectedOAuthToken() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	Mockito.when(httpClient.execute(Mockito.any())).thenThrow(IOException.class);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpSession httpSession = Mockito.mock(HttpSession.class);
	Mockito.when(httpSession.getAttribute(Mockito.anyString())).thenReturn("");
	Mockito.when(httpRequest.getSession(false)).thenReturn(httpSession);
	Mockito.when(httpRequest.getParameter("oauth_token")).thenReturn("mock.token");
	Mockito.when(httpRequest.getParameter("oauth_verifier")).thenReturn("mock.verifier");
	exEx.expect(GSException.class);
	tAuth.handleCallback(httpRequest);
    }

    @Test
    public void gettingTokenAfterCallbackRaisesAnException() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	Mockito.when(httpClient.execute(Mockito.any())).thenThrow(IOException.class);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpSession httpSession = Mockito.mock(HttpSession.class);
	Mockito.when(httpSession.getAttribute(Mockito.anyString())).thenReturn("mocked.attribute");
	Mockito.when(httpRequest.getSession(false)).thenReturn(httpSession);
	Mockito.when(httpRequest.getParameter("oauth_token")).thenReturn("mock.token");
	Mockito.when(httpRequest.getParameter("oauth_verifier")).thenReturn("mock.verifier");
	exEx.expect(GSException.class);
	tAuth.handleCallback(httpRequest);
    }

    @Test
    public void gettingUserEmailAfterCallbackRaisesAnException() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	CloseableHttpResponse httpTokenResp = getValidTokenResp();
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpTokenResp).thenThrow(IOException.class);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpSession httpSession = Mockito.mock(HttpSession.class);
	Mockito.when(httpSession.getAttribute(Mockito.anyString())).thenReturn("mocked.attribute");
	Mockito.when(httpRequest.getSession(false)).thenReturn(httpSession);
	Mockito.when(httpRequest.getParameter("oauth_token")).thenReturn("mock.token");
	Mockito.when(httpRequest.getParameter("oauth_verifier")).thenReturn("mock.verifier");
	exEx.expect(GSException.class);
	tAuth.handleCallback(httpRequest);
    }

    @Test
    public void getTokenValue() throws GSException, IOException {
	tAuth.initialize(validConf);
	tAuth.setHttpClient(httpClient);
	CloseableHttpResponse httpTokenResp = getValidTokenResp();
	CloseableHttpResponse httpEmailResp = getValidUserEmailResp();
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpTokenResp).thenReturn(httpEmailResp);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	HttpSession httpSession = Mockito.mock(HttpSession.class);
	Mockito.when(httpSession.getAttribute(Mockito.anyString())).thenReturn("mocked.attribute");
	Mockito.when(httpRequest.getSession(false)).thenReturn(httpSession);
	Mockito.when(httpRequest.getParameter("oauth_token")).thenReturn("mock.token");
	Mockito.when(httpRequest.getParameter("oauth_verifier")).thenReturn("mock.verifier");
	Token actual = tAuth.handleCallback(httpRequest);
	Token expected = new Token();
	expected.setEmail("mocked.email");
	expected.setToken("mocked.token");
	expected.setTokenSecret("mocked.secret");
	expected.setType(null);
	expected.setServiceProvider("twitter");
	Assert.assertEquals(expected, actual);
    }

    @Test
    @Ignore
    public void computeSignatureRaiseAnException() throws Exception {
	List<String[]> oauth_params = Mockito.spy(new ArrayList<>());
	Mockito.when(oauth_params.size()).thenThrow(InvalidKeyException.class);
	List<String[]> additional_params = Mockito.spy(new ArrayList<>());
	exEx.expect(GSException.class);
	tAuth.createSignature("POST", "non.encoded.baseurl.org", oauth_params, additional_params, "client.secret", "access.token.secret");
    }

    @Test
    public void computeSignatureWithNoTokenSecretGoesFine() throws Exception {
	List<String[]> oauth_params = new ArrayList<>();
	List<String[]> additional_params = new ArrayList<>();
	tAuth.createSignature("POST", "non.encoded.baseurl.org", oauth_params, additional_params, "client.secret", null);
    }

    private CloseableHttpResponse getValidTokenResp() throws IOException {
	CloseableHttpResponse resp = Mockito.mock(CloseableHttpResponse.class);
	HttpEntity entity = Mockito.mock(HttpEntity.class);
	InputStream stream = new ByteArrayInputStream(
		"value1=1&oauth_token=mocked.token&oauth_token_secret=mocked.secret&value2=2".getBytes(StandardCharsets.UTF_8));
	Mockito.when(entity.getContent()).thenReturn(stream);
	Mockito.when(resp.getEntity()).thenReturn(entity);
	return resp;
    }

    private CloseableHttpResponse getValidUserEmailResp() throws IOException {
	CloseableHttpResponse resp = Mockito.mock(CloseableHttpResponse.class);
	HttpEntity entity = Mockito.mock(HttpEntity.class);
	InputStream stream = new ByteArrayInputStream("{\"email\":\"mocked.email\"}".getBytes(StandardCharsets.UTF_8));
	Mockito.when(entity.getContent()).thenReturn(stream);
	Mockito.when(resp.getEntity()).thenReturn(entity);
	return resp;
    }
}
