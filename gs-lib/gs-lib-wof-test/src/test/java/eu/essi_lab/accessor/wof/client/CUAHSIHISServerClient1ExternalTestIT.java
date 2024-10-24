package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class CUAHSIHISServerClient1ExternalTestIT extends CUAHSIHISServerClientTest {

    @Before
    public void init() {
	this.client = new CUAHSIHISServerClient1_1(CUAHSIEndpoints.ENDPOINT1);
    }

    @Test
    public void testServer1() throws GSException, UnsupportedEncodingException, TransformerException {

	// exercises get sites and gets first site with get site info
	SiteInfo site = testHISServerEndpoint(client, 1);

	assertServer1SiteInfo(site);

	List<TimeSeries> series = site.getSeries();
	TestCase.assertEquals(3, series.size());
	TimeSeries serie = series.get(0);

	assertServer1Serie(serie);
	TestCase.assertTrue(serie.getValueCount() >= 192);
	TestCase.assertEquals("2003-10-19T09:00:00", serie.getBeginTimePosition());
	TestCase.assertEquals("2015-03-27T09:00:00", serie.getEndTimePosition());
	TestCase.assertEquals("2003-10-19T08:00:00", serie.getBeginTimePositionUTC());
	TestCase.assertEquals("2015-03-27T08:00:00", serie.getEndTimePositionUTC());
	Date beginDate = serie.getBeginTimePositionDate();
	Date endDate = serie.getEndTimePositionDate();
	System.out.println(beginDate.getTime());
	System.out.println(endDate.getTime());


	TimeSeriesResponseDocument timeSeriesResponse = client.getValues("hsl-tos", "TOS01000025", "Precipitation", null, null, null,
		"2003-10-19T09:00:00", "2003-10-22T09:00:00");
	TimeSeries timeSeries = timeSeriesResponse.getTimeSeries().get(0);

	SiteInfo siteInfo = timeSeries.getSiteInfo();
	assertServer1SiteInfo(siteInfo);

	assertServer1Serie(timeSeries);

	TestCase.assertEquals("Toscana", timeSeries.getSourceContactName());
	TestCase.assertEquals("main", timeSeries.getSourceContactType());
	TestCase.assertEquals("Toscana@Toscana.it", timeSeries.getSourceContactEmail());
	TestCase.assertEquals("333", timeSeries.getSourceContactPhone());
	TestCase.assertEquals("Toscana, Toscana, Toscana, 333", timeSeries.getSourceContactAddress());
	TestCase.assertEquals("", timeSeries.getSourceLink());

	TestCase.assertEquals("nc", timeSeries.getCensorCode());
	TestCase.assertEquals("not censored", timeSeries.getCensorCodeDescription());

	TestCase.assertEquals(
		"Quality controlled data that have passed quality assurance procedures such as routine estimation of timing and sensor calibration or visual inspection and removal of obvious errors. An example is USGS published streamflow records following parsing through USGS quality control procedures.",
		timeSeries.getQualityControlLevelExplanation());

	Value value = timeSeries.getValues().get(0);
	TestCase.assertEquals("nc", value.getCensorCode());
	TestCase.assertEquals("2003-10-19T09:00:00", value.getDateTime());
	TestCase.assertEquals("1", value.getTimeOffset());
	TestCase.assertEquals("2003-10-19T08:00:00", value.getDateTimeUTC());
	TestCase.assertEquals("0", value.getMethodCode());
	TestCase.assertEquals("1", value.getSourceCode());
	TestCase.assertEquals("1", value.getQualityControlLevelCode());
	TestCase.assertEquals("7", value.getValue());

    }

    private void assertServer1SiteInfo(SiteInfo site) {
	TestCase.assertEquals("Vara", site.getSiteName());
	TestCase.assertEquals("hsl-tos", site.getSiteCodeNetwork());
	TestCase.assertEquals("1", site.getSiteId());
	TestCase.assertEquals("TOS01000025", site.getSiteCode());
	TestCase.assertEquals("44.084009", site.getLatitude());
	TestCase.assertEquals("10.128944", site.getLongitude());
    }

    private void assertServer1Serie(TimeSeries serie) {
	TestCase.assertEquals("Precipitation", serie.getVariableCode());
	TestCase.assertEquals("2", serie.getVariableID());
	TestCase.assertEquals("hsl-tos", serie.getVariableVocabulary());
	TestCase.assertEquals("Precipitation", serie.getVariableName());
	TestCase.assertEquals("Field Observation", serie.getValueType());
	TestCase.assertEquals("Cumulative", serie.getDataType());
	TestCase.assertEquals("Hydrology", serie.getGeneralCategory());
	TestCase.assertEquals("Precipitation", serie.getSampleMedium());
	TestCase.assertEquals("millimeter", serie.getUnitName());
	TestCase.assertEquals("Length", serie.getUnitType());
	TestCase.assertEquals("mm", serie.getUnitAbbreviation());
	TestCase.assertEquals("54", serie.getUnitCode());
	TestCase.assertEquals("-9999", serie.getNoDataValue());
	TestCase.assertEquals(false, serie.isTimeScaleRegular());
	TestCase.assertEquals("day", serie.getTimeScaleUnitName());
	TestCase.assertEquals("Time", serie.getTimeScaleUnitType());
	TestCase.assertEquals("d", serie.getTimeScaleUnitAbbreviation());
	TestCase.assertEquals("104", serie.getTimeScaleUnitCode());
	TestCase.assertEquals(1l, serie.getTimeScaleTimeSupport().longValue());
	TestCase.assertEquals("Not Applicable", serie.getSpeciation());

	TestCase.assertEquals("0", serie.getMethodId());
	TestCase.assertEquals("0", serie.getMethodCode());
	TestCase.assertEquals("No method specified", serie.getMethodDescription());
	TestCase.assertEquals("", serie.getMethodLink());

	TestCase.assertEquals("1", serie.getSourceId());
	TestCase.assertEquals("Toscana", serie.getSourceOrganization());
	TestCase.assertEquals("Toscana", serie.getSourceDescription());
	TestCase.assertEquals("Toscana", serie.getSourceCitation());

	TestCase.assertEquals("1", serie.getQualityControlLevelID());
	TestCase.assertEquals("1", serie.getQualityControlLevelCode());
	TestCase.assertEquals("Quality controlled data", serie.getQualityControlLevelDefinition());
    }

}
