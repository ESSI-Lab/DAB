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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class CSW_RIPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * @param role
     */
    public CSW_RIPermissionPolicySet() {

	super("cswri");
    }

    @Override
    protected void editPPSPolicy() {

	String discoveryRuleId = createRandomId();

	//
	// discovery rule
	//
	setDiscoveryAction(discoveryRuleId);

	ApplyType pathApply = createPathApply("csw");
	ApplyType viewIdApply = createViewIdentifiersApply("cite-csw-ri");

	setAndCondition(discoveryRuleId, pathApply, viewIdApply);
    }
}
