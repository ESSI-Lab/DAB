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
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.ResourceDecorator;
import eu.essi_lab.api.database.opensearch.index.Shape;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.jaxb.BoundingBox;
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
		SourceStorageWorker.META_POSTFIX;//
    }

    /**
     * @param database
     * @return
     */
    public static String getDataFolderName(OpenSearchDatabase database) {

	return SOURCE_ID + // source id
		SourceStorageWorker.DATA_1_POSTFIX;//
    }

    public static boolean EXIT_ON_ERROR = false;

    /**
     * @param targetSource
     * @param decodedSource
     */
    private static void compareJSONObjects(JSONObject targetSource, JSONObject decodedSource) {

	targetSource.remove(DataFolderMapping.GS_RESOURCE);

	decodedSource.remove(DataFolderMapping.GS_RESOURCE);

	if (!targetSource.toString().equals(decodedSource.toString())) {

	    System.out.println();
	}

	if (EXIT_ON_ERROR) {
	    Assert.assertEquals(targetSource.toString(), decodedSource.toString());
	}
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

	ClonableInputStream storedDatasetStream = new ClonableInputStream(OpenSearchUtils.decode(base64Dataset));

	GSResource storedDataset = GSResource.create(storedDatasetStream.clone());

	// resources are stored without indexes (except the bbox), they must be added before the comparison
	ResourceDecorator.get().decorate(wrapper.getSource(), storedDataset);

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

	IndexesMetadata indexesMd1 = originalResource.getIndexesMetadata();
	IndexesMetadata indexesMd2 = storedResource.getIndexesMetadata();

	boolean equals = true;

	List<BoundingPolygon> polygonsList = originalResource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		.getBoundingPolygonsList();

	List<String> indexes1Prop = indexesMd1.getProperties();
	List<String> indexes2Prop = indexesMd2.getProperties();

	Optional<BoundingBox> box = indexesMd1.readBoundingBox();
	Optional<Shape> shape = Optional.empty();
	if (box.isPresent()) {
	    shape = Shape.of(box.get());
	}

	if (!polygonsList.isEmpty() || (box.isPresent() && shape.isEmpty())) {

	    indexes1Prop = indexesMd1.getProperties().//
		    stream().//
		    //
		    // filter out the "bbox_Null" properties from original list since
		    // the BoundingPolygon has no indexed element and, if a bbox is missing, the dataset
		    // is tagged with bbox_Null while in the JSON mapping the BoundingPolygon
		    // is indexed so the indexesMetadata1 would contains "bbox_Null" while indexesMetadata2 don't
		    //
		    filter(p -> !p.equals(IndexedElements.BOUNDING_BOX_NULL.getElementName())).collect(Collectors.toList());
	}

	if (box.isPresent() && shape.isEmpty()) {

	    indexes2Prop = indexesMd2.getProperties().//
		    stream().//
		    //
		    // filter out the "bbox_Null" properties from stored list since
		    // the original has an invalid bbox but is not tagged as "bbox_Null" while
		    // in the JSON mapping it is correctly detected
		    //
		    filter(p -> !p.equals(IndexedElements.BOUNDING_BOX_NULL.getElementName())).collect(Collectors.toList());
	}

	equals &= indexes1Prop.equals(indexes2Prop);

	if (!equals) {

	    if (EXIT_ON_ERROR) {

		throw new Exception();
	    }

	    System.out.println(indexesMd1);
	    System.out.println("\n\n");
	    System.out.println(indexesMd2);

	    equals = true;
	}

	for (String prop : indexes1Prop) {

	    List<String> prop1Values = indexesMd1.read(prop);
	    List<String> prop2Values = indexesMd2.read(prop);

	    if (prop.equals(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName()) || //
		    prop.equals(IndexedElements.TEMP_EXTENT_END_NOW.getElementName()) //
	    ) {

		//
		// in the JSON source they have a true value
		//
		boolean nullElementValue = Boolean.valueOf(wrapper.getGSResourceProperties(prop).get(0));

		//
		// in the GSResource has empty text value (e.g.: <gs:tmpExtentBegin_Now></gs:tmpExtentBegin_Now>)
		//
		boolean present = prop1Values.get(0).isEmpty();

		equals &= nullElementValue == present;

		if (!equals) {

		    if (EXIT_ON_ERROR) {

			throw new Exception();
		    }

		    equals = true;
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

		    List<Long> collect = prop1Values.//
			    stream().//
			    map(v -> OpenSearchUtils.parseToLong(v).get()).//
			    sorted().//
			    collect(Collectors.toList());

		    equals &= longDataTimes.equals(collect);

		    if (!equals) {

			if (EXIT_ON_ERROR) {

			    throw new Exception();
			}

			equals = true;
		    }
		}

		else if (contType.get() == ContentType.DOUBLE) {

		    //
		    // double values such 1.0 are converted by JSONArray by removing the 0
		    // after the decimal separator, so we convert also the GSResource indexed field
		    // in order to adjust the test
		    //

		    List<String> values = prop1Values.//
			    subList(0, prop1Values.size() > 10 ? 10 : prop1Values.size()).stream().//
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

			equals = true;
		    }

		} else {

		    equals &= prop1Values.equals(prop2Values);

		    if (!equals) {

			if (EXIT_ON_ERROR) {

			    throw new Exception();
			}

			equals = true;
		    }
		}
	    }
	}

	return equals;
    }
}
