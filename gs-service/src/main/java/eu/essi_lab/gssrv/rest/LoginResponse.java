package eu.essi_lab.gssrv.rest;

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
public class LoginResponse {
    private boolean success;
    private String message;
    private String firstName;
    private String lastName;
    private String email;
    private String apiKey;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, String firstName, String lastName, String email, String apiKey) {
	this.success = success;
	this.message = message;
	this.firstName = firstName;
	this.lastName = lastName;
	this.email = email;
	this.apiKey = apiKey;
    }

    public boolean isSuccess() {
	return success;
    }

    public void setSuccess(boolean success) {
	this.success = success;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public String getFirstName() {
	return firstName;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public void setLastName(String lastName) {
	this.lastName = lastName;
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
