/**
 * 
 */
package eu.essi_lab.authorization.builder;

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

import java.util.ArrayList;

import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

/**
 * @author Fabrizio
 */
public class RPSBuilder {

    /**
     * 
     */
    public RPSBuilder() {
    }

    /**
     * @return
     */
    public static PolicySet build(String role) {

	ArrayList<AnyOf> anyOfList = new ArrayList<AnyOf>();

	AnyOf anyOf = XACML_JAXBUtils.createAnyOfAllOfMatch(//
		StandardFunction.ANYURI_EQUAL.getId(), //
		"roles;" + role, //
		StandardDatatypes.ANYURI.getId(), //

		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value(), //
		StandardDatatypes.ANYURI.getId(), //
		false);//

	anyOfList.add(anyOf);

	Target target = new Target(anyOfList);

	return XACML_JAXBUtils.createPolicySet(//
		target, //
		"RPS:" + role + ":role", //
		"PPS:" + role + ":role", //
		StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_PERMIT_OVERRIDES.getId());
    }
}
