package eu.essi_lab.model.configuration;

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(Include.NON_NULL)
public class GSInitConfigurationResponse {

    private Result result;
    private Message message;

    public enum Message {

	INVALID_URL("Invalid URL"),
	UNKNOWN(""),
	CONFIGURATION_URL(""),
	EXISTING_AND_ROOT_NOT_ALLOWED("You can not use an existing configuration and provide a root user"),
	ROOT_USER_REQUIRED("The root user is mandatory when creating a new configuration"),
	NO_OAUTH_PROVIDER("OAuth provider is required"),
	NO_OAUTH_PROVIDER_ID("Missing OAuth Client Id"),
	NO_OAUTH_PROVIDER_SECRET("Missing OAuth Client Secret");

	private String msg;

	private Message(String m) {
	    msg = m;
	}

	@Override
	@JsonValue
	public String toString() {

	    return msg;

	}

	public void setMessage(String m) {
	    msg = m;
	}

    }

    public enum Result {
	SUCCESS,
	FAIL;

    }

    public Result getResult() {
	return result;
    }

    public void setResult(Result result) {
	this.result = result;
    }

    public Message getMessage() {

	return message;
    }

    public void setMessage(Message m) {

	message = m;
    }

}
