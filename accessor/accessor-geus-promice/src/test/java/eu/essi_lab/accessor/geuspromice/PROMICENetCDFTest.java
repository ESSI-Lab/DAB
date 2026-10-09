package eu.essi_lab.accessor.geuspromice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class PROMICENetCDFTest {

    @Test
    public void cfTimeConversion() {

	assertEquals("1995-06-07T00:00:00Z",
		ISO8601DateTimeUtils.getISO8601DateTime(PROMICENetCDF.toDate(0, "days since 1995-06-07 00:00:00").get()));
	assertEquals("1995-06-08T12:00:00Z",
		ISO8601DateTimeUtils.getISO8601DateTime(PROMICENetCDF.toDate(1.5, "days since 1995-06-07 00:00:00").get()));
	assertEquals("1995-06-07T15:00:00Z",
		ISO8601DateTimeUtils.getISO8601DateTime(PROMICENetCDF.toDate(2, "hours since 1995-06-07 13:00:00").get()));
	assertEquals("2000-01-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(PROMICENetCDF.toDate(0, "days since 2000-01-01").get()));

	assertTrue(PROMICENetCDF.toDate(Double.NaN, "days since 1995-06-07 00:00:00").isEmpty());
	assertTrue(PROMICENetCDF.toDate(1, "weeks since 1995-06-07").isEmpty());
	assertTrue(PROMICENetCDF.toDate(1, "unknown").isEmpty());
    }

    @Test
    public void resolutionNormalization() {

	assertEquals("P1D", PROMICENetCDF.normalizeResolution("P1DT0H0M0S"));
	assertEquals("PT1H", PROMICENetCDF.normalizeResolution("P0DT1H0M0S"));
	assertEquals("PT10M", PROMICENetCDF.normalizeResolution("P0DT0H10M0S"));
	assertEquals("P1M", PROMICENetCDF.normalizeResolution("P30DT0H0M0S"));
	assertNull(PROMICENetCDF.normalizeResolution(""));
	assertNull(PROMICENetCDF.normalizeResolution("not a duration"));
    }

    @Test
    public void mapperHelpers() {

	assertEquals("°C", PROMICEMapper.normalizeUnits("degrees_C"));
	assertEquals("°C", PROMICEMapper.normalizeUnits("C"));
	assertEquals("", PROMICEMapper.normalizeUnits("-"));
	assertEquals("W m-2", PROMICEMapper.normalizeUnits("W m-2"));
	assertEquals("daily", PROMICEMapper.getResolutionLabel("P1D"));
	assertEquals("time series", PROMICEMapper.getResolutionLabel(null));
    }
}
