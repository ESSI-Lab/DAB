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

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.authorization.rps.AbstractRolePolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * A {@link PolicySetLoader} which loads a single policy having the anonymous role with admin permissions.<br>
 * To use for test purpose
 * 
 * @author Fabrizio
 */
public class AnonymousFullGrantsLoader implements PolicySetLoader {

    /**
     * @author Fabrizio
     */
    private class AnonymousPPS extends AbstractPermissionPolicySet {

	public AnonymousPPS() {

	    super(BasicRole.ANONYMOUS.getRole());
	}

	@Override
	protected void editPPSPolicy() {

	    setDiscoveryAction(createRandomId());
	    setAccessAction(createRandomId());
	}
    }

    /**
     * @author Fabrizio
     */
    private class AnonymousRPS extends AbstractRolePolicySet {

	public AnonymousRPS() {

	    super(BasicRole.ANONYMOUS.getRole());
	}
    }

    @Override
    public List<PolicySetWrapper> loadRolePolicySets() {

	return Arrays.asList(new PolicySetWrapper() {

	    @Override
	    public String getRole() {

		return BasicRole.ANONYMOUS.getRole();
	    }

	    @Override
	    public PolicySet getPolicySet() {

		return new AnonymousRPS().getPolicySet();
	    }
	});
    }

    @Override
    public List<PolicySetWrapper> loadPermissionPolicySets() {

	return Arrays.asList(new PolicySetWrapper() {

	    @Override
	    public String getRole() {

		return BasicRole.ANONYMOUS.getRole();
	    }

	    @Override
	    public PolicySet getPolicySet() {

		return new AnonymousPPS().getPolicySet();
	    }
	});
    }
}
