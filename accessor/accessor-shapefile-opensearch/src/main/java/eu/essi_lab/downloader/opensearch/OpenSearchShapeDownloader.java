package eu.essi_lab.downloader.opensearch;

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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.style.Fill;
import org.geotools.api.style.PolygonSymbolizer;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Style;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.StyleBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.opensearch.shape.OpenSearchShapeMapper;
import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

/**
 * Downloads WKT or rendered PNG for shapes stored in OpenSearch.
 */
public class OpenSearchShapeDownloader extends DataDownloader {

    @Override
    public boolean canConnect() {
	return true;
    }

    @Override
    public boolean canDownload() {
	return online.getProtocol() != null && online.getProtocol().equals(OpenSearchShapeMapper.ONLINE_PROTOCOL);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    List<DataDescriptor> ret = new ArrayList<>();
	    OpenSearchFeature feature = loadFeature();
	    if (feature == null) {
		return null;
	    }

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
	    descriptor2.setEPSG3857SpatialDimensions(bbox.getKey().getKey(), bbox.getKey().getValue(), bbox.getValue().getKey(),
		    bbox.getValue().getValue());
	    descriptor2.setCRS(CRS.EPSG_3857());
	    ret.add(descriptor2);

	    return ret;
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	try {
	    OpenSearchFeature feature = loadFeature();
	    if (feature == null) {
		return null;
	    }

	    DataFormat format = targetDescriptor.getDataFormat();
	    if (format != null && format.equals(DataFormat.WKT())) {
		WKTWriter wktWriter = new WKTWriter();
		String wkt = wktWriter.write(feature.getGeometry());
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wkt");
		try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
		    fos.write(wkt.getBytes(StandardCharsets.UTF_8));
		}
		return tmpFile;
	    }

	    FeatureMetadata metadata = feature.getFeatureMetadata();
	    List<DataDimension> spatialDimensions = targetDescriptor.getSpatialDimensions();
	    if (spatialDimensions.size() < 2) {
		return null;
	    }
	    DataDimension firstDimension = spatialDimensions.get(0);
	    DataDimension secondDimension = spatialDimensions.get(1);
	    Number min1 = firstDimension.getContinueDimension().getLower();
	    Number max1 = firstDimension.getContinueDimension().getUpper();
	    Number min2 = secondDimension.getContinueDimension().getLower();
	    Number max2 = secondDimension.getContinueDimension().getUpper();
	    if (min1 == null || min2 == null || max1 == null || max2 == null) {
		return null;
	    }

	    CRS crs = targetDescriptor.getCRS();
	    if (crs == null) {
		return null;
	    }

	    CoordinateReferenceSystem targetCRS = crs.getDecodedCRS();
	    SimpleFeatureCollection collection = toFeatureCollection(feature.getGeometry(), online.getName());
	    ReferencedEnvelope boundingBox = new ReferencedEnvelope(min1.doubleValue(), max1.doubleValue(), min2.doubleValue(),
		    max2.doubleValue(), targetCRS);

	    MapContent map = new MapContent();
	    try {
		Style style = createCustomLineStyle(java.awt.Color.BLUE, 4);
		map.addLayer(new FeatureLayer(collection, style));

		Long size1 = firstDimension.getContinueDimension().getSize();
		Long size2 = secondDimension.getContinueDimension().getSize();
		BufferedImage image = renderToImage(map, boundingBox, size1.intValue(), size2.intValue());

		File output = File.createTempFile(getClass().getSimpleName(), ".png");
		javax.imageio.ImageIO.write(image, "png", output);
		return output;
	    } finally {
		map.dispose();
	    }

	} catch (Exception e) {
	    return null;
	}
    }

    private OpenSearchFeature loadFeature() throws Exception {
	return OpenSearchFeatureManager.getInstance().getFeature(online.getLinkage(), online.getName());
    }

    private static SimpleFeatureCollection toFeatureCollection(Geometry geometry, String featureId) {

	SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
	typeBuilder.setName("opensearchShape");
	typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
	typeBuilder.add("geometry", geometry.getClass());
	SimpleFeatureType featureType = typeBuilder.buildFeatureType();

	SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
	featureBuilder.add(geometry);
	SimpleFeature simpleFeature = featureBuilder.buildFeature(featureId);

	return new ListFeatureCollection(featureType, List.of(simpleFeature));
    }

    private static Style createCustomLineStyle(java.awt.Color color, float lineWidth) {
	StyleBuilder builder = new StyleBuilder();
	Stroke stroke = builder.createStroke(color, lineWidth);
	Fill fill = builder.createFill(color, 0.3f);
	PolygonSymbolizer polygonSymbolizer = builder.createPolygonSymbolizer(stroke, fill);
	return builder.createStyle(polygonSymbolizer);
    }

    public static BufferedImage renderToImage(MapContent map, ReferencedEnvelope envelope, int width, int height) {
	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = image.createGraphics();
	GTRenderer renderer = new StreamingRenderer();
	renderer.setMapContent(map);
	renderer.paint(g2d, new java.awt.Rectangle(width, height), envelope);
	g2d.dispose();
	return image;
    }
}
