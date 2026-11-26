package eu.essi_lab.test.authorization.userfinder.test;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.userfinder.InvalidBearerTokenException;
import eu.essi_lab.authorization.userfinder.KeycloakBearerTokenResolver;
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.setting.database.SecondaryUsersDatabaseSetting;
import eu.essi_lab.model.auth.GSUser;
import jakarta.servlet.http.HttpServletRequest;

public class KeycloakBearerTokenResolverTest {

    private static final String ISSUER = "https://keycloak.example.org/realms/users";

    private RSAKey key;
    private SecondaryUsersDatabaseSetting setting;
    private KeycloakBearerTokenResolver resolver;

    @Before
    public void before() throws Exception {

	key = new RSAKeyGenerator(2048).keyID("k1").generate();

	setting = new SecondaryUsersDatabaseSetting();
	setting.setDatabaseUri("https://keycloak.example.org");
	setting.setDatabaseName("users");
	setting.setUsersRole("his_central");
	setting.setDownloadTiers("standard=100,20\nlarge=2000,200");

	resolver = new KeycloakBearerTokenResolver(setting, new ImmutableJWKSet<>(new JWKSet(key.toPublicJWK())));
    }

    //
    // valid tokens
    //

    @Test
    public void validToken() throws Exception {

	GSUser user = resolver.resolve(token(claims(List.of("api", "downloads", "view:his-central", "download-tier:standard",
		"download-tier:large")).build()));

	Assert.assertEquals("user-1", user.getIdentifier());
	Assert.assertEquals("his_central", user.getRole());
	Assert.assertEquals(KeycloakBearerTokenResolver.AUTH_PROVIDER, user.getAuthProvider());
	Assert.assertTrue(user.isEnabled());

	Assert.assertTrue(user.hasPermission("api"));
	Assert.assertTrue(user.hasPermission("downloads"));
	Assert.assertArrayEquals(new String[] { "his-central" }, user.getAllowedViews());

	// the largest tier
	Assert.assertEquals(Integer.valueOf(2000), user.getMaxDownloadSizeMB());
	Assert.assertEquals(Integer.valueOf(200), user.getMaxDownloadPartSizeMB());

	Assert.assertEquals("user@example.org", user.getStringPropertyValue("email").get());
	Assert.assertEquals("user@example.org", user.getStringPropertyValue("username").get());
	Assert.assertEquals("Ada", user.getStringPropertyValue("firstName").get());
	Assert.assertEquals("Lovelace", user.getStringPropertyValue("lastName").get());
    }

    @Test
    public void roleAlwaysFromConfiguration() throws Exception {

	JWTClaimsSet claims = claims(List.of("api", "admin")).//
		claim("role", "admin").//
		claim("realm_access", Map.of("roles", List.of("admin"))).//
		build();

	GSUser user = resolver.resolve(token(claims));

	Assert.assertEquals("his_central", user.getRole());
	Assert.assertFalse(user.hasPermission("admin"));
    }

    @Test
    public void missingConfiguredRoleGivesAnonymous() throws Exception {

	setting.setUsersRole("");

	GSUser user = resolver.resolve(token(claims(List.of("api")).build()));

	Assert.assertEquals(BasicRole.ANONYMOUS.getRole(), user.getRole());
    }

    @Test
    public void permissionsAreCappedByConfiguration() throws Exception {

	setting.setAllowedPermissions("api");

	GSUser user = resolver.resolve(token(claims(List.of("api", "downloads", "superpower")).build()));

	Assert.assertEquals("api", user.getStringPropertyValue("permissions").get());
	Assert.assertTrue(user.hasPermission("api"));
	Assert.assertFalse(user.hasPermission("downloads"));
	Assert.assertFalse(user.hasPermission("superpower"));
    }

    @Test
    public void noRoles() throws Exception {

	GSUser user = resolver.resolve(token(claims(List.of()).build()));

	Assert.assertFalse(user.hasPermission("api"));
	Assert.assertTrue(user.getStringPropertyValue("permissions").isEmpty());

	// no view roles: all views
	Assert.assertEquals(0, user.getAllowedViews().length);

	// no tier: default limits
	Assert.assertNull(user.getMaxDownloadSizeMB());
	Assert.assertNull(user.getMaxDownloadPartSizeMB());
    }

