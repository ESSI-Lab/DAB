/**
 * 
 */
package eu.essi_lab.authorization;

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

import eu.essi_lab.model.auth.GSUser;

/**
 * @author Fabrizio
 */
public enum BasicRole {

    /**
     * 
     */
    DEFAULT("default"),
    /**
     * 
     */
    ADMIN("admin"),
    /**
     * 
     */
    ANONYMOUS("anonymous");
    
    /**
     * 
     */
    public static final String ANONYMOUS_ROLE_VALUE = "anonymous";
    
    /**
     * 
     */
    public static final String ADMIN_ROLE_VALUE = "admin";


    private String role;

    /**
     * @param policySet
     */
    private BasicRole(String role) {

	this.role = role;
    }

    /**
     * @return
     */
    public String getRole() {

	return role;
    }

    /**
     * @return
     */
    public static GSUser createAnonymousUser() {

	GSUser user = new GSUser();
	user.setIdentifier(BasicRole.ANONYMOUS.getRole());
	user.setRole(BasicRole.ANONYMOUS.getRole());

	return user;
    }
}
