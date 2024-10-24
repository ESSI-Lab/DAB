/**
 * 
 */
package eu.essi_lab.authorization.builder;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;

import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class PPSBuilder {

    private PolicySet pps;
    private List<Serializable> list;
    
    /**
     * @param role
     */
    public PPSBuilder(String role) {

	list = new ArrayList<Serializable>();
	
	pps = XACML_JAXBUtils.createPolicySet(//
		"PPS:" + role + ":role", //
		list,//
		StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_OVERRIDES.getId());
    }

    /**
     * @param policy
     * @return
     */
    public PPSBuilder addPolicy(Policy policy) {

	list.add(policy);
	return this;
    }

    /**
     * @return
     */
    public PolicySet build() {

	return pps;
    }
}
