/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_GSResourceTempExtentTest {

    @Test
    public void onlyTempExtentBeginTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	String dateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(dateTime);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.of(dateTime));

	checkTempExtentBeginPresent(wrapper, true);

	checkTempExtentBeginNow(wrapper, false);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndNow(wrapper, false);

	checkTempExtentEndPresent(wrapper, false);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void tempExtentBeginBeforeNowTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	TemporalExtent temporalExtent = new TemporalExtent();

	FrameValue frameValue = FrameValue.P10D;
	temporalExtent.setBeforeNowBeginPosition(frameValue);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginNow(wrapper, false);

	checkTempExtentBeginBeforeNow(wrapper, Optional.of(frameValue));

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndNow(wrapper, false);

	checkTempExtentEndPresent(wrapper, false);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void tempExtentBeginNowTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setIndeterminateBeginPosition(TimeIndeterminateValueType.NOW);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginNow(wrapper, true);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndNow(wrapper, false);

	checkTempExtentEndPresent(wrapper, false);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void tempExtentBeginNowWithStringValueTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition("now");

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginNow(wrapper, true);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndNow(wrapper, false);

	checkTempExtentEndPresent(wrapper, false);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void tempExtentEndNowTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginNow(wrapper, false);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndPresent(wrapper, false);

	checkTempExtentEndNow(wrapper, true);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void tempExtentEndNowWithStringValueTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setEndPosition("now");

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginNow(wrapper, false);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.empty());

	checkTempExtentEndPresent(wrapper, false);

	checkTempExtentEndNow(wrapper, true);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void onlyTempExtentEndTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	String dateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setEndPosition(dateTime);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.of(dateTime));

	checkTempExtentEndPresent(wrapper, true);

	checkTempExtentEndNow(wrapper, false);

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.empty());

	checkTempExtentBeginNow(wrapper, false);

	checkTempExtentBeginPresent(wrapper, false);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void bothTempExtentTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));
	
	String beginDateTime = ISO8601DateTimeUtils.getISO8601DateTime();
	String endDateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	TemporalExtent temporalExtent = new TemporalExtent();

	temporalExtent.setBeginPosition(beginDateTime);
	temporalExtent.setEndPosition(endDateTime);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addTemporalExtent(temporalExtent);

	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	checkTempExtendBegin(wrapper, Optional.of(beginDateTime));

	checkTempExtentBeginPresent(wrapper, true);

	checkTempExtentBeginBeforeNow(wrapper, Optional.empty());

	checkTempExtentBeginNow(wrapper, false);

	//
	//
	//

	checkTempExtendEnd(wrapper, Optional.of(endDateTime));

	checkTempExtentEndPresent(wrapper, true);

	checkTempExtentEndNow(wrapper, false);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    /**
     * @param wrapper
     * @param beginTime
     */
    private void checkTempExtendBegin(SourceWrapper wrapper, Optional<String> beginTime) {

	checkTempExtent(wrapper, MetadataElement.TEMP_EXTENT_BEGIN, beginTime);
    }

    /**
     * @param wrapper
     * @param endTime
     */
    private void checkTempExtendEnd(SourceWrapper wrapper, Optional<String> endTime) {

	checkTempExtent(wrapper, MetadataElement.TEMP_EXTENT_END, endTime);
    }

    /**
     * @param wrapper
     * @param dateTime
     */
    private void checkTempExtent(SourceWrapper wrapper, MetadataElement el, Optional<String> dateTime) {

	List<String> list = wrapper.getGSResourceProperties(el);

	if (dateTime.isPresent()) {

	    Long longValue = OpenSearchUtils.parseToLong(dateTime.get()).get();

	    Assert.assertEquals(longValue, Long.valueOf(list.get(0)));

	} else {

	    Assert.assertTrue(list.isEmpty());
	}
    }

    /**
     * @param wrapper
     * @param expectPresent
     */
    private void checkTempExtentBeginPresent(SourceWrapper wrapper, boolean expectPresent) {

	checkProperty(wrapper, MetadataElement.TEMP_EXTENT_BEGIN.getName(), expectPresent);
    }

    /**
     * @param wrapper
     * @param expectPresent
     */
    private void checkTempExtentBeginNow(SourceWrapper wrapper, boolean expectPresent) {

	checkProperty(wrapper, IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName(), expectPresent);
    }

    /**
     * @param wrapper
     * @param expectPresent
     */
    private void checkTempExtentBeginBeforeNow(SourceWrapper wrapper, Optional<FrameValue> value) {

	List<String> list = wrapper.getGSResourceProperties(MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW);

	if (value.isPresent()) {

	    Assert.assertEquals(value.get(), FrameValue.valueOf(list.get(0)));

	} else {

	    Assert.assertTrue(list.isEmpty());
	}
    }

    /**
     * @param wrapper
     * @param expectPresent
     */
    private void checkTempExtentEndNow(SourceWrapper wrapper, boolean expectPresent) {

	checkProperty(wrapper, IndexedElements.TEMP_EXTENT_END_NOW.getElementName(), expectPresent);
    }

    /**
     * @param wrapper
     * @param expectPresent
     */
    private void checkTempExtentEndPresent(SourceWrapper wrapper, boolean expectPresent) {

	checkProperty(wrapper, MetadataElement.TEMP_EXTENT_END.getName(), expectPresent);
    }

    /**
     * @param wrapper
     * @param property
     * @param expectPresent
     */
    private void checkProperty(SourceWrapper wrapper, String property, boolean expectPresent) {

	List<String> list = wrapper.getGSResourceProperties(property);

	Assert.assertEquals(expectPresent, list.size() > 0);
    }
}
