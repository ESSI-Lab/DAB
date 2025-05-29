/**
 * 
 */
package eu.essi_lab.authorization.psloader;

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

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.authorization.rps.AbstractRolePolicySet;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class DefaultPolicySetLoader implements PolicySetLoader {

    private PolicySetWrapper ppsToSet;

    @Override
    public List<PolicySetWrapper> loadRolePolicySets() {

	return StreamUtils.iteratorToStream(ServiceLoader.load(AbstractRolePolicySet.class).iterator()).collect(Collectors.toList());
    }

    @Override
    public List<PolicySetWrapper> loadPermissionPolicySets() {

	List<PolicySetWrapper> out = StreamUtils
		.iteratorToStream(//
			ServiceLoader.load(AbstractPermissionPolicySet.class).iterator())
		.//
		collect(Collectors.toList());

	if (ppsToSet != null) {

	    PolicySetWrapper policySet = out.stream().//
		    filter(pps -> pps.getPolicySet().getPolicySetId().equals(ppsToSet.getPolicySet().getPolicySetId())).//
		    findFirst().//
		    get();

	    out.remove(policySet);
	    out.add(ppsToSet);
	}

	return out;
    }

    @Override
    public void setPermissionPolicySet(PolicySetWrapper pps) {

	this.ppsToSet = pps;
    }
}
