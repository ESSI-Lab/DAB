package eu.essi_lab.authorization.userfinder;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import org.geotools.data.view.DefaultView;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.authentication.util.TokenProvider;
import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserBaseClient;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Finds the user who originates the supplied <code>request</code>
 * 
 * @author Fabrizio
 */
public abstract class UserFinder {

    /**
     * 1 day
     */
    private static final long UPDATE_PERIOD = 1000 * 60 * 60 * 24;
    private static final UsersCacheUpdater UPDATER_TASK = new UsersCacheUpdater();

    static {

	Timer timer = new Timer();
	timer.scheduleAtFixedRate(UPDATER_TASK, 0, UPDATE_PERIOD);
    }

    /**
     * Caches
     */
    private static List<GSUser> users = new ArrayList<GSUser>();
    private static ExpiringCache<View> views = new ExpiringCache<>();
    static {
	views.setDuration(UPDATE_PERIOD);
    }

    /**
     * @author Fabrizio
     */
    private static class UsersCacheUpdater extends TimerTask {

	private UserFinder finder;

	/**
	 * @param authorizer
	 */
	public void setUserFinder(UserFinder finder) {

	    this.finder = finder;
	}

	@Override
	public void run() {

	    synchronized (this) {

		if (Objects.isNull(finder)) {
		    return;
		}

		try {

		    GSLoggerFactory.getLogger(getClass()).debug("Updating users cache STARTED");

		    finder.updateUsersCache();

		    GSLoggerFactory.getLogger(getClass()).debug("Updating users cache ENDED");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error occurred while updating users cache");

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	};
    }

    private UserBaseClient client;
    private DatabaseReader reader;
    protected GSConfiguration configuration;
    protected TokenProvider tokenProvider;

    public UserFinder() {

	tokenProvider = new TokenProvider();
    }

    /**
     * Finds the user who originates the supplied <code>request</code>.<br>
     * <br>
     * For the authentication mechanism which allows
     * the admin user to initialize and manage the configuration, the returned user must have the registered
     * administration identifier (e.g.: the email by which the user is registered with Google OAuth 2.0). In this case
     * the role can be omitted since the authentication is based on the matching between the user identifier and the
     * identifier written in the configuration.<br>
     * <br>
     * For discovery and access requests, the returned user must have at least
     * the {@link GSUser#getRole()} since this attribute is required to execute the authorization mechanism.
     * <br>
     * If no user can be identified, the user with the {@link BasicRole#ANONYMOUS} role must be returned
     * 
     * @param request
     * @return
     */
    public GSUser findUser(HttpServletRequest request) throws Exception {

	UPDATER_TASK.setUserFinder(this);

	List<String> identifiers = findIdentifiers(request);

	GSUser user = findUser(identifiers);

	return user;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    protected abstract List<String> findIdentifiers(HttpServletRequest request) throws Exception;

    /**
     * @param identifiers
     * @return
     * @throws Exception
     */
    protected abstract GSUser findUser(List<String> identifiers) throws Exception;

    public abstract void enableUser(String identifier) throws Exception;

    public void addUser(GSUser user) throws Exception {
	client.store(user);
    }

    /**
     * @throws Exception
     */
    protected void updateUsersCache() throws Exception {

	if (Objects.nonNull(client)) {

	    users = client.getUsers();

	}
    }

    public List<GSUser> getUsers() throws Exception {
	return getUsers(true);
    }
    
    /**
     * @return
     * @throws Exception
     */
    public List<GSUser> getUsers(boolean useCache) throws Exception {

	synchronized (UPDATER_TASK) {

	    if (users.isEmpty() && Objects.nonNull(client) || !useCache) {

		updateUsersCache();

	    } else {

		GSLoggerFactory.getLogger(getClass()).debug("Reading users from cache");
	    }

	    return users;
	}
    }

    /**
     * @param identifier
     * @return
     * @throws GSException
     */
    protected Optional<View> getView(String identifier) {

	if (identifier == null) {
	    return Optional.empty();
	}
	Optional<View> view = Optional.ofNullable(views.get(identifier));
	if (!view.isPresent()) {

	    try {
		view = getDatabaseReader().getView(identifier);

		if (view.isPresent()) {

		    views.put(identifier, view.get());
		}
	    } catch (GSException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return view;
    }

    /**
     * @return
     */
    public TokenProvider getTokenProvider() {

	return tokenProvider;
    }

    /**
     * @param tokenProvider
     */
    public void setTokenProvider(TokenProvider tokenProvider) {

	this.tokenProvider = tokenProvider;
    }

    /**
     * @return
     */
    public GSConfiguration getConfiguration() {

	return configuration;
    }

    /**
     * @param configuration
     */
    public void setConfiguration(GSConfiguration configuration) {

	this.configuration = configuration;
    }

    /**
     * @param client
     */
    public void setClient(UserBaseClient client) {

	this.client = client;
    }

    /**
     * @return
     */
    public UserBaseClient getClient() {

	return client;
    }

    /**
     * @param reader
     */
    public void setDatabaseReader(DatabaseReader reader) {

	this.reader = reader;
    }

    /**
     * @return
     */
    public DatabaseReader getDatabaseReader() {

	return reader;
    }

}
