package eu.essi_lab.api.database.marklogic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.internal.Folder;
import eu.essi_lab.api.database.marklogic.search.DistinctQueryHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.api.database.marklogic.search.semantic.MarkLogicSemanticReader;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
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
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
public class MarkLogicReader extends AbstractGSconfigurable implements DatabaseReader {

    /**
     *
     */
    private static final long serialVersionUID = -7394037640618943119L;

    private Map<String, GSConfOption<?>> supported = new HashMap<>();

    //
    // ERROR MESSAGES
    //
    private static final String MARK_LOGIC_DISCOVER_RESULTS_JAXB_ERROR = "MARK_LOGIC_DISCOVER_RESULTS_JAXB_ERROR";
    private static final String MARK_LOGIC_GET_RESOURCE_ERROR = "MARK_LOGIC_GET_RESOURCE_ERROR";
    private static final String MARK_LOGIC_LIST_KEYS_ERROR = "MARK_LOGIC_LIST_KEYS_ERROR";
    private static final String MARK_LOGIC_GET_USER_ERROR = "MARK_LOGIC_GET_USER_ERROR";
    private static final String MARK_LOGIC_GET_USERS_ERROR = "MARK_LOGIC_GET_USERS_ERROR";
    private static final String MARK_LOGIC_RESOURCE_EXISTS_ERROR = "MARK_LOGIC_RESOURCE_EXISTS_ERROR";

    //
    // LOGS
    //
    private static final String MARKLOGIC_READER_COUNT_STARTED = "Marklogic Reader Count STARTED";
    private static final String MARKLOGIC_READER_COUNT_COMPLETED = "Marklogic Reader Count ENDED";
    private static final String MARKLOGIC_READER_DISCOVER_STARTED = "Marklogic Reader Discover STARTED";
    private static final String MARKLOGIC_READER_DISCOVER_COMPLETED = "Marklogic Reader Discover ENDED";
    private static final String MARKLOGIC_READER_STATS_COMPUTING_STARTED = "Marklogic Reader Stats computing STARTED";
    private static final String MARKLOGIC_READER_STATS_COMPUTING_ENDED = "Marklogic Reader Stats computing ENDED";

    @JsonIgnore
    private transient MarkLogicDatabase markLogicDB;

    @JsonIgnore
    private transient StorageUri dbUri;

    public MarkLogicReader() {
	// empty constructor need by json serializer/deserializer
    }

    @Override
    @JsonIgnore
    public void setDatabase(Database dataBase) {

	this.markLogicDB = (MarkLogicDatabase) dataBase;
    }

