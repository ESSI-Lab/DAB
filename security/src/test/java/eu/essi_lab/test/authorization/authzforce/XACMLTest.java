/**
 *
 */
package eu.essi_lab.test.authorization.authzforce;

import eu.essi_lab.authorization.*;
import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.authorization.psloader.*;
import eu.essi_lab.authorization.rps.*;
import eu.essi_lab.authorization.xacml.*;
import eu.essi_lab.test.authorization.authzforce.essi.policies.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.junit.*;

import java.io.*;
import java.util.*;

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
