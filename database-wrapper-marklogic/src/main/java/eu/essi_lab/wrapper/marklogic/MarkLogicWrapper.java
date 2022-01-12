package eu.essi_lab.wrapper.marklogic;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.Transaction;
import com.marklogic.client.document.BinaryDocumentManager;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.semantics.SPARQLQueryManager;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.ItemType;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmNode;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
public class MarkLogicWrapper {

    private static final String ESSI_LAST_UPDATE_ELEMNAME = "essilastupdate";
    private static final String ESSI_LAST_UPDATE_START = "<" + ESSI_LAST_UPDATE_ELEMNAME + ">";
    private static final String ESSI_LAST_UPDATE_END = "</" + ESSI_LAST_UPDATE_ELEMNAME + ">";

    //
    // 1 minute of timeout
    //
    private static final int DEFAULT_TIMEOUT = 60;

    private ContentSource contentSource;
    private DatabaseClient databaseClient;
    private XMLDocumentManager xmlDocManager;
    private BinaryDocumentManager binaryDocManager;
    private SPARQLQueryManager sparqlQueryManager;
    private String dataBaseName;
    private String user;
    private String password;

    private URI restURI;
    private GenericDocumentManager genericDocManager;
    private JSONDocumentManager jsonDocumentManager;

    /**
     * @param uri
     * @param dataBaseName
     * @throws URISyntaxException
     * @throws XccConfigException
     */
    public MarkLogicWrapper(String uri, String dataBaseName) throws URISyntaxException, XccConfigException {
	this(uri, null, null, dataBaseName);
    }

    /**
     * @param uri
     * @param user
     * @param password
     * @param dataBaseName
     * @throws URISyntaxException
     * @throws XccConfigException
     */
    public MarkLogicWrapper(String uri, String user, String password, String dataBaseName) throws URISyntaxException, XccConfigException {

	init(uri, user, password, dataBaseName);
    }

    /**
     * @param uri
     * @param user
     * @param password
     * @param dataBaseName
     * @throws URISyntaxException
     * @throws XccConfigException
     */
    void init(String uri, String user, String password, String dataBaseName) throws URISyntaxException, XccConfigException {

	this.user = user;
	this.password = password;
	this.dataBaseName = dataBaseName;

	String restUri = null;
	String xdbcUri = null;

	if (user != null && password != null && !user.equals("") && !password.equals("")) {

	    //
	    // user and password provided as arguments (e.g: uri = xdbc://db-host-name:8000,8004)
	    //
	    String restHost = uri.substring(0, uri.indexOf(","));
	    String xdbcHost = uri.substring(0, uri.lastIndexOf(":") + 1) + uri.replace(restHost + ",", "");

	    restUri = restHost.replace("xdbc://", "xdbc://" + user + ":" + password + "@");
	    xdbcUri = xdbcHost.replace("xdbc://", "xdbc://" + user + ":" + password + "@");

	} else {

	    //
	    // user and password provided in the URI (e.g: xdbc://USER:PASSWORD@db-host-name:8000,8004)
	    //
	    restUri = uri.substring(0, uri.lastIndexOf(","));
	    xdbcUri = uri.substring(0, uri.lastIndexOf(":") + 1) + uri.substring(uri.lastIndexOf(",") + 1, uri.length());
	}

	restURI = new URI(restUri);
	URI contentSourceURI = new URI(xdbcUri);

	String userInfo = restURI.getUserInfo();

	if (user == null) {

	    this.user = userInfo.substring(0, userInfo.indexOf(":"));
	}

	if (password == null) {

	    this.password = userInfo.substring(userInfo.indexOf(":") + 1, userInfo.length());
	}

	GSLoggerFactory.getLogger(getClass()).info("DB URI: {}", uri);
	GSLoggerFactory.getLogger(getClass()).info("User: {}", this.user);
	GSLoggerFactory.getLogger(getClass()).info("Password: {}", this.password);
	GSLoggerFactory.getLogger(getClass()).info("DB name: {}", this.dataBaseName);
	GSLoggerFactory.getLogger(getClass()).info("Rest URI: {}", restUri);
	GSLoggerFactory.getLogger(getClass()).info("XDBC URI: {}", xdbcUri);

	if (dataBaseName == null) {
	    contentSource = ContentSourceFactory.newContentSource(new URI(xdbcUri));
	} else {
	    contentSource = ContentSourceFactory.newContentSource(//
		    contentSourceURI.getHost(), //
		    contentSourceURI.getPort(), //
		    contentSourceURI.getUserInfo().split(":")[0], //
		    contentSourceURI.getUserInfo().split(":")[1], //
		    dataBaseName //
	    );//
	}

	databaseClient = DatabaseClientFactory.newClient(//
		restURI.getHost(), //
		restURI.getPort(), //
		dataBaseName, //
		new DigestAuthContext(this.user, this.password));

	xmlDocManager = databaseClient.newXMLDocumentManager();
	jsonDocumentManager = databaseClient.newJSONDocumentManager();
	binaryDocManager = databaseClient.newBinaryDocumentManager();
	genericDocManager = databaseClient.newDocumentManager();
	sparqlQueryManager = databaseClient.newSPARQLQueryManager();
    }

