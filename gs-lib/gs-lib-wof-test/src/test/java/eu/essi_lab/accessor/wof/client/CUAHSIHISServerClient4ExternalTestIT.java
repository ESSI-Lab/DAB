package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class CUAHSIHISServerClient4ExternalTestIT extends CUAHSIHISServerClientTest {

    @Before
    public void init() {
	this.client = new CUAHSIHISServerClient1_1(CUAHSIEndpoints.ENDPOINT4);
    }

    @Test
    public void testServer4() throws GSException, UnsupportedEncodingException, TransformerException {

	HIS4ValuesValidator validator = new HIS4ValuesValidator();

	SiteInfo site = testHISServerEndpoint(client, 10);
	/*
	 * Assert SiteInfo obtained after getSiteInfo call
	 */
	validator.assertServer4SiteInfo(site);

	List<TimeSeries> series = site.getSeries();
	TestCase.assertTrue(series.size() > 40);
	TimeSeries serie = series.get(0);

	/*
	 * Assert TimeSeries obtained after getSiteInfo call
	 */
	validator.assertServer4Serie(serie);
	// start information present only in the series description
	TestCase.assertTrue(serie.getValueCount() >= 186664);
	TestCase.assertEquals("2005-08-04T11:30:00", serie.getBeginTimePosition());
	TestCase.assertEquals("2016-08-02T12:30:00", serie.getEndTimePosition());
	TestCase.assertEquals("2005-08-04T18:30:00", serie.getBeginTimePositionUTC());
	TestCase.assertEquals("2016-08-02T19:30:00", serie.getEndTimePositionUTC());
	TestCase.assertEquals("", serie.getMethodCode());
	TestCase.assertEquals("", serie.getMethodLink());
	// end

	TimeSeriesResponseDocument timeSeriesResponse = client.getValues("LBR", "USU-LBR-Mendon", "USU3", null, null, null,
		"2005-08-04T11:30:00", "2005-08-05T11:30:00");

	validator.assertHIS4Values(timeSeriesResponse);

	TimeSeries timeSeries = timeSeriesResponse.getTimeSeries().get(0);

	/*
	 * We enrich the time series and test
	 */
	timeSeries = client.getAugmentedTimeSeries(site, "USU3", null, null, null);

	SiteInfo siteInfo = timeSeries.getSiteInfo();

	/*
	 * Assert SiteInfo obtained after getRicherTimeSeries call
	 */
	validator.assertServer4SiteInfo(siteInfo);
	// start additional values with respect to the previous call
	TestCase.assertEquals("1345", siteInfo.getElevationMetres());
	TestCase.assertEquals("NGVD29", siteInfo.getVerticalDatum());
	TestCase.assertEquals("Utah", siteInfo.getState());
	// end

	/*
	 * Assert TimeSeries obtained after getRicherTimeSeries call
	 */
	validator.assertServer4Serie(timeSeries);

	// NB: this is the augmented information!
	TestCase.assertTrue(serie.getValueCount() >= 186664);
	TestCase.assertEquals("2005-08-04T11:30:00", serie.getBeginTimePosition());
	TestCase.assertEquals("2016-08-02T12:30:00", serie.getEndTimePosition());
	TestCase.assertEquals("2005-08-04T18:30:00", serie.getBeginTimePositionUTC());
	TestCase.assertEquals("2016-08-02T19:30:00", serie.getEndTimePositionUTC());

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

	Value value = timeSeries.getValues().get(0);
	TestCase.assertEquals("nc", value.getCensorCode());
	TestCase.assertEquals("2005-08-04T11:30:00", value.getDateTime());
	TestCase.assertEquals("-07:00", value.getTimeOffset());
	TestCase.assertEquals("2005-08-04T18:30:00", value.getDateTimeUTC());
	TestCase.assertEquals("4", value.getMethodCode());
	TestCase.assertEquals("1", value.getSourceCode());
	TestCase.assertEquals("0", value.getQualityControlLevelCode());
	TestCase.assertEquals("13.24467", value.getValue());
	// end

    }

    @Test
    public void testServer4BIS() throws ParseException, GSException {
	String date = "2005-08-04T18:30:00";
	SimpleDateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

	Date d = iso8601OutputFormat.parse(date);

	TimeSeriesResponseDocument tsrd = client.getValues("LBR", "USU-LBR-Mendon", "USU3", null, null, null, d, d);
	List<Value> values = tsrd.getTimeSeries().get(0).getValues();
	Value v = values.get(0);
	TestCase.assertEquals("13.24467", v.getValue());
    }

}
