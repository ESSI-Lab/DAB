package eu.essi_lab.accessor.wof.client;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo.SiteProperty;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class HIS4ValuesValidator {

    private SimpleDateFormat iso8601WMLFormat;

    public HIS4ValuesValidator() {
	this.iso8601WMLFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601WMLFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void assertHIS4Values(TimeSeriesResponseDocument timeSeriesResponse) throws GSException {
	assertHIS4Values(timeSeriesResponse, null, null, null);
    }

    public void assertHIS4Values(TimeSeriesResponseDocument timeSeriesResponse, Date start, Date end, Integer expectedSize)
	    throws GSException {
	TimeSeries timeSeries = timeSeriesResponse.getTimeSeries().get(0);

	SiteInfo siteInfo = timeSeries.getSiteInfo();

	/*
	 * Assert SiteInfo obtained after getValues call
	 */
	assertServer4SiteInfo(siteInfo);
	// start additional values with respect to the previous call
	TestCase.assertEquals(1345.0, Double.parseDouble(siteInfo.getElevationMetres()), Math.pow(10, -10));
	TestCase.assertEquals("NGVD29", siteInfo.getVerticalDatum());
	TestCase.assertEquals("Utah", siteInfo.getState());
	// end

	/*
	 * Assert TimeSeries obtained after getValues call
	 */
	assertServer4Serie(timeSeries);
	// start additional values with respect to the previous call
	TestCase.assertEquals("4", timeSeries.getMethodCode());
	TestCase.assertEquals("http://www.campbellsci.com", timeSeries.getMethodLink());
	TestCase.assertEquals("1", timeSeries.getSourceCode());
	TestCase.assertEquals("inlandWaters", timeSeries.getSourceMetadataTopicCategory());
	TestCase.assertEquals("Little Bear River Conservation Effects Assessment Project Water Quality Data",
		timeSeries.getSourceMetadataTitle());
	TestCase.assertEquals(
		"Under funding from the United States Department of Agricultures Conservations Effects Assessment program, USU is conducting continuous water quality monitoring of the Little Bear River to demonstrate and compare the use of alternative monitoring approaches in estimating pollutant concentrations and loads.",
		timeSeries.getSourceMetadataAbstract());
	TestCase.assertEquals("Unknown", timeSeries.getSourceMetadataProfileVersion());
	TestCase.assertEquals("Jeff Horsburgh", timeSeries.getSourceContactName());
	TestCase.assertEquals("main", timeSeries.getSourceContactType());
	TestCase.assertEquals("jeff.horsburgh@usu.edu", timeSeries.getSourceContactEmail());
	TestCase.assertEquals("1-435-797-2946", timeSeries.getSourceContactPhone());
	TestCase.assertEquals("8200 Old Main Hill\n,Logan, UT 84322-8200", timeSeries.getSourceContactAddress());
	TestCase.assertEquals("http://littlebearriver.usu.edu", timeSeries.getSourceLink());
	TestCase.assertEquals("nc", timeSeries.getCensorCode());
	TestCase.assertEquals("not censored", timeSeries.getCensorCodeDescription());

	if (start == null || end == null) {
	    org.junit.Assert.assertEquals(49, timeSeries.getValues().size());

	    Value value = timeSeries.getValues().get(0);
	    TestCase.assertEquals("nc", value.getCensorCode());
	    TestCase.assertEquals("2005-08-04T11:30:00", value.getDateTime());
	    TestCase.assertEquals("-07:00", value.getTimeOffset());
	    TestCase.assertEquals("2005-08-04T18:30:00", value.getDateTimeUTC());
	    TestCase.assertEquals("4", value.getMethodCode());
	    TestCase.assertEquals("1", value.getSourceCode());
	    TestCase.assertEquals("0", value.getQualityControlLevelCode());
	    TestCase.assertEquals("13.24467", value.getValue());

	    value = timeSeries.getValues().get(48);
	    TestCase.assertEquals("nc", value.getCensorCode());
	    TestCase.assertEquals("2005-08-05T11:30:00", value.getDateTime());
	    TestCase.assertEquals("-07:00", value.getTimeOffset());
	    TestCase.assertEquals("2005-08-05T18:30:00", value.getDateTimeUTC());
	    TestCase.assertEquals("4", value.getMethodCode());
	    TestCase.assertEquals("1", value.getSourceCode());
	    TestCase.assertEquals("0", value.getQualityControlLevelCode());
	    TestCase.assertEquals("13.34372", value.getValue());
	} else {

	    long selectedStart = start.getTime();
	    long selectedEnd = end.getTime();

	    List<Value> values = timeSeries.getValues();

	    long fullStart = 1123180200000l;
	    long fullEnd = 1123180200000l + 1000 * 60 * 60 * 24;

	    long finalStart = Math.max(selectedStart, fullStart);
	    long finalEnd = Math.min(selectedEnd, fullEnd);

	    int calculatedSize;
	    if (finalEnd < finalStart) {
		calculatedSize = 0;
	    } else {
		long millisecondPortion = finalEnd - finalStart;
		// values are regular on a half an hour interval
		calculatedSize = (int) (millisecondPortion / (1000 * 60 * 30)) + 1;
	    }
	    if (expectedSize == null) {
		expectedSize = calculatedSize;
	    } else {
		TestCase.assertEquals((long) expectedSize, (long) calculatedSize);
	    }

	    TestCase.assertEquals((long) expectedSize, (long) values.size());
	    Long actualStart = null;
	    Long actualEnd = null;
	    for (Value value : values) {
		String utc = value.getDateTimeUTC();
		try {
		    Date date = iso8601WMLFormat.parse(utc);
		    long tmp = date.getTime();
		    if (actualStart == null || tmp < actualStart) {
			actualStart = tmp;
		    }
		    if (actualEnd == null || tmp > actualEnd) {
			actualEnd = tmp;
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	    if (actualStart != null) {
		TestCase.assertTrue(actualStart >= selectedStart);
		TestCase.assertTrue(actualEnd <= selectedEnd);
	    }

	}
	// end

    }

    public void assertServer4SiteInfo(SiteInfo site) {
	TestCase.assertEquals("Little Bear River at Mendon Road near Mendon, Utah", site.getSiteName());
	TestCase.assertEquals("LBR", site.getSiteCodeNetwork());
	TestCase.assertEquals("1", site.getSiteId());
	TestCase.assertEquals("USU-LBR-Mendon", site.getSiteCode());
	TestCase.assertEquals("41.718473", site.getLatitude());
	TestCase.assertEquals("-111.946402", site.getLongitude());
	TestCase.assertEquals("EPSG:4269", site.getSRS());
	TestCase.assertEquals("NAD83 / UTM zone 12N", site.getLocalSiteXYProjectionInformation());
	TestCase.assertEquals("421276.323", site.getLocalSiteX());
	TestCase.assertEquals("4618952.04", site.getLocalSiteY());
	TestCase.assertEquals("Cache", site.getCounty());
	TestCase.assertEquals("Located below county road bridge at Mendon Road crossing", site.getSiteComments());
	TestCase.assertEquals("10", site.getProperty(SiteProperty.POS_ACCURACY_M));
	TestCase.assertEquals("Stream", site.getProperty(SiteProperty.SITE_TYPE));

    }

    public void assertServer4Serie(TimeSeries serie) {
	TestCase.assertEquals("USU3", serie.getVariableCode());
	TestCase.assertEquals("3", serie.getVariableID());
	TestCase.assertEquals("LBR", serie.getVariableVocabulary());
	TestCase.assertEquals("Battery voltage", serie.getVariableName());
	TestCase.assertEquals("Field Observation", serie.getValueType());
	TestCase.assertEquals("Minimum", serie.getDataType());
	TestCase.assertEquals("Instrumentation", serie.getGeneralCategory());
	TestCase.assertEquals("Not Relevant", serie.getSampleMedium());
	TestCase.assertEquals("volts", serie.getUnitName());
	TestCase.assertEquals("Potential Difference", serie.getUnitType());
	TestCase.assertEquals("V", serie.getUnitAbbreviation());
	TestCase.assertEquals("168", serie.getUnitCode());
	TestCase.assertEquals(-9999.0, Double.parseDouble(serie.getNoDataValue()), Math.pow(10, -10));
	TestCase.assertEquals(true, serie.isTimeScaleRegular());
	TestCase.assertEquals("minute", serie.getTimeScaleUnitName());
	TestCase.assertEquals("Time", serie.getTimeScaleUnitType());
	TestCase.assertEquals("min", serie.getTimeScaleUnitAbbreviation());
	TestCase.assertEquals("102", serie.getTimeScaleUnitCode());
	TestCase.assertEquals(30l, serie.getTimeScaleTimeSupport().longValue());
	TestCase.assertEquals("Not Applicable", serie.getSpeciation());

	TestCase.assertEquals("4", serie.getMethodId());
	TestCase.assertEquals("Battery voltage measured by Campbell Scientific CR206 datalogger.", serie.getMethodDescription());

	TestCase.assertEquals("1", serie.getSourceId());
	TestCase.assertEquals("Utah State University Utah Water Research Laboratory", serie.getSourceOrganization());
	TestCase.assertEquals("Continuous water quality monitoring by Utah State University as part of the USDA CEAP Grant",
		serie.getSourceDescription());
	TestCase.assertEquals(
		"Continuous water quality monitoring by Jeff Horsburgh, David Stevens, Nancy Mesner and others from Utah State University as part of the USDA Conservation Effects Assessment Grant",
		serie.getSourceCitation());

	TestCase.assertEquals("0", serie.getQualityControlLevelID());
	TestCase.assertEquals("0", serie.getQualityControlLevelCode());
	TestCase.assertEquals("Raw data", serie.getQualityControlLevelDefinition());

    }
}