    @Test
    public void rolesOfOtherClientsAreIgnored() throws Exception {

	JWTClaimsSet claims = claims(List.of()).//
		claim("resource_access", Map.of("account", Map.of("roles", List.of("api", "downloads", "view:secret")))).//
		build();

	GSUser user = resolver.resolve(token(claims));

	Assert.assertFalse(user.hasPermission("api"));
	Assert.assertEquals(0, user.getAllowedViews().length);
    }

    @Test
    public void unknownTierIsIgnored() throws Exception {

	GSUser user = resolver.resolve(token(claims(List.of("download-tier:huge")).build()));

	Assert.assertNull(user.getMaxDownloadSizeMB());

	user = resolver.resolve(token(claims(List.of("download-tier:huge", "download-tier:standard")).build()));

	Assert.assertEquals(Integer.valueOf(100), user.getMaxDownloadSizeMB());
	Assert.assertEquals(Integer.valueOf(20), user.getMaxDownloadPartSizeMB());
    }

    @Test
    public void unverifiedEmailIsNotUsed() throws Exception {

	GSUser user = resolver.resolve(token(claims(List.of("api")).claim("email_verified", false).build()));

	Assert.assertTrue(user.getStringPropertyValue("email").isEmpty());

	user = resolver.resolve(token(claims(List.of("api")).claim("email_verified", null).build()));

	Assert.assertTrue(user.getStringPropertyValue("email").isEmpty());
    }

    @Test
    public void multipleAudiences() throws Exception {

	GSUser user = resolver.resolve(token(claims(List.of("api")).audience(List.of("account", "dab")).build()));

	Assert.assertTrue(user.hasPermission("api"));
    }

    //
    // invalid tokens
    //

