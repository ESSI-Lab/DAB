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

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.messages.web.WebRequest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class AnonymousPermissionPolicySet extends AbstractPermissionPolicySet {

    public AnonymousPermissionPolicySet() {

	super(BasicRole.ANONYMOUS.getRole());
    }

    @Override
    protected void editPPSPolicy() {

	//
	// discovery rule
	//
	{
	    String ruleId = createRandomId();

	    setDiscoveryAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);
	    
	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    setAndCondition(ruleId, createDiscoveryPathApply());
	}

	//
	// access rule
	//
	{
	    String ruleId = createRandomId();

	    setAccessAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);
	    
	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    setAndCondition(ruleId, createAccessPathApply());
	}
	
	//
	// ESSILab Client Identifier rule
	//
	{
	    String discoveryRuleId = "clientRule";

	    //
	    // discovery rule
	    //
	    setDiscoveryAction(discoveryRuleId);

	    ApplyType allowedClientId = createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	    setAndCondition(discoveryRuleId, allowedClientId);
	}
    }
}
