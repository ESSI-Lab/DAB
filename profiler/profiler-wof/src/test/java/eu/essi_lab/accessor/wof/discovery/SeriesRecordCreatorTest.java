package eu.essi_lab.accessor.wof.discovery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.essi_lab.accessor.wof.discovery.series.SeriesRecordCreator;

public class SeriesRecordCreatorTest {

    @Test
    public void testName() throws Exception {
	SeriesRecordCreator creator = new SeriesRecordCreator();
	String seriesRecord = creator.createSeriesRecord("ServCode&", "ServURL", "location", "VarCode", "VarName", "beginDate", "endDate",
		"ValueCount", "Sitename", "latitude", "longitude", "datatype", "valuetype", "samplemedium", "timeunits", "conceptKeyword",
		"genCategory", "TimeSupport");

	String result = "<SeriesRecord><ServCode>ServCode&amp;</ServCode><ServURL>ServURL</ServURL><location>location</location><VarCode>VarCode</VarCode><VarName>VarName</VarName><beginDate>beginDate</beginDate><endDate>endDate</endDate><ValueCount>ValueCount</ValueCount><Sitename>Sitename</Sitename><latitude>latitude</latitude><longitude>longitude</longitude><datatype>datatype</datatype><valuetype>valuetype</valuetype><samplemedium>samplemedium</samplemedium><timeunits>timeunits</timeunits><conceptKeyword>conceptKeyword</conceptKeyword><genCategory>genCategory</genCategory><TimeSupport>TimeSupport</TimeSupport></SeriesRecord>";

	assertEquals(result, seriesRecord.trim());

    }
}
