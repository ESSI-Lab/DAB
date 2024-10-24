package eu.essi_lab.accessor.usgswatersrv.codes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class USGSCountyExternalTestIT {

    @Test
    public void test() {
	HashMap<String, String> codes = USGSCounty.getInstance().getProperties("01007");
	String name = codes.get("county_nm");
	assertEquals("Bibb County", name);
    }

}
