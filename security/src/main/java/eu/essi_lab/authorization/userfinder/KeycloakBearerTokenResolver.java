/**
 *
 */
package eu.essi_lab.authorization.userfinder;

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

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.cfga.gs.setting.database.SecondaryUsersDatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SecondaryUsersDatabaseSetting.DownloadTier;
import eu.essi_lab.model.auth.GSUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Builds the {@link GSUser} of a request authenticated with a Keycloak access token (<code>Authorization: Bearer &lt;token&gt;</code>),
 * according to the {@link SecondaryUsersDatabaseSetting}.<br>
 * <br>
 * The token is verified locally with the realm public keys (fetched once and cached): signature, issuer, expiration and audience (the
 * configured client). No credentials are needed and the realm users are never read.<br>
 * <br>
 * The user is built from the token claims:
 * <ul>
 * <li>identifier: the <code>sub</code> claim (Keycloak user id)</li>
 * <li>role: always the configured one ({@link BasicRole#ANONYMOUS} if not configured)</li>
 * <li><code>permissions</code>: the configured client roles which are also allowed by the setting</li>
 * <li><code>allowedViews</code>: the client roles <code>view:&lt;view id&gt;</code> (none means all views)</li>
 * <li><code>maxDownloadSizeMB</code>, <code>maxDownloadPartSizeMB</code>: the largest configured tier among the client roles
 * <code>download-tier:&lt;tier name&gt;</code> (none means the default limits)</li>
 * <li><code>email</code>: only if verified; <code>username</code>, <code>firstName</code>, <code>lastName</code></li>
 * </ul>
 */
public class KeycloakBearerTokenResolver {

    public static final String AUTH_PROVIDER = "keycloak";
    public static final String VIEW_ROLE_PREFIX = "view:";
    public static final String DOWNLOAD_TIER_ROLE_PREFIX = "download-tier:";

    private static final String BEARER_PREFIX = "bearer ";

    /**
     * One cached key source per realm, shared by all the requests
     */
    private static final Map<String, JWKSource<SecurityContext>> KEY_SOURCES = new ConcurrentHashMap<>();

    private final SecondaryUsersDatabaseSetting setting;
    private final DefaultJWTProcessor<SecurityContext> processor;

    /**
     * Uses the realm public keys
     *
     * @param setting
     */
    public KeycloakBearerTokenResolver(SecondaryUsersDatabaseSetting setting) {

	this(setting, KEY_SOURCES.computeIfAbsent(setting.getJwksUrl(), KeycloakBearerTokenResolver::createRemoteKeySource));
    }

    /**
     * @param setting
     * @param keySource
     */
    public KeycloakBearerTokenResolver(SecondaryUsersDatabaseSetting setting, JWKSource<SecurityContext> keySource) {

	this.setting = setting;

	this.processor = new DefaultJWTProcessor<>();

	processor.setJWSKeySelector(new JWSVerificationKeySelector<>(//
		Set.of(JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512, //
			JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512, //
			JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512),
		keySource));

	JWTClaimsSet exactMatch = new JWTClaimsSet.Builder().issuer(setting.getIssuer()).build();

	processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(//
		setting.getClientId().orElse(SecondaryUsersDatabaseSetting.DEFAULT_CLIENT_ID), //
		exactMatch, //
		Set.of("sub", "exp", "iat")));
    }

    /**
     * @param request
     * @return the token of the <code>Authorization: Bearer &lt;token&gt;</code> header, if present
     */
    public static Optional<String> readBearerToken(HttpServletRequest request) {

	String header = request.getHeader("Authorization");

	if (header == null || header.length() <= BEARER_PREFIX.length()
		|| !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {

	    return Optional.empty();
	}

	String token = header.substring(BEARER_PREFIX.length()).trim();

	return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    /**
     * Reads the issuer <b>without</b> verifying the token, to decide whether this resolver is in charge of it. Tokens of other issuers (or
     * which are not JWTs) are not handled by this resolver and must be ignored.
     *
     * @param token
     * @return
     */
    public boolean isIssuedByRealm(String token) {

	try {

	    return setting.getIssuer().equals(JWTParser.parse(token).getJWTClaimsSet().getIssuer());

	} catch (ParseException e) {

	    return false;
	}
    }

    /**
     * @param token
     * @return
     * @throws InvalidBearerTokenException if the token is not valid
     */
    public GSUser resolve(String token) throws InvalidBearerTokenException {

	JWTClaimsSet claims;

	try {

	    claims = processor.process(token, null);

	} catch (ParseException | BadJOSEException | JOSEException e) {

	    throw new InvalidBearerTokenException(e.getMessage(), e);
	}

	Object typ = claims.getClaim("typ");

	if (typ != null && !"Bearer".equals(typ)) {

	    throw new InvalidBearerTokenException("Not an access token: " + typ);
	}

	return toUser(claims);
    }

    /**
     * @param claims
     * @return
     */
    GSUser toUser(JWTClaimsSet claims) {

	GSUser user = new GSUser();

	user.setIdentifier(claims.getSubject());
	user.setEnabled(true);
	user.setAuthProvider(AUTH_PROVIDER);
	user.setRole(setting.getUsersRole().orElse(BasicRole.ANONYMOUS.getRole()));

	List<String> clientRoles = readClientRoles(claims, setting.getClientId().orElse(SecondaryUsersDatabaseSetting.DEFAULT_CLIENT_ID));

	//
	// permissions
	//
	List<String> allowed = setting.getAllowedPermissions();

	String permissions = String.join(",", clientRoles.stream().filter(allowed::contains).toList());

	if (!permissions.isEmpty()) {
	    user.setPropertyValue("permissions", permissions);
	}

	//
	// views (none means all views)
	//
	String views = String.join(",", clientRoles.stream().//
		filter(r -> r.startsWith(VIEW_ROLE_PREFIX)).//
		map(r -> r.substring(VIEW_ROLE_PREFIX.length()).trim()).//
		filter(v -> !v.isEmpty()).//
		toList());

	if (!views.isEmpty()) {
	    user.setPropertyValue("allowedViews", views);
	}

	//
	// download limits: the largest configured tier
	//
	Map<String, DownloadTier> tiers = setting.getDownloadTiers();

	clientRoles.stream().//
		filter(r -> r.startsWith(DOWNLOAD_TIER_ROLE_PREFIX)).//
		map(r -> tiers.get(r.substring(DOWNLOAD_TIER_ROLE_PREFIX.length()).trim())).//
		filter(Objects::nonNull).//
		max(Comparator.comparingInt(DownloadTier::maxDownloadSizeMB)).//
		ifPresent(tier -> {

		    user.setPropertyValue("maxDownloadSizeMB", String.valueOf(tier.maxDownloadSizeMB()));

		    if (tier.maxDownloadPartSizeMB() != null) {
			user.setPropertyValue("maxDownloadPartSizeMB", String.valueOf(tier.maxDownloadPartSizeMB()));
		    }
		});

	//
	// profile
	//
	if (Boolean.TRUE.equals(readBoolean(claims, "email_verified"))) {
	    readString(claims, "email").ifPresent(v -> user.setPropertyValue("email", v));
	}

	readString(claims, "preferred_username").ifPresent(v -> user.setPropertyValue("username", v));
	readString(claims, "given_name").ifPresent(v -> user.setPropertyValue("firstName", v));
	readString(claims, "family_name").ifPresent(v -> user.setPropertyValue("lastName", v));

	return user;
    }

    /**
     * @param claims
     * @param clientId
     * @return the roles in <code>resource_access.&lt;clientId&gt;.roles</code>
     */
    static List<String> readClientRoles(JWTClaimsSet claims, String clientId) {

	try {

	    Map<String, Object> resourceAccess = claims.getJSONObjectClaim("resource_access");

	    if (resourceAccess == null || !(resourceAccess.get(clientId) instanceof Map<?, ?> client)) {
		return Collections.emptyList();
	    }

	    if (!(client.get("roles") instanceof Collection<?> roles)) {
		return Collections.emptyList();
	    }

	    List<String> out = new ArrayList<>();
	    roles.forEach(r -> {
		if (r instanceof String s) {
		    out.add(s);
		}
	    });

	    return out;

	} catch (ParseException e) {

	    return Collections.emptyList();
	}
    }

    private static Optional<String> readString(JWTClaimsSet claims, String name) {

	try {
	    return Optional.ofNullable(claims.getStringClaim(name)).map(String::trim).filter(s -> !s.isEmpty());
	} catch (ParseException e) {
	    return Optional.empty();
	}
    }

    private static Boolean readBoolean(JWTClaimsSet claims, String name) {

	try {
	    return claims.getBooleanClaim(name);
	} catch (ParseException e) {
	    return null;
	}
    }

    /**
     * @param jwksUrl
     * @return
     */
    private static JWKSource<SecurityContext> createRemoteKeySource(String jwksUrl) {

	try {

	    return JWKSourceBuilder.<SecurityContext> create(URI.create(jwksUrl).toURL()).//
		    retrying(true).//
		    build();

	} catch (MalformedURLException | IllegalArgumentException e) {

	    throw new IllegalStateException("Invalid Keycloak public keys URL: " + jwksUrl, e);
	}
    }

    /**
     * @param user
     * @return true if the user was built from a bearer token by this resolver (it can not be found again from the request URL)
     */
    public static boolean isBearerTokenUser(GSUser user) {

	return user != null && AUTH_PROVIDER.equals(user.getAuthProvider());
    }

    /**
     * @param user
     * @return a short description for logs
     */
    static String describe(GSUser user) {

	return user.getIdentifier() + " " + user.getProperties().stream().map(p -> p.getName() + "=" + p.getValue()).toList();
    }
}
