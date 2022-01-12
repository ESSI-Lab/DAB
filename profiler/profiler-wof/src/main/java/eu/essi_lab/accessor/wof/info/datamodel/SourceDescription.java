package eu.essi_lab.accessor.wof.info.datamodel;

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

import eu.essi_lab.messages.bond.SpatialExtent;
public class SourceDescription {
    private String url;
    private String description;
    private String title;
    private String email;
    private String phone;
    private String organization;
    private String organizationWebsite;
    private String serviceInformationURL;
    private String citation;
    private String aabstract;
    private String valuecount;
    private String variablecount;
    private String sitecount;
    private String networkName;
    private String serviceID;
    private String supplementalInformation;
    private SpatialExtent extent;

    public String getServiceInformationURL() {
	return serviceInformationURL;
    }

    public void setServiceInformationURL(String serviceInformationURL) {
	this.serviceInformationURL = serviceInformationURL;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    public String getOrganization() {
	return organization;
    }

    public void setOrganization(String organization) {
	this.organization = organization;
    }

    public String getOrganizationWebsite() {
	return organizationWebsite;
    }

    public void setOrganizationWebsite(String organizationWebsite) {
	this.organizationWebsite = organizationWebsite;
    }

    public String getCitation() {
	return citation;
    }

    public void setCitation(String citation) {
	this.citation = citation;
    }

    public String getAabstract() {
	return aabstract;
    }

    public void setAabstract(String aabstract) {
	this.aabstract = aabstract;
    }

    public String getValuecount() {
	return valuecount;
    }

    public void setValuecount(String valuecount) {
	this.valuecount = valuecount;
    }

    public String getVariablecount() {
	return variablecount;
    }

    public void setVariablecount(String variablecount) {
	this.variablecount = variablecount;
    }

    public String getSitecount() {
	return sitecount;
    }

    public void setSitecount(String sitecount) {
	this.sitecount = sitecount;
    }

    public String getNetworkName() {
	return networkName;
    }

    public void setNetworkName(String networkName) {
	this.networkName = networkName;
    }

    public String getServiceID() {
	return serviceID;
    }

    public void setServiceID(String serviceID) {
	this.serviceID = serviceID;
    }

    public String getSupplementalInformation() {
	return supplementalInformation;
    }

    public void setSupplementalInformation(String supplementalInformation) {
	this.supplementalInformation = supplementalInformation;
    }

    public SpatialExtent getExtent() {
	return extent;
    }

    public void setExtent(SpatialExtent extent) {
	this.extent = extent;
    }

}