    /**
     * @param timeout transaction timeout in seconds
     * @return
     */
    public Transaction openTransaction(int timeout) {

	return databaseClient.openTransaction(ISO8601DateTimeUtils.getISO8601DateTime(), timeout);
    }

    /**
     * @return
     */
    public Transaction openTransaction() {
	try {
	    Transaction ret = databaseClient.openTransaction(ISO8601DateTimeUtils.getISO8601DateTime(), DEFAULT_TIMEOUT);
	    return ret;
	} catch (Throwable e) {
	    GSLoggerFactory.getLogger(getClass()).info("Catched exception");
	    e.printStackTrace();
	    throw e;
	}
    }

    /**
     * @return
     */
    public DatabaseClient getClient() {

	return databaseClient;
    }

    /**
     * @return
     */
    public XMLDocumentManager getXMLDocumentManager() {

	return xmlDocManager;
    }

    /**
     * @return
     */
    public JSONDocumentManager getJSONDocumentManager() {

	return jsonDocumentManager;
    }

    /**
     * @return
     */
    public BinaryDocumentManager getBinaryDocumentManager() {

	return binaryDocManager;
    }

    /**
     * @return
     */
    public GenericDocumentManager getGenericDocumentManager() {

	return genericDocManager;
    }

    /**
     * @return
     */
    public SPARQLQueryManager getSPARQLQueryManager() {

	return sparqlQueryManager;
    }

    /**
     * @return
     */
    public ContentSource getContentSource() {

	return contentSource;
    }

    /**
     * @param xquery
     * @return
     * @throws RequestException
     */
    public ResultSequence submit(String xquery) throws RequestException {

	Session session = createNewSession();

	AdhocQuery request = session.newAdhocQuery(xquery);

	ResultSequence result = session.submitRequest(request);

	session.close();

	return result;
    }

