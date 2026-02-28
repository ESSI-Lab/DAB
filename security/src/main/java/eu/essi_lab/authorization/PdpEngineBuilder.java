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

import eu.essi_lab.authorization.psloader.*;
import eu.essi_lab.authorization.xacml.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.utils.*;
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

    private List<PolicySet> rpsList;
    private List<PolicySet> ppsList;
    private static BasePdpEngine basePdpEngine;

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

	List<String> roles = pps.stream().map(PolicySetWrapper::getRole).toList();

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

	return build(false);
    }

    /**
     * @param reinit
     * @return
     * @throws Exception
     */
    public CloseablePdpEngine build(boolean reinit) throws Exception {

	if (basePdpEngine == null || reinit) {

	    File pdpFile = createPDPFile(createRootPolicySet(), ppsList, rpsList);

	    PdpEngineConfiguration conf = PdpEngineConfiguration.getInstance(pdpFile.getAbsolutePath(), null, null);

	    if (reinit) {

		return new BasePdpEngine(conf);
	    }

	    basePdpEngine = new BasePdpEngine(conf);
	}

	return basePdpEngine;
    }

    /**
     * @param root
     * @param ppsList
     * @param rpsList
     * @return
     * @throws Exception
     */
    private File createPDPFile(PolicySet root, List<PolicySet> ppsList, List<PolicySet> rpsList) throws Exception {

	Path baseDir = FileUtils.getTempDir("authzforce-pdp", true).toPath();

	JAXBContext ctx = JAXBContext.newInstance(PolicySet.class);

	Marshaller marshaller = ctx.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	BiConsumer<Serializable, Path> writePolicySet = (policySet, path) -> {

	    try {

		QName qName = new QName("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "PolicySet");

		JAXBElement<PolicySet> element = new JAXBElement(qName, PolicySet.class, null, policySet);

		marshaller.marshal(element, path.toFile());

	    } catch (Exception e) {

		throw new RuntimeException(e);
	    }
	};

	String rootFileName = root.getPolicySetId() + ".xml";
	writePolicySet.accept(root, baseDir.resolve(rootFileName));

	List<String> locations = new ArrayList<>();
	locations.add(baseDir + "/root.xml");

	ArrayList<PolicySet> policySets = new ArrayList<>(ppsList);
	policySets.addAll(rpsList);

	for (PolicySet pps : policySets) {

	    String fileName = pps.getPolicySetId().replace(":", "_") + ".xml";

	    Path path = baseDir.resolve(fileName);
	    locations.add(path.toString());

	    writePolicySet.accept(pps, path);
	}

	String collect = locations.stream().map(path -> "<policyLocation>" + path + "</policyLocation>").collect(Collectors.joining("\n"));

	String pdpXml = """
		<pdp xmlns="http://authzforce.github.io/core/xmlns/pdp/8"
		     version="8.0"
		     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		
		     <policyProvider id="fileBased" xsi:type="StaticPolicyProvider">
		
		         %s
		
		     </policyProvider>
		
		  <rootPolicyRef>root</rootPolicyRef>
		</pdp>
		""".formatted(collect);

	Path pdpFile = baseDir.resolve("pdp.xml");

	Files.writeString(pdpFile, pdpXml, StandardCharsets.UTF_8);

	return pdpFile.toFile();
    }

    /**
     * Creates the root policy set
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
