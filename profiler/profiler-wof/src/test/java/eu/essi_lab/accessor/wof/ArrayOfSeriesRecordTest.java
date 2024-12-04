package eu.essi_lab.accessor.wof;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import eu.essi_lab.profiler.wof.info.datamodel.ArrayOfSeriesRecord;

public class ArrayOfSeriesRecordTest {
    @Test
    public void test() throws Exception {
	ArrayOfSeriesRecord record = new ArrayOfSeriesRecord();
	String serverCode = "servCode";
	String genCategory = "genCat";
	String conceptKeyword = "ck";
	String timeUnits = "seconds";
	String sampleMedium = "water";
	String valueType = "vt";
	String dataType = "dt";
	String longitude = "30";
	String latitude = "13";
	String siteName = "siteName";
	String valueCount = "31";
	String endDate = "2013";
	String beginDate = "1800";
	String varName = "temperature";
	String varCode = "temp";
	String location = "calenzano";
	String serverURL = "http://";
	String timeSupport = "1";

	record.addSeriesRecord(serverCode, serverURL, location, varCode, varName, beginDate, endDate, valueCount, siteName, latitude,
		longitude, dataType, valueType, sampleMedium, timeUnits, conceptKeyword, genCategory, timeSupport);

	record.addSeriesRecord(serverCode, serverURL, location, varCode, varName, beginDate, endDate, valueCount, siteName, latitude,
		longitude, dataType, valueType, sampleMedium, timeUnits, conceptKeyword, genCategory, timeSupport);

	InputStream stream = ArrayOfSeriesRecordTest.class.getClassLoader().getResourceAsStream("cuahsi/test1.xml");
	String str = IOUtils.toString(stream, "UTF-8");

	assertEquals(str.replaceAll("[^A-Za-z0-9]", ""), record.asString().replaceAll("[^A-Za-z0-9]", ""));
    }
}
