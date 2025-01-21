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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.marklogic.search.DistinctQueryHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.jaxb.ViewFactory;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicReader implements DatabaseReader {

    //
    // ERROR MESSAGES
    //
    private static final String MARK_LOGIC_DISCOVER_RESULTS_JAXB_ERROR = "MARK_LOGIC_DISCOVER_RESULTS_JAXB_ERROR";
    private static final String MARK_LOGIC_GET_RESOURCE_ERROR = "MARK_LOGIC_GET_RESOURCE_ERROR";
    private static final String MARK_LOGIC_LIST_KEYS_ERROR = "MARK_LOGIC_LIST_KEYS_ERROR";
    private static final String MARK_LOGIC_GET_USER_ERROR = "MARK_LOGIC_GET_USER_ERROR";
    private static final String MARK_LOGIC_GET_USERS_ERROR = "MARK_LOGIC_GET_USERS_ERROR";
    private static final String MARK_LOGIC_RESOURCE_EXISTS_ERROR = "MARK_LOGIC_RESOURCE_EXISTS_ERROR";
    private static final String MARK_LOGIC_GET_FOLDER_ERROR = "MARK_LOGIC_GET_FOLDER_ERROR";

    //
    // LOGS
    //
    private static final String MARKLOGIC_READER_COUNT_STARTED = "Marklogic Reader Count STARTED";
    private static final String MARKLOGIC_READER_COUNT_COMPLETED = "Marklogic Reader Count ENDED";
    private static final String MARKLOGIC_READER_DISCOVER_STARTED = "Marklogic Reader Discover STARTED";
    private static final String MARKLOGIC_READER_DISCOVER_COMPLETED = "Marklogic Reader Discover ENDED";
    private static final String MARKLOGIC_READER_STATS_COMPUTING_STARTED = "Marklogic Reader Stats computing STARTED";
    private static final String MARKLOGIC_READER_STATS_COMPUTING_ENDED = "Marklogic Reader Stats computing ENDED";

    private MarkLogicDatabase markLogicDB;
    private StorageInfo dbUri;
    private DatabaseFolder viewFolder;

    private HashMap<String, String> viewsByCreatorMap;

    public MarkLogicReader() {

	viewsByCreatorMap = new HashMap<>();
    }

    @Override
    public void setDatabase(Database dataBase) {

	this.markLogicDB = (MarkLogicDatabase) dataBase;
    }

    @Override
    public MarkLogicDatabase getDatabase() {

	return this.markLogicDB;
    }

    @Override
    public boolean supports(StorageInfo dbUri) {

	if (MarkLogicDatabase.isSupported(dbUri)) {

	    this.dbUri = dbUri;
	    return true;
	}

	return false;
    }

    @Override
    public boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException {

	String index = null;
	switch (identifierType) {
	case ORIGINAL:
	    index = ResourceProperty.ORIGINAL_ID.getName();
	    break;
	case PRIVATE:
	    return !getResources(identifierType, identifier).isEmpty();

	case PUBLIC:
	    index = MetadataElement.IDENTIFIER.getName();
	    break;
	}

	String xQuery = "xquery version \"1.0-ml\";\n";
	xQuery += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";
	xQuery += "xdmp:estimate(cts:search(doc(), cts:element-range-query(fn:QName('" + NameSpace.GS_DATA_MODEL_SCHEMA_URI + "','";
	xQuery += index + "')";
	xQuery += ",'=','" + identifier + "',(\"score-function=linear\"),0.0),(\"unfiltered\",\"score-simple\"),0))";

	try {
	    int result = Integer.valueOf(markLogicDB.execXQuery(xQuery).asString());
	    return result > 0;

	} catch (RequestException e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_RESOURCE_EXISTS_ERROR, //
		    e);
	}
    }

    @Override
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {

	return getResource(originalIdentifier, source) != null;
    }

    @Override
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException {

	List<GSResource> out = new ArrayList<GSResource>();

	try {

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());
	    message.setIncludeDeleted(true);
	    //
	    // disables the time stamp check of the source id query. in this moment
	    // the harvesting time stamp is not yet updated, so only the old resource has a timestamp less
	    // then the harvesting time stamp. if the check is enabled, the new resource is not inserted
	    // in the result set
	    //
	    message.disableDataFolderCheck();

	    Bond bond = null;

	    switch (identifierType) {
	    case PRIVATE:
		//
		// privateId is not indexed
		//
		DatabaseFolder[] folders = markLogicDB.getDataFolders();
		for (DatabaseFolder folder : folders) {

		    Node node = folder.get(identifier);
		    if (Objects.nonNull(node)) {
			return Arrays.asList(GSResource.create(node));
		    }
		}
		return out;
	    case ORIGINAL:
		bond = BondFactory.createOriginalIdentifierBond(identifier);
		break;
	    case PUBLIC:
		bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifier);
		break;
	    case OAI_HEADER:
		bond = BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.OAI_PMH_HEADER_ID, identifier);
		break;
	    }

	    message.setPermittedBond(bond);
	    message.setNormalizedBond(bond);
	    message.setUserBond(bond);

	    int count = count(message).getCount();

	    if (count > 0) {
		message.setPage(new Page(1, count));

		ResultSet<GSResource> resultSet = discover(message);
		out = resultSet.getResultsList();
	    }
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_RESOURCE_ERROR, //
		    e);
	}

	return out;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException {

	return getResource(originalIdentifier, source, false);
    }

    /**
     * @param originalIdentifier
     * @param source
     * @param includeDeleted
     * @return
     * @throws GSException
     */
    @Override
    public GSResource getResource(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {

	List<GSResource> resultsList = getResources(originalIdentifier, source, includeDeleted);
	if (!resultsList.isEmpty()) {

	    if (resultsList.size() > 1) {
		//
		// this should no longer happen from GIP-423
		//
		GSLoggerFactory.getLogger(MarkLogicReader.class).warn("Found {} resources with originalId [{}] from the source [{}] !!!",
			resultsList.size(), originalIdentifier, source.getUniqueIdentifier());
	    }

	    return resultsList.get(0);
	}

	return null;
    }

    /**
     * Special method used during the tag recovering phase. Normally different resources with same original id are not
     * allowed, but at the
     * end of a non-first harvesting, it is common to have 2 copies of the same resource, from the previous harvesting
     * and from the current
     * one
     */
    @Override
    public List<GSResource> getResources(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());
	message.setIncludeDeleted(includeDeleted);
	//
	// disables the time stamp check of the source id query. in this moment
	// the harvesting time stamp is not yet updated, so only the old resource has a timestamp less
	// then the harvesting time stamp. if the check is enabled, the new resource is not inserted
	// in the result set
	//
	message.disableDataFolderCheck();

	ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier());
	ResourcePropertyBond identifierBond = BondFactory.createOriginalIdentifierBond(originalIdentifier);
	LogicalBond andBond = BondFactory.createAndBond(sourceBond, identifierBond);

	message.setPermittedBond(andBond);
	message.setNormalizedBond(andBond);
	message.setUserBond(andBond);
	// using a page count set to 10, in order to check whether there are more than
	// one resource with the same original identifier for the supplied source
	// in that case a warning is logged, maybe an exception is more appropriate?
	//
	// this should no longer happen from GIP-423
	//
	message.setPage(new Page(10));

	ResultSet<GSResource> resultSet = discover(message);
	GSException exception = resultSet.getException();
	if (!exception.getErrorInfoList().isEmpty()) {
	    throw exception;
	}

	return resultSet.getResultsList();
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_COUNT_STARTED);
	// String qs = message.getWebRequest().getQueryString();
	Optional<WebRequest> oqs = Optional.ofNullable(message.getWebRequest());
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_COUNTQUERY_GENERATION,
		message.getRequestId(), oqs);

	MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(message, markLogicDB);
	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());
	bondParser.parse(handler);

	Optional<Queryable> optionalElement = message.getDistinctValuesElement();

	String ctsSearch = null;

	if (optionalElement.isPresent()) {

	    ctsSearch = DistinctQueryHandler.handleCount(message, handler);

	} else {

	    ctsSearch = handler.getCTSSearch(true);
	}

	pl.logPerformance(GSLoggerFactory.getLogger(MarkLogicReader.class));

	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_COUNTQUERY_EXECUTION, message.getRequestId(), oqs);

	DiscoveryCountResponse estimate = markLogicDB.estimate(ctsSearch, message.isQueryRegistrationEnabled(), message.getRequestId());

	pl.logPerformance(GSLoggerFactory.getLogger(MarkLogicReader.class));
	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_COUNT_COMPLETED);

	return estimate;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	ResultSet<Node> nodes = discoverNodes(message);

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Creation of {} GSResources STARTED",
	// nodes.getResultsList().size());

	Optional<WebRequest> oqs = Optional.ofNullable(message.getWebRequest());

	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MARKLOGIC_NODES_TO_GS_RESOURCE_MAPPING,
		message.getRequestId(), oqs);

	List<GSResource> resources = new ArrayList<>();

	for (Node node : nodes.getResultsList()) {
	    try {
		GSResource res = GSResource.create(node);
		resources.add(res);
	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(MarkLogicReader.class).error("JAXB Exception reading discovery query results", e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			MARK_LOGIC_DISCOVER_RESULTS_JAXB_ERROR, //
			e);
	    }
	}

	pl.logPerformance(GSLoggerFactory.getLogger(MarkLogicReader.class));
	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Creation of {} GSResources ENDED",
	// nodes.getResultsList().size());

	ResultSet<GSResource> resultSet = new ResultSet<>();
	resultSet.setResultsList(resources);

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_STARTED);

	Page page = message.getPage();

	int start = page.getStart();
	int count = page.getSize();

	List<Node> nodes = markLogicDB.search(message, start, count, Node.class);

	ResultSet<Node> resultSet = new ResultSet<>();
	resultSet.setResultsList(nodes);

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_STARTED);

	Page page = message.getPage();

	int start = page.getStart();
	int count = page.getSize();

	List<String> nodes = markLogicDB.search(message, start, count, String.class);

	ResultSet<String> resultSet = new ResultSet<>();
	resultSet.setResultsList(nodes);

	// GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	try {

	    // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting users STARTED");

	    String suiteId = getDatabase().getIdentifier();

	    String query = "cts:search(doc(),cts:directory-query('/" + suiteId + "_" + MarkLogicDatabase.USERS_FOLDER
		    + "/'),('unfiltered','score-simple'),0)";

	    List<GSUser> users = StreamUtils.iteratorToStream(getDatabase().execXQuery(query).iterator()).//
		    map(item -> {
			try {
			    return GSUser.create(item.asInputStream());
			} catch (JAXBException e) {
			    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
			    e.printStackTrace();
			}
			return null;
		    }).filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting users ENDED");
	    // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Found {} users", users.size());

	    return users;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting users: {}", e.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_USERS_ERROR, //
		    e);
	}
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {

	try {

	    if (viewFolder == null) {

		viewFolder = getDatabase().getViewFolder(false);

		if (viewFolder == null) {

		    GSLoggerFactory.getLogger(getClass()).warn("View folder missing");

		    return Optional.empty();
		}
	    }

	    String viewQuery = getViewQuery(viewFolder, viewId);

	    MarkLogicWrapper wrapper = getDatabase().getWrapper();

	    ResultSequence resultSequence = wrapper.submit(viewQuery);

	    if (resultSequence.hasNext()) {

		ViewFactory factory = new ViewFactory();

		Unmarshaller unmarshaller = factory.createUnmarshaller();

		InputStream stream = resultSequence.next().asInputStream();

		View view = (View) unmarshaller.unmarshal(stream);

		// GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Unmarshalling view {} ENDED", id);

		return Optional.of(view);

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("View {} not found", viewId);

		return Optional.empty();
	    }

	    // ViewFactory factory = new ViewFactory();
	    //
	    // Unmarshaller unmarshaller = factory.createUnmarshaller();
	    //
	    // if (viewFolder.exists(id)) {
	    //
	    // // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting view {} STARTED", id);
	    //
	    // InputStream binary = viewFolder.getBinary(id);
	    //
	    // // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting view {} ENDED", id);
	    //
	    // // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Unmarshalling view {} STARTED", id);
	    //
	    // View view = (View) unmarshaller.unmarshal(binary);
	    //
	    // // GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Unmarshalling view {} ENDED", id);
	    //
	    // return Optional.of(view);
	    // }
	    //
	    // return Optional.empty();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Exception requesting view: {}", e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_RESOURCE_ERROR, //
		    e);
	}
    }

    /**
     * @param viewId
     * @return
     */
    private String getViewQuery(DatabaseFolder viewFolder, String viewId) {

	String query = "xquery version \"1.0-ml\"; \n";
	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $id := '" + viewId + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris) \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where (fn:contains($xml,fn:concat('<id>',$id,'</id>')) or $doc//id = $id) return $xml";

	return query;
    }

    @Override
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException {

	try {

	    DatabaseFolder folder = getDatabase().getViewFolder(false);

	    if (folder == null) {

		return new ArrayList<>();
	    }

	    String[] ret = folder.listKeys();

	    List<String> idsList = new ArrayList<>(Arrays.asList(ret));

	    if (request.getCreator().isPresent() || request.getOwner().isPresent()) {

		if (viewFolder == null) {

		    GSLoggerFactory.getLogger(getClass()).info("Get view folder STARTED");

		    viewFolder = getDatabase().getViewFolder(false);

		    GSLoggerFactory.getLogger(getClass()).info("Get view folder ENDED");

		    if (viewFolder == null) {

			GSLoggerFactory.getLogger(getClass()).warn("View folder missing");

			return new ArrayList<>();
		    }
		}

		String viewQuery = ListViewIdsQueryHandler.getListViewIdsQuery(folder, request);

		MarkLogicWrapper wrapper = getDatabase().getWrapper();

		ResultSequence resultSequence = wrapper.submit(viewQuery);

		idsList = Arrays.asList(resultSequence.asStrings());
	    }

	    int fromIndex = Math.min(idsList.size(), request.getStart());
	    int toIndex = Math.min(idsList.size(), request.getStart() + request.getCount());

	    idsList = idsList.subList(fromIndex, toIndex);

	    return idsList;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting views from {} to {}", request.getStart(),
		    request.getCount());

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_LIST_KEYS_ERROR, //
		    e);
	}

    }

    public static void main(String[] args) {

	String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	s += "<view>";
	s += "<viewBond>";
	s += "   <viewIdentifier>view3</viewIdentifier>";
	s += "</viewBond>";
	s += "<creationTime>2022-10-13T11:42:46.485+02:00</creationTime>";
	s += " <creator>creator1</creator>";
	s += " <id>id3</id>";
	s += "</view>";

	s += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	s += "<view>";
	s += "<viewBond>";
	s += "  <viewIdentifier>view4</viewIdentifier>";
	s += "</viewBond>";
	s += "<creationTime>2022-10-13T11:42:46.580+02:00</creationTime>";
	s += "<creator>creator1</creator>";
	s += "<id>id4</id>";
	s += "</view>";

	Pattern pattern = Pattern.compile("<id>(.*?)</id>");
	Matcher matcher = pattern.matcher(s);
	while (matcher.find()) {
	    System.out.println(matcher.group(1));
	}
    }

}
