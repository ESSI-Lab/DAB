package eu.essi_lab.authorization.pps;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

/**
 * @author Fabrizio
 */
public abstract class AbstractGEOSSViewPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    static final String GEOSS_VIEW_CREATOR = "geoss";

    /**
     * 
     */
    public static final String SUPPORTED_PATH = "rest-views";

    /**
     * @param role
     */
    public AbstractGEOSSViewPermissionPolicySet(String role) {
	super(role);
    }

    private String userIdentifier;

    /**
     * @param userIdentifier
     */
    public void setUserIdentifier(String userIdentifier) {

	this.userIdentifier = userIdentifier;
    }

    /**
     * @return the userIdentifier
     */
    public String getUserIdentifier() {

	return userIdentifier;
    }

}
