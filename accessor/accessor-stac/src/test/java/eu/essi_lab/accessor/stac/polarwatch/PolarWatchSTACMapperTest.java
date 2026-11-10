package eu.essi_lab.accessor.stac.polarwatch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * Fixtures are real PolarWatch STAC collections and ERDDAP info documents, wrapped as done by
 * {@link PolarWatchSTACConnector}
 */
public class PolarWatchSTACMapperTest {

    private GSResource map(String resource) throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("polarwatch/" + resource);

	OriginalMetadata om = new OriginalMetadata();
	om.setSchemeURI(PolarWatchSTACMapper.POLARWATCH_STAC_SCHEME_URI);
	om.setMetadata(IOStreamUtils.asUTF8String(stream));

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	return new PolarWatchSTACMapper().map(om, source);
    }

    private static List<Online> onlines(MIMetadata md) {

	List<Online> out = new ArrayList<>();
	md.getDistribution().getDistributionOnlines().forEachRemaining(out::add);
	return out;
    }

    @Test
    public void viirsERDDAPGridTest() throws Exception {

	GSResource resource = map("viirsEnvelope.json");
	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Assert.assertTrue(resource instanceof Dataset);
	Assert.assertEquals("noaacwVIIRSn20iceconcNP06Daily", resource.getOriginalId().get());
	Assert.assertEquals("Ice Concentration, NOAA-20 VIIRS, Near Real-Time, Polar Stereographic (North), Daily",
		resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	// bbox from the ERDDAP WKT bounds, not the projected STAC bbox (±3434002.5 m)
	GeographicBoundingBox bbox = md.getDataIdentification().getGeographicBoundingBoxes().next();
	Assert.assertEquals(90.0, bbox.getNorth(), 0.001);
	Assert.assertEquals(-180.0, bbox.getWest(), 0.001);
	Assert.assertEquals(180.0, bbox.getEast(), 0.001);
	Assert.assertEquals(47.197074, bbox.getSouth(), 0.001);

	// near real time: open ended
	TemporalExtent extent = md.getDataIdentification().getTemporalExtent();
	Assert.assertEquals("2021-03-23T17:43:11Z", extent.getBeginPosition());
	Assert.assertEquals(TimeIndeterminateValueType.NOW, extent.getIndeterminateEndPosition());

	// ERDDAP variables, coordinate variables excluded
	List<String> variables = new ArrayList<>();
	md.getCoverageDescriptions().forEachRemaining(c -> variables.add(c.getAttributeIdentifier()));
	Assert.assertEquals(List.of("IceConc"), variables);
	CoverageDescription coverage = md.getCoverageDescriptions().next();
	Assert.assertEquals("Ice Concentration", coverage.getAttributeTitle());

	List<String> platforms = md.getDataIdentification().getKeywordsOfType("platform");
	Assert.assertEquals(List.of("NOAA-20"), platforms);
	Assert.assertEquals(List.of("VIIRS"), md.getDataIdentification().getKeywordsOfType("instrument"));

	Assert.assertEquals("EPSG:3413", md.getReferenceSystemInfos().next().getCode());

	List<Online> onlines = onlines(md);

	Online opendap = onlines.stream().filter(o -> NetProtocolWrapper.OPENDAP.getCommonURN().equals(o.getProtocol())).findFirst().get();
	Assert.assertEquals("https://coastwatch.noaa.gov/erddap/griddap/noaacwVIIRSn20iceconcNP06Daily", opendap.getLinkage());
	Assert.assertEquals("download", opendap.getFunctionCode());

	Assert.assertTrue(onlines.stream().anyMatch(o -> o.getLinkage().equals("https://coastwatch.noaa.gov/erddap/files/noaacwVIIRSn20iceconcNP06Daily")
		&& o.getFunctionCode().equals("download")));

	Assert.assertTrue(onlines.stream().anyMatch(
		o -> o.getLinkage().equals("https://polarwatch.noaa.gov/stac/collections/noaacwVIIRSn20iceconcNP06Daily/collection.json")));

	// the ERDDAP endpoint is linked by STAC both as 'service' and 'about': no duplicates
	long distinct = onlines.stream().map(Online::getLinkage).distinct().count();
	Assert.assertEquals(onlines.size(), distinct);
    }

    @Test
    public void laraProjectedAxesTest() throws Exception {

	GSResource resource = map("laraEnvelope.json");
	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Assert.assertEquals("lara_snowdepth_5yrs_awi_nh", resource.getOriginalId().get());

	// no ERDDAP lat/lon bounds and a global STAC bbox: derived from the projected x/y axes (NSIDC 25 km grid)
	GeographicBoundingBox bbox = md.getDataIdentification().getGeographicBoundingBoxes().next();
	Assert.assertEquals(90.0, bbox.getNorth(), 0.001);
	Assert.assertTrue("south was " + bbox.getSouth(), bbox.getSouth() > 25 && bbox.getSouth() < 40);

	TemporalExtent extent = md.getDataIdentification().getTemporalExtent();
	Assert.assertEquals("2018-01-12T00:00:00+00:00", extent.getBeginPosition());
	Assert.assertEquals("2018-12-12T00:00:00+00:00", extent.getEndPosition());

	List<String> variables = new ArrayList<>();
	md.getCoverageDescriptions().forEachRemaining(c -> variables.add(c.getAttributeIdentifier()));
	Assert.assertEquals(List.of("LaRA_sd_5yr"), variables);

	Assert.assertEquals(List.of("ICESat-2", "CryoSat-2"), md.getDataIdentification().getKeywordsOfType("platform"));

	Assert.assertTrue(onlines(md).stream().anyMatch(o -> o.getLinkage().equals("https://polarwatch.noaa.gov/erddap/griddap/lara_snowdepth_5yrs_awi_nh")
		&& NetProtocolWrapper.OPENDAP.getCommonURN().equals(o.getProtocol())));
    }

    @Test
    public void iabpWithoutERDDAPTest() throws Exception {

	GSResource resource = map("iabpEnvelope.json");
	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	// folder name, not the 'buoy_drift_velocities' id
	Assert.assertEquals("noaaIABPbuoyDrift", resource.getOriginalId().get());

	GeographicBoundingBox bbox = md.getDataIdentification().getGeographicBoundingBoxes().next();
	Assert.assertEquals(60.0, bbox.getSouth(), 0.001);
	Assert.assertEquals(90.0, bbox.getNorth(), 0.001);

	// "Current" end
	TemporalExtent extent = md.getDataIdentification().getTemporalExtent();
	Assert.assertEquals("2018-01-01", extent.getBeginPosition());
	Assert.assertEquals(TimeIndeterminateValueType.NOW, extent.getIndeterminateEndPosition());

	Assert.assertEquals(12500.0, md.getDataIdentification().getSpatialResolution().getDistanceValue(), 0.001);

	Assert.assertEquals(List.of("GPS", "Argos", "Iridium"), md.getDataIdentification().getKeywordsOfType("instrument"));

	List<Online> onlines = onlines(md);

	Assert.assertTrue(onlines.stream().noneMatch(o -> NetProtocolWrapper.OPENDAP.getCommonURN().equals(o.getProtocol())));

	// site root relative href
	Assert.assertTrue(onlines.stream().anyMatch(
		o -> o.getLinkage().equals("https://polarwatch.noaa.gov/stac/collections/noaaIABPbuoyDrift/buoy_interactive_vector_map.html")
			&& o.getFunctionCode().equals("information")));

	Assert.assertTrue(onlines.stream().anyMatch(o -> o.getLinkage().equals("https://iabp.apl.uw.edu/") && o.getFunctionCode().equals("download")));
    }

    @Test
    public void resolveHrefTest() {

	String catalog = "https://polarwatch.noaa.gov/stac/catalog.json";
	String collection = "https://polarwatch.noaa.gov/stac/collections/lara/collection.json";

	Assert.assertEquals("https://polarwatch.noaa.gov/stac/collections/lara/item_1.json",
		PolarWatchSTACMapper.resolveHref("./item_1.json", catalog, collection));

	Assert.assertEquals("https://polarwatch.noaa.gov/stac/collections/lara/collection.json",
		PolarWatchSTACMapper.resolveHref("./collections/lara/collection.json", catalog, collection));

	Assert.assertEquals("https://polarwatch.noaa.gov/stac/collections/lara/viewer.html",
		PolarWatchSTACMapper.resolveHref("stac/collections/lara/viewer.html", catalog, collection));

	Assert.assertEquals("https://example.org/a", PolarWatchSTACMapper.resolveHref("https://example.org/a", catalog, collection));
    }

    @Test
    public void parseResolutionTest() {

	Assert.assertEquals(12500.0, PolarWatchSTACMapper.parseResolution("12.5 km").get(), 0.001);
	Assert.assertEquals(1000.0, PolarWatchSTACMapper.parseResolution("1km").get(), 0.001);
	Assert.assertFalse(PolarWatchSTACMapper.parseResolution("").isPresent());
    }
}
