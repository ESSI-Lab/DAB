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

import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * GWP users are allowed to discovery and access if and only if:<br>
 * <br>
 * 1) the view creator is "geoss"<br>
 * 2) the origin header is "https://www.geoportal.org" or "https://geoss.uat.esaportal.eu"<br>
 * 3) the discovery path is one of:<br>
 * "opensearch",<br>
 * "csw",<br>
 * "cswisogeo",<br>
 * "hiscentral.asmx",<br>
 * "arpa-rest",<br>
 * "cuahsi_1_1.asmx",<br>
 * "rest",<br>
 * "hydrocsv",<br>
 * "sos",<br>
 * "gwis",<br>
 * "ArcGIS",<br>
 * "gwps"<br>
 * <br>
 * 4) the access path is supported<br>
 * 
 * @author Fabrizio
 */
public class GWPPermissionPolicySet extends AbstractPermissionPolicySet {

    /**
     * 
     */
    private static final String GEOSS_VIEW_CREATOR = "geoss";

    /**
     * 
     */
    public static final String[] GWP_ORIGINS_HEADER = new String[] {

	    "https://www.geoportal.org", //
	    "https://geoss.uat.esaportal.eu", //
	    "http://geoss.sit.esaportal.eu", //
	    "http://geoss.devel.esaportal.eu", //
	    
	    "https://geoss.sit.esaportal.eu", //
	    "https://geoss.devel.esaportal.eu",//
	    "https://afrigeoss.eversis.com",//	    
	    "https://gpp.devel.esaportal.eu",//
	    "https://gpp.uat.esaportal.eu",//
	    	    
            "http://gpp-data.devel.esaportal.eu",// 
            "http://gpp-data.uat.esaportal.eu",// 
            "http://gpp-data.sit.esaportal.eu",// 
            "http://data.geoportal.org",// 
            "http://data.geoportal.app",// 
            
            "https://gpp-data.devel.esaportal.eu",//  
            "https://gpp-data.uat.esaportal.eu",//  
            "https://gpp-data.sit.esaportal.eu",//  
            "https://data.geoportal.org",//  
            "https://data.geoportal.app" 
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
		"gwps",
		"worldcereal");

	//
	// denied discovery rule
	//
	{
	    String ruleId = "gwp:denied:permission:to:discover";

	    setDiscoveryAction(ruleId);
	    setUnapplicableRule(ruleId);

	    setOrCondition(//
		    ruleId, //
		    XACML_JAXBUtils.createNotApply(discoveryApplyType), // unsupported path
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply(GEOSS_VIEW_CREATOR)), // not geoss view
												// creator
		    XACML_JAXBUtils.createNotApply(createOriginHeaderApply(GWP_ORIGINS_HEADER)) // not geoss client
												// (redundant check)
	    );
	}

	//
	// allowed discovery rule
	//
	{
	    String ruleId = "gwp:allowed:permission:to:discover";

	    setDiscoveryAction(ruleId);

	    setAndCondition(//
		    ruleId, //
		    discoveryApplyType, //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR), //
		    createOriginHeaderApply(GWP_ORIGINS_HEADER) //
	    ); //
	}

	//
	// denied access rule
	//
	{
	    String ruleId = "gwp:denied:permission:to:access";

	    setAccessAction(ruleId);

	    setDiscoveryAction(ruleId);
	    setUnapplicableRule(ruleId);

	    setOrCondition(//
		    ruleId, //
		    XACML_JAXBUtils.createNotApply(createAccessPathApply()), // unsupported path
		    XACML_JAXBUtils.createNotApply(createViewCreatorApply(GEOSS_VIEW_CREATOR)), // not geoss view
												// creator
		    XACML_JAXBUtils.createNotApply(createOriginHeaderApply(GWP_ORIGINS_HEADER)) // not geoss client
												// (redundant check)
	    ); //
	}

	//
	// allowed access rule
	//
	{
	    String ruleId = "gwp:allowed:permission:to:access";

	    setAccessAction(ruleId);

	    setAndCondition(//
		    ruleId, //
		    createAccessPathApply(), //
		    createViewCreatorApply(GEOSS_VIEW_CREATOR), //
		    createOriginHeaderApply(GWP_ORIGINS_HEADER));//
	}
    }
}
