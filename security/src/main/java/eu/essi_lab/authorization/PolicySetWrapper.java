/**
 * 
 */
package eu.essi_lab.authorization;

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

import java.util.List;
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public interface PolicySetWrapper {

    /**
     * @author Fabrizio
     */
    public enum Action {
	/**
	 * 
	 */
	DISCOVERY,
	/**
	 * 
	 */
	DISCOVERY_SEMANTIC,
	/**
	 * 
	 */
	STATISTICS,
	/**
	 * 
	 */
	ACCESS,
	/**
	 * 
	 */
	READ_VIEW,
	/**
	 * 
	 */
	CREATE_VIEW,
	/**
	 * 
	 */
	UPDATE_VIEW,
	/**
	 * 
	 */
	DELETE_VIEW,
	/**
	 * 
	 */
	OTHER;//

	/**
	 * @param name
	 */
	private Action() {
	}

	/**
	 * @return
	 */
	public String getId() {

	    return name().toLowerCase();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum Issuer {
	/**
	 * 
	 */
	OFFSET,
	/**
	 * 
	 */
	MAX_RECORDS,
	/**
	 * 
	 */
	VIEW_ID,
	/**
	 * 
	 */
	VIEW_CREATOR,
	/**
	 * 
	 */
	DOWNLOAD,
	/**
	 * 
	 */
	PATH,
	/**
	 * 
	 */
	ALLOWED_IP,
	/**
	 * 
	 */
	SOURCE,
	/**
	 * 
	 */
	CLIENT_IDENTIFIER,
	/**
	 * 
	 */
	ORIGIN,
	/**
	 * 
	 */
	VIEW_VISIBILITY,
	/**
	 * 
	 */
	VIEW_OWNER;

	/**
	 * @return
	 */
	public String getId() {

	    return name().toLowerCase();
	}
    }

    /**
     * @param policies
     * @param identifier
     * @return
     */
    public static Optional<Policy> getPolicy(List<PolicySetWrapper> policies, String identifier) {

	return policies.//
		stream().//
		map(p -> p.getPolicySet()).//
		map(p -> p.getPolicySetsAndPoliciesAndPolicySetIdReferences()).//
		flatMap(p -> p.stream()).//
		filter(s -> s instanceof Policy).//
		map(s -> (Policy) s).//
		filter(p -> p.getPolicyId().equals(identifier)). //
		findFirst();
    }

    /**
     * @param policies
     * @param identifier
     * @return
     */
    public static Optional<PolicySet> getPolicySet(List<PolicySetWrapper> policies, String identifier) {

	return policies.//
		stream().//
		map(p -> p.getPolicySet()).//
		filter(p -> p.getPolicySetId().equals(identifier)).//
		findFirst();
    }

    /**
     * @return
     */
    String getRole();

    /**
     * @return
     */
    PolicySet getPolicySet();
}
