package eu.essi_lab.pdk.rsm.impl.json.jsapi;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.iso.datamodel.classes.*;
import jakarta.xml.bind.*;
import net.opengis.iso19139.gco.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.*;
import org.json.*;

import java.math.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class JSONSpatialRepresentationMapper {

    private static final String MD_DIMENSION_NAME_TYPE_CODE_URI = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_DimensionNameTypeCode";

    private static final String MD_TOPOLOGY_LEVEL_CODE_URI = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_TopologyLevelCode";

    private static final String MD_GEOMETRIC_OBJECT_TYPE_CODE_URI = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_GeometricObjectTypeCode";

    private static final String MD_CELL_GEOMETRY_CODE_URI = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_CellGeometryCode";
    private final MDMetadata metadata;

    /**
     * @param mdMetadata
     */
    public JSONSpatialRepresentationMapper(MDMetadata mdMetadata) {

	this.metadata = mdMetadata;
    }

    /**
     * @param mi
     * @return
     */
    public Optional<JSONObject> map() {
	try {

	    JSONObject vectorSpatialRepresentation = vectorSpatialRepresentationInfoToJson(metadata);

	    if (vectorSpatialRepresentation != null) {
		JSONObject spatialRepInfo = new JSONObject();
		spatialRepInfo.put("vector_spatial_representation", vectorSpatialRepresentation);

		return Optional.of(spatialRepInfo);
	    }

	    GridSpatialRepresentation grid = metadata.getGridSpatialRepresentation();

	    if (grid == null) {

		return Optional.empty();
	    }

	    JSONObject gridRep = new JSONObject();
	    Integer numDim = grid.getNumberOfDimensions();

	    if (numDim != null) {
		gridRep.put("number_of_dimensions", numDim);
	    }

	    String cellGeometry = grid.getCellGeometryCode();

	    if (cellGeometry != null) {
		String cellUri = gridCellGeometryUri(grid);
		gridRep.put("cell_geometry", codeObj(cellGeometry, cellUri));
	    }

	    BooleanPropertyType transParam = grid.getElementType().getTransformationParameterAvailability();

	    if (transParam != null) {
		gridRep.put("transformation_parameter_availability", transParam.isBoolean());
	    }

	    JSONArray axisArr = new JSONArray();
	    Iterator<Dimension> dimIt = grid.getAxisDimensions();

	    if (dimIt != null) {
		while (dimIt.hasNext()) {
		    JSONObject axisObj = axisDimensionToJson(dimIt.next());
		    if (axisObj != null)
			axisArr.put(axisObj);
		}
	    }

	    if (!axisArr.isEmpty()) {
		gridRep.put("axis_dimension_properties", axisArr);
	    }

	    JSONObject spatialRepInfo = new JSONObject();

	    spatialRepInfo.put("grid_spatial_representation", gridRep);

	    return Optional.of(spatialRepInfo);

	} catch (Exception e) {

	    return Optional.empty();
	}
    }

    private JSONObject vectorSpatialRepresentationInfoToJson(MDMetadata mi) {
	try {
	    List<MDSpatialRepresentationPropertyType> spatialReps = mi.getElementType().getSpatialRepresentationInfo();
	    if (spatialReps == null)
		return null;
	    for (MDSpatialRepresentationPropertyType spatialRep : spatialReps) {
		if (spatialRep == null || spatialRep.getAbstractMDSpatialRepresentation() == null)
		    continue;
		AbstractMDSpatialRepresentationType value = spatialRep.getAbstractMDSpatialRepresentation().getValue();
		if (!(value instanceof MDVectorSpatialRepresentationType))
		    continue;

		MDVectorSpatialRepresentationType vector = (MDVectorSpatialRepresentationType) value;
		JSONObject out = new JSONObject();

		MDTopologyLevelCodePropertyType topologyLevel = vector.getTopologyLevel();
		if (topologyLevel != null && topologyLevel.getMDTopologyLevelCode() != null) {
		    CodeListValueType topologyCode = topologyLevel.getMDTopologyLevelCode();
		    out.put("topology_level", codeObj(topologyCode.getCodeListValue(),
			    normalizeCodelistUri(topologyCode.getCodeList(), MD_TOPOLOGY_LEVEL_CODE_URI)));
		}

		if (vector.getGeometricObjects() != null && !vector.getGeometricObjects().isEmpty()) {
		    JSONArray geometricObjects = new JSONArray();
		    for (MDGeometricObjectsPropertyType geometricProperty : vector.getGeometricObjects()) {
			if (geometricProperty == null)
			    continue;
			MDGeometricObjectsType geometric = geometricProperty.getMDGeometricObjects();
			if (geometric == null)
			    continue;
			JSONObject geometricJson = new JSONObject();
			if (geometric.getGeometricObjectType() != null
				&& geometric.getGeometricObjectType().getMDGeometricObjectTypeCode() != null) {
			    CodeListValueType geometricCode = geometric.getGeometricObjectType().getMDGeometricObjectTypeCode();
			    geometricJson.put("geometric_object_type", codeObj(geometricCode.getCodeListValue(),
				    normalizeCodelistUri(geometricCode.getCodeList(), MD_GEOMETRIC_OBJECT_TYPE_CODE_URI)));
			}
			if (geometric.getGeometricObjectCount() != null && geometric.getGeometricObjectCount().getInteger() != null) {
			    geometricJson.put("geometric_object_count", geometric.getGeometricObjectCount().getInteger().intValue());
			}
			if (geometricJson.length() > 0)
			    geometricObjects.put(geometricJson);
		    }
		    if (geometricObjects.length() > 0)
			out.put("geometric_objects", geometricObjects);
		}

		return out.length() > 0 ? out : null;
	    }
	} catch (Exception e) {
	    return null;
	}
	return null;
    }

    private String gridCellGeometryUri(GridSpatialRepresentation grid) {
	try {
	    MDCellGeometryCodePropertyType prop = grid.getElementType().getCellGeometry();
	    if (prop == null)
		return null;
	    Object code = prop.getMDCellGeometryCode();
	    if (code instanceof CodeListValueType) {
		String uri = ((CodeListValueType) code).getCodeList();
		return normalizeCodelistUri(uri, MD_CELL_GEOMETRY_CODE_URI);
	    }
	    return null;
	} catch (Exception e) {
	    return MD_CELL_GEOMETRY_CODE_URI;
	}
    }

    private JSONObject axisDimensionToJson(Dimension dim) {
	try {
	    JSONObject o = new JSONObject();
	    String dimNameVal = dim.getDimensionNameTypeCode();
	    String dimNameUri = dimensionNameTypeCodeUri(dim);
	    if (dimNameVal != null)
		o.put("dimension_name", codeObj(dimNameVal, dimNameUri));
	    BigInteger size = dim.getDimensionSize();
	    if (size != null)
		o.put("dimension_size", size.intValue() == 0 ? "unknown" : size.intValue());
	    Double resVal = dim.getResolutionValue();
	    String resUom = dim.getResolutionUOM();
	    if (resVal != null || resUom != null) {
		JSONObject res = new JSONObject();
		if (resUom != null)
		    res.put("uom", resUom);
		if (resVal != null)
		    res.put("value", resVal == Math.floor(resVal) ? resVal.intValue() : resVal);
		o.put("resolution", res);
	    }
	    return o.length() > 0 ? o : null;
	} catch (Exception e) {
	    return null;
	}
    }

    private String dimensionNameTypeCodeUri(Dimension dim) {

	try {
	    MDDimensionNameTypeCodePropertyType prop = dim.getElementType().getDimensionName();
	    if (prop == null)
		return null;
	    Object code = prop.getMDDimensionNameTypeCode();
	    if (code instanceof JAXBElement)
		code = ((JAXBElement<?>) code).getValue();
	    if (code instanceof CodeListValueType) {
		String uri = ((CodeListValueType) code).getCodeList();
		return normalizeCodelistUri(uri, MD_DIMENSION_NAME_TYPE_CODE_URI);
	    }
	    return null;
	} catch (Exception e) {
	    return MD_DIMENSION_NAME_TYPE_CODE_URI;
	}
    }

    private String normalizeCodelistUri(String uri, String fallback) {
	if (uri == null || uri.isEmpty())
	    return fallback;
	if (uri.startsWith("http://www.isotc211.org/2005/resources/codeList.xml#")) {
	    return uri.replace("http://www.isotc211.org/2005/resources/codeList.xml#",
		    "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#");
	}
	return uri;
    }

    private JSONObject codeObj(String value) {
	return codeObj(value, null);
    }

    private JSONObject codeObj(String value, String uri) {
	if (value == null)
	    return null;
	JSONObject o = new JSONObject();
	if (uri != null)
	    o.put("uri", uri);
	o.put("value", value);
	return o;
    }
}
