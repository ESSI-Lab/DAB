/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.indexes.IndexedElements;
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

	return SOURCE_ID + // source id
		SourceStorageWorker.META_PREFIX;//
    }

    /**
     * @param database
     * @return
     */
    public static String getDataFolderName(OpenSearchDatabase database) {

	return SOURCE_ID + // source id
		SourceStorageWorker.DATA_1_PREFIX;//
    }

    /**
     * @param wrapper
     * @param targetDataset
     * @param folder
     * @param key
     * @throws Exception
     */
    public static void compareResources(//
	    SourceWrapper wrapper, //
	    GSResource targetDataset, //
	    DatabaseFolder folder, //
	    String key) throws Exception {

	Optional<String> optResource = wrapper.getGSResource();
	Assert.assertTrue(optResource.isPresent());

	String base64resource = optResource.get();
	InputStream decoded = IndexData.decode(base64resource);

	GSResource decodedResource = GSResource.create(decoded);

	Node binary = folder.get(key);

	GSResource folderResource = GSResource.create(binary);

	Assert.assertTrue(compareProperties(wrapper, targetDataset, decodedResource));
	Assert.assertTrue(compareProperties(wrapper, targetDataset, folderResource));
    }

    /**
     * @param wrapper
     * @param res1
     * @param res2
     * @return
     * @throws Exception
     */
    private static boolean compareProperties(SourceWrapper wrapper, GSResource res1, GSResource res2) throws Exception {

	IndexesMetadata indexesMetadata1 = res1.getIndexesMetadata();
	IndexesMetadata indexesMetadata2 = res2.getIndexesMetadata();

	boolean equals = true;

	equals &= indexesMetadata1.getProperties().equals(indexesMetadata2.getProperties());

	if (!equals) {

	    throw new Exception();
	}

	for (String prop : indexesMetadata1.getProperties()) {

	    equals &= indexesMetadata1.read(prop).equals(indexesMetadata2.read(prop));

	    if (!equals) {

		throw new Exception();
	    }

	    if (prop.equals(IndexedElements.BOUNDING_BOX_NULL.getElementName())) {

		boolean hasBoundingPolygons = !res1.getHarmonizedMetadata().//
			getCoreMetadata().//
			getDataIdentification().//
			getBoundingPolygonsList().//
			isEmpty();

		//
		// bounding polygons are not put in the GSResource indexed elements, so in this case
		// the GSResource has the bbox_Null element, but the JSON source has
		// the bounding polygons related shape
		//
		List<String> bboxes = wrapper.getGSResourceProperties(MetadataElement.BOUNDING_BOX);

		if (hasBoundingPolygons) {

		    equals &= !bboxes.isEmpty();

		    if (!equals) {

			throw new Exception();
		    }

		} else {

		    equals &= bboxes.isEmpty();

		    if (!equals) {

			throw new Exception();
		    }
		}

	    } else if (prop.equals(IndexedElements.TEMP_EXTENT_BEGIN_NULL.getElementName()) || //
		    prop.equals(IndexedElements.TEMP_EXTENT_END_NULL.getElementName()) || //
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

		if (contType.get() == ContentType.ISO8601_DATE || contType.get() == ContentType.ISO8601_DATE_TIME) {

		    List<Long> longDataTimes = wrapper.getGSResourceProperties(prop).//
			    stream().//
			    map(v -> Long.valueOf(v)).//
			    sorted().//
			    collect(Collectors.toList());

		    List<Long> collect = indexesMetadata1.read(prop).//
			    stream().//
			    map(v -> IndexData.parseDateTime(v).get()).//
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
