package eu.essi_lab.authentication.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.authentication.token.Token;
import eu.essi_lab.authentication.token.TokenProvider;
import eu.essi_lab.model.exceptions.GSException;

public class TokenProviderTest {

    @Rule
    public ExpectedException expExc = ExpectedException.none();
    private TokenProvider tokenProvider;
    private Token token;

    @Before
    public void initTest() {
	tokenProvider = new TokenProvider("hMACSecretPassphrase");
	token = new Token();
    }

    @Test
    public void addNullValuedClaim() throws GSException {
	String claimName = "name";
	String[] claimValue = null;
	tokenProvider.addClaim(claimName, claimValue);
	String actualToken = tokenProvider.getToken();
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4NTI0NzIxNDYsIm5hbWUiOm51bGx9.yAFaHr_oIJXImCEf6qRv6XZzRHrrwLsZ0T6Sd3-5YUI";
	Assert.assertEquals(expectedToken.split("\\.")[0], actualToken.split("\\.")[0]);
    }

    @Test
    public void getTokenWhoHasAnEmptyClaim() throws GSException {
	String usernameClaimName = "name";
	String usernameClaimValue = "jim";
	String roleClaimName = "role";
	String[] roleClaimValue = new String[] {};
	String actualToken = tokenProvider.addClaim(usernameClaimName, usernameClaimValue).addClaim(roleClaimName, roleClaimValue)
		.getToken();
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4NTI0NzIxNDYsIm5hbWUiOiJqaW0iLCJyb2xlIjpbXX0.5xQthdnAPSGEoT6nQqlrNUj7ppUP9wVm0bmBdcRn7uw";
	Assert.assertEquals(expectedToken.split("\\.")[0], actualToken.split("\\.")[0]);
    }

    @Test
    public void getValidJwtToken() throws GSException {
	String usernameClaimName = "name";
	String usernameClaimValue = "jim";
	String roleClaimName = "role";
	String[] roleClaimValue = new String[] { "superuser", "supervisor", "counselor" };
	String actualToken = tokenProvider.addClaim(roleClaimName, roleClaimValue).addClaim(usernameClaimName, usernameClaimValue)
		.getToken();
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4NTI0NzIxNDYsInJvbGUiOlsic3VwZXJ1c2VyIiwic3VwZXJ2aXNvciIsImNvdW5zZWxvciJdLCJuYW1lIjoiamltIn0.UzvkO46IHUShi5ATSCC5R6JYJzSYz5eVmN8RCn2_ZoQ";
	Assert.assertEquals(expectedToken.split("\\.")[0], actualToken.split("\\.")[0]);
    }

    @Test
    public void computeJwtTokenByEmptyToken() throws GSException {
	String actualToken = tokenProvider.getToken(token);
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4NTI0NzIxNDYsImVtYWlsIjpudWxsLCJhdXRoZW50aWNhdGlvbi1wcm92aWRlciI6bnVsbH0.9hdQD7gYjMlT7P8NrsgKVJCowV5rLjmPbF-EBIZamIs";
	Assert.assertEquals(expectedToken.split("\\.")[0], actualToken.split("\\.")[0]);
    }

    @Test
    public void computeJwtTokenByToken() throws GSException {
	token.setClientURL("someClientUrl");
	token.setEmail("email");
	token.setServiceProvider("serviceProvider");
	token.setToken("token");
	token.setTokenSecret("tokenSecret");
	token.setType("type");
	String actualToken = tokenProvider.getToken(token);
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4NTI0NzIxNDYsImVtYWlsIjoiZW1haWwiLCJhdXRoZW50aWNhdGlvbi1wcm92aWRlciI6InNlcnZpY2VQcm92aWRlciJ9.H4gVU-MY7nUVB1ApSyORYmy3Ep0AdX3tm27ZHZpw03g";
	Assert.assertEquals(expectedToken.split("\\.")[0], actualToken.split("\\.")[0]);
    }

    @Test
    public void verifyNullToken() {
	String nullToken = null;

	Assert.assertFalse(tokenProvider.isValid(nullToken));
    }

    @Test
    public void verifyInvalidToken() {
	String invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImVsdmlzIiwiZW1haWwiOiJ0aGVraW5nQGhlbGwub3JnIn0.PGO3QCci-YOkKqmC7K3SLqRMW3QQB8ba55yyOq7KZrA";
	boolean result = tokenProvider.isValid(invalidToken);
	Assert.assertFalse(result);
    }

    @Test
    public void verifyValidToken() {
	String validToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRoZW50aWNhdGlvbi1wcm92aWRlciI6InNlcnZpY2VQcm92aWRlciIsImVtYWlsIjoiZW1haWwifQ.3yo5w2H3HBHBWzvBRyBLwpEkTkWkyzUgrSHvcmNTl3g";
	boolean result = tokenProvider.isValid(validToken);
	Assert.assertTrue(result);
    }

    @Test
    public void decodeNullToken() throws GSException {
	String nullToken = null;
	expExc.expect(GSException.class);
	tokenProvider.decode(nullToken);
	Assert.fail();
    }

    @Test
    public void decodeInvalidToken() throws GSException {
	String invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImVsdmlzIiwiZW1haWwiOiJ0aGVraW5nQGhlbGwub3JnIn0.PGO3QCci-YOkKqmC7K3SLqRMW3QQB8ba55yyOq7KZrA";
	expExc.expect(GSException.class);
	tokenProvider.decode(invalidToken);
	Assert.fail();
    }

    @Test
    public void decodeValidToken() throws GSException {
	String validToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRoZW50aWNhdGlvbi1wcm92aWRlciI6InNlcnZpY2VQcm92aWRlciIsImVtYWlsIjoiZW1haWwifQ.3yo5w2H3HBHBWzvBRyBLwpEkTkWkyzUgrSHvcmNTl3g";
	String expectedEmail = "email";
	String expectedAuthProvider = "serviceProvider";
	JsonNode json = tokenProvider.decode(validToken);
	String actualEmail = json.get("email").asText();
	Assert.assertEquals(expectedEmail, actualEmail);
	String actualAuthProvider = json.get("authentication-provider").asText();
	Assert.assertEquals(expectedAuthProvider, actualAuthProvider);
    }
}
