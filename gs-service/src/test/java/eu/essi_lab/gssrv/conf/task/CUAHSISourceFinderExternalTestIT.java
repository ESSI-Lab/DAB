package eu.essi_lab.gssrv.conf.task;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.gssrv.conf.task.CUAHSISourceFinder;

public class CUAHSISourceFinderExternalTestIT {

    @Test
    public void test() {
	CUAHSISourceFinder finder = new CUAHSISourceFinder();
	List<HarvestingSetting> sources = finder.getSources("https://hiscentral.cuahsi.org/webservices/hiscentral.asmx", "cuahsi-hc");
	assertTrue(sources.size() > 10);
	for (HarvestingSetting source : sources) {
	    // System.out.println(source.getIdentifier() + " " + source.getName());
	    System.out.print("\"" + source.getSelectedAccessorSetting().getGSSourceSetting().getSourceIdentifier() + "\",");
	}
	System.out.println();

    }

}
