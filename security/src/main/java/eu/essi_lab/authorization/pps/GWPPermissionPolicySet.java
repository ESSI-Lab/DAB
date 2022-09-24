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

import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class GWPPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    private static final String[] GWP_ORIGINS_HEADER = new String[] {

	    "https://www.geoportal.org", //
	    "https://geoss.uat.esaportal.eu", //
    };

    public GWPPermissionPolicySet() {

	super("gwp");
    }

    @Override
    protected void editPPSPolicy() {

	ApplyType discoveryApplyType = createPathApply(//
		"opensearch", //
		"csw", //
		"cswisogeo", //
		"hiscentral.asmx", //
		"arpa-rest", //
		"cuahsi_1_1.asmx", //
		"rest", //
		"hydrocsv", //
		"sos", //
		"gwis", //
		"ArcGIS", //
		"gwps");

	//
	// discovery rule for the non GWP clients
	//
	{
	    String ruleId = "Permission:to:discover:not:gwp:clients";

	    setDiscoveryAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);

	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    ApplyType notGeossViewCreatorApply = XACML_JAXBUtils.createNotApply(createOriginHeaderApply(GWP_ORIGINS_HEADER));

	    setAndCondition(ruleId, discoveryApplyType, notGeossViewCreatorApply);
	}

	//
	// discovery rule for the GWP clients
	//
	{
	    String ruleId = "Permission:to:discover:gwp:clients";

	    setDiscoveryAction(ruleId);

	    setAndCondition(ruleId, discoveryApplyType, createOriginHeaderApply(GWP_ORIGINS_HEADER));
	}

	//
	// access rule for the non GWP clients
	//
	{
	    String ruleId = "Permission:to:access:not:gwp:clients";

	    setAccessAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);

	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    ApplyType notGeossViewCreatorApply = XACML_JAXBUtils.createNotApply(createOriginHeaderApply(GWP_ORIGINS_HEADER));

	    setAndCondition(ruleId, createAccessPathApply(), notGeossViewCreatorApply);
	}

	//
	// access rule for the GWP clients
	//
	{
	    String ruleId = "Permission:to:access:gwp:clients";

	    setAccessAction(ruleId);

	    setAndCondition(ruleId, createAccessPathApply(), createOriginHeaderApply(GWP_ORIGINS_HEADER));
	}
    }
}
