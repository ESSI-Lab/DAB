package eu.essi_lab.accessor.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;

public class ShapeFileReaderTest {

    @Test
    public void test() throws Exception {
//	String shapeZip = "/tmp/s3-test16149361482404773207/Limiti_Amministativi_AdBD_2018.zip";
	String shapeZip = "/tmp/s3-test16149361482404773207/UnitOfManagement_IT_20181025.zip";
	File shapeZipFile = new File(shapeZip);

	// Unzip the Shapefile to a temporary directory
	Path tempDir = Files.createTempDirectory("shapefile");
	File zipFileDir = Files.createTempDirectory("zip-shape").toFile();

	unzipShapefile(shapeZipFile, tempDir);
	File tempDirFile = tempDir.toFile();
	String[] list = tempDirFile.list(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		if (name.endsWith(".shp")) {
		    return true;
		}
		return false;
	    }
	});
	// Read the Shapefile
	File shpFile = new File(tempDir.toFile(), list[0]); // Adjust the name if necessary
	ShapeFileMetadata metadata = new ShapeFileMetadata(shpFile);
	List<FeatureMetadata> features = metadata.getFeatures();
	for (FeatureMetadata feature : features) {
	    System.out.println(feature.getId());
	    HashMap<String, String> attrs = feature.getAttributes();
	    Set<String> names = attrs.keySet();
	    for (String name : names) {
		System.out.println("Attribute "+name+": "+attrs.get(name));
	    }
	    System.out.println("BBOX: "+feature.getEast()+" "+feature.getWest());
	    System.out.println(feature.marshal());
	}
    }

    public static void unzipShapefile(File zipFilePath, Path outputDir) throws Exception {
	try (InputStream fis = new FileInputStream(zipFilePath); ZipInputStream zis = new ZipInputStream(fis)) {
	    ZipEntry entry;
	    while ((entry = zis.getNextEntry()) != null) {
		Path outputFile = outputDir.resolve(entry.getName());
		Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
	    }
	}
    }



}