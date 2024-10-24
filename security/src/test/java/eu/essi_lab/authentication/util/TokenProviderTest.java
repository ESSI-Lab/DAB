package eu.essi_lab.authentication.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.model.exceptions.GSException;

public class TokenProviderTest {

    @Rule
    public ExpectedException expExc = ExpectedException.none();
    private TokenProvider tokenProvider;
    private Token token;

    @Before
    public void initTest() {
	tokenProvider = new TokenProvider();
	token = new Token();
    }

    @Test
    public void addNullValuedClaim() throws GSException {
	String claimName = "name";
	String[] claimValue = null;
	tokenProvider.addClaim(claimName, claimValue);
	String actualToken = tokenProvider.getToken();
	String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.e30.WPDCe7u67nslDXOKUpo5huMXlSjLe5ZczuZ5cFsjDBE";
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
	String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbXSwibmFtZSI6ImppbSJ9.WeNaxii56hopa2fdYY2iyASZ19Di69FUa-bRd1H2OPU";
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
	String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbInN1cGVydXNlciIsInN1cGVydmlzb3IiLCJjb3Vuc2Vsb3IiXSwibmFtZSI6ImppbSJ9.85da4nyo30hFcAdfHvY92lm3QMbuMywZJ411Q_Pue-4";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void computeJwtTokenByEmptyToken() throws GSException {
	String actualToken = tokenProvider.getToken(token);
	String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.e30.WPDCe7u67nslDXOKUpo5huMXlSjLe5ZczuZ5cFsjDBE";
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
	String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRoZW50aWNhdGlvbi1wcm92aWRlciI6InNlcnZpY2VQcm92aWRlciIsImVtYWlsIjoiZW1haWwifQ.3yo5w2H3HBHBWzvBRyBLwpEkTkWkyzUgrSHvcmNTl3g";
	Assert.assertEquals(expectedToken, actualToken);
    }

    @Test
    public void verifyNullToken() {
	String nullToken = null;
	expExc.expect(NullPointerException.class);
	tokenProvider.isValid(nullToken);
	Assert.fail();
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
	expExc.expect(NullPointerException.class);
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
