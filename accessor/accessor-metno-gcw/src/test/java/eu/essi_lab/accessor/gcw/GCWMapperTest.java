package eu.essi_lab.accessor.gcw;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class GCWMapperTest {

    @Test
    public void testSeriesName() {

	assertEquals("TA", GCWOPeNDAP.getSeriesName("TA", null));
	assertEquals("soil_temperature[3]", GCWOPeNDAP.getSeriesName("soil_temperature", 3));

	SimpleEntry<String, Integer> parsed = GCWOPeNDAP.parseSeriesName("soil_temperature[3]");
	assertEquals("soil_temperature", parsed.getKey());
	assertEquals(Integer.valueOf(3), parsed.getValue());
	assertNull(GCWOPeNDAP.parseSeriesName("TA").getValue());
    }

    @Test
    public void testResolution() {

	assertEquals("PT30S", GCWMapper.normalizeResolution("PT30S"));
	assertEquals("PT10M", GCWMapper.normalizeResolution("P600S"));
	assertEquals("PT625S", GCWMapper.normalizeResolution("P625S"));
	assertEquals("PT1H", GCWMapper.normalizeResolution("P0DT1H0M0S"));
	assertEquals("P1D", GCWMapper.normalizeResolution("P1DT0H0M0S"));
	assertEquals("P1M", GCWMapper.normalizeResolution("P1M"));
	assertNull(GCWMapper.normalizeResolution("hourly"));
	assertNull(GCWMapper.normalizeResolution(null));
    }

    @Test
    public void testHelpers() {

	assertEquals(InterpolationType.AVERAGE, GCWMapper.getInterpolation("time: mean").get());
	assertEquals(InterpolationType.TOTAL, GCWMapper.getInterpolation("area: point time: sum").get());
	assertTrue(GCWMapper.getInterpolation("area: mean").isEmpty());
	assertEquals("°C", GCWMapper.normalizeUnits("Celsius"));
	assertEquals("°C", GCWMapper.normalizeUnits("degC"));
	assertEquals("K", GCWMapper.normalizeUnits("K"));

	JSONObject level = new JSONObject().put(GCWTimeSeriesDataset.SERIES_VARIABLE, "soil_temperature")
		.put(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX, 1).put(GCWTimeSeriesDataset.SERIES_LEVEL_NAME, "depth")
		.put(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE, 20.0).put(GCWTimeSeriesDataset.SERIES_LEVEL_UNITS, "cm");
	assertEquals("at 20 cm depth", GCWMapper.getLevelLabel(level));
	assertEquals("soil temperature", GCWMapper.getName(level.put("standard_name", "soil_temperature")));
    }

    @Test
    public void testMapping() throws Exception {

	JSONObject catalogue = new JSONObject()//
		.put(GCWClient.ID, "no.met.adc:test")//
		.put(GCWClient.TITLE, "SYNOP data from station BJORNOYA")//
		.put(GCWClient.ABSTRACT, "Observations from the WMO station Bjornoya.")//
		.put(GCWClient.KEYWORDS, new JSONArray().put("EARTH SCIENCE > ATMOSPHERE"))//
		.put(GCWClient.CONTACTS, new JSONArray().put(new JSONObject().put(GCWClient.CONTACT_ORGANISATION, "MET Norway")
			.put(GCWClient.CONTACT_EMAIL, "adc-support@met.no").put(GCWClient.CONTACT_ROLE, "pointOfContact")))//
		.put(GCWClient.CONSTRAINTS, new JSONArray().put("CC-BY-4.0"))//
		.put(GCWClient.OPENDAP, "https://thredds.met.no/thredds/dodsC/arcticdata/obsSynop/01028")//
		.put(GCWClient.LANDING_PAGE, "https://adc.met.no/dataset/test")//
		.put(GCWClient.NORTH, "75").put(GCWClient.SOUTH, "74").put(GCWClient.WEST, "18").put(GCWClient.EAST, "20");

	JSONObject metadata = new JSONObject()//
		.put(GCWConnector.RECORD_CATALOGUE, catalogue)//
		.put(GCWConnector.RECORD_DATASET, new JSONObject().put("time_coverage_resolution", "PT1H"))//
		.put(GCWConnector.RECORD_STATION, new JSONObject().put(GCWTimeSeriesDataset.STATION_ID, "01028")
			.put(GCWTimeSeriesDataset.STATION_LATITUDE, "74.5035").put(GCWTimeSeriesDataset.STATION_LONGITUDE, "18.998")
			.put(GCWTimeSeriesDataset.STATION_ALTITUDE, "16"))//
		.put(GCWConnector.RECORD_SERIES, new JSONObject().put(GCWTimeSeriesDataset.SERIES_VARIABLE, "TA")
			.put("standard_name", "air_temperature").put("long_name", "air temperature at 2 meter").put("units", "Celsius"))//
		.put(GCWConnector.RECORD_BEGIN, 1767225600000l)//
		.put(GCWConnector.RECORD_END, 1793836800000l);

	OriginalMetadata original = new OriginalMetadata();
	original.setMetadata(metadata.toString());
	original.setSchemeURI(GCWMapper.GCW_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(GCWClient.DEFAULT_ENDPOINT);

	GSResource resource = new GCWMapper().map(original, source);
	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("air temperature at 2 meter - SYNOP data from station BJORNOYA", core.getTitle());
	assertTrue(core.getAbstract().contains("Observations from the WMO station Bjornoya."));
	assertEquals(74.5035, core.getBoundingBox().getNorth(), 0.0001);
	assertEquals(18.998, core.getBoundingBox().getWest(), 0.0001);
	assertEquals("°C", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("PT1H", resource.getExtensionHandler().getTimeResolutionDuration8601().get());

	Online online = core.getOnline();
	assertEquals("TA", online.getName());
	assertEquals(NetProtocolWrapper.GCW.getCommonURN(), online.getProtocol());
	assertEquals("https://thredds.met.no/thredds/dodsC/arcticdata/obsSynop/01028", online.getLinkage());

	metadata.getJSONObject(GCWConnector.RECORD_SERIES).put(GCWTimeSeriesDataset.SERIES_VARIABLE, "WW").put("long_name",
		"present weather according to WMO code table 020003");
	original.setMetadata(metadata.toString());
	resource = new GCWMapper().map(original, source);
	assertEquals(InterpolationType.CATEGORICAL, resource.getExtensionHandler().getTimeInterpolation().get());
    }
}
