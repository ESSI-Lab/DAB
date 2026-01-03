package eu.essi_lab.gssrv.rest;

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
public class UpdateUserRequest {
    private String email;
    private String apiKey;
    private String userIdentifier;
    private String propertyName;
    private String propertyValue;

    public String getUserIdentifier() {
	return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
	this.userIdentifier = userIdentifier;
    }

    public String getPropertyName() {
	return propertyName;
    }

    public void setPropertyName(String propertyName) {
	this.propertyName = propertyName;
    }

    public String getPropertyValue() {
	return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
	this.propertyValue = propertyValue;
    }

    // Default constructor for JSON deserialization
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String email, String apiKey) {
	this.email = email;
	this.apiKey = apiKey;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getApiKey() {
	return apiKey;
    }

    public void setApiKey(String apiKey) {
	this.apiKey = apiKey;
    }
}
