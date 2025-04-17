package eu.essi_lab.whos.prohmsat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.api.style.Fill;
import org.geotools.api.style.Graphic;
import org.geotools.api.style.Mark;
import org.geotools.api.style.Rule;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Style;
import org.geotools.api.style.StyleFactory;
import org.geotools.api.style.Symbolizer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.model.resource.data.CRSUtils;

public class ProhmsatShapeReader {

    public static void main(String[] args) throws MalformedURLException, Exception {
	File file = new File("/home/boldrini/prohmsat/hrc_plata_daqcs_hmfs_basin_shapefiles/hrc_plata_daqcs_hmfs_basin_geom.shp");

	DataCacheConnector connector;

	String dbname = "testshape1";
	String sourceId = "test";

	// connector= DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_DOCKERHUB_1_3,
	// new URL("http://localhost:9200"), "admin", "admin", dbname);
	// connector = DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_AWS,
	// new URL(System.getProperty("dataCacheHost")), System.getProperty("dataCacheUser"),
	// System.getProperty("dataCachePassword"),
	// dbname);
	//
	// connector.configure(OpenSearchConnector.FLUSH_INTERVAL_MS, "1000");
	// connector.configure(OpenSearchConnector.MAX_BULK_SIZE, "1000");
	// connector.configure(OpenSearchConnector.CACHED_DAYS, "0");

	java.util.Map<String, Object> connect = new HashMap();
	connect.put("url", file.toURI().toString());

	DataStore dataStore = DataStoreFinder.getDataStore(connect);
	String[] typeNames = dataStore.getTypeNames();
	String typeName = typeNames[0];

	System.out.println("Reading content " + typeName);

	FeatureSource featureSource = dataStore.getFeatureSource(typeName);
	FeatureCollection collection = featureSource.getFeatures();
	FeatureIterator iterator = collection.features();
	CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
	double s = -38;
	double w = -68;
	double n = -11;
	double e = -40;
	SimpleEntry<Double, Double> lower = new SimpleEntry<>(s, w);	
	SimpleEntry<Double, Double> upper = new SimpleEntry<>(n, e);
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<>(lower, upper);
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = CRSUtils.translateBBOX(sourceCorners,
		eu.essi_lab.model.resource.data.CRS.EPSG_4326(), eu.essi_lab.model.resource.data.CRS.EPSG_3857());
	SimpleEntry<Double, Double> lower2 = bbox.getKey();
	SimpleEntry<Double, Double> upper2 = bbox.getValue();

	ReferencedEnvelope mapBounds = new ReferencedEnvelope(lower2.getKey(), upper2.getKey(), lower2.getValue(), upper2.getValue(), crs);

	MapContent mapContent = new MapContent();
	mapContent.setTitle("Polygon Map");

	FeatureLayer layer = new FeatureLayer(collection, createDefaultStyle());
	mapContent.addLayer(layer);

	int imageWidth = 800;
	int imageHeight = 400;

	BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
	Graphics2D graphics = image.createGraphics();
	StreamingRenderer renderer = new StreamingRenderer();
	renderer.setMapContent(mapContent);
	renderer.paint(graphics, new Rectangle(imageWidth, imageHeight), mapBounds);

	File outputFile = new File("/tmp/output.png");
	try {
	    ImageIO.write(image, "png", outputFile);
	} catch (IOException ee) {
	    ee.printStackTrace();
	}

	// try {
	// while (iterator.hasNext()) {
	// Feature feature = iterator.next();
	// GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
	// FeatureId id = feature.getIdentifier();
	// System.out.println(id.getID());
	// Collection<? extends Property> v = feature.getValue();
	// Property property = feature.getProperty("VALUE");
	// Double d = (Double) property.getValue();
	// Long l = d.longValue();
	// org.locationtech.jts.geom.MultiPolygon mp = (org.locationtech.jts.geom.MultiPolygon)
	// sourceGeometry.getValue();
	// Coordinate[] coordinates = mp.getCoordinates();
	// int i = 5;
	//
	//
	//
	//
	// List<SimpleEntry<BigDecimal, BigDecimal>> coords = new ArrayList<>();
	// for (int j = 0; j < coordinates.length; j++) {
	// double x = coordinates[j].getX();
	// double y = coordinates[j].getY();
	// SimpleEntry<BigDecimal, BigDecimal> coord = new SimpleEntry<BigDecimal, BigDecimal>(new BigDecimal(x),
	// new BigDecimal(y));
	// coords.add(coord);
	// }
	//
	// StationRecord station = new StationRecord();
	// station.setDataIdentifier("TEST" + i);
	// station.setSourceIdentifier("TEST");
	//
	// Polygon4326 polygon = new Polygon4326(coords);
	// station.setPolygon(polygon);
	// // connector.writeStation(station);
	// break;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// iterator.close();
	// Thread.sleep(2000);
	// connector.close();
	// }

    }
    
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;
    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    private static GeomType geometryType = GeomType.POLYGON;

    private enum GeomType {
        POINT,
        LINE,
        POLYGON
    };
    /** Create a default Style for feature display */
    private static Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }
    private static String geometryAttributeName = "the_geom";

    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing a Symbolizer tailored to the
     * geometry type of the features that we are displaying.
     */
    private static Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

        switch (geometryType) {
            case POLYGON:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
                symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;

            case LINE:
                symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
                break;

            case POINT:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

                Mark mark = sf.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(POINT_SIZE));

                symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

}
