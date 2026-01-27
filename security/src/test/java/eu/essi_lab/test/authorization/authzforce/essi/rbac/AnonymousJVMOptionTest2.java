package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.messages.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class AnonymousJVMOptionTest2 {

    static {

	System.setProperty(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT.getOption(), "3");
	System.setProperty(JVMOption.ANONYMOUS_OFFSET_LIMIT.getOption(), "100");
    }

    /**
     *
     */
    @Test
    public void javaOptionsTest() {

	Assert.assertEquals(3, AbstractPermissionPolicySet.ANONYMOUS_PAGE_SIZE_LIMIT);

	Assert.assertEquals(100, AbstractPermissionPolicySet.ANONYMOUS_OFFSET_LIMIT);
    }
}
