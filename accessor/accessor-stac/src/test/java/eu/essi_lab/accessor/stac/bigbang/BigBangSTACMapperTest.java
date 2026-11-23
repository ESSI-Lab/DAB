package eu.essi_lab.accessor.stac.bigbang;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * The fixture is the real BIGBANG 10.0 total precipitation collection (item links trimmed to 12 monthly and 2
 * annual), wrapped as done by {@link BigBangSTACConnector}
 */
public class BigBangSTACMapperTest {

    private GSResource map(String resource) throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("bigbang/" + resource);

	OriginalMetadata om = new OriginalMetadata();
	om.setSchemeURI(BigBangSTACMapper.BIGBANG_STAC_SCHEME_URI);
	om.setMetadata(IOStreamUtils.asUTF8String(stream));

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	return new BigBangSTACMapper().map(om, source);
    }

    @Test
    public void totalPrecipitationTest() throws Exception {

	GSResource resource = map("totalPrecipitationEnvelope.json");
	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Assert.assertTrue(resource instanceof Dataset);
	Assert.assertEquals("bigbang100-total-precipitation", resource.getOriginalId().get());
	Assert.assertEquals("BIGBANG 10.0 - Total Precipitation (TP)", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	String abs = md.getDataIdentification().getAbstract();
	Assert.assertTrue(abs.startsWith("Total Precipitation (Precipitazioni totali) - symbol TP, unit: mm."));
	Assert.assertTrue(abs.endsWith("The collection includes 14 individual grids (12 monthly, 2 annual), each published as a STAC item."));

	Assert.assertTrue(md.getDataIdentification().getSupplementalInformation().startsWith("Cite data as follows: Braca G."));

	List<String> keywords = new ArrayList<>();
	md.getDataIdentification().getKeywordsValues().forEachRemaining(keywords::add);
	Assert.assertTrue(keywords.containsAll(List.of("BIGBANG", "ISPRA", "TP", "monthly", "annual")));

	// extents
	GeographicBoundingBox bbox = md.getDataIdentification().getGeographicBoundingBoxes().next();
	Assert.assertEquals(47.413586, bbox.getNorth(), 0.000001);
	Assert.assertEquals(4.434638, bbox.getWest(), 0.000001);
	Assert.assertEquals(34.256941, bbox.getSouth(), 0.000001);
	Assert.assertEquals(21.556096, bbox.getEast(), 0.000001);

	TemporalExtent extent = md.getDataIdentification().getTemporalExtent();
	Assert.assertEquals("1951-01-01T00:00:00Z", extent.getBeginPosition());
	Assert.assertEquals("2025-12-31T23:59:59Z", extent.getEndPosition());

	Assert.assertEquals("EPSG:3035", md.getReferenceSystemInfos().next().getCode());

	// variable
	CoverageDescription coverage = md.getCoverageDescriptions().next();
	Assert.assertEquals("TP", coverage.getAttributeIdentifier());
	Assert.assertEquals("Total Precipitation", coverage.getAttributeTitle());
	Assert.assertEquals("Total Precipitation (mm)", coverage.getAttributeDescription());

	Assert.assertEquals("millimetre", resource.getExtensionHandler().getAttributeUnits().get());
	Assert.assertEquals("mm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());

	// license from the catalog/collection license link
	String useLimitation = md.getDataIdentification().getLegalConstraintsUseLimitations().next();
	Assert.assertEquals("Italian Open Data License v2.0 (IODL 2.0)", useLimitation);

	// contacts: the two providers plus the corresponding author
	List<ResponsibleParty> contacts = new ArrayList<>();
	md.getDataIdentification().getPointOfContacts().forEachRemaining(contacts::add);
	Assert.assertEquals(3, contacts.size());
	Assert.assertEquals("originator", contacts.get(0).getRoleCode());

	ResponsibleParty author = contacts.get(1);
	Assert.assertEquals("Giovanni Braca", author.getIndividualName());
	Assert.assertEquals("giovanni.braca@isprambiente.it", author.getContact().getAddress().getElectronicMailAddress());

	Assert.assertEquals("distributor", contacts.get(2).getRoleCode());

	// online resources: 4 archives, 5 documents, via, license, STAC collection
	List<Online> onlines = new ArrayList<>();
	md.getDistribution().getDistributionOnlines().forEachRemaining(onlines::add);
	Assert.assertEquals(12, onlines.size());

	Online monthly = onlines.stream()
		.filter(o -> o.getLinkage().equals("https://boldrini.essi-lab.eu/data/ASCII_GRID/TOTAL_PRECIPITATION/TP_MONTHLY_1951-2025.zip"))
		.findFirst().get();
	Assert.assertEquals("download", monthly.getFunctionCode());
	Assert.assertEquals("Total Precipitation - monthly 1951-2025", monthly.getName());

	Assert.assertTrue(onlines.stream().anyMatch(
		o -> o.getLinkage().equals("https://boldrini.essi-lab.eu/stac/bigbang100-total-precipitation/collection.json")));
    }

    @Test
    public void collectionAsParentTest() throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("bigbang/totalPrecipitationEnvelope.json");
	JSONObject envelope = new JSONObject(IOStreamUtils.asUTF8String(stream));
	envelope.put(BigBangSTACMapper.HARVEST_ITEMS, true);

	OriginalMetadata om = new OriginalMetadata();
	om.setSchemeURI(BigBangSTACMapper.BIGBANG_STAC_SCHEME_URI);
	om.setMetadata(envelope.toString());

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	GSResource resource = new BigBangSTACMapper().map(om, source);

	Assert.assertTrue(resource instanceof DatasetCollection);
	Assert.assertEquals("bigbang100-total-precipitation", resource.getOriginalId().get());
	Assert.assertEquals("series", resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getHierarchyLevelName());
    }

    @Test
    public void itemTest() throws Exception {

	GSResource resource = map("totalPrecipitationItemEnvelope.json");
	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Assert.assertTrue(resource instanceof Dataset);
	Assert.assertEquals("bigbang100-total-precipitation/tp_1951_01c", resource.getOriginalId().get());
	Assert.assertEquals("bigbang100-total-precipitation", md.getParentIdentifier());
	Assert.assertEquals("BIGBANG 10.0 - Total Precipitation 1951-01", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	String abs = md.getDataIdentification().getAbstract();
	Assert.assertTrue(abs.startsWith("Monthly total precipitation grid, January 1951.\n\nGrid of the collection \"BIGBANG 10.0 - Total Precipitation (TP)\"."));

	TemporalExtent extent = md.getDataIdentification().getTemporalExtent();
	Assert.assertEquals("1951-01-01T00:00:00Z", extent.getBeginPosition());
	Assert.assertEquals("1951-01-31T23:59:59Z", extent.getEndPosition());

	GeographicBoundingBox bbox = md.getDataIdentification().getGeographicBoundingBoxes().next();
	Assert.assertEquals(47.413586, bbox.getNorth(), 0.000001);
	Assert.assertEquals(4.434638, bbox.getWest(), 0.000001);

	Assert.assertEquals("EPSG:3035", md.getReferenceSystemInfos().next().getCode());

	CoverageDescription coverage = md.getCoverageDescriptions().next();
	Assert.assertEquals("TP", coverage.getAttributeIdentifier());
	Assert.assertEquals("Total Precipitation (mm)", coverage.getAttributeDescription());

	Assert.assertEquals("millimetre", resource.getExtensionHandler().getAttributeUnits().get());
	Assert.assertEquals("mm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());

	Assert.assertEquals("Italian Open Data License v2.0 (IODL 2.0)", md.getDataIdentification().getLegalConstraintsUseLimitations().next());

	List<ResponsibleParty> contacts = new ArrayList<>();
	md.getDataIdentification().getPointOfContacts().forEachRemaining(contacts::add);
	Assert.assertEquals(3, contacts.size());
	Assert.assertEquals("Giovanni Braca", contacts.get(1).getIndividualName());

	// online resources: the grid zip, 5 documents, via, license, STAC item
	List<Online> onlines = new ArrayList<>();
	md.getDistribution().getDistributionOnlines().forEachRemaining(onlines::add);
	Assert.assertEquals(9, onlines.size());

	Online data = onlines.get(0);
	Assert.assertEquals("https://boldrini.essi-lab.eu/data/ASCII_GRID/TOTAL_PRECIPITATION/MONTHLY/tp_1951_01c.zip", data.getLinkage());
	Assert.assertEquals("download", data.getFunctionCode());

	Assert.assertEquals("https://boldrini.essi-lab.eu/stac/bigbang100-total-precipitation/items/tp_1951_01c.json",
		onlines.get(onlines.size() - 1).getLinkage());
    }

    @Test
    public void variableFallbackTest() {

	Assert.assertEquals("10.0", BigBangSTACMapper.readVersion("BIGBANG 10.0 - PR-AE (DF)").get());
	Assert.assertEquals("mm", BigBangSTACMapper.readUnit(new org.json.JSONObject("{\"assets\":{\"a\":{\"unit\":\"mm\"}}}")));
	Assert.assertEquals("millimetre per metre", BigBangSTACMapper.readUnitName("mm/m"));
	Assert.assertEquals("degree Celsius", BigBangSTACMapper.readUnitName("°C"));
	Assert.assertEquals("dimensionless", BigBangSTACMapper.readUnitName("1"));
	Assert.assertEquals("kg", BigBangSTACMapper.readUnitName("kg"));
	Assert.assertNull(BigBangSTACMapper.summarizeItems(new org.json.JSONObject("{\"links\":[]}")));
    }
}
