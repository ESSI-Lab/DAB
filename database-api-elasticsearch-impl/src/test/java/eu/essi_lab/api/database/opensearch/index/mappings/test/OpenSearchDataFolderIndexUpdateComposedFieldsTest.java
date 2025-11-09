package eu.essi_lab.api.database.opensearch.index.mappings.test;

import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.JavaOptions;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolderIndexUpdateComposedFieldsTest extends OpenSearchTest {

    /**
     *
     */
    private static final JSONObject ORIGINAL_MAPPING = DataFolderMapping.get().getMapping();

    @Before
    public void before() throws GSException, IOException {
    }

    @Test
    public void allFieldsTest() throws GSException, IOException {

	final List<String> originalFields = new JSONObject(ORIGINAL_MAPPING.toString()).//
		getJSONObject("properties").keySet().stream().sorted().toList();

	final int mappingFieldsCount = originalFields.size();

	//
	// set an empty mapping to the data-folder index
	//

	DataFolderMapping dataFolderMapping = spy(DataFolderMapping.class);

	String emptyMapping = """
		{
		  	"dynamic": "true"
		}""";

	when(dataFolderMapping.getMapping()).thenReturn(new JSONObject(emptyMapping));

	try (MockedStatic<DataFolderMapping> mocked = mockStatic(DataFolderMapping.class)) {

	    mocked.when(DataFolderMapping::get).thenReturn(dataFolderMapping);

	    mocked.when(() -> DataFolderMapping.toDateField(anyString())).thenAnswer(invocation -> {
		String arg = invocation.getArgument(0, String.class);
		return arg + "_date";
	    });

	    //
	    // removes all the indexes and recreate them, but without updating the data-folder index
	    // this way, the data-folder index will have no fields according to the mocked DataFolderMapping
	    //

	    clearIndexes();

	    System.setProperty(JavaOptions.INIT_OPENSEARCH_INDEXES.getOption(), "true");

	    System.setProperty(JavaOptions.UPDATE_DATA_FOLDER_INDEX.getOption(), "false");

	    OpenSearchDatabase dataBase = createDataBase();

	    //
	    // expecting no fields in the data-folder index
	    //

	    Set<String> fields = readFields(dataBase.getClient());

	    Assert.assertTrue(fields.isEmpty());

	    //
	    // expecting all the MetadataElement and ResourceProperty fields in the data-folder index
	    // the data-folder index has also other fields that are not added in the checkAndUpdate method
	    // (for example gsResource, writingFolder, etc...)
	    //

	    DataFolderMapping.get().checkAndUpdate(dataBase.getClient());

	    fields = readFields(dataBase.getClient());

	    final List<String> list = MetadataElement.listValues().stream().map(MetadataElement::getName).collect(Collectors.toList());
	    list.addAll(ResourceProperty.listValues().stream().map(ResourceProperty::getName).collect(Collectors.toList()));

	    fields.stream().sorted().forEach(field -> System.out.println(field));
	    list.stream().sorted().forEach(field -> System.out.println(field));

	    Assert.assertTrue(fields.containsAll(list));
	}

    }

    @Test
    public void missingComposedOrganizationTest() throws GSException, IOException {

	//
	// removes the composed element organization and set it to the data-folder index
	//

	JSONObject mockedMapping = new JSONObject(ORIGINAL_MAPPING.toString());

	mockedMapping.getJSONObject("properties").remove(MetadataElement.ORGANIZATION.getName());

	DataFolderMapping dataFolderMapping = spy(DataFolderMapping.class);

	when(dataFolderMapping.getMapping()).thenReturn(new JSONObject(mockedMapping));

	try (MockedStatic<DataFolderMapping> mocked = mockStatic(DataFolderMapping.class)) {

	    mocked.when(DataFolderMapping::get).thenReturn(dataFolderMapping);

	    mocked.when(() -> DataFolderMapping.toDateField(anyString())).thenAnswer(invocation -> {
		String arg = invocation.getArgument(0, String.class);
		return arg + "_date";
	    });

	    //
	    // removes all the indexes and recreate them, but without updating the data-folder index
	    // this way, the data-folder index will have all the fields but organization,
	    // according to the mocked DataFolderMapping
	    //

	    clearIndexes();

	    System.setProperty(JavaOptions.INIT_OPENSEARCH_INDEXES.getOption(), "true");

	    System.setProperty(JavaOptions.UPDATE_DATA_FOLDER_INDEX.getOption(), "false");

	    OpenSearchDatabase dataBase = createDataBase();

	    //
	    // expecting no organization field in the data-folder index
	    //

	    Set<String> fields = readFields(dataBase.getClient());

	    Assert.assertFalse(fields.contains(MetadataElement.ORGANIZATION.getName()));

	    //
	    // expecting also organization field after updating
	    //

	    DataFolderMapping.get().checkAndUpdate(dataBase.getClient());

	    fields = readFields(dataBase.getClient());

	    Assert.assertTrue(fields.contains(MetadataElement.ORGANIZATION.getName()));
	}
    }

    @Test
    public void updatingComposedOrganizationTest() throws GSException, IOException {

	//
	// removes the orgName property from the composed element organization
	// and set it to the data-folder index
	//

	final String fieldName = "orgName";

	JSONObject mockedMapping = new JSONObject(ORIGINAL_MAPPING.toString());

	final JSONObject nestedOrganization = mockedMapping.getJSONObject("properties")
		.getJSONObject(MetadataElement.ORGANIZATION.getName());

	nestedOrganization.getJSONObject("properties").remove(fieldName);
	nestedOrganization.getJSONObject("properties").remove(IndexMapping.toKeywordField(fieldName));

	DataFolderMapping dataFolderMapping = spy(DataFolderMapping.class);

	when(dataFolderMapping.getMapping()).thenReturn(new JSONObject(mockedMapping));

	when(dataFolderMapping.getMappingStream()).thenReturn(IOStreamUtils.asStream(mockedMapping.toString()));

	try (MockedStatic<DataFolderMapping> mocked = mockStatic(DataFolderMapping.class)) {

	    mocked.when(DataFolderMapping::get).thenReturn(dataFolderMapping);

	    mocked.when(() -> DataFolderMapping.toDateField(anyString())).thenAnswer(invocation -> {
		String arg = invocation.getArgument(0, String.class);
		return arg + "_date";
	    });

	    //
	    // removes all the indexes and recreate them, but without updating the data-folder index
	    // this way, the data-folder index will have the organization field missing the
	    // orgName nested field according to the mocked mapping
	    //

	    clearIndexes();

	    System.setProperty(JavaOptions.INIT_OPENSEARCH_INDEXES.getOption(), "true");

	    System.setProperty(JavaOptions.UPDATE_DATA_FOLDER_INDEX.getOption(), "false");

	    OpenSearchDatabase dataBase = createDataBase();

	    //
	    // expecting organization field missing the orgName nested field in the data-folder index
	    //

	    JSONObject orgNested = OpenSearchUtils.toJSONObject(
		    readFieldsToProperties(dataBase.getClient()).get(MetadataElement.ORGANIZATION.getName()));

	    Assert.assertFalse(orgNested.getJSONObject("properties").has(fieldName));

	    //
	    // expecting also orgName field after updating
	    //

	    DataFolderMapping.get().checkAndUpdate(dataBase.getClient());

	    orgNested = OpenSearchUtils.toJSONObject(
		    readFieldsToProperties(dataBase.getClient()).get(MetadataElement.ORGANIZATION.getName()));

	    Assert.assertTrue(orgNested.getJSONObject("properties").has(fieldName));
	}
    }

    /**
     * @param client
     * @return
     * @throws IOException
     */
    private Set<String> readFields(OpenSearchClient client) throws IOException {

	GetMappingResponse getMappingResponse = client.indices()
		.getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	IndexMappingRecord record = getMappingResponse.result().values().iterator().next();

	return record.mappings().properties().keySet();
    }

    /**
     * @param client
     * @return
     * @throws IOException
     */
    private Map<String, Property> readFieldsToProperties(OpenSearchClient client) throws IOException {

	GetMappingResponse getMappingResponse = client.indices()
		.getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	IndexMappingRecord record = getMappingResponse.result().values().iterator().next();

	return record.mappings().properties();
    }

    /**
     * @throws GSException
     */
    private void clearIndexes() throws GSException, IOException {

	OpenSearchClient client = OpenSearchDatabase.createNoSSLContextClient(OpenSearchDatabase.createLocalServiceInfo());

	for (String index : IndexMapping.getIndexes(false)) {

	    if (DataFolderMapping.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}
    }

}
