package eu.essi_lab.gssrv.conf.task.turtle;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import org.apache.jena.shacl.ValidationReport;
import org.quartz.JobExecutionContext;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

/**
 * @author boldrini
 */
public class TurtleTask extends AbstractCustomTask {

    // Two usages are expected
    // 1) mapping and upload of sources
    // access: xxx
    // secret: xxx
    // hostname: dataset.geodab.eu
    // source: source1-id
    // source: source2-id (optional, for multiple sources, to be used in custom
    // tasks)
    // 2) aggregation of already processed sources and upload of result
    // access: xxx
    // secret: xxx
    // hostname: dataset.geodab.eu
    // source: source1-id
    // source: source2-id
    // aggregate: fair-ease
    public enum TurtleTaskKey {
	TEST_KEY("test:"), //
	AGGREGATE_KEY("aggregate:"), //
	SOURCE_KEY("source:"), //
	ACCESS_KEY("access:"), //
	SECRET_KEY("secret:"), //
	HOSTNAME_KEY("hostname:"), //

	;

	private String id;

	TurtleTaskKey(String id) {
	    this.id = id;
	}

	@Override
	public String toString() {
	    return id;
	}

	public static String getExpectedKeys() {
	    String ret = "";
	    for (TurtleTaskKey key : values()) {
		ret += key.toString() + " ";
	    }
	    return ret;
	}

	public static SimpleEntry<TurtleTaskKey, String> decodeLine(String line) {
	    for (TurtleTaskKey key : values()) {
		if (line.toLowerCase().startsWith(key.toString().toLowerCase())) {
		    SimpleEntry<TurtleTaskKey, String> ret = new SimpleEntry<>(key, line.substring(key.toString().length()).trim());
		    return ret;
		}
	    }
	    return null;
	}
    }

    String hostname = "dataset.geodab.eu";
    String path = "dataset";

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("Turtle task STARTED");
	log(status, "Turtle task STARTED");

