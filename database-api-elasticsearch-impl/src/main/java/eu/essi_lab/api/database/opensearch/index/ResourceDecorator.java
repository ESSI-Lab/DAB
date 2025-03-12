/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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

		// if the resource binary is present, bbox is already in,
		// since the IndexesMetadata.clear(false) do not remove it
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

	//
	// adds the shape to the extension handler; it can be used in case the resource binary
	// is excluded thus the bbox is not present in the indexed metadata
	// furthermore this extension can include the geometry in case of multi polygon, which 
	// is never put in the indexes metadata
	//
	if (source.has(MetadataElement.BOUNDING_BOX.getName())) {

	    String shape = source.getString(MetadataElement.BOUNDING_BOX.getName());
	    res.getExtensionHandler().setShape(shape);
	}

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
