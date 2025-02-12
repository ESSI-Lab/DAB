/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

import java.util.Date;

import org.json.JSONObject;

import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class ResourceDecorator {

    private ResourceDecorator() {
    }

    /**
     * @return
     */
    public static ResourceDecorator get() {

	return new ResourceDecorator();
    }

    /**
     * @param source
     * @param res
     * @return
     */
    public GSResource decorate(JSONObject source, GSResource res) {

	IndexesMetadata indexesMd = res.getIndexesMetadata();

	ResourceProperty.listValues().forEach(pr -> {

	    if (source.has(pr.getName())) {

		IndexedElement element = new IndexedElement(pr.getName());

		source.getJSONArray(pr.getName()).forEach(v -> {

		    Object value = v;

		    if (pr.getContentType() == ContentType.ISO8601_DATE || pr.getContentType() == ContentType.ISO8601_DATE_TIME) {

			if (source.has(DataFolderMapping.toDateField(pr.getName()))) {

			    value = source.getJSONArray(DataFolderMapping.toDateField(pr.getName())).get(0);

			} else {

			    value = ISO8601DateTimeUtils.getISO8601DateTime(new Date(Long.valueOf(v.toString())));
			}
		    }

		    if (!element.getValues().contains(value)) {

			element.getValues().add(value.toString());
		    }
		});

		if (!indexesMd.getProperties().contains(pr.getName())) {

		    indexesMd.write(element);
		}
	    }
	});

	MetadataElement.listValues().forEach(el -> {

	    if (source.has(el.getName())) {

		// bbox is already in, the IndexesMetadata.clear(false) do not remove it
		// @see IndexData
		if (!el.getName().equals(MetadataElement.BOUNDING_BOX.getName())) {

		    IndexedElement element = new IndexedElement(el.getName());

		    source.getJSONArray(el.getName()).forEach(v -> {

			Object value = v;

			if (el.getContentType() == ContentType.ISO8601_DATE || el.getContentType() == ContentType.ISO8601_DATE_TIME) {

			    if (source.has(DataFolderMapping.toDateField(el.getName()))) {

				value = source.getJSONArray(DataFolderMapping.toDateField(el.getName())).get(0);

			    } else {

				value = ISO8601DateTimeUtils.getISO8601DateTime(new Date(Long.valueOf(v.toString())));
			    }
			}

			element.getValues().add(value.toString());
		    });

		    indexesMd.write(element);
		}
	    }
	});

	if (source.has(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName())) {

	    indexesMd.write(IndexedElements.TEMP_EXTENT_BEGIN_NOW);
	}

	if (source.has(IndexedElements.TEMP_EXTENT_END_NOW.getElementName())) {

	    indexesMd.write(IndexedElements.TEMP_EXTENT_END_NOW);
	}

	if (!source.has(MetadataElement.TEMP_EXTENT_BEGIN.getName()) && //
		!source.has(MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW.getName()) //
		&& !source.has(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName())) {

	    indexesMd.write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
	}

	if (!source.has(MetadataElement.TEMP_EXTENT_END.getName()) && !source.has(IndexedElements.TEMP_EXTENT_END_NOW.getElementName())) {

	    indexesMd.write(IndexedElements.TEMP_EXTENT_END_NULL);
	}

	if (!source.has(MetadataElement.BOUNDING_BOX.getName())) {

	    indexesMd.write(IndexedElements.BOUNDING_BOX_NULL);
	}

	return res;
    }

}
