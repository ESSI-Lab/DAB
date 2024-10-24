package eu.essi_lab.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.accessor.imo.ZRXPBlock;
import eu.essi_lab.accessor.imo.ZRXPDocument;
import eu.essi_lab.accessor.imo.ZRXPKeyword;

public class ZRXPDocumentTest {
    @Test
    public void testName() throws Exception {
	InputStream stream = ZRXPDocumentTest.class.getClassLoader().getResourceAsStream("test-data/Arctic_hycos_grdc_Export.zrxp");
	File tmp = File.createTempFile(ZRXPDocumentTest.class.getSimpleName(), ".zrxp");
	tmp.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	ZRXPDocument doc = new ZRXPDocument(tmp);
	List<ZRXPBlock> blocks = doc.getBlocks();
	for (ZRXPBlock block : blocks) {
	    System.out.println(block);
	    System.out.println();
	}

	assertEquals(21, blocks.size());
	ZRXPBlock block0 = blocks.get(0);
	assertEquals("Reykjafoss", block0.getStationName());
	assertEquals((Integer) 2, block0.getStartHeadersLine());
	assertEquals((Integer) 7, block0.getStartDataLine());
	assertEquals("m³/s", block0.getUnit());
	SimpleEntry<Date, Double> dataFirst = block0.getDataValues().get(0);
	assertEquals(5.89, dataFirst.getValue(), 0.001);
	assertEquals(1573963200000l, dataFirst.getKey().getTime());
	SimpleEntry<Date, Double> dataLast = block0.getDataValues().get(block0.getDataValues().size() - 1);
	assertEquals(6.68, dataLast.getValue(), 0.001);
	assertEquals(1574121600000l, dataLast.getKey().getTime());

	String latitude = block0.getLatitude();

	assertNull(latitude);

	ZRXPBlock block2 = blocks.get(2);
	assertEquals("V30", block2.getStationIdentifier());
	assertEquals("V320, Þjórsártún", block2.getStationName());
	assertEquals("Þjórsá", block2.getRiverName());
	assertEquals("Q.O", block2.getParameterName());
	assertEquals("m³/s", block2.getUnit());
	assertEquals("-777", block2.getMissingDataValue());
	assertEquals("UTC-0", block2.getTimeZone());
	assertEquals((Integer) 110, block2.getStartHeadersLine());
	assertEquals((Integer) 115, block2.getStartDataLine());

	block0.addHeaderInFile(ZRXPKeyword.LATITUDE, "34");

	ZRXPDocument doc2 = new ZRXPDocument(tmp);
	ZRXPBlock b0 = doc2.getBlocks().get(0);
	assertEquals("Reykjafoss", b0.getStationName());
	assertEquals("34", b0.getLatitude());

	tmp.delete();
    }

    @Test
    public void testParseLine() throws Exception {
	ZRXPDocument doc = new ZRXPDocument(null);
	assertTrue(doc.isCommentLine(""));
	assertTrue(doc.isCommentLine("#"));
	assertTrue(doc.isCommentLine("##"));
	assertTrue(doc.isCommentLine("## comment"));
	assertTrue(doc.isCommentLine("###"));

	assertFalse(doc.isCommentLine("3")); // data
	assertFalse(doc.isCommentLine("#x")); // header

	assertFalse(doc.isHeaderLine(""));
	assertFalse(doc.isHeaderLine("#"));
	assertFalse(doc.isHeaderLine("##"));
	assertFalse(doc.isHeaderLine("## comment"));
	assertFalse(doc.isHeaderLine("###"));

	assertFalse(doc.isHeaderLine("3")); // data
	assertTrue(doc.isHeaderLine("#x")); // header

    }
}
