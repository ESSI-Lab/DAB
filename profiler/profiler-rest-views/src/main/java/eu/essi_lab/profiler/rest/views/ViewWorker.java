package eu.essi_lab.profiler.rest.views;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.Optional;

import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSWriteRolePolicySet;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ViewWorker {

    private DatabaseReader reader;
    private StorageInfo databaseURI;
    private WebRequest request;
    private RequestMessage message;
    private DatabaseWriter writer;

    /**
     * @param message
     * @throws GSException
     */
    public ViewWorker(RequestMessage message) throws GSException {
	this.message = message;
	this.request = message.getWebRequest();
	this.databaseURI = ConfigurationWrapper.getStorageInfo();
	this.reader = DatabaseProviderFactory.getReader(databaseURI);
	this.writer = DatabaseProviderFactory.getWriter(databaseURI);
    }

    /**
     * To be updated with new roles.<br>
     * Retrieves the base view identifier of the views that the given user can handle,
     * according to his role (e.g.: users with role
     * "geoss-read", "geoss-private-write" or "geoss-write" can handle the root view "geoss")
     * 
     * @param user
     * @return
     */
    static String getUserRootViewIdentifier(GSUser user) {

	String role = user.getRole();

	switch (role) {
	case GEOSSReadRolePolicySet.ROLE:
	case GEOSSPrivateWriteRolePolicySet.ROLE:
	case GEOSSWriteRolePolicySet.ROLE:
	case BasicRole.ADMIN_ROLE_VALUE:
	    return "geoss";

	case BasicRole.ANONYMOUS_ROLE_VALUE:

	    throw new IllegalArgumentException("Unknown token");
	}

	throw new IllegalArgumentException("User role " + user.getRole() + " has no associated base view");
    }

    /**
     * @param webRequest
     * @return
     */
    static Optional<JSONObject> getBodyView(WebRequest webRequest) throws RuntimeException {

	ClonableInputStream bodyStream = webRequest.getBodyStream();

	Optional<JSONObject> out = Optional.empty();

	switch (webRequest.getServletRequest().getMethod().toUpperCase()) {
	case "PUT":
	case "POST":

	    try {
		JSONObject bodyView = new JSONObject(IOStreamUtils.asUTF8String(bodyStream.clone()));

		//
		// view fields check (to be improved with constraints check)
		//

		if (!bodyView.has("id")) {

		    throw new RuntimeException("Required field 'id' of the request view is missing");
		}

		if (!bodyView.has("label")) {

		    throw new RuntimeException("Required field 'label' of the request view is missing");
		}

		out = Optional.of(bodyView);

	    } catch (JSONException | IOException e) {

		throw new RuntimeException("Unable to parse view: " + e.getMessage());
	    }
	}

	return out;
    }

    /**
     * @return
     */
    public DatabaseReader getDatabaseReader() {
	return reader;
    }

    /**
     * @return
     */
    public DatabaseWriter getDatabaseWriter() {

	return writer;
    }

    /**
     * @return
     */
    public WebRequest getRequest() {

	return request;
    }

    /**
     * @return
     */
    public RequestMessage getMessage() {

	return message;
    }

    /**
     * @param status
     * @param text
     * @return
     */
    protected static String createMessage(Status status, String text) {

	JSONObject object = new JSONObject();
	object.put("status", status.getStatusCode());
	object.put("statusText", text);

	return object.toString(3);
    }

    // /**
    // * @param text
    // * @return
    // */
    // protected static String createMessage(String text) {
    //
    // JSONObject object = new JSONObject();
    // object.put("status", Status.OK.getStatusCode());
    // object.put("statusText", text);
    //
    // return object.toString(3);
    // }
}
