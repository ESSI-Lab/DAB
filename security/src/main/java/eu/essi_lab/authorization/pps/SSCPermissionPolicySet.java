/**
 * 
 */
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

import java.util.Arrays;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * This policy allows the Service Status Checker requests coming from 194.67.141.5 to execute "Describe
 * sensor" requests on the "sos" path, since the {@link DescribeSensorTransformer}
 * set the page size to 10000 at the moment, and an anonymous user would be excluded
 * 
 * @author Fabrizio
 */
public class SSCPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    public static final List<String> ALLOWED_IP_LIST = Arrays.asList(//
	    "194.67.141.5", //
	    "152.61.128.50");//

    public SSCPermissionPolicySet() {

	super("ssc");
    }

    @Override
    protected void editPPSPolicy() {

	ApplyType pathApply = createPathApply("sos", "wcs", "wms");
	ApplyType ipApply = createAllowedIPApply(ALLOWED_IP_LIST.toArray(new String[] {}));

	//
	// discovery
	//

	String discoveryRuleId = "permission:to:discover:ssc:client";

	setDiscoveryAction(discoveryRuleId);

	setAndCondition(discoveryRuleId, pathApply, ipApply);

	//
	// access
	//

	String accessRuleId = "permission:to:access:ssc:client";

	setAccessAction(accessRuleId);

	setAndCondition(accessRuleId, pathApply, ipApply);
    }
}
