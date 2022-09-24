/**
 * 
 */
package eu.essi_lab.authorization.pps;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;

import eu.essi_lab.messages.web.WebRequest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class KMAPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    private static final List<String> ALLOWED_IP_LIST = Arrays.asList(//
	    "203.247.93.102", //
	    "203.247.93.103", //
	    "203.247.93.114", //
	    "221.151.118.17", //
	    "210.107.255.106", //
	    "210.107.255.24", //
	    "210.107.255.22", //
	    "210.107.255.108", //
	    "203.239.43.21", //
	    "218.154.54.13",//
	    "203.247.93.106");//

    public KMAPermissionPolicySet() {

	super("kma", StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_OVERRIDES.getId());
    }

    @Override
    protected void editPPSPolicy() {

	{
	    String discoveryRuleId = "clientRule";

	    //
	    // discovery rule
	    //
	    setDiscoveryAction(discoveryRuleId);

	    ApplyType allowedClientId = createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	    setAndCondition(discoveryRuleId, allowedClientId);
	}
	{
	    String discoveryRuleId = "ipAndPathRule";

	    //
	    // discovery rule
	    //
	    setDiscoveryAction(discoveryRuleId);

	    ApplyType pathApply = createPathApply("csw", "kmaoaipmh");
	    ApplyType ipApply = createAllowedIPApply(ALLOWED_IP_LIST.toArray(new String[] {}));

	    setAndCondition(discoveryRuleId, pathApply, ipApply);
	}

    }
}