    @Override
    @JsonIgnore
    public MarkLogicDatabase getDatabase() {

	return (MarkLogicDatabase) this.markLogicDB;
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
		Folder[] folders = markLogicDB.getDataFolders();
		for (Folder folder : folders) {

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

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	try {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_STATS_COMPUTING_STARTED);

	    StatisticsResponse response = markLogicDB.compute(message);

	    GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_STATS_COMPUTING_ENDED);

	    return response;

	} catch (GSException e) {

	    throw e;
	}
    }

    @Override
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_COUNT_STARTED);
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

	DiscoveryCountResponse estimate = markLogicDB.estimate(ctsSearch, message.isQueryRegistrationEnabled());

	pl.logPerformance(GSLoggerFactory.getLogger(MarkLogicReader.class));
	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_COUNT_COMPLETED);

	return estimate;
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	ResultSet<Node> nodes = discoverNodes(message);

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Creation of {} GSResources STARTED", nodes.getResultsList().size());

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
	GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Creation of {} GSResources ENDED", nodes.getResultsList().size());

	ResultSet<GSResource> resultSet = new ResultSet<>();
	resultSet.setResultsList(resources);

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }

    @Override
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_STARTED);

	Page page = message.getPage();

	int start = page.getStart();
	int count = page.getSize();

	List<Node> nodes = markLogicDB.search(message, start, count, Node.class);

	ResultSet<Node> resultSet = new ResultSet<>();
	resultSet.setResultsList(nodes);

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_STARTED);

	Page page = message.getPage();

	int start = page.getStart();
	int count = page.getSize();

	List<String> nodes = markLogicDB.search(message, start, count,String.class);

	ResultSet<String> resultSet = new ResultSet<>();
	resultSet.setResultsList(nodes);

	GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_DISCOVER_COMPLETED);

	return resultSet;
    }
    
    @Override
    public SemanticCountResponse count(SemanticMessage message) throws GSException {

	MarkLogicSemanticReader semanticReader = new MarkLogicSemanticReader(markLogicDB);
	return semanticReader.count(message);
    }

    @Override
    public SemanticResponse<GSKnowledgeResourceDescription> execute(SemanticMessage message) throws GSException {

	MarkLogicSemanticReader semanticReader = new MarkLogicSemanticReader(markLogicDB);
	return semanticReader.execute(message);
    }

    @Override
    public Optional<GSKnowledgeResourceDescription> getKnowlegdeResource(GSKnowledgeScheme scheme, String subjectid) throws GSException {

	MarkLogicSemanticReader semanticReader = new MarkLogicSemanticReader(markLogicDB);
	return semanticReader.getKnowlegdeResource(scheme, subjectid);
    }

    @Override
    public boolean supports(StorageUri dbUri) {

	if (dbUri != null && dbUri.getUri() != null && dbUri.getUri().startsWith("xdbc")) {
	    this.dbUri = dbUri;
	    return true;
	}

	return false;
    }

    @Override
    @JsonIgnore
    public StorageUri getStorageUri() {

	return dbUri;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	// nothing to do on option set
    }

    @Override
    public void onFlush() throws GSException {
	// nothing to do on flush
    }

 

    @Override
    @JsonIgnore
    public Optional<GSUser> getUser(String identifier) throws GSException {

	try {

	    return getUsers().stream().filter(u -> u.getIdentifier().equals(identifier)).findFirst();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting user {}", identifier);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_USER_ERROR, //
		    e);
	}
    }

    @Override
    @JsonIgnore
    public List<GSUser> getUsers() throws GSException {

	try {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting users STARTED");

	    String suiteId = getDatabase().getSuiteIdentifier();

	    String query = "cts:search(doc(),cts:directory-query('/" + suiteId + "_" + MarkLogicDatabase.USERS_PROTECTED_FOLDER
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

	    GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting users ENDED");
	    GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Found {} users", users.size());

	    return users;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting users");

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
    public Optional<View> getView(String id) throws GSException {

	try {

	    Folder folder = getViewFolder();

	    if (folder == null) {
		return Optional.empty();
	    }

	    ViewFactory factory = new ViewFactory();

	    Unmarshaller unmarshaller = factory.createUnmarshaller();

	    if (folder.exists(id)) {

		GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting view {} STARTED", id);

		InputStream binary = folder.getBinary(id);

		GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Getting view {} ENDED", id);

		GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Unmarshalling view {} STARTED", id);

		View view = (View) unmarshaller.unmarshal(binary);

		GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Unmarshalling view {} ENDED", id);

		return Optional.of(view);
	    }

	    return Optional.empty();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting view {}", id);

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

    @Override
    public List<String> getViewIdentifiers(int start, int count) throws GSException {
	return getViewIdentifiers(start, count, null);
    }

    @Override
    public List<String> getViewIdentifiers(int start, int count, String creator) throws GSException {

	try {

	    Folder folder = getViewFolder();

	    if (folder == null) {
		return new ArrayList<>();
	    }

	    String[] ret = folder.listKeys();

	    List<String> list = new ArrayList<>(Arrays.asList(ret));

	    if (creator != null) {
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
		    String id = (String) it.next();
		    Optional<View> optionalView = getView(id);
		    if (optionalView.isPresent()) {
			View view = optionalView.get();
			if (view.getCreator() == null || !view.getCreator().equals(creator)) {
			    it.remove();
			}
		    } else {
			it.remove();
		    }
		}
	    }

	    int fromIndex = Math.min(list.size(), start);
	    int toIndex = Math.min(list.size(), start + count);
	    list = list.subList(fromIndex, toIndex);
	    return list;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).error("Exception requesting views from {} to {}", start, count);

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

    protected Folder getViewFolder() throws RequestException {

	return getProtectedFolder(MarkLogicDatabase.VIEWS_PROTECTED_FOLDER);
    }

    protected Folder getUsersFolder() throws RequestException {

	return getProtectedFolder(MarkLogicDatabase.USERS_PROTECTED_FOLDER);
    }

    protected Folder getProtectedFolder(String dirURI) throws RequestException {

	MarkLogicDatabase mldb = getDatabase();
	return mldb.getFolder(dirURI);
    }
}
