package eu.essi_lab.bufr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.bufr.datamodel.BUFRElement;
import eu.essi_lab.bufr.datamodel.BUFRRecord;

public class BUFRReaderTest {

    private File tmp;

    @Before
    public void before() throws IOException {

	String str = "Bolivia/A_IS_MI_01_SLLP_071200-xxx_C_SBBR_20191107T16_0011_792.bfr";

	str = "Brazil/A_IS_AI_01_SBBR_010000-RRA_C_SBBR_20191107T15_5822_204.bfr";

	str = "Argentina/A_IS_II_01_SABM_070300-RRA_C_SBBR_20191107T16_1703_692.bfr";

	str = "Paraguay/A_IS_II_01_SGAS_050300-xxx_C_SBBR_20191107T15_5934_586.bfr";

	InputStream stream = BUFRReaderTest.class.getClassLoader().getResourceAsStream(str);

	this.tmp = File.createTempFile(getClass().getSimpleName(), ".bfr");
	this.tmp.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
    }

    @Test
    public void testName() throws Exception {

	BUFRReader reader = new BUFRReader();
	List<BUFRRecord> records = reader.extractRecords(tmp.getAbsolutePath());
	int i = 0;
	for (BUFRRecord record : records) {
	    System.out.println("Record #" + i++);
	    // record.print();

	    List<BUFRElement> ves = record.identifyVariables();
	    for (BUFRElement ve : ves) {
		System.out.println(ve.getName());
	    }

	    record.marshal(System.out);
	}

    }

    @After
    public void after() {
	tmp.delete();
    }

}
