package eu.essi_lab.authorization.psloader;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.pps.AdminPermissionPolicySet;
import eu.essi_lab.authorization.pps.AnonymousPermissionPolicySet;
import eu.essi_lab.authorization.rps.AdminRolePolicySet;
import eu.essi_lab.authorization.rps.AnonymousRolePolicySet;

/**
 * @author Fabrizio
 */
public class DefaultPolicySetLoader implements PolicySetLoader {

    @Override
    public List<PolicySetWrapper> loadRolePolicySets() {

	return Arrays.asList(//
		new AdminRolePolicySet(), //
		new AnonymousRolePolicySet() //
);
    }

    @Override
    public List<PolicySetWrapper> loadPermissionPolicySets() {

	return Arrays.asList(//
		new AdminPermissionPolicySet(), //
		new AnonymousPermissionPolicySet() //
);
    }
}
