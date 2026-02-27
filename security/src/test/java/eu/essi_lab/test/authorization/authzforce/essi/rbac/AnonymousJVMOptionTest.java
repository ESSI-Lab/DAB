package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.messages.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class AnonymousJVMOptionTest {

    /**
     *
     */
    @Test
    public void javaOptionsTest1() {

	synchronized (AnonymousJVMOptionTest.class) {

	    Assert.assertEquals(200, AbstractPermissionPolicySet.ANONYMOUS_PAGE_SIZE_LIMIT);

	    Assert.assertEquals(Integer.MAX_VALUE, AbstractPermissionPolicySet.ANONYMOUS_OFFSET_LIMIT);

	}
    }


}
