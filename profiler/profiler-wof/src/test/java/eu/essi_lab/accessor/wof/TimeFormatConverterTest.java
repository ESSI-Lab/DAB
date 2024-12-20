package eu.essi_lab.accessor.wof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.profiler.wof.TimeFormatConverter;

public class TimeFormatConverterTest {

    private TimeFormatConverter converter;

    @Before
    public void init() {
	this.converter = new TimeFormatConverter();
    }

    @Test
    public void testGetSeriesFormatGood() throws Exception {
	assertEquals("2018-12-02T00:00:00Z", converter.convertGetSeriesTimeFormatToISO8601("12/02/2018"));
	assertEquals("2018-12-02T00:00:00Z", converter.convertGetSeriesTimeFormatToISO8601("2018-12-02"));
    }

    @Test
    public void testGetSeriesFormatBad() throws Exception {
	assertNotEquals("2018-02-12T00:00:00Z", converter.convertGetSeriesTimeFormatToISO8601("12/02/2028"));
    }

    @Test
    public void testGetSeriesFormatWrong() throws Exception {
	assertNull(converter.convertGetSeriesTimeFormatToISO8601("12_02_2028")); // completely wrong because different
										 // separator
    }

    @Test
    public void testGetValuesFormatGood() throws Exception {
	assertEquals("2018-02-12T00:30:00Z", converter.convertGetValuesTimeFormatToISO8601("2018-02-12T00:30"));
	assertEquals("2018-02-12T00:00:00Z", converter.convertGetValuesTimeFormatToISO8601("2018-02-12"));
    }

    @Test
    public void testGetValuesFormatBad() throws Exception {
	assertNotEquals("2018-02-12T00:00:00Z", converter.convertGetValuesTimeFormatToISO8601("2018-02-12T00:30"));
	assertNotEquals("2018-02-12T00:30:00Z", converter.convertGetValuesTimeFormatToISO8601("2018-02-12"));
    }

    @Test
    public void testGetValuesFormatWrong() throws Exception {
	assertNull(converter.convertGetValuesTimeFormatToISO8601("2018/02/12T00:30:00")); // completely wrong because
											  // different separator
	assertNull(converter.convertGetValuesTimeFormatToISO8601("12/02/2018")); // get series format
    }

}
