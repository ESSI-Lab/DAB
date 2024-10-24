package eu.essi_lab.authorization.pps;

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

import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 *         -> enables discovery and access of resources from views having creator (base view) "geoss" and (visibility
 *         "public" or (visibility "private" and owner is the user))
 *         -> enables listing of views having creator (base view) "geoss" and (visibility "public" or (visibility
 *         "private" and owner is the user))
 *         -> enables creation of views having base view "geoss" and with private visibility
 *         -> enables deleting of views having as owner the user identifier
 *         -> updating allowed if:
 *         1) the existing view (view to update) owns to the user
 *         2) the updated view has creator (base view) "geoss" and private visibility
 */
public class GEOSSPrivateWritePermissionPolicySet extends GEOSSReadPermissionPolicySet {

    /**
     * 
     */
    public GEOSSPrivateWritePermissionPolicySet() {

	super(GEOSSPrivateWriteRolePolicySet.ROLE);
    }

    @Override
    protected void editPPSPolicy() {

	super.editPPSPolicy();

	//
	// allowed create views rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:create:views";

	    setCreateViewAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createPathApply(GEOSSWritePermissionPolicySet.SUPPORTED_PATH), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR),

		    createViewVisibilityApply(ViewVisibility.PRIVATE)); //
	}

	//
	// allowed update views rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:update:views";

	    setUpdateViewAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createPathApply(GEOSSWritePermissionPolicySet.SUPPORTED_PATH), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR),

		    createViewVisibilityApply(ViewVisibility.PRIVATE), //

		    createViewOwnerApply(getUserIdentifier())); //
	}

	//
	// allowed delete views rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:delete:views";

	    setDeleteViewAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createPathApply(GEOSSWritePermissionPolicySet.SUPPORTED_PATH), //

		    createViewOwnerApply(getUserIdentifier()));
	}
    }
}
