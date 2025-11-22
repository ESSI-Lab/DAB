package eu.essi_lab.api.database.opensearch.index.mappings.test;

import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolderIndexUpdateSimpleFieldsInternalTestIT extends OpenSearchTest {

    private static final String FIELD_NAME = "testField";

    @Test
    public void simpleFieldTest() throws GSException, IOException {

	//
	//
	//

	OpenSearchDatabase dataBase = createDataBase();

	OpenSearchClient client = dataBase.getClient();

	//
	//
	//

	try (MockedStatic<MetadataElement> mocked = mockStatic(MetadataElement.class)) {

	    mocked.when(MetadataElement::listQueryables).thenReturn(Arrays.asList(

		    new Queryable() {

			@Override
			public String getName() {

			    return FIELD_NAME;
			}

			@Override
			public ContentType getContentType() {

			    return ContentType.TEXTUAL;
			}

			@Override
			public boolean isVolatile() {

			    return false;
			}

			@Override
			public void setEnabled(boolean enabled) {

			}

			@Override
			public boolean isEnabled() {

			    return true;
			}
		    }));

	    List<Queryable> result = MetadataElement.listQueryables();

	    assert result.size() == 1;
	    assert result.get(0).getName().equals(FIELD_NAME);

	    //
	    //
	    //

	    GetMappingResponse mapping = client.indices()
		    .getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	    IndexMappingRecord record = mapping.result().values().iterator().next();

	    Set<String> properties = record.mappings().properties().keySet();

	    Assert.assertFalse(properties.contains(FIELD_NAME));

	    //
	    //
	    //

	    DataFolderMapping.get().checkAndUpdate(client);

	    //
	    //
	    //

	    mapping = client.indices().getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	    record = mapping.result().values().iterator().next();

	    properties = record.mappings().properties().keySet();

	    Assert.assertTrue(properties.contains(FIELD_NAME));
	}
    }

    @Test
    public void resourcePropertyTest() throws GSException, IOException {

	//
	//
	//

	OpenSearchDatabase dataBase = createDataBase();

	OpenSearchClient client = dataBase.getClient();

	//
	//
	//

	try (MockedStatic<ResourceProperty> mocked = mockStatic(ResourceProperty.class)) {

	    mocked.when(ResourceProperty::listQueryables).thenReturn(Arrays.asList(

		    new Queryable() {

			@Override
			public String getName() {

			    return FIELD_NAME;
			}

			@Override
			public ContentType getContentType() {

			    return ContentType.TEXTUAL;
			}

			@Override
			public boolean isVolatile() {

			    return false;
			}

			@Override
			public void setEnabled(boolean enabled) {

			}

			@Override
			public boolean isEnabled() {

			    return true;
			}
		    }));

	    List<Queryable> result = ResourceProperty.listQueryables();

	    assert result.size() == 1;
	    assert result.get(0).getName().equals(FIELD_NAME);

	    //
	    //
	    //

	    GetMappingResponse mapping = client.indices()
		    .getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	    IndexMappingRecord record = mapping.result().values().iterator().next();

	    Set<String> properties = record.mappings().properties().keySet();

	    Assert.assertFalse(properties.contains(FIELD_NAME));

	    //
	    //
	    //

	    DataFolderMapping.get().checkAndUpdate(client);

	    //
	    //
	    //

	    mapping = client.indices().getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	    record = mapping.result().values().iterator().next();

	    properties = record.mappings().properties().keySet();

	    Assert.assertTrue(properties.contains(FIELD_NAME));

	}
    }
}
