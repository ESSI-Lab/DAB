package eu.essi_lab.profiler.rest.handler.token;

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

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSWriteRolePolicySet;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserIdentifierType;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author Fabrizio
 */
public class TokenGeneratorHandler extends DefaultRequestHandler {

    @Override
    public final String getStringResponse(WebRequest webRequest) throws GSException {

	if (webRequest.isOptionsRequest()) {
	    return "";
	}

	String queryString = webRequest.getQueryString();

	KeyValueParser parser = new KeyValueParser(queryString);

	String role = parser.getValue("role");

	return generateTokenAndUser(role, "");
    }

    /**
     * @param role
     * @return
     * @throws GSException
     */
    public String generateTokenAndUser(String role, String tokenPostfix) throws GSException {

	String token = UUID.randomUUID().toString() + tokenPostfix;

	JSONObject object = new JSONObject();
	object.put("token", token);

	//
	//
	//

	GSUser user = new GSUser(token, UserIdentifierType.USER_TOKEN, role);

	DatabaseWriter writer = DatabaseProviderFactory.getDatabaseWriter(ConfigurationWrapper.getDatabaseURI());

	writer.store(user);

	//
	//
	//

	return object.toString(3);
    }

    /**
     * @return
     */
    protected String createXMLResponse(WebRequest webRequest) throws GSException {

	return null;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	if (webRequest.isOptionsRequest()) {

	    return null;
	}

	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	message.setResponseEncoding(MediaType.APPLICATION_JSON);

	if (request.isOptionsRequest()) {

	    return message;
	}

	String queryString = request.getQueryString();

	KeyValueParser parser = new KeyValueParser(queryString);

	String role = parser.getValue("role");

	if (role == null || role == KeyValueParser.UNDEFINED) {

	    message.setError("User role missing");
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setLocator("request");
	} else {

	    switch (role) {
	    case GEOSSPrivateWriteRolePolicySet.ROLE:
		break;
	    case GEOSSWriteRolePolicySet.ROLE:
		break;
	    case GEOSSReadRolePolicySet.ROLE:
		break;
	    default:

		message.setError("Unsupported role");
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setLocator("request");
	    }
	}

	return message;
    }
}
