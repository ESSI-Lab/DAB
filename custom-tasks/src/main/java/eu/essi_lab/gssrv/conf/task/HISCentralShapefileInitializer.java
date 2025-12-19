package eu.essi_lab.gssrv.conf.task;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class HISCentralShapefileInitializer extends AbstractCustomTask {

    public HISCentralShapefileInitializer() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getName() {
	return "HIS-Central shapefile initializer task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("HIS-Central shapefile init STARTED");
	OpenSearchDatabase database = new OpenSearchDatabase();

	database.initialize(ConfigurationWrapper.getStorageInfo());
	OpenSearchFolder folder = new OpenSearchFolder(database, OpenSearchDatabase.SHAPE_FILES_FOLDER);
	Optional<S3TransferWrapper> optManager = ConfigurationWrapper.getS3TransferWrapper();
	if (optManager.isPresent()) {
	    S3TransferWrapper wrapper = optManager.get();
	    File s3Dir = null;
	    try {
		s3Dir = Files.createTempDirectory("s3-shape").toFile();
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Error creating dir");
	    }
	    ListObjectsV2Response objects = wrapper.listObjects("his-central");
	    List<S3Object> contents = objects.contents();
	    for (S3Object content : contents) {
		System.out.println(content.key());
	    }
	    wrapper.downloadDir(s3Dir, "his-central", "polygons");

	    String[] zipFiles = s3Dir.list(new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
		    if (name.endsWith(".zip")) {
			return true;
		    }
		    return false;
		}
	    });

	    for (String zipFile : zipFiles) {
		InputStream stream = new FileInputStream(new File(s3Dir,zipFile));
		boolean stored = folder.store("UOMIT20181025", FolderEntry.of(stream), EntryType.SHAPE_FILE);
		stream.close();
		GSLoggerFactory.getLogger(getClass()).info("Stored shape file {}: {}", zipFile, stored);

	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).info("No s3 connector configured");

	}

	GSLoggerFactory.getLogger(getClass()).info("HIS-Central shapefile init ENDED");
    }

}
