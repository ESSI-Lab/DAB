/**
 * 
 */
package eu.essi_lab.authorization.builder;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.jaxb.common.ObjectFactories;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;

/**
 * @author Fabrizio
 */
public class PPSPolicyBuilder {

    private Policy policy;
    private List<Serializable> rulesList;

    /**
     * 
     */
    public PPSPolicyBuilder(String policyId, String ruleCombiningAlgorithm) {

	rulesList = new ArrayList<Serializable>();

	policy = new Policy(//
		null, //
		null, //
		null, //
		ObjectFactories.XACML().createTarget(), //
		rulesList, //
		null, //
		null, //
		policyId, //
		"1.0", //
		ruleCombiningAlgorithm, //
		null); //
    }

    /**
     * @param rule
     * @return
     */
    public PPSPolicyBuilder addRule(Rule rule) {

	rulesList.add(rule);
	return this;
    }

    /**
     * @return
     */
    public Policy build() {

	return policy;
    }
}
