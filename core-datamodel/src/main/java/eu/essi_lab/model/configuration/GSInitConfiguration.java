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

@JsonInclude(Include.NON_EMPTY)
public class GSInitConfiguration {

    private Boolean useExisting;
    
    private Boolean relativePath;
    
    private String url;

    private String rootUser;

    private String oauthProviderName;

    private String oauthProviderSecret;

    private String oauthProviderId;

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public Boolean getUseExisting() {
	return useExisting;
    }

    public void setUseExisting(Boolean useExisting) {
	this.useExisting = useExisting;
    }

    public Boolean getRelativePath() {
	return relativePath;
    }

    public void setRelativePath(Boolean relativePath) {
	this.relativePath = relativePath;
    }

    public String getRootUser() {
	return rootUser;
    }

    public void setRootUser(String rootUser) {
	this.rootUser = rootUser;
    }

    public String getOauthProviderName() {
	return oauthProviderName;
    }

    public void setOauthProviderName(String oauthProviderName) {
	this.oauthProviderName = oauthProviderName;
    }

    public String getOauthProviderSecret() {
	return oauthProviderSecret;
    }

    public void setOauthProviderSecret(String oauthProviderSecret) {
	this.oauthProviderSecret = oauthProviderSecret;
    }

    public String getOauthProviderId() {
	return oauthProviderId;
    }

    public void setOauthProviderId(String oauthProviderId) {
	this.oauthProviderId = oauthProviderId;
    }
}
