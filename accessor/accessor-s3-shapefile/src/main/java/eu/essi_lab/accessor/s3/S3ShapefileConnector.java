package eu.essi_lab.accessor.s3;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class S3ShapefileConnector extends HarvestedQueryConnector<S3ShapefileConnectorSetting> {

    public static final String TYPE = "S3ShapefileConnector";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("s3");
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

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	String sourceUrl = getSourceURL();

	S3ShapeFileClient client = new S3ShapeFileClient(sourceUrl);
	File s3Dir = null;
	try {
	    s3Dir = Files.createTempDirectory("s3-shape").toFile();
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Error creating dir");
	}
	client.downloadTo(s3Dir);

	File[] zipFiles = s3Dir.listFiles();
	for (File zipFile : zipFiles) {
	    try {
		File s3unzippedDir = Files.createTempDirectory("s3-unzipped").toFile();
		unzipShapefile(zipFile, s3unzippedDir.toPath());
		String[] shpFiles = s3unzippedDir.list(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			if (name.endsWith(".shp")) {
			    return true;
			}
			return false;
		    }
		});
		File shapeFile = new File(s3unzippedDir, shpFiles[0]);

		String url = sourceUrl;
		if (!url.endsWith("/")) {
		    url = url + "/";
		}
		url = url + zipFile.getName();

		ShapeFileMetadata metadata = new ShapeFileMetadata(shapeFile);
		List<FeatureMetadata> features = metadata.getFeatures();
		for (FeatureMetadata feature : features) {
		    feature.setUrl(url);
		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.HIS_CENTRAL_SHAPEFILE);
		    metadataRecord.setMetadata(feature.marshal());
		    ret.addRecord(metadataRecord);
		}

		String[] unzippedFiles = s3unzippedDir.list();
		for (String file : unzippedFiles) {
		    File tmp = new File(s3unzippedDir, file);
		    tmp.delete();
		}
		s3unzippedDir.delete();
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Error reading shape file");
	    }

	    zipFile.delete();
	}
	s3Dir.delete();

	ret.setResumptionToken(null);

	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.HIS_CENTRAL_SHAPEFILE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected S3ShapefileConnectorSetting initSetting() {
	return new S3ShapefileConnectorSetting();
    }

}
