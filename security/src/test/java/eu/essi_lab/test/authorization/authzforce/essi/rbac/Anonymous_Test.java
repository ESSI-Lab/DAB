/**
 *
 */
package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import eu.essi_lab.authorization.*;
import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.test.authorization.authzforce.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.junit.*;

import java.io.*;

import static eu.essi_lab.authorization.pps.AbstractPermissionPolicySet.*;

/**
 * Anonymous users are allowed to discovery and access if and only if: 1) offset is <= 200 - 2) max records <= 50 - - 4) the discovery path
 * is one of: "opensearch", "bnhs", "thredds", "csw", "cswisogeo", "oaipmh", "hiscentral.asmx", "arpa-rest", "cuahsi_1_1.asmx", "rest",
 * "hydrocsv", "sos", "wfs", "gwis", "ArcGIS", "ArcGISProxy", "gwps", "timeseries-api", "semantic" - 5) the access path is one of:
 * "cuahsi_1_1.asmx", "rest", "hydrocsv", "thredds", "wms", "wfs", "sos", "gwps", "ArcGIS", "ArcGISProxy", "timeseries-api", "gwis" - OR -
 * 1) the client identifier {@link WebRequest#ESSI_LAB_CLIENT_IDENTIFIER} is provided
 *
 * @author Fabrizio
 */
public class Anonymous_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole(BasicRole.ANONYMOUS.getRole());
    }

    /**
     *
     */
    @Test
    public void javaOptionsDefaultValuesTest() {

	Assert.assertEquals((int) JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get(),
		AbstractPermissionPolicySet.ANONYMOUS_OFFSET_LIMIT);

	Assert.assertEquals((int) JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get(),
		AbstractPermissionPolicySet.ANONYMOUS_PAGE_SIZE_LIMIT);
    }

    /**
     * Deny: max records > 50
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get() + 1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: offset > 200
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setOffset(JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get() + 1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: invalid path
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryDenyTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: view provided
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryDenyTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny: all invalid
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryDenyTest5() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: all invalid but ESSI-Lab client
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryPermitTestESSIClient1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setClientId(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit: all invalid but ESSI-Lab client
     *
     * @throws IOException
     */
    @Test
    public void anonymousOtherPermitTestESSIClient1() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setClientId(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryPermitTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousOtherDenyTest1() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("opensearch");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryPermitTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("oaipmh");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousOtherDenyTest2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("oaipmh");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousDiscoveryPermitTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("oaipmh");

	wrapper.setOffset(JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get());

	wrapper.setMaxRecords(JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get());

	wrapper.setViewIdentifier(VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousOtherDenyTest3() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("oaipmh");

	wrapper.setOffset(JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get());

	wrapper.setMaxRecords(JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get());

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    //
    //
    //

    /**
     * Deny: max records > 50
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessDenyTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get() + 1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: offset > 200
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessDenyTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setOffset(JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get() + 1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: invalid path
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessDenyTest3() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: view provided
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessDenyTest4() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny: all invalid
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessDenyTest5() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: all invalid but ESSI-Lab client
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessPermitTestESSIClient1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setClientId(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit: all invalid but ESSI-Lab client
     *
     * @throws IOException
     */
    @Test
    public void anonymousOtherPermitTestESSIClient1_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setClientId(WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);

	wrapper.setPath("path");

	wrapper.setOffset(1000);

	wrapper.setMaxRecords(1000);

	wrapper.setViewCreator("creator");

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessPermitTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessPermitTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wfs");

	wrapper.setOffset(1);

	wrapper.setMaxRecords(1);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void anonymousAccessPermitTest3() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("timeseries-api");

	wrapper.setOffset(JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get());

	wrapper.setMaxRecords(JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get());

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.PERMIT);
    }

}
