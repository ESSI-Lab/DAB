/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class TestUtils {

    /**
     * 
     */
    public static final String SOURCE_ID = "acronet";

    /**
     * @param database
     * @return
     */
    public static String getMetaFolderName(OpenSearchDatabase database) {

	return getMetaFolderName(database, SOURCE_ID);
    }

    /**
     * @param database
     * @param sourceId
     * @return
     */
    public static String getMetaFolderName(OpenSearchDatabase database, String sourceId) {

	return sourceId + // source id
		SourceStorageWorker.META_PREFIX;//
    }

    /**
     * @param database
     * @return
     */
    public static String getDataFolderName(OpenSearchDatabase database) {

	return SOURCE_ID + // source id
		SourceStorageWorker.DATA_1_POSTFIX;//
    }

    /**
     * @param targetSource
     * @param decodedSource
     */
    private static void compareJSONObjects(JSONObject targetSource, JSONObject decodedSource) {

	targetSource.remove(DataFolderMapping.GS_RESOURCE);

	decodedSource.remove(DataFolderMapping.GS_RESOURCE);

	Assert.assertEquals(targetSource.toString(), decodedSource.toString());
    }

    /**
     * @param wrapper
     * @param originalDataset
     * @param folder
     * @param key
     * @throws Exception
     */
    public static void compareResources(//
	    SourceWrapper wrapper, //
	    GSResource originalDataset, //
	    DatabaseFolder folder, //
	    String key) throws Exception {

	//
	// 1) retrieves the stored dataset from the Base64 encoded string
	//

	Assert.assertTrue(wrapper.getGSResource().isPresent());

	String base64Dataset = wrapper.getGSResource().get();

	ClonableInputStream storedDatasetStream = new ClonableInputStream(ConversionUtils.decode(base64Dataset));

	GSResource storedDataset = GSResource.create(storedDatasetStream.clone());

	// resources are stored without indexes (except the bbox), they must be added before the comparison
	IndexData.decorate(wrapper.getSource(), storedDataset);

	//
	// 2) compares the stored dataset indexes with the original one indexes
	//

	Assert.assertTrue(compareIndexedElements(wrapper, originalDataset, storedDataset));

	//
	// 3.1) compares the JSON objects resulting from the mapping of the two resources
	//
	// [ the IndexData method used here for the mapping reads the indexes value from the IndexesMetadata ]
	// [ and it removes the IndexesMetadata from the dataset, so in order to preserve the original dataset ]
	// [ for the next tests, we use a clone ]
	//

	IndexData ofTargetMethod_1 = null;
	IndexData ofDecodedMethod_1 = null;

	IndexData ofTargetMethod_2 = null;
	IndexData ofDecodedMethod_2 = null;

	{

	    ofTargetMethod_1 = IndexData.of((OpenSearchFolder) folder, GSResource.create(originalDataset.asDocument(true)));

	    ofDecodedMethod_1 = IndexData.of((OpenSearchFolder) folder, GSResource.create(storedDataset.asDocument(true)));

	    compareJSONObjects(ofTargetMethod_1.getDataObject(), ofDecodedMethod_1.getDataObject());
	}

	//
	// 3.2) compares the JSON objects resulting from the mapping of the two resources
	//
	// [ the IndexData method used here for the mapping reads the indexes using the XMLDocumentReader ]
	// [ and it removes the IndexesMetadata from the dataset, so in order to preserve the original dataset ]
	// [ for the next tests, we use a clone ]
	//
	{

	    ofTargetMethod_2 = IndexData.of((OpenSearchFolder) folder, GSResource.create(originalDataset.asDocument(true)).asStream());

	    ofDecodedMethod_2 = IndexData.of((OpenSearchFolder) folder, GSResource.create(storedDataset.asDocument(true)).asStream());

	    compareJSONObjects(ofTargetMethod_2.getDataObject(), ofDecodedMethod_2.getDataObject());
	}

	//
	// 3.3) compares the JSON objects resulting from the two mapping methods
	//

	compareJSONObjects(ofTargetMethod_1.getDataObject(), ofTargetMethod_2.getDataObject());

	compareJSONObjects(ofDecodedMethod_1.getDataObject(), ofDecodedMethod_2.getDataObject());

	//
	// 4) retrieves the dataset from the folder and compares the indexes with the original one
	//

	Node binary = folder.get(key);

	GSResource folderDataset = GSResource.create(binary);

	Assert.assertTrue(compareIndexedElements(wrapper, originalDataset, folderDataset));
    }

    /**
     * @param wrapper
     * @param originalResource
     * @param storedResource
     * @return
     * @throws Exception
     */
    private static boolean compareIndexedElements(SourceWrapper wrapper, GSResource originalResource, GSResource storedResource)
	    throws Exception {

	IndexesMetadata indexesMetadata1 = originalResource.getIndexesMetadata();
	IndexesMetadata indexesMetadata2 = storedResource.getIndexesMetadata();

	boolean equals = true;

	List<String> indexes1Prop = indexesMetadata1.getProperties().//
		stream().//
		//
		// filter out the bbox_Null properties since it is not mapped 
		// because if the dataset has BoundingPolygon it is not indexed, so the JSON source
		// have the bbox value but the original dataset
		//
		filter(p -> !p.equals(IndexedElements.BOUNDING_BOX_NULL.getElementName())).
		collect(Collectors.toList());

	equals &= indexes1Prop.equals(indexesMetadata2.getProperties());

	if (!equals) {

	    throw new Exception();
	}

	for (String prop : indexes1Prop) {

	    List<String> prop1 = indexesMetadata1.read(prop);
	    List<String> prop2 = indexesMetadata2.read(prop).//
		    stream().//
		    map(v -> v.replace(".000Z", "Z")).//
		    collect(Collectors.toList());

	    equals &= prop1.equals(prop2);

	    if (!equals) {

		throw new Exception();
	    }

	    if ( //
	    prop.equals(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName()) || //
		    prop.equals(IndexedElements.TEMP_EXTENT_END_NOW.getElementName()) //
	    ) {

		//
		// in the JSON source they have a true value
		//
		boolean nullElementValue = Boolean.valueOf(wrapper.getGSResourceProperties(prop).get(0));

		//
		// in the GSResource has no values but they are present (e.g.:
		// <gs:tmpExtentBegin_Now></gs:tmpExtentBegin_Now>
		//
		boolean present = indexesMetadata1.getProperties().//
			stream().//
			filter(p -> p.equals(prop)).//
			findFirst().//
			isPresent();

		equals &= nullElementValue == present;

		if (!equals) {

		    throw new Exception();
		}

	    } else {

		Optional<ContentType> contType = ResourceProperty.optFromName(prop).map(rp -> rp.getContentType());

		if (!contType.isPresent()) {

		    contType = MetadataElement.optFromName(prop).map(rp -> rp.getContentType());
		}

		if (!contType.isPresent()) {

		    // null properties have no content type
		    Assert.assertTrue(prop.endsWith("_Null"));
		}

		else if (contType.get() == ContentType.ISO8601_DATE || contType.get() == ContentType.ISO8601_DATE_TIME) {

		    List<Long> longDataTimes = wrapper.getGSResourceProperties(prop).//
			    stream().//
			    map(v -> Long.valueOf(v)).//
			    sorted().//
			    collect(Collectors.toList());

		    List<Long> collect = indexesMetadata1.read(prop).//
			    stream().//
			    map(v -> ConversionUtils.parseToLong(v).get()).//
			    sorted().//
			    collect(Collectors.toList());

		    equals &= longDataTimes.equals(collect);

		    if (!equals) {

			throw new Exception();
		    }

		} else {

		    //
		    // double values such 1.0 are converted by JSONArray by removing the 0
		    // after the decimal separator, so we convert also the GSResource indexed field
		    // in order to adjust the test
		    //
		    List<String> list = indexesMetadata1.read(prop);

		    List<String> values = list.//
			    subList(0, list.size() > 10 ? 10 : list.size()).stream().//
			    map(v -> {

				try {
				    Double.valueOf(v);
				    if (v.endsWith(".0")) {

					return v.replace(".0", "");
				    }
				} catch (NumberFormatException ex) {

				}

				return v;

			    }).//
			    collect(Collectors.toList());

		    List<String> gsResourceProperties = wrapper.getGSResourceProperties(prop);

		    List<String> wrapperValues = gsResourceProperties.//
			    subList(0, gsResourceProperties.size() > 10 ? 10 : gsResourceProperties.size()).stream().//
			    map(v -> {

				try {
				    Double.valueOf(v);
				    if (v.endsWith(".0")) {

					return v.replace(".0", "");
				    }
				} catch (NumberFormatException ex) {

				}

				return v;

			    }).//
			    collect(Collectors.toList());

		    equals &= wrapperValues.equals(values);

		    if (!equals) {

			throw new Exception();
		    }
		}
	    }
	}

	return equals;
    }
}