    /**
     * @param uri
     * @param doc
     * @return
     * @throws Exception
     */
    public boolean store(String uri, Document doc) throws Exception {

	Transaction transaction = openTransaction();
	XMLDocumentManager docManager = getXMLDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists == null) { // if the doc no not exists

		DOMHandle domHandle = new DOMHandle(doc);

		docManager.write(uri, domHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param doc
     * @return
     * @throws Exception
     */
    public boolean storeJSON(String uri, JSONObject object) throws Exception {

	Transaction transaction = openTransaction();
	JSONDocumentManager jsonManager = getJSONDocumentManager();

	try {

	    DocumentDescriptor exists = jsonManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists == null) { // if the doc no not exists

		JSONWriteHandle domHandle = new StringHandle(object.toString(3));

		jsonManager.write(uri, domHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param doc
     * @return
     * @throws Exception
     */
    public boolean replaceJSON(String uri, JSONObject object) throws Exception {

	Transaction transaction = openTransaction();
	JSONDocumentManager jsonManager = getJSONDocumentManager();

	try {

	    DocumentDescriptor exists = jsonManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists != null) { // if the doc exists

		JSONWriteHandle domHandle = new StringHandle(object.toString(3));

		jsonManager.write(uri, domHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param doc
     * @return
     * @throws Exception
     */
    public boolean replace(String uri, Document doc) throws Exception {

	Transaction transaction = openTransaction();
	XMLDocumentManager docManager = getXMLDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists != null) { // if the doc exists

		DOMHandle domHandle = new DOMHandle(doc);

		docManager.write(uri, domHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param res
     * @throws Exception
     */
    public boolean storeBinary(String uri, InputStream res) throws Exception {

	return storeBinary(uri, res, null);
    }

    /**
     * @param uri
     * @param res
     * @param modificationDate
     * @return
     * @throws Exception
     */
    public boolean storeBinary(String uri, InputStream res, Date modificationDate) throws Exception {

	Transaction transaction = openTransaction();
	BinaryDocumentManager docManager = getBinaryDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists == null) { // if the doc no not exists

		InputStreamHandle binaryHandle = new InputStreamHandle(res);

		docManager.write(uri, binaryHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    if (out && modificationDate != null) {

		String lastUpdateXQuery = createLastUpdateXQuery(uri, modificationDate);
		submit(lastUpdateXQuery);
	    }

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param res
     * @return
     */
    public boolean replaceBinary(String uri, InputStream res) {

	Transaction transaction = openTransaction();
	BinaryDocumentManager docManager = getBinaryDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists != null) { // if the doc exists

		InputStreamHandle binaryHandle = new InputStreamHandle(res);

		docManager.write(uri, binaryHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws RequestException
     */
    public Node get(String uri) throws ParserConfigurationException, IOException, SAXException, RequestException {

	Transaction transaction = openTransaction();
	XMLDocumentManager docManager = getXMLDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    Node out = null;

	    if (exists != null) { // if the doc exists

		DocumentPage page = docManager.read(transaction, uri);

		DOMHandle handle = page.next().getContent(new DOMHandle());

		out = handle.get();
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param uri
     * @return
     * @throws Exception
     */
    public InputStream getBinary(String uri) throws Exception {

	Transaction transaction = openTransaction();
	GenericDocumentManager docManager = getGenericDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    InputStream out = null;

	    if (exists != null) { // if the doc exists

		DocumentPage page = docManager.read(transaction, uri);

		InputStreamHandle handle = page.next().getContent(new InputStreamHandle());

		out = handle.get();
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * See GIP-244 and test eu.essi_lab.api.database.marklogic.test.GIP_244_Test
     * 
     * @param uri
     * @return
     * @throws Exception
     */
    public InputStream getNonTransactionalBinary(String uri) throws Exception {

	GenericDocumentManager docManager = getGenericDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri);
	    InputStream out = null;

	    if (exists != null) { // if the doc exists

		DocumentPage page = docManager.read(uri);

		InputStreamHandle handle = page.next().getContent(new InputStreamHandle());

		out = handle.get();
	    }

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    throw ex;
	}
    }

    /**
     * @param uri
     * @param essiLastUpdate
     * @return
     */
    private String createLastUpdateXQuery(String uri, Date essiLastUpdate) {

	String isoDate = ISO8601DateTimeUtils.getISO8601DateTime(essiLastUpdate);

	StringBuilder builder = new StringBuilder(ESSI_LAST_UPDATE_START);
	builder.append(isoDate);
	builder.append(ESSI_LAST_UPDATE_END);

	return "xdmp:document-set-property('" + uri + "',(" + builder.toString() + "))";
    }

    /**
     * @return
     */
    private Session createNewSession() {

	return contentSource.newSession();
    }

    /**
     * @param nodeURI
     * @return
     * @throws Exception
     */
    public Node getBinaryProperties(String nodeURI) throws Exception {

	ResultSequence rs = submit("xdmp:document-properties('" + nodeURI + "')");

	if (rs.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Result sequence for binary peroperties of {} is empty, returning null", nodeURI);
	    return null;
	}

	ResultItem rsItem = rs.next();

	ItemType itemType = rsItem.getItemType();

	XdmItem item = rsItem.getItem();
	if (itemType == ItemType.BINARY) {

	    GSLoggerFactory.getLogger(getClass()).warn("Item type of binary peroperties of {} is binary, returning null", nodeURI);

	    return null;
	}

	DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
	factory.setNamespaceAware(true);
	DocumentBuilder docBuilder = factory.newDocumentBuilder();

	Node node = ((XdmNode) item).asW3cNode(docBuilder);

	return node;

    }

    /**
     * @param uri
     * @throws Exception
     */
    public boolean remove(String uri) throws Exception {

	Transaction transaction = openTransaction();
	GenericDocumentManager docManager = getGenericDocumentManager();

	try {

	    DocumentDescriptor exists = docManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists != null) { // if the doc exists

		docManager.delete(exists, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }
}
