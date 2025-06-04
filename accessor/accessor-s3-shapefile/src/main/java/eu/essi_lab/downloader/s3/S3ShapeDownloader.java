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
import java.io.FilenameFilter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
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
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.style.LineSymbolizer;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Style;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.StyleBuilder;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.accessor.s3.S3ShapeFileClient;
import eu.essi_lab.accessor.s3.ShapeFileMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
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

	private S3ShapeFileClient client;
	private String name;

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
		String linkage = online.getLinkage();
		this.client = new S3ShapeFileClient(linkage);
		
		this.name = online.getName();
	}

	@Override
	public boolean canDownload() {

		return (online.getProtocol() != null && online.getProtocol().equals("HTTP-SHAPE"));

	}

	public static void unzipShapefile(File zipFilePath, File outputDir) throws Exception {
		try (InputStream fis = new FileInputStream(zipFilePath); ZipInputStream zis = new ZipInputStream(fis)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path outputFile = outputDir.toPath().resolve(entry.getName());
				Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	@Override
	public boolean canSubset(String dimensionName) {
		return true;

	}

	@Override
	public List<DataDescriptor> getRemoteDescriptors() throws GSException {
		try {
			List<DataDescriptor> ret = new ArrayList<>();

			SimpleEntry<File, FeatureMetadata> feature = getFeature(online);

			FeatureMetadata metadata = feature.getValue();
			BigDecimal south = metadata.getSouth();
			BigDecimal east = metadata.getEast();
			BigDecimal west = metadata.getWest();
			BigDecimal north = metadata.getNorth();

			DataDescriptor descriptor = new DataDescriptor();
			descriptor.setDataType(DataType.GRID);
			descriptor.setDataFormat(DataFormat.IMAGE_PNG());

			descriptor.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(),
					west.doubleValue());
			descriptor.setCRS(CRS.EPSG_4326());
			ret.add(descriptor);

			DataDescriptor descriptor2 = new DataDescriptor();
			descriptor2.setDataType(DataType.GRID);
			descriptor2.setDataFormat(DataFormat.IMAGE_PNG());

			SimpleEntry<Double, Double> lower = new SimpleEntry<>(south.doubleValue(), west.doubleValue());
			SimpleEntry<Double, Double> upper = new SimpleEntry<>(north.doubleValue(), east.doubleValue());
			SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<>(
					lower, upper);
			SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = CRSUtils
					.translateBBOX(sourceCorners, CRS.EPSG_4326(), CRS.EPSG_3857());
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

	private boolean isEmpty(File directory) {
		// Get the list of files in the directory
		String[] files = directory.list();

		// Check if the directory is empty
		if (files != null && files.length > 0) {
			return false;
		} else {
			return true;
		}
	}

	private File getPersistentTempFolder(String newDirName) {
		String tempDirPath = System.getProperty("java.io.tmpdir");

		// Create the full path for the new directory
		File newDir = new File(tempDirPath, newDirName);

		// Check if the directory doesn't exist
		if (!newDir.exists()) {
			// Attempt to create the directory
			if (newDir.mkdirs()) {
				System.out.println("Directory created successfully in the temp folder: " + newDir.getAbsolutePath());
			} else {
				System.out.println("Failed to create the directory in the temp folder.");
			}
		} else {
			System.out.println("Directory already exists in the temp folder.");
		}
		return newDir;
	}

	private Long getUpdatedResolution(Number resolution, Number i) {
		if (resolution instanceof Long && i instanceof Long) {
			return (Long) resolution * (Long) i;
		}
		return Math.round(resolution.doubleValue() * i.doubleValue());
	}

	@Override
	public File download(DataDescriptor targetDescriptor) throws GSException {
		try {

			SimpleEntry<File, FeatureMetadata> feature = getFeature(online);
			File file = feature.getKey();
			FeatureMetadata metadata = feature.getValue();
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

						FileDataStore store = FileDataStoreFinder.getDataStore(file);
						SimpleFeatureSource featureSource = store.getFeatureSource();

						FilterFactory ff = org.geotools.factory.CommonFactoryFinder.getFilterFactory(null);
						Filter filter = ff.id(Collections.singleton(ff.featureId(online.getName())));

						// Define the target CRS (e.g., EPSG:3857)
						CoordinateReferenceSystem targetCRS = crs.getDecodedCRS();

						// Reproject the features to the target CRS
						org.geotools.referencing.CRS.findMathTransform(
								featureSource.getSchema().getCoordinateReferenceSystem(), targetCRS, true);

						// Define a bounding box
						ReferencedEnvelope boundingBox = new ReferencedEnvelope(min1.doubleValue(), max1.doubleValue(),
								min2.doubleValue(), max2.doubleValue(), targetCRS);

						Query query = new Query(featureSource.getSchema().getTypeName(), filter);
						SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);

						// Create a style for rendering
						MapContent map = new MapContent();
//						Style style = SLD.createSimpleStyle(featureSource.getSchema());
						Style style = createCustomLineStyle(java.awt.Color.YELLOW, 3);
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

		        // Create a LineSymbolizer using the stroke
		        LineSymbolizer lineSymbolizer = builder.createLineSymbolizer(stroke);

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

	private SimpleEntry<File, FeatureMetadata> getFeature(Online online) throws Exception {
		DataDescriptor descriptor = new DataDescriptor();
		descriptor.setDataType(DataType.GRID);
		descriptor.setDataFormat(DataFormat.IMAGE_PNG());

		String zipName = online.getLinkage().substring(online.getLinkage().lastIndexOf("/") + 1);
		String folderName = "S3-ZIP-" + zipName;

		File persistentTempFolder = getPersistentTempFolder(folderName);

		if (isEmpty(persistentTempFolder)) {

			client.downloadTo(persistentTempFolder);

		}

		String unzipFolderName = "S3-UNZIP-" + online.getLinkage().substring(online.getLinkage().lastIndexOf("/") + 1);

		File persistentUnzipFolder = getPersistentTempFolder(unzipFolderName);

		if (isEmpty(persistentUnzipFolder)) {

			String[] list = persistentTempFolder.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(zipName)) {
						return true;
					}
					return false;
				}
			});

			unzipShapefile(new File(persistentTempFolder, list[0]), persistentUnzipFolder);

		}

		String[] shapes = persistentUnzipFolder.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".shp")) {
					return true;
				}
				return false;
			}
		});

		String name = online.getName();

		File file = new File(persistentUnzipFolder, shapes[0]);
		ShapeFileMetadata metadata = new ShapeFileMetadata(file);
		List<FeatureMetadata> features = metadata.getFeatures();

		for (FeatureMetadata feature : features) {
			if (!feature.getId().equals(name)) {
				continue;
			}

			GSLoggerFactory.getLogger(getClass()).info("Found {}", name);
			SimpleEntry<File, FeatureMetadata> ret = new SimpleEntry<File, FeatureMetadata>(file, feature);
			return ret;

		}
		return null;
	}

}
