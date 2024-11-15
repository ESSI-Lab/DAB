package eu.essi_lab.gssrv.conf.task;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;

public class CUAHSISourceFinderExternalTestIT {

    @Test
    public void test() {
	CUAHSISourceFinder finder = new CUAHSISourceFinder();
	List<HarvestingSetting> sources = finder.getSources("https://hiscentral.cuahsi.org/webservices/hiscentral.asmx", "test");
	assertTrue(sources.size() > 10);
	for (HarvestingSetting source : sources) {
	    System.out.println(source.getIdentifier() + " " + source.getName());
	}

    }

}
