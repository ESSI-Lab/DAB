package eu.essi_lab.downloader.s3;

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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.style.Fill;
import org.geotools.api.style.LineSymbolizer;
import org.geotools.api.style.PolygonSymbolizer;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Style;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.StyleBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.accessor.s3.S3ShapeFileClient;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class S3ShapeDownloader extends DataDownloader {

    FilterFactory ff = org.geotools.factory.CommonFactoryFinder.getFilterFactory(null);

    @Override
    public boolean canConnect() {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);

    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals("HTTP-SHAPE"));

    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    List<DataDescriptor> ret = new ArrayList<>();

	    Feature feature = FeatureManager.getInstance().getFeature(online.getLinkage(), online.getName());

	    FeatureMetadata metadata = feature.getFeatureMetadata();
	    BigDecimal south = metadata.getSouth();
	    BigDecimal east = metadata.getEast();
	    BigDecimal west = metadata.getWest();
	    BigDecimal north = metadata.getNorth();

	    DataDescriptor descriptor1 = new DataDescriptor();
	    descriptor1.setDataType(DataType.VECTOR);
	    descriptor1.setDataFormat(DataFormat.WKT());

	    descriptor1.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	    descriptor1.setCRS(CRS.EPSG_4326());
	    ret.add(descriptor1);

	    DataDescriptor descriptor = new DataDescriptor();
	    descriptor.setDataType(DataType.GRID);
	    descriptor.setDataFormat(DataFormat.IMAGE_PNG());

	    descriptor.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	    descriptor.setCRS(CRS.EPSG_4326());
	    ret.add(descriptor);

	    DataDescriptor descriptor2 = new DataDescriptor();
	    descriptor2.setDataType(DataType.GRID);
	    descriptor2.setDataFormat(DataFormat.IMAGE_PNG());

	    SimpleEntry<Double, Double> lower = new SimpleEntry<>(south.doubleValue(), west.doubleValue());
	    SimpleEntry<Double, Double> upper = new SimpleEntry<>(north.doubleValue(), east.doubleValue());
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<>(lower, upper);
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = CRSUtils.translateBBOX(sourceCorners,
		    CRS.EPSG_4326(), CRS.EPSG_3857());
	    SimpleEntry<Double, Double> lower2 = bbox.getKey();
	    SimpleEntry<Double, Double> upper2 = bbox.getValue();
	    Double minx = lower2.getKey();
	    Double miny = lower2.getValue();
	    Double maxx = upper2.getKey();
	    Double maxy = upper2.getValue();

	    descriptor2.setEPSG3857SpatialDimensions(minx, miny, maxx, maxy);
	    descriptor2.setCRS(CRS.EPSG_3857());
	    ret.add(descriptor2);

	    return ret;
	} catch (

	Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	try {
	    Feature feature = FeatureManager.getInstance().getFeature(online.getLinkage(), online.getName());

	    DataFormat format = targetDescriptor.getDataFormat();
	    if (format != null && format.equals(DataFormat.WKT())) {

		SimpleFeatureSource featureSource = feature.getFeatureSource();
		Filter filter = ff.id(Collections.singleton(ff.featureId(online.getName())));
		Query query = new Query(featureSource.getSchema().getTypeName(), filter);
		SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);
		WKTWriter wktWriter = new WKTWriter();
		try (SimpleFeatureIterator iterator = featureCollection.features()) {
		    while (iterator.hasNext()) {
			SimpleFeature f = iterator.next();
			Object geomObj = f.getDefaultGeometry();
			if (geomObj instanceof Geometry) {
			    Geometry geometry = (Geometry) geomObj;
			    String wkt = wktWriter.write(geometry);
			    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wkt");
			    FileOutputStream fos = new FileOutputStream(tmpFile);
			    fos.write(wkt.getBytes(StandardCharsets.UTF_8));
			    fos.close();
			    return tmpFile;
			}
		    }
		}
	    }

	    FeatureMetadata metadata = feature.getFeatureMetadata();
	    BigDecimal south = metadata.getSouth();
	    BigDecimal east = metadata.getEast();
	    BigDecimal west = metadata.getWest();
	    BigDecimal north = metadata.getNorth();

	    List<DataDimension> spatialDimensions = targetDescriptor.getSpatialDimensions();
	    DataDimension firstDimension = spatialDimensions.get(0);
	    DataDimension secondDimension = spatialDimensions.get(1);
	    if (firstDimension != null && secondDimension != null) {
		Number min1 = firstDimension.getContinueDimension().getLower();
		Number max1 = firstDimension.getContinueDimension().getUpper();
		Number min2 = secondDimension.getContinueDimension().getLower();
		Number max2 = secondDimension.getContinueDimension().getUpper();
		if (min1 != null && min2 != null && max1 != null && max2 != null) {

		    CRS crs = targetDescriptor.getCRS();
		    if (crs != null) {

			SimpleFeatureSource featureSource = feature.getFeatureSource();

			Filter filter = ff.id(Collections.singleton(ff.featureId(online.getName())));

			// Define the target CRS (e.g., EPSG:3857)
			CoordinateReferenceSystem targetCRS = crs.getDecodedCRS();

			// Reproject the features to the target CRS
			org.geotools.referencing.CRS.findMathTransform(featureSource.getSchema().getCoordinateReferenceSystem(), targetCRS,
				true);

			// Define a bounding box
			ReferencedEnvelope boundingBox = new ReferencedEnvelope(min1.doubleValue(), max1.doubleValue(), min2.doubleValue(),
				max2.doubleValue(), targetCRS);

			Query query = new Query(featureSource.getSchema().getTypeName(), filter);
			SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);

			// Create a style for rendering
			MapContent map = new MapContent();
			// Style style = SLD.createSimpleStyle(featureSource.getSchema());
			Style style = createCustomLineStyle(java.awt.Color.BLUE, 4);
			FeatureLayer layer = new FeatureLayer(featureCollection, style);
			map.addLayer(layer);

			// Render the map to an image
			Long size1 = firstDimension.getContinueDimension().getSize();
			Long size2 = secondDimension.getContinueDimension().getSize();
			BufferedImage image = renderToImage(map, boundingBox, size1.intValue(), size2.intValue());

			// Save the image to a file
			File output = File.createTempFile(getClass().getSimpleName(), ".png");
			javax.imageio.ImageIO.write(image, "png", output);
			System.out.println("Image created: " + output.getAbsolutePath());
			return output;

		    }
		}
	    }
	    return null;

	} catch (

	Exception e) {
	    e.printStackTrace();
	}
	return null;

    }

    private static Style createCustomLineStyle(java.awt.Color color, float lineWidth) {
	StyleBuilder builder = new StyleBuilder();

	// Create stroke with custom color and width
	Stroke stroke = builder.createStroke(color, lineWidth);
	Fill fill = builder.createFill(color, 0.3);
	// Create a LineSymbolizer using the stroke
	PolygonSymbolizer lineSymbolizer = builder.createPolygonSymbolizer(stroke,fill);

	// Return the style based on the symbolizer
	return builder.createStyle(lineSymbolizer);
    }

    public static BufferedImage renderToImage(MapContent map, ReferencedEnvelope envelope, int width, int height) {
	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = image.createGraphics();

	GTRenderer renderer = new StreamingRenderer();
	renderer.setMapContent(map);

	// Setup rendering area (bounding box) and image dimensions
	renderer.paint(g2d, new java.awt.Rectangle(width, height), envelope);

	g2d.dispose();
	return image;
    }

}
