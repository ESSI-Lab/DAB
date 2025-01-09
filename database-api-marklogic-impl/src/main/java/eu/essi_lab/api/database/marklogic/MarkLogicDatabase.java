package eu.essi_lab.api.database.marklogic;

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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XQueryException;
import com.marklogic.xcc.types.ItemType;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmNode;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.marklogic.search.DistinctQueryHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSelectorBuilder;
import eu.essi_lab.api.database.marklogic.search.RegisteredQueriesManager;
import eu.essi_lab.api.database.marklogic.stats.StatisticsQueryManager;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicDatabase extends Database {

    /**
     * Query trace disabled because not strictly necessary and it makes logs unreadable
     */
    public static final boolean QUERY_TRACE = false;

    public static final Boolean SKIP_REGISTERED_QUERIES_MANAGER = true;
    protected static final String SEMANTIC_FOLDER = "semantic";

    //
    // ---
    //

    private static final String RUNTIME_INFO_PROTECTED_FOLDER = "runtime-info";
    private static final String GS_MODULES_PROTECTED_FOLDER = "gs-modules";
    //
    // ---
    //
    // Indexes manager is always disabled. Use IndexingManagerTool to update indexes
    //
    // ---
    //
    public static boolean indexesManagerEnabled;
    //
    // ---
    //
    private MarkLogicWrapper wrapper;
    private String dbIdentifier;
    private StorageInfo dbInfo;
    private boolean initialized;
    private HashMap<String, MarkLogicSourceStorageWorker> workersMap;
    private RegisteredQueriesManager regQueriesManager;
    //
    // ---
    //
    private static final String MARKLOGIC_XCC_CONFIGURATION_ERROR = "MARKLOGIC_XCC_CONFIGURATION_ERROR";
    private static final String MARKLOGIC_URI_SYNTAX_CONFIGURATION_ERROR = "MARKLOGIC_URI_SYNTAX_CONFIGURATION_ERROR";
    private static final String MARKLOGIC_SEARCH_ERROR = "MARKLOGIC_SEARCH_ERROR";
    private static final String MARKLOGIC_ESTIMATE_ERROR = "MARKLOGIC_ESTIMATE_ERROR";
    private static final String MARKLOGIC_SOURCE_STORAGE_INIT_ERROR = "MARKLOGIC_SOURCE_STORAGE_INIT_ERROR";
    private static final String MARKLOGIC_INIT_CONFIGURATION_ERROR = "MARKLOGIC_INIT_CONFIGURATION_ERROR";
    private static final String MARKLOGIC_MODULE_INIT_ERROR = "MARKLOGIC_MODULE_INIT_ERROR";
    private static final String MARKLOGIC_STATS_ERROR = "MARKLOGIC_STATS_ERROR";
    //
    // ---
    //
    private static final String WRAPPER_COUNT_START = "Marklogic Wrapper Estimating STARTED";
    private static final String WRAPPER_COUNT_ENDED = "Marklogic Wrapper Estimating ENDED";
    private static final String MARKLOGIC_COUNT_COMPLETED = "Marklogic Count ENDED";
    private static final String MARKLOGIC_DB_SEARCH_STARTED = "Marklogic Database Search STARTED";
    private static final String MARKLOGIC_DB_SEARCH_ENDED = "Marklogic Database Search ENDED";
    private static final String MARKLOGIC_DB_COMPUTATION_STARTED = "Marklogic Computation STARTED";
    private static final String MARKLOGIC_DB_COMPUTATION_ENDED = "Marklogic Computation ENDED";
    private static final String WRAPPER_SUBMIT_STARTED = "Marklogic Wrapper Submit STARTED";
    private static final String WRAPPER_SUBMIT_ENDED = "Marklogic Wrapper Submit ENDED";

    private static final String FUNCTIONS_MODULE_NAME = "functions-module.xqy";
    private static final String FUNCTIONS_MODULE_PATH = "/gs-modules/functions-module.xqy";

    //
    // This variable enables the HTTP load balancer on the XCC protocol
    //
    // See: http://docs.marklogic.com/8.0/guide/xcc/concepts#id_85598
    // See: https://help.marklogic.com/Knowledgebase/Article/View/32/15/load-balancing-in-marklogic
    //
    static {

	System.setProperty("xcc.httpcompliant", "true");
    }

    public MarkLogicDatabase() {

	workersMap = new HashMap<>();
    }

    @Override
    public void initialize(StorageInfo storageUri) throws GSException {

	if (!initialized) {

	    dbIdentifier = storageUri.getIdentifier() == null ? UUID.randomUUID().toString() : storageUri.getIdentifier();

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB initialization STARTED");

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB wrapper initialization STARTED");
	    initializeWrapper(storageUri);
	    //
	    // now the wrapper, required by the manager, is initialized and the manager can be created
	    //
	    regQueriesManager = new RegisteredQueriesManager(this);

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB wrapper initialization ENDED");

	    if (indexesManagerEnabled) {
		GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB index manager initialization STARTED");
		initializeIndexManager();
		GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB index manager initialization ENDED");
	    } else {
		GSLoggerFactory.getLogger(this.getClass()).warn("MarkLogic DB index manager initialization DISABLED");
	    }

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB storage folders initialization STARTED");
	    initializeStorageWorkers();
	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB storage folders initialization ENDED");

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB module initialization STARTED");

	    try {
		if (!exists_(FUNCTIONS_MODULE_PATH)) {

		    initializeModule();

		} else {

		    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB module already exists");
		}
	    } catch (RequestException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
		GSLoggerFactory.getLogger(this.getClass())
			.error("Error occurred during module existance check, an attempt to store it will be done");

		initializeModule();
	    }

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB module initialization ENDED");

	    if (!SKIP_REGISTERED_QUERIES_MANAGER) {
		GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB registered queries doc initialization STARTED");
		regQueriesManager.init(storageUri.getName());
		GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB registered queries doc initialization ENDED");
	    }

	    GSLoggerFactory.getLogger(this.getClass()).info("MarkLogic DB initialization ENDED");
	}
    }

    @Override
    public void configure(DatabaseSetting setting) {

	try {
	    initialize(setting.asStorageUri());

	} catch (GSException e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(//
		    e.getErrorInfoList().get(0).getErrorDescription(), //
		    e.getCause());
	}
    }

    @Override
    public DatabaseSetting getSetting() {

	return new DatabaseSetting(dbInfo);
    }

    @Override
    public void release() throws GSException {

	GSLoggerFactory.getLogger(this.getClass()).info("Releasing MarklogicClient connection");
	getWrapper().getClient().release();
    }

    @Override
    public boolean supports(StorageInfo dbUri) {

	if (isSupported(dbUri)) {

	    this.dbInfo = dbUri;
	    return true;
	}

	return false;
    }

    /**
     * @param dbUri
     * @return
     */
    public static boolean isSupported(StorageInfo dbUri) {

	if (dbUri != null && dbUri.getUri() != null && dbUri.getUri().startsWith("xdbc")) {
	    return true;
	}

	return false;
    }

    @Override
    public StorageInfo getStorageInfo() {

	return dbInfo;
    }

    /**
     * @return
     */

    public String getIdentifier() {

	return dbIdentifier;
    }

    private void initializeWrapper(StorageInfo dbInfo) throws GSException {

	// ------------------------------------------
	//
	// content source initialization
	//
	try {

	    String uri = dbInfo.getUri();
	    String dataBaseName = dbInfo.getName();
	    String user = dbInfo.getUser();
	    String password = dbInfo.getPassword();

	    wrapper = new MarkLogicWrapper(uri, user, password, dataBaseName);

	    this.dbInfo = dbInfo;
	    this.initialized = true;

	} catch (URISyntaxException e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    MARKLOGIC_URI_SYNTAX_CONFIGURATION_ERROR, //
		    e);

	} catch (Exception e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    MARKLOGIC_XCC_CONFIGURATION_ERROR, //
		    e);
	}
    }

    private void initializeIndexManager() throws GSException {
	try {
	    new MarkLogicIndexesManager(this);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).error("Fatal Error instantiating marklogic index manager", e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    MARKLOGIC_INIT_CONFIGURATION_ERROR, //
		    e);
	}

    }

    private void initializeStorageWorkers() throws GSException {
	try {

	    DatabaseFolder[] folders = getFolders();
	    for (int i = 0; i < folders.length; i++) {
		String sourceId = SourceStorageWorker.retrieveSourceName(dbIdentifier, folders[i].getName());

		if (sourceId != null) {

		    MarkLogicSourceStorageWorker worker = new MarkLogicSourceStorageWorker(sourceId, this);
		    workersMap.put(sourceId, worker);
		}
	    }

	} catch (Exception e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    MARKLOGIC_SOURCE_STORAGE_INIT_ERROR, //
		    e);
	}
    }

    /**
     * @throws GSException
     */
    private void initializeModule() throws GSException {

	try {

	    InputStream stream = getClass().getClassLoader().getResourceAsStream(FUNCTIONS_MODULE_NAME);

	    getWrapper().storeBinary(FUNCTIONS_MODULE_PATH, stream);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    MARKLOGIC_MODULE_INIT_ERROR, //
		    e);
	}
    }

    /**
     * @param ctsSearch
     * @return
     * @throws GSException
     */
    public DiscoveryCountResponse estimate(String ctsSearch, boolean updateRegQueries, String requestID) throws GSException {

	// logger.debug("--- Start of ML estimate query ---");
	// logger.debug(estimate);
	// logger.debug("--- End of ML estimate query ---");

	DiscoveryCountResponse countResult = new DiscoveryCountResponse();

	try {

	    // GSLoggerFactory.getLogger(getClass()).trace("[1/5] Estimating result sequence STARTED");

	    ResultSequence rs = getEstimateResultSequence(ctsSearch, requestID);

	    // GSLoggerFactory.getLogger(getClass()).trace("[1/5] Estimating result sequence ENDED");

	    // GSLoggerFactory.getLogger(getClass()).trace("[2/5] Acquiring item STARTED");

	    ResultItem next = rs.next();

	    ItemType itemType = next.getItemType();

	    // GSLoggerFactory.getLogger(getClass()).trace("Item type: {}", itemType.toString());

	    String nextAsString = next.asString();
	    // GSLoggerFactory.getLogger(getClass()).trace("Item as string: <{}>", nextAsString);

	    // GSLoggerFactory.getLogger(getClass()).trace("[2/5] Acquiring item ENDED");

	    if (itemType.equals(ValueType.XS_INTEGER)) {

		countResult.setCount(Integer.valueOf(nextAsString));

		// GSLoggerFactory.getLogger(getClass()).trace("Item is integer, no other phase required");

		// logger.trace(MARKLOGIC_COUNT_COMPLETED);

		return countResult;
	    }

	    // GSLoggerFactory.getLogger(getClass()).trace("[3/5] Parsing item STARTED");

	    XMLDocumentReader reader = CommonNameSpaceContext.createCommonReader(nextAsString);

	    // GSLoggerFactory.getLogger(getClass()).trace("[3/5] Parsing item ENDED");

	    // GSLoggerFactory.getLogger(getClass()).trace("[4/5] Evaluating tf STARTED");

	    Node tfMap = reader.evaluateNodes("//gs:termFrequency")[0];

	    if (tfMap != null) {

		TermFrequencyMap termFrequencyMap = TermFrequencyMap.create(tfMap);
		countResult.setTermFrequencyMap(termFrequencyMap);
	    }

	    // GSLoggerFactory.getLogger(getClass()).trace("[4/5] Evaluating tf ENDED");

	    // GSLoggerFactory.getLogger(getClass()).trace("[5/5] Evaluating estimate STARTED");

	    String count = reader.evaluateString("//gs:estimate");

	    countResult.setCount(Integer.valueOf(count));

	    // GSLoggerFactory.getLogger(getClass()).trace("[5/5] Evaluating estimate ENDED");

	    //
	    // *** DISABLED ***
	    //
	    // updates the registered query doc
	    //
	    if (updateRegQueries) {

		String regQuery = reader.evaluateString("//gs:registeredQuery");

		regQueriesManager.update(regQuery);
	    }

	    // logger.trace(MARKLOGIC_COUNT_COMPLETED);

	    return countResult;

	} catch (XQueryException xqe) {

	    GSLoggerFactory.getLogger(getClass()).error(xqe.getMessage(), xqe);

	    String formatString = xqe.getFormatString();
	    throw GSException.createException(//
		    getClass(), //
		    formatString, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARKLOGIC_ESTIMATE_ERROR, //
		    xqe);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARKLOGIC_ESTIMATE_ERROR, //
		    e);
	}
    }

    /**
     * @param message
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> search(DiscoveryMessage message, int start, int count, Class<T> clazz) throws GSException {

	List<T> list = new ArrayList<>();

	try {

	    // GSLoggerFactory.getLogger(this.getClass()).trace(MARKLOGIC_DB_SEARCH_STARTED);

	    ResultSequence rs = getDiscoverResultSequence(message, start, count);
	    Optional<WebRequest> oqs = Optional.ofNullable(message.getWebRequest());

	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_NODES_CREATION,
		    message.getRequestId(), oqs);

	    Iterator<ResultItem> iterator = rs.iterator();

	    DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    while (iterator.hasNext()) {

		ResultItem next = iterator.next();
		XdmItem item = next.getItem();
		XdmNode xdmNode = ((XdmNode) item);

		if (clazz.equals(Node.class)) {

		    Node node = xdmNode.asW3cNode(builder);
		    list.add((T) node);

		} else if (clazz.equals(String.class)) {

		    String str = xdmNode.asString();
		    list.add((T) str);
		}
	    }

	    pl.logPerformance(GSLoggerFactory.getLogger(this.getClass()));
	    // GSLoggerFactory.getLogger(this.getClass()).trace(MARKLOGIC_DB_SEARCH_ENDED);

	} catch (GSException ex) {

	    throw ex;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARKLOGIC_SEARCH_ERROR, //
		    e);
	}

	return list;
    }

    /**
     * @param statsMessage
     * @return
     * @throws RequestException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public StatisticsResponse compute(StatisticsMessage statsMessage) throws GSException {

	try {

	    // GSLoggerFactory.getLogger(this.getClass()).trace(MARKLOGIC_DB_COMPUTATION_STARTED);

	    String statQuery = StatisticsQueryManager.getInstance().createComputeQuery(statsMessage, this);

	    statQuery = includePreamble(statQuery);

	    // GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_SUBMIT_STARTED);

	    ResultSequence resultSequence = execXQuery(statQuery);

	    // GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_SUBMIT_ENDED);

	    String stringResult = resultSequence.asString();

	    StatisticsResponse response = StatisticsResponse.create(stringResult);

	    // GSLoggerFactory.getLogger(this.getClass()).trace(MARKLOGIC_DB_COMPUTATION_ENDED);

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARKLOGIC_STATS_ERROR);
	}
    }

    /**
     * @param ctsSearch
     * @param requestID
     * @return
     * @throws RequestException
     */
    public ResultSequence getEstimateResultSequence(String ctsSearch, String requestID) throws RequestException {

	String estimate = includePreamble(ctsSearch);

	// GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_COUNT_START);

	ResultSequence rs = execXQuery(estimate, requestID);

	// GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_COUNT_ENDED);

	return rs;
    }

    /**
     * @param message
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public ResultSequence getDiscoverResultSequence(DiscoveryMessage message, int start, int count) throws GSException {

	try {
	    Optional<WebRequest> oqs = Optional.ofNullable(message.getWebRequest());
	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_QUERY_GENERATION,
		    message.getRequestId(), oqs);

	    MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(message, this);
	    DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());
	    bondParser.parse(handler);

	    String ctsSearch = handler.getCTSSearch(false);

	    if (message.getDistinctValuesElement().isPresent()) {

		String ctsSearchQuery = handler.getCTSSearchQuery(true);

		ctsSearch = DistinctQueryHandler.handleSearch(message, ctsSearchQuery, this, count, start);

	    } else {

		ctsSearch = applyPagination(ctsSearch, start, count);
	    }

	    ctsSearch = includePreamble(ctsSearch);

	    MarkLogicSelectorBuilder selBuilder = new MarkLogicSelectorBuilder(//
		    message, //
		    ctsSearch);

	    ctsSearch = selBuilder.applySelection();
	    pl.logPerformance(GSLoggerFactory.getLogger(this.getClass()));
	    // logger.debug("--- Start of ML search query ---");
	    // logger.debug(ctsSearch);
	    // logger.debug("--- End of ML search query ---");

	    // GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_SUBMIT_STARTED);

	    pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_QUERY_EXECUTION, message.getRequestId(), oqs);

	    ResultSequence rs = execXQuery(ctsSearch);

	    pl.logPerformance(GSLoggerFactory.getLogger(this.getClass()));
	    // GSLoggerFactory.getLogger(this.getClass()).trace(WRAPPER_SUBMIT_ENDED);

	    return rs;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARKLOGIC_SEARCH_ERROR, //
		    e);
	}
    }

    /**
     * @return
     * @throws GSException
     * @throws Exception
     */
    @Override
    public SourceStorageWorker getWorker(String sourceId) throws GSException {

	MarkLogicSourceStorageWorker worker = workersMap.get(sourceId);
	if (worker == null) {
	    worker = new MarkLogicSourceStorageWorker(sourceId, this);
	    workersMap.put(sourceId, worker);
	}

	return worker;
    }

    /**
     * @return
     */

    public ContentSource getContentSource() {

	return wrapper.getContentSource();
    }

    // -----------------------------------------------------
    //
    // Folders section
    //
    //

    @Override
    public DatabaseFolder getFolder(String folderName) throws GSException {

	checkName(folderName);
	folderName = normalizeName(folderName);

	try {

	    if (exists_(folderName)) {
		return createFolder(folderName);
	    }

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_GET_FOLDER_ERROR", ex);
	}

	return null;
    }

    @Override
    public Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException {

	checkName(folderName);
	String normalizefolderName = normalizeName(folderName);

	try {

	    if (exists_(normalizefolderName)) {
		return Optional.ofNullable(createFolder(normalizefolderName));
	    }

	    if (!createIfNotExist) {
		return Optional.empty();
	    }

	    boolean created = addFolder(folderName);

	    if (created) {

		return Optional.ofNullable(getFolder(folderName));
	    }

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_GET_FOLDER_ERROR", ex);
	}

	return Optional.empty();
    }

    @Override
    public boolean addFolder(String folderName) throws GSException {

	try {
	    if (existsFolder(folderName)) {
		return false;
	    }

	    checkName(folderName);
	    folderName = normalizeName(folderName);

	    String xquery = "xdmp:directory-create('" + folderName + "');";

	    ResultSequence ret = execXQuery(xquery);
	    ret.close();

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_ADD_FOLDER_ERROR", ex);
	}

	return true;
    }

    @Override
    public boolean existsFolder(String folderName) throws GSException {

	checkName(folderName);
	folderName = normalizeName(folderName);

	try {

	    return exists_(folderName);

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_EXISTS_FOLDER_ERROR", ex);
	}
    }

    /**
     * @param folderName
     * @return
     * @throws RequestException
     */
    @Override
    public boolean removeFolder(String folderName) throws GSException {

	String simpleName = folderName;

	if (!existsFolder(folderName)) {

	    GSLoggerFactory.getLogger(getClass()).debug("Folder {} do not exists, unable to remove");

	    return false;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Removing folder {} STARTED", simpleName);

	checkName(folderName);
	folderName = normalizeName(folderName);

	try {
	    getFolder(simpleName).clear();
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	ResultSequence ret;
	try {
	    ret = execXQuery("xdmp:directory-delete('" + folderName + "');");

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_REMOVE_FOLDER_ERROR", ex);
	}

	ret.close();

	GSLoggerFactory.getLogger(getClass()).debug("Removing folder {} ENDED", simpleName);

	return true;
    }

    @Override
    public MarkLogicFolder[] getFolders() throws GSException {

	ResultSequence ret = null;
	try {
	    ret = execXQuery(" for $x in xdmp:directory-properties('/','1')[not(normalize-space())] return base-uri($x)");
	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MARKLOGIC_GET_FOLDERS_ERROR", ex);
	}

	String[] results = ret.asStrings();
	ret.close();

	ArrayList<MarkLogicFolder> out = new ArrayList<>();
	for (int i = 0; i < results.length; i++) {

	    String name = results[i];
	    if (name.startsWith("/" + getIdentifier()) || getIdentifier().equals("ROOT")) {
		out.add(createFolder(results[i]));
	    }
	}

	return out.toArray(new MarkLogicFolder[] {});
    }

    @Override
    public DatabaseFolder findWritingFolder(SourceStorageWorker worker) throws GSException {

	DatabaseFolder folder = worker.getWritingFolder(Optional.empty());

	if (folder == null) {

	    if (worker.existsData1Folder() && worker.existsData2Folder()) {

		throw GSException.createException(//
			getClass(), //
			"Both data-1 and data-2 folders exist", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			"MarkLogicBothDataFoldersExistError" //
		);
	    }

	    if (worker.existsData1Folder()) {

		return worker.getData1Folder();
	    }

	    if (worker.existsData2Folder()) {

		return worker.getData2Folder();
	    }

	    throw GSException.createException(//
		    getClass(), //
		    "No data folder found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "MarkLogicNODataFoldersExistError" //
	    );
	}

	return folder;
    }

    public MarkLogicFolder[] getDataFolders() throws GSException {

	MarkLogicFolder[] folders = getFolders();
	ArrayList<MarkLogicFolder> out = new ArrayList<>();
	for (int i = 0; i < folders.length; i++) {
	    MarkLogicFolder folder = folders[i];
	    String uri = folder.getURI();
	    if (!uri.endsWith(SourceStorageWorker.META_PREFIX + "/")) {
		out.add(folder);
	    }
	}
	return out.toArray(new MarkLogicFolder[] {});
    }

    public MarkLogicFolder[] getMetaFolders() throws GSException {

	MarkLogicFolder[] folders = getFolders();
	ArrayList<MarkLogicFolder> out = new ArrayList<>();
	for (int i = 0; i < folders.length; i++) {
	    MarkLogicFolder folder = folders[i];
	    String uri = folder.getURI();
	    if (uri.endsWith(SourceStorageWorker.META_PREFIX + "/")) {
		out.add(folder);
	    }
	}
	return out.toArray(new MarkLogicFolder[] {});
    }

    public MarkLogicFolder[] getProtectedFolders() throws GSException {

	MarkLogicFolder[] folders = getFolders();
	ArrayList<MarkLogicFolder> out = new ArrayList<>();
	for (int i = 0; i < folders.length; i++) {
	    MarkLogicFolder folder = folders[i];
	    String uri = folder.getURI();
	    if (getProtectedFoldersNames().stream().anyMatch(n -> uri.startsWith("/" + n))) {
		out.add(folder);
	    }
	}
	return out.toArray(new MarkLogicFolder[] {});
    }

    public static List<String> getProtectedFoldersNames() {

	return Arrays.asList(//
		GS_MODULES_PROTECTED_FOLDER, //
		RUNTIME_INFO_PROTECTED_FOLDER, //
		USERS_FOLDER, //
		VIEWS_FOLDER, //
		AUGMENTERS_FOLDER, //
		RegisteredQueriesManager.REGISTERED_QUERIES_PROTECTED_FOLDER);
    }

    public int getFoldersCount() throws GSException {

	return getFolders().length;
    }

    /**
     * This method removes all folders, included the protected folders if and only if this is the "ROOT" database,
     * otherwise protected
     * folders are not removed like a calling of {@link #removeFolders()} (protected folders own to the "ROOT" database)
     *
     * @throws Exception
     */
    public void removeAllFolders() throws Exception {

	DatabaseFolder[] folders = getFolders();
	for (DatabaseFolder folder : folders) {

	    folder.clear();

	    String name = folder.getName();
	    removeFolder(name);
	}
    }

    /**
     * This method removes all folders except the protected folders
     *
     * @throws Exception
     */
    public void removeFolders() throws Exception {

	MarkLogicFolder[] folders = getFolders();
	for (MarkLogicFolder folder : folders) {

	    boolean noneMatch = getProtectedFoldersNames().//
		    stream().//
		    noneMatch(n -> folder.getURI().contains(n));

	    if (noneMatch) {

		folder.clear();

		String name = folder.getName();
		removeFolder(name);
	    }
	}
    }

    /**
     * @param xQuery
     * @return
     * @throws RequestException
     */
    public ResultSequence execXQuery(String xQuery) throws RequestException {

	return wrapper.submit(xQuery);
    }

    /**
     * @param xQuery
     * @param requestID
     * @return
     * @throws RequestException
     */
    public ResultSequence execXQuery(String xQuery, String requestID) throws RequestException {

	return wrapper.submit(xQuery, requestID);
    }

    /**
     * @param folderName
     * @param excludDeleted
     * @return
     * @throws RequestException
     */
    @Override
    public List<String> getIdentifiers(IdentifierType type, String folderName, boolean excludDeleted) throws GSException {

	String dirURI = normalizeName(folderName);

	String query = "xquery version \"1.0-ml\";\n";
	query += "declare namespace " + NameSpace.GI_SUITE_DATA_MODEL.getPrefix() + " = \"" + NameSpace.GI_SUITE_DATA_MODEL.getURI()
		+ "\";\n";

	String dirQuery = "cts:directory-query('" + dirURI + "','infinity')";
	String noDelQuery = "cts:not-query(cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'!=','',(\"score-function=linear\"),0.0))";
	String andQuery = "cts:and-query((" + dirQuery + "," + noDelQuery + "))";
	String constraint = excludDeleted ? andQuery : dirQuery;

	Queryable queryable = null;
	switch (type) {
	case OAI_HEADER:
	    queryable = ResourceProperty.OAI_PMH_HEADER_ID;
	    break;
	case ORIGINAL:
	    queryable = ResourceProperty.ORIGINAL_ID;
	    break;
	case PRIVATE:
	    queryable = ResourceProperty.PRIVATE_ID;
	    break;
	case PUBLIC:
	    queryable = MetadataElement.IDENTIFIER;
	    break;
	}

	query += "cts:element-values(\n" + "fn:QName(\"" + NameSpace.GI_SUITE_DATA_MODEL.getURI() + "\",\"" + queryable.getName() + "\"),\n"
		+ "(),(),\n" + constraint + ")";

	ResultSequence rs = null;

	try {
	    rs = execXQuery(query);

	} catch (RequestException ex) {

	    throw GSException.createException(getClass(), "MarkLogicGetIdentifiersError", ex);
	}

	ArrayList<String> out = new ArrayList<>();
	out.addAll(Arrays.asList(rs.asStrings()));
	return out;
    }

    /**
     * @return
     */
    public MarkLogicWrapper getWrapper() {

	return wrapper;
    }

    /**
     * @return
     */
    public RegisteredQueriesManager getRegisteredQueriesManager() {

	return regQueriesManager;
    }

    protected MarkLogicFolder createFolder(String dir) {

	return new MarkLogicFolder(this, dir);
    }

    private String applyPagination(String ctsSearch, int start, int count) {

	//
	// expected min value of start is 1, not 0!
	//
	ctsSearch = XQueryBuilder.subsequence(ctsSearch, start, count);

	return ctsSearch;
    }

    protected String includePreamble(String ctsQuery) {

	String out = "xquery version \"1.0-ml\";\n";
	out += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/" + GS_MODULES_PROTECTED_FOLDER + "/"
		+ FUNCTIONS_MODULE_NAME + "\";\n";
	// out += "declare namespace " + NameSpace.GI_SUITE_DATA_MODEL.getPrefix() + " = \"" +
	// NameSpace.GI_SUITE_DATA_MODEL.getURI()
	// + "\";\n";
	out += xdmpQueryTrace() + ",\n";
	out += ctsQuery + "\n";
	out += ",xdmp:query-trace(false());";

	return out;
    }

    private boolean exists_(String resourceName) throws RequestException {

	ResultSequence result = execXQuery("exists(xdmp:document-properties('" + resourceName + "'));");
	boolean ret = result.asString().toLowerCase().contains("true");
	result.close();
	return ret;
    }

    String normalizeName(String name) {

	if (!getIdentifier().equals("ROOT") && !name.contains(getIdentifier())) {
	    return "/" + getIdentifier() + "_" + name + "/";
	}

	return "/" + name + "/";
    }

    /**
     * @return
     */
    String xdmpQueryTrace() {

	return QUERY_TRACE ? "xdmp:query-trace(true())" : "xdmp:query-trace(false())";
    }

    private void checkName(String name) throws IllegalArgumentException {

	if (name == null)
	    throw new IllegalArgumentException("Argument cannot be null");

	if (name.startsWith("/") || name.contains("\\") || name.endsWith("/")) {
	    throw new IllegalArgumentException("Argument cannot start with or end with slashes and it can not contain back slashes");
	}
    }

    @Override
    public String getType() {

	return "MarkLogicDatabase";
    }
}
