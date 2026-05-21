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

/**
 * Resolves shape upload owners and checks who may list, upload, or delete predefined shape areas.
 */
public final class PredefinedShapeAccess {

    /** Owner value stored for administrator uploads and actions. */
    public static final String ADMIN_OWNER = eu.essi_lab.messages.bond.spatial.ShapeLayerOwner.ADMIN_OWNER;

    public static final String FORBIDDEN_MESSAGE = "Not allowed to modify this shape area";

    private PredefinedShapeAccess() {
    }

    /**
     * @param loginResponse authenticated session
     * @return {@value #ADMIN_OWNER} for administrators, otherwise the user URI (API key)
     */
    public static String ownerFromLogin(LoginResponse loginResponse) {

	if (loginResponse.isAdmin()) {
	    return ADMIN_OWNER;
	}

	if (loginResponse.getUser() != null && loginResponse.getUser().getUri() != null
		&& !loginResponse.getUser().getUri().isBlank()) {
	    return loginResponse.getUser().getUri();
	}

	return loginResponse.getApiKey();
    }

    /**
     * @param actorOwner owner of the current user ({@link #ownerFromLogin(LoginResponse)})
     * @param actorIsAdmin whether the current user is an administrator
     * @param resourceOwner owner stored on the shape upload or feature; may be empty for legacy data
     * @return {@code true} if the actor may change or delete the resource
     */
    public static boolean canManage(String actorOwner, boolean actorIsAdmin, String resourceOwner) {

	if (actorIsAdmin) {
	    return true;
	}

	if (resourceOwner == null || resourceOwner.isBlank()) {
	    return false;
	}

	return actorOwner != null && actorOwner.equals(resourceOwner);
    }
}
