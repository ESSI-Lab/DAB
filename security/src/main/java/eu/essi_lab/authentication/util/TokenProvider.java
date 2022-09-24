package eu.essi_lab.authentication.util;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Build a JWT token by an existing {@link Token} object or by adding claims and
 * encoding them.<br>
 * Beware. Right now token is encoded using hard-coded configuration. This is not
 * good and will be only temporary. We must change this as soon as possible.
 * Parameters like:<br>
 * - {@link TokenProvider#hMACSecretPassphrase},<br>
 * - {@link TokenProvider#EXPIRATION_TIME} <br>
 * must be turned into configurable parameters.
 * 
 * @author pezzati
 */
public class TokenProvider {

    /**
     * 
     */
    public static final String USER_COOKIE_NAME = "gput";
    /**
     * 
     */
    public static final String INVALID_JWT_TOKEN = "INVALID_JWT_TOKEN";

    /**
     * 
     */
    public static long EXPIRATION_TIME = 1800000;
    private byte[] hMACSecretPassphrase;
    private Builder jwtBuilder = null;
    private Algorithm hmac256;
    private Verification verifier;
    private ObjectMapper mapper;

    public TokenProvider() {
	hMACSecretPassphrase = Base64.getEncoder().encode("hMACSecretPassphrase".getBytes(StandardCharsets.UTF_8));
	hmac256 = Algorithm.HMAC256(hMACSecretPassphrase);
	jwtBuilder = JWT.create();
	verifier = JWT.require(hmac256);
	mapper = new ObjectMapper();
    }

    /**
     * @param claimName
     * @param claimValue
     * @return
     */
    public TokenProvider addClaim(String claimName, String... claimValue) {

	jwtBuilder.withArrayClaim(claimName, claimValue);

	return this;
    }

    /**
     * @param claimName
     * @param claimValue
     * @return
     */
    public TokenProvider addClaim(String claimName, String claimValue) {

	jwtBuilder.withClaim(claimName, claimValue);

	return this;
    }

    /**
     * @return
     */
    public String getToken() {

	return jwtBuilder.sign(hmac256);
    }

    /**
     * TokenProvider is able to parse {@link Token} instances in claim sets.
     * When parsing, TokenProvider takes {@link Token#getEmail()} and
     * {@link Token#getServiceProvider()} values and add them to jwt token's
     * claim set.
     * 
     * @param token the {@link Token} to be parsed. It can't be null.
     * @return the corresponding jwt token.
     */
    public String getToken(Token token) {

	jwtBuilder.withClaim("email", token.getEmail());
	jwtBuilder.withClaim("authentication-provider", token.getServiceProvider());

	return jwtBuilder.sign(hmac256);
    }

    /**
     * @param value
     * @return
     */
    public boolean isValid(String value) {

	try {
	    verifier.build().verify(value);

	} catch (JWTVerificationException jve) {
	    GSLoggerFactory.getLogger(getClass()).error(jve.getMessage());
	    return false;
	}

	return true;
    }

    /**
     * @param request
     * @param email
     * @return
     */
    public Optional<String> findOAuth2Attribute(HttpServletRequest request, boolean email) {

	Cookie[] cookies = request.getCookies();

	if (cookies != null) {

	    for (Cookie cookie : cookies) {

		String name = cookie.getName();

		if (name.equals(USER_COOKIE_NAME)) {

		    JsonNode token = null;
		    try {
			token = decode(cookie.getValue());
		    } catch (GSException e) {
			GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
			return Optional.empty();
		    }

		    if (email) {
			return Optional.of(token.get("email").asText());
		    } else {
			return Optional.of(token.get("authentication-provider").asText());
		    }
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * Verifies the presence and validity in the cookies of the "gput" cookie which provides the user email
     * 
     * @param cookies
     * @return
     */
    public boolean isAuthenticatedUser(Cookie[] cookies) {

	for (Cookie cookie : cookies) {

	    String name = cookie.getName();

	    if (USER_COOKIE_NAME.equals(name)) {

		boolean valid = isValid(cookie.getValue());

		if (valid) {

		    GSLoggerFactory.getLogger(getClass()).debug("User authenticated");

		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("User not authenticated");
		}

		return valid;
	    }
	}

	return false;
    }

    /**
     * @param value
     * @return
     * @throws GSException
     */
    public JsonNode decode(String value) throws GSException {
	try {

	    DecodedJWT jwttoken = verifier.build().verify(value);
	    String payload = new String(Base64.getDecoder().decode(jwttoken.getPayload()));
	    return mapper.readValue(payload, JsonNode.class);

	} catch (JWTVerificationException jve) {

	    GSLoggerFactory.getLogger(getClass()).error(String.format("Error while decoding %s jwt token", value), jve);

	    throw GSException.createException(//
		    getClass(), //
		    jve.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_JWT_TOKEN, //
		    jve);

	} catch (IOException ioe) {

	    GSLoggerFactory.getLogger(getClass()).error(String.format("Error while decoding %s jwt token", value), ioe);

	    throw GSException.createException(//
		    getClass(), //
		    ioe.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_JWT_TOKEN, //
		    ioe);
	}
    }
}
