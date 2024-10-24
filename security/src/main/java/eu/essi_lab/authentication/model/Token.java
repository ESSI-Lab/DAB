package eu.essi_lab.authentication.model;

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

import java.util.Objects;

public class Token {

    private String type;
    private String token;
    private String email;
    private String tokenSecret;
    private String serviceProvider;
    private String clientURL;

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getTokenSecret() {
	return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
	this.tokenSecret = tokenSecret;
    }

    public String getServiceProvider() {
	return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
	this.serviceProvider = serviceProvider;
    }

    @Override
    public int hashCode() {
	return Objects.hash(type, token, email, tokenSecret, serviceProvider);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null)
	    return false;
	if (!(obj instanceof Token))
	    return false;
	Token token = (Token) obj;
	if (this.type == null ? token.type != null : !this.type.equals(token.type))
	    return false;
	if (this.token == null ? token.token != null : !this.token.equals(token.token))
	    return false;
	if (this.email == null ? token.email != null : !this.email.equals(token.email))
	    return false;
	if (this.tokenSecret == null ? token.tokenSecret != null : !this.tokenSecret.equals(token.tokenSecret))
	    return false;
	if (this.serviceProvider == null ? token.serviceProvider != null : !this.serviceProvider.equals(token.serviceProvider))
	    return false;
	return true;
    }

    public String getClientURL() {
	return clientURL;
    }

    public void setClientURL(String clientURL) {
	this.clientURL = clientURL;
    }
}
