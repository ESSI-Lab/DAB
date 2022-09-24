package eu.essi_lab.model.pluggable;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * A provider of {@link Pluggable} components
 *
 * @author Fabrizio
 */
public class Provider {

    private String email;
    private String organization;

    public static Provider essiLabProvider() {
	return new ESSILabProvider();
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getOrganization() {
	return organization;
    }

    public void setOrganization(String organization) {
	this.organization = organization;
    }

    @Override
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof Provider))
	    return false;

	Provider provider = (Provider) object;
	return ((this.getEmail() == null && provider.getEmail() == null) || this.getEmail().equals(provider.getEmail())) && (
		(this.getOrganization() == null && provider.getOrganization() == null) || this.getOrganization().equals(
			provider.getOrganization()));

    }
}
