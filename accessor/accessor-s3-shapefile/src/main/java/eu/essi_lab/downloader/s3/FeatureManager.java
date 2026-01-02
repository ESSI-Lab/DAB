package eu.essi_lab.downloader.s3;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;

import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.accessor.s3.S3ShapeFileClient;
import eu.essi_lab.accessor.s3.ShapeFileMetadata;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

public class FeatureManager {
    private static FeatureManager instance = null;

    private FeatureManager() {

    }

    public static FeatureManager getInstance() {
	if (instance == null) {
	    instance = new FeatureManager();
	}
	return instance;
    }

    private HashMap<String, Feature> features = new HashMap<String, Feature>();

    public static final String COMPLETED = "reallyCompleted";

    public static final Object SYNCH = new Object();

    private HashMap<String, S3ShapeFileClient> clients = new HashMap<String, S3ShapeFileClient>();

    public Feature getFeature(String linkage, String name) throws Exception {

	String id = linkage + ":" + name;

	Feature ret = features.get(id);

	if (ret != null) {
	    return ret;
	}

	String unzipFolderName = "S3-UNZIP-" + linkage.substring(linkage.lastIndexOf("/") + 1);

	File persistentUnzipFolder = getPersistentTempFolder(unzipFolderName);

	synchronized (SYNCH) {

	    ret = features.get(id);

	    if (ret != null) {
		return ret;
	    }

	    ret = new Feature();

	    S3ShapeFileClient client = clients.get(linkage);
	    if (client == null) {
		client = new S3ShapeFileClient(linkage);
		clients.put(linkage, client);
	    }

	    DataDescriptor descriptor = new DataDescriptor();
	    descriptor.setDataType(DataType.GRID);
	    descriptor.setDataFormat(DataFormat.IMAGE_PNG());

	    String zipName = linkage.substring(linkage.lastIndexOf("/") + 1);
	    String folderName = "S3-ZIP-" + zipName;

	    File persistentTempFolder = getPersistentTempFolder(folderName);

	    if (isEmpty(persistentTempFolder)) {

		client.downloadTo(persistentTempFolder);

		ByteArrayInputStream stream = new ByteArrayInputStream(COMPLETED.getBytes());
		File completedFile = new File(persistentTempFolder, COMPLETED);

		IOUtils.copy(stream, new FileOutputStream(completedFile));
	    }

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

		ByteArrayInputStream stream = new ByteArrayInputStream(COMPLETED.getBytes());
		File completedFile = new File(persistentUnzipFolder, COMPLETED);

		IOUtils.copy(stream, new FileOutputStream(completedFile));
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

	    File shapeFile = new File(persistentUnzipFolder, shapes[0]);
	    FileDataStore store = FileDataStoreFinder.getDataStore(shapeFile);
	    SimpleFeatureSource featureSource = store.getFeatureSource();
	    ret.setFeatureSource(featureSource);

	    ShapeFileMetadata metadata = new ShapeFileMetadata(featureSource);
	    List<FeatureMetadata> features = metadata.getFeatures();

	    for (FeatureMetadata feature : features) {
		if (!feature.getId().equals(name)) {
		    continue;
		}

		GSLoggerFactory.getLogger(getClass()).info("Found {}", name);
		ret.setFeatureMetadata(feature);

		this.features.put(id, ret);
		return ret;

	    }
	    return null;

	}

    }

    private boolean isEmpty(File directory) {
	// Get the list of files in the directory
	String[] files = directory.list();

	// Check if the directory is not empty
	if (files != null && files.length > 0 && Arrays.asList(files).stream().anyMatch(f -> f.contains(COMPLETED))) {

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

    // private Long getUpdatedResolution(Number resolution, Number i) {
    // if (resolution instanceof Long && i instanceof Long) {
    // return (Long) resolution * (Long) i;
    // }
    // return Math.round(resolution.doubleValue() * i.doubleValue());
    // }

    public synchronized static void unzipShapefile(File zipFilePath, File outputDir) throws Exception {
	try (InputStream fis = new FileInputStream(zipFilePath); ZipInputStream zis = new ZipInputStream(fis)) {
	    ZipEntry entry;
	    while ((entry = zis.getNextEntry()) != null) {
		Path outputFile = outputDir.toPath().resolve(entry.getName());
		Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
	    }
	}
    }
}
