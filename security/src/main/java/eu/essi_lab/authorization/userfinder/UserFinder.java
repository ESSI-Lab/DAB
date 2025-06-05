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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.authentication.util.TokenProvider;
import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.messages.bond.DynamicViewAnd;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserBaseClient;
import eu.essi_lab.model.auth.UserIdentifierType;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class UserFinder {

    private Optional<String> email;
    private Optional<String> authProvider;

    private static final String USER_FINDING_ERROR = "USER_FINDING_ERROR";

    private static final long USERS_UPDATE_PERIOD = TimeUnit.MINUTES.toMillis(30);
    private static final UsersCacheUpdater USERS_UPDATER_TASK = new UsersCacheUpdater();

    private static final long VIEWS_CACHE_DURATION = TimeUnit.MINUTES.toMillis(30);

    static {

	Timer timer = new Timer();
	timer.scheduleAtFixedRate(USERS_UPDATER_TASK, 0, USERS_UPDATE_PERIOD);
    }

    private static List<GSUser> users;
    private static final ExpiringCache<View> VIEWS = new ExpiringCache<>();
    static {
	VIEWS.setDuration(VIEWS_CACHE_DURATION);
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
    protected TokenProvider tokenProvider;
    private DatabaseWriter writer;
    private StringBuilder logBuilder;

    public UserFinder() {

	tokenProvider = new TokenProvider();
    }

    /**
     * @return
     * @throws GSException
     */
    public static UserFinder create() throws GSException {

	UserFinder finder = new UserFinder();

	StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();

	DatabaseReader reader = DatabaseProviderFactory.getReader(storageUri);
	DatabaseWriter writer = DatabaseProviderFactory.getWriter(storageUri);

	finder.setClient(reader);
	finder.setDatabaseReader(reader);
	finder.setDatabaseWriter(writer);

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
     * @param writer
     */
    public void setDatabaseWriter(DatabaseWriter writer) {

	this.writer = writer;
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

	USERS_UPDATER_TASK.setUserFinder(this);

	List<GSProperty<String>> identifiers = findIdentifiers(request);

	GSUser user = findUser(identifiers);

	return user;
    }

    /**
     * @param id
     * @return
     */
    private String getViewCreator(String id) {
	Optional<View> view = getView(id);
	if (view.isPresent()) {
	    // we can extract the creator of the view
	    return view.get().getCreator();
	} else {
	    // we can extract the creator of the view also in case of dynamic AND bonds (e.g. gs-view-and(...))
	    Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(id);
	    if (dynamicView.isPresent()) {
		if (dynamicView.get() instanceof DynamicViewAnd) {
		    DynamicViewAnd dva = (DynamicViewAnd) dynamicView.get();
		    List<Bond> operands = dva.getDynamicBond().getOperands();
		    for (Bond operand : operands) {
			if (operand instanceof ViewBond) {
			    ViewBond viewBond = (ViewBond) operand;
			    view = getView(viewBond.getViewIdentifier());
			    if (view.isPresent()) {
				return view.get().getCreator();
			    }
			}
		    }
		}
	    }
	}
	return null;
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
	    writer.store(user.get());
	}
    }

    /**
     * @throws Exception
     */
    public void updateUsersCache() throws Exception {

	synchronized (USERS_UPDATER_TASK) {

	    if (client != null) {

		users = client.getUsers();
	    }
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

	synchronized (USERS_UPDATER_TASK) {

	    if (users == null && client != null || !useCache) {

		updateUsersCache();

	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Reading users from cache");
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
	Optional<View> view = Optional.ofNullable(VIEWS.get(identifier));
	if (!view.isPresent()) {

	    try {
		view = getDatabaseReader().getView(identifier);

		if (view.isPresent()) {

		    VIEWS.put(identifier, view.get());
		}
	    } catch (GSException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return view;
    }

    /**
     * @param tokenProvider
     */
    public void setTokenProvider(TokenProvider tokenProvider) {

	this.tokenProvider = tokenProvider;
    }

    /**
     * @param client
     */
    public void setClient(UserBaseClient client) {

	this.client = client;
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

    /**
     * @return the writer
     */
    public DatabaseWriter getWriter() {

	return writer;
    }

    protected List<GSProperty<String>> findIdentifiers(HttpServletRequest request) throws Exception {

	//
	// find all the identifiers in the request according
	// to different strategies
	//
	List<GSProperty<String>> identifiers = new ArrayList<>();

	//
	// 1 - OAuth 2.0 identifier
	//
	email = tokenProvider.findOAuth2Attribute(request, true);
	authProvider = tokenProvider.findOAuth2Attribute(request, false);

	logBuilder = new StringBuilder();

	if (email.isPresent()) {

	    logBuilder.append("\n- OAuth 2.0 email: " + email.get());
	    identifiers.add(new GSProperty<String>(UserIdentifierType.OAUTH_EMAIL.getType(), email.get()));
	}

	if (authProvider.isPresent()) {

	    logBuilder.append("\n- OAuth 2.0 provider: " + authProvider.get());
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

	    viewId.ifPresent(id -> {

		logBuilder.append("\n- View id: " + id);
		identifiers.add(new GSProperty<>(UserIdentifierType.VIEW_IDENTIFIER.getType(), id));

		String creator = getViewCreator(id);
		if (creator != null) {

		    logBuilder.append("\n- View creator: " + creator);

		    identifiers.add(new GSProperty<>(UserIdentifierType.VIEW_CREATOR.getType(), creator));
		}
	    });
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
		filter(u -> u.isEnabled()).//

		// the user properties contains one of the found properties identifiers
		filter(u -> u.getProperties().stream().anyMatch(p -> identifiers.contains(p))).//

		findFirst();

	if (!user.isPresent()) {

	    user = Optional.of(BasicRole.createAnonymousUser());

	    //
	    // if the request is executed by a user logged with OAuth 2.0, then the token provider returns the
	    // email used as identifier. if such user is not registered in the userbase, the returned user will
	    // have the anonymous role with the email as identifier.
	    // the ESSIAdminService allows such user to handle the config since
	    // to determinate if the email owns to the admin, it makes a comparison with the config user email.
	    // anyway the user can be an admin user registered in the configuration, and the isAdmin checks it
	    //
	    if (email.isPresent()) {

		user.get().setIdentifier(email.get());

		if (isAdmin(user.get())) {

		    // GSLoggerFactory.getLogger(getClass()).debug("Admin user registered in the configuration");

		    user.get().setRole(BasicRole.ADMIN.getRole());
		}
	    }
	}

	if (authProvider.isPresent()) {

	    user.get().setAuthProvider(authProvider.get());
	}

	if (logBuilder != null) {

	    logBuilder.append("\n- User found: " + user.get());

	    GSLoggerFactory.getLogger(getClass()).debug(logBuilder.toString());
	}

	return user.get();
    }

    /**
     * Determines if the given user is an admin by comparing its identifier with the one provided by the configuration
     * 
     * @param user
     * @return
     * @throws GSException
     */
    private boolean isAdmin(GSUser user) throws GSException {

	Optional<String> configRootUser = ConfigurationWrapper.readAdminIdentifier();

	if (configRootUser.isPresent()) {

	    return user.getIdentifier() != null && user.getIdentifier().equalsIgnoreCase(configRootUser.get());
	}

	return false;
    }

}
