/**
 *
 */
package eu.essi_lab.authorization.userfinder;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.*;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.UsersManager;
import eu.essi_lab.lib.net.keycloak.KeycloakUsersClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class KeycloakUsersManager implements UsersManager {

    /**
     *
     */
    public static final String KEYCLOAK_TYPE = "Keycloak";

    private KeycloakUsersClient manager;

    /**
     *
     */
    public KeycloakUsersManager() {

    }

    @Override
    public boolean supports(StorageInfo info) {

	return info.getType().orElse("").equals(KEYCLOAK_TYPE);
    }

    @Override
    public void initialize(StorageInfo info) throws GSException {

	String uri = info.getUri();
	String user = info.getUser();
	String password = info.getPassword();
	String name = info.getName();

	manager = new KeycloakUsersClient();
	manager.setServiceUrl(uri);
	manager.setAdminPassword(password);
	manager.setAdminUser(user);
	manager.setUsersRealm(name);

    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	try {
	    return manager.list(getAccessToken()).stream().map(KeycloakUserMapper::toGSUser).collect(Collectors.toList());

	} catch (IOException | InterruptedException | GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "KeycloakUsersManagerGetUsersError", e);
	}
    }

    @Override
    public void store(GSUser user) throws GSException {

	try {
	    manager.deleteByUserName(getAccessToken(), user.getIdentifier());

	} catch (NoSuchElementException e) {
	    // first time user is stored, no problem
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "KeycloakUsersManagerRemoveBeforeStoreUserError", e);
	}

	try {

	    boolean created = manager.create(getAccessToken(), KeycloakUserMapper.toKeycloakUser(user));

	    if (!created) {

		throw GSException.createException(//
			getClass(), //
			"Unable to store user", //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			"KeycloakUsersManagerStoreUserError");

	    }

	} catch (IOException | InterruptedException | GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "KeycloakUsersManagerStoreUserError", e);
	}
    }

    @Override
    public void removeUser(String userName) throws GSException {

	try {

	    boolean deleted = manager.deleteByUserName(getAccessToken(), userName);

	    if (!deleted) {

		throw GSException.createException(//
			getClass(), //
			"Unable to delete user", //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			"KeycloakUsersManagerStoreUserError");

	    }

	} catch (IOException | InterruptedException | GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "KeycloakUsersManagerRemoveUserError", e);
	}
    }

    /**
     * @return
     * @throws GSException
     */
    private String getAccessToken() throws GSException {

	try {
	    return manager.getAccessToken();

	} catch (IOException | InterruptedException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "KeycloakUsersManagerGetAccessTokenError", e);
	}
    }

}
