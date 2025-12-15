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

import java.util.*;

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
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpudWxsfQ.Nc4NUvFqk8tFA5IOS1ahj3PMXTAtPV5kJBIdW3Gp8ZQ";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void getTokenWhoHasAnEmptyClaim() throws GSException {
	String usernameClaimName = "name";
	String usernameClaimValue = "jim";
	String roleClaimName = "role";
	String[] roleClaimValue = new String[] {};
	String actualToken = tokenProvider.addClaim(usernameClaimName, usernameClaimValue).addClaim(roleClaimName, roleClaimValue)
		.getToken();
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiamltIiwicm9sZSI6W119.OpP5yDhM85HXxJRrHwc1vV1ECG7LGPr8QEim0d4G64U";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void getValidJwtToken() throws GSException {
	String usernameClaimName = "name";
	String usernameClaimValue = "jim";
	String roleClaimName = "role";
	String[] roleClaimValue = new String[] { "superuser", "supervisor", "counselor" };
	String actualToken = tokenProvider.addClaim(roleClaimName, roleClaimValue).addClaim(usernameClaimName, usernameClaimValue)
		.getToken();
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjpbInN1cGVydXNlciIsInN1cGVydmlzb3IiLCJjb3Vuc2Vsb3IiXSwibmFtZSI6ImppbSJ9.7M8ZFkPhGmcYbdSKOMXMZqewuI02a97x1dfdrLusphE";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void computeJwtTokenByEmptyToken() throws GSException {
	String actualToken = tokenProvider.getToken(token);
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6bnVsbCwiYXV0aGVudGljYXRpb24tcHJvdmlkZXIiOm51bGx9.nPRbFyPes-9xs8QorrHeiEb59UmxtDyiS3VARQIzTsc";
	Assert.assertEquals(expectedToken, actualToken);
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
	String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImVtYWlsIiwiYXV0aGVudGljYXRpb24tcHJvdmlkZXIiOiJzZXJ2aWNlUHJvdmlkZXIifQ.67q7ScGcE6oHCHoZdkIvM2hRh5QoOXTTItxKKJyPm44";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void verifyNullToken() {
	String nullToken = null;

	Assert.assertFalse(tokenProvider.isValid(nullToken));
    }

    @Test
    public void verifyInvalidToken() {
	boolean result = tokenProvider.isValid(UUID.randomUUID().toString());
	Assert.assertFalse(result);
    }

    /**
     * @return
     */
    private static String getValidToken() {

	return System.getProperty("token");
    }

    @Test
    public void verifyValidToken() {
	String validToken = getValidToken();
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
