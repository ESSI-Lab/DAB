package eu.essi_lab.gssrv.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.OutputFormat;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Profile;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Target;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class SupportService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/odip")
    public Response odip(//
	    @QueryParam("target") String target, //
	    @QueryParam("term") String term, //
	    @QueryParam("profile") String profile, //
	    @QueryParam("label") String label, //
	    @QueryParam("suggestion") String suggestion, //
	    @QueryParam("callback") String callback) { //

	ODIPVocabularyHandler handler = new ODIPVocabularyHandler();
	handler.setOutputFormat(OutputFormat.JSON);

	String output = null;
	try {
	    if (label != null) {
		output = handler.getTerm(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target), label);
	    } else if (term != null) {
		output = handler.getLabel(term);
	    } else {
		output = handler.listLabels(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target), suggestion);
	    }
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    JSONObject object = new JSONObject();
	    object.put("error", ex.getMessage());
	    output = object.toString();
	}

	output = callback + "(" + output + ")";

	return Response.ok(output, MediaType.APPLICATION_JSON).build();
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);

	if (loginResponse.isSuccess()) {

	    return Response.ok(loginResponse).build();

	} else {

	    return Response.serverError().entity(loginResponse).build();
	}
    }

    private LoginResponse getLoginResponse(LoginRequest request) {
	try {

	    UserFinder uf = UserFinder.create();
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
		    LoginResponse response = new LoginResponse(true, "Login successful", user.getStringPropertyValue("firstName"),
			    user.getStringPropertyValue("lastName"), request.getEmail(), request.getApiKey());
		    response.setPermissions(user.getStringPropertyValue("permissions"));
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
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    LoginResponse resp = new LoginResponse(false, "Server error: " + ex.getMessage(), null, null, null, null);
	    return resp;
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/listUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);
	BasicResponse listResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		listResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    HashMap<String, List<GSUser>> usersByRole = new HashMap<String, List<GSUser>>();
		    GSUser adminUser = null;
		    for (GSUser user : users) {
			if (request.getApiKey().equals(user.getUri())) {
			    adminUser = user;
			}
			String role = user.getRole();
			List<GSUser> list = usersByRole.get(role);
			if (list == null) {
			    list = new ArrayList<GSUser>();
			    usersByRole.put(role, list);
			}
			list.add(user);
		    }
		    if (adminUser == null) {
			listResponse.setSuccess(false);
			listResponse.setMessage("admin user not found");
			return Response.serverError().entity(listResponse).build();
		    }
		    List<GSUser> userList = usersByRole.get(adminUser.getRole());
		    for (GSUser user : userList) {
			listResponse.getUsers().add(user);
		    }
		    return Response.ok(listResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    listResponse.setSuccess(false);
		    listResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(listResponse).build();
		}

	    } else {
		listResponse.setSuccess(false);
		listResponse.setMessage("not authorized");
		return Response.serverError().entity(listResponse).build();
	    }
	} else {
	    listResponse.setSuccess(false);
	    listResponse.setMessage("not authenticated");
	    return Response.serverError().entity(listResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/updateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UpdateUserRequest request) {
	LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getApiKey());
	LoginResponse loginResponse = getLoginResponse(loginRequest);
	BasicResponse basicResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		basicResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    GSUser targetUser = null;
		    for (GSUser user : users) {
			if (request.getUserIdentifier().equals(user.getUri())) {
			    targetUser = user;
			}
		    }
		    if (targetUser == null) {
			basicResponse.setSuccess(false);
			basicResponse.setMessage("target user not found");
			return Response.serverError().entity(basicResponse).build();
		    }
		    targetUser.setPropertyValue(request.getPropertyName(), request.getPropertyValue());
		    uf.getWriter().store(targetUser);

		    return Response.ok(basicResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    basicResponse.setSuccess(false);
		    basicResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(basicResponse).build();
		}

	    } else {
		basicResponse.setSuccess(false);
		basicResponse.setMessage("not authorized");
		return Response.serverError().entity(basicResponse).build();
	    }
	} else {
	    basicResponse.setSuccess(false);
	    basicResponse.setMessage("not authenticated");
	    return Response.serverError().entity(basicResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @DELETE
    @Path("/deleteUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(UpdateUserRequest request) {
	LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getApiKey());
	LoginResponse loginResponse = getLoginResponse(loginRequest);
	BasicResponse basicResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		basicResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    GSUser targetUser = null;
		    for (GSUser user : users) {
			if (request.getUserIdentifier().equals(user.getUri())) {
			    targetUser = user;
			}
		    }
		    if (targetUser == null) {
			basicResponse.setSuccess(false);
			basicResponse.setMessage("target user not found");
			return Response.serverError().entity(basicResponse).build();
		    }
		    uf.getWriter().removeUser(request.getUserIdentifier());

		    return Response.ok(basicResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    basicResponse.setSuccess(false);
		    basicResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(basicResponse).build();
		}

	    } else {
		basicResponse.setSuccess(false);
		basicResponse.setMessage("not authorized");
		return Response.serverError().entity(basicResponse).build();
	    }
	} else {
	    basicResponse.setSuccess(false);
	    basicResponse.setMessage("not authenticated");
	    return Response.serverError().entity(basicResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/modifyUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyUser(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);
	BasicResponse listResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		listResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    HashMap<String, List<GSUser>> usersByRole = new HashMap<String, List<GSUser>>();
		    GSUser adminUser = null;
		    for (GSUser user : users) {
			if (request.getApiKey().equals(user.getUri())) {
			    adminUser = user;
			}
			String role = user.getRole();
			List<GSUser> list = usersByRole.get(role);
			if (list == null) {
			    list = new ArrayList<GSUser>();
			    usersByRole.put(role, list);
			}
			list.add(user);
		    }
		    if (adminUser == null) {
			listResponse.setSuccess(false);
			listResponse.setMessage("admin user not found");
			return Response.serverError().entity(listResponse).build();
		    }
		    List<GSUser> userList = usersByRole.get(adminUser.getRole());
		    for (GSUser user : userList) {
			listResponse.getUsers().add(user);
		    }
		    return Response.ok(listResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    listResponse.setSuccess(false);
		    listResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(listResponse).build();
		}

	    } else {
		listResponse.setSuccess(false);
		listResponse.setMessage("not authorized");
		return Response.serverError().entity(listResponse).build();
	    }
	} else {
	    listResponse.setSuccess(false);
	    listResponse.setMessage("not authenticated");
	    return Response.serverError().entity(listResponse).build();
	}
    }
}
