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
public class EIFFELPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    private static final String EIFFEL_VIEW_CREATOR = "eiffel";

    public EIFFELPermissionPolicySet() {

	super("eiffel");
    }

    @Override
    protected void editPPSPolicy() {

	//
	// discovery rule for the non EIFFEL creator
	//
	{
	    String ruleId = "Permission:to:discover:not:eiffel:creator";

	    setDiscoveryAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);
	    
	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    ApplyType notEIFFELCreatorApply = XACML_JAXBUtils.createNotApply(createViewCreatorApply(EIFFEL_VIEW_CREATOR));

	    setAndCondition(ruleId, createDiscoveryPathApply(), notEIFFELCreatorApply);
	}

	//
	// discovery rule for the EIFFEL creator
	//
	{
	    String ruleId = "Permission:to:discover:eiffel:creator";

	    setDiscoveryAction(ruleId);

	    setAndCondition(ruleId, createDiscoveryPathApply(), createViewCreatorApply(EIFFEL_VIEW_CREATOR));
	}

	//
	// access rule for the non EIFFEL creator
	//
	{
	    String ruleId = "Permission:to:access:not:eiffel:creator";

	    setAccessAction(ruleId);

	    setOffsetLimit(ruleId, DEFAULT_OFFSET_LIMIT);
	    
	    setMaxRecordsLimit(ruleId, DEFAULT_MAX_RECORDS_LIMIT);

	    ApplyType notEIFFELCreatorApply = XACML_JAXBUtils.createNotApply(createViewCreatorApply(EIFFEL_VIEW_CREATOR));

	    setAndCondition(ruleId, createAccessPathApply(), notEIFFELCreatorApply);
	}

	//
	// access rule for the EIFFEL creator
	//
	{
	    String ruleId = "Permission:to:access:eiffel:creator";

	    setAccessAction(ruleId);

	    setAndCondition(ruleId, createAccessPathApply(), createViewCreatorApply(EIFFEL_VIEW_CREATOR));
	}
    }

}
