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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.amazonaws.util.StringUtils;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.messages.bond.DynamicViewAnd;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class DefaultUserFinder extends UserFinder {

    private Optional<String> email;
    private Optional<String> authProvider;

    @Override
    protected List<String> findIdentifiers(HttpServletRequest request) throws Exception {
	//
	// find all the identifiers in the request according
	// to different strategies
	//
	List<String> identifiers = new ArrayList<>();

	//
	// 1 - OAuth 2.0 identifier
	//
	email = tokenProvider.findOAuth2Attribute(request, true);
	authProvider = tokenProvider.findOAuth2Attribute(request, false);

	if (email.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).debug("OAuth 2.0 email: {}", email.get());
	    identifiers.add(email.get());
	}

	if (authProvider.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).debug("OAuth 2.0 provider: {}", authProvider.get());
	}

	//
	// 2 - remote host
	//
	String remoteHost = request.getRemoteHost();
	GSLoggerFactory.getLogger(getClass()).debug("Remote host: {}", remoteHost);

	identifiers.add(remoteHost);

	//
	// 3 - 'x-forwarded-for' header
	//
	List<String> xForwardedForHeaders = WebRequest.readXForwardedForHeaders(request);

	identifiers.addAll(xForwardedForHeaders);

	xForwardedForHeaders.forEach(ip -> GSLoggerFactory.getLogger(getClass()).debug(WebRequest.X_FORWARDED_FOR_HEADER + ": {}", ip));

	//
	// 4 - 'Origin' header
	//
	Optional<String> originHeader = WebRequest.readOriginHeader(request);

	if (originHeader.isPresent()) {

	    identifiers.add(originHeader.get());

	    GSLoggerFactory.getLogger(getClass()).debug(WebRequest.ORIGIN_HEADER + ": {}", originHeader.get());
	}

	//
	// 5,6 - view identifier and creator
	//
	WebRequest webRequest = new WebRequest();
	webRequest.setServletRequest(request, false);

	Optional<String> viewId = webRequest.extractViewId();

	viewId.ifPresent(id -> {

	    GSLoggerFactory.getLogger(getClass()).debug("View id: {}", id);
	    identifiers.add(id);

	    String creator = getViewCreator(id);
	    if (creator != null) {
		GSLoggerFactory.getLogger(getClass()).debug("View creator: {}", creator);
		identifiers.add(creator);
	    }

	});
	
	

	//
	// 7 - client identifier
	//
	Optional<String> clientId = webRequest.readClientIdentifierHeader();

	clientId.ifPresent(id -> identifiers.add(id));
	
	//
	// 8 - path token
	//
	
	Optional<String> tokenId = webRequest.extractTokenId();

	tokenId.ifPresent(id -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Token id: {}", id);
	    identifiers.add(id);

	});

	return identifiers;
    }

    public String getViewCreator(String id) {
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

    @Override
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
	    addUser(user.get());
	}
    }

    @Override
    protected GSUser findUser(List<String> identifiers) throws Exception {

	List<GSUser> users = getUsers();

	List<GSUser> matchedUsers = users.stream().filter(u -> //

	// user must be enabled
	u.isEnabled() && (

	//
	// normal case, string equality comparison
	//
	identifiers.contains(u.getIdentifier())
		//
		// this case is useful for example when a user can be identified not only by
		// a specific identifier (case covered by the above case)
		// but from a partial identifier, for example a partial IP
		// with less than 4 groups (e.g: 192.168.12)
		//
		|| identifiers.stream().anyMatch(id -> id.contains(u.getIdentifier())))

	).collect(Collectors.toList());
	
	Optional<GSUser> user = matchedUsers.stream().sorted(new Comparator<GSUser>() {

	    @Override
	    public int compare(GSUser o1, GSUser o2) {
		return o2.getIdentifier().compareTo(o1.getIdentifier());
	    }
	}).findFirst(); // selects the matched user with the longer identifier. this is to select whos-34324 instead of general whos (to be deleted)
	

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

		    GSLoggerFactory.getLogger(getClass()).debug("Admin user registered in the configuration");

		    user.get().setRole(BasicRole.ADMIN.getRole());
		}
	    }
	}

	if (authProvider.isPresent()) {

	    user.get().setAuthProvider(authProvider.get());
	}

	GSLoggerFactory.getLogger(getClass()).debug("User found: {}", user.get());

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

	if (configuration == null) {

	    return false;
	}

	Optional<String> configRootUser = configuration.readAdminIdentifier();

	if (configRootUser.isPresent()) {

	    return user.getIdentifier() != null && user.getIdentifier().equalsIgnoreCase(configRootUser.get());
	}

	return false;
    }

    public static void main(String[] args) throws Exception {
	String id = UUID.randomUUID().toString();
	String token = eu.essi_lab.lib.utils.StringUtils.hashSHA1messageDigest(id);
	System.out.println(id);
	System.out.println(token);
    }
}
