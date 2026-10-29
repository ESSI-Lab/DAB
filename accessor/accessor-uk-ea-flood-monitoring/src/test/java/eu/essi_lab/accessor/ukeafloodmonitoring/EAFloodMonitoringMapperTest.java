package eu.essi_lab.accessor.ukeafloodmonitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class EAFloodMonitoringMapperTest {

    @Test
    public void mapTest() throws GSException {

	EAFloodMonitoringEntity series = EAFloodMonitoringClientTest.createWaterLevelSeries();

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Bourton Dickler - Water level (mASD, 15 min)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("metres above stage datum"));
	assertTrue(coreMetadata.getAbstract().contains("River Dikler"));
	assertTrue(coreMetadata.getAbstract().contains("rolling window"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2026-09-29T00:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("1029TH-level-stage-i-15_min-mASD", online.getName());
	assertEquals(NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN(), online.getProtocol());

	assertEquals("mASD", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("metres above stage datum", resource.getExtensionHandler().getAttributeUnits().get());
	assertTrue(resource.getExtensionHandler().getCountry().get().startsWith("United Kingdom"));
	assertEquals("PT15M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN() + ":level-stage",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertTrue(coreMetadata.getMIMetadata().getDataIdentification().getLegalConstraints().next().getUseLimitation()
		.contains("Open Government Licence"));
    }

    @Test
    public void tidalMeanTest() throws GSException {

	EAFloodMonitoringEntity series = EAFloodMonitoringClientTest.createWaterLevelSeries();
	JSONObject object = series.getObject();
	object.put(EAFloodMonitoringClient.QUALIFIER, "Tidal Level");
	object.put(EAFloodMonitoringClient.VALUE_TYPE, "mean");
	object.put(EAFloodMonitoringClient.UNIT_NAME, "mAOD");

	GSResource resource = map(series);

	assertEquals("Bourton Dickler - Tidal level (mAOD, 15 min mean)", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	assertEquals(InterpolationType.AVERAGE, resource.getExtensionHandler().getTimeInterpolation().get());
	assertEquals("PT15M", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
    }

    @Test
    public void propertyNameTest() {

	assertEquals("Water level (downstream stage)", name("level", "Downstream Stage"));
	assertEquals("Groundwater level", name("level", "Groundwater"));
	assertEquals("Water level (sensor 2)", name("level", "2"));
	assertEquals("Discharge", name("flow", ""));
	assertEquals("Water temperature", name("temperature", "Water"));
	assertEquals("Air temperature", name("temperature", "Dry Bulb"));
	assertEquals("Precipitation", name("rainfall", "Tipping Bucket Raingauge"));

	assertEquals("1 hour", EAFloodMonitoringMapper.getPeriodLabel(3600));
	assertEquals("PT1H", EAFloodMonitoringMapper.getPeriodDuration(3600));
	assertEquals("5 min", EAFloodMonitoringMapper.getPeriodLabel(300));
    }

    /**
     * @param parameter
     * @param qualifier
     * @return
     */
    private static String name(String parameter, String qualifier) {

	JSONObject object = new JSONObject();
	object.put(EAFloodMonitoringClient.PARAMETER, parameter);
	object.put(EAFloodMonitoringClient.QUALIFIER, qualifier);
	return EAFloodMonitoringMapper.getPropertyName(new EAFloodMonitoringEntity(object));
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private static GSResource map(EAFloodMonitoringEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(EAFloodMonitoringMapper.EA_FLOOD_MONITORING_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(EAFloodMonitoringClient.DEFAULT_ENDPOINT);

	return new EAFloodMonitoringMapper().map(originalMetadata, source);
    }
}
