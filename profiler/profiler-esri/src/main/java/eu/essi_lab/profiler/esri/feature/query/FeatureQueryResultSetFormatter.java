package eu.essi_lab.profiler.esri.feature.query;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.ByteArrayInputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.esri.arcgis.protobuf.FeatureCollection;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Feature;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FeatureResult;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FeatureResult.Builder;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Geometry;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.QueryResult;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Scale;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.SpatialReference;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Transform;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Translate;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.UniqueIdField;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Value;

import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.esri.feature.ESRIFieldType;
import eu.essi_lab.profiler.esri.feature.FeatureLayer;
import eu.essi_lab.profiler.esri.feature.Field;

/**
 * @author boldrini
 */
public class FeatureQueryResultSetFormatter extends DiscoveryResultSetFormatter<Node> {

    /**
     * The encoding name of {@link #ESRI_FEATURE_SERVER_FORMATTING_ENCODING}
     */
    public static final String ESRI_FEATURE_SERVER_ENCODING_NAME = "esri-feature-server";
    /**
     * The encoding version of {@link #ESRI_FEATURE_SERVER_FORMATTING_ENCODING}
     */
    public static final String ESRI_FEATURE_SERVER_RECORD_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding ESRI_FEATURE_SERVER_FORMATTING_ENCODING = new FormattingEncoding();

