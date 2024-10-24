/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_RESOURCE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

/**
 * @author Fabrizio
 */
public class AuthForceXMLParserTest extends XACMLTest{

    @Test
    public void unmarshalTest1() throws JAXBException, IllegalArgumentException, MalformedURLException {

	XmlUtils.XmlnsFilteringParser xacmlParserFactory = XacmlJaxbParsingUtils.getXacmlParserFactory(false).getInstance();

	URL reqUrl = AuthForceXMLParserTest.class.getClassLoader().getResource("xml-requests/request1.xml");
	
	Request request = (Request) xacmlParserFactory.parse(reqUrl);

	Assert.assertTrue(request.isCombinedDecision());
    }
    
    @Test
    public void unmarshalTest2() throws JAXBException, IllegalArgumentException, MalformedURLException {

	URL reqUrl = AuthForceXMLParserTest.class.getClassLoader().getResource("xml-requests/request1.xml");

	Request request = (Request) Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(reqUrl);
	
	Assert.assertTrue(request.isCombinedDecision());
    }

    @Test
    public void marshalTest() throws IllegalArgumentException, IOException, JAXBException {

	URL resource = AuthForceXMLParserTest.class.getClassLoader().getResource("pdp.xml");

	PdpEngineConfiguration pdpEngineConf = PdpEngineConfiguration.getInstance(resource.getFile());

	BasePdpEngine pdp = new BasePdpEngine(pdpEngineConf);

	DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(-1, -1);

	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of("test1"), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("Julius Hibbert"));

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());

	AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("boss"));

	requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.ANYURI, //
		new AnyUriValue("http://medico.com/record/patient/BartSimpson"));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("read"));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	DecisionRequest request = requestBuilder.build(false);
	
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
//	 oasis.names.tc.xacml._3_0.core.schema.wd_17.Request r = new Request(requestDefaults, attributes, multiRequests, returnPolicyIdList, combinedDecision)
//	
//	
//	final Marshaller marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
//	marshaller.marshal(policy, outputStream);
//	
//	System.out.println(outputStream);
	
	pdp.close();

	

    }
}
