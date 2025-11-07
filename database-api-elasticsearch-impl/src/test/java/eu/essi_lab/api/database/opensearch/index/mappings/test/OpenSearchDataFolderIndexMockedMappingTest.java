package eu.essi_lab.api.database.opensearch.index.mappings.test;

import com.vaadin.flow.component.page.Meta;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.messages.JavaOptions;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import kotlin.Metadata;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

/**
 *
 */
public class OpenSearchDataFolderIndexMockedMappingTest {

    @Test
    public void allFieldsTest() throws GSException, IOException {

	JSONObject originalMapping = DataFolderMapping.get().getMapping();

	final List<String> originalFields = originalMapping.getJSONObject("properties").keySet().stream().sorted().toList();

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

	    OpenSearchDatabase dataBase = OpenSearchDatabase.createLocalService();

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

	JSONObject mockedMapping = DataFolderMapping.get().getMapping();

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

	    OpenSearchDatabase dataBase = OpenSearchDatabase.createLocalService();

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
