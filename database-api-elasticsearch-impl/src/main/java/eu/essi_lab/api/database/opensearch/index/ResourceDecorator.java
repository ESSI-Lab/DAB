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
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
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

	    Optional.ofNullable(source.optString(Shape.CENTROID, null)).ifPresent(v -> res.getExtensionHandler().setCentroid(v));

	    Optional.ofNullable(source.opt(Shape.AREA)).ifPresent(v -> res.getExtensionHandler().setArea(Double.valueOf(v.toString())));
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

	//
	// if the binary is excluded
	// 1) source can be set, if the source id is available
	// 2) all extended properties can be set, if available
	//
	if (!source.has(DataFolderMapping.GS_RESOURCE)) {

	    Optional<JSONArray> sourceId = Optional.ofNullable(source.optJSONArray(ResourceProperty.SOURCE_ID.getName(), null));

	    if (sourceId.isPresent()) {

		ConfigurationWrapper.getAllSources().//
			stream().//
			filter(s -> s.getUniqueIdentifier().equals(sourceId.get().get(0))).//
			findFirst().//
			ifPresent(s -> res.setSource(s));
	    }

	    if (source.has(MetadataElement.BNHS_INFO.getName())) {

		res.getExtensionHandler().setBNHSInfo(source.getJSONArray(MetadataElement.BNHS_INFO.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.RIVER_BASIN.getName())) {

		res.getExtensionHandler().setRiverBasin(source.getJSONArray(MetadataElement.RIVER_BASIN.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.RIVER.getName())) {

		res.getExtensionHandler().setRiver(source.getJSONArray(MetadataElement.RIVER.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.COUNTRY.getName())) {

		res.getExtensionHandler().setCountry(source.getJSONArray(MetadataElement.COUNTRY.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.COUNTRY_ISO3.getName())) {

		res.getExtensionHandler().setCountryISO3(source.getJSONArray(MetadataElement.COUNTRY_ISO3.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.DATA_SIZE.getName())) {

		res.getExtensionHandler()
			.setDataSize(Long.valueOf(source.getJSONArray(MetadataElement.DATA_SIZE.getName()).get(0).toString()));
	    }

	    if (source.has(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER.getName())) {

		res.getExtensionHandler().setUniqueInstrumentIdentifier(
			source.getJSONArray(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER.getName())) {

		res.getExtensionHandler().setUniquePlatformIdentifier(
			source.getJSONArray(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER.getName())) {

		res.getExtensionHandler().setUniqueAttributeIdentifier(
			source.getJSONArray(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.CROP_TYPES.getName())) {

		res.getExtensionHandler().setCropTypes(source.getJSONArray(MetadataElement.CROP_TYPES.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_INTERPOLATION.getName())) {

		res.getExtensionHandler()
			.setTimeInterpolation(source.getJSONArray(MetadataElement.TIME_INTERPOLATION.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_SUPPORT.getName())) {

		res.getExtensionHandler().setTimeSupport(source.getJSONArray(MetadataElement.TIME_SUPPORT.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_RESOLUTION.getName())) {

		res.getExtensionHandler()
			.setTimeResolution(source.getJSONArray(MetadataElement.TIME_RESOLUTION.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_UNITS.getName())) {

		res.getExtensionHandler().setTimeUnits(source.getJSONArray(MetadataElement.TIME_UNITS.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_UNITS_ABBREVIATION.getName())) {

		res.getExtensionHandler()
			.setTimeUnitsAbbreviation(source.getJSONArray(MetadataElement.TIME_UNITS_ABBREVIATION.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_RESOLUTION_DURATION_8601.getName())) {

		res.getExtensionHandler().setTimeResolutionDuration8601(
			source.getJSONArray(MetadataElement.TIME_RESOLUTION_DURATION_8601.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.TIME_AGGREGATION_DURATION_8601.getName())) {

		res.getExtensionHandler().setTimeAggregationDuration8601(
			source.getJSONArray(MetadataElement.TIME_AGGREGATION_DURATION_8601.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.OBSERVED_PROPERTY_URI.getName())) {

		res.getExtensionHandler()
			.setObservedPropertyURI(source.getJSONArray(MetadataElement.OBSERVED_PROPERTY_URI.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.WIS_TOPIC_HIERARCHY.getName())) {

		res.getExtensionHandler()
			.setWISTopicHierarchy(source.getJSONArray(MetadataElement.WIS_TOPIC_HIERARCHY.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.ATTRIBUTE_UNITS.getName())) {

		res.getExtensionHandler()
			.setAttributeUnits(source.getJSONArray(MetadataElement.ATTRIBUTE_UNITS.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.ATTRIBUTE_UNITS_URI.getName())) {

		res.getExtensionHandler()
			.setAttributeUnitsURI(source.getJSONArray(MetadataElement.ATTRIBUTE_UNITS_URI.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION.getName())) {

		res.getExtensionHandler().setAttributeUnitsAbbreviation(
			source.getJSONArray(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION.getName()).get(0).toString());
	    }

	    if (source.has(MetadataElement.ATTRIBUTE_MISSING_VALUE.getName())) {

		res.getExtensionHandler()
			.setAttributeMissingValue(source.getJSONArray(MetadataElement.ATTRIBUTE_MISSING_VALUE.getName()).get(0).toString());
	    }
	}

	return res;
    }

}
