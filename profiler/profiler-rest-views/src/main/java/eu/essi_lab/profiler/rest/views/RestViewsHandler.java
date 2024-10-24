/**
 * 
 */
package eu.essi_lab.profiler.rest.views;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.authorization.xacml.XACMLAuthorizer;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.view.CreateViewMessage;
import eu.essi_lab.messages.view.DeleteViewMessage;
import eu.essi_lab.messages.view.ReadViewMessage;
import eu.essi_lab.messages.view.UpdateViewMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * @author Fabrizio
 */
public class RestViewsHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	RestViewsValidator validator = new RestViewsValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String method = webRequest.getServletRequest().getMethod();

	Optional<String> optViewId = webRequest.extractViewId();

	Optional<View> optView = Optional.empty();

	if (optViewId.isPresent()) {

	    optView = getDatabaseReader().getView(optViewId.get());
	}

	Optional<JSONObject> bodyView = Optional.empty();

	try {

	    bodyView = ViewWorker.getBodyView(webRequest);

	} catch (RuntimeException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    builder = builder.status(Status.BAD_REQUEST);

	    return ViewWorker.createMessage(Status.BAD_REQUEST, ex.getMessage());
	}

	RequestMessage message = null;
	method = method.toUpperCase();

	switch (method) {
	case "GET":
	    if (optViewId.isPresent() && !optView.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View [" + optViewId.get() + "] not found");
	    }

	    message = new ReadViewMessage();
	    break;
	case "POST":

	    if (!bodyView.isPresent()) {

		return ViewWorker.createMessage(Status.BAD_REQUEST, "Request body missing");
	    }

	    String newViewId = bodyView.get().getString("id");

	    if (!StringUtils.hasOnlyLettersNumbersAndUnderscores(newViewId)) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "The given view identifier [" + newViewId + "] is not valid");
	    }

	    message = new CreateViewMessage();

	    break;

	case "PUT":

	    //
	    // this check is actually redundant since it is already done in ViewWorker.getBodyView(webRequest)
	    //
	    if (!optViewId.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View identifier missing");
	    }

	    if (!optView.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View [" + optViewId.get() + "] to update not found");
	    }

	    if (!bodyView.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "Request body missing");
	    }

	    String bodyViewId = bodyView.get().getString("id");
	    if (!bodyViewId.equals(optViewId.get())) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View identifier in the request path [" + optViewId.get()
			+ "] and view identifier in the request body [" + bodyViewId + "] do not match");
	    }

	    message = new UpdateViewMessage();

	    break;
	case "DELETE":

	    // this should never happen, since a GET or DELETE request without id
	    // in the last uri segment is not accepted
	    if (!optViewId.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View identifier missing");
	    }

	    if (!optView.isPresent()) {

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, "View [" + optViewId.get() + "] not found");
	    }

	    message = new DeleteViewMessage();
	    break;
	}

	//
	// authorization
	//

	boolean authorized = false;

	try {

	    authorized = authorize(bodyView, message, webRequest, optView, optViewId, method);

	} catch (RuntimeException rex) {

	    builder = builder.status(Status.BAD_REQUEST);

	    return ViewWorker.createMessage(Status.BAD_REQUEST, rex.getMessage());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    return ViewWorker.createMessage(Status.BAD_REQUEST, ex.getMessage());
	}

	if (!authorized) {

	    builder = builder.status(Status.FORBIDDEN);

	    return ViewWorker.createMessage(Status.FORBIDDEN, "Unauthorized");
	}

	//
	// GET view/views
	//
	if (method.equals("GET")) {

	    GetViewWorker worker = new GetViewWorker(message, optView);
	    try{
	    
		return worker.get();
		
	    }catch (RuntimeException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, e.getMessage().replace("\"", "'"));
	    }
	}

	//
	// POST: add new view
	//
	if (method.equals("POST")) {

	    PostViewWorker worker = new PostViewWorker(message, bodyView.get());

	    try {
		return worker.post();

	    } catch (RuntimeException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, e.getMessage().replace("\"", "'"));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		builder = builder.status(Status.INTERNAL_SERVER_ERROR);

		return ViewWorker.createMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage().replace("\"", "'"));
	    }
	}

	//
	// PUT: edit view
	//
	if (method.equals("PUT")) {
	    PutViewWorker worker = new PutViewWorker(message, bodyView.get());

	    try {

		return worker.put();

	    } catch (RuntimeException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		builder = builder.status(Status.BAD_REQUEST);

		return ViewWorker.createMessage(Status.BAD_REQUEST, e.getMessage().replace("\"", "'"));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		builder = builder.status(Status.INTERNAL_SERVER_ERROR);

		return ViewWorker.createMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage().replace("\"", "'"));
	    }
	}

	//
	// Delete view
	//

	DeleteViewWorker worker = new DeleteViewWorker(message, optView.get());

	try {

	    return worker.delete();

	} catch (RuntimeException e) {

	    builder = builder.status(Status.FORBIDDEN);

	    return ViewWorker.createMessage(Status.FORBIDDEN, e.getMessage());

	} catch (Exception e) {

	    builder = builder.status(Status.INTERNAL_SERVER_ERROR);

	    return ViewWorker.createMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

    /**
     * @param bodyView
     * @param message
     * @param webRequest
     * @param optViewToUdpate
     * @param optViewId
     * @param method
     * @return
     * @throws Exception
     */
    private boolean authorize(//
	    Optional<JSONObject> bodyView, //
	    RequestMessage message, //
	    WebRequest webRequest, //
	    Optional<View> optViewToUdpate, //
	    Optional<String> optViewId, //
	    String method) throws Exception {

	XACMLAuthorizer authorizer = new XACMLAuthorizer();

	message.setRequestId(webRequest.getRequestId());

	message.setWebRequest(webRequest);

	message.setCurrentUser(webRequest.getCurrentUser());

	message.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	String query = webRequest.getQueryString();

	Page page = new Page(1, 1);

	if (query != null) {

	    KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());

	    String s = parser.getValue("start");
	    String c = parser.getValue("count");

	    if (s != null) {
		page.setStart(Integer.valueOf(s));
	    }

	    if (c != null) {
		page.setSize(Integer.valueOf(c));
	    }
	}

	message.setPage(page);

	//
	// a GET request without view ID is a "paginated list view" request.
	// this kind of request is always permitted, and the views allowed to be listed
	// (possibly none) are filtered in the GetViewWorker.get method
	//
	if (method.equals("GET") && !optViewId.isPresent()) {

	    authorizer.close();

	    return true;
	}

	//
	// we are going to retrieve an existing view,
	//
	if (optViewId.isPresent() && (method.equals("GET") || method.equals("DELETE"))) {

	    WebRequestTransformer.setView(optViewId.get(), message.getDataBaseURI(), message);
	}

	//
	// in case of POST method, the view is not yet created but the message needs a view
	// since the visibility and creator properties are required to evaluate the creation rule of the write view
	// policies.
	// so we need to create a view with the required properties and put it in the message
	//

	else if (method.equals("POST")) {

	    setMessageView(webRequest.getCurrentUser().getIdentifier(), bodyView, message);
	}

	//
	// in case of PUT method, a view with its identifier already exists in the DB.
	// there are 2 checks to do, both to the existing view and to the updated view
	// 1) the user is allowed to update the existing view with the given identifier?
	// the check is done comparing the user identifier with the owner of the existing view, they must match.
	// we must use as view owner the owner of the existing view, in order to verify that the view to update owns to
	// the user
	// 2) the user is allowed to set the given visibility and base view (creator) to the updated view?
	// if we use the existing view to evaluate the update rule of the write view policies, the visibility and
	// creator properties are the ones of the existing view, and not the ones of the view which is in the request
	// body.
	//
	// so we need to create a view with the visibility and creator of the view in the request body, but with the
	// owner of the existing view, then we put it in the message
	//
	// for example, a user with "geoss-private-view" role wants to update one of its views (all with private
	// visibility) with a view having public visibility. the request must be denied, but if to evaluate it we use
	// the existing view (having private visibility), the request will be authorized
	// the same reasonings can be applied to the parent view that for a user with "geoss-private-view" role must be
	// "geoss", so the updated view must be compliant with this rule
	//
	//
	else if (method.equals("PUT")) {

	    //
	    // using the owner of the existing view
	    //
	    setMessageView(optViewToUdpate.get().getOwner(), bodyView, message);
	}

	boolean authorized = authorizer.isAuthorized(message);

	authorizer.close();

	return authorized;
    }

    /**
     * @param userIdentifier
     * @param bodyView
     * @param message
     * @throws GSException
     */
    private void setMessageView(String userIdentifier, Optional<JSONObject> bodyView, RequestMessage message) throws GSException {

	ViewVisibility visibility = bodyView.get().getBoolean("visible") == true ? ViewVisibility.PUBLIC : ViewVisibility.PRIVATE;

	//
	// per default the parent view of a view correspond to the user root view (creator), e.g.: geoss
	//
	String userRootViewId = ViewWorker.getUserRootViewIdentifier(message.getCurrentUser().get());

	String creator = userRootViewId;

	if (bodyView.get().has("parentView")) {

	    //
	    // if the parentView is present, we check...
	    //

	    String parentViewId = bodyView.get().getString("parentView");

	    Optional<View> optParentView = getDatabaseReader().getView(parentViewId);

	    //
	    // 1) if the related view exists
	    //

	    if (!optParentView.isPresent()) {

		throw new RuntimeException("The given parent view [" + parentViewId + "] do not exists");
	    }

	    //
	    // 2) if the related view has as creator the the user base view
	    //
	    // this check could be done directly here instead of in the XACML authorizer, this way
	    // a detailed error message can be returned
	    // otherwise, if the check fails, the request will be forbidden without explanation
	    //
	    //

	    creator = optParentView.get().getCreator();

	    if (creator == null) {

		creator = AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE;
	    }

	    //
	    // if (!creator.equals(userBaseViewId)) {
	    //
	    // throw new RuntimeException("The given parent view [" + parentViewId + "] is not a sub-view of [" +
	    // userBaseViewId + "]");
	    // }

	    //
	    // 3) if the given parent view is usable by the user. to be usable it must be public or he should own it
	    //

	    if (bodyView.get().has("parentView")) {

		String parentView = bodyView.get().getString("parentView");

		if (!checkParentView(parentView, message.getCurrentUser().get())) {

		    throw new RuntimeException("Current user is not authorized to set the given parent view [" + parentView + "] as parent");
		}
	    }
	}

	View view = new View(UUID.randomUUID().toString());

	// used by both create and update rules
	view.setVisibility(visibility);
	view.setCreator(creator);
	// used by the update rule
	view.setOwner(userIdentifier);

	message.setView(view);
    }

    /**
     * @return
     * @throws GSException
     */
    private DatabaseReader getDatabaseReader() throws GSException {

	return DatabaseProviderFactory.getDatabaseReader(ConfigurationWrapper.getDatabaseURI());
    }

    /**
     * @param parentViewId
     * @param currentUser
     * @return
     * @throws GSException
     */
    private boolean checkParentView(String parentViewId, GSUser currentUser) throws GSException {

	List<String> privateUserViewIds = new ArrayList<String>();
	List<String> publicViewIds = new ArrayList<String>();

	privateUserViewIds = getDatabaseReader().getViewIdentifiers(

		GetViewIdentifiersRequest.create(//

			ViewWorker.getUserRootViewIdentifier(currentUser), //
			currentUser.getIdentifier(), //
			ViewVisibility.PRIVATE)

	);

	publicViewIds = getDatabaseReader().getViewIdentifiers(

		GetViewIdentifiersRequest.create(//

			ViewWorker.getUserRootViewIdentifier(currentUser), //
			ViewVisibility.PUBLIC));

	List<String> allowedViewIds = new ArrayList<>(privateUserViewIds);
	allowedViewIds.addAll(publicViewIds);

	return allowedViewIds.contains(parentViewId);
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("application", "json");
    }
}
