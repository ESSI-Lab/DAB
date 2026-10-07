package eu.essi_lab.gssrv.conf;

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

import java.util.Optional;

import eu.essi_lab.authentication.token.TokenProvider;
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JVMOption;
import eu.essi_lab.model.auth.GSUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authorization of the DAB administrators, shared by the configurator and the administration services (e.g. the
 * harvesting status). The administrator is the user authenticated with the OAuth 2.0 provider configured in the
 * {@link eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting} (the authentication is stored in the
 * {@link TokenProvider#USER_COOKIE_NAME} cookie), whose identifier is the configured administrator identifier, or
 * belongs to the {@link GSUser#ESSI_LAB_DOMAIN}
 *
 * @author boldrini
 */
public class AdminAuthorization {

    /**
     * @author boldrini
     */
    public enum Result {

	/**
	 * The user is an administrator
	 */
	AUTHORIZED,

	/**
	 * The user is not authenticated with the OAuth 2.0 provider
	 */
	NOT_AUTHENTICATED,

	/**
	 * The user is authenticated, but is not an administrator
	 */
	NOT_ADMIN,

	/**
	 * The user cannot be identified because of an error
	 */
	ERROR
    }

    /**
     * @param request
     * @return
     */
    public static Result check(HttpServletRequest request) {

	if (JVMOption.isEnabled(JVMOption.SKIP_CONFIG_AUTHORIZATION)) {

	    return Result.AUTHORIZED;
	}

	GSUser user;

	try {
	    user = UserFinder.findCurrentUser(request);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(AdminAuthorization.class).error(e);

	    return Result.ERROR;
	}

	String userId = user.getIdentifier();

	//
	// this is required to allow all ESSI-Lab users with no registered OAuth 2.0 Client IDs
	//
	if (userId.contains(GSUser.ESSI_LAB_DOMAIN)) {

	    return Result.AUTHORIZED;
	}

	Optional<String> adminId = ConfigurationWrapper.readAdminIdentifier();

	if (adminId.isPresent() && adminId.get().equals(userId)) {

	    return Result.AUTHORIZED;
	}

	return findEmail(request).isPresent() ? Result.NOT_ADMIN : Result.NOT_AUTHENTICATED;
    }

    /**
     * @param request
     * @return the e-mail of the user authenticated with the OAuth 2.0 provider, if any
     */
    public static Optional<String> findEmail(HttpServletRequest request) {

	try {

	    return new TokenProvider().findOAuth2Attribute(request, true);

	} catch (Exception e) {

	    return Optional.empty();
	}
    }
}
