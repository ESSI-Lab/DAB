package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class CUAHSIHISServerClient3ExternalTestIT extends CUAHSIHISServerClientTest {

    @Before
    public void init() {
	this.client = new CUAHSIHISServerClient1_1(CUAHSIEndpoints.ENDPOINT3);
    }

    @Test
    public void testServer3() throws GSException, UnsupportedEncodingException, TransformerException {

	SiteInfo site = testHISServerEndpoint(client, 10);
	TestCase.assertEquals("Ancient_Forest", site.getSiteName());
	TestCase.assertEquals("GlobalRiversObservatory", site.getSiteCodeNetwork());
	TestCase.assertEquals("1", site.getSiteId());
	TestCase.assertEquals("AncientF", site.getSiteCode());
	TestCase.assertEquals("53.762455", site.getLatitude());
	TestCase.assertEquals("-121.223328", site.getLongitude());
	TestCase.assertEquals("WGS84", site.getLocalSiteXYProjectionInformation());
	TestCase.assertEquals("53.762455", site.getLocalSiteX());
	TestCase.assertEquals("-121.223328", site.getLocalSiteY());
	TestCase.assertEquals("BC", site.getState());
	List<TimeSeries> series = site.getSeries();
	TestCase.assertTrue(series.size() > 25);
	TimeSeries serie = series.get(0);

	TestCase.assertEquals("nh4", serie.getVariableCode());
	TestCase.assertEquals("14", serie.getVariableID());
	TestCase.assertEquals("GlobalRiversObservatory", serie.getVariableVocabulary());
	TestCase.assertEquals("Nitrogen, NH4", serie.getVariableName());
	TestCase.assertEquals("Field Observation", serie.getValueType());
	TestCase.assertEquals("Sporadic", serie.getDataType());
	TestCase.assertEquals("Unknown", serie.getGeneralCategory());
	TestCase.assertEquals("Surface water", serie.getSampleMedium());
	TestCase.assertEquals("micromoles per liter", serie.getUnitName());
	TestCase.assertEquals("Concentration", serie.getUnitType());
	TestCase.assertEquals("umol/L", serie.getUnitAbbreviation());
	TestCase.assertEquals("303", serie.getUnitCode());
	TestCase.assertEquals("-9999", serie.getNoDataValue());
	TestCase.assertEquals(false, serie.isTimeScaleRegular());
	TestCase.assertEquals("hour", serie.getTimeScaleUnitName());
	TestCase.assertEquals("Time", serie.getTimeScaleUnitType());
	TestCase.assertEquals("hr", serie.getTimeScaleUnitAbbreviation());
	TestCase.assertEquals("103", serie.getTimeScaleUnitCode());
	TestCase.assertEquals(0l, serie.getTimeScaleTimeSupport().longValue());
	TestCase.assertEquals("NH4", serie.getSpeciation());
	TestCase.assertTrue(serie.getValueCount() >= 1); // this series has only a value
	TestCase.assertEquals("2011-06-02T00:00:00", serie.getBeginTimePosition());
	TestCase.assertEquals("2011-06-02T00:00:00", serie.getEndTimePosition());
	TestCase.assertEquals("2011-06-02T00:00:00", serie.getBeginTimePositionUTC());
	TestCase.assertEquals("2011-06-02T00:00:00", serie.getEndTimePositionUTC());
	TestCase.assertEquals("0", serie.getMethodId());
	TestCase.assertEquals("", serie.getMethodCode());
	TestCase.assertEquals("No method specified", serie.getMethodDescription());
	TestCase.assertEquals("", serie.getMethodLink());
	TestCase.assertEquals("3", serie.getSourceId());
	TestCase.assertEquals("Woods Hole Oceanographic Institute", serie.getSourceOrganization());
	TestCase.assertEquals("Unknown", serie.getSourceDescription());
	TestCase.assertEquals("Unknown", serie.getSourceCitation());
	TestCase.assertEquals("1", serie.getQualityControlLevelID());
	TestCase.assertEquals("1", serie.getQualityControlLevelCode());
	TestCase.assertEquals("Quality controlled data", serie.getQualityControlLevelDefinition());

    }

}
