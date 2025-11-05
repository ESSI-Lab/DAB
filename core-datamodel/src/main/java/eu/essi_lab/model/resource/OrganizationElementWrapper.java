package eu.essi_lab.model.resource;

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

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementBuilder;

/**
 * @author Fabrizio
 */
public class OrganizationElementWrapper {

    private ComposedElement element;

    /**
     * @param element
     */
    private OrganizationElementWrapper(ComposedElement element) {

	this.element = element;
    }

    /**
     * @param name
     * @return
     */
    public static ComposedElement build() {

	return ComposedElementBuilder.get("organization_v2").//

		addItem("orgName", Queryable.ContentType.TEXTUAL).//
		addItem("orgURI", Queryable.ContentType.TEXTUAL).//
		addItem("individualName", Queryable.ContentType.TEXTUAL).//
		addItem("individualURI", Queryable.ContentType.TEXTUAL).//
		addItem("email", Queryable.ContentType.TEXTUAL).//
		addItem("role", Queryable.ContentType.TEXTUAL).//
		addItem("homePageURL", Queryable.ContentType.TEXTUAL).//
		addItem("hash", Queryable.ContentType.TEXTUAL).//
		build();
    }

    /**
     * @return
     */
    public static OrganizationElementWrapper get() {

	return new OrganizationElementWrapper(build());
    }

    /**
     * @return the element
     */
    public ComposedElement getElement() {

	return element;
    }

    /**
     * @return
     */
    public String getHash() {

	return element.getProperty("hash").get().getStringValue();
    }

    /**
     * @param hash
     */
    public void setHash(String hash) {

	element.getProperty("hash").get().setValue(hash);
    }

    /**
     * @return
     */
    public String getOrgName() {

	return element.getProperty("orgName").get().getStringValue();
    }

    /**
     * @param orgName
     */
    public void setOrgName(String orgName) {

	element.getProperty("orgName").get().setValue(orgName);
    }

    /**
     * @return
     */
    public String getOrgUri() {

	return element.getProperty("orgURI").get().getStringValue();
    }

    /**
     * @return
     */
    public String getIndividualName() {

	return element.getProperty("individualName").get().getStringValue();
    }

    /**
     * @param individualName
     */
    public void setIndividualName(String individualName) {

	element.getProperty("individualName").get().setValue(individualName);
    }

    /**
     * @return
     */
    public String getIndividualURI() {

	return element.getProperty("individualURI").get().getStringValue();
    }

    /**
     * @param individualURI
     */
    public void setIndividualURI(String individualURI) {

	element.getProperty("individualURI").get().setValue(individualURI);
    }

    /**
     * @return
     */
    public String getEmail() {

	return element.getProperty("email").get().getStringValue();
    }

    /**
     * @param email
     */
    public void setEmail(String email) {

	element.getProperty("email").get().setValue(email);
    }

    /**
     * @return
     */
    public String getRole() {

	return element.getProperty("role").get().getStringValue();
    }

    /**
     * @param role
     */
    public void setRole(String role) {

	element.getProperty("role").get().setValue(role);
    }

    /**
     * @return
     */
    public String getHomePageURL() {

	return element.getProperty("homePageURL").get().getStringValue();
    }

    /**
     * @param URL
     */
    public void setHomePageURL(String url) {

	element.getProperty("homePageURL").get().setValue(url);
    }

    /**
     * @param orgURI
     */
    public void getOrgUri(String orgURI) {

	element.getProperty("orgURI").get().setValue(orgURI);
    }

    /**
     * @param orgName
     */
    public void setOrgURI(String orgURI) {

	element.getProperty("orgURI").get().setValue(orgURI);
    }

}
