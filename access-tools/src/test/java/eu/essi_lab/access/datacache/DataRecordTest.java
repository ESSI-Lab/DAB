package eu.essi_lab.access.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.utils.JsonFileBuffer;

public class DataRecordTest {

    @Test
    public void test() throws Exception {
	Path tmp = Files.createTempFile("test", ".txt");
	JsonFileBuffer<DataRecord> buffer = new JsonFileBuffer<DataRecord>(tmp.toFile(), DataRecord.class);
	Date now = new Date();
	DataRecord d1 = new DataRecord(now, new BigDecimal("3.5"), "m", "temperature",
		new LatitudeLongitude(new BigDecimal("2.5"), new BigDecimal("10.3")), "d1");
	DataRecord d2 = new DataRecord(now, new BigDecimal("13.5"), "m", "rain",
		new LatitudeLongitude(new BigDecimal("2.5"), new BigDecimal("8.1")), "d2");
	List<DataRecord> polled;
	assertEquals(0, buffer.sizeEstimate());
	buffer.add(d1);
	assertEquals(1, buffer.sizeEstimate());
	buffer.add(d1);
	assertEquals(2, buffer.sizeEstimate());
	buffer.add(d2);
	assertEquals(3, buffer.sizeEstimate());
	polled = buffer.poll(1);
	assertEquals(2, buffer.sizeEstimate());
	checkD1(polled.get(0));

	polled = buffer.poll(1);
	assertEquals(1, buffer.sizeEstimate());
	checkD1(polled.get(0));
	polled = buffer.poll(1);
	assertEquals(0, buffer.sizeEstimate());
	checkD2(polled.get(0));

	buffer.add(d1);
	assertEquals(1, buffer.sizeEstimate());
	buffer.add(d1);
	assertEquals(2, buffer.sizeEstimate());
	buffer.add(d2);
	assertEquals(3, buffer.sizeEstimate());

	polled = buffer.poll(100);

	assertEquals(3, polled.size());
	checkD1(polled.get(0));
	checkD1(polled.get(1));
	checkD2(polled.get(2));

	tmp.toFile().delete();
    }

    private void checkD1(DataRecord dataRecord) {
	assertEquals("d1", dataRecord.getDataIdentifier());
	assertEquals(new BigDecimal("3.5"), dataRecord.getValue());
	assertEquals(new BigDecimal("2.5"), dataRecord.getLatitudeLongitude().getLatitude());
	assertEquals(new BigDecimal("10.3"), dataRecord.getLatitudeLongitude().getLongitude());

    }

    private void checkD2(DataRecord dataRecord) {
	assertEquals("d2", dataRecord.getDataIdentifier());
	assertEquals(new BigDecimal("13.5"), dataRecord.getValue());
	assertEquals(new BigDecimal("2.5"), dataRecord.getLatitudeLongitude().getLatitude());
	assertEquals(new BigDecimal("8.1"), dataRecord.getLatitudeLongitude().getLongitude());

    }

}
