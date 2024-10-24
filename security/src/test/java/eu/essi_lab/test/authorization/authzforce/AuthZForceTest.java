/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_RESOURCE;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.MediumInteger;
import org.ow2.authzforce.core.pdp.api.value.Rfc822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class AuthZForceTest extends XACMLTest{

    private DecisionRequestBuilder<?> requestBuilder;
    private BasePdpEngine pdp;

    @Before
    public void init() throws IllegalArgumentException, IOException {

	URL resource = AuthZForceTest.class.getClassLoader().getResource("pdp.xml");

	PdpEngineConfiguration pdpEngineConf = PdpEngineConfiguration.getInstance(resource.getFile());

	pdp = new BasePdpEngine(pdpEngineConf);

	requestBuilder = pdp.newRequestBuilder(-1, -1);
    }

    @Test
    public void test1() throws IOException {

	//
	// Add subject ID attribute (access-subject category), no issuer, string value "john"
	//
	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of("test1"), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("Julius Hibbert"));

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	//
	// Add subject role(s) attribute to access-subject category, no issuer, string value "boss"
	//
	// AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
	// XACML_1_0_ACCESS_SUBJECT.value(), //
	// Optional.empty(), //
	// XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
	//
	// AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(//
	// StandardDatatypes.STRING, //
	// new StringValue("boss"));
	//
	// requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);
	//
	//
	//
	// ------------------------------
	//
	//

	// Add resource ID attribute (resource category), no issuer, string value "/some/resource/location"
	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.ANYURI, //
		new AnyUriValue("http://medico.com/record/patient/BartSimpson"));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);

	// Add action ID attribute (action category), no issuer, string value "GET"
	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("read"));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// No more attribute, let's size the request creation
	//
	DecisionRequest request = requestBuilder.build(false);

	// System.out.println(request.toString());

	//
	// Evaluate the request
	//
	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void test1_1() {

	{

	    AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		    XACML_1_0_ACCESS_SUBJECT.value(), //
		    Optional.of("test1.1"), //
		    XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	    AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		    StandardDatatypes.INTEGER, //
		    new IntegerValue(new MediumInteger(51)));

	    requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	    //
	    // ---
	    //
	    DecisionRequest request = requestBuilder.build(false);

	    // System.out.println(request.toString());

	    DecisionResult result = pdp.evaluate(request);

	    DecisionType decision = result.getDecision();

	    System.out.println(decision);

	    Assert.assertTrue(decision == DecisionType.PERMIT);
	}

	{

	    AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		    XACML_1_0_ACCESS_SUBJECT.value(), //
		    Optional.of("test1.1"), //
		    XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	    AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		    StandardDatatypes.INTEGER, //
		    new IntegerValue(new MediumInteger(49)));
	    
	    //
	    // reset the request
	    //
	    requestBuilder.reset();
	    
	    requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	    //
	    // ---
	    //
	    DecisionRequest request = requestBuilder.build(false);

	    // System.out.println(request.toString());

	    DecisionResult result = pdp.evaluate(request);

	    DecisionType decision = result.getDecision();

	    System.out.println(decision);

	    Assert.assertTrue(decision == DecisionType.DENY);
	}
    }

    @Test
    public void test2() {

	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of("test2"), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	AttributeBag<?> subjectIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.RFC822NAME, //
		Arrays.asList(new Rfc822NameValue("pippo@med.example.com")));

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	//
	// the action is allowed since everything is allowed
	//
	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("read"));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//
	DecisionRequest request = requestBuilder.build(false);

	// System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void test3() {

	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of("test3"), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.INTEGER, //
		new IntegerValue(new MediumInteger(34)));

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);

	//
	// ---
	//
	DecisionRequest request = requestBuilder.build(false);

	// System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @After
    public void close() throws IOException {

	pdp.close();
    }
}
