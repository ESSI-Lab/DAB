/**
 *
 */
package eu.essi_lab.authorization;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.authorization.builder.*;
import eu.essi_lab.authorization.psloader.*;
import eu.essi_lab.authorization.xacml.*;
import jakarta.xml.bind.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.impl.*;
import org.ow2.authzforce.core.pdp.impl.combining.*;

import javax.xml.namespace.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class PdpEngineBuilder {

    /**
     *
     */
    public static final String ROOT_POLICY_SET_ID = "root";

    private List<PolicySetWrapper> rpsList;
    private List<PolicySetWrapper> ppsList;

    /**
     *
     */
    public PdpEngineBuilder() {

	rpsList = new ArrayList<PolicySetWrapper>();
	ppsList = new ArrayList<PolicySetWrapper>();
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

	rpsList.add(rps);
	ppsList.add(pps);
    }

    /**
     * @return
     * @throws Exception
     */
    public CloseablePdpEngine build() throws Exception {

	// GSLoggerFactory.getLogger(getClass()).debug("Engine building STARTED");

	//
	// 1 ---
	//

	//	Pdp pdp = createPdp();

	File pdpFile = createPDPFile(createRootPolicySet(), ppsList, rpsList);

	PdpEngineConfiguration conf = PdpEngineConfiguration.getInstance(pdpFile.getAbsolutePath(), null, null);

	//
	// 2 ---
	//

	//	PdpEngineConfiguration pdpEngineConf = new PdpEngineConfiguration(pdp, new DefaultEnvironmentProperties());

	BasePdpEngine engine = new BasePdpEngine(conf);

	// GSLoggerFactory.getLogger(getClass()).debug("Engine building ENDED");

	return engine;
    }

    /**
     * @param root
     * @param ppsList
     * @param rpsList
     * @return
     * @throws Exception
     */
    private File createPDPFile(PolicySet root, List<PolicySetWrapper> ppsList, List<PolicySetWrapper> rpsList) throws Exception {

	Path baseDir = Files.createTempDirectory("authzforce-pdp-");
	//	Path policiesDir = Files.createDirectory(baseDir.resolve("policies"));

	JAXBContext ctx = JAXBContext.newInstance(PolicySet.class);

	Marshaller marshaller = ctx.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	BiConsumer<PolicySet, Path> writePolicy = (policySet, path) -> {

	    try {

		QName qName = new QName("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "PolicySet");

		JAXBElement<PolicySet> element = new JAXBElement(qName, PolicySet.class, null, policySet);

		marshaller.marshal(element, path.toFile());

	    } catch (Exception e) {

		throw new RuntimeException(e);
	    }
	};

	String rootFileName = root.getPolicySetId() + ".xml";
	writePolicy.accept(root, baseDir.resolve(rootFileName));

	//	for (PolicySet pps : ppsList) {
	//	    String fileName = pps.getPolicySetId().replace(":", "_") + ".xml";
	//	    writePolicy.accept(pps, baseDir.resolve(fileName));
	//	}
	//
	//	for (PolicySet rps : rpsList) {
	//	    String fileName = rps.getPolicySetId().replace(":", "_") + ".xml";
	//	    writePolicy.accept(rps, baseDir.resolve(fileName));
	//	}

	String pdpXml = """
		<pdp xmlns="http://authzforce.github.io/core/xmlns/pdp/8"
		     version="8.0"
		     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		
		     <policyProvider id="fileBased" xsi:type="StaticPolicyProvider">
		
		          <policyLocation>%s</policyLocation>
		
		     </policyProvider>
		
		  <rootPolicyRef>%s</rootPolicyRef>
		</pdp>
		""".formatted(baseDir + "/root.xml", root.getPolicySetId());

	Path pdpFile = baseDir.resolve("pdp.xml");

	Files.writeString(pdpFile, pdpXml, StandardCharsets.UTF_8);

	return pdpFile.toFile();
    }

    /**
     * Creates the pdp which references to the root policy set and to all the PPS and RPS
     *
     * @param rootPolicySetFile
     * @return
     * @throws Exception
     */
    //    private Pdp createPdp() throws Exception {
    //
    //	Path confFile = Paths.get("pdp.xml");
    //
    //	PdpEngineConfiguration conf = PdpEngineConfiguration.getInstance(confFile.toFile(), null, null);
    //
    //	PdpEngine pdpEngine = new BasePdpEngine(conf);
    //
    //	Pdp pdp = new Pdp();
    //
    //	pdp.setVersion("6.0.0");
    //
    //	StaticPolicyProvider staticRefBasedRootPolicyProvider = new StaticPolicyProvider();
    //	staticRefBasedRootPolicyProvider.setId("rootPolicyProvider");
    //	//	staticRefBasedRootPolicyProvider.setPolicyRef(new IdReferenceType(ROOT_POLICY_SET_ID, null, null, null));
    //
    //	// set the root policy provider
    //	pdp.setRootPolicyRef(staticRefBasedRootPolicyProvider);
    //
    //	IdListPolicyProvider idListPolicyProvider = new IdListPolicyProvider(createRootPolicySet());
    //
    //	// adds the PPS
    //	for (PolicySet pps : ppsList) {
    //
    //	    idListPolicyProvider.getIdentifiers().add(pps.getPolicySetId());
    //	}
    //
    //	// adds the RPS
    //	for (PolicySet rps : rpsList) {
    //
    //	    idListPolicyProvider.getIdentifiers().add(rps.getPolicySetId());
    //	}
    //
    //	// set the ref policy provider
    //	pdp.setPolicyProvider(idListPolicyProvider);
    //
    //	return pdp;
    //    }

    /**
     * Creates the root policy set with the wrapper policy set inside. The wrapper policy set refer only to the RPS
     *
     * @param rpsList
     * @return
     */
    private PolicySet createRootPolicySet() {

	ArrayList<Serializable> wrapperList = new ArrayList<>();

	for (PolicySetWrapper pps : ppsList) {

	    PolicySet build = RPSBuilder.build(pps.getRole(), pps.getPolicy().get());

	    //	    String policySetId = rps.getPolicySetId();
	    //	    IdReferenceType idReferenceType = new IdReferenceType(policySetId, null, null, null);

	    //	    JAXBElement<IdReferenceType> ref = ObjectFactories.XACML().createPolicySetIdReference(idReferenceType);

	    wrapperList.add(build);

	}

	//	for (PolicySet rps : ppsList) {
	//
	//	    String policySetId = rps.getPolicySetId();
	//	    IdReferenceType idReferenceType = new IdReferenceType(policySetId, null, null, null);
	//
	//	    	    JAXBElement<IdReferenceType> ref = ObjectFactories.XACML().createPolicySetIdReference(idReferenceType);
	//
	//	    wrapperList.add(ref);
	//
	//	}

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
