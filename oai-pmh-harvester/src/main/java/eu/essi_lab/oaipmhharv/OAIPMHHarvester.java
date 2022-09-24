package eu.essi_lab.oaipmhharv;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.adk.harvest.HarvestedAccessor;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.SystemPropertyChecker;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public class OAIPMHHarvester {

    private final ArrayList<String> supportedArgs = new ArrayList<>();
    private String help;
    private LinkedHashMap<String, String> argsMap;
    private File reportFolder;
    private File dataFolder;
    private File currentFolderFile;
    private int currentSize;
    private int zipCount;
    private List<String> errors;
    private String startTime;
    private Chronometer chronometer;
    private int harvestedCount;
    private NumberFormat format;
    private AmazonS3 client;
    private ExecutorService pool;
    private int deletedRecordsCount;
    private int errorsCount;
    private static final String DELETE_TIMESTAMP = "deleteTimeStamp";
    private static final String TIMESTAMP = "timeStamp";
    private static final String UNABLE_TO_WRITE_RECORD = "Unable to write record file: ";
    private static final String UTF8 = "UTF-8";

    public OAIPMHHarvester() {

	supportedArgs.add("-help");

	supportedArgs.add("-programFolder"); // default: user-folder/oph/

	supportedArgs.add("-awsSecretKey");
	supportedArgs.add("-awsClientId");

	supportedArgs.add("-outputFormat"); // default ISO (values:ISO,DC)

	supportedArgs.add("-deleteTimeStamp"); // default: no
	supportedArgs.add("-timeStamp"); // default: none

	supportedArgs.add("-maxFiles"); // default: no limit

	supportedArgs.add("-zipContentSize"); // default: 500MB

	supportedArgs.add("-uploadZipFiles"); // default: yes
	supportedArgs.add("-removeZipFiles"); // default: yes

	supportedArgs.add("-resumptionToken"); // default: none
	supportedArgs.add("-currentFolder"); // default: none
	supportedArgs.add("-harvestedCount"); // default: 0
	supportedArgs.add("-zipCount"); // default: 0

	supportedArgs.add("-stopOnDeleted"); // default: false

	supportedArgs.add("-clearOnExit"); // default: false

	supportedArgs.add("-maxErrors"); // default: 100

	supportedArgs.add("-endpoint"); // hidden arg. Default: http://kma.geodab.eu/dab/services/oaipmh?

	help = "- OAI-PMH Harvester\n";
	help += "- Version: 1.0.0\n\n";

	help += "\n- Available settings -\n\n";

	help += "All the following arguments are optionals.\n";
	help += "To set the value use '=' after the argument name (e.g: java -cp \"oph.jar;lib/*\" eu.essi_lab.oaipmhharv.OAIPMHHarvester -programFolder=C:/oph/report)\n";
	help += "Arguments must be separated by a blank space.\n\n";
	help += "-programFolder: default: 'user-folder/oph/'\n";
	help += "-outputFormat: metadata format of the zipped files. Default: 'ISO'. Admitted values: 'ISO','DC'\n";

	help += "-deleteTimeStamp: if set to 'true' the last harvesting time stamp is cleared (if exists) before starts the ";
	help += "new harvesting. Default: 'no'. Admitted values: 'yes','no'\n";

	help += "-timeStamp: if set, it overrides the stored last harvesting time stamp\n";

	help += "-maxFiles: limit the number of downloaded files (must be > 0). Default: unlimited'\n";

	help += "-maxErrors: limit the number of errors that can occur during harvesting before to abort. Default: 100'\n";

	help += "-zipContentSize: the total size, expressed in MB, of the xml files to reach before to compress them in to a ZIP file. Default: '\n";

	help += "-uploadZipFiles: if set to 'yes', the zip files are uploaded on Amazon S3 using a separate thread. Default: 'yes'. Admitted values: 'yes','no'\n";
	help += "-removeZipFiles: if set to 'yes', the zip files are removed after the upload. This option is ignored if 'uploadZipFiles is set to 'no'.'Default: 'yes'. Admitted values: 'yes','no'\n";

	help += "-stopOnDeleted: if set to 'yes', the harvesting process stops when the first deleted record is found. Default: 'no'. Admitted values: 'yes','no'\n";

	help += "-clearOnExit: if set to 'yes', at the end of each harvesting, clears all the previous output folders (if any). The process can require much time. ";
	help += "Default 'yes'. Admitted values: 'yes','no'\n";

	help += "-help: get this help\n";

	argsMap = new LinkedHashMap<>();

	argsMap.put("programFolder", System.getProperty("user.home") + File.separator + "oph");
	argsMap.put("outputFormat", "ISO");

	argsMap.put(DELETE_TIMESTAMP, "no");
	argsMap.put(TIMESTAMP, "none");

	argsMap.put("maxFiles", "unlimited");
	argsMap.put("zipContentSize", "250");

	argsMap.put("resumptionToken", "none");
	argsMap.put("currentFolder", "none");
	argsMap.put("harvestedCount", "0");
	argsMap.put("zipCount", "0");

	argsMap.put("uploadZipFiles", "yes");
	argsMap.put("removeZipFiles", "yes");

	argsMap.put("maxErrors", "100");

	argsMap.put("stopOnDeleted", "no");

	argsMap.put("clearOnExit", "yes");

	argsMap.put("endpoint", "http://kma.geodab.eu/dab/services/oaipmh?");

	argsMap.put("awsSecretKey", System.getProperty("awsPassword"));
	argsMap.put("awsClientId", System.getProperty("awsUser"));

	errors = new ArrayList<>();

	format = NumberFormat.getNumberInstance();
	format.setMaximumFractionDigits(2);

	pool = Executors.newCachedThreadPool();
    }

    public void start(String[] args) throws GSException {

	startTime = ISO8601DateTimeUtils.getISO8601DateTime();

	chronometer = new Chronometer(TimeFormat.DAYS_HOUR_MIN_SEC);
	chronometer.start();

	readArgs(args);

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("--------");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Settings");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("--------");

	printSettings();

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("------------------------");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Creating program folders");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("------------------------");

	createProgramFolders();

	if (argsMap.get("uploadZipFiles").equals("yes")) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("-------------------");
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Creating AWS Client");
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("-------------------");

	    String awsClientId = argsMap.get("awsClientId");
	    String awsSecretKey = argsMap.get("awsSecretKey");

	    try {
		createASWClient(awsClientId, awsSecretKey);
	    } catch (Throwable t) {

		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(t.getMessage(), t);
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to create AWS client, exit");

		System.exit(1);
	    }
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("AWS client created");
	}

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("----------------");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Start harvesting");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("----------------");

	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	source.setEndpoint(argsMap.get("endpoint"));
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	HarvestedAccessor accessor = new HarvestedAccessor();
	try {
	    accessor.setGSSource(source);
	} catch (GSException e) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to set accessor source, exit");

	    System.exit(1);
	}

	OAIPMHConnector oaiConnector = new OAIPMHConnector();

	String maxFiles = argsMap.get("maxFiles");
	if (!maxFiles.equals("unlimited")) {

	    oaiConnector.setMaxRecords(Integer.valueOf(maxFiles));
	}
	accessor.setConnector(oaiConnector);

	ListRecordsRequest request = new ListRecordsRequest();

	String argTimeStamp = argsMap.get(TIMESTAMP);
	String timeStamp = readTimeStamp();
	// the argument overrides the stored time stamp
	String ts = null;
	if (!argTimeStamp.equals("none")) {
	    if (argTimeStamp.equals("NOW")) {
		argTimeStamp = ISO8601DateTimeUtils.getISO8601DateTime();
	    }
	    ts = argTimeStamp;
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Setting time stamp from argument: " + ts);
	} else if (timeStamp != null) {
	    ts = timeStamp;
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Setting time stamp from file: " + ts);
	}

	if (ts != null) {
	    request.setFromDateStamp(ts);
	}

	long zipContentSize = Long.valueOf(argsMap.get("zipContentSize")) * 1000000;

	String resumptionToken = argsMap.get("resumptionToken");
	if (resumptionToken.equals("none")) {
	    resumptionToken = null;
	}
	harvestedCount = Integer.valueOf(argsMap.get("harvestedCount"));
	zipCount = Integer.valueOf(argsMap.get("zipCount"));

	int maxErrors = Integer.valueOf(argsMap.get("maxErrors"));

	boolean stopOnDeleted = argsMap.get("stopOnDeleted").endsWith("yes") ? true : false;
	boolean stop = false;
	do {

	    request.setResumptionToken(resumptionToken);
	    ListRecordsResponse<GSResource> response = null;
	    try {
		response = accessor.listRecords(request);
	    } catch (GSException e) {
		errorsCount++;
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Error occurred executing list records, skipping");
	    }

	    if (errorsCount >= maxErrors) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Too many errors occurred, aborting harvesting!");
		break;
	    }

	    if (response != null) {

		Iterator<GSResource> records = response.getRecords();

		int deletedCount = 0;
		int recordsCount = 0;
		while (records.hasNext()) {

		    recordsCount++;

		    GSResource next = records.next();
		    if (next.getPropertyHandler().isDeleted()) {

			if (stopOnDeleted) {
			    GSLoggerFactory.getLogger(OAIPMHHarvester.class)
				    .info("Found deleted record. stopOnDeleted option enabled, stopping harvesting");
			    stop = true;
			    break;
			}

			deletedCount++;
			deletedRecordsCount++;
		    } else {
			storeResource(next);
			harvestedCount++;

			if (currentSize >= zipContentSize) {
			    File zipFile = createZipFile();
			    uploadFile(zipFile);
			}
		    }
		}

		GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Response records: {}", recordsCount);
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Deleted records: {}", deletedCount);

		resumptionToken = response.getResumptionToken();

		if (stop) {
		    resumptionToken = null;
		}
	    }

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Harvested files: {}", harvestedCount);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Current folder size (MB):{}", ((float) currentSize / 1000000));

	} while (resumptionToken != null);

	// zip the remaining files (if any)
	File zipFile = createZipFile();
	uploadFile(zipFile);

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("---------------");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Harvesting done");
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("---------------");

	writeTimeStamp();
	clearOnExit();
	writeReport();
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Exit");
    }

    public static void main(String[] args) throws GSException {

	boolean check = SystemPropertyChecker.checkProperties("awsUser","awsPassword");
	if (!check) {
	    System.exit(1);
	}
	
	OAIPMHHarvester harvester = new OAIPMHHarvester();
	harvester.start(args);
    }

    private void readArgs(String[] args) {

	for (int i = 0; i < args.length; i++) {

	    String arg = args[i];
	    String[] split = arg.split("=");

	    String argName = split[0];
	    if (!supportedArgs.contains(argName)) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("No supported argument: " + argName);
		System.out.println(help);
		System.exit(1);
	    }
	    String argValue = null;
	    if (split.length > 1) {
		argValue = split[1];
	    }

	    checkArg(argName, argValue);

	    argsMap.put(argName.replace("-", ""), argValue);
	}
    }

    private void checkArg(String argName, String argValue) {

	if (argName.equals("-help")) {
	    System.out.println(help);
	    System.exit(0);
	}

	if (argValue == null || argValue.equals("")) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Missing argument value of " + argName);
	    System.exit(1);
	}

	switch (argName) {
	case "-outputFolder":

	    if (!new File(argValue).isDirectory()) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Not a valid folder: " + argValue);
		System.exit(1);
	    }

	    break;

	case DELETE_TIMESTAMP:

	    if (!argValue.equals("yes") && !argValue.equals("no")) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported deleteTimeStamp value: " + argValue);
		System.exit(1);
	    }

	    break;

	case TIMESTAMP:
	    break;

	case "currentFolder":
	    break;

	case "clearOnExit":

	    if (!argValue.equals("yes") && !argValue.equals("no")) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported clearOnExit value: " + argValue);
		System.exit(1);
	    }

	    break;

	case "stopOnDeleted":

	    if (!argValue.equals("yes") && !argValue.equals("no")) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported stopOnDeleted value: " + argValue);
		System.exit(1);
	    }

	    break;

	case "uploadZipFiles":

	    if (!argValue.equals("yes") && !argValue.equals("no")) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported uploadZipFiles value: " + argValue);
		System.exit(1);
	    }

	    break;
	case "-outputFormat":

	    if (!argValue.equals("ISO") && !argValue.equals("DC")) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported output format: " + argValue);
		System.exit(1);
	    }

	    break;

	case "-maxFiles":

	    try {
		if (argValue.equals("unlimited")) {
		    break;
		}
		if (Integer.valueOf(argValue) <= 0) {
		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported maxFiles value: " + argValue);
		    System.exit(1);
		}
	    } catch (NumberFormatException ex) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported maxFiles value: " + argValue);
		System.exit(1);
	    }

	    break;

	case "-zipContentSize":

	    try {
		if (Integer.valueOf(argValue) <= 0) {
		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported zipContentSize value: " + argValue);
		    System.exit(1);
		}
	    } catch (NumberFormatException ex) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported zipContentSize value: " + argValue);
		System.exit(1);
	    }

	    break;
	case "-maxErrors":

	    try {
		if (Integer.valueOf(argValue) <= 0) {
		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported maxErrors value: " + argValue);
		    System.exit(1);
		}
	    } catch (NumberFormatException ex) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unsupported maxErrors value: " + argValue);
		System.exit(1);
	    }

	    break;
	}
    }

    private void createProgramFolders() {

	String currentFolder = argsMap.get("currentFolder");
	if (currentFolder.equals("none")) {
	    currentFolder = ISO8601DateTimeUtils.getISO8601DateTime().replaceAll(":", "").replaceAll("-", "");
	}

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Current folder: " + currentFolder);

	currentFolderFile = new File(argsMap.get("programFolder") + File.separator + currentFolder);

	reportFolder = new File(currentFolderFile, "report");
	dataFolder = new File(currentFolderFile, "data");

	boolean mkdirs = reportFolder.mkdirs();
	mkdirs = dataFolder.mkdirs();

	if (!mkdirs && !reportFolder.exists() && !dataFolder.exists()) {

	    errors.add("Unable to create program folders, exit!");
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to create program folders, exit!");

	    writeReport();
	    System.exit(1);

	} else {
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Report folder created at: " + reportFolder);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Data folder created at: " + dataFolder);
	}
    }

    private String readTimeStamp() {

	File file = new File(argsMap.get("programFolder"), "timeStamp.xml");

	if (argsMap.get(DELETE_TIMESTAMP).equals("yes")) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("deleteTimeStamp option set to 'yes'");

	    if (file.exists()) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Deleting time stamp file: " + file);
		boolean delete = file.delete();
		if (!delete) {
		    errors.add("Unable to delete the timeStamp file, exit!");
		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to delete the timeStamp file, exit!");

		    writeReport();
		    System.exit(1);

		} else {
		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Time stamp file deleted");
		}
	    } else {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("No time stamp file found, nothing to delete");
	    }
	    return null;
	}

	if (!file.exists()) {
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("No time stamp found");
	    return null;
	}
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Reading time stamp");

	try {
	    FileInputStream fileInputStream = new FileInputStream(file);
	    XMLDocumentReader reader = new XMLDocumentReader(fileInputStream);
	    String timeStamp = reader.evaluateString(TIMESTAMP);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Time stamp value: " + timeStamp);

	    return timeStamp;

	} catch (Exception e) {

	    errors.add("Unable to read the time stamp: " + e.getMessage() + ". Exit!");

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to read the timeStamp, exit!");

	    writeReport();
	    System.exit(1);
	}

	return null;
    }

    private void storeResource(GSResource next) {

	MDMetadata mdMetadata = null;
	try {
	    mdMetadata = next.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata();
	} catch (Exception e) {

	    errors.add("Unable to get record to store: " + e.getMessage());

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Unable to get record to store: " + e.getMessage());

	    return;
	}

	File file = new File(dataFolder, harvestedCount + ".xml");

	try {
	    boolean created = file.createNewFile();
	    if (!created) {

		errors.add(UNABLE_TO_WRITE_RECORD + file);

		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(UNABLE_TO_WRITE_RECORD + file);
		return;
	    }
	} catch (Exception e) {

	    errors.add(UNABLE_TO_WRITE_RECORD + e.getMessage());

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(UNABLE_TO_WRITE_RECORD + e.getMessage());
	    return;
	}

	try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

	    byte[] bytes = mdMetadata.asString(false).getBytes(UTF8);
	    currentSize += bytes.length;
	    fileOutputStream.write(bytes);
	    fileOutputStream.flush();

	} catch (Exception e) {

	    errors.add(UNABLE_TO_WRITE_RECORD + e.getMessage());

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(e.getMessage(), e);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).error(UNABLE_TO_WRITE_RECORD + e.getMessage());
	}
    }

    private File createZipFile() {

	File[] xmlFiles = dataFolder.listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return pathname.isFile() && pathname.getName().endsWith(".xml");
	    }
	});

	File zip = null;

	if (argsMap.get("uploadZipFiles").equals("yes")) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Compressing " + xmlFiles.length + " files, it may take mouch time");

	    float count = 0;
	    float percent = 0;
	    try (FileOutputStream outputStream = new FileOutputStream(zip);
		    ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, Charset.forName(UTF8));) {
		zip = new File(dataFolder, zipCount + ".zip");

		for (File file : xmlFiles) {

		    ZipEntry zipEntry = new ZipEntry(file.getName());
		    zipEntry.setTime(file.lastModified());

		    zipOutputStream.putNextEntry(zipEntry);

		    Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());

		    byte[] bytes = Files.readAllBytes(path);

		    zipOutputStream.write(bytes);
		    zipOutputStream.closeEntry();

		    float p = (count / (float) xmlFiles.length) * 100;
		    if (p >= percent) {

			GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Compression status: {}%", format.format(p));
			percent += 1;
		    }

		    count++;
		}

		zipOutputStream.flush();

	    } catch (Exception e) {

		errors.add("Error occurred, unable to create zip file:" + e.getMessage());

		GSLoggerFactory.getLogger(OAIPMHHarvester.class).error("Error occurred, unable to create zip file", e);

	    }

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Compression status: 100%");

	    if (zip != null) {
		GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Created zip file: {}", zip);
		zipCount++;
	    }

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Current zip files count: {}", zipCount);

	} else {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("'uploadZipFiles' option set to 'no', skipping zip file creation");
	}

	currentSize = 0;

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Removing harvested files");
	// remove all the xml files
	for (File file : xmlFiles) {
	    file.delete();
	}
	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Harvested files removed");

	return zip;
    }

    private void writeTimeStamp() {

	File file = new File(argsMap.get("programFolder"), "timeStamp.xml");

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Writing time stamp file");

	if (file.exists()) {

	    boolean delete = file.delete();
	    if (!delete) {

		errors.add("Unable to delete time stamp file. Next harvesting will retrieve all the available records");

		GSLoggerFactory.getLogger(OAIPMHHarvester.class)
			.warn("Unable to delete time stamp file. Next harvesting will retrieve all the available records");
		return;
	    }
	}

	try {
	    boolean created = file.createNewFile();
	    if (!created) {

		errors.add("Unable to create time stamp file. Next harvesting will retrieve all the available records");

		GSLoggerFactory.getLogger(OAIPMHHarvester.class)
			.warn("Unable to create time stamp file. Next harvesting will retrieve all the available records");
		return;
	    }
	} catch (Exception e) {

	    errors.add("Unable to create time stamp file: " + e.getMessage() + ". Next harvesting will retrieve all the available records");

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class)
		    .warn("Unable to create time stamp file. Next harvesting will retrieve all the available records");

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).warn(e.getMessage(), e);
	    return;
	}

	try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {

	    String dateTime = ISO8601DateTimeUtils.getISO8601DateTime();
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Time stamp value: " + dateTime);

	    String timeStamp = "<timeStamp>" + dateTime + "</timeStamp>";
	    fileOutputStream.write(timeStamp.getBytes(UTF8));

	    fileOutputStream.flush();

	} catch (Exception e) {

	    errors.add("Unable to write time stamp file: " + e.getMessage() + ". Next harvesting will retrieve all the available records");

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class)
		    .warn("Unable to write time stamp file. Next harvesting will retrieve all the available records");

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).warn(e.getMessage(), e);
	}
    }

    private void clearOnExit() {

	if (argsMap.get("clearOnExit").equals("yes")) {

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Remove previous folders. It may take much time");

	    File programFolder = new File(argsMap.get("programFolder"));
	    File[] previousFolders = programFolder.listFiles(new FileFilter() {

		@Override
		public boolean accept(File pathname) {

		    return pathname.isDirectory() && !pathname.getAbsolutePath().equals(currentFolderFile.getPath());
		}
	    });

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Found " + previousFolders.length + " folders to remove");

	    for (File folder : previousFolders) {

		File[] internalFolders = folder.listFiles();
		for (File intFolder : internalFolders) {

		    File[] listFiles = intFolder.listFiles();
		    for (File file : listFiles) {
			file.delete();
		    }

		    intFolder.delete();
		}
		folder.delete();
	    }
	}
    }

    private void writeReport() {

	GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Writing report file");

	File report = new File(reportFolder, "report.xml");

	String xml = "<report>\n";
	xml += "   <startTime>" + startTime + "</startTime>\n";
	xml += "   <endTime>" + ISO8601DateTimeUtils.getISO8601DateTime() + "</endTime>\n";
	xml += "   <elapsedTime>" + chronometer.formatElapsedTime() + " (DD:HH:mm:ss)</elapsedTime>\n";
	xml += "   <harvestedRecords>" + harvestedCount + "</harvestedRecords>\n";
	xml += "   <deletedRecords>" + deletedRecordsCount + "</deletedRecords>\n";
	xml += "   <errors>" + errorsCount + "</errors>\n";

	for (String error : errors) {
	    xml += "   <error>" + error + "</error>\n";
	}
	xml += "   <zipFiles>" + zipCount + "</zipFiles>\n";
	xml += "</report>";

	try (FileOutputStream fileOutputStream = new FileOutputStream(report);) {

	    fileOutputStream.write(xml.getBytes(UTF8));

	    fileOutputStream.flush();

	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Report created: " + xml);
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Report stored at: " + report);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).warn("Unable to write report file: " + e.getMessage());
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).warn(e.getMessage(), e);
	}
    }

    private void createASWClient(String awsClient, String awsSecret) {

	BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsClient, awsSecret);

	client = AmazonS3ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(awsCreds))
		.build();
    }

    private void uploadFile(File file) {

	if (argsMap.get("uploadZipFiles").equals("yes")) {

	    pool.submit(new Runnable() {

		@Override
		public void run() {

		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Uploading zip file: " + file);

		    PutObjectRequest p = new PutObjectRequest("geodabzip", currentFolderFile.getName() + "/" + file.getName(), file);

		    PutObjectResult putObject = client.putObject(p);
		    // ObjectMetadata metadata = putObject.getMetadata();

		    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Upload done");

		    if (argsMap.get("removeZipFiles").equals("yes")) {
			GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Removing uploaded file");
			boolean delete = file.delete();
			if (!delete) {
			    GSLoggerFactory.getLogger(OAIPMHHarvester.class).warn("Unable to remove upload file");
			} else {
			    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("Uploaded file removed");
			}
		    }
		}
	    });
	} else {
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info("'uploadZipFiles' option set to 'no', skipping upload");
	}
    }

    private void printSettings() {

	Set<String> keySet = argsMap.keySet();
	for (String key : keySet) {
	    GSLoggerFactory.getLogger(OAIPMHHarvester.class).info(key + "=[" + argsMap.get(key) + "]");
	}
    }
}
