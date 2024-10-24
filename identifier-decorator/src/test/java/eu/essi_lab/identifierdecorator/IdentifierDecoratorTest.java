//package eu.essi_lab.identifierdecorator;
//
//import java.util.Map;
//import java.util.UUID;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.mockito.Mockito;
//
//import eu.essi_lab.api.database.DatabaseReader;
//import eu.essi_lab.api.database.DatabaseWriter;
//import eu.essi_lab.model.GSSource;
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.model.resource.Dataset;
//import eu.essi_lab.model.resource.GSResource;
//import eu.essi_lab.model.resource.OriginalMetadata;
//
///**
// * Given an HarmonizedMetadata model IdentifierDecorator returns back a PublicId
// * value. The assert 'exitsts in db' means there is an existing resource in the
// * database who: existing_resource.publicId == resource.OID. The assert 'OID in
// * database' means there is an existing resource in the database who:
// * existing_resource.OID == resource.OID and existing_resource.source ==
// * resource.source.
// * 
// * @author pezzati
// */
//public class IdentifierDecoratorTest {
//
//    @Rule
//    public ExpectedException expectedException = ExpectedException.none();
//    private IIdentifierDecorator identifierDecorator;
//    private SourcePriorityDocument sourcePriorityDoc;
//    private DatabaseReader dbReader;
//    private DatabaseWriter dbWriter;
//    private GSResource resource;
//    private OriginalMetadata originalMetadata;
//    private GSSource gSSource;
//
//    @Before
//    public void init() {
//	sourcePriorityDoc = Mockito.mock(SourcePriorityDocument.class);
//	dbReader = Mockito.mock(DatabaseReader.class);
//	dbWriter = Mockito.mock(DatabaseWriter.class);
//	gSSource = Mockito.spy(new GSSource());
//	originalMetadata = Mockito.spy(new OriginalMetadata());
//	resource = Mockito.spy(new Dataset());
//	Mockito.when(resource.getOriginalMetadata()).thenReturn(originalMetadata);
//	Mockito.when(resource.getSource()).thenReturn(gSSource);
//	identifierDecorator = Mockito.spy(new IdentifierDecorator(sourcePriorityDoc, dbReader, dbWriter));
//    }
//    
//    @Test
//    public void checkSSS(){
//	
//    }
//
//    @Test
//    public void checkEmptyConstructorAndGetterAndSetters() {
//	identifierDecorator = new IdentifierDecorator();
//	identifierDecorator.setDbReader(null);
//	identifierDecorator.setDbWriter(null);
//	identifierDecorator.setSourcePriorityDocument(null);
//	Assert.assertNull(identifierDecorator.getDbReader());
//	Assert.assertNull(identifierDecorator.getDbWriter());
//	Assert.assertNull(identifierDecorator.getSourcePriorityDocument());
//    }
//
//    @Test
//    public void getIdentifierAboutDistributedNullMetadata() throws GSException {
//	expectedException.expect(NullPointerException.class);
//	identifierDecorator.decorateDistributedIdentifier(null);
//    }
//
//    @Test
//    public void getIdentifierAboutHarvestedNullMetadata() throws GSException {
//	expectedException.expect(NullPointerException.class);
//	identifierDecorator.decorateHarvestedIdentifier(null);
//    }
//
//    /**
//     * Test checks that given distributed resource public identifier and private
//     * identifier will be set with the same identifier. publicId = privateId =
//     * UUID.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void getIdentifierAboutDistributedValidMetadata() throws GSException {
//	String expectedPrivateIdentifier = UUID.randomUUID().toString();
//	String expectedPublicIdentifier = expectedPrivateIdentifier;
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(expectedPrivateIdentifier);
//	identifierDecorator.decorateDistributedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Assert.assertEquals(expectedPrivateIdentifier, actualPrivateIdentifier);
//	Assert.assertEquals(expectedPublicIdentifier, actualPublicIdentifier);
//	Assert.assertEquals(actualPrivateIdentifier, actualPublicIdentifier);
//    }
//
//    /**
//     * Test checks that resource who does not exists in database and is not
//     * present in the priority doc will have its private and public identifiers
//     * set this way: publicId = privateId = UUID.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void assignIdentifiersToResourceNotInPriorityQueueAndNotExistingsInDatabase() throws GSException {
//	String uniqueId = UUID.randomUUID().toString();
//	String expectedPrivateIdentifier = uniqueId;
//	String expectedPublicIdentifier = expectedPrivateIdentifier;
//	Mockito.when(resource.getOriginalId()).thenReturn("resourceOriginalId");
//	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(false);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId())).thenReturn(false);
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(uniqueId);
//	identifierDecorator.decorateHarvestedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(1)).generateUniqueIdentifier();
//	Assert.assertEquals(expectedPrivateIdentifier, actualPrivateIdentifier);
//	Assert.assertEquals(expectedPublicIdentifier, actualPublicIdentifier);
//	Assert.assertEquals(actualPrivateIdentifier, actualPublicIdentifier);
//    }
//    
//    /**
//     * Test checks that resource who does not exists in database and is not
//     * present in the priority doc will have its private and public identifiers
//     * set this way: publicId = privateId = UUID.
//     * 
//     * @throws GSException
//     */
////    @Test
////    public void assignIdentifiersToResourceNotInPriorityQueueAndNotExistingsInDatabaseNEW() throws GSException {
////	String uniqueId = UUID.randomUUID().toString();
////	String expectedPrivateIdentifier = uniqueId;
////	String expectedPublicIdentifier = expectedPrivateIdentifier;
////	Mockito.when(resource.getOriginalId()).thenReturn(uniqueId);
////	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(false);
////	Mockito.when(dbReader.resourceExists(resource.getOriginalId())).thenReturn(false);
////	//Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(uniqueId);
////	identifierDecorator.decorateHarvestedIdentifier(resource);
////	String actualPrivateIdentifier = resource.getPrivateId();
////	String actualPublicIdentifier = resource.getPublicId();
////	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(0)).generateUniqueIdentifier();
////	Assert.assertEquals(expectedPrivateIdentifier, actualPrivateIdentifier);
////	Assert.assertEquals(expectedPublicIdentifier, actualPublicIdentifier);
////	Assert.assertEquals(actualPrivateIdentifier, actualPublicIdentifier);
////    }
//
//    /**
//     * Test checks that resource who exists in database but is not in the
//     * priority doc will have its private and public identifier set this way:
//     * publicId = privateId = UUID.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void assignIdentifiersToResourceNotInPriorityQueueAndExistingInDatabase() throws GSException {
//	String resourceId = "existingResourcePublicId";
//	String expectedPrivateIdentifier = "expectedGeneartedUniqueIdentifier";
//	String expectedPublicIdentifier = expectedPrivateIdentifier;
//	GSResource existingGSResource = Mockito.mock(GSResource.class);
//	Mockito.when(existingGSResource.getPrivateId()).thenReturn("existingResourcePrivateId");
//	Mockito.when(existingGSResource.getPublicId()).thenReturn(resourceId);
//	Mockito.when(existingGSResource.getSource()).thenReturn(gSSource);
//	Mockito.when(resource.getOriginalId()).thenReturn(resourceId);
//	Mockito.when(resource.getOriginalMetadata()).thenReturn(originalMetadata);
//	Mockito.when(resource.getSource()).thenReturn(gSSource);
//	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(false);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId(), resource.getSource())).thenReturn(true);
//	Mockito.when(dbReader.getResource(resource.getOriginalId(), resource.getSource())).thenReturn(existingGSResource);
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(expectedPrivateIdentifier);
//	identifierDecorator.decorateHarvestedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(1)).generateUniqueIdentifier();
//	Assert.assertEquals(expectedPrivateIdentifier, actualPrivateIdentifier);
//	Assert.assertEquals(expectedPublicIdentifier, actualPublicIdentifier);
//	Assert.assertEquals(actualPrivateIdentifier, actualPublicIdentifier);
//    }
//
//    /**
//     * Test checks that resource who doesn't exists in database but is in the
//     * priority doc and it's OID is not in database will have its private and
//     * public identifier set this way: publicId = OID, privateId = UUID.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void assignIdentifiersToResourceInPriorityQueueNotExistingsInDatabaseAndOIDNotInDatabase() throws GSException {
//	String resourceId = "existingResourcePublicId";
//	String randomId = UUID.randomUUID().toString();
//	String expectedPrivateIdentifier = randomId;
//	String expectedPublicIdentifier = resourceId;
//	Mockito.when(resource.getOriginalId()).thenReturn(resourceId);
//	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(true);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId(), resource.getSource())).thenReturn(false);
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(randomId);
//	identifierDecorator.decorateHarvestedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(1)).generateUniqueIdentifier();
//	Assert.assertEquals(expectedPrivateIdentifier, actualPrivateIdentifier);
//	Assert.assertEquals(expectedPublicIdentifier, actualPublicIdentifier);
//    }
//
//    /**
//     * Test checks that resource who doesn't exists in database but is in the
//     * priority doc and it's OID is in database with LOW priority will have its
//     * private and public identifier set this way: publicId = privateId = UUID.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void assignIdentifiersToResourceInPriorityQueueNotExistingsInDatabaseAndOIDInDatabaseWithLowPriority() throws GSException {
//	String resourceId = "existingResourcePublicId";
//	String randomId = UUID.randomUUID().toString();
//	String expectedPrivateIdentifier = randomId;
//	String expectedPublicIdentifier = expectedPrivateIdentifier;
//	Mockito.when(resource.getOriginalId()).thenReturn(resourceId);
//	GSSource lowerPrioritySource = Mockito.mock(GSSource.class);
//	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(true);
//	Mockito.when(sourcePriorityDoc.hasGreaterPriorityThan(resource.getSource(), lowerPrioritySource)).thenReturn(false);
//	GSResource lowerPriorityResource = Mockito.mock(GSResource.class);
//	Mockito.when(lowerPriorityResource.getSource()).thenReturn(lowerPrioritySource);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId(), resource.getSource())).thenReturn(false);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId())).thenReturn(true);
//	Mockito.when(dbReader.getResource(resource.getOriginalId())).thenReturn(lowerPriorityResource);
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(randomId);
//	identifierDecorator.decorateHarvestedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(1)).generateUniqueIdentifier();
//    }
//
//    /**
//     * test check that resource who doesn't exists in database but is in the
//     * priority doc and it's OID is in database with HIGH priority will have its
//     * private and public identifier set this way: publicId = OID, privateId =
//     * UUID. Test also checks that existing resource will be saved with its
//     * identifier set this way: existing.publicId = existing.privateId.
//     * 
//     * @throws GSException
//     */
//    @Test
//    public void assignIdentifiersToResourceInPriorityQueueNotExistingsInDatabaseAndOIDInDatabaseWithHighPriority() throws GSException {
//	String resourceId = "existingResourcePublicId";
//	String randomId = UUID.randomUUID().toString();
//	String expectedPrivateIdentifier = randomId;
//	String expectedPublicIdentifier = resourceId;
//	Mockito.when(resource.getOriginalId()).thenReturn(resourceId);
//	GSSource lowerPrioritySource = Mockito.mock(GSSource.class);
//	Mockito.when(sourcePriorityDoc.isPrioritySource(resource.getSource())).thenReturn(true);
//	Mockito.when(sourcePriorityDoc.hasGreaterPriorityThan(resource.getSource(), lowerPrioritySource)).thenReturn(true);
//	GSResource lowerPriorityResource = Mockito.mock(GSResource.class);
//	Mockito.when(lowerPriorityResource.getSource()).thenReturn(lowerPrioritySource);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId(), resource.getSource())).thenReturn(false);
//	Mockito.when(dbReader.resourceExists(resource.getOriginalId())).thenReturn(true);
//	Mockito.when(dbReader.getResource(resource.getOriginalId())).thenReturn(lowerPriorityResource);
//	Mockito.when(((IdentifierDecorator) identifierDecorator).generateUniqueIdentifier()).thenReturn(randomId);
//	identifierDecorator.decorateHarvestedIdentifier(resource);
//	String actualPrivateIdentifier = resource.getPrivateId();
//	String actualPublicIdentifier = resource.getPublicId();
//	Mockito.verify(((IdentifierDecorator) identifierDecorator), Mockito.times(1)).generateUniqueIdentifier();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void identifierDecoratorDoesNotSupportOptions() {
//	GSConfOption<String> option1 = Mockito.mock(GSConfOption.class);
//	GSConfOption<String> option2 = Mockito.mock(GSConfOption.class);
//	((IdentifierDecorator) identifierDecorator).getSupportedOptions().put("Option1", option1);
//	((IdentifierDecorator) identifierDecorator).getSupportedOptions().put("Option2", option2);
//	Map<String, GSConfOption<?>> supportedOptions = ((IdentifierDecorator) identifierDecorator).getSupportedOptions();
//	Assert.assertTrue(supportedOptions.isEmpty());
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void identifierDecoratorDoesNotSetOptions() throws GSException {
//	GSConfOption<String> option1 = Mockito.mock(GSConfOption.class);
//	((IdentifierDecorator) identifierDecorator).onOptionSet(option1);
//    }
//
//    @Test
//    public void identifierDecoratorDoesNotFlushAnything() throws GSException {
//	((IdentifierDecorator) identifierDecorator).onFlush();
//    }
//}