    @Test(expected = InvalidBearerTokenException.class)
    public void expiredToken() throws Exception {

	resolver.resolve(token(claims(List.of("api")).//
		issueTime(new Date(System.currentTimeMillis() - 3_600_000)).//
		expirationTime(new Date(System.currentTimeMillis() - 600_000)).//
		build()));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void wrongAudience() throws Exception {

	resolver.resolve(token(claims(List.of("api")).audience("account").build()));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void missingAudience() throws Exception {

	resolver.resolve(token(claims(List.of("api")).audience((String) null).build()));
    }

    @Test
    public void wrongIssuer() throws Exception {

	String token = token(claims(List.of("api")).issuer("https://evil.example.org/realms/users").build());

	Assert.assertFalse(resolver.isIssuedByRealm(token));

	try {
	    resolver.resolve(token);
	    Assert.fail("Exception expected");
	} catch (InvalidBearerTokenException e) {
	    // expected
	}
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void wrongSignature() throws Exception {

	RSAKey other = new RSAKeyGenerator(2048).keyID("k1").generate();

	resolver.resolve(token(claims(List.of("api")).build(), other));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void unknownKey() throws Exception {

	RSAKey other = new RSAKeyGenerator(2048).keyID("k2").generate();

	resolver.resolve(token(claims(List.of("api")).build(), other));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void unsignedToken() throws Exception {

	String token = new com.nimbusds.jwt.PlainJWT(claims(List.of("api")).build()).serialize();

	Assert.assertTrue(resolver.isIssuedByRealm(token));

	resolver.resolve(token);
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void idTokenIsRejected() throws Exception {

	resolver.resolve(token(claims(List.of("api")).claim("typ", "ID").build()));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void tamperedToken() throws Exception {

	String token = token(claims(List.of("api")).build());

	String[] parts = token.split("\\.");

	String forgedPayload = com.nimbusds.jose.util.Base64URL
		.encode(claims(List.of("api", "downloads", "download-tier:large")).build().toString()).toString();

	resolver.resolve(parts[0] + "." + forgedPayload + "." + parts[2]);
    }

    @Test
    public void notAJwt() {

	Assert.assertFalse(resolver.isIssuedByRealm("abc"));
	Assert.assertFalse(resolver.isIssuedByRealm("his_central-568a4888-d6bd-4be3-b7a6-d9887997bb0f"));
    }

    //
    // Authorization header
    //

    @Test
    public void readBearerToken() {

	Assert.assertEquals("abc", KeycloakBearerTokenResolver.readBearerToken(request("Bearer abc", null, null)).get());
	Assert.assertEquals("abc", KeycloakBearerTokenResolver.readBearerToken(request("bearer  abc ", null, null)).get());

	Assert.assertTrue(KeycloakBearerTokenResolver.readBearerToken(request(null, null, null)).isEmpty());
	Assert.assertTrue(KeycloakBearerTokenResolver.readBearerToken(request("Bearer ", null, null)).isEmpty());
	Assert.assertTrue(KeycloakBearerTokenResolver.readBearerToken(request("Basic dXNlcjpwd2Q=", null, null)).isEmpty());
    }

    //
    // UserFinder
    //

    @Test
    public void userFinderWithoutBearerToken() throws Exception {

	HttpServletRequest request = request(null, "/gs-service/services/essi/token/abc/view/his-central/om-api/features", null);

	Assert.assertTrue(UserFinder.findBearerUser(request, Optional.of(resolver)).isEmpty());
    }

    @Test
    public void userFinderWithoutSecondaryUsers() throws Exception {

	HttpServletRequest request = request("Bearer " + token(claims(List.of("api")).build()), null, null);

	Assert.assertTrue(UserFinder.findBearerUser(request, Optional.empty()).isEmpty());
    }

    @Test
    public void userFinderIgnoresOtherIssuers() throws Exception {

	HttpServletRequest request = request("Bearer " + token(claims(List.of("api")).issuer("https://other.example.org").build()), null,
		null);

	Assert.assertTrue(UserFinder.findBearerUser(request, Optional.of(resolver)).isEmpty());

	request = request("Bearer opaque-token", null, null);

	Assert.assertTrue(UserFinder.findBearerUser(request, Optional.of(resolver)).isEmpty());
    }

    @Test
    public void userFinderWithValidBearerToken() throws Exception {

	// the user token in the path is ignored
	HttpServletRequest request = request(//
		"Bearer " + token(claims(List.of("api")).build()), //
		"/gs-service/services/essi/token/his_central-568a4888/view/his-central/om-api/features", //
		null);

	GSUser user = UserFinder.findBearerUser(request, Optional.of(resolver)).get();

	Assert.assertEquals("user-1", user.getIdentifier());
	Assert.assertEquals("his_central", user.getRole());
	Assert.assertTrue(user.hasPermission("api"));
    }

    @Test(expected = InvalidBearerTokenException.class)
    public void userFinderWithExpiredBearerToken() throws Exception {

	HttpServletRequest request = request("Bearer " + token(claims(List.of("api")).//
		issueTime(new Date(System.currentTimeMillis() - 3_600_000)).//
		expirationTime(new Date(System.currentTimeMillis() - 600_000)).//
		build()), null, null);

	UserFinder.findBearerUser(request, Optional.of(resolver));
    }

    //
    // helpers
    //

    private static JWTClaimsSet.Builder claims(List<String> dabRoles) {

	long now = System.currentTimeMillis();

	return new JWTClaimsSet.Builder().//
		issuer(ISSUER).//
		subject("user-1").//
		audience("dab").//
		issueTime(new Date(now)).//
		expirationTime(new Date(now + 300_000)).//
		claim("typ", "Bearer").//
		claim("azp", "his-central-portal").//
		claim("email", "user@example.org").//
		claim("email_verified", true).//
		claim("preferred_username", "user@example.org").//
		claim("given_name", "Ada").//
		claim("family_name", "Lovelace").//
		claim("resource_access", Map.of("dab", Map.of("roles", dabRoles)));
    }

    private String token(JWTClaimsSet claims) throws Exception {

	return token(claims, key);
    }

    private static String token(JWTClaimsSet claims, RSAKey signingKey) throws Exception {

	SignedJWT jwt = new SignedJWT(//
		new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).type(JOSEObjectType.JWT).build(), //
		claims);

	jwt.sign(new RSASSASigner(signingKey));

	return jwt.serialize();
    }

    private static HttpServletRequest request(String authorization, String uri, String query) {

	HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

	Mockito.when(request.getHeader("Authorization")).thenReturn(authorization);
	Mockito.when(request.getRequestURI()).thenReturn(uri);
	Mockito.when(request.getQueryString()).thenReturn(query);

	return request;
    }
}
