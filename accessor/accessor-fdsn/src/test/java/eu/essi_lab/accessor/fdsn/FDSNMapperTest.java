package eu.essi_lab.accessor.fdsn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import junit.framework.TestCase;

/**
 * @author ilsanto
 * @author boldrini
 */
public class FDSNMapperTest {

    private static final Double TOL = 0.0000000001;

    @Test
    public void testFDSNServiceLoaderIResourceMapperAccessor() {

	PluginsLoader<IResourceMapper> pluginsLoader = new PluginsLoader<>();
	List<IResourceMapper> mappers = pluginsLoader.loadPlugins(IResourceMapper.class);

	boolean found = mappers.stream().//
		anyMatch(mapper -> FDSNMapper.class.isAssignableFrom(mapper.getClass()));

	Assert.assertTrue("Can not find " + FDSNMapper.class + " via Java Service Loader of class " + IResourceMapper.class, found);
    }

    @Test
    public void testMapperFromExample() throws IOException, GSException {

	InputStream stream = FDSNMapperTest.class.getClassLoader().getResourceAsStream("test-quakeml-1.xml");

	TestCase.assertNotNull(stream);

	String metadata = IOUtils.toString(stream);

	stream.close();

	FDSNMapper mapper = new FDSNMapper();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(metadata);

	HarmonizedMetadata harmonizedMetadata = mapper.map(originalMD, new GSSource()).getHarmonizedMetadata();

	TestCase.assertNotNull(harmonizedMetadata);

	// Core metadata tests

	CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	// Metadata tests

	TestCase.assertEquals("dataset", miMetadata.getHierarchyLevelName());

	TestCase.assertEquals("eng", miMetadata.getLanguage());

	TestCase.assertEquals(null, miMetadata.getFileIdentifier());

	// Data identification tests

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	TestCase.assertEquals("2017-03-15T23:29:48Z", dataIdentification.getCitationCreationDate());

	TestCase.assertEquals("2017-03-15T23:29:48Z", dataIdentification.getTemporalExtent().getBeginPosition());

	TestCase.assertEquals("2017-03-15T23:29:48Z", dataIdentification.getTemporalExtent().getEndPosition());

	String latitude = "35.863";
	TestCase.assertEquals(latitude, dataIdentification.getGeographicBoundingBox().getSouth().toString());
	TestCase.assertEquals(latitude, dataIdentification.getGeographicBoundingBox().getNorth().toString());

	String longitude = "27.6151";
	TestCase.assertEquals(longitude, dataIdentification.getGeographicBoundingBox().getEast().toString());
	TestCase.assertEquals(longitude, dataIdentification.getGeographicBoundingBox().getWest().toString());

	TestCase.assertEquals(16920, dataIdentification.getVerticalExtent().getMinimumValue(), TOL);
	TestCase.assertEquals(16920, dataIdentification.getVerticalExtent().getMaximumValue(), TOL);

	String title = "earthquake of magnitude 4.3 (magnitude type mb) localized in  at lat: 35.863; lon: 27.6151; depth: 16.92 km depth";

	TestCase.assertEquals(title, dataIdentification.getCitationTitle());

	String abs = "earthquake of magnitude 4.3 (magnitude type mb) localized in  at lat: 35.863; lon: 27.6151; depth: 16.92 km depth";
	TestCase.assertEquals(abs, dataIdentification.getAbstract());

	Iterator<String> keywordsIterator = dataIdentification.getKeywordsValues();
	Set<String> keys = Sets.newHashSet(keywordsIterator);
	TestCase.assertTrue(keys.contains("QuakeML"));

	// Distribution information tests

	Distribution distributionInformation = miMetadata.getDistribution();
	List<Online> onlines = Lists.newArrayList(distributionInformation.getDistributionOnlines());
	TestCase.assertEquals(1, onlines.size());
	Online online = onlines.get(0);

	TestCase.assertEquals("http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20008se3&format=quakeml", online.getLinkage());
	TestCase.assertEquals("HTTP-GET", online.getProtocol());
	TestCase.assertEquals("http://www.essi-lab.eu/broker/accesstypes/direct", online.getDescriptionGmxAnchor());
	TestCase.assertEquals("information", online.getFunctionCode());

	ArrayList<Format> formats = Lists.newArrayList(distributionInformation.getFormats());

	TestCase.assertEquals(1, formats.size());

	Format format = formats.get(0);
	TestCase.assertEquals("QuakeML", format.getName());
	TestCase.assertEquals("1.2", format.getVersion());

    }
}