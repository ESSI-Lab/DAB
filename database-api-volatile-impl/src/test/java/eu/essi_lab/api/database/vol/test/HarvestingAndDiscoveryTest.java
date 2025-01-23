/**
 * 
 */
package eu.essi_lab.api.database.vol.test;

import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.vol.VolatileDatabase;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class HarvestingAndDiscoveryTest {

    private DiscoveryMessage message;
    protected VolatileDatabase volatileDatabase;

    protected static final String SOURCE_1_ID = "source-test-1";
    protected static final String SOURCE_2_ID = "source-test-2";
    protected DatabaseWriter writer;
    protected DatabaseReader reader;
    protected DatabaseFinder finder;
    protected SourceStorage storage;

    protected GSSource source1;
    protected GSSource source2;
    protected String suiteIdentifier;
    protected Database provider;

    private static final StorageInfo TEST_DB_URI = new DatabaseSetting(true).asStorageUri();
    static {
	TEST_DB_URI.setIdentifier("SUITE_ID");
    }

    public HarvestingAndDiscoveryTest() throws GSException {

	source1 = new GSSource();
	source1.setUniqueIdentifier(SOURCE_1_ID);

	source2 = new GSSource();
	source2.setUniqueIdentifier(SOURCE_2_ID);

	try {
	    provider = DatabaseFactory.get(TEST_DB_URI);
	    provider.initialize(TEST_DB_URI);

	} catch (GSException e) {

	    fail("Exception thrown");
	}

	// ----------------------
	//
	// creates the consumers
	//
	writer = DatabaseProviderFactory.getWriter(TEST_DB_URI);
	reader = DatabaseProviderFactory.getReader(TEST_DB_URI);
	finder = DatabaseProviderFactory.getFinder(TEST_DB_URI);
	storage = DatabaseProviderFactory.getSourceStorage(TEST_DB_URI);

	volatileDatabase = (VolatileDatabase) provider;
    }

    @Test
    public void test() throws Exception {

	//
	// disabling feature
	//
	// SourceStorageWorker.enableSmartHarvestingFinalizationFeature(false);

	message = new DiscoveryMessage();

	try {

	    // clear the database (low level call)
	    volatileDatabase.removeFolders();

	    notHarvestedSourceTest();

	    sourceConstraintsTest();

	    sourceAndTitleTest();

	    resourceExistsTest();

	    nonHarvestedSourceAndTitleTest();

	    // clear the database (low level call)
	    volatileDatabase.removeFolders();

	    // minMaxResourceTimeStampTest();
	    // minMaxtTitleTest();

	    // clear the database (low level call)
	    volatileDatabase.removeFolders();

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private void resourceExistsTest() throws GSException {

	// creates the sources directories
	storage.harvestingStarted(source1, HarvestingStrategy.FULL, false, false);
	storage.harvestingStarted(source2, HarvestingStrategy.FULL, false, false);

	// stores some resources
	String title1 = "title1";
	String title2 = "title2";

	ResourceDescriptor d1 = new ResourceDescriptor();
	d1.setTitle(title1);
	String privateId1 = UUID.randomUUID().toString();
	d1.setIdentitifer(privateId1);

	OriginalMetadata originalMetadata1 = new OriginalMetadata();
	d1.setOriginalId("originalId1");
	d1.setOriginalMetadata(originalMetadata1);

	ResourceDescriptor d2 = new ResourceDescriptor();
	d2.setTitle(title2);
	String privateId2 = UUID.randomUUID().toString();
	d2.setIdentitifer(privateId2);

	OriginalMetadata originalMetadata2 = new OriginalMetadata();
	d2.setOriginalId("originalId2");
	d2.setOriginalMetadata(originalMetadata2);

	storeResources(d1, source1, 1); // "source1", "originalId1"
	storeResources(d1, source2, 1); // "source2", "originalId1"

	storeResources(d2, source1, 1); // "source1", "originalId2"
	storeResources(d2, source2, 1); // "source2", "originalId2"

	// finalization
	storage.harvestingEnded(source1, HarvestingStrategy.FULL);
	storage.harvestingEnded(source2, HarvestingStrategy.FULL);

	{
	    boolean resourceExists = reader.resourceExists(IdentifierType.PRIVATE, privateId1 + "_0");
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResources(IdentifierType.PRIVATE, privateId1 + "_0").get(0);
	    Assert.assertNotNull(resource);

	}
	{
	    boolean resourceExists = reader.resourceExists(IdentifierType.PRIVATE, privateId2 + "_0");
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResources(IdentifierType.PRIVATE, privateId2 + "_0").get(0);
	    Assert.assertNotNull(resource);

	}
	{
	    boolean resourceExists = reader.resourceExists("originalId1", source1);
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResource("originalId1", source1);
	    Assert.assertNotNull(resource);
	}
	{
	    boolean resourceExists = reader.resourceExists("originalId2", source1);
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResource("originalId2", source1);
	    Assert.assertNotNull(resource);
	}
	{
	    boolean resourceExists = reader.resourceExists("originalId1", source2);
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResource("originalId1", source2);
	    Assert.assertNotNull(resource);
	}
	{
	    boolean resourceExists = reader.resourceExists("originalId2", source2);
	    Assert.assertTrue(resourceExists);

	    GSResource resource = reader.getResource("originalId2", source2);
	    Assert.assertNotNull(resource);
	}
	{
	    boolean resourceExists = reader.resourceExists("originalId3", source2);
	    Assert.assertFalse(resourceExists);
	}

    }

    public void notHarvestedSourceTest() throws Exception {

	ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond("none");

	message.setPermittedBond(sourceBond);
	message.setPage(new Page(10));

	ResultSet<GSResource> resultSet = finder.discover(message);

	List<GSResource> results = resultSet.getResultsList();

	Assert.assertEquals(results.size(), 0);

	DiscoveryCountResponse countResult = finder.count(message);
	checkMap(countResult, 0);

	Assert.assertEquals(countResult.getCount(), 0);
    }

    public void sourceConstraintsTest() throws Exception {

	// creates the sources directories
	storage.harvestingStarted(source1, HarvestingStrategy.FULL, false, false);
	storage.harvestingStarted(source2, HarvestingStrategy.FULL, false, false);

	// stores some resources
	ResourceDescriptor d1 = new ResourceDescriptor();
	d1.setIdentitifer(UUID.randomUUID().toString());

	ResourceDescriptor d2 = new ResourceDescriptor();
	d2.setIdentitifer(UUID.randomUUID().toString());

	storeResources(d1, source1, 5);
	storeResources(d2, source2, 7);

	// finalization
	storage.harvestingEnded(source1, HarvestingStrategy.FULL);
	storage.harvestingEnded(source2, HarvestingStrategy.FULL);

	message.setSources(Arrays.asList(source1, source2));

	// test
	// sourceConstraintsTest(SOURCE_1_ID, 5, false);
	// sourceConstraintsTest(SOURCE_2_ID, 7, false);
	// sourceConstraintsTest(SOURCE_1_ID, 5, true);
	// sourceConstraintsTest(SOURCE_2_ID, 7, true);

	sourceConstraintsTest(SOURCE_1_ID, 10, false);
	sourceConstraintsTest(SOURCE_2_ID, 10, false);
	sourceConstraintsTest(SOURCE_1_ID, 12, true);
	sourceConstraintsTest(SOURCE_2_ID, 12, true);
    }

    public void nonHarvestedSourceAndTitleTest() throws Exception {

	// stores some resources
	String title1 = "title1";
	String title2 = "title2";

	// test
	// sourceAndTitleTest("none", title1, 0, false);
	// sourceAndTitleTest("none", title1, 0, false);
	//
	// sourceAndTitleTest("none", title2, 0, true);
	// sourceAndTitleTest("none", title2, 0, true);

	sourceAndTitleTest("none", title1, 10, false);
	sourceAndTitleTest("none", title1, 10, false);

	sourceAndTitleTest("none", title2, 32, true);
	sourceAndTitleTest("none", title2, 32, true);
    }

    public void sourceAndTitleTest() throws Exception {

	// creates the sources directories
	storage.harvestingStarted(source1, HarvestingStrategy.FULL, false, false);
	storage.harvestingStarted(source2, HarvestingStrategy.FULL, false, false);

	// stores some resources
	String title1 = "title1";
	String title2 = "title2";

	ResourceDescriptor d1 = new ResourceDescriptor();
	d1.setTitle(title1);
	d1.setIdentitifer(UUID.randomUUID().toString());

	ResourceDescriptor d2 = new ResourceDescriptor();
	d2.setTitle(title2);
	d2.setIdentitifer(UUID.randomUUID().toString());

	storeResources(d1, source1, 3);
	storeResources(d1, source2, 3);

	storeResources(d2, source1, 5);
	storeResources(d2, source2, 5);

	// finalization
	storage.harvestingEnded(source1, HarvestingStrategy.FULL);
	storage.harvestingEnded(source2, HarvestingStrategy.FULL);

	// test
	// sourceAndTitleTest(SOURCE_1_ID, title1, 3, false);
	// sourceAndTitleTest(SOURCE_2_ID, title1, 3, false);
	//
	// sourceAndTitleTest(SOURCE_1_ID, title2, 5, true);
	// sourceAndTitleTest(SOURCE_2_ID, title2, 5, true);

	sourceAndTitleTest(SOURCE_1_ID, title1, 10, false);
	sourceAndTitleTest(SOURCE_2_ID, title1, 10, false);

	sourceAndTitleTest(SOURCE_1_ID, title2, 28, true);
	sourceAndTitleTest(SOURCE_2_ID, title2, 28, true);
    }

    public void minMaxResourceTimeStampTest() throws Exception {

	// creates the sources directories
	storage.harvestingStarted(source1, HarvestingStrategy.FULL, false, false);

	// stores some resources
	ResourceDescriptor rd1 = new ResourceDescriptor();
	rd1.setResourceTimeStamp(ISO8601DateTimeUtils.getISO8601DateTime(2000, 1, 1));
	rd1.setIdentitifer(UUID.randomUUID().toString());

	ResourceDescriptor rd2 = new ResourceDescriptor();
	rd2.setResourceTimeStamp(ISO8601DateTimeUtils.getISO8601DateTime(2001, 1, 1));
	rd2.setIdentitifer(UUID.randomUUID().toString());

	ResourceDescriptor rd3 = new ResourceDescriptor();
	rd3.setResourceTimeStamp(ISO8601DateTimeUtils.getISO8601DateTime(2003, 1, 1));
	rd3.setIdentitifer(UUID.randomUUID().toString());

	storeResources(rd1, source1, 1);
	storeResources(rd2, source1, 1);
	storeResources(rd3, source1, 1);

	// finalization
	storage.harvestingEnded(source1, HarvestingStrategy.FULL);

	// -------------------------------
	//
	// test min resource time stamp
	//
	ResourcePropertyBond minTimeStampBond = BondFactory.createMinMaxResourceTimeStampBond(BondOperator.MIN);

	message.setPermittedBond(minTimeStampBond);
	message.setPage(new Page(1));

	ResultSet<GSResource> resultSet = finder.discover(message);

	List<GSResource> results = resultSet.getResultsList();
	GSResource gsResource = results.get(0);

	String resTimeStamp = gsResource.getIndexesMetadata().read(ResourceProperty.RESOURCE_TIME_STAMP).get();

	Assert.assertTrue(resTimeStamp.startsWith("2000-01-01"));

	Assert.assertEquals(1, results.size());

	DiscoveryCountResponse countResult = finder.count(message);

	checkMap(countResult, 0);

	Assert.assertEquals(1, countResult.getCount());

	// -------------------------------
	//
	// test max resource time stamp
	//
	ResourcePropertyBond maxTimeStampBond = BondFactory.createMinMaxResourceTimeStampBond(BondOperator.MAX);

	message.setPermittedBond(maxTimeStampBond);
	message.setPage(new Page(1));

	resultSet = finder.discover(message);

	results = resultSet.getResultsList();
	gsResource = results.get(0);

	resTimeStamp = gsResource.getIndexesMetadata().read(ResourceProperty.RESOURCE_TIME_STAMP).get();

	Assert.assertTrue(resTimeStamp.startsWith("2003-01-01"));

	Assert.assertEquals(1, results.size());

	countResult = finder.count(message);

	checkMap(countResult, 0);

	Assert.assertEquals(1, countResult.getCount());

    }

    // public void minMaxtTitleTest() throws Exception {
    //
    // // stores some resources
    // ResourceDescriptor rd1 = new ResourceDescriptor();
    // rd1.setTitle("title1");
    // rd1.setIdentitifer(UUID.randomUUID().toString());
    //
    // ResourceDescriptor rd2 = new ResourceDescriptor();
    // rd2.setTitle("title2");
    // rd2.setIdentitifer(UUID.randomUUID().toString());
    //
    // ResourceDescriptor rd3 = new ResourceDescriptor();
    // rd3.setTitle("title3");
    // rd3.setIdentitifer(UUID.randomUUID().toString());
    //
    // storeResources(rd1, source1, 1);
    // storeResources(rd2, source1, 1);
    // storeResources(rd3, source1, 1);
    //
    // // finalization
    // storage.harvestingEnded(source1, HarvestingStrategy.FULL);
    //
    // // test min title
    // SimpleElementBond minTimeStampBond = new SimpleElementBond(BondOperator.MIN, MetadataElement.TITLE);
    //
    // message.setPermittedBond(minTimeStampBond);
    // message.setPage(new Page(1));
    //
    // ResultSet<GSResource> resultSet = finder.discover(message);
    // checkMap(resultSet, 0);
    //
    // List<GSResource> results = resultSet.getResultsList();
    // GSResource gsResource = results.get(0);
    //
    // String title = gsResource.getHarmonizedMetadata().getCoreMetadata().getTitle();
    // Assert.assertEquals(title, "title1");
    //
    // Assert.assertEquals(results.size(), 1);
    //
    // int count = finder.count(message);
    // Assert.assertEquals(count, 1);
    //
    // // -------------------------------
    // //
    // // test max title
    // //
    // SimpleElementBond maxTimeStampBond = new SimpleElementBond(BondOperator.MAX, MetadataElement.TITLE);
    //
    // message.setPermittedBond(maxTimeStampBond);
    // message.setPage(new Page(1));
    //
    // resultSet = finder.discover(message);
    // checkMap(resultSet, 0);
    //
    // results = resultSet.getResultsList();
    // gsResource = results.get(0);
    //
    // title = gsResource.getHarmonizedMetadata().getCoreMetadata().getTitle();
    // Assert.assertEquals(title, "title3");
    //
    // Assert.assertEquals(results.size(), 1);
    //
    // count = finder.count(message);
    // Assert.assertEquals(count, 1);
    // }

    protected static class ResourceDescriptor {

	private String identitifer;
	private String title;
	private String abstract_;
	private GeographicBoundingBox bbox;
	private TemporalExtent temporal;
	private String resourceTimeStamp;
	private String format;
	private OriginalMetadata originalMetadata;
	private String originalId;
	private double distanceValue;
	private boolean hasLegal;
	private Integer denominator;

	public double getDistanceValue() {
	    return distanceValue;
	}

	public void setDistanceValue(double distanceValue) {
	    this.distanceValue = distanceValue;
	}

	public String getOriginalId() {
	    return originalId;
	}

	public void setOriginalId(String originalId) {
	    this.originalId = originalId;
	}

	public String getFormat() {
	    return format;
	}

	public void setFormat(String format) {
	    this.format = format;
	}

	public String getResourceTimeStamp() {
	    return resourceTimeStamp;
	}

	public void setResourceTimeStamp(String resourceTimeStamp) {
	    this.resourceTimeStamp = resourceTimeStamp;
	}

	public String getIdentitifer() {
	    return identitifer;
	}

	public void setIdentitifer(String identitifer) {
	    this.identitifer = identitifer;
	}

	public String getTitle() {
	    return title;
	}

	public void setTitle(String title) {
	    this.title = title;
	}

	public String getAbstract() {
	    return abstract_;
	}

	public void setAbstract(String abstract_) {
	    this.abstract_ = abstract_;
	}

	public GeographicBoundingBox getBbox() {
	    return bbox;
	}

	public void setBbox(GeographicBoundingBox bbox) {
	    this.bbox = bbox;
	}

	public TemporalExtent getTemporal() {
	    return temporal;
	}

	public void setTemporal(TemporalExtent temporal) {
	    this.temporal = temporal;
	}

	public OriginalMetadata getOriginalMetadata() {
	    return originalMetadata;
	}

	public void setOriginalMetadata(OriginalMetadata originalMetadata) {
	    this.originalMetadata = originalMetadata;
	}

	public void setHasUseLegalConstraints(boolean hasSecurity) {
	    this.hasLegal = hasSecurity;
	}

	public boolean getHasUseLegalConstraints() {
	    return hasLegal;
	}

	public void setDenominator(int denominator) {
	    this.denominator = denominator;
	}

	public Integer getDenominator() {
	    return denominator;
	}

    }

    protected void storeResources(ResourceDescriptor rd, GSSource source, int count, int quality) throws GSException {

	storeResources(rd, source, count, quality, -1);
    }

    protected void storeResources(ResourceDescriptor rd, GSSource source, int count, int quality, int score) throws GSException {

	for (int i = 0; i < count; i++) {

	    Dataset dataset = new Dataset();
	    dataset.setSource(source);
	    dataset.setPrivateId(rd.getIdentitifer() + "_" + i);
	    dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(rd.getIdentitifer());

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    dataset.setOriginalId(rd.getOriginalId());

	    if (rd.getOriginalMetadata() != null) {
		dataset.setOriginalMetadata(rd.getOriginalMetadata());
	    } else {
		dataset.setOriginalMetadata(originalMetadata);
	    }

	    // ----------------------------
	    //
	    // set the properties
	    //
	    String title = rd.getTitle();
	    if (title != null) {
		dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);
	    }

	    String abs = rd.getAbstract();
	    if (abs != null) {
		dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract(abs);
	    }

	    boolean hasUseLegal = rd.getHasUseLegalConstraints();
	    if (hasUseLegal) {
		LegalConstraints legalConstraints = new LegalConstraints();
		legalConstraints.addUseConstraintsCode("code");
		dataset.getHarmonizedMetadata().//
			getCoreMetadata().//
			getMIMetadata().//
			getDataIdentification().//
			addLegalConstraints(legalConstraints);
	    }

	    MDResolution resolution = new MDResolution();

	    dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().setSpatialResolution(resolution);

	    double distanceValue = rd.getDistanceValue();

	    resolution.setDistance(null, distanceValue);

	    Integer denominator = rd.getDenominator();
	    if (denominator != null) {
		resolution.setEquivalentScale(new BigInteger("" + denominator));
	    }

	    GeographicBoundingBox bbox = rd.getBbox();
	    if (bbox != null) {
		dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addGeographicBoundingBox(bbox);
	    }

	    TemporalExtent temporal = rd.getTemporal();
	    if (temporal != null) {
		dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addTemporalExtent(temporal);
	    }

	    // set the indexes
	    IndexedElementsWriter.write(dataset);
	    if (quality > 0) {
		// overrides the quality
		dataset.getIndexesMetadata().remove(ResourceProperty.METADATA_QUALITY.getName());

		dataset.getIndexesMetadata()
			.write(new IndexedResourceProperty(ResourceProperty.METADATA_QUALITY, String.valueOf(quality)));

	    }
	    if (rd.getResourceTimeStamp() != null) {
		// overrides the resource time stamp
		dataset.getIndexesMetadata().remove(ResourceProperty.RESOURCE_TIME_STAMP.getName());

		dataset.getIndexesMetadata().write(
			new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(rd.getResourceTimeStamp())));
	    }

	    if (score > -1) {

		dataset.getPropertyHandler().setSSCSCore(score);
	    }

	    // -------------------------------------
	    //
	    // stores the resource
	    //
	    writer.store(dataset);
	}
    }

    protected void storeResources(ResourceDescriptor rd, GSSource source, int count) throws GSException {

	storeResources(rd, source, count, -1);
    }

    //
    // ----------------------------------------------------------------------------------------------------------------------
    //

    private void sourceConstraintsTest(String sourceId, int expectedResources, boolean count) throws GSException {

	ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);

	message.setPermittedBond(sourceBond);
	message.setPage(new Page(10));

	if (!count) {

	    ResultSet<GSResource> resultSet = finder.discover(message);

	    List<GSResource> results = resultSet.getResultsList();

	    Assert.assertEquals(expectedResources, results.size());
	} else {
	    DiscoveryCountResponse countResult = finder.count(message);

	    checkMap(countResult, 0);

	    Assert.assertEquals(expectedResources, countResult.getCount());
	}
    }

    private void sourceAndTitleTest(String sourceId, String title, int expectedResources, boolean count) throws GSException {

	ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);
	SimpleValueBond simpleElBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, title);
	LogicalBond logicalBond = BondFactory.createAndBond(sourceBond, simpleElBond);

	message.setPermittedBond(logicalBond);
	message.setPage(new Page(10));

	if (!count) {

	    ResultSet<GSResource> resultSet = finder.discover(message);

	    List<GSResource> results = resultSet.getResultsList();

	    Assert.assertEquals(expectedResources, results.size());
	} else {
	    DiscoveryCountResponse countResult = finder.count(message);

	    // checkMap(resultSet, 0);

	    Assert.assertEquals(expectedResources, countResult.getCount());
	}
    }

    private void enhanceMessage(Bond bond, boolean estimate) {

	if (bond != null) {
	    message.setPermittedBond(bond);
	}
    }

    private void checkMap(DiscoveryCountResponse countResult, int targetsCount) {

	Optional<TermFrequencyMap> map = countResult.getTermFrequencyMap();
	// Assert.assertFalse(map.isPresent());
	// Assert.assertEquals(targetsCount, map.get().getTargetsCount());
    }

}
