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

import java.util.Arrays;
import java.util.List;

/**
 * KMA users are allowed ONLY to discover if and only if:<br><br>
 *  
 * 1) the view identifier is "KMA"<br>
 *  
 * 2) the discovery path is "csw" or "kmaoaipmh"<br>
 *  
 * 3) the IP is supported (redundant check)<br>
 * 
 * @author Fabrizio
 */
public class KMAPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    public static final List<String> ALLOWED_IP_LIST = Arrays.asList(//
	    "203.247.93.102", //
	    "203.247.93.103", //
	    "203.247.93.114", //
	    "221.151.118.17", //
	    "210.107.255.106", //
	    "210.107.255.24", //
	    "210.107.255.22", //
	    "210.107.255.108", //
	    "203.239.43.21", //
	    "218.154.54.13", //
	    "203.247.93.106");//

    public KMAPermissionPolicySet() {

	super("kma");
    }

    @Override//	    setAndCondition(ruleId, //
//    createDiscoveryPathApply(), //
//    XACML_JAXBUtils.createNotApply(createViewCreatorApply("trigger")));
    protected void editPPSPolicy() {

	String ruleId = "kmaoaipmh:discovery:rule";

	setDiscoveryAction(ruleId);

	setAndCondition(//
		ruleId, //

		createPathApply("csw", "kmaoaipmh"), //
		createViewIdentifiersApply("KMA"), //
		createAllowedIPApply(ALLOWED_IP_LIST.toArray(new String[] {}))// redundant check
	);
    }
}
