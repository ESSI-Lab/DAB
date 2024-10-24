import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.dws.client.DWSData;
import eu.essi_lab.accessor.dws.client.DWSFlowData;
import eu.essi_lab.accessor.dws.client.DWSPrimaryData;
import eu.essi_lab.accessor.dws.client.DWSVolumeData;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class DWSDataParsingTest {
    @Test
    public void testFlow() throws Exception {
	InputStream is = DWSDataParsingTest.class.getResourceAsStream("flow-data.csv");
	DWSFlowData flowData = new DWSFlowData(is);
	List<DWSData> data = flowData.getData();
	System.out.println("\n\nflow data");
	testData(data);	
	for (DWSData d : data) {
	    System.out.println(d.getDate() + " " + d.getValue() + " " + d.getQualityCode());
	}
    }

    @Test
    public void testVolume() throws Exception {
	InputStream is = DWSDataParsingTest.class.getResourceAsStream("volume.csv");
	DWSVolumeData flowData = new DWSVolumeData(is);
	List<DWSData> data = flowData.getData();
	System.out.println("\n\nvolume data");
	for (DWSData d : data) {
	    System.out.println(d.getDate() + " " + d.getValue() + " " + d.getQualityCode());
	}
    }

    @Test
    public void testPrimary() throws Exception {
	InputStream is = DWSDataParsingTest.class.getResourceAsStream("primary.csv");
	DWSPrimaryData primaryData = new DWSPrimaryData(is);
	List<DWSData> data = primaryData.getLevelData();
	System.out.println("\n\nlevel data");
	testData(data);
	System.out.println("\n\ndischarge data");
	data = primaryData.getDischargeData();
	testData(data);
    }

    private void testData(List<DWSData> data) {
	assertTrue(data.size() > 10);
	for (DWSData d : data) {
	    System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(d.getDate()) + " " + d.getValue() + " " + d.getQualityCode());
	}

    }
}
