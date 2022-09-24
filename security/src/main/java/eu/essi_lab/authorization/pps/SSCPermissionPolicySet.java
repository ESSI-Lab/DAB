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
 * This policy allows in particular the Service Status Checker requests coming from 194.67.141.5 to execute "Describe sensor" requests on the "sos" path, since the {@link DescribeSensorTransformer} 
 * set the page size to 10000 at the moment, and an anonymous user would be excluded
 * 
 * @author Fabrizio
 */
public class SSCPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    private static final List<String> ALLOWED_IP_LIST = Arrays.asList(//
	    "194.67.141.5",//
	    "152.61.128.50");//

    public SSCPermissionPolicySet() {

	super("ssc", StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_OVERRIDES.getId());
    }

    @Override
    protected void editPPSPolicy() {

	{
	    String discoveryRuleId = "Permission:to:discover:essilab:client";

	    setDiscoveryAction(discoveryRuleId);

	    ApplyType allowedClientId = createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	    setAndCondition(discoveryRuleId, allowedClientId);
	}
	{
	    String discoveryRuleId = "Permission:to:discover:ssc:client";

	    setDiscoveryAction(discoveryRuleId);

	    ApplyType pathApply = createPathApply("sos", "wcs");
	    ApplyType ipApply = createAllowedIPApply(ALLOWED_IP_LIST.toArray(new String[] {}));

	    setAndCondition(discoveryRuleId, pathApply, ipApply);
	}

	{
	    String accessRuleId = "Permission:to:access:essilab:client";

	    setAccessAction(accessRuleId);

	    ApplyType allowedClientId = createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	    setAndCondition(accessRuleId, allowedClientId);
	}
	{
	    String accessRuleId = "Permission:to:access:ssc:client";

	    setAccessAction(accessRuleId);

	    ApplyType pathApply = createPathApply("sos", "wcs");
	    ApplyType ipApply = createAllowedIPApply(ALLOWED_IP_LIST.toArray(new String[] {}));

	    setAndCondition(accessRuleId, pathApply, ipApply);
	}
    }
}
