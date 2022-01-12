package eu.essi_lab.lib.xml;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.UnicodeBOMInputStream;
public class XMLDocumentReader extends XMLNodeReader {

    private static boolean LOG = false;

    protected static DocumentBuilder builder;

    static {
	try {
	    synchronized (LOCK) {
		builder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();
	    }

	} catch (ParserConfigurationException e) {
	    // Document builder factory instantiation error.. this should not happen!
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(XMLDocumentReader.class).error("The default XML document builder could not be instantiated");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	}
    }

    /**
     * @return
     */
    public Document getDocument() {
	return (Document) targetNode;
    }

    /*
     * CONSTRUCTORS
     */

    /**
     * Creates a new <code>XMLDocumentReader</code> from the supplied <code>document</code>
     * 
     * @param node a non <code>null</code> W3C node
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentReader(Document document) {

	this.targetNode = document;
    }

    /**
     * Constructs a new <code>XMLDocumentReader</code> from a {@link File}
     * 
     * @param file {@link File} containing the content to be parsed
     * @throws IOException If any IO errors occur
     * @throws SAXException If any parse errors occur
     * @throws IllegalArgumentException When <code>stream</code> is <code>null</code>
     */
    public XMLDocumentReader(File file) throws SAXException, IOException {
	init(file);
    }

    private void init(File file) throws SAXException, IOException {
	if (file == null) {
	    throw new IllegalArgumentException("XMLDocument: the file containing the content to be parsed is null");
	}

	String uuid = UUID.randomUUID().toString();
	if (LOG)
	    GSLoggerFactory.getLogger(getClass()).trace("XML_LOCK_WAIT {}", uuid);

	synchronized (LOCK) {

	    if (LOG)
		GSLoggerFactory.getLogger(getClass()).trace("XML_LOCK_ENTERED {}", uuid);

	    FileInputStream fis = null;
	    UnicodeBOMInputStream ubis = null;
	    InputStreamReader reader = null;
	    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	    try {
		final Runnable actualTask = new Runnable() {
		    int i = 0;

		    @Override
		    public void run() {
			i++;
			if (LOG)
			    GSLoggerFactory.getLogger(getClass()).info("LOCK_LONG_WAIT in lock {}: {} seconds", uuid, (i * 10));
			if (i >= 20) {
			    GSLoggerFactory.getLogger(getClass()).info("LOCK_LONG_WAIT EXTREME in lock {}: {} seconds", uuid, (i * 10));
			    executorService.shutdown();
			    try {
				File dump = File.createTempFile(getClass().getSimpleName(), ".dump.xml");
				Files.copy(file.toPath(), dump.toPath(), StandardCopyOption.REPLACE_EXISTING);
				GSLoggerFactory.getLogger(getClass()).info("Dumped XML file in lock {} at: {}", uuid,
					dump.toPath().toString());
			    } catch (Exception e) {
				e.printStackTrace();
				GSLoggerFactory.getLogger(getClass()).info("Error during dump in lock {} at: {}", uuid, e.getMessage());
			    }
			}
		    }
		};
		executorService.scheduleAtFixedRate(actualTask, 10, 10, TimeUnit.SECONDS);

		fis = new FileInputStream(file);
		ubis = new UnicodeBOMInputStream(fis);
		reader = new InputStreamReader(ubis, StandardCharsets.UTF_8);
		InputSource source = new InputSource(reader);
		ubis.skipBOM();
		Document ret = builder.parse(source);
		targetNode = ret;
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).info("Exception during XML parsing: " + e.getMessage());
		throw e;
	    } finally {
		if (ubis != null)
		    ubis.close();
		if (reader != null)
		    reader.close();
		if (fis != null)
		    fis.close();
		executorService.shutdown();
	    }

	}

	if (LOG)
	    GSLoggerFactory.getLogger(getClass()).trace("XML_LOCK_RELEASED {}", uuid);

    }

    /**
     * Input stream constructor. Delegates to the file constructor
     * 
     * @param stream {@link InputStream} containing the content to be parsed
     * @throws IOException If any IO errors occur
     * @throws SAXException If any parse errors occur
     * @throws IllegalArgumentException When <code>stream</code> is <code>null</code>
     */
    public XMLDocumentReader(InputStream stream) throws SAXException, IOException {

	if (stream == null) {
	    throw new IllegalArgumentException("XMLDocument: the stream containing the content to be parsed is null");
	}
	// GSLoggerFactory.getLogger(getClass()).trace("Creating tmp file");
	File tmp = File.createTempFile(getClass().getSimpleName(), ".xml");
	tmp.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(stream, fos);
	stream.close();
	init(tmp);
	// GSLoggerFactory.getLogger(getClass()).trace("Deleting tmp file");
	tmp.delete();

    }

    /**
     * String constructor. Delegates to the input stream constructor.
     * 
     * @param document
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentReader(String document) throws SAXException, IOException {
	this(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
    }

}
