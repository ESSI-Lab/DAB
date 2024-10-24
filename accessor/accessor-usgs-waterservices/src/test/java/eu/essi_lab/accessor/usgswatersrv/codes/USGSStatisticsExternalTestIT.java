package eu.essi_lab.accessor.usgswatersrv.codes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class USGSStatisticsExternalTestIT {

    @Test
    public void test() {
	HashMap<String, String> codes = USGSStatistics.getInstance().getProperties("00003");
	String name = codes.get("stat_NM");
	assertEquals("MEAN", name);
    }

}
