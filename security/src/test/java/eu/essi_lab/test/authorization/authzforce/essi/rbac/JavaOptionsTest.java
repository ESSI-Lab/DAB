package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.messages.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class JavaOptionsTest {

    static {

	System.setProperty(JavaOptions.ANONYMOUS_PAGE_SIZE_LIMIT.getOption(), "3");
	System.setProperty(JavaOptions.ANONYMOUS_OFFSET_LIMIT.getOption(), "-1");
    }

    /**
     *
     */
    @Test
    public void javaOptionsTest() {

	Assert.assertEquals(3, AbstractPermissionPolicySet.ANONYMOUS_PAGE_SIZE_LIMIT);
	Assert.assertEquals(Integer.MAX_VALUE, AbstractPermissionPolicySet.ANONYMOUS_OFFSET_LIMIT);
    }
}
