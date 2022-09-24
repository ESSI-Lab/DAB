/**
 * 
 */
package eu.essi_lab.authorization;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.DefaultEnvironmentProperties;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefBasedRootPolicyProvider;

import eu.essi_lab.authorization.authzforce.ext.IdListPolicyProvider;
import eu.essi_lab.authorization.psloader.PolicySetLoader;
import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class PdpEngineBuilder {

    /**
     * 
     */
    public static final String ROOT_POLICY_SET_ID = "root";

    private List<PolicySet> rpsList;
    private List<PolicySet> ppsList;

    /**
     * 
     */
    public PdpEngineBuilder() {

	rpsList = new ArrayList<PolicySet>();
	ppsList = new ArrayList<PolicySet>();
    }
    
    /**
     * Adds all the policies loaded by the given <code>loader</code>
     * 
     * @param loader
     */
    public void addPolicies(PolicySetLoader loader) {
	
	List<PolicySetWrapper> rps = loader.loadRolePolicySets();
	List<PolicySetWrapper> pps = loader.loadPermissionPolicySets();

	List<String> roles = pps.stream().map(p -> p.getRole()).collect(Collectors.toList());

	for (String role : roles) {

	    addPolicies(//
		    rps.stream().filter(p -> p.getRole().equals(role)).findFirst().get(), //
		    pps.stream().filter(p -> p.getRole().equals(role)).findFirst().get());
	}
    }

    /**
     * Adds the specified RPS and related PPS
     * 
     * @param rps a role policy set to add
     * @param pps a permission policy set to add
     */
    public void addPolicies(PolicySetWrapper rps, PolicySetWrapper pps) {

	rpsList.add(rps.getPolicySet());
	ppsList.add(pps.getPolicySet());
    }

    /**
     * @return
     * @throws Exception
     */
    public CloseablePdpEngine build() throws Exception {

//	GSLoggerFactory.getLogger(getClass()).debug("Engine building STARTED");

	//
	// 1 ---
	//

	Pdp pdp = createPdp();

	//
	// 2 ---
	//

	PdpEngineConfiguration pdpEngineConf = new PdpEngineConfiguration(pdp, new DefaultEnvironmentProperties());

	BasePdpEngine engine = new BasePdpEngine(pdpEngineConf);

//	GSLoggerFactory.getLogger(getClass()).debug("Engine building ENDED");

	return engine;
    }

    /**
     * Creates the pdp which references to the root policy set and to all the PPS and RPS
     * 
     * @param rootPolicySetFile
     * @return
     * @throws Exception
     */
    private Pdp createPdp() throws Exception {

	Pdp pdp = new Pdp();

	pdp.setVersion("6.0.0");

	StaticRefBasedRootPolicyProvider staticRefBasedRootPolicyProvider = new StaticRefBasedRootPolicyProvider();
	staticRefBasedRootPolicyProvider.setId("rootPolicyProvider");
	staticRefBasedRootPolicyProvider.setPolicyRef(new IdReferenceType(ROOT_POLICY_SET_ID, null, null, null));

	// set the root policy provider
	pdp.setRootPolicyProvider(staticRefBasedRootPolicyProvider);

	IdListPolicyProvider idListPolicyProvider = new IdListPolicyProvider(createRootPolicySet());

	// adds the PPS
	for (PolicySet pps : ppsList) {

	    idListPolicyProvider.getIdentifiers().add(pps.getPolicySetId());
	}

	// adds the RPS
	for (PolicySet rps : rpsList) {

	    idListPolicyProvider.getIdentifiers().add(rps.getPolicySetId());
	}

	// set the ref policy provider
	pdp.setRefPolicyProvider(idListPolicyProvider);

	return pdp;
    }

    /**
     * Creates the root policy set with the wrapper policy set inside. The wrapper policy set refer only to the RPS
     * 
     * @param rpsList
     * @return
     */
    private PolicySet createRootPolicySet() {

	ArrayList<Serializable> wrapperList = new ArrayList<>();

	for (PolicySet rps : rpsList) {

	    String policySetId = rps.getPolicySetId();
	    IdReferenceType idReferenceType = new IdReferenceType(policySetId, null, null, null);

	    JAXBElement<IdReferenceType> ref = ObjectFactories.XACML().createPolicySetIdReference(idReferenceType);

	    wrapperList.add(ref);
	}

	PolicySet wrapperPolicySet = XACML_JAXBUtils.createPolicySet(//
		"WrapperSet", //
		wrapperList, //
		StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_UNLESS_PERMIT.getId());

	ArrayList<Serializable> rootList = new ArrayList<>();
	rootList.add(wrapperPolicySet);

	return XACML_JAXBUtils.createPolicySet(//
		ROOT_POLICY_SET_ID, //
		rootList, //
		StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_OVERRIDES.getId());
    }
}
