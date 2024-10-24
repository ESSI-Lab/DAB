/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.ow2.authzforce.core.pdp.impl.PdpExtensions;
import org.ow2.authzforce.core.pdp.impl.PdpModelHandler;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefBasedRootPolicyProvider;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefPolicyProvider;

import eu.essi_lab.authorization.PdpEngineBuilder;
import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.authzforce.ext.IdListPolicyProvider;
import eu.essi_lab.authorization.psloader.PolicySetLoaderFactory;
import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class PDP_JAXB_Test extends XACMLTest {

    @Test
    public void test1() throws JAXBException {

	Pdp pdp = new Pdp();

	pdp.setVersion("6.0.0");

	StaticRefBasedRootPolicyProvider staticRefBasedRootPolicyProvider = new StaticRefBasedRootPolicyProvider();
	staticRefBasedRootPolicyProvider.setId("rootPolicyProvider");
	staticRefBasedRootPolicyProvider.setPolicyRef(new IdReferenceType("root", null, null, null));

	pdp.setRootPolicyProvider(staticRefBasedRootPolicyProvider);

	StaticRefPolicyProvider staticRefPolicyProvider = new StaticRefPolicyProvider();

	staticRefPolicyProvider.setId("refPolicyProvider");

	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\rbac-root-policy-set.xml");

	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\kma-pps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\admin-pps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\default-pps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\limited-pps.xml");

	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\kma-rps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\admin-rps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\default-rps.xml");
	staticRefPolicyProvider.getPolicyLocations()
		.add("F:\\GIT\\GI-project\\authorization\\src\\test\\resources\\essi-rbac-test\\limited-rps.xml");

	pdp.setRefPolicyProvider(staticRefPolicyProvider);

	PdpModelHandler pdpModelHandler = new PdpModelHandler(null, null);

	pdpModelHandler.marshal(pdp, System.out);
    }

    @Test
    public void test2() throws JAXBException {

	List<Class> asList = new ArrayList<>(Arrays.asList(PdpExtensions.getExtensionJaxbClasses().toArray(new Class[] {})));
	asList.add(IdListPolicyProvider.class);
	asList.add(Pdp.class);

	JAXBContext newInstance = JAXBContext.newInstance(asList.toArray(new Class[] {}));

	Pdp pdp = new Pdp();

	pdp.setVersion("6.0.0");

	StaticRefBasedRootPolicyProvider staticRefBasedRootPolicyProvider = new StaticRefBasedRootPolicyProvider();
	staticRefBasedRootPolicyProvider.setId("rootPolicyProvider");
	staticRefBasedRootPolicyProvider.setPolicyRef(new IdReferenceType(PdpEngineBuilder.ROOT_POLICY_SET_ID, null, null, null));

	// set the root policy provider
	pdp.setRootPolicyProvider(staticRefBasedRootPolicyProvider);

	IdListPolicyProvider idListPolicyProvider = new IdListPolicyProvider(createRootPolicySet());

	// adds the PPS
	for (PolicySetWrapper pps : PolicySetLoaderFactory.createPolicySetLoader().loadPermissionPolicySets()) {

	    idListPolicyProvider.getIdentifiers().add(pps.getPolicySet().getPolicySetId());
	}

	// adds the RPS
	for (PolicySetWrapper rps : PolicySetLoaderFactory.createPolicySetLoader().loadRolePolicySets()) {

	    idListPolicyProvider.getIdentifiers().add(rps.getPolicySet().getPolicySetId());
	}

	// set the ref policy provider
	pdp.setRefPolicyProvider(idListPolicyProvider);

	Marshaller marshaller = newInstance.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	marshaller.marshal(pdp, System.out);
    }

    private PolicySet createRootPolicySet() {

	ArrayList<Serializable> wrapperList = new ArrayList<>();

	for (PolicySetWrapper rps : PolicySetLoaderFactory.createPolicySetLoader().loadRolePolicySets()) {

	    String policySetId = rps.getPolicySet().getPolicySetId();
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
		PdpEngineBuilder.ROOT_POLICY_SET_ID, //
		rootList, //
		StandardCombiningAlgorithm.XACML_3_0_POLICY_COMBINING_DENY_OVERRIDES.getId());
    }
}
