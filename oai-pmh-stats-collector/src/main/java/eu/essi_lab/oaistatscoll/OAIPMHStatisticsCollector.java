package eu.essi_lab.oaistatscoll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.Lists;

import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.adk.harvest.HarvestedAccessor;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.SystemPropertyChecker;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public class OAIPMHStatisticsCollector {

    private static final int POOL_SIZE = 5;
    private static final List<String> noRecordsSources = new ArrayList<String>();
    private static final List<String> recordsSources = new ArrayList<String>();

    private static final HashMap<String, String[]> VALUES_MAP = new HashMap<>();

    private static final String OAI_KMA_URL = "http://gs-service-production.geodab.eu/gs-service/services/kmaoaipmh?";
    private static int maxConnectorTries;
    private static int maxRecords = 0;

    public OAIPMHStatisticsCollector() {
    }

    /**
     * @author Fabrizio
     */
    private static class SetHarvester implements Runnable {

	private String setSpec;
	private String setName;

	/**
	 * @param setSpecAndName
	 */
	public SetHarvester(String setSpecAndName) {

	    this.setSpec = setSpecAndName.split("SEP")[0];
	    this.setName = setSpecAndName.split("SEP")[1];
	}

	@Override
	public void run() {

	    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("--------");
	    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Harvesting STARTED");

	    Chronometer chronometer = new Chronometer(TimeFormat.DAYS_HOUR_MIN_SEC);
	    chronometer.start();

	    GSSource source = new GSSource();
	    source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	    source.setEndpoint(OAI_KMA_URL);
	    source.setUniqueIdentifier(UUID.randomUUID().toString());

	    HarvestedAccessor accessor = new HarvestedAccessor();
	    accessor.setMaxAttemptsCount(1);
	    try {
		accessor.setGSSource(source);
	    } catch (GSException ex) {
	    }

	    OAIPMHConnector oaiConnector = new OAIPMHConnector();
	    oaiConnector.setMaxAttemptsCount(maxConnectorTries);
	    oaiConnector.setESSILabClientId();

	    @SuppressWarnings("unchecked")
	    GSConfOption<String> gsConfOption = (GSConfOption<String>) oaiConnector.getSupportedOptions()
		    .get(OAIPMHConnector.SET_OPTION_KEY);
	    gsConfOption.setValue(setSpec);

	    // oaiConnector.setPreferredPrefix("oai_dc");
	    if (maxRecords > 0) {
		oaiConnector.setMaxRecords(maxRecords);
	    }

	    accessor.setConnector(oaiConnector);

	    ListRecordsRequest request = new ListRecordsRequest();

	    String resumptionToken = null;
	    int deletedCount = 0;
	    int recordsCount = 0;
	    int totalCount = 0;
	    boolean errors = false;

	    do {

		request.setResumptionToken(resumptionToken);
		ListRecordsResponse<GSResource> response = null;

		try {
		    response = accessor.listRecords(request);

		} catch (Exception e) {
		    errors = true;

		    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).error(e.getMessage(), e);

		    break;
		}

		if (response != null) {

		    Iterator<GSResource> records = response.getRecords();

		    while (records.hasNext()) {

			totalCount++;

			GSResource next = records.next();
			if (next.getPropertyHandler().isDeleted()) {

			    deletedCount++;

			} else {

			    recordsCount++;
			}
		    }

		    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Records count: {}", recordsCount);
		    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Deleted count: {}", deletedCount);
		    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Total count: {}", totalCount);

		    resumptionToken = response.getResumptionToken();
		}

	    } while (resumptionToken != null);

	    String elapsedTime = chronometer.formatElapsedTime();
	    long timeMillis = chronometer.getElapsedTimeMillis();

	    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Harvesting ENDED");
	    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("[" + setName + "] Elapsed time: " + elapsedTime);

	    String[] values = new String[7];
	    try {
		VALUES_MAP.put(URLDecoder.decode(setSpec, "UTF-8"), values);
	    } catch (UnsupportedEncodingException e) {
	    }
	    values[0] = setSpec;
	    values[1] = setName;
	    values[2] = String.valueOf(recordsCount);
	    values[3] = String.valueOf(deletedCount);
	    values[4] = String.valueOf(totalCount);
	    values[5] = errors ? "Yes" : "No";
	    values[6] = String.valueOf(timeMillis);

	    if (recordsCount == 0) {
		noRecordsSources.add(setSpec);
	    } else {
		recordsSources.add(setSpec);
	    }

	    GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("--------");
	}
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	boolean check = SystemPropertyChecker.checkProperties("awsUser", "awsPassword", "dbUrl", "dbUser", "dbPassword", "java.io.tmpdir");
	if (!check) {
	    System.exit(1);
	}

	String oaiEndpont = OAI_KMA_URL;
	OAIPMHStatisticsCollector.maxConnectorTries = 5;
	OAIPMHStatisticsCollector.maxRecords = 0;
	int maxSets = 0;

	for (int i = 0; i < args.length; i++) {

	    String arg = args[i];
	    String[] split = arg.split("=");

	    String argName = split[0];
	    String argValue = null;

	    if (split.length > 1) {
		argValue = split[1];
	    }

	    switch (argName) {
	    case "-oaiEnpoint":
		if (Objects.nonNull(argValue) && !argValue.isEmpty()) {
		    oaiEndpont = argValue;
		}
		break;
	    case "-maxTries":

		if (Objects.nonNull(oaiEndpont) && !oaiEndpont.isEmpty()) {
		    OAIPMHStatisticsCollector.maxConnectorTries = Integer.valueOf(argValue);
		}

		break;
	    case "-maxRecords":

		if (Objects.nonNull(oaiEndpont) && !oaiEndpont.isEmpty()) {
		    OAIPMHStatisticsCollector.maxRecords = Integer.valueOf(argValue);
		}

		break;
	    case "-maxSets":

		if (Objects.nonNull(oaiEndpont) && !oaiEndpont.isEmpty()) {
		    maxSets = Integer.valueOf(argValue);
		}

		break;
	    }
	}

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).debug("- Endpoint: " + oaiEndpont);
	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).debug("- Max tries: " + OAIPMHStatisticsCollector.maxConnectorTries);
	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).debug("- Max records: " + OAIPMHStatisticsCollector.maxRecords);
	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).debug("- Max sets: " + maxSets);

	Downloader downloader = new Downloader();
	InputStream stream = downloader.downloadStream(oaiEndpont + "verb=ListSets").get();
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	List<String> setsSpec = reader.evaluateTextContent("//*:setSpec/text()");
	List<String> setsNames = reader.evaluateTextContent("//*:setName/text()");

	if (maxSets > 0) {
	    setsSpec = setsSpec.subList(0, maxSets);
	    setsNames = setsNames.subList(0, maxSets);
	}

	List<String> specAndNames = new ArrayList<String>();
	for (int i = 0; i < setsSpec.size(); i++) {
	    specAndNames.add(setsSpec.get(i) + "SEP" + setsNames.get(i));
	}

	List<List<String>> partitions = Lists.partition(specAndNames, POOL_SIZE);
	TaskListExecutor<SetHarvester> executor = new TaskListExecutor<>(POOL_SIZE);

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("KMA harvesting STARTED");

	partitions.forEach(part -> {

	    part.forEach(setSpecAndName -> {

		SetHarvester setHarvester = new SetHarvester(setSpecAndName);
		executor.addTask(setHarvester);
	    });
	});

	executor.executeAndWait();

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("KMA harvesting ENDED");

	// +2 = header + footer
	int tableRows = VALUES_MAP.size() + 2;
	int tableCols = 7;
	String[][] table = new String[tableRows][tableCols];// row col
	table[0][0] = "Source label";
	table[0][1] = "Source id";
	table[0][2] = "Records count";
	table[0][3] = "Deleted count";
	table[0][4] = "Total count";
	table[0][5] = "Errors";
	table[0][6] = "Elapsed time";

	String[] keys = VALUES_MAP.keySet().toArray(new String[] {});
	for (int i = 0; i < keys.length; i++) {

	    String[] values = VALUES_MAP.get(keys[i]);

	    String sourceId = values[0];
	    String sourceLabel = values[1];
	    String recordsCount = values[2];
	    String deletedCount = values[3];
	    String totalCount = values[4];
	    String errors = values[5];
	    String time = values[6];

	    table[i + 1][0] = sourceLabel;
	    table[tableRows - 1][0] = "";

	    table[i + 1][1] = sourceId;
	    table[tableRows - 1][1] = "";

	    table[i + 1][2] = recordsCount;
	    table[tableRows - 1][2] = table[tableRows - 1][2] == null ? recordsCount
		    : String.valueOf(Integer.valueOf(table[tableRows - 1][2]) + Integer.valueOf(recordsCount));

	    table[i + 1][3] = deletedCount;
	    table[tableRows - 1][3] = table[tableRows - 1][3] == null ? deletedCount
		    : String.valueOf(Integer.valueOf(table[tableRows - 1][3]) + Integer.valueOf(deletedCount));

	    table[i + 1][4] = totalCount;
	    table[tableRows - 1][4] = table[tableRows - 1][4] == null ? totalCount
		    : String.valueOf(Integer.valueOf(table[tableRows - 1][4]) + Integer.valueOf(totalCount));

	    table[i + 1][5] = errors;
	    int errorsInt = errors.equals("Yes") ? 1 : 0;
	    table[tableRows - 1][5] = table[tableRows - 1][5] == null ? String.valueOf(errorsInt)
		    : String.valueOf(Integer.valueOf(table[tableRows - 1][5]) + Integer.valueOf(errorsInt));

	    table[i + 1][6] = Chronometer.formatElapsedTime(Long.valueOf(time), TimeFormat.DAYS_HOUR_MIN_SEC);
	    table[tableRows - 1][6] = table[tableRows - 1][6] == null ? time
		    : String.valueOf(Integer.valueOf(table[tableRows - 1][6]) + Long.valueOf(time));
	}

	// for (int row = 1; row < tableRows - 1; row++) {
	//
	// for (int col = 2; col < tableCols; col++) {
	//
	// switch(col) {
	// case 2:
	// case 3:
	// case 4:
	// table[tableRows][col] = table[row]
	// }
	// }
	//
	// }

	String htmlTable = createHTMLTable(table);
	uploadHtmlTableToDB(htmlTable);

	uploadReportToDB();
    }

    /**
     * @param table
     * @param format
     * @return
     */
    private static String createHTMLTable(String[][] table) {

	StringBuilder builder = new StringBuilder();

	builder.append("<html>");
	builder.append("<head>");
	builder.append("<style>");
	builder.append("#table {");
	builder.append("  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;");
	builder.append("  border-collapse: collapse;");
	builder.append("  width: 100%;");
	builder.append("}");
	builder.append("#table td, #table th {");
	builder.append(" border: 1px solid #ddd;");
	builder.append("  padding: 8px;");
	builder.append("}");
	builder.append("#table tr:nth-child(even){background-color: #f2f2f2;}");
	builder.append("#table tr:hover {background-color: #ddd;}");
	builder.append("#table th {");
	builder.append("  padding-top: 12px;");
	builder.append("  padding-bottom: 12px;");
	builder.append("  text-align: left;");
	builder.append("  background-color: #4CAF50;");
	builder.append("  color: white;");
	builder.append("}");
	builder.append("</style>");
	builder.append("</head>");

	builder.append("<body>");

	builder.append("<script>");
	builder.append("var asc = true;");
	builder.append("function sortTable(selectedCol) {");
	builder.append("  var table, rows, switching, i, x, y, shouldSwitch;");
	builder.append("  table = document.getElementById('table');");
	builder.append("  switching = true;");
	builder.append(" while (switching) {");
	builder.append("   switching = false;");
	builder.append("    rows = table.rows;");
	builder.append("    for (i = 1; i < (rows.length - 2); i++) {");
	builder.append("      shouldSwitch = false;");
	builder.append("      x = rows[i].getElementsByTagName('TD')[selectedCol];");
	builder.append("      y = rows[i + 1].getElementsByTagName('TD')[selectedCol];");
	builder.append("      if(asc){");

	builder.append(
		"        if ( (selectedCol >=2 && selectedCol <=4) && parseInt(x.innerHTML.replace(/[.]/g,'')) > parseInt(y.innerHTML.replace(/[.]/g,''))) {");
	builder.append("          shouldSwitch = true;");
	builder.append("          break;");
	builder.append("        }");

	builder.append(
		"       if ( (selectedCol == 0 || selectedCol == 1 || selectedCol == 5 || selectedCol == 6) && x.innerHTML.replace(/[.]/g,'') > y.innerHTML.replace(/[.]/g,'')) {");
	builder.append("         shouldSwitch = true;");
	builder.append("         break;");
	builder.append("       }");

	builder.append("      }else{");

	builder.append(
		"       if ( (selectedCol == 0 || selectedCol == 1 || selectedCol == 5 || selectedCol == 6) && x.innerHTML.replace(/[.]/g,'') < y.innerHTML.replace(/[.]/g,'')) {");
	builder.append("         shouldSwitch = true;");
	builder.append("         break;");
	builder.append("       }");

	builder.append(
		"       if ( (selectedCol >=2 && selectedCol <=4) && parseInt(x.innerHTML.replace(/[.]/g,'')) < parseInt(y.innerHTML.replace(/[.]/g,''))) {");
	builder.append("         shouldSwitch = true;");
	builder.append("         break;");
	builder.append("       }");

	builder.append("      }");
	builder.append("    }");
	builder.append("    if (shouldSwitch) {");
	builder.append("      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);");
	builder.append("      switching = true;");
	builder.append("    }");
	builder.append("  }");
	builder.append("  asc = !asc;");
	builder.append("}");
	builder.append("</script>");

	builder.append("<table id='table' style='width:100%'>");

	int rows = table.length;
	int cols = table[0].length;

	for (int row = 0; row < rows; row++) {

	    builder.append("<tr>");

	    for (int col = 0; col < cols; col++) {

		String style = "";
		String value = table[row][col];

		if (row == table.length - 1 && col == table[0].length - 1) {

		    value = Chronometer.formatElapsedTime(Long.valueOf(table[row][col]), TimeFormat.DAYS_HOUR_MIN_SEC);
		}

		if (row == 0) {

		    builder.append("<th" + style + "><button style='width: 100%;' onclick='sortTable(" + col + ")'>Sort</button><br>");

		} else if (row == table.length - 1 && col > 1) {

		    builder.append("<td" + " style='background-color: black; color: white;'" + ">");

		} else {

		    builder.append("<td" + style + ">");
		}

		builder.append(value);
		builder.append(row == 0 ? "</th>" : "</td>");
	    }

	    builder.append("</tr>");
	}

	builder.append("</table>");

	builder.append("</table></body></html>");

	return builder.toString();
    }

    /**
     * @return
     * @throws Exception
     */
    private static void uploadHtmlTableToDB(String htmlTable) throws Exception {

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Uploading html table STARTED");

	File reportFile = new File(System.getProperty("java.io.tmpdir") + "/report.txt");

	FileOutputStream outputStream = new FileOutputStream(reportFile);

	outputStream.write(htmlTable.getBytes("UTF-8"));

	outputStream.close();

	FileInputStream inputStream = new FileInputStream(reportFile);

	String reportUri = "/kmaharvester/" + ISO8601DateTimeUtils.getISO8601DateTime() + "/htmlTable.html";

	StorageUri storageUri = new StorageUri(System.getProperty("dbUrl"));
	storageUri.setStorageName("PRODUCTION-DB");
	storageUri.setUser(System.getProperty("dbUser"));
	storageUri.setPassword(System.getProperty("dbPassword"));

	DatabaseProvider provider = new DatabaseProviderFactory().create(storageUri);
	provider.initialize(storageUri, "preprodenvconf");

	MarkLogicDatabase markLogicDB = (MarkLogicDatabase) new DatabaseConsumerFactory().//
		createDataBaseReader(storageUri).getDatabase();

	markLogicDB.getWrapper().storeBinary(reportUri, inputStream);

	String uriString = storageUri.getUri();
	uriString = uriString.substring(0, uriString.lastIndexOf(","));
	URI uri = new URI(uriString);

	String restUri = "http://" + uri.getHost() + ":8000/v1/documents?category=content&uri=" + reportUri + "&database="
		+ storageUri.getStorageName();

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Table URI: \n" + restUri);

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Uploading html table ENDED");
    }

    /**
     * @return
     * @throws Exception
     */
    private static void uploadReportToDB() throws Exception {

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Uploading report file STARTED");

	File reportFile = new File(System.getProperty("java.io.tmpdir") + "/report.txt");

	FileOutputStream outputStream = new FileOutputStream(reportFile);

	outputStream.write("- Sources with records -\n\n".getBytes("UTF-8"));

	recordsSources.forEach(srcId -> {

	    try {
		outputStream.write(srcId.getBytes("UTF-8"));
		outputStream.write("\n".getBytes("UTF-8"));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).error("Error occurred: " + e.getMessage());
		GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).error(e.getMessage(), e);
	    }
	});

	outputStream.write("\n- No records sources -\n\n".getBytes("UTF-8"));

	noRecordsSources.forEach(srcId -> {

	    try {
		outputStream.write(srcId.getBytes("UTF-8"));
		outputStream.write("\n".getBytes("UTF-8"));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).error("Error occurred: " + e.getMessage());
		GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).error(e.getMessage(), e);
	    }
	});

	outputStream.close();

	FileInputStream inputStream = new FileInputStream(reportFile);

	String reportUri = "/kmaharvester/" + ISO8601DateTimeUtils.getISO8601DateTime() + "/report.txt";

	StorageUri storageUri = new StorageUri(System.getProperty("dbUrl"));
	storageUri.setStorageName("PRODUCTION-DB");
	storageUri.setUser(System.getProperty("dbUser"));
	storageUri.setPassword(System.getProperty("dbPassword"));

	DatabaseProvider provider = new DatabaseProviderFactory().create(storageUri);
	provider.initialize(storageUri, "preprodenvconf");

	MarkLogicDatabase markLogicDB = (MarkLogicDatabase) new DatabaseConsumerFactory().//
		createDataBaseReader(storageUri).getDatabase();

	markLogicDB.getWrapper().storeBinary(reportUri, inputStream);

	String uriString = storageUri.getUri();
	uriString = uriString.substring(0, uriString.lastIndexOf(","));
	URI uri = new URI(uriString);

	String restUri = "http://" + uri.getHost() + ":8000/v1/documents?category=content&uri=" + reportUri + "&database="
		+ storageUri.getStorageName();

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Report URI: \n" + restUri);

	GSLoggerFactory.getLogger(OAIPMHStatisticsCollector.class).info("Uploading report file ENDED");
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static void uploadReportToS3() throws Exception {

	// GSLoggerFactory.getLogger(KMAHarvester.class).info("Uploading report file STARTED");
	//
	// File reportFile = new File(System.getProperty("java.io.tmpdir") + "/report.txt");
	//
	// FileOutputStream outputStream = new FileOutputStream(reportFile);
	//
	// MSG_MAP.keySet().forEach(key -> {
	//
	// MSG_MAP.get(key).forEach(msg -> {
	//
	// try {
	// outputStream.write(msg.getBytes("UTF-8"));
	// outputStream.write("\n".getBytes("UTF-8"));
	// } catch (Exception e) {
	// GSLoggerFactory.getLogger(KMAHarvester.class).error("Error occurred: " + e.getMessage());
	// GSLoggerFactory.getLogger(KMAHarvester.class).error(e.getMessage(), e);
	// }
	// });
	// });
	//
	// outputStream.write("\n Sources with 0 valid records\n".getBytes("UTF-8"));
	//
	// MSG_MAP.keySet().forEach(key -> {
	//
	// MSG_MAP.get(key).forEach(msg -> {
	//
	// try {
	// outputStream.write(msg.getBytes("UTF-8"));
	// outputStream.write("\n".getBytes("UTF-8"));
	// } catch (Exception e) {
	// GSLoggerFactory.getLogger(KMAHarvester.class).error("Error occurred: " + e.getMessage());
	// GSLoggerFactory.getLogger(KMAHarvester.class).error(e.getMessage(), e);
	// }
	// });
	// });
	//
	// outputStream.close();
	//
	// PutObjectRequest p = new PutObjectRequest("report", "kmaharvester/report.txt", reportFile);
	//
	// PutObjectResult putObject = createASWClient().putObject(p);
	//
	// GSLoggerFactory.getLogger(KMAHarvester.class).info("Uploading report file ENDED");
    }

    /**
     * @return
     */
    @SuppressWarnings("unused")
    private static AmazonS3 createASWClient() {

	String awsSecret = System.getProperty("awsPassword");
	String awsClient = System.getProperty("awsUser");

	BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsClient, awsSecret);

	return AmazonS3ClientBuilder.//
		standard().//
		withRegion("us-east-1").//
		withCredentials(new AWSStaticCredentialsProvider(awsCreds)).//
		build();
    }
}
