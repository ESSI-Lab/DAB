/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;

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
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class AnyOfAllFunctionTest extends XACMLTest{

    private DecisionRequestBuilder<?> requestBuilder;
    private BasePdpEngine pdp;

    @Before
    public void init() throws IllegalArgumentException, IOException {

	URL resource = AuthZForceTest.class.getClassLoader().getResource("any-of-all/pdp.xml");

	PdpEngineConfiguration pdpEngineConf = PdpEngineConfiguration.getInstance(resource.getFile());

	pdp = new BasePdpEngine(pdpEngineConf);

	requestBuilder = pdp.newRequestBuilder(-1, -1);
    }

    @Test
    public void testRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testReadWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("read"), new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testWriteRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("write"), new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testReadRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("read"), new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testWriteWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("write"), new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.PERMIT);
    }

    @Test
    public void testReadDelete() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("read"), new StringValue("delete")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testWriteDelete() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("write"), new StringValue("delete")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testDeleteRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("delete"), new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testDeleteWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("delete"), new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testReadDeleteWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("read"), new StringValue("delete"), new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testWriteDeleteRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("write"), new StringValue("delete"), new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testDeleteReadWrite() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("delete"), new StringValue("read"), new StringValue("write")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testDeleteWriteRead() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("delete"), new StringValue("write"), new StringValue("read")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @Test
    public void testDelete() {

	requestBuilder.reset();

	//
	// -----
	//

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		Arrays.asList(new StringValue("delete")));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);

	//
	// ---
	//

	DecisionRequest request = requestBuilder.build(false);

	System.out.println(request.toString());

	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == DecisionType.DENY);
    }

    @After
    public void close() throws IOException {

	pdp.close();
    }
}
