package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.messages.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class AnonymousJVMOptionTest {

    static {

	System.setProperty(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT.getOption(), "3");
	System.setProperty(JVMOption.ANONYMOUS_OFFSET_LIMIT.getOption(), "-1");
    }

    /**
     *
     */
    @Test
    public void javaOptionsTest() {

	Assert.assertEquals(3, AbstractPermissionPolicySet.ANONYMOUS_PAGE_SIZE_LIMIT);

	// the declared value -1 is mapped to Integer.MAX_VALUE
	Assert.assertEquals(Integer.MAX_VALUE, AbstractPermissionPolicySet.ANONYMOUS_OFFSET_LIMIT);
    }
}
