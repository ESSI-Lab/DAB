package eu.essi_lab.profiler.esri.feature.query;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.xml.xpath.XPathExpressionException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialEntity;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.esri.feature.ESRIFieldType;
import eu.essi_lab.profiler.esri.feature.FeatureLayer;
import eu.essi_lab.profiler.esri.feature.Field;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;

public class CachedCollections {
    private static CachedCollections instance = new CachedCollections();

    /**
     * Map view:layer name to collection
     */
    private HashMap<String, DefaultFeatureCollection> collections = new HashMap<>();
    private HashMap<String, SimpleFeatureType> featureTypes = new HashMap<>();

    public static CachedCollections getInstance() {
	return instance;
    }

    public boolean isPresent(String view, FeatureLayer layer) {
	return collections.containsKey(view + ":" + layer.getName());
    }

    private HashSet<String> inPreparation = new HashSet<String>();

    public static String geometryAttributeName = "geom";

    public void prepare(WebRequest request, String view, FeatureLayer layer){
	if (request == null) {
	    request = new WebRequest();
	}
	WebRequest req = request;
	String layerName = layer.getName();
	String id = view + ":" + layerName;
	synchronized (collections) {
	    DefaultFeatureCollection c = collections.get(id);
	    if (c == null && !inPreparation.contains(id)) {
		GSLoggerFactory.getLogger(getClass()).info("Preparing cached layer {}", id);

		inPreparation.add(id);
	    } else {
		return;
	    }
	}
	// PREPARATION
	Thread t = new Thread() {
	    @Override
	    public void run() {
		try {
		    DefaultFeatureCollection c = new DefaultFeatureCollection();

		    ServiceLoader<IDiscoveryNodeExecutor> loader = ServiceLoader.load(IDiscoveryNodeExecutor.class);
		    IDiscoveryNodeExecutor executor = loader.iterator().next();

		    ResultSet<Node> resultSet = null;
		    ResultSet<Node> tmpResultSet = new ResultSet<>();

		    FeatureQueryRequestTransformer transformer = new FeatureQueryRequestTransformer();
		    DiscoveryMessage discoveryMessage = transformer.transform(req);
		    discoveryMessage.setUserBond(null);
		    Page page = discoveryMessage.getPage();
		    int pageSize = page.getSize();
		    do {

			discoveryMessage.setRequestId(CachedCollections.class.getSimpleName());

			StorageInfo storageUri = FeatureQueryHandler.getStorageURI(discoveryMessage);

			WebRequestTransformer.setView(view, storageUri, discoveryMessage);

			discoveryMessage.setSources(ConfigurationWrapper.getAllSources());
			discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

			tmpResultSet = executor.retrieveNodes(discoveryMessage);

			if (resultSet == null) {
			    resultSet = tmpResultSet;
			} else {
			    resultSet.getResultsList().addAll(tmpResultSet.getResultsList());

			}
			page.setStart(page.getStart() + pageSize);

		    } while (resultSet.getResultsList().size() < resultSet.getCountResponse().getCount()
			    && !tmpResultSet.getResultsList().isEmpty());
		    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

		    for (Node result : resultSet.getResultsList()) {
			XMLDocumentReader reader = new XMLDocumentReader(result.getOwnerDocument());

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

			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

			typeBuilder.setName(layer.getName());
			typeBuilder.setCRS(CRS.decode("CRS:84"));
			List<Field> fields = layer.getFields();
			typeBuilder.add(geometryAttributeName, Point.class);

			for (Field field : fields) {

			    typeBuilder.add(field.getName(), field.getType().encode());
			}
			SimpleFeatureType featureType = typeBuilder.buildFeatureType();
			featureTypes.put(id, featureType);
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

			String platformId = reader.evaluateString("//*:extension/*:uniquePlatformId");

			Double s = reader.evaluateNumber("//*:southBoundLatitude[1]/*:Decimal").doubleValue();
			Double w = reader.evaluateNumber("//*:westBoundLongitude[1]/*:Decimal").doubleValue();
			Coordinate coordinate = new Coordinate(w, s);
			Point geometry = geometryFactory.createPoint(coordinate);
			featureBuilder.add(geometry);

			for (Field field : fields) {
			    Object value = null;
			    if (field.getType().equals(ESRIFieldType.WHOS_PAGE)) {
				Boolean executable = reader
					.evaluateBoolean("//*:extension/*:accessReports/*:report/*:lastSucceededTest[.='EXECUTION']");
				value = "not available";
				if (executable) {
				    if (platformId != null) {
					value = "${HOSTNAME}/gs-service/services/bnhs/station/" + platformId + "/";
				    }

				}
			    } else if (field.getType().equals(ESRIFieldType.LATITUDE)) {
				s = reader.evaluateNumber("//*:southBoundLatitude[1]/*:Decimal").doubleValue();
				value = "" + s;
			    } else if (field.getType().equals(ESRIFieldType.LONGITUDE)) {
				w = reader.evaluateNumber("//*:westBoundLongitude[1]/*:Decimal").doubleValue();
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
			    featureBuilder.add(value);
			}

			SimpleFeature feature = featureBuilder.buildFeature(null);
			// feature.getUserData().put(colorAttributeName, getColor(record.getMetadataIdentifier()));
			// feature.getUserData().put(beginAttributeName, record.getBegin());
			// feature.getUserData().put(endAttributeName, record.getEnd());
			c.add(feature);

		    }

		    synchronized (collections) {
			GSLoggerFactory.getLogger(getClass()).info("Prepared cached layer {} size {}", id, c.size());
			inPreparation.remove(id);
			collections.put(id, c);
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }

	};
	t.start();

    }

    public static final String BNHS_SEPARATOR = "\t";

    private Date readTime(XMLDocumentReader reader, Node node) throws XPathExpressionException {
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
	return ISO8601DateTimeUtils.parseISO8601ToDate(ret).get();
    }

    public SimpleFeatureCollection queryCollection(String vid, FeatureLayer layer, Optional<Bond> userBond) {
	String id = vid + ":" + layer.getName();
	SimpleFeatureType featureType = featureTypes.get(id);

	DefaultFeatureCollection collection = collections.get(id);
	if (collection == null) {
	    return null;
	}
	if (userBond.isEmpty() || userBond.get() == null) {
	    return collection;
	}
	Bond bond = userBond.get();
	DiscoveryBondParser parser = new DiscoveryBondParser(bond);

	List<Filter> filters = new ArrayList<Filter>();
	List<Filter> orFilters = new ArrayList<Filter>();
	parser.parse(new DiscoveryBondHandler() {

	    @Override
	    public void startLogicalBond(LogicalBond bond) {

	    }

	    @Override
	    public void separator() {

	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {

	    }

	    @Override
	    public void endLogicalBond(LogicalBond bond) {

	    }

	    @Override
	    public void viewBond(ViewBond bond) {

	    }

	    @Override
	    public void spatialBond(SpatialBond sb) {
		SpatialEntity pv = sb.getPropertyValue();
		if (pv instanceof SpatialExtent) {
		    SpatialExtent se = (SpatialExtent) pv;
		    double minx = se.getWest();
		    double miny = se.getSouth();
		    double maxx = se.getEast();
		    double maxy = se.getNorth();
		    filters.add(ff.bbox(ff.property(geometryAttributeName), minx, miny, maxx, maxy, "CRS:84"));
		}
	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond bond) {
		BondOperator operator = bond.getOperator();
		MetadataElement element = bond.getProperty();
		Field field = layer.getField(element);
		// Integer position = layer.getPosition(field);
		String value = bond.getPropertyValue();
		if (value.contains("(")) {
		    value = value.replace("(", "%");
		}
		if (value.contains(")")) {
		    value = value.replace(")", "%");
		}
		String attributeName = field.getName();
		Expression expr1 = ff.property(attributeName);
		if (operator.equals(BondOperator.TEXT_SEARCH)) {
		    filters.add(ff.like(expr1, value, "*", "%", "!", false));
		} else {
		    filters.add(ff.like(expr1, value, "*", "%", "!", true));
		}

	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {

	    }

	    @Override
	    public void resourcePropertyBond(ResourcePropertyBond bond) {

	    }

	    @Override
	    public void customBond(QueryableBond<String> bond) {

	    }
	});

	if (!orFilters.isEmpty()) {
	    filters.add(ff.or(orFilters));
	}

	Filter filter = null;
	switch (filters.size()) {
	case 0:
	    return collection;
	case 1:
	    filter = filters.get(0);
	    break;
	default:
	    filter = ff.and(filters);
	    break;
	}

	SimpleFeatureCollection ret = collection.subCollection(filter);
	GSLoggerFactory.getLogger(getClass()).info("returnin sub collection, size {}", ret.size());
	return ret;

    }

    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
}
