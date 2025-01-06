package eu.essi_lab.profiler.wms.extent.map;

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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX3857;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.Response;
import eu.essi_lab.access.datacache.ResponseListener;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.profiler.wms.extent.WMSLayer;

public class Layer {

    private String name;
    private Optional<String> viewId;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Layer(String name, Optional<String> viewId) {
	this.name = name;
	this.viewId = viewId;

    }

    private boolean completed = false;
    private Date lastHarvesting = null;
    private DefaultFeatureCollection collectionPoints;
    private HashSet<String> featureIdentifiers;
    private DefaultFeatureCollection collectionLines;

    public boolean isCompleted() {
	return completed;
    }

    public void setCompleted() {
	this.completed = true;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public List<StationRecord> getStations(BBOX bbox, Date begin, Date end) {
	List<StationRecord> ret = new ArrayList<>();
	SimpleFeatureCollection targetPoints = collectionPoints;
	SimpleFeatureCollection targetLines = collectionLines;

	if (begin != null && end != null) {
	    Filter beginDateFilter = ff.lessOrEqual(ff.property(beginAttributeName), ff.literal(end));
	    Filter endDateFilter = ff.greaterOrEqual(ff.property(endAttributeName), ff.literal(begin));
	    Filter timeFilter = ff.and(beginDateFilter, endDateFilter);
	    targetPoints = targetPoints.subCollection(timeFilter);
	    targetLines = targetLines.subCollection(timeFilter);
	}
	if (bbox != null) {
	    org.opengis.filter.spatial.BBOX bboxFilter;
	    switch (bbox.getCrs()) {
	    case "CRS:84":
		bboxFilter = ff.bbox(geometryAttributeName, bbox.getMinx().doubleValue(), bbox.getMiny().doubleValue(),
			bbox.getMaxx().doubleValue(), bbox.getMaxy().doubleValue(), bbox.getCrs());
		break;
	    default:
		bboxFilter = ff.bbox(geometryAttributeName, bbox.getMiny().doubleValue(), bbox.getMinx().doubleValue(),
			bbox.getMaxy().doubleValue(), bbox.getMaxx().doubleValue(), bbox.getCrs());
		break;
	    }
	    targetPoints = targetPoints.subCollection(bboxFilter);
	    targetLines = targetLines.subCollection(bboxFilter);
	}

	SimpleFeatureIterator linesIterator = targetLines.features();
	SimpleFeatureIterator pointsIterator = targetPoints.features();
	List<SimpleFeatureIterator> list = new ArrayList<SimpleFeatureIterator>();
	list.add(linesIterator);
	list.add(pointsIterator);

	for (SimpleFeatureIterator iterator : list) {

	    while (iterator.hasNext()) {
		SimpleFeature next = iterator.next();
		Date b = (Date) next.getAttribute(beginAttributeName);
		Date e = (Date) next.getAttribute(endAttributeName);
		Object geom = next.getDefaultGeometry();
		String id = (String) next.getAttribute(idAttributeName);
		String name = (String) next.getAttribute(nameAttributeName);
		Envelope bbox4326 = null;
		Envelope bbox3857 = null;
		if (geom instanceof Geometry) {
		    Geometry line = (Geometry) geom;

		    try {
			bbox4326 = calculateBbox(line, "EPSG:4326");
			bbox3857 = calculateBbox(line, "EPSG:3857");
		    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		}
		StationRecord station;
		try {
		    BBOX4326 b4326 = new BBOX4326(//
			    new BigDecimal(bbox4326.getMinY()), //
			    new BigDecimal(bbox4326.getMaxY()), //
			    new BigDecimal(bbox4326.getMinX()), //
			    new BigDecimal(bbox4326.getMaxX())//
		    );
		    BBOX3857 b3857 = new BBOX3857(//
			    new BigDecimal(bbox3857.getMinY()), //
			    new BigDecimal(bbox3857.getMaxY()), //
			    new BigDecimal(bbox3857.getMinX()), //
			    new BigDecimal(bbox3857.getMaxX())//
		    );
		    station = new StationRecord(b4326, b3857, id, id, name, null, null, "i-change", null);
		    station.setPlatformIdentifier(id);
		    ret.add(station);
		} catch (Exception e1) {
		    e1.printStackTrace();
		}

	    }
	}
	return ret;

    }

    public static Envelope calculateBbox(Geometry lineString, String targetCRS) throws Exception {
	// Parsing the EPSG code to get CoordinateReferenceSystem
	CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
	CoordinateReferenceSystem targetCrs = CRS.decode(targetCRS);

	// Create a transform from sourceCRS to targetCRS
	MathTransform transform = CRS.findMathTransform(sourceCRS, targetCrs, true);

	// Transform the LineString to the target CRS
	Geometry transformedGeometry = JTS.transform(lineString, transform);

	// Calculate the bbox of the transformed LineString
	Envelope bbox = transformedGeometry.getEnvelopeInternal();
	return bbox;
    }

    public StreamingOutput getImageResponse(String version, String crs, String bboxString, Integer width, Integer height, String format,
	    String time) {
	if (bboxString != null && crs != null) {
	    String[] split = bboxString.split(",");
	    BigDecimal minx = null;
	    BigDecimal miny = null;
	    BigDecimal maxx = null;
	    BigDecimal maxy = null;

	    //
	    if (version.equals("1.3.0") && crs.equals("EPSG:4326")) {
		// northing, easting
		miny = new BigDecimal(split[0]);
		minx = new BigDecimal(split[1]);
		maxy = new BigDecimal(split[2]);
		maxx = new BigDecimal(split[3]);
	    } else {
		// case "CRS:84":
		// case "EPSG:3857":
		// easting, northing
		minx = new BigDecimal(split[0]);
		miny = new BigDecimal(split[1]);
		maxx = new BigDecimal(split[2]);
		maxy = new BigDecimal(split[3]);
	    }

	    if (crs.equals("EPSG:4326")) {
		crs = "CRS:84";
	    }

	    double bminx = minx.doubleValue();
	    double bminy = miny.doubleValue();
	    double bmaxx = maxx.doubleValue();
	    double bmaxy = maxy.doubleValue();
	    CoordinateReferenceSystem crs2;
	    try {
		crs2 = CRS.decode(crs);

		ReferencedEnvelope mapBounds = new ReferencedEnvelope(bminx, bmaxx, bminy, bmaxy, crs2);

		return new StreamingOutput() {

		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
			MapContent mapContent = new MapContent();
			mapContent.setTitle(name);

			SimpleFeatureCollection targetPoints = collectionPoints;
			SimpleFeatureCollection targetLines = collectionLines;
			if (time != null && !time.isEmpty()) {
			    Date bottomTime = null;
			    Date upTime = null;
			    if (time.contains("/")) {
				String[] stime = time.split("/");
				Optional<Date> bottomTimeOpt = ISO8601DateTimeUtils.parseISO8601ToDate(stime[0]);
				Optional<Date> upTimeOpt = ISO8601DateTimeUtils.parseISO8601ToDate(stime[1]);
				if (bottomTimeOpt.isPresent() && upTimeOpt.isPresent()) {
				    bottomTime = bottomTimeOpt.get();
				    upTime = upTimeOpt.get();
				}
			    } else {
				if (time.contains("T")) {
				    // specific time
				    Optional<Date> bottomTimeOpt = ISO8601DateTimeUtils.parseISO8601ToDate(time);
				    if (bottomTimeOpt.isPresent()) {
					bottomTime = bottomTimeOpt.get();
					upTime = bottomTime;
				    }
				} else {
				    Optional<Date> dateOpt = ISO8601DateTimeUtils.parseISO8601ToDate(time);
				    if (dateOpt.isPresent()) {
					Date date = dateOpt.get();
					LocalDate localDate = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
					bottomTime = ISO8601DateTimeUtils.parseISO8601ToDate(localDate.atStartOfDay().format(formatter))
						.get();
					upTime = ISO8601DateTimeUtils
						.parseISO8601ToDate(localDate.plusDays(1).atStartOfDay().format(formatter)).get();
				    }
				}

			    }
			    if (upTime != null && bottomTime != null) {
				Filter beginDateFilter = ff.lessOrEqual(ff.property(beginAttributeName), ff.literal(upTime));
				Filter endDateFilter = ff.greaterOrEqual(ff.property(endAttributeName), ff.literal(bottomTime));

				Filter timeFilter = ff.and(beginDateFilter, endDateFilter);
				// print(targetPoints);
				targetPoints = targetPoints.subCollection(timeFilter);
				// print(targetPoints);
				targetLines = targetLines.subCollection(timeFilter);
			    }
			}

			FeatureLayer layerPoints = new FeatureLayer(targetPoints, createDefaultStylePoints(targetPoints));
			mapContent.addLayer(layerPoints);
			FeatureLayer layerLines = new FeatureLayer(targetLines, createDefaultStyleLines(targetLines));
			mapContent.addLayer(layerLines);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			StreamingRenderer renderer = new StreamingRenderer();
			renderer.setMapContent(mapContent);
			renderer.paint(graphics, new Rectangle(width, height), mapBounds);

			try {
			    ImageIO.write(image, "png", output);
			} catch (IOException ee) {
			    ee.printStackTrace();
			}

		    }

		};
	    } catch (

	    Exception e) {
		e.printStackTrace();
	    }

	}
	return null;
    }

    private void print(SimpleFeatureCollection targetPoints) {
	SimpleFeatureIterator sit = targetPoints.features();
	while (sit.hasNext()) {
	    SimpleFeature si = sit.next();
	    String str = si.getAttribute(beginAttributeName) + " " + si.getAttribute(endAttributeName) + " " + si.getDefaultGeometry();
	    System.out.println(str);
	}

    }

    static DataCacheConnector dataCacheConnector;
    static {
	dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();
	if (dataCacheConnector == null) {
	    DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	    try {
		dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(Layer.class).error(e);
	    }

	}
    }

    public void prepare() {
	GSLoggerFactory.getLogger(getClass()).info("Preparing cached layer {}", name);

	this.collectionPoints = new DefaultFeatureCollection();
	this.collectionLines = new DefaultFeatureCollection();
	featureIdentifiers = new HashSet<>();

	Thread t = new Thread() {
	    @Override
	    public void run() {

		try {

		    String[] layerSplit = new String[] {};
		    if (Layer.this.name.contains(",")) {
			layerSplit = Layer.this.name.split(",");
		    } else {
			layerSplit = new String[] { Layer.this.name };
		    }

		    List<SimpleEntry<String, String>> properties = new ArrayList<>();

		    List<WMSLayer> wmsLayers = WMSLayer.decode(viewId);
		    for (String layer : layerSplit) {
			for (WMSLayer wmsLayer : wmsLayers) {
			    if (wmsLayer.getLayerName().equals(layer)) {
				properties.add(new SimpleEntry<>(wmsLayer.getProperty(), wmsLayer.getValue()));
			    }
			}
		    }

		    SimpleEntry<String, String>[] props = properties.toArray(new SimpleEntry[] {});

		    ResponseListener<StationRecord> listener = new ResponseListener<StationRecord>() {

			@Override
			public void recordsReturned(Response<StationRecord> response) {
			    GSLoggerFactory.getLogger(getClass()).info("Creating collections... {} points {} lines",
				    collectionPoints.size(), collectionLines.size());
			    List<StationRecord> records = response.getRecords();

			    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

			    rec: for (int r = 0; r < records.size(); r++) {
				try {
				    StationRecord record = records.get(r);
				    if (r % 400 == 0) {
					GSLoggerFactory.getLogger(getClass()).info("index: {}" + r);
				    }

				    BBOX4326 bbox = record.getBbox4326();
				    BigDecimal south = bbox.getSouth();
				    BigDecimal north = bbox.getNorth();
				    BigDecimal east = bbox.getEast();
				    BigDecimal west = bbox.getWest();

				    Geometry geometry = null;

				    if (Math.abs(south.doubleValue() - north.doubleValue()) > 0.00000001
					    && Math.abs(east.doubleValue() - west.doubleValue()) > 0.00000001) {

					String shape = record.getShape();

					if (shape != null && shape.toLowerCase().contains("linestring")) {

					    shape = shape.substring(shape.indexOf("("));
					    shape = shape.replace("(", "").replace(")", "").trim();
					    List<List<Double>> ps = new ArrayList<List<Double>>();
					    if (shape.contains(",")) {
						String[] split = shape.split(",");
						for (String point : split) {
						    point = point.trim();
						    if (point.contains(" ")) {
							String[] doubles = point.split(" ");
							if (doubles.length > 1) {
							    List<Double> p = new ArrayList<Double>();
							    double lon = Double.parseDouble(doubles[0]);
							    p.add(lon);
							    double lat = Double.parseDouble(doubles[1]);
							    p.add(lat);
							    if (doubles.length > 2) {
								double alt = Double.parseDouble(doubles[2]);
								p.add(alt);
							    }
							    ps.add(p);
							}
						    }
						}
					    }

					    if (!ps.isEmpty()) {
						Coordinate[] coords = new Coordinate[ps.size()];
						for (int i = 0; i < ps.size(); i++) {
						    List<Double> coord = ps.get(i);
						    Coordinate coordinate = null;
						    if (coord.size() == 3) {
							coordinate = new Coordinate(coord.get(1), coord.get(0), coord.get(2));
						    } else {
							coordinate = new Coordinate(coord.get(1), coord.get(0));
						    }
						    coords[i] = coordinate;
						}
						geometry = geometryFactory.createLineString(coords);
					    }
					} else {
					    if (true)
						continue rec;
					}

				    } else {

					Coordinate coordinate = new Coordinate(west.doubleValue(), south.doubleValue());
					geometry = geometryFactory.createPoint(coordinate);

				    }

				    SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
				    typeBuilder.setName("MyFeatureType");
				    typeBuilder.setCRS(CRS.decode("CRS:84"));
				    if (geometry != null) {
					String n = record.getDatasetName() != null ? record.getDatasetName() : record.getPlatformName();
					typeBuilder.add(geometryAttributeName, geometry.getClass());
					typeBuilder.add(colorAttributeName, String.class);
					typeBuilder.add(beginAttributeName, Date.class);
					typeBuilder.add(endAttributeName, Date.class);
					typeBuilder.add(harvestingAttributeName, Date.class);
					typeBuilder.add(idAttributeName, String.class);
					typeBuilder.add(nameAttributeName, String.class);
					SimpleFeatureType featureType = typeBuilder.buildFeatureType();
					String c = getColor(record.getMetadataIdentifier());
					SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
					featureBuilder.add(geometry);
					featureBuilder.add(c);
					featureBuilder.add(record.getBegin());
					featureBuilder.add(record.getEnd());
					featureBuilder.add(record.getLastHarvesting());
					featureBuilder.add(record.getPlatformIdentifier());
					featureBuilder.add(n);
					SimpleFeature feature = featureBuilder.buildFeature(null);
					if (!featureIdentifiers.contains(record.getPlatformIdentifier())) {
					    featureIdentifiers.add(record.getPlatformIdentifier());
					    if (geometry instanceof Point) {
						collectionPoints.add(feature);
					    } else {
						collectionLines.add(feature);
					    }
					    Date tmpHarvesting = record.getLastHarvesting();
					    if (lastHarvesting == null || lastHarvesting.before(tmpHarvesting)) {
						lastHarvesting = tmpHarvesting;
					    }
					}

				    }

				} catch (Exception e) {
				    e.printStackTrace();
				}

			    }

			    if (response.isCompleted()) {
				completed = true;
				GSLoggerFactory.getLogger(getClass()).info("Prepared cached layer {}... {} points {} lines", name,
					collectionPoints.size(), collectionLines.size());
			    } else {
				GSLoggerFactory.getLogger(getClass()).info("Continuing preparation of cached layer {}", name);
			    }

			}

			private String getColor(String metadataIdentifier) {
			    int r = 0;
			    int g = 0;
			    int b = 0;
			    if (metadataIdentifier != null) {
				Random random = new Random(metadataIdentifier.hashCode());
				r = (int) Math.floor(random.nextDouble() * 255);
				g = (int) Math.floor(random.nextDouble() * 255);
				b = (int) Math.floor(random.nextDouble() * 255);
			    }
			    int rgb = (r << 16) | (g << 8) | b;
			    String hex = Integer.toHexString(rgb & 0xFFFFFF);
			    hex = String.format("#%06X", (0xFFFFFF & Integer.parseInt(hex, 16)));
			    return hex;
			}

			@Override
			public boolean isCompleted() {

			    return false;
			}
		    };
		    BBOX bbox = null;
		    dataCacheConnector.getStationsWithProperties(listener, lastHarvesting, bbox, null, false, props);
		    GSLoggerFactory.getLogger(getClass()).info("layer implementation completed");
		    setCompleted();

		} catch (Exception e) {

		}
	    }
	};

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	scheduler.scheduleAtFixedRate(t, 0, 5, TimeUnit.MINUTES);

    }

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    private enum GeomType {
	POINT, LINE, POLYGON, LINESTRING
    };

    /**
     * Create a default Style for feature display
     * 
     * @param collection2
     */
    private static Style createDefaultStyleLines(SimpleFeatureCollection collection) {
	FeatureTypeStyle fts = sf.createFeatureTypeStyle();
	fts.rules().add(createRule(GeomType.LINE, LINE_COLOUR, FILL_COLOUR));
	Style style = sf.createStyle();
	style.featureTypeStyles().add(fts);
	return style;
    }

    private static Style createDefaultStylePoints(SimpleFeatureCollection collection) {
	FeatureTypeStyle fts = sf.createFeatureTypeStyle();
	fts.rules().add(createRule(GeomType.POINT, LINE_COLOUR, FILL_COLOUR));
	Style style = sf.createStyle();
	style.featureTypeStyles().add(fts);
	return style;
    }

    public static String geometryAttributeName = "geom";
    public static String colorAttributeName = "color";
    public static String beginAttributeName = "begin";
    public static String endAttributeName = "end";
    public static String idAttributeName = "id";
    public static String nameAttributeName = "name";
    public static String harvestingAttributeName = "harvesting";
    private static final Color LINE_COLOUR = Color.GREEN;
    private static final Color FILL_COLOUR = Color.GREEN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 0.5f;
    private static final float LINE_WIDTH = 10.0f;
    private static final float POINT_SIZE = 10.0f;

    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing a Symbolizer
     * tailored to the geometry type of the features that we are displaying.
     */
    private static Rule createRule(GeomType geometryType, Color outlineColor, Color fillColor) {
	Symbolizer symbolizer = null;
	Fill fill = null;

	PropertyName colorExpression = ff.property(colorAttributeName);
	Stroke stroke = sf.createStroke(colorExpression, // Color based on attribute value
		ff.literal(LINE_WIDTH) // Stroke width
	);

	switch (geometryType) {
	case POLYGON:
	    fill = sf.createFill(colorExpression, ff.literal(OPACITY));
	    symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
	    break;
	case LINE:
	case LINESTRING:
	    // fill = sf.createFill(colorExpression, ff.literal(OPACITY));
	    symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
	    break;

	case POINT:
	    fill = sf.createFill(colorExpression, ff.literal(OPACITY));

	    Mark mark = sf.getCircleMark();
	    mark.setFill(fill);
	    mark.setStroke(stroke);

	    Graphic graphic = sf.createDefaultGraphic();
	    graphic.graphicalSymbols().clear();
	    graphic.graphicalSymbols().add(mark);
	    graphic.setSize(ff.literal(POINT_SIZE));

	    symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
	    break;
	}
	Rule rule = sf.createRule();
	rule.symbolizers().add(symbolizer);
	return rule;

    }

}