    static {
	ESRI_FEATURE_SERVER_FORMATTING_ENCODING.setEncoding(ESRI_FEATURE_SERVER_ENCODING_NAME);
	ESRI_FEATURE_SERVER_FORMATTING_ENCODING.setEncodingVersion(ESRI_FEATURE_SERVER_RECORD_ENCODING_VERSION);
	ESRI_FEATURE_SERVER_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);

    }

    private static final String ESRI_FEATURE_SERVER_RESULT_SET_FORMATTER_ERROR = "ESRI_FEATURE_SERVER_RESULT_SET_FORMATTER_ERROR";
    private double xScale = 1e-9;
    private double yScale = 1e-9;

    public FeatureQueryResultSetFormatter() {

    }

    public static final String BNHS_SEPARATOR = "\t";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<Node> resultSet) throws GSException {
	// long start = System.currentTimeMillis();

	Builder featureResultBuilder = FeatureResult.newBuilder();
	List<com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Field> pbfFields = new ArrayList<>();

	List<Feature> features = new ArrayList<>();

	int wkid = 102100;
	Integer latestWkid = 3857;

	try {

	    Set<Object> distinctValues = new HashSet<>();

	    ESRIRequest esriRequest = new ESRIRequest(message.getWebRequest());
	    boolean idOnly = esriRequest.isIdentifiersOnly();
	    boolean distinct = esriRequest.isDistinctValues();
	    JSONObject response = new JSONObject();
	    Optional<String> optionalView = message.getWebRequest().extractViewId();
	    String view = null;
	    if (optionalView.isPresent()) {
		view = optionalView.get();
	    }
	    String path = message.getWebRequest().getServletRequest().getPathInfo();
	    String id = path.substring(path.indexOf("FeatureServer/"));
	    id = id.substring(id.indexOf("/") + 1);
	    id = id.substring(0, id.indexOf("/"));
	    FeatureLayer layer = FeatureLayer.getLayer(id);

	    if (layer == null) {
		JSONObject error = new JSONObject();
		error.put("code", 500);
		error.put("message", "Layer not found");
		JSONArray details = new JSONArray();
		error.put("details", details);
		response.put("error", error);
	    } else {

		String returnGeometryValue = esriRequest.getParameter("returnGeometry");
		Boolean returnGeometry = true;
		String outSR = esriRequest.getParameter("outSR");

		if (returnGeometryValue != null && returnGeometryValue.toLowerCase().equals("false")) {
		    returnGeometry = false;
		}

		String objectId = layer.getField(ESRIFieldType.OID).getName();
		response.put("objectIdFieldName", objectId);
		featureResultBuilder = featureResultBuilder.setObjectIdFieldName(objectId);

		if (!idOnly) {
		    response.put("globalIdFieldName", "");
		    JSONObject uniqueJSON = new JSONObject();
		    String uniqueId = layer.getField(ESRIFieldType.OID).getName();
		    uniqueJSON.put("name", uniqueId);
		    UniqueIdField uniqueIdField = UniqueIdField.newBuilder().setName(uniqueId).setIsSystemMaintained(true).build();
		    featureResultBuilder = featureResultBuilder.setUniqueIdField(uniqueIdField);
		    uniqueJSON.put("isSystemMaintained", true);
		    response.put("uniqueIdField", uniqueJSON);

		    if (returnGeometry) {
			response.put("geometryType", "esriGeometryPoint");
		    }
		    if (returnGeometry) {
			JSONObject reference = new JSONObject();

			if (outSR != null && outSR.equals("4326")) {
			    wkid = 4326;
			    latestWkid = 4326;
			}

			reference.put("wkid", wkid);
			reference.put("latestWkid", latestWkid);

			response.put("spatialReference", reference);
		    }
		}
		List<Field> fields = layer.getFields();

		JSONArray fieldArray = new JSONArray();

		List<Field> returnFields;

		if (idOnly) {
		    returnFields = new ArrayList<>();
		    returnFields.add(layer.getObjectIdField());
		} else {

		    String outFields = esriRequest.getParameter("outFields");
		    returnFields = layer.getOutFields(outFields);

		    for (Field field : returnFields) {

			JSONObject fieldObject = new JSONObject();
			fieldObject.put("name", field.getName());
			fieldObject.put("alias", field.getAlias());
			fieldObject.put("type", field.getType().getId());
			if (field.getLength() != null) {
			    fieldObject.put("length", field.getLength());
			}
			fieldArray.put(fieldObject);

			// PBF

			com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Field pbfField = com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Field
				.newBuilder()//
				.setName(field.getName()).setFieldType(field.getType().getType()).setAlias(field.getAlias()).build();
			pbfFields.add(pbfField);
		    }

		    response.put("fields", fieldArray);

		}

		// base link generation
		UriInfo uri = message.getWebRequest().getUriInfo();
		path = path.substring(0, path.indexOf("ArcGIS")) + "bnhs/station/";
		String base = uri.getBaseUri().toString();
		if (base.endsWith("/essi") && path.startsWith("/essi")) {
		    base = base.replace("/essi", "");
		}
		String link = base + path;
		link = link.replace("http://", "https://");

		// result set cycle
		List<Node> nodes = resultSet.getResultsList();

		JSONArray idArray = new JSONArray();
		JSONArray featureArray = new JSONArray();

		for (Node node : nodes) {
		    XMLDocumentReader reader;
		    if (node instanceof org.w3c.dom.Document) {
			reader = new XMLDocumentReader((Document) node);			
		    }else {
			reader = new XMLDocumentReader(node.getOwnerDocument());
		    }
		     
		    String bnhs = reader.evaluateString("//*:" + MetadataElement.BNHS_INFO_EL_NAME + "[1]");
		    HashMap<String, String> bnhsMap = new HashMap<>();
		    if (bnhs != null) {
			String[] split = bnhs.split(BNHS_SEPARATOR);
			for (int i = 0; i < split.length - 2; i += 2) {
			    String column = split[i];
			    String value = split[i + 1];
			    bnhsMap.put(column, value);
			}
		    }

		    // IDENTIFIERS ONLY

		    if (idOnly) {
			String idString = bnhsMap.get(layer.getObjectIdField().getBNHSProperty().getLabel());
			try {
			    Long idLong = Long.parseLong(idString);
			    idArray.put(idLong);
			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error("Unparsable ESRI id: {}", idString);
			}
			continue;
		    }

		    // FEATURES

		    JSONObject featureObject = new JSONObject();

		    com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.Feature.Builder featureBuilder = Feature
			    .newBuilder();

		    // GEOMETRY

		    if (returnGeometry) {
			JSONObject geometry = new JSONObject();
			Double s = reader.evaluateNumber("//*:southBoundLatitude[1]/*:Decimal").doubleValue();
			Double w = reader.evaluateNumber("//*:westBoundLongitude[1]/*:Decimal").doubleValue();
			Double x = null;
			Double y = null;
			if (outSR != null && outSR.equals("4326")) {
			    x = w;
			    y = s;

			} else {
			    SimpleEntry<Double, Double> dest = CRSUtils.translatePoint(new SimpleEntry<Double, Double>(s, w),
				    CRS.EPSG_4326(), CRS.EPSG_3857());
			    x = dest.getKey();
			    y = dest.getValue();
			}
			geometry.put("x", x);
			geometry.put("y", y);

			featureObject.put("geometry", geometry);

			// PBF
			long xLong = new Double(x / xScale).longValue();
			long yLong = new Double(y / yScale).longValue();
			featureBuilder = featureBuilder.setGeometry(Geometry.newBuilder().addCoords(xLong).addCoords(yLong).build());
		    }

		    // ATTRIBUTES

		    List<Value> attributes = new ArrayList<>();

		    JSONObject attribute = new JSONObject();

		    String platformId = reader.evaluateString("//*:extension/*:uniquePlatformId");

		    Object lastInsertedValue = null;
		    for (Field field : returnFields) {
			Object value = null;

			if (field.getType().equals(ESRIFieldType.WHOS_PAGE)) {
			    Boolean executable = reader
				    .evaluateBoolean("//*:extension/*:accessReports/*:report/*:lastSucceededTest[.='EXECUTION']");

			    value = "not available";
			    if (executable) {

				if (platformId != null) {
				    // if (optionalExecutable.get().booleanValue()) {

				    // remove view
				    if (link.contains("/view/")) {
					String firstPart = link.substring(0, link.indexOf("/view/"));
					String secondPart = link.substring(link.indexOf("/view/"));
					secondPart = secondPart.replace("/view/", "");
					secondPart = secondPart.substring(secondPart.indexOf("/"));
					link = firstPart + secondPart;
				    }

				    if (link.contains("/essi/")) {
					link = link.replace("/essi", "");
				    }

				    value = link + platformId + "/";
				}

			    }
			} else if (field.getType().equals(ESRIFieldType.LATITUDE)) {
			    Double s = reader.evaluateNumber("//*:southBoundLatitude[1]/*:Decimal").doubleValue();
			    value = "" + s;
			} else if (field.getType().equals(ESRIFieldType.LONGITUDE)) {
			    Double w = reader.evaluateNumber("//*:westBoundLongitude[1]/*:Decimal").doubleValue();
			    value = "" + w;
			} else if (field.getType().equals(ESRIFieldType.OID)) {
			    value = layer.getResourceIdentifier(view, reader);
			} else if (field.getType().equals(ESRIFieldType.START_DATE)) {
			    Node beginNode = reader.evaluateNode("//*:TimePeriod[1]/*:beginPosition");
			    value = readTime(reader, beginNode);
			} else if (field.getType().equals(ESRIFieldType.END_DATE)) {
			    Node endNode = reader.evaluateNode("//*:TimePeriod[1]/*:endPosition");
			    value = readTime(reader, endNode);

			} else if (field.getType().equals(ESRIFieldType.ISO_TITLE)) {
			    String title = reader.evaluateString(
				    "//*:identificationInfo[1]/*:MD_DataIdentification[1]/*:citation[1]/*:CI_Citation/*:title/*[1]");
			    value = title;
			} else if (field.getBNHSProperty() != null) {
			    BNHSProperty property = field.getBNHSProperty();
			    value = bnhsMap.get(property.getLabel());
			    switch (field.getType()) {
			    case BOOLEAN:
				if (value != null && !value.toString().isEmpty()) {
				    value = value.toString().toLowerCase();
				    if (value.equals("y") || value.equals("yes")) {
					value = "true";
				    }
				    value = Boolean.parseBoolean(value.toString());
				} else {
				    value = false;
				}
				break;
			    case DATE:
			    case START_DATE:
			    case END_DATE:
				break;
			    case DOUBLE:
				if (value != null && !value.toString().isEmpty()) {
				    try {
					value = Double.parseDouble(value.toString());
				    } catch (Exception e) {
					// TODO: handle exception
				    }
				}
				break;
			    case INTEGER:
				if (value != null && !value.toString().isEmpty()) {
				    value = Integer.parseInt(value.toString());
				}
				break;
			    case OID:
				try {
				    value = Long.parseLong(value.toString());
				} catch (Exception e) {
				    GSLoggerFactory.getLogger(getClass()).error("Unparsable id: {}", value);
				}
			    case ISO_TITLE:
			    case LATITUDE:
			    case LONGITUDE:
			    case STRING:
			    default:
				if (value != null && !value.toString().isEmpty()) {
				    value = value.toString();
				}
			    }
			}
			switch (field.getType().getType()) {
			case esriFieldTypeSmallInteger:
			case esriFieldTypeInteger:
			case esriFieldTypeSingle:
			    attributes.add(Value.newBuilder().setSintValue((Integer) value).build());
			    break;
			case esriFieldTypeDouble:
			    if (value == null) {
				attributes.add(Value.newBuilder().setDoubleValue(-9999).build());
			    } else {
				if (value instanceof String) {
				    if (value.equals("")) {
					attributes.add(Value.newBuilder().setDoubleValue(-9999).build());
				    } else {
					try {
					    value = Double.parseDouble((String) value);
					    attributes.add(Value.newBuilder().setDoubleValue((Double) value).build());
					} catch (Exception e) {
					    attributes.add(Value.newBuilder().setDoubleValue(-9999).build());
					}

				    }
				}
			    }
			    break;
			case esriFieldTypeString:
			case esriFieldTypeOID:
			    String s = "";
			    if (value != null) {
				s = value.toString();
			    }
			    attributes.add(Value.newBuilder().setStringValue(s).build());
			    break;
			case esriFieldTypeDate:
			    if (value == null) {
				attributes.add(Value.newBuilder().setInt64Value(-9999).build());
			    } else {
				if (value instanceof String) {
				    String str = (String) value;
				    attributes.add(Value.newBuilder().setStringValue(str).build());
				} else {
				    System.err.println("unexpected");
				}

			    }

			    break;
			case esriFieldTypeGeometry:
			case esriFieldTypeBlob:
			case esriFieldTypeRaster:
			case esriFieldTypeGUID:
			case esriFieldTypeGlobalID:
			case esriFieldTypeXML:
			default:
			    attributes.add(Value.newBuilder().setStringValue((String) value).build());
			    break;
			}

			attribute.put(field.getName(), value);
			lastInsertedValue = value;
		    }

		    if (distinct && returnFields.size() == 1 && distinctValues.contains(lastInsertedValue)) {
			// nothing to do, already present && distinct asked
		    } else {
			// JSON
			featureObject.put("attributes", attribute);
			featureArray.put(featureObject);
			// PBF
			featureBuilder = featureBuilder.addAllAttributes(attributes);
			features.add(featureBuilder.build());
			
			distinctValues.add(lastInsertedValue);
			
		    }

		}
		if (idOnly) {
		    response.put("objectIds", idArray);
		} else {
		    response.put("features", featureArray);
		}

	    }

	    String ret = response.toString();
	    // JS callback addition
	    String callback = esriRequest.getParameter("callback");
	    if (callback != null && !callback.isEmpty()) {
		ret = callback + "(" + ret + ");";
	    }
	    // long end = System.currentTimeMillis() - start;
	    // System.err.println(end + " ms");
	    String format = esriRequest.getParameter("f");
	    if (format != null && format.equals("pbf")) {
		// PBF format

		SpatialReference spatialReference = SpatialReference.newBuilder().setWkid(wkid).setLatestWkid(latestWkid).build();

		Scale scale = Scale.newBuilder().setXScale(xScale).setYScale(-yScale).build();
		Translate translate = Translate.newBuilder().setXTranslate(0.).setYTranslate(0.).build();
		Transform transform = Transform.newBuilder().setScale(scale).setTranslate(translate).build();
		FeatureResult featureResult = featureResultBuilder//
			.setTransform(transform)//
			.setSpatialReference(spatialReference)//
			.addAllFields(pbfFields) //
			.addAllFeatures(features)//
			.build();
		QueryResult queryResult = QueryResult.newBuilder().setFeatureResult(featureResult).build();
		FeatureCollectionPBuffer featureCollection = FeatureCollection.FeatureCollectionPBuffer.newBuilder()
			.setQueryResult(queryResult).build();

		byte[] bytes = featureCollection.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		return buildResponse(bis, "application/x-protobuf");
	    }
	    // default: JSON
	    return buildResponse(ret, "application/json");

	} catch (

	Exception ex) {
	    ex.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ESRI_FEATURE_SERVER_RESULT_SET_FORMATTER_ERROR);
	}
    }

    private String readTime(XMLDocumentReader reader, Node node) throws XPathExpressionException {
	String ret;
	String endIndeterminate = reader.evaluateString(node, "@indeterminatePosition");
	if (endIndeterminate != null && endIndeterminate.equals("now")) {
	    ret = ISO8601DateTimeUtils.getISO8601DateTime();
	} else {
	    String frame = reader.evaluateString(node, "@frame");
	    ret = reader.evaluateString(node, ".");
	    if (frame != null && frame.equals("http://essi-lab.eu/time-constants/before-now")) {
		FrameValue fv = FrameValue.valueOf(ret);
		long ms = new Date().getTime() - fv.asMillis();
		ISO8601DateTimeUtils.getISO8601DateTime(new Date(ms));
	    }
	}
	return ret;
    }

    // private GSResource initResource(GSResource resource, Node node) {
    // try {
    // if (resource == null) {
    // return GSResource.create(node);
    // }
    // } catch (JAXBException e) {
    // e.printStackTrace();
    // }
    // return resource;
    // }

    @Override
    public FormattingEncoding getEncoding() {

	return ESRI_FEATURE_SERVER_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private static Response buildResponse(Object entity, String mimeType) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(entity);
	builder = builder.type(mimeType);

	return builder.build();
    }
}
