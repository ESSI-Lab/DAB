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

import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;

/**
 * Users having this policy role, are allowed to discovery if and only if:<br>
 * <br>
 * 1) the view creator is {@link #getCreator()}<br>
 * 2) the discovery path is supported<br>
 * <br>
 * Users having this policy role, are allowed to access if and only if:<br>
 * <br>
 * 1) the view creator is {@link #getCreator()}<br>
 * 2) the access path is is supported<br>
 * <br>
 * Users having this policy role are also allowed to perform other actions if and only if:<br>
 * <br>
 * 1) the view creator is {@link #getCreator()}<br>
 * 2) the discovery path is supported OR the access path is is supported<br>
 * 
 * @author Fabrizio
 */
public abstract class CreatorPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * @param role
     */
    public CreatorPermissionPolicySet(String role) {

	super(role);
    }

    @Override
    protected void editPPSPolicy() {

	//
	// denied discovery rule
	//
	{
	    String ruleId = getRole() + ":denied:permission:to:discover";

	    setDiscoveryAction(ruleId);
	    setUnapplicableRule(ruleId);

	    setOrCondition(//
		    ruleId, //
		    XACML_JAXBUtils.createNotApply(createDiscoveryPathApply()), // unsupported path
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply(getCreator())) // wrong view creator

	    ); //
	}

	//
	// allowed discovery rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:discover";

	    setDiscoveryAction(ruleId);

	    setAndCondition(//
		    ruleId, //
		    createDiscoveryPathApply(), //
		    createViewCreatorApply(getCreator()) //
	    ); //
	}

	//
	// denied access rule
	//
	{
	    String ruleId = getRole() + ":denied:permission:to:access";

	    setAccessAction(ruleId);
	    setUnapplicableRule(ruleId);

	    setOrCondition(//
		    ruleId, //
		    XACML_JAXBUtils.createNotApply(createAccessPathApply()), // unsupported path
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply(getCreator())) // wrong view creator

	    ); //
	}

	//
	// allowed access rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:access";

	    setAccessAction(ruleId);

	    setAndCondition(//
		    ruleId, //
		    createAccessPathApply(), //
		    createViewCreatorApply(getCreator()));//
	}

	//
	// denied other action rule
	//
	{
	    String ruleId = getRole() + ":denied:permission:to:other:action";

	    setOtherAction(ruleId);
	    setUnapplicableRule(ruleId);

	    setOrCondition(//
		    ruleId, //
		    XACML_JAXBUtils.createNotApply(createAccessPathApply()), //
		    XACML_JAXBUtils.createNotApply(createDiscoveryPathApply()), //
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply(getCreator())) // wrong view creator

	    ); //
	}

	//
	// allowed other action rule
	//
	{
	    String ruleId = getRole() + ":allowed:permission:to:other:action";

	    setOtherAction(ruleId);

	    setAndCondition(//
		    ruleId, //

		    createORApply(//
			    createAccessPathApply(), //
			    createDiscoveryPathApply()), //

		    createViewCreatorApply(getCreator()));//
	}
    }

    /**
     * @return
     */
    protected abstract String getCreator();
}
