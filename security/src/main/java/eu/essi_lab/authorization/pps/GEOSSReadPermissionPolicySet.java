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

import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * -> enables discovery and access of resources from views having creator (base view) "geoss" and (visibility "public"
 * or (visibility "private" and owner is the user))
 * -> enables listing of views having creator (base view) "geoss" and (visibility "public" or (visibility "private" and
 * owner is the user))
 * 
 * @author Fabrizio
 */
public class GEOSSReadPermissionPolicySet extends AbstractGEOSSViewPermissionPolicySet {

    /**
    * 
    */
    public GEOSSReadPermissionPolicySet() {

	this(GEOSSReadRolePolicySet.ROLE);
    }

    /**
     * 
     */
    public GEOSSReadPermissionPolicySet(String role) {

	super(role);
    }

    @Override
    protected void editPPSPolicy() {

	//
	// allowed discovery rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:discover";

	    setDiscoveryAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createDiscoveryPathApply(), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR),

		    createORApply(

			    createViewVisibilityApply(ViewVisibility.PUBLIC),

			    createANDApply(

				    createViewVisibilityApply(ViewVisibility.PRIVATE), //
				    createViewOwnerApply(getUserIdentifier())

			    ))); //
	}

	//
	// allowed access rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:access";

	    setAccessAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createDiscoveryPathApply(), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR),

		    createORApply(

			    createViewVisibilityApply(ViewVisibility.PUBLIC),

			    createANDApply(

				    createViewVisibilityApply(ViewVisibility.PRIVATE), //
				    createViewOwnerApply(getUserIdentifier())

			    ))); //
	}

	//
	// allowed read views rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:read:views";

	    setReadViewAction(ruleId);

	    setAndCondition(//

		    ruleId, //
		    createDiscoveryPathApply(), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR),

		    createORApply(

			    createViewVisibilityApply(ViewVisibility.PUBLIC),

			    createANDApply(

				    createViewVisibilityApply(ViewVisibility.PRIVATE), //
				    createViewOwnerApply(getUserIdentifier())

			    ))); //
	}
    }
}
