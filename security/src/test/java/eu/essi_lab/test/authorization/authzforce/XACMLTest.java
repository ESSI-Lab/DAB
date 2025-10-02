/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import eu.essi_lab.authorization.PdpEngineBuilder;
import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.authzforce.ext.IdListRefPolicyProvider;
import eu.essi_lab.authorization.pps.AdminPermissionPolicySet;
import eu.essi_lab.authorization.pps.AnonymousPermissionPolicySet;
import eu.essi_lab.authorization.pps.CSW_RIPermissionPolicySet;
import eu.essi_lab.authorization.pps.EIFFELPermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSPrivateWritePermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSReadPermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.authorization.pps.GWPPermissionPolicySet;
import eu.essi_lab.authorization.pps.HISCentralPermissionPolicySet;
import eu.essi_lab.authorization.pps.HISCentralTestPermissionPolicySet;
import eu.essi_lab.authorization.pps.KMAPermissionPolicySet;
import eu.essi_lab.authorization.pps.LODGEOSSPermissionPolicySet;
import eu.essi_lab.authorization.pps.SSCPermissionPolicySet;
import eu.essi_lab.authorization.pps.SeadatanetPermissionPolicySet;
import eu.essi_lab.authorization.pps.WHOSPermissionPolicySet;
import eu.essi_lab.authorization.psloader.PolicySetLoader;
import eu.essi_lab.authorization.rps.AdminRolePolicySet;
import eu.essi_lab.authorization.rps.AnonymousRolePolicySet;
import eu.essi_lab.authorization.rps.CSW_RIRolePolicySet;
import eu.essi_lab.authorization.rps.EIFFELRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GWPRolePolicySet;
import eu.essi_lab.authorization.rps.HISCentralRolePolicySet;
import eu.essi_lab.authorization.rps.HISCentralTestRolePolicySet;
import eu.essi_lab.authorization.rps.KMARolePolicySet;
import eu.essi_lab.authorization.rps.LODGEOSSRolePolicySet;
import eu.essi_lab.authorization.rps.SSCRolePolicySet;
import eu.essi_lab.authorization.rps.SeadatanetRolePolicySet;
import eu.essi_lab.authorization.rps.WHOSRolePolicySet;
import eu.essi_lab.authorization.xacml.PdpEngineWrapper;
import eu.essi_lab.test.authorization.authzforce.essi.policies.DefaultPermissionPolicySet;
import eu.essi_lab.test.authorization.authzforce.essi.policies.DefaultRolePolicySet;
import eu.essi_lab.test.authorization.authzforce.essi.policies.LimitedPermissionPolicySet;
import eu.essi_lab.test.authorization.authzforce.essi.policies.LimitedRolePolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class XACMLTest {

    static {

	//
	// the file access is required for local schemas while http is required in particular
	// for the authzforce XACML data model
	//
	System.setProperty("javax.xml.accessExternalSchema", "all");
    }

    protected PdpEngineWrapper wrapper;

    @Before
    public void init() throws Exception {

	PolicySetLoader loader = new PolicySetLoader() {

	    @Override
	    public List<PolicySetWrapper> loadRolePolicySets() {

		return Arrays.asList(//
			new DefaultRolePolicySet(), //
			new LimitedRolePolicySet(), //
			new KMARolePolicySet(), //
			new GWPRolePolicySet(), //
			new AdminRolePolicySet(), //
			new AnonymousRolePolicySet(), //
			new WHOSRolePolicySet(), //
			new CSW_RIRolePolicySet(), //
			new EIFFELRolePolicySet(), //
			new HISCentralRolePolicySet(), //
			new HISCentralTestRolePolicySet(), //
			new LODGEOSSRolePolicySet(), //
			new WHOSRolePolicySet(), //
			new SSCRolePolicySet(), //
			new SeadatanetRolePolicySet(), //
			new GEOSSReadRolePolicySet(), //
			new GEOSSPrivateWriteRolePolicySet(), //
			new GEOSSWriteRolePolicySet());
	    }

	    @Override
	    public List<PolicySetWrapper> loadPermissionPolicySets() {

		return Arrays.asList(//
			new DefaultPermissionPolicySet(), //
			new LimitedPermissionPolicySet(), //
			new AnonymousPermissionPolicySet(), //
			new AdminPermissionPolicySet(), //
			new CSW_RIPermissionPolicySet(), //
			new EIFFELPermissionPolicySet(), //
			new GWPPermissionPolicySet(), //
			new HISCentralPermissionPolicySet(), //
			new HISCentralTestPermissionPolicySet(), //
			new KMAPermissionPolicySet(), //
			new LODGEOSSPermissionPolicySet(), //
			new SeadatanetPermissionPolicySet(), //
			new SSCPermissionPolicySet(), //
			new WHOSPermissionPolicySet(), //
			new GEOSSReadPermissionPolicySet(), //
			new GEOSSPrivateWritePermissionPolicySet(), //
			new GEOSSWritePermissionPolicySet());
	    }

	    @Override
	    public void setPermissionPolicySet(PolicySetWrapper geossReadPPS) {
	    }
	};

	IdListRefPolicyProvider.setPolicySetLoader(loader);

	PdpEngineBuilder builder = new PdpEngineBuilder();

	builder.addPolicies(loader);

	wrapper = new PdpEngineWrapper(builder.build());
    }

    /**
     * @param expectedType
     */
    protected void evaluate(DecisionType expectedType) {

	DecisionType decision = wrapper.evaluate();

	System.out.println(decision);

	Assert.assertEquals(expectedType, decision);

    }

    @After
    public void close() throws IOException {

	wrapper.close();
    }
}