	// SETTINGS RETRIEVAL
	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String settings = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		settings = options;
	    }
	}
	if (settings == null) {
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for turtle task");
	    return;
	}
	String[] lines = settings.split("\n");
	String secret = null;
	String access = null;
	boolean aggregateMode = false;
	String aggregatedTarget = null;
	boolean test = false;
	List<String> sources = new ArrayList<>();
	for (String line : lines) {
	    SimpleEntry<TurtleTaskKey, String> decoded = TurtleTaskKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass())
			.error("unexpected settings for turtle task. Expected: " + TurtleTaskKey.getExpectedKeys());
		return;
	    }
	    switch (decoded.getKey()) {
	    case TEST_KEY:
		test = (decoded.getValue() != null && decoded.getValue().toLowerCase().contains("true")) ? true : false;
		break;
	    case SECRET_KEY:
		secret = decoded.getValue();
		break;
	    case SOURCE_KEY:
		sources.add(decoded.getValue());
		break;
	    case ACCESS_KEY:
		access = decoded.getValue();
		break;
	    case HOSTNAME_KEY:
		hostname = decoded.getValue();
		break;
	    case AGGREGATE_KEY:
		aggregateMode = true;
		aggregatedTarget = decoded.getValue();
		break;
	    default:
		break;
	    }
	}

	S3TransferWrapper wrapper = null;
	if (access != null && secret != null) {
	    wrapper = new S3TransferWrapper();
	    wrapper.setAccessKey(access);
	    wrapper.setSecretKey(secret);
	    wrapper.setACLPublicRead(true);
	}

	if (aggregateMode) {

	    // download the sources files, aggregate and upload the result

	    List<File> sourceFiles = new ArrayList<>();
	    List<File> validSourceFiles = new ArrayList<>();
	    for (String sourceId : sources) {
		String url = "https://" + hostname + "/" + path + "/" + sourceId + "/" + sourceId + ".ttl";
		File file = downloadFile(url, sourceId + ".ttl");
		if (file == null) {
		    return;
		}
		sourceFiles.add(file);

		String validUrl = "https://" + hostname + "/" + path + "/" + sourceId + "/" + sourceId + "-valid.ttl";
		File validFile = downloadFile(validUrl, sourceId + "-valid.ttl");
		if (validFile == null) {
		    return;
		}
		validSourceFiles.add(validFile);
	    }
	    TurtleAggregator aggregator = new TurtleAggregator();
	    File finalFile = File.createTempFile(getClass().getSimpleName(), aggregatedTarget + ".ttl");
	    aggregator.aggregate(finalFile.getAbsolutePath(), sourceFiles);
	    if (wrapper != null) {
		wrapper.uploadFile(finalFile.getAbsolutePath(), hostname, path + "/view/" + aggregatedTarget + ".ttl");
	    }
	    finalFile.delete();

	    File validfinalFile = File.createTempFile(getClass().getSimpleName(), aggregatedTarget + "-valid.ttl");
	    aggregator.aggregate(validfinalFile.getAbsolutePath(), validSourceFiles);
	    if (wrapper != null) {
		wrapper.uploadFile(validfinalFile.getAbsolutePath(), hostname, path + "/view/" + aggregatedTarget + "-valid.ttl");
	    }
	    validfinalFile.delete();

	} else {

	    // for each source, retrieve the original files, generate the turtle files and
	    // the aggregated source file, then upload

	    for (String sourceId : sources) {

		int pageSize = 250;

		ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
		IDiscoveryExecutor executor = loader.iterator().next();

		DiscoveryMessage discoveryMessage = new DiscoveryMessage();
		discoveryMessage.setRequestId("turtle-task-" + sourceId + "-" + UUID.randomUUID());
		discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
		discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
		discoveryMessage.setExcludeResourceBinary(false);
		discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
		discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
		ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond(sourceId);
		discoveryMessage.setPermittedBond(bond);
		discoveryMessage.setUserBond(bond);
		discoveryMessage.setNormalizedBond(bond);

		int start = 9750;
		int file = 0;
		TurtleMapper mapper = new TurtleMapper();

		String tmpSourcedir = Files.createTempDirectory("turtle-task-" + sourceId).toFile().getAbsolutePath();
		GSLoggerFactory.getLogger(getClass()).info("Created turtle dir {}", tmpSourcedir);
		File sourceDir = new File(tmpSourcedir);

		List<File> turtles = new ArrayList<>();
		discoveryMessage.setSortOrder(SortOrder.ASCENDING);
		discoveryMessage.setSortProperty(ResourceProperty.PRIVATE_ID);
		SearchAfter searchAfter = null;
		main: while (true) {

		    // CHECKING CANCELED JOB

//		    if (ConfigurationWrapper.isJobCanceled(context)) {
//			GSLoggerFactory.getLogger(getClass()).info("Turtle task CANCELED");
//			log(status, "Turtle task CANCELED");
//			status.setPhase(JobPhase.CANCELED);
//			return;
//		    }

		    GSLoggerFactory.getLogger(getClass()).info("Turtle task {} at record {}", sourceId, start);
		    discoveryMessage.setPage(new Page(start, pageSize));
		    start = start + pageSize;

		    if (searchAfter != null) {
			discoveryMessage.setSearchAfter(searchAfter);
		    }
		    ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
		    if (resultSet.getSearchAfter().isPresent()) {
			searchAfter = resultSet.getSearchAfter().get();
		    }
		    List<GSResource> resources = resultSet.getResultsList();

		    int i = 0;
		    for (GSResource resource : resources) {
			i++;
			if (test && i > 3) {
			    break main;
			}
			String turtle = mapper.map(discoveryMessage, resource);
			String filename = null;
			if (resource.getOriginalId().isPresent()) {
			    filename = resource.getOriginalId().get();
			}
			if (filename == null) {
			    filename = resource.getPrivateId();
			}
			if (filename == null) {
			    filename = resource.getPublicId();
			}
			File temp = new File(sourceDir, filename + ".ttl");
			BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
			if (turtle != null) {
			    writer.write(turtle);
			} else {
			    GSLoggerFactory.getLogger(getClass()).error("Error in the turtle mapper");
			}
			writer.close();
			turtles.add(temp);
		    }

		    if (resources.isEmpty()) {
			break;
		    }
		}

		File sourceFile = new File(tmpSourcedir, sourceId + ".ttl");
		File validSourceFile = new File(tmpSourcedir, sourceId + "-valid.ttl");
		File reportSourceFile = new File(tmpSourcedir, sourceId + "-report.txt");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile));
			BufferedWriter validWriter = new BufferedWriter(new FileWriter(validSourceFile));
			BufferedWriter reportWriter = new BufferedWriter(new FileWriter(reportSourceFile));) {
		    long all = 0;
		    long good = 0;
		    List<File> toBeUploaded = new ArrayList<File>();
		    for (File turtle : turtles) {
			all++;
			boolean valid = false;
			String webTurtle = path + "/" + sourceId + "/" + turtle.getName();
			// Perform SHACL validation
			try {
			    ValidationReport report = TurtleValidator.validate(turtle);

			    // Check if the data conforms to the SHACL shapes
			    if (report.conforms()) {
				GSLoggerFactory.getLogger(getClass()).info("conforms: {}", turtle.getName());
				valid = true;
				reportWriter.write("File validated fine: https://s3.amazonaws.com/" + hostname + "/" + webTurtle);
				reportWriter.newLine();
				good++;
			    } else {
				GSLoggerFactory.getLogger(getClass()).info("does not conform: {}", turtle.getName());
				reportWriter.write("Validation error for file: https://s3.amazonaws.com/" + hostname + "/" + webTurtle);
				reportWriter.newLine();
				// Print the validation report (optional)
				report.getEntries().forEach(entry -> {
				    try {
					reportWriter.write(entry.message());
					reportWriter.newLine();
					reportWriter.write("" + entry.focusNode());
					reportWriter.newLine();
					reportWriter.write("" + entry.resultPath());
					reportWriter.newLine();
					reportWriter.write("" + entry.sourceConstraintComponent());
					reportWriter.newLine();
					reportWriter.write("-------------");
					reportWriter.newLine();
				    } catch (IOException e) {
					e.printStackTrace();
				    }
				});
			    }

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).info("does not conform: {}", turtle.getName());
			    reportWriter
				    .write("Preliminary validation error for file: https://s3.amazonaws.com/" + hostname + "/" + webTurtle);
			    reportWriter.newLine();
			    reportWriter.write(e.getMessage());
			    reportWriter.newLine();
			}

			// writing to files
			try (BufferedReader reader = new BufferedReader(new FileReader(turtle))) {
			    String line;
			    while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.newLine();
				if (valid) {
				    validWriter.write(line);
				    validWriter.newLine();
				}
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
			toBeUploaded.add(turtle);
			if (toBeUploaded.size() == 100) {
			    uploadFiles(wrapper, sourceId, toBeUploaded);
			}
		    }
		    uploadFiles(wrapper, sourceId, toBeUploaded);

		    reportWriter.newLine();
		    reportWriter.write("Total files: " + all);
		    reportWriter.newLine();
		    reportWriter.write("Valid files: " + good + " " + ((((double) good / (double) all)) * 100.0) + "%");
		    reportWriter.newLine();
		}
		GSLoggerFactory.getLogger(getClass()).info("Created turtle output {}", sourceFile.getAbsolutePath());
		// upload outputFile
		if (wrapper != null) {

		    wrapper.uploadFile(sourceFile.getAbsolutePath(), hostname, path + "/" + sourceId + "/" + sourceId + ".ttl");

		    wrapper.uploadFile(validSourceFile.getAbsolutePath(), hostname, path + "/" + sourceId + "/" + sourceId + "-valid.ttl");

		    wrapper.uploadFile(reportSourceFile.getAbsolutePath(), hostname,
			    path + "/" + sourceId + "/" + sourceId + "-report.txt");
		}
		sourceFile.delete();
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Number of sources: {}", sources.size());

	}
	GSLoggerFactory.getLogger(getClass()).info("Turtle task ENDED");
	log(status, "Turtle task ENDED");
    }

    private void uploadFiles(S3TransferWrapper wrapper, String sourceId, List<File> toBeUploaded) {
	if (wrapper != null) {
	    List<UploadFileRequest> requests = new ArrayList<UploadFileRequest>();
	    for (File file : toBeUploaded) {
		String webTurtle = path + "/" + sourceId + "/" + file.getName();
		UploadFileRequest uploadRequest = wrapper.getUploadRequest(file.getAbsolutePath(), hostname, webTurtle, "text/turtle");
		requests.add(uploadRequest);
	    }
	    wrapper.uploadFiles(requests);
	}
	GSLoggerFactory.getLogger(getClass()).info("removing temporary files");
	for (File file : toBeUploaded) {
	    file.delete();
	}
	toBeUploaded.clear();

    }

    private File downloadFile(String url, String filename) throws Exception {
	Downloader downloader = new Downloader();
	Optional<InputStream> response = downloader.downloadOptionalStream(url);
	if (response.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("missing file: {}", url);
	    return null;
	} else {
	    InputStream stream = response.get();
	    File sourceFile = File.createTempFile(getClass().getSimpleName(), filename);
	    FileOutputStream fos = new FileOutputStream(sourceFile);
	    IOUtils.copy(stream, fos);
	    stream.close();
	    fos.close();
	    return sourceFile;
	}
    }

    @Override
    public String getName() {

	return "Turtle task";
    }
}
