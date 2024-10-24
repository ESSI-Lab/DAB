package eu.essi_lab.accessor.usgswatersrv.codes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class USGSStateExternalTestIT {

    @Test
    public void test() {
	HashMap<String, String> codes = USGSState.getInstance().getProperties("23");
	String name = codes.get("STATE_NAME");
	assertEquals("Maine", name);
    }

}
