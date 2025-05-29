/**
 * 
 */
package eu.essi_lab.authorization.pps;

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

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import eu.essi_lab.messages.web.WebRequest;

/**
 * Anonymous users are allowed to discovery and access (no other actions are allowed),
 * if and only if:<br>
 * <br>
 * 1) offset is <= 200<br>
 * 2) max records <= 50<br>
 * 3) the discovery path is supported (for discovery queries)<br>
 * 5) the access path is supported (for access queries)<br>
 * <br>
 * OR<br>
 * <br>
 * 1) the client identifier {@link WebRequest#ESSI_LAB_CLIENT_IDENTIFIER} is provided, in this case also other actions
 * are allowed
 * 
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
	    String ruleId = "anonymous:discovery:rule";

	    setDiscoveryAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);

	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    setAndCondition(ruleId, //
		    createDiscoveryPathApply(), //
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply("i-change"))

	    );
	}

	//
	// access rule
	//
	{
	    String ruleId = "anonymous:access:rule";

	    setAccessAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);

	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    setAndCondition(//
		    ruleId, //
		    createAccessPathApply(), //
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply("i-change"))

	    );


	}

	//
	// ESSI-Lab client discovery rule
	//
	{
	    String ruleId = "anonymous:discovery:essi:client:rule";

	    setDiscoveryAction(ruleId);

	    setAndCondition(ruleId, createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER));
	}

	//
	// ESSI-Lab client access rule
	//
	{
	    String ruleId = "anonymous:access:essi:client:rule";

	    setAccessAction(ruleId);

	    setAndCondition(ruleId, createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER));
	}

	//
	// ESSI-Lab client other rule
	//
	{
	    String ruleId = "anonymous:other:action:essi:client:rule";

	    setOtherAction(ruleId);

	    setAndCondition(ruleId, createAllowedClientIdentifiersApply(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER));
	}
    }
}
