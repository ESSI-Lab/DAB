/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.marklogic.client.ResourceNotResendableException;
import com.marklogic.client.Transaction;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class RegisteredQueriesManager {

    public static final String REGISTERED_QUERIES_PROTECTED_FOLDER = "reg-queries";

    private static final String MARK_LOGIC_REGISTERED_QUERIES_DOC_INIT_ERROR = "MARK_LOGIC_REGISTERED_QUERIES_DOC_INIT_ERROR";
    private static final String REGISTERED_QUERIES_DOC = "reg-queries-doc.xml";
    public static final String REGISTERED_QUERIES_DOC_URI = "/" + REGISTERED_QUERIES_PROTECTED_FOLDER + "/" + REGISTERED_QUERIES_DOC;

    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 1 week of expiration time
    private static final int MAX_COUNT = 100; // max number of allowed reg.queries

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory() {

	@Override
	public Thread newThread(Runnable r) {

	    Thread thread = new Thread(r);
	    thread.setPriority(Thread.MIN_PRIORITY);
	    thread.setName(thread.getName() + "_REG_QUERIES_MANAGER");

	    return thread;
	}
    });

    //
    // ---
    //
    private static final String TIME_STAMP_ATTRIBUTE = "timeStamp";
    private static final String SCORE_ATTRIBUTE = "score";
    public static final String REG_QUERY_ELEMENT = "registeredQuery";
    public static final String TIME_STAMP_XPATH = "//gs:" + TIME_STAMP_ATTRIBUTE + "/text()";
    private static final String REG_QUERIES_ROOT_XPATH = "//gs:registered-queries";
    public static final String REG_QUERIES_XPATH = "//gs:" + REG_QUERY_ELEMENT + "/text()";

    private MarkLogicDatabase markLogicDB;
    private MarkLogicWrapper wrapper;

    private int maxCount;
    private long expirationTime;

    //
    // even if transactions locks the document at Database level,
    // this synch object avoids at GS-Service instance level, concurrent requests for transactions
    //
    private static final Object LOCK = new Object();

    /**
     * @param markLogicDB
     */
    public RegisteredQueriesManager(MarkLogicDatabase markLogicDB) {

	this.markLogicDB = markLogicDB;
	this.wrapper = this.markLogicDB.getWrapper();

	setMaxCount(MAX_COUNT);
	setExpirationTime(EXPIRATION_TIME);
    }

    /**
     * @return
     */
    public int getMaxCount() {

	return maxCount;
    }

    /**
     * @param maxCount
     */
    public void setMaxCount(int maxCount) {

	this.maxCount = maxCount;
    }

    /**
     * @param time
     */
    public void setExpirationTime(long time) {

	this.expirationTime = time;
    }

    /**
     * @return
     */
    public long getExpirationTime() {

	return this.expirationTime;
    }

    /**
     * @param dbName
     * @throws GSException
     */
    public void init(String dbName) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Registered queries manager init");

	synchronized (LOCK) {

	    GSLoggerFactory.getLogger(getClass()).info("Opening transaction");
	    //
	    // stores the registered queries doc (if not already there)
	    //

	    Transaction transaction = wrapper.openTransaction();

	    GSLoggerFactory.getLogger(getClass()).info("Opened transaction");

	    try {
		DocumentDescriptor descriptor = wrapper.getXMLDocumentManager().exists(//
			REGISTERED_QUERIES_DOC_URI, //
			transaction);
		
		if (descriptor == null ) {

		    GSLoggerFactory.getLogger(getClass()).info("Storing doc STARTED");

		    InputStreamHandle handle = new InputStreamHandle(createDocStream());

		    wrapper.getXMLDocumentManager().write(REGISTERED_QUERIES_DOC_URI, handle, transaction);
		    
		    handle.close();

		    GSLoggerFactory.getLogger(getClass()).info("Storing doc ENDED");

		} else {

		    GSLoggerFactory.getLogger(getClass()).info("Registered queries doc already there");
		}

		transaction.commit();

	    } catch (Exception ex) {

		ex.printStackTrace();

		transaction.rollback();

		throw GSException.createException(//
			getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			MARK_LOGIC_REGISTERED_QUERIES_DOC_INIT_ERROR, //
			ex);
	    }
	}
    }

    /**
     * @param regQuery
     */
    public void update(final String regQuery) {

	Runnable command = new Runnable() {

	    @Override
	    public void run() {

		GSLoggerFactory.getLogger(getClass()).info("Updating registered query");

		synchronized (LOCK) {

		    XMLDocumentManager docManager = wrapper.getXMLDocumentManager();
		    Transaction transaction = wrapper.openTransaction(300); // 5 minutes timeout

		    InputStreamHandle handle = null;
		    InputStreamHandle content = null;
		    InputStream docStream = null;

		    try {

			handle = new InputStreamHandle();

			DocumentPage page = docManager.read(transaction, REGISTERED_QUERIES_DOC_URI);

			content = page.next().getContent(handle);
			docStream = content.get();

			XMLDocumentReader reader = CommonNameSpaceContext.createCommonReader(docStream);

			//
			// first removes and deregisters expired queries (if any)
			//
			deregisterExpiredQueries(reader);

			List<String> regQueries = reader.evaluateTextContent(REG_QUERIES_XPATH);

			int count = regQueries.size();
			GSLoggerFactory.getLogger(getClass()).debug("Current registered queries count: {}", count);

			boolean newQuery = !regQueries.contains(regQuery);

			boolean countReached = count == maxCount;

			if (countReached && newQuery) {

			    GSLoggerFactory.getLogger(getClass())
				    .debug("Max count reached, deregistering lowest score query and adding new one");

			    docStream = deregisterAndAdd(reader, regQuery);

			} else if (!countReached && newQuery) {

			    GSLoggerFactory.getLogger(getClass()).debug("Adding new query");

			    //
			    // adds new query with score 1
			    //
			    docStream = update(reader, regQuery, true);

			} else {

			    if (countReached) {
				GSLoggerFactory.getLogger(getClass()).debug("Max count reached");
			    }

			    GSLoggerFactory.getLogger(getClass()).debug("Updating query score");

			    //
			    // removes query and adds it with increased score
			    //
			    docStream = update(reader, regQuery, false);
			}

			GSLoggerFactory.getLogger(getClass()).debug("Document updating STARTED");

			handle = new InputStreamHandle(docStream);

			docManager.write(REGISTERED_QUERIES_DOC_URI, handle, transaction);

			GSLoggerFactory.getLogger(getClass()).debug("Document updating ENDED");

			transaction.commit();

		    } catch (ResourceNotResendableException rnrex) {

			GSLoggerFactory.getLogger(getClass()).warn(rnrex.getMessage());

			transaction.rollback();

		    } catch (Exception ex) {

			ex.printStackTrace();

			GSLoggerFactory.getLogger(getClass()).error("Unable to update registered queries doc: " + ex.getMessage());

			transaction.rollback();

		    } finally {

			if (handle != null) {
			    try {
				docStream.close();
			    } catch (IOException e) {
			    }
			    content.close();
			    handle.close();
			}
		    }
		}
	    }
	};

	EXECUTOR_SERVICE.execute(command);
    }

    /**
     * @param queryToRemove
     * @throws RequestException
     */
    public void deregister(String queryToRemove) throws RequestException {

	String query = "xquery version '1.0-ml';\n";
	query += "declare namespace html = 'http://www.w3.org/1999/xhtml';\n";
	query += "declare namespace gs = 'http://flora.eu/gi-suite/1.0/dataModel/schema';\n";

	query += "cts:deregister(xs:unsignedLong(" + queryToRemove + "))";

	markLogicDB.execXQuery(query);
    }

    /**
     * @throws RequestException
     */
    public void deregister() throws RequestException {

	String query = "xquery version '1.0-ml';\n";
	query += "declare namespace html = 'http://www.w3.org/1999/xhtml';\n";
	query += "declare namespace gs = 'http://flora.eu/gi-suite/1.0/dataModel/schema';\n";

	query += "for $id in document('/" + REGISTERED_QUERIES_PROTECTED_FOLDER + "/" + REGISTERED_QUERIES_DOC + "')" + REG_QUERIES_XPATH;
	query += "return cts:deregister(xs:unsignedLong($id))";

	markLogicDB.execXQuery(query);
    }

    /**
     * Finds the reg.query with the lowest score. in case of more queries with same lowest score,
     * the oldest one is returned (that is the first in document insert order)
     * 
     * @param reader
     * @return
     * @throws XPathExpressionException
     */
    public String getLowestScoreQuery(XMLDocumentReader reader) throws XPathExpressionException {

	return Arrays.asList(reader.evaluateNodes("//gs:" + REG_QUERY_ELEMENT)).//
		stream().//
		min((n1, n2) -> {

		    Integer score1 = Integer.valueOf(n1.getAttributes().item(0).getNodeValue());
		    Integer score2 = Integer.valueOf(n2.getAttributes().item(0).getNodeValue());
		    return score1.compareTo(score2);

		}).map(n -> n.getTextContent()).//
		get();
    }

    /**
     * @param reader
     * @throws XPathExpressionException
     * @throws RequestException
     */
    private void deregisterExpiredQueries(XMLDocumentReader reader) throws Exception {

	List<Node> expiredQueries = Arrays.asList(reader.evaluateNodes("//gs:" + REG_QUERY_ELEMENT)).//
		stream().filter(n -> {

		    //
		    // null check required for backward-compatibility with previous version
		    // which do not support this feature
		    //
		    Node item = n.getAttributes().getNamedItem("gs:" + TIME_STAMP_ATTRIBUTE);
		    if (item != null) {

			String value = item.getNodeValue();
			long time = ISO8601DateTimeUtils.parseISO8601ToDate(value).get().getTime();

			return System.currentTimeMillis() - time > expirationTime;
		    }
		    return false;
		}).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).debug("Found {} expired queries", expiredQueries.size());

	for (Node node : expiredQueries) {

	    String queryToRemove = node.getTextContent();

	    deregisterAndRemove(reader, queryToRemove);
	}
    }

    /**
     * @param reader
     * @param newQuery
     * @return
     * @throws Exception
     */
    private InputStream deregisterAndAdd(XMLDocumentReader reader, String newQuery) throws Exception {

	String queryToRemove = getLowestScoreQuery(reader);

	XMLDocumentWriter writer = deregisterAndRemove(reader, queryToRemove);

	//
	// adds the new query
	//
	writer.addNode(REG_QUERIES_ROOT_XPATH, createRegQueryElement(newQuery, 1));

	return reader.asStream();
    }

    /**
     * @param reader
     * @param queryToRemove
     * @return
     * @throws Exception
     */
    private XMLDocumentWriter deregisterAndRemove(XMLDocumentReader reader, String queryToRemove) throws Exception {

	deregister(queryToRemove);

	XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	writer.remove("//gs:" + REG_QUERY_ELEMENT + "[text()='" + queryToRemove + "']");

	return writer;
    }

    /**
     * @param reader
     * @param regQuery
     * @return
     */
    private InputStream update(XMLDocumentReader reader, String regQuery, boolean newQuery) throws Exception {

	XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	int score = 1;
	if (!newQuery) {

	    score = Integer.valueOf(reader.evaluateString("//gs:" + REG_QUERY_ELEMENT + "[text()='" + regQuery + "']/@gs:score"));
	    score += 1;

	    writer.remove("//gs:" + REG_QUERY_ELEMENT + "[text()='" + regQuery + "']");
	}

	writer.addNode(REG_QUERIES_ROOT_XPATH, createRegQueryElement(regQuery, score));

	return reader.asStream();
    }

    /**
     * @return
     */
    private InputStream createDocStream() {

	return getClass().getClassLoader().getResourceAsStream(REGISTERED_QUERIES_DOC);
    }

    /**
     * @param regQuery
     * @param score
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Element createRegQueryElement(String regQuery, int score) throws ParserConfigurationException, SAXException, IOException {

	return createElement("<gs:" + REG_QUERY_ELEMENT + " gs:" + SCORE_ATTRIBUTE + "='" + score + "' gs:timeStamp='"
		+ ISO8601DateTimeUtils.getISO8601DateTime() + "'>" + regQuery + "</gs:" + REG_QUERY_ELEMENT + ">");
    }

    /**
     * @param element
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Element createElement(String element) throws ParserConfigurationException, SAXException, IOException {

	ByteArrayInputStream stream = new ByteArrayInputStream(element.getBytes(StandardCharsets.UTF_8));

	DocumentBuilderFactory builderFactory = XMLFactories.newDocumentBuilderFactory();

	builderFactory.setNamespaceAware(false);

	DocumentBuilder builder = builderFactory.newDocumentBuilder();

	return builder.parse(stream).getDocumentElement();
    }
}
