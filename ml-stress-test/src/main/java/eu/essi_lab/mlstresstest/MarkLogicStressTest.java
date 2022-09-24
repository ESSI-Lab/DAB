/**
 * 
 */
package eu.essi_lab.mlstresstest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import org.xml.sax.SAXException;

import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class MarkLogicStressTest {

    private static final Object LOCK = new Object();

    private StorageUri dbUri;
    private String suiteId;
    private int queriesPerMinute;
    private long testLength;

    private MarkLogicDatabase markLogicDB;
    private DatabaseReader reader;

    private int queriesInterval;
    // how many elements to retrieve from the indexes
    private int indexLimit;

    private Bond currentBond;
    private boolean isCountQuery;
    private DecimalFormat format;

    //
    // 32 queries every minute, one every 1,875 seconds.
    // must be multiple of 32 so every minute there are
    // 16 different types of count queries and 16 different types of search queries.
    // every minute the same particular type of count and search query is executed 1 time
    //
    private static final int DEFAULT_QUERIES_PER_MINUTE = 32;
    //
    // default test length is 24 hours
    //
    private static final long DEFAULT_TEST_LENGTH = 1000 * 60;
    //
    // default
    //
    private static final int DEFAULT_INDEX_LIMIT = 10;

    //
    // ---
    //
    private List<String> sourcesIds;
    private List<String> keywords;
    private int queryTypeIndex;
    private List<String> timeBeginList;
    private List<String> timeEndList;
    //
    // --- STATS
    //

    // number of performed count queries
    // private int queriesCount;

    //
    // --- BOND AND TIME TABLE
    //
    // bond with the lowest count execution time
    private Bond shortestCountBond;
    // the execution time of the bond with the lowest count execution time
    private long shortestCountTime;

    // bond with the highest count execution time
    private Bond longestCountBond;
    // the execution time of the bond with the highest count execution time
    private long longestCountTime;

    // bond with the lowest search execution time
    private Bond shortestSearchBond;
    // the execution time of the bond with the lowest search execution time
    private long shortestSearchTime;

    // bond with the highest search execution time
    private Bond longestSearchBond;
    // the execution time of the bond with the highest search execution time
    private long longestSearchTime;

    private String awsInstanceType;
    private String storageType;
    private String cacheConfig;
    private String clusterConfig;
    private String note;

    private boolean queryRegistration;
    //
    // ---
    //
    private static final byte IDS_QUERY = 0;
    private static final byte IDS_TIME_BEGIN_QUERY = 1;
    private static final byte IDS_TIME_END_QUERY = 2;
    private static final byte IDS_TIME_BEGIN_TIME_END_QUERY = 3;
    private static final byte IDS_BBOX_QUERY = 4;
    private static final byte IDS_TIME_BEGIN_BBOX_QUERY = 5;
    private static final byte IDS_TIME_END_BBOX_QUERY = 6;
    private static final byte IDS_TIME_BEGIN_TIME_END_BBOX_QUERY = 7;
    private static final byte IDS_KEYWORD_QUERY = 8;
    private static final byte IDS_TIME_BEGIN_KEYWORD_QUERY = 9;
    private static final byte IDS_TIME_END_KEYWORD_QUERY = 10;
    private static final byte IDS_TIME_BEGIN_TIME_END_KEYWORD_QUERY = 11;
    private static final byte IDS_KEYWORD_BBOX_QUERY = 12;
    private static final byte IDS_KEYWORD_BBOX_QUERY_TIME_BEGIN = 13;
    private static final byte IDS_KEYWORD_BBOX_QUERY_TIME_END = 14;
    private static final byte IDS_KEYWORD_BBOX_QUERY_TIME_BEGIN_TIME_END = 15;
    private static final byte QUERIES_TYPE = 16;
    private static final List<String> QUERIES_TYPES_LIST = new ArrayList<>();
    static {
	QUERIES_TYPES_LIST.add("IDS");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN");
	QUERIES_TYPES_LIST.add("IDS_TIME_END");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN_TIME_END");
	QUERIES_TYPES_LIST.add("IDS_BBOX");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN_BBOX");
	QUERIES_TYPES_LIST.add("IDS_TIME_END_BBOX");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN_TIME_END_BBOX");
	QUERIES_TYPES_LIST.add("IDS_KEYWORD");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN_KEYWORD");
	QUERIES_TYPES_LIST.add("IDS_TIME_END_KEYWORD");
	QUERIES_TYPES_LIST.add("IDS_TIME_BEGIN_TIME_END_KEYWORD");
	QUERIES_TYPES_LIST.add("IDS_KEYWORD_BBOX");
	QUERIES_TYPES_LIST.add("IDS_KEYWORD_BBOX_TIME_BEGIN");
	QUERIES_TYPES_LIST.add("IDS_KEYWORD_BBOX_TIME_END");
	QUERIES_TYPES_LIST.add("IDS_KEYWORD_BBOX_TIME_BEGIN_TIME_END");
    }

    //
    // ---
    //
    private static final String BBOX_0 = "-90,-180,90,180";
    private static final String BBOX_1 = "-90,-180,90,0";
    private static final String BBOX_2 = "-90,0,90,180";
    private static final String BBOX_3 = "0,-180,90,180";
    private static final String BBOX_4 = "-90,-180,0,180";
    private static final String BBOX_5 = "0,-180,90,-90";
    private static final String BBOX_6 = "0,-90,90,0";
    private static final String BBOX_7 = "0,0,90,90";
    private static final String BBOX_8 = "0,90,90,180";
    private static final String BBOX_9 = "-90,-180,0,-90";
    private static final String BBOX_10 = "-90,-90,0,0";
    private static final String BBOX_11 = "-90,0,0,90";
    private static final String BBOX_12 = "-90,-90,0,180";
    private static final String BBOX_13 = "0,-180,90,0";
    private static final String BBOX_14 = "0,0,90,180";
    private static final String BBOX_15 = "-90,-180,0,0";
    private static final String BBOX_16 = "-90,0,0,180";
    private static final List<String> BBOX_LIST = new ArrayList<>();
    static {
	BBOX_LIST.add(BBOX_0);
	BBOX_LIST.add(BBOX_1);
	BBOX_LIST.add(BBOX_2);
	BBOX_LIST.add(BBOX_3);
	BBOX_LIST.add(BBOX_4);
	BBOX_LIST.add(BBOX_5);
	BBOX_LIST.add(BBOX_6);
	BBOX_LIST.add(BBOX_7);
	BBOX_LIST.add(BBOX_8);
	BBOX_LIST.add(BBOX_9);
	BBOX_LIST.add(BBOX_10);
	BBOX_LIST.add(BBOX_11);
	BBOX_LIST.add(BBOX_12);
	BBOX_LIST.add(BBOX_13);
	BBOX_LIST.add(BBOX_14);
	BBOX_LIST.add(BBOX_15);
	BBOX_LIST.add(BBOX_16);
    }
    //
    // ---
    //
    private static final byte MIN_TIME_COL = 1;
    private static final byte MAX_TIME_COL = 2;
    private static final byte AVG_TIME_COL = 3;
    private static final byte MIN_COUNT_COL = 4;
    private static final byte MAX_COUNT_COL = 5;
    private static final byte AVG_COUNT_COL = 6;
    private static final byte COUNT_QUERIES_COUNT_COL = 7;
    private static final byte SEARCH_QUERIES_COUNT_COL = 4;
    private static final byte GLOBAL_AVERAGE_ROW = QUERIES_TYPE + 1;

    //
    // | QUERY_TYPE | MIN_TIME | MAX_TIME | AVG_TIME | MIN_COUNT | MAX_COUNT | AVG_COUNT | QUERIES_COUNT |
    //
    private static final byte TABLE_ROWS = QUERIES_TYPE + 2; // 1 for the header, 1 for the footer (averages)
    private static final byte COUNT_TABLE_COLS = 8;
    private static final String[][] COUNT_TABLE = new String[TABLE_ROWS][COUNT_TABLE_COLS];
    //
    // | QUERY_TYPE | MIN_TIME | MAX_TIME | AVG_TIME | QUERIES_COUNT |
    //
    private static final byte SEARCH_TABLE_COLS = 5;
    private static final String[][] SEARCH_TABLE = new String[TABLE_ROWS][SEARCH_TABLE_COLS];

    static {
	// init header
	COUNT_TABLE[0][0] = "QUERY_TYPE";
	COUNT_TABLE[0][1] = "MIN_TIME";
	COUNT_TABLE[0][2] = "MAX_TIME";
	COUNT_TABLE[0][3] = "AVG_TIME";
	COUNT_TABLE[0][4] = "MIN_COUNT";
	COUNT_TABLE[0][5] = "MAX_COUNT";
	COUNT_TABLE[0][6] = "AVG_COUNT";
	COUNT_TABLE[0][7] = "QUERIES_COUNT";
	// init global average row
	COUNT_TABLE[TABLE_ROWS - 1][0] = "AVERAGE";
	COUNT_TABLE[TABLE_ROWS - 1][COUNT_QUERIES_COUNT_COL] = "-";

	// init query type column
	for (int row = 1; row < TABLE_ROWS - 1; row++) {
	    COUNT_TABLE[row][0] = QUERIES_TYPES_LIST.get(row - 1);
	}

	// init values
	for (int row = 1; row < TABLE_ROWS; row++) {
	    for (int col = 1; col < COUNT_TABLE_COLS; col++) {
		String value = "0";
		if (col == MIN_TIME_COL || col == MIN_COUNT_COL) {
		    value = String.valueOf(Long.MAX_VALUE);
		}
		COUNT_TABLE[row][col] = value;
	    }
	}

	// init header
	SEARCH_TABLE[0][0] = "QUERY_TYPE";
	SEARCH_TABLE[0][1] = "MIN_TIME";
	SEARCH_TABLE[0][2] = "MAX_TIME";
	SEARCH_TABLE[0][3] = "AVG_TIME";
	SEARCH_TABLE[0][4] = "QUERIES_COUNT";
	// init global average row
	SEARCH_TABLE[TABLE_ROWS - 1][0] = "AVERAGE";
	SEARCH_TABLE[TABLE_ROWS - 1][SEARCH_QUERIES_COUNT_COL] = "-";

	// init query type column
	for (int row = 1; row < TABLE_ROWS - 1; row++) {
	    SEARCH_TABLE[row][0] = QUERIES_TYPES_LIST.get(row - 1);
	}

	// init values
	for (int row = 1; row < TABLE_ROWS; row++) {
	    for (int col = 1; col < SEARCH_TABLE_COLS; col++) {
		String value = "0";
		if (col == MIN_TIME_COL) {
		    value = String.valueOf(Long.MAX_VALUE);
		}
		SEARCH_TABLE[row][col] = value;
	    }
	}
    }

    /**
     * 
     */
    public MarkLogicStressTest() {

	setQueriesPerMinute(DEFAULT_QUERIES_PER_MINUTE);
	setTestLength(DEFAULT_TEST_LENGTH);
	setIndexLimit(DEFAULT_INDEX_LIMIT);

	shortestCountTime = Long.MAX_VALUE;
	shortestSearchTime = Long.MAX_VALUE;

	format = new DecimalFormat();
	format.setMaximumFractionDigits(3);
	format.setGroupingUsed(true);
	format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ITALIAN));

	setAwsInstanceType("");
	setStorageType("");
	setCacheConfig("");
	setClusterConfig("");
	setNote("");
	enableQueryRegistration(true);
    }

    /**
     * @param enable
     */
    public void enableQueryRegistration(boolean enable) {

	this.queryRegistration = enable;
    }

    /**
     * @param note
     */
    public void setNote(String note) {
	this.note = note;
    }

    /**
     * @param clusterConfig
     */
    public void setClusterConfig(String clusterConfig) {

	this.clusterConfig = clusterConfig;
    }

    /**
     * @param cacheConfig
     */
    public void setCacheConfig(String cacheConfig) {

	this.cacheConfig = cacheConfig;
    }

    /**
     * @param awsInstanceType
     */
    public void setAwsInstanceType(String awsInstanceType) {

	this.awsInstanceType = awsInstanceType;
    }

    /**
     * @param storageType
     */
    public void setStorageType(String storageType) {

	this.storageType = storageType;
    }

    /**
     * @param queriesPerMinute
     */
    public void setQueriesPerMinute(int queriesPerMinute) {

	this.queriesPerMinute = queriesPerMinute;
    }

    /**
     * @param indexLimit
     */
    public void setIndexLimit(int indexLimit) {

	this.indexLimit = indexLimit;
    }

    /**
     * @param testLength
     */
    public void setTestLength(long testLength) {

	this.testLength = testLength;
    }

    /**
     * @param suiteId
     */
    public void setSuiteId(String suiteId) {

	this.suiteId = suiteId;
    }

    /**
     * @param uri
     */
    public void setDBUri(StorageUri uri) {

	this.dbUri = uri;
    }

    /**
     * @throws Exception
     * @throws UnsupportedEncodingException
     */
    public void start() throws Exception {

	init();

	startExitTimer();

	startTestTimer();
    }

    /**
     * 
     */
    private void startExitTimer() {

	new Timer().schedule(new TimerTask() {

	    @Override
	    public void run() {

		synchronized (LOCK) {

		    try {

			GSLoggerFactory.getLogger(getClass()).info("Test ENDED");

			String[][] conTimeTable = createContraintsAndTimeTable();

			updateTable(SEARCH_TABLE, SEARCH_TABLE_COLS, SEARCH_QUERIES_COUNT_COL);

			updateTable(COUNT_TABLE, COUNT_TABLE_COLS, COUNT_QUERIES_COUNT_COL);

			GSLoggerFactory.getLogger(getClass()).info("Uploading report STARTED");

			String conTimeTableHtml = createHTMLTable(conTimeTable);
			String searchTableHtml = createHTMLTable(SEARCH_TABLE);
			String countTableHtml = createHTMLTable(COUNT_TABLE);

			String htmlPage = createHTMLReport(conTimeTableHtml, searchTableHtml, countTableHtml);

			String uri = uploadReport(htmlPage);

			GSLoggerFactory.getLogger(getClass()).info("Report uploaded to: \n" + uri);
			GSLoggerFactory.getLogger(getClass()).info("Uploading report ENDED");

			System.exit(0);

		    } catch (Exception e) {

			e.printStackTrace();

			GSLoggerFactory.getLogger(getClass()).error("Unable to store report");
			GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
			GSLoggerFactory.getLogger(getClass()).error("Test ENDED");

			System.exit(1);

		    } finally {

			try {
			    markLogicDB.release();
			} catch (GSException e) {
			    e.printStackTrace();
			}
		    }
		}
	    }

	}, this.testLength);
    }

    /**
     * 
     */
    private void startTestTimer() {

	GSLoggerFactory.getLogger(getClass()).info("Test STARTED");

	isCountQuery = true;
	currentBond = null;

	Timer timer = new Timer();
	TimerTask timerTask = new TimerTask() {

	    @Override
	    public void run() {

		synchronized (LOCK) {

		    try {

			if (isCountQuery) {

			    currentBond = createCountBond();
			    isCountQuery = false;

			    doCountQuery();

			} else {

			    isCountQuery = true;

			    doSearchQuery();
			}
		    } catch (Exception ex) {

			ex.printStackTrace();

			GSLoggerFactory.getLogger(getClass()).error("Error occurred: " + ex.getMessage());

		    } finally {

			if (isCountQuery) {

			    if (queryTypeIndex == QUERIES_TYPE - 1) {
				queryTypeIndex = 0;
			    } else {
				queryTypeIndex++;
			    }
			}
		    }
		}
	    }
	};

	timer.scheduleAtFixedRate(timerTask, 0, queriesInterval);
    }

    /**
     * @param bond
     * @throws GSException
     */
    private void doCountQuery() throws Exception {

	Chronometer chronometer = new Chronometer(TimeFormat.MIN_SEC_MLS);

	GSLoggerFactory.getLogger(getClass()).info("Current query type: " + QUERIES_TYPES_LIST.get(queryTypeIndex));
	GSLoggerFactory.getLogger(getClass()).info("Current constraints: " + readConstraints(currentBond));

	DiscoveryMessage message = new DiscoveryMessage();
	message.setNormalizedBond(currentBond);
	message.setPermittedBond(currentBond);
	message.setUserBond(currentBond);
	message.setPage(new Page(10));
	message.setTermFrequencyTargets(Arrays.asList(//
		ResourceProperty.SOURCE_ID, //
		MetadataElement.KEYWORD, //
		MetadataElement.DISTRIBUTION_FORMAT, //
		MetadataElement.ONLINE_PROTOCOL));//

	MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(message, markLogicDB);
	DiscoveryBondParser bondParser = new DiscoveryBondParser(message);
	bondParser.parse(handler);

	GSLoggerFactory.getLogger(getClass()).info("Count query STARTED");

	chronometer.start();

	ResultSequence rs = markLogicDB.getEstimateResultSequence(handler.getCTSSearch(true));

	GSLoggerFactory.getLogger(getClass()).info("Count query ENDED");

	long timeMillis = chronometer.getElapsedTimeMillis();
	String time = chronometer.formatElapsedTime(timeMillis);

	ResultItem next = rs.next();
	InputStream stream = next.asInputStream();

	XMLDocumentReader xmlDocument = createReader(stream);
	
	if (queryRegistration) {
	    //
	    // updates the registered query doc
	    //
	    String regQuery = xmlDocument.evaluateString("//gs:registeredQuery");
	    markLogicDB.getRegisteredQueriesManager().update(regQuery);
	}

	int count = Integer.valueOf(xmlDocument.evaluateString("//gs:estimate"));

	if (timeMillis < shortestCountTime) {
	    shortestCountTime = timeMillis;
	    shortestCountBond = currentBond;
	}

	if (timeMillis > longestCountTime) {
	    longestCountTime = timeMillis;
	    longestCountBond = currentBond;
	}

	//
	// ------------------------------
	//
	long minTime = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][MIN_TIME_COL]);
	long maxTime = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][MAX_TIME_COL]);
	// this col holds until the end the total amount of time, then the average
	long totalTime = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][AVG_TIME_COL]);

	long minCount = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][MIN_COUNT_COL]);
	long maxCount = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][MAX_COUNT_COL]);
	// this col holds until the end the total amount of count, then the average
	long totalCount = Long.valueOf(COUNT_TABLE[queryTypeIndex + 1][AVG_COUNT_COL]);

	if (timeMillis < minTime) {
	    COUNT_TABLE[queryTypeIndex + 1][MIN_TIME_COL] = String.valueOf(timeMillis);
	}
	if (timeMillis > maxTime) {
	    COUNT_TABLE[queryTypeIndex + 1][MAX_TIME_COL] = String.valueOf(timeMillis);
	}
	COUNT_TABLE[queryTypeIndex + 1][AVG_TIME_COL] = String.valueOf(timeMillis + totalTime);

	if (count < minCount) {
	    COUNT_TABLE[queryTypeIndex + 1][MIN_COUNT_COL] = String.valueOf(count);
	}
	if (count > maxCount) {
	    COUNT_TABLE[queryTypeIndex + 1][MAX_COUNT_COL] = String.valueOf(count);
	}
	COUNT_TABLE[queryTypeIndex + 1][AVG_COUNT_COL] = String.valueOf(count + totalCount);
	//
	// ------------------------------
	//
	int queriesCount = Integer.valueOf(COUNT_TABLE[queryTypeIndex + 1][COUNT_QUERIES_COUNT_COL]);
	COUNT_TABLE[queryTypeIndex + 1][COUNT_QUERIES_COUNT_COL] = String.valueOf(++queriesCount);

	GSLoggerFactory.getLogger(getClass()).info("Elapsed time: " + time);
	GSLoggerFactory.getLogger(getClass()).info("Count: " + count);
    }

    /**
     * @param bond
     * @throws GSException
     */
    private void doSearchQuery() throws GSException {

	Chronometer chronometer = new Chronometer(TimeFormat.MIN_SEC_MLS);

	GSLoggerFactory.getLogger(getClass()).info("Current query type: " + QUERIES_TYPES_LIST.get(queryTypeIndex));
	GSLoggerFactory.getLogger(getClass()).info("Current constraints: " + readConstraints(currentBond));

	DiscoveryMessage message = new DiscoveryMessage();

	ResourceSelector selector = new ResourceSelector();
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setSubset(ResourceSubset.FULL);
	selector.setIncludeOriginal(false);

	message.setResourceSelector(selector);

	message.setNormalizedBond(currentBond);
	message.setPermittedBond(currentBond);
	message.setUserBond(currentBond);
	message.setPage(new Page(10));

	MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(message, markLogicDB);
	DiscoveryBondParser bondParser = new DiscoveryBondParser(message);
	bondParser.parse(handler);

	GSLoggerFactory.getLogger(getClass()).info("Search query STARTED");

	chronometer.start();

	markLogicDB.getDiscoverResultSequence(message, message.getPage().getStart(), message.getPage().getSize());

	long timeMillis = chronometer.getElapsedTimeMillis();
	String time = chronometer.formatElapsedTime(timeMillis);

	GSLoggerFactory.getLogger(getClass()).info("Search query ENDED");

	if (timeMillis < shortestSearchTime) {
	    shortestSearchTime = timeMillis;
	    shortestSearchBond = currentBond;
	}

	if (timeMillis > longestSearchTime) {
	    longestSearchTime = timeMillis;
	    longestSearchBond = currentBond;
	}

	//
	// ------------------------------
	//
	long minTime = Long.valueOf(SEARCH_TABLE[queryTypeIndex + 1][MIN_TIME_COL]);
	long maxTime = Long.valueOf(SEARCH_TABLE[queryTypeIndex + 1][MAX_TIME_COL]);
	// this col holds until the end the total amount of time, then the average
	long totalTime = Long.valueOf(SEARCH_TABLE[queryTypeIndex + 1][AVG_TIME_COL]);

	if (timeMillis < minTime) {
	    SEARCH_TABLE[queryTypeIndex + 1][MIN_TIME_COL] = String.valueOf(timeMillis);
	}
	if (timeMillis > maxTime) {
	    SEARCH_TABLE[queryTypeIndex + 1][MAX_TIME_COL] = String.valueOf(timeMillis);
	}
	SEARCH_TABLE[queryTypeIndex + 1][AVG_TIME_COL] = String.valueOf(timeMillis + totalTime);
	//
	// ------------------------------
	//
	int queriesCount = Integer.valueOf(SEARCH_TABLE[queryTypeIndex + 1][SEARCH_QUERIES_COUNT_COL]);
	SEARCH_TABLE[queryTypeIndex + 1][SEARCH_QUERIES_COUNT_COL] = String.valueOf(++queriesCount);

	GSLoggerFactory.getLogger(getClass()).info("Elapsed time: " + time);
	GSLoggerFactory.getLogger(getClass()).info("--------------------------------------");
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private XMLDocumentReader createReader(InputStream stream) throws SAXException, IOException {

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	Map<String, String> namespaces = new HashMap<>();
	namespaces.put(NameSpace.GI_SUITE_DATA_MODEL.getPrefix(), NameSpace.GI_SUITE_DATA_MODEL.getURI());

	reader.setNamespaces(namespaces);

	return reader;
    }

    /**
     * @param bond
     * @return
     */
    private String readConstraints(Bond bond) {

	if (bond instanceof ResourcePropertyBond) {

	    return "Base constraints";
	}

	StringBuilder out = new StringBuilder();

	LogicalBond andBond = (LogicalBond) bond;

	for (Bond operand : andBond.getOperands()) {

	    readConstraints(out, operand);
	}

	if (out.toString().isEmpty()) {

	    return "Base constraints";
	}

	return "Base constraints AND "
		+ out.toString().substring(0, out.length() - " AND".length() - 1).replace("\n", " ").replace("  ", " ");
    }

    /**
     * @param builder
     * @param bond
     */
    private void readConstraints(StringBuilder builder, Bond bond) {

	if (bond instanceof LogicalBond) {

	    LogicalBond andBond = (LogicalBond) bond;

	    for (Bond operand : andBond.getOperands()) {
		readConstraints(builder, operand);
	    }
	} else {

	    if (!(bond instanceof ResourcePropertyBond)) {

		builder.append(bond.toString() + " AND ");
	    }
	}
    }

    /**
     * 
     */
    private void init() throws Exception {

	queriesInterval = 60000 / queriesPerMinute;

	GSLoggerFactory.getLogger(getClass()).info("Initialization STARTED");

	Chronometer testLengthChron = new Chronometer(TimeFormat.DAYS_HOUR_MIN_SEC);
	Chronometer intervalChron = new Chronometer(TimeFormat.MIN_SEC_MLS);

	GSLoggerFactory.getLogger(getClass()).info("DB endpoint: " + dbUri.getUri());
	GSLoggerFactory.getLogger(getClass()).info("DB user: " + dbUri.getUser());
	GSLoggerFactory.getLogger(getClass()).info("DB password: " + dbUri.getPassword());
	GSLoggerFactory.getLogger(getClass()).info("Suite id: " + suiteId);
	GSLoggerFactory.getLogger(getClass()).info("Queries per minute: " + queriesPerMinute);
	GSLoggerFactory.getLogger(getClass()).info("Index limit: " + indexLimit);
	GSLoggerFactory.getLogger(getClass()).info("Queries interval: " + intervalChron.formatElapsedTime(queriesInterval));
	GSLoggerFactory.getLogger(getClass()).info("Test length: " + testLengthChron.formatElapsedTime(testLength));

	GSLoggerFactory.getLogger(getClass()).info("Initializing JAXB context STARTED");
	initJaxb();
	GSLoggerFactory.getLogger(getClass()).info("Initializing JAXB context ENDED");

	DatabaseProvider provider = DatabaseProviderFactory.create(dbUri);
	provider.initialize(dbUri, suiteId);

	reader = DatabaseConsumerFactory.createDataBaseReader(dbUri);
	markLogicDB = (MarkLogicDatabase) reader.getDatabase();

	GSLoggerFactory.getLogger(getClass()).info("Initializing sources ids list STARTED");

	sourcesIds = getSourcesIds();

	GSLoggerFactory.getLogger(getClass()).info("Initializing sources ids list ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Initializing keywords list STARTED");

	keywords = getKeywords();

	GSLoggerFactory.getLogger(getClass()).info("Initializing keywords list ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Initializing time begin list STARTED");

	timeBeginList = getTimeBeginList();

	GSLoggerFactory.getLogger(getClass()).info("Initializing time begin list ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Initializing time end list STARTED");

	timeEndList = getTimeEndList();

	GSLoggerFactory.getLogger(getClass()).info("Initializing time end list ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Initialization ENDED");
    }

    /**
     * @throws Exception
     */
    private void initJaxb() throws Exception {

	CommonContext.createMarshaller(true);
	CommonContext.createUnmarshaller();
	new Dataset();
    }

    /**
     * @return
     * @throws RequestException
     */
    private List<String> readIndex(String indexName) throws RequestException {

	int indexLimit_ = this.indexLimit;
	if (indexName == ResourceProperty.SOURCE_ID_NAME) {
	    indexLimit_ = 500;
	}

	String xQuery = "xquery version '1.0-ml';\n";
	xQuery += "declare namespace html = 'http://www.w3.org/1999/xhtml';\n";
	xQuery += "import module namespace gs='http://flora.eu/gi-suite/1.0/dataModel/schema' at '/gs-modules/functions-module.xqy';\n";
	xQuery += "cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','" + indexName
		+ "'),(), (\"fragment-frequency\",\"frequency-order\",\"descending\",\"limit=" + indexLimit_ + "\",\"eager\"), ())";

	ResultSequence result = markLogicDB.execXQuery(xQuery);
	return Arrays.asList(result.asStrings());
    }

    /**
     * @return
     * @throws RequestException
     */
    private List<String> getSourcesIds() throws RequestException {

	return readIndex(ResourceProperty.SOURCE_ID_NAME);
    }

    /**
     * @return
     * @throws RequestException
     */
    private List<String> getKeywords() throws RequestException {

	return readIndex(MetadataElement.KEYWORD_EL_NAME);
    }

    /**
     * @return
     * @throws RequestException
     */
    private List<String> getTimeBeginList() throws RequestException {

	return readIndex(MetadataElement.TEMP_EXTENT_BEGIN.getName());
    }

    /**
     * @return
     * @throws RequestException
     */
    private List<String> getTimeEndList() throws RequestException {

	return readIndex(MetadataElement.TEMP_EXTENT_END.getName());
    }

    /**
     * @return
     */
    private Bond createSourcesIdsBond() {

	if (sourcesIds.size() == 1) {

	    return BondFactory.createSourceIdentifierBond(sourcesIds.get(0));
	}

	LogicalBond orBond = BondFactory.createOrBond();
	for (String id : sourcesIds) {
	    orBond.getOperands().add(BondFactory.createSourceIdentifierBond(id));
	}

	return orBond;
    }

    /**
     * @return
     */
    private SpatialBond createRandomBboxBond() {

	int randomNum = ThreadLocalRandom.current().nextInt(0, BBOX_LIST.size());

	String bbox = BBOX_LIST.get(randomNum);
	String[] split = bbox.split(",");
	return BondFactory.createSpatialExtentBond(BondOperator.INTERSECTS,
		new SpatialExtent(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3])));
    }

    /**
     * @return
     */
    private SimpleValueBond createRandomKeywordBond() {

	int randomNum = ThreadLocalRandom.current().nextInt(0, keywords.size());

	String keyword = keywords.get(randomNum);
	return BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, keyword);
    }

    /**
     * @return
     */
    private SimpleValueBond createRandomTimeBeginBond() {

	int randomNum = ThreadLocalRandom.current().nextInt(0, timeBeginList.size());

	String time = timeBeginList.get(randomNum);
	return BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, time);
    }

    /**
     * @return
     */
    private SimpleValueBond createRandomTimeEndBond() {

	int randomNum = ThreadLocalRandom.current().nextInt(0, timeEndList.size());

	String time = timeEndList.get(randomNum);
	return BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, time);
    }

    /**
     * @return
     */
    private Bond createCountBond() {

	Bond sourcesIdsBond = createSourcesIdsBond();

	SimpleValueBond timeBeginBond = createRandomTimeBeginBond();
	SimpleValueBond timeEndBond = createRandomTimeEndBond();

	String timeBegin = timeBeginBond.getPropertyValue();
	String timeEnd = timeEndBond.getPropertyValue();

	if (timeBegin.compareTo(timeEnd) > 0) {

	    timeEndBond.setPropertyValue(timeBegin);
	    timeBeginBond.setPropertyValue(timeEnd);
	}

	SimpleValueBond keywordBond = createRandomKeywordBond();
	SpatialBond bboxBond = createRandomBboxBond();

	Bond bond = sourcesIdsBond;

	switch (queryTypeIndex) {
	case IDS_QUERY:
	    bond = sourcesIdsBond;
	    break;
	case IDS_TIME_BEGIN_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond);
	    break;
	case IDS_TIME_END_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeEndBond);
	    break;
	case IDS_TIME_BEGIN_TIME_END_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond, timeEndBond);
	    break;
	case IDS_BBOX_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, bboxBond);
	    break;
	case IDS_TIME_BEGIN_BBOX_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond, bboxBond);
	    break;
	case IDS_TIME_END_BBOX_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeEndBond, bboxBond);
	    break;
	case IDS_TIME_BEGIN_TIME_END_BBOX_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond, timeEndBond, bboxBond);
	    break;
	case IDS_KEYWORD_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, keywordBond);
	    break;
	case IDS_TIME_BEGIN_KEYWORD_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond, keywordBond);
	    break;
	case IDS_TIME_END_KEYWORD_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeEndBond, keywordBond);
	    break;
	case IDS_TIME_BEGIN_TIME_END_KEYWORD_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, timeBeginBond, timeEndBond, keywordBond);
	    break;
	case IDS_KEYWORD_BBOX_QUERY:
	    bond = BondFactory.createAndBond(sourcesIdsBond, keywordBond, bboxBond);
	    break;
	case IDS_KEYWORD_BBOX_QUERY_TIME_BEGIN:
	    bond = BondFactory.createAndBond(sourcesIdsBond, keywordBond, bboxBond, timeBeginBond);
	    break;
	case IDS_KEYWORD_BBOX_QUERY_TIME_END:
	    bond = BondFactory.createAndBond(sourcesIdsBond, keywordBond, bboxBond, timeEndBond);
	    break;
	case IDS_KEYWORD_BBOX_QUERY_TIME_BEGIN_TIME_END:
	    bond = BondFactory.createAndBond(sourcesIdsBond, keywordBond, bboxBond, timeBeginBond, timeEndBond);
	    break;
	}

	return bond;
    }

    /**
     * @param conTimeTableHtml
     * @param searchTableHtml
     * @param countTableHtml
     * @return
     */
    private String createHTMLReport(String conTimeTableHtml, String searchTableHtml, String countTableHtml) {

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

	builder.append("<body><table style='width:700px'>");

	Chronometer testLengthChron = new Chronometer(TimeFormat.DAYS_HOUR_MIN_SEC);
	Chronometer intervalChron = new Chronometer(TimeFormat.MIN_SEC_MLS);

	builder.append("<tr><td><li>DB endpoint: " + dbUri.getUri() + "</li>");
	builder.append("<li>DB user: " + dbUri.getUser() + "</li>");
	builder.append("<li>DB password: " + dbUri.getPassword() + "</li>");
	builder.append("<li>Suite id: " + suiteId + "</li></td>");

	builder.append("<li>AWS instance type: " + awsInstanceType + "</li></td>");
	builder.append("<li>Storage type: " + storageType + "</li></td>");
	builder.append("<li>Cache config: " + cacheConfig + "</li></td>");
	builder.append("<li>Cluster config: " + clusterConfig + "</li></td>");
	builder.append("<li>Note: " + note + "</li></td>");

	builder.append("<li>Suite id: " + suiteId + "</li></td>");

	builder.append("<td><li>Queries per minute: " + queriesPerMinute + "</li>");
	builder.append("<li>Queries interval: " + intervalChron.formatElapsedTime(queriesInterval) + "</li>");
	builder.append("<li>Index limit: " + indexLimit + "</li>");
	builder.append("<li>Test length: " + testLengthChron.formatElapsedTime(testLength) + "</li>");
	builder.append("<li>End date: " + ISO8601DateTimeUtils.getISO8601DateTime() + "</li>");

	builder.append("</td></tr></table>");

	builder.append("<table style='width:100%'>");

	builder.append("<tr><td>");
	builder.append(conTimeTableHtml);
	builder.append("</tr></td>");

	builder.append("<tr><td>");
	builder.append(countTableHtml);
	builder.append("</tr></td>");

	builder.append("<tr><td>");
	builder.append(searchTableHtml);
	builder.append("</tr></td>");

	builder.append("</table></body></html>");

	return builder.toString();
    }

    /**
     * @param table
     * @return
     */
    private String createHTMLTable(String[][] table) {

	StringBuilder builder = new StringBuilder();

	builder.append("<table id='table' style='width:100%'>");

	int rows = table.length;
	int cols = table[0].length;

	for (int row = 0; row < rows; row++) {

	    builder.append("<tr>");

	    for (int col = 0; col < cols; col++) {

		String style = "";

		if (row == rows - 1) {

		    if (col == AVG_TIME_COL && row == GLOBAL_AVERAGE_ROW) {
			style = " style='color: blue; background-color: yellow'";
		    } else {
			style = " style='color: white; background-color: gray'";
		    }
		}

		builder.append(row == 0 ? "<th" + style + ">" : "<td" + style + ">");
		builder.append(table[row][col]);
		builder.append(row == 0 ? "</th>" : "</td>");
	    }

	    builder.append("</tr>");
	}

	builder.append("</table>");

	return builder.toString();
    }

    /**
     * @param table
     * @param colCount
     * @param queriesCountCol
     */
    private void updateTable(String[][] table, int colCount, int queriesCountCol) {

	Chronometer chron = new Chronometer(TimeFormat.SEC_MLS);

	//
	// computes the global average of the MIN_TIME_COL,MAX_TIME_COL,MIN_COUNT_COL and MAX_COUNT_COL
	// and writes them in the GLOBAL_AVERAGE_ROW row at the proper column
	//
	for (int col = 1; col < colCount - 1; col++) {

	    long total = 0;

	    for (int row = 1; row < TABLE_ROWS - 1; row++) {

		total += Long.valueOf(table[row][col]);
	    }

	    long avg = total / QUERIES_TYPE;

	    if (col == MIN_TIME_COL || col == MAX_TIME_COL) {

		table[GLOBAL_AVERAGE_ROW][col] = chron.formatElapsedTime(avg);

	    } else if (col == MIN_COUNT_COL || col == MAX_COUNT_COL) {

		table[GLOBAL_AVERAGE_ROW][col] = format.format(avg);
	    }
	}

	for (int row = 1; row < TABLE_ROWS - 1; row++) {
	    for (int col = 1; col < colCount; col++) {

		if (col == MIN_TIME_COL || col == MAX_TIME_COL) {

		    long value = Long.valueOf(table[row][col]);
		    table[row][col] = chron.formatElapsedTime(value);

		} else if (col == MIN_COUNT_COL || col == MAX_COUNT_COL || col == queriesCountCol) {

		    double count = Double.valueOf(table[row][col]);
		    table[row][col] = format.format(count);
		}
	    }
	}

	//
	// computes the average of the AVG_TIME_COL and AVG_COUNT_COL for each query type, and
	// writes it properly formatted.
	// also computes the global average of the AVG_TIME_COL and AVG_COUNT_COL
	// and writes them in the GLOBAL_AVERAGE_ROW row at the proper column
	//
	for (int col = 1; col < colCount - 1; col++) {

	    double total = 0;

	    for (int row = 1; row < TABLE_ROWS - 1; row++) {

		String value = (table[row][queriesCountCol]).replaceAll("\\.", "");
		int queriesCount = Integer.valueOf(value);

		if (col == AVG_TIME_COL) {

		    long total_ = Long.valueOf(table[row][AVG_TIME_COL]);
		    long avg = total_ / queriesCount;
		    table[row][AVG_TIME_COL] = chron.formatElapsedTime(avg);

		    total += avg;

		} else if (col == AVG_COUNT_COL) {

		    double total_ = (double) Long.valueOf(table[row][AVG_COUNT_COL]);
		    double avg = total_ / (double) queriesCount;
		    table[row][AVG_COUNT_COL] = format.format(avg);

		    total += avg;
		}
	    }

	    double avg = total / QUERIES_TYPE;

	    if (col == AVG_TIME_COL) {

		table[GLOBAL_AVERAGE_ROW][col] = chron.formatElapsedTime((long) avg);

	    } else if (col == AVG_COUNT_COL) {

		table[GLOBAL_AVERAGE_ROW][col] = format.format(avg);
	    }
	}
    }

    /**
     * @return
     */
    // @formatter:off
    private String[][] createContraintsAndTimeTable() {

	Chronometer chron = new Chronometer(TimeFormat.SEC_MLS);

	GSLoggerFactory.getLogger(getClass()).info("Shortest count time: " + chron.formatElapsedTime(shortestCountTime));
	GSLoggerFactory.getLogger(getClass()).info("Shortest count bond: " + readConstraints(shortestCountBond));
	GSLoggerFactory.getLogger(getClass()).info("Longest count time: " + chron.formatElapsedTime(longestCountTime));
	GSLoggerFactory.getLogger(getClass()).info("Longest count bond: " + readConstraints(longestCountBond));

	GSLoggerFactory.getLogger(getClass()).info("Shortest search time: " + chron.formatElapsedTime(shortestSearchTime));
	GSLoggerFactory.getLogger(getClass()).info("Shortest search bond: " + readConstraints(shortestSearchBond));
	GSLoggerFactory.getLogger(getClass()).info("Longest search time: " + chron.formatElapsedTime(longestSearchTime));
	GSLoggerFactory.getLogger(getClass()).info("Longest search bond: " + readConstraints(longestSearchBond));

	//
	// | ---- 0,0 | 0,1 SHORTEST_COUNT | 0,2 LONGEST_COUNT | 0,3 SHORTEST_SEARCH | 0,4 LONGEST_SEARCH |
	// | BOND 1,0 | 1,1 -------------- | 1,2 ------------- | 1,3 --------------- | 1,4 -------------- |
	// | TIME 2,0 | 2,1 -------------- | 2,2 ------------- | 2,3 --------------- | 2,4 -------------- |
	//
	String[][] constraintsTimeTable = new String[3][5];
	constraintsTimeTable[0][0] = "";
	constraintsTimeTable[0][1] = "SHORTEST_COUNT";
	constraintsTimeTable[0][2] = "LONGEST_COUNT";
	constraintsTimeTable[0][3] = "SHORTEST_SEARCH";
	constraintsTimeTable[0][4] = "LONGEST_SEARCH";

	constraintsTimeTable[1][0] = "CONSTRAINTS";
	constraintsTimeTable[2][0] = "TIME";

	constraintsTimeTable[1][1] = readConstraints(shortestCountBond);
	constraintsTimeTable[2][1] = chron.formatElapsedTime(shortestCountTime);

	constraintsTimeTable[1][2] = readConstraints(longestCountBond);
	constraintsTimeTable[2][2] = chron.formatElapsedTime(longestCountTime);

	constraintsTimeTable[1][3] = readConstraints(shortestSearchBond);
	constraintsTimeTable[2][3] = chron.formatElapsedTime(shortestSearchTime);

	constraintsTimeTable[1][4] = readConstraints(longestSearchBond);
	constraintsTimeTable[2][4] = chron.formatElapsedTime(longestSearchTime);
	
	return constraintsTimeTable;
    }// @formatter:on

    /**
     * @param htmlReport
     * @return
     * @throws Exception
     * @throws UnsupportedEncodingException
     */
    private String uploadReport(String htmlReport) throws Exception {

	String time = ISO8601DateTimeUtils.getISO8601DateTime();

	String dirUri = "/stress-test-report/M52XL-ST1/" + time + "/";

	String reportUri = dirUri + "htmlReport.html";

	markLogicDB.getWrapper().storeBinary(reportUri, //
		new ByteArrayInputStream(htmlReport.getBytes("UTF-8")));

	String uriString = dbUri.getUri();
	uriString = uriString.substring(0, uriString.lastIndexOf(","));
	URI uri = new URI(uriString);

	String restUri = "http://" + uri.getHost() + ":8000/v1/documents?category=content&uri=" + reportUri + "&database="
		+ dbUri.getStorageName();

	return restUri;
    }

    /**
     * @param args
     * @param argName
     * @return
     */
    private static String readArg(String[] args, String argName) {

	for (int i = 0; i < args.length; i++) {

	    String arg = args[i];
	    String[] split = arg.split("=");

	    String argName_ = split[0];

	    if (split.length > 1) {
		String argValue = split[1];
		if (argName_.equals(argName)) {
		    return argValue;
		}
	    }
	}

	return null;
    }

    public static void main(String[] args) throws Exception {

	MarkLogicStressTest stressTest = new MarkLogicStressTest();

	args = new String[] { //

		"INDEX_LIMIT=500", //
		"TEST_LENGTH=900000", // (1000 * 60) * 15 -> 15 minutes
		"QUERIES_PER_MINUTE=64", //
		"SUITE_ID=preprodenvconf", //
		"DB_URI="+System.getProperty("dbUrl"), //
		"DB_NAME=PRODUCTION-DB", //
		"DB_PWD="+System.getProperty("dbPassword"), //
		"DB_USER="+System.getProperty("dbUser"),//
	};

	String indexLimit = readArg(args, "INDEX_LIMIT");
	String testLength = readArg(args, "TEST_LENGTH");
	String queriesPerMinute = readArg(args, "QUERIES_PER_MINUTE");
	String suiteId = readArg(args, "SUITE_ID");
	String dbUri = readArg(args, "DB_URI");
	String dbName = readArg(args, "DB_NAME");
	String dbPassword = readArg(args, "DB_PWD");
	String dbUser = readArg(args, "DB_USER");

	// String suiteId = "preprodenvconf";
	// String suiteId = "testenvconf";
	// String suiteId = "gisuite";

	StorageUri storageUri = new StorageUri(dbUri);
	storageUri.setStorageName(dbName);
	storageUri.setUser(dbUser);
	storageUri.setPassword(dbPassword);

	//
	// the index limit impacts on the reuse of a registered query.
	// the lowest the limit, the highest is the probability that a
	// registered query is used because the combination of keywords/time-begin/time-end
	// depends on that limit. with a limit of 1, only one element of the index is retrieved
	// so the combinations are not so many
	//
	stressTest.setIndexLimit(Integer.valueOf(indexLimit));
	stressTest.setSuiteId(suiteId);
	stressTest.setDBUri(storageUri);
	stressTest.setQueriesPerMinute(Integer.valueOf(queriesPerMinute));
	// stressTest.setTestLength(1000 * 60 * 60 * 24); // 8 hours // 24 hours 86400000
	stressTest.setTestLength(Long.valueOf(testLength)); // 2 minutes

	stressTest.enableQueryRegistration(false);

//	stressTest.setAwsInstanceType("c5.18xlarge");
//	stressTest.setStorageType("SSD");
//	stressTest.setCacheConfig("default");
//	stressTest.setClusterConfig("1ED node");
	stressTest.setNote("M5.2XL-ST1");

	stressTest.start();
    }
}
