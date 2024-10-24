package eu.essi_lab.accessor.arcgis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class AGOLMapperTest {

    @Test
    public void test() throws GSException, IOException {

	AGOLMapper mapper = new AGOLMapper();

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = AGOLConnectorTest.class.getClassLoader().getResourceAsStream("singleOriginal.json");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals(Optional.of("6bddc1f4c3644d758581cf4cf7b2fded"), mapped.getOriginalId());

	Assert.assertEquals("Federal Clean Air Act Ozone Attainment California",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals("2014-01-28T00:04:17Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationCreationDate());

	Assert.assertEquals("2014-01-28T06:12:01Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	Assert.assertEquals(
		"According to the 2008 amendment to the US E.P.A. Clean Air Act, areas (county parcels) with 8 hour average ozone concentrations greater than 0.075ppm (150 ug/m^3) more than one day a year. Ozone is included in the Clean Air Act (42 U.S.C. 7401 et seq.) under the National Ambient Air Quality Standards (NAAQS) that apply for outdoor air for the entirety of the United States. Research has shown oil and gas development utilizing hydraulic fracturing can contribute to elevated concentrations of ambient ozone ((Utah Department of Environmental Quality, 2013; Olaguer, E. P. \"The Potential near-Source Ozone Impacts of Upstream Oil and Gas Industry Emissions.\" Journal of the Air & Waste Management Association 62, no. 8 (2012): 966-77))",
		mapped.getHarmonizedMetadata().getCoreMetadata().getAbstract());

	Assert.assertEquals("air",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getKeywords().next().getElement()
			.getValue().getKeyword().get(0).getCharacterString().getValue());

	Assert.assertEquals(
		"http://www.arcgis.com/sharing/rest/content/items/6bddc1f4c3644d758581cf4cf7b2fded/info/thumbnail/thumbnail.png",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGraphicOverview()
			.getFileName());

	Assert.assertEquals(32.4235175955401,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getSouth(), 0.001);

	Assert.assertEquals(-113.49863787908843,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getEast(), 0.001);

	Assert.assertEquals(-124.50605939842023,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getWest(), 0.001);

	Assert.assertEquals(42.06893788884729,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getNorth(), 0.001);

    }

    @Test
    public void test2() throws GSException, IOException {

	AGOLMapper mapper = new AGOLMapper();

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = AGOLConnectorTest.class.getClassLoader().getResourceAsStream("singleOriginalNullDescription.json");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals(Optional.of("6bddc1f4c3644d758581cf4cf7b2fded"), mapped.getOriginalId());

	Assert.assertEquals("Federal Clean Air Act Ozone Attainment California",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals("2014-01-28T00:04:17Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationCreationDate());

	Assert.assertEquals("2014-01-28T06:12:01Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	Assert.assertEquals("air",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getKeywords().next().getElement()
			.getValue().getKeyword().get(0).getCharacterString().getValue());

	Assert.assertEquals(
		"http://www.arcgis.com/sharing/rest/content/items/6bddc1f4c3644d758581cf4cf7b2fded/info/thumbnail/thumbnail.png",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGraphicOverview()
			.getFileName());

	Assert.assertEquals(32.4235175955401,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getSouth(), 0.001);

	Assert.assertEquals(-113.49863787908843,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getEast(), 0.001);

	Assert.assertEquals(-124.50605939842023,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getWest(), 0.001);

	Assert.assertEquals(42.06893788884729,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getNorth(), 0.001);

    }

    @Test
    public void test3() throws GSException, IOException {

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	AGOLMapper mapper = new AGOLMapper();

	InputStream stream = AGOLConnectorTest.class.getClassLoader().getResourceAsStream("extentIssue.json");

	String md = IOStreamUtils.asUTF8String(stream);

	JSONObject all = new JSONObject(md);

	JSONArray array = all.getJSONArray("results");

	for (int i = 0; i < array.length(); i++) {

	    OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	    Mockito.doReturn(array.getJSONObject(i).toString()).when(om).getMetadata();

	    GSResource mapped = mapper.map(om, source);

	}

    }

}