/**
 *
 */
package eu.essi_lab.authorization.userfinder;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.authentication.token.*;
import eu.essi_lab.authorization.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.auth.*;
import eu.essi_lab.model.exceptions.*;
import jakarta.servlet.http.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class UserFinder {

    private static final String USER_FINDING_ERROR = "USER_FINDING_ERROR";

    private static final long USERS_UPDATE_PERIOD = TimeUnit.MINUTES.toMillis(30);
    private static final UsersCacheUpdater USERS_UPDATER_TASK = new UsersCacheUpdater();

    private static final long VIEWS_CACHE_DURATION = TimeUnit.MINUTES.toMillis(30);

    static {

	Timer timer = new Timer();
	timer.scheduleAtFixedRate(USERS_UPDATER_TASK, 0, USERS_UPDATE_PERIOD);
    }

    private static List<GSUser> users;

    private final ViewManager viewManager;

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

		    finder.updateUsersCache();

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error occurred while updating users cache");

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}
    }

    private Optional<String> email;
    private Optional<String> authProvider;
    private StringBuilder logBuilder;

    private UsersWriter usersWriter;
    private UsersReader usersReader;

    protected TokenProvider tokenProvider;

    /**
     *
     */
    private UserFinder() {

	tokenProvider = new TokenProvider();

	viewManager = new ViewManager();
    }

    /**
     * @return
     * @throws GSException
     */
    public static UserFinder create() throws GSException {

	UserFinder finder = new UserFinder();

	StorageInfo storageInfo = ConfigurationWrapper.getStorageInfo();
	DatabaseReader reader = DatabaseProviderFactory.getReader(storageInfo);

	finder.viewManager.setDatabaseReader(reader);

	if (ConfigurationWrapper.getUsersStorageInfo().isEmpty()) {

	    DatabaseWriter writer = DatabaseProviderFactory.getWriter(storageInfo);

	    finder.setUsersReader(reader);
	    finder.setUsersWriter(writer);

	} else {

	    StorageInfo usersStorageInfo = ConfigurationWrapper.getUsersStorageInfo().get();

	    UsersManager usersManager = UsersManagerFactory.get(usersStorageInfo);

	    finder.setUsersReader(usersManager);
	    finder.setUsersWriter(usersManager);
	}

	return finder;
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    public static GSUser findCurrentUser(HttpServletRequest request) throws GSException {

	UserFinder finder = create();

	GSUser user = null;

	try {
	    user = finder.findUser(request);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(UserFinder.class).error(e.getMessage(), e);

	    throw GSException.createException(//
		    UserFinder.class, //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USER_FINDING_ERROR);
	}

	return user;
    }

    /**
     * Finds the user who originates the supplied <code>request</code>.<br>
     * <br>
     * For the authentication mechanism which allows the admin user to initialize and manage the configuration, the returned user must have
     * the registered administration identifier (e.g.: the email by which the user is registered with Google OAuth 2.0). In this case the
     * role can be omitted since the authentication is based on the matching between the user identifier and the identifier written in the
     * configuration.<br>
     * <br>
     * For discovery and access requests, the returned user must have at least the {@link GSUser#getRole()} since this attribute is required
     * to execute the authorization mechanism.
     * <br>
     * If no user can be identified, the user with the {@link BasicRole#ANONYMOUS} role must be returned
     *
     * @param request
     * @return
     */
    public GSUser findUser(HttpServletRequest request) throws Exception {

	USERS_UPDATER_TASK.setUserFinder(this);

	List<GSProperty<String>> identifiers = findIdentifiers(request);

	GSUser user = findUser(identifiers);

	return user;
    }

    /**
     * @param identifier
     * @throws Exception
     */
    public void enableUser(String identifier) throws Exception {

	List<GSUser> users = getUsers(false);

	Optional<GSUser> user = users.stream().filter(u -> //

		// user must be disabled
		!u.isEnabled() &&

			//
			// normal case, string equality comparison
			//
			identifier.equals(u.getIdentifier())).findFirst();

	if (user.isPresent()) {

	    user.get().setEnabled(true);

	    usersWriter.store(user.get());
	}
    }

    /**
     * @throws Exception
     */
    public void updateUsersCache() throws Exception {

	synchronized (USERS_UPDATER_TASK) {

	    if (usersReader != null) {

		users = usersReader.getUsers();
	    }
	}
    }

    /**
     * @return
     * @throws Exception
     */
    public List<GSUser> getUsers() throws Exception {

	return getUsers(true);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<GSUser> getUsers(boolean useCache) throws Exception {

	synchronized (USERS_UPDATER_TASK) {

	    if (users == null && usersReader != null || !useCache) {

		updateUsersCache();

	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Reading users from cache");
	    }

	    return users;
	}
    }

    /**
     * @param writer
     */
    public void setUsersWriter(UsersWriter writer) {

	this.usersWriter = writer;
    }

    /**
     * @param reader
     */
    public void setUsersReader(UsersReader reader) {

	this.usersReader = reader;
    }

    /**
     * @return the writer
     */
    public UsersWriter getUsersWriter() {

	return usersWriter;
    }

    protected List<GSProperty<String>> findIdentifiers(HttpServletRequest request) throws Exception {

	//
	// find all the identifiers in the request according
	// to different strategies
	//
	List<GSProperty<String>> identifiers = new ArrayList<>();

	logBuilder = new StringBuilder();

	//
	// 1 - OAuth 2.0 identifier
	//

	if (!discardEssiOauthAdminUser()) {

	    email = tokenProvider.findOAuth2Attribute(request, true);
	    authProvider = tokenProvider.findOAuth2Attribute(request, false);

	    if (email.isPresent()) {

		logBuilder.append("\n- OAuth 2.0 email: " + email.get());
		identifiers.add(new GSProperty<String>(UserIdentifierType.OAUTH_EMAIL.getType(), email.get()));
	    }

	    if (authProvider.isPresent()) {

		logBuilder.append("\n- OAuth 2.0 provider: " + authProvider.get());
	    }
	} else {

	    email = Optional.empty();
	    authProvider = Optional.empty();

	    logBuilder.append("\n- Discarding OAuth 2.0 admin user");
	}

	//
	// 2 - remote host
	//
	String remoteHost = request.getRemoteHost();
	logBuilder.append("\n- Remote host: " + remoteHost);

	identifiers.add(new GSProperty<String>(UserIdentifierType.HOST.getType(), remoteHost));

	//
	// 3 - 'x-forwarded-for' header
	//
	List<String> xForwardedForHeaders = WebRequest.readXForwardedForHeaders(request);

	xForwardedForHeaders.forEach(x -> identifiers.add(new GSProperty<String>(UserIdentifierType.X_FORWARDER_FOR.getType(), x)));

	xForwardedForHeaders.forEach(ip -> logBuilder.append("\n- Xforw: " + ip));

	//
	// 4 - 'Origin' header
	//
	Optional<String> originHeader = WebRequest.readOriginHeader(request);

	if (originHeader.isPresent()) {

	    identifiers.add(new GSProperty<>(UserIdentifierType.ORIGIN_HEADER.getType(), originHeader.get()));

	    logBuilder.append("\n- " + WebRequest.ORIGIN_HEADER + ": " + originHeader.get());
	}

	WebRequest webRequest = new WebRequest();
	webRequest.setServletRequest(request, false);

	//
	// 5 - user token
	//
	Optional<String> userTokenId = webRequest.extractTokenId();
	if (userTokenId.isPresent()) {

	    logBuilder.append("\n- User token: " + userTokenId.get());

	    identifiers.add(new GSProperty<>(UserIdentifierType.USER_TOKEN.getType(), userTokenId.get()));
	}

	//
	// 6,7 - view identifier and creator
	//
	if (!userTokenId.isPresent()) {

	    //
	    // see UserFinderProductionTokenCasesExternalTestIT comment
	    //

	    Optional<String> viewId = webRequest.extractViewId();

	    if (viewId.isPresent()) {

		logBuilder.append("\n- View id: " + viewId.get());
		identifiers.add(new GSProperty<>(UserIdentifierType.VIEW_IDENTIFIER.getType(), viewId.get()));

		Optional<String> creator = getViewCreator(viewId.get());

		if (creator.isPresent()) {

		    logBuilder.append("\n- View creator: " + creator);

		    identifiers.add(new GSProperty<>(UserIdentifierType.VIEW_CREATOR.getType(), creator.get()));
		}
	    }
	}

	//
	// 8 - client identifier
	//
	Optional<String> clientId = webRequest.readClientIdentifierHeader();

	clientId.ifPresent(id -> {

	    logBuilder.append("\n- Client id: " + id);
	    identifiers.add(new GSProperty<>(UserIdentifierType.CLIENT_IDENTIFIER.getType(), id));
	});

	// removes verbose log when the requests come from the Vaadin internal server
	if (request.getServletPath() != null && request.getServletPath().contains("configuration")) {

	    logBuilder = null;
	}

	return identifiers;
    }

    /**
     * @param identifiers
     * @return
     * @throws Exception
     */
    protected GSUser findUser(List<GSProperty<String>> identifiers) throws Exception {

	List<GSUser> users = getUsers();

	Optional<GSUser> user = users.//
		stream().//

		// user must be enabled
			filter(GSUser::isEnabled).//

		// the user properties contains one of the found properties identifiers
			filter(u -> u.getProperties().stream().anyMatch(identifiers::contains)).//

			findFirst();

	if (user.isEmpty()) {

	    user = Optional.of(BasicRole.createAnonymousUser());

	    //
	    // if the request is executed by a user logged with OAuth 2.0, then the token provider returns the
	    // email used as identifier and compares it with the configured admin email
	    //
	    if (email.isPresent() && isAdmin(email.get())) {

		user.get().setIdentifier(email.get());
		user.get().setRole(BasicRole.ADMIN.getRole());
	    }
	}

	if (authProvider.isPresent()) {

	    user.get().setAuthProvider(authProvider.get());
	}

	if (logBuilder != null) {

	    logBuilder.append("\n- User found: ").append(user.get());

	    GSLoggerFactory.getLogger(getClass()).debug(logBuilder.toString());
	}

	return user.get();
    }

    /**
     * @param id
     * @return
     */
    private Optional<String> getViewCreator(String id) throws Exception {

	Optional<View> view = viewManager.getView(id);

	if (view.isEmpty()) {

	    Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(id);

	    if (dynamicView.isPresent()) {

		if (dynamicView.get() instanceof DynamicViewAnd dva) {

		    List<Bond> operands = dva.getDynamicBond().getOperands();

		    for (Bond operand : operands) {

			if (operand instanceof ViewBond viewBond) {

			    view = viewManager.getView(viewBond.getViewIdentifier());
			}
		    }
		}
	    }
	}

	return view.map(View::getCreator);
    }

    /**
     * Determines if the given user is an admin by comparing its identifier (e-mail) with the one provided by the configuration
     *
     * @param user
     * @return
     * @throws GSException
     */
    private boolean isAdmin(String email) throws GSException {

	Optional<String> adminUser = ConfigurationWrapper.readAdminIdentifier();

	Boolean discard = ConfigurationWrapper.getSystemSettings().//
		readKeyValue(SystemSetting.KeyValueOptionKeys.DISCARD_ESSI_OAUTH_ADMIN_USER).//
		map(Boolean::valueOf).//
		orElse(false);

	return !discard && adminUser.filter(email::equalsIgnoreCase).isPresent();
    }

    /**
     * @return
     */
    private boolean discardEssiOauthAdminUser() {

	return ConfigurationWrapper.getSystemSettings().//
		readKeyValue(SystemSetting.KeyValueOptionKeys.DISCARD_ESSI_OAUTH_ADMIN_USER).//
		map(Boolean::valueOf).//
		orElse(false);
    }

}
