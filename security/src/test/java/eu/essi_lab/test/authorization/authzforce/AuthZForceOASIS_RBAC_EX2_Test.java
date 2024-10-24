/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_RESOURCE;

import java.io.IOException;
import java.net.URL;
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
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class AuthZForceOASIS_RBAC_EX2_Test extends XACMLTest{

    private DecisionRequestBuilder<?> requestBuilder;
    private BasePdpEngine pdp;

    @Before
    public void init() throws IllegalArgumentException, IOException {

	URL resource = AuthZForceOASIS_RBAC_EX2_Test.class.getClassLoader().getResource("oasis-rbac-ex2/rbac-pdp.xml");

	PdpEngineConfiguration pdpEngineConf = PdpEngineConfiguration.getInstance(resource.getFile());

	pdp = new BasePdpEngine(pdpEngineConf);

	requestBuilder = pdp.newRequestBuilder(-1, -1);
    }

    /**
     * manager wants sign a purchase order. sign action is a manager role action
     *
     * @throws IOException
     */
    @Test
    public void managerSignTest() throws IOException {

	requestBuilder.reset();

	addManager();

	addAction("sign");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * manager wants delete a purchase order. delete is a manager action
     *
     * @throws IOException
     */
    @Test
    public void managerDeleteTest() throws IOException {

	requestBuilder.reset();

	addManager();

	addAction("delete");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * manager wants create a purchase order. create is an action inherited from the employee role
     *
     * @throws IOException
     */
    @Test
    public void managerCreateTest() throws IOException {

	requestBuilder.reset();

	addManager();

	addAction("create");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * manager wants read a purchase order. read is an action inherited from the employee role
     *
     * @throws IOException
     */
    @Test
    public void managerReadTest() throws IOException {

	requestBuilder.reset();

	addManager();

	addAction("read");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * manager wants destroy a purchase order. destroy is an undefined action
     *
     * @throws IOException
     */
    @Test
    public void managerDestroyTest() throws IOException {

	requestBuilder.reset();

	addManager();

	addAction("destroy");

	addPurchaseOrderResource();

	evaluate(DecisionType.DENY);
    }

    /**
     * employee wants read a purchase order. read is an employee role action
     * 
     * @throws IOException
     */
    @Test
    public void employeeReadTest() throws IOException {

	requestBuilder.reset();

	addEmployee();

	addAction("read");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * employee wants create a purchase order. create is an employee role action
     * 
     * @throws IOException
     */
    @Test
    public void employeeCreateTest() throws IOException {

	requestBuilder.reset();

	addEmployee();

	addAction("create");

	addPurchaseOrderResource();

	evaluate(DecisionType.PERMIT);
    }

    /**
     * employee wants sign a purchase order. sign is a manager role action
     * 
     * @throws IOException
     */
    @Test
    public void employeeSignTest() throws IOException {

	requestBuilder.reset();

	addEmployee();

	addAction("sign");

	addPurchaseOrderResource();

	evaluate(DecisionType.DENY);
    }

    /**
     * employee wants delete a purchase order. delete is a manager role action
     * 
     * @throws IOException
     */
    @Test
    public void employeeDeleteTest() throws IOException {

	requestBuilder.reset();

	addEmployee();

	addAction("delete");

	addPurchaseOrderResource();

	evaluate(DecisionType.DENY);
    }
    
    /**
     * employee wants destroy a purchase order. destroy is an undefined action
     *
     * @throws IOException
     */
    @Test
    public void  employeeDestroyTest() throws IOException {

	requestBuilder.reset();

	addEmployee();

	addAction("destroy");

	addPurchaseOrderResource();

	evaluate(DecisionType.DENY);
    }

    /**
     * 
     */
    private void addManager() {

	AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());

	AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.ANYURI, //
		new AnyUriValue("roles;manager"));

	requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);
    }

    /**
     * 
     */
    private void addEmployee() {

	AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());

	AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.ANYURI, //
		new AnyUriValue("roles;employee"));

	requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);
    }

    /**
     * @param action
     */
    private void addAction(String action) {

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue(action));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);
    }

    /**
     * 
     */
    private void addPurchaseOrderResource() {

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue("purchase order"));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);
    }

    /**
     * /**
     * 
     * @param expectedType
     */
    protected void evaluate(DecisionType expectedType) {

	//
	// request creation
	//
	DecisionRequest request = requestBuilder.build(false);

	//
	// Evaluate the request
	//
	DecisionResult result = pdp.evaluate(request);

	DecisionType decision = result.getDecision();

	System.out.println(decision);

	Assert.assertTrue(decision == expectedType);

    }

    @After
    public void close() throws IOException {

	pdp.close();
    }
}
