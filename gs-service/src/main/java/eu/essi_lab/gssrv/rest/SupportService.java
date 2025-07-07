package eu.essi_lab.gssrv.rest;

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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.OutputFormat;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Profile;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Target;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class SupportService {

    /**
     * @author Fabrizio
     */
    public record LoginRequest(String email, String apiKey) {
	public LoginRequest() {// default constructor for JSON deserialization
	    this(null, null);
	}
    };

    /**
     * @author Fabrizio
     */
    public record LoginResponse(boolean success, String message, String firstName, String lastName, String email, String apiKey) {
	public LoginResponse() {// default constructor for JSON deserialization
	    this(false, null, null, null, null, null);
	}
    };

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

		if (request.apiKey().equals(user.getUri()) && request.email().equals(email)) {
		    LoginResponse response = new LoginResponse(true, "Login successful", "Test", "User", request.email(), request.apiKey());
		    return Response.ok(response).build();
		}
	    }

	    LoginResponse response = new LoginResponse(false, "Invalid credentials", null, null, null, null);
	    return Response.ok(response).build();

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    return Response.serverError().entity(new LoginResponse(false, "Server error: " + ex.getMessage(), null, null, null, null))
		    .build();
	}
    }
}
