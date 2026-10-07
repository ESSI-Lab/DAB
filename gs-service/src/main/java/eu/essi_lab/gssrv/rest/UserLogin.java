package eu.essi_lab.gssrv.rest;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;

/**
 * Checks the credentials (e-mail and API key) of a {@link LoginRequest} against the DAB users
 *
 * @author boldrini
 */
public class UserLogin {

    /**
     * @param request
     * @return a successful response if the credentials match a DAB user; {@link LoginResponse#isAdmin()} tells whether the
     *         user is an administrator
     */
    @SuppressWarnings("rawtypes")
    public static LoginResponse login(LoginRequest request) {

	if (request == null || request.getEmail() == null || request.getApiKey() == null) {

	    return new LoginResponse(false, "Missing credentials", null, null, null, null);
	}


	try {

	    UserFinder uf = UserFinder.newInstance();
	    List<GSUser> users = uf.getUsers(false);

	    for (GSUser user : users) {

		String firstName = null;
		String email = null;
		String lastName = null;

		List<GSProperty> properties = user.getProperties();

		for (GSProperty<?> prop : properties) {
		    if (prop.getName().equals("firstName")) {
			firstName = prop.getValue().toString();
		    }
		    if (prop.getName().equals("lastName")) {
			firstName = prop.getValue().toString();
		    }
		    if (prop.getName().equals("email")) {
			email = prop.getValue().toString();
		    }
		}

		if (request.getApiKey().equals(user.getUri()) && request.getEmail().equals(email)) {

		    LoginResponse response = new LoginResponse(//
			    true, //
			    "Login successful", //
			    user.getStringPropertyValue("firstName").get(), //
			    user.getStringPropertyValue("lastName").get(), //
			    request.getEmail(), //
			    request.getApiKey());

		    Optional<String> perm = user.getStringPropertyValue("permissions");
		    if (perm.isPresent()) {
		    response.setPermissions(perm.get());
		    }
		    
		    response.setUser(user);

		    List<String> adminUsers = ConfigurationWrapper.getAdminUsers();

		    if (adminUsers != null) {
			for (String adminUser : adminUsers) {
			    if (user.getUri().equals(adminUser) || request.getEmail().equals(adminUser)) {
				response.setAdmin(true);
			    }
			}
		    }

		    return response;
		}
	    }
	    LoginResponse response = new LoginResponse(false, "Invalid credentials", null, null, null, null);
	    return response;

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(UserLogin.class).error(ex.getMessage(), ex);
	    LoginResponse resp = new LoginResponse(false, "Server error: " + ex.getMessage(), null, null, null, null);
	    return resp;
	}
    }
}
