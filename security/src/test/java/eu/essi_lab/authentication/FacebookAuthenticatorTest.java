package eu.essi_lab.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
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
 * Facebook authenticator test check requests made by users who wants to login
 * with facebook oauth2 protocol ang get an access token back.
 * 
 * @author pezzati
 */
public class FacebookAuthenticatorTest {

    @Rule
    public ExpectedException exEx = ExpectedException.none();
    private FacebookOAuth2Authenticator fAuth;
    private JsonNode invalidConf;
    private JsonNode validConf;
    private CloseableHttpClient httpClient;

    @Before
    public void setUpTest() throws URISyntaxException, IOException {
	fAuth = new FacebookOAuth2Authenticator();
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
	Mockito.when(invalidConf.get("client-id")).thenReturn(clientIdValue);
	Mockito.when(invalidConf.get("client-secret")).thenReturn(clientSecretValue);
	Mockito.when(invalidConf.get("redirect-uri")).thenReturn(redirectUriValue);
	Mockito.when(invalidConf.get("login-url")).thenReturn(loginUrlValue);
	Mockito.when(invalidConf.get("token-url")).thenReturn(tokenUrlValue);
	Mockito.when(invalidConf.get("userinfo-url")).thenReturn(userinfoUrlValue);
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
	Mockito.when(validConf.get("client-id")).thenReturn(clientIdValue);
	Mockito.when(validConf.get("client-secret")).thenReturn(clientSecretValue);
	Mockito.when(validConf.get("redirect-uri")).thenReturn(redirectUriValue);
	Mockito.when(validConf.get("login-url")).thenReturn(loginUrlValue);
	Mockito.when(validConf.get("token-url")).thenReturn(tokenUrlValue);
	Mockito.when(validConf.get("userinfo-url")).thenReturn(userinfoUrlValue);
    }

    @Test
    public void initializeAuthenticatorByANullJsonConfiguration() throws GSException {
	exEx.expect(GSException.class);
//	exEx.expectMessage("Configuration is null or empty.");
	fAuth.initialize(null);
	Assert.fail();
    }

    @Test
    public void jsonConfigurationLacksOneProperty() throws GSException {
	exEx.expect(GSException.class);
	fAuth.initialize(invalidConf);
	Assert.fail();
    }

    @Test
    public void initializeAuthenticatorByAGoodJsonConfiguration() throws GSException {
	fAuth.initialize(validConf);
    }

    @Test
    public void handleNullLoginRequest() throws GSException {
	exEx.expect(GSException.class);
	fAuth.handleLogin(null, null,null);
	Assert.fail();
    }

    @Test
    @Ignore
    public void redirectRaisesAnIOException() throws GSException, IOException {
	fAuth.initialize(validConf);
	HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
	Mockito.doThrow(new IOException()).when(httpResponse).sendRedirect(Mockito.anyString());
	exEx.expect(GSException.class);
	fAuth.handleLogin(null, httpResponse,null);
	Assert.fail();
    }

    @Test
    public void handleNullCallbackRequest() throws GSException {
	exEx.expect(GSException.class);
	fAuth.handleCallback(null);
	Assert.fail();
    }

    @Test
    public void handleCallbackRequestWithoutCode() throws GSException {
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	exEx.expect(GSException.class);
//	exEx.expectMessage("Service provider callback has no code.");
	fAuth.handleCallback(httpRequest);
	Assert.fail();
    }

    @Test
    @Ignore
    public void gettingTokenAfterCallbackRaisesAnException() throws GSException, IOException {
	fAuth.initialize(validConf);
	fAuth.setHttpClient(httpClient);
	Mockito.when(httpClient.execute(Mockito.any())).thenThrow(IOException.class);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	Mockito.when(httpRequest.getParameter("code")).thenReturn("mock.code");
	exEx.expect(GSException.class);
	fAuth.handleCallback(httpRequest);
    }

    @Test
    @Ignore
    public void gettingUserEmailAfterCallbackRaisesAnException() throws GSException, IOException {
	fAuth.initialize(validConf);
	fAuth.setHttpClient(httpClient);
	CloseableHttpResponse httpTokenResp = getValidTokenResp();
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpTokenResp).thenThrow(IOException.class);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	Mockito.when(httpRequest.getParameter("code")).thenReturn("mock.code");
	exEx.expect(GSException.class);
	fAuth.handleCallback(httpRequest);
    }

    @Test
    @Ignore
    public void getTokenValue() throws GSException, IOException {
	fAuth.initialize(validConf);
	fAuth.setHttpClient(httpClient);
	CloseableHttpResponse httpTokenResp = getValidTokenResp();
	CloseableHttpResponse httpEmailResp = getValidUserEmailResp();
	Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpTokenResp).thenReturn(httpEmailResp);
	HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
	Mockito.when(httpRequest.getParameter("code")).thenReturn("mock.code");
	Token actual = fAuth.handleCallback(httpRequest);
	Token expected = new Token();
	expected.setEmail("mocked.email");
	expected.setToken("mocked.token");
	expected.setTokenSecret(null);
	expected.setType("Bearer");
	expected.setServiceProvider("facebook");
	Assert.assertEquals(expected, actual);
    }

    private CloseableHttpResponse getValidTokenResp() throws IOException {
	CloseableHttpResponse resp = Mockito.mock(CloseableHttpResponse.class);
	HttpEntity entity = Mockito.mock(HttpEntity.class);
	InputStream stream = new ByteArrayInputStream("{\"access_token\":\"mocked.token\", \"token_type\":\"Bearer\"}".getBytes(StandardCharsets.UTF_8));
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
