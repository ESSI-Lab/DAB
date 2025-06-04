/**
 *
 */
package eu.essi_lab.accessor.digitalglobe;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.utils.ShapeReader;
import eu.essi_lab.lib.utils.zip.Unzipper;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class DigitalGlobeConnector extends HarvestedQueryConnector<DigitalGlobeConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "DigitalGlobeConnector";

    private static final String DIGITAL_GLOBE_LINKS_RETRIEVAL_CONNECTION_ERROR = "DIGITAL_GLOBE_LINKS_RETRIEVAL_CONNECTION_ERROR";
    private static final String DIGITAL_GLOBE_SHAPE_RETRIEVAL_CONNECTION_ERROR = "DIGITAL_GLOBE_SHAPE_RETRIEVAL_CONNECTION_ERROR";
    private static final String DIGITAL_GLOBE_SHAPE_READING_ERROR = "DIGITAL_GLOBE_SHAPE_READING_ERROR";
    private static final String DIG_GLOBE_DATA_PROPNAME = "ACQDATE";

    private List<List<String>> linksLists;
    private transient IterationLogger iterationLogger;
    private int recordsCount;
    private int max;
    private File tempFolder;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> out = new ListRecordsResponse<>();

	String url = getSourceURL();
	if (!url.endsWith("/")) {
	    url += "/";
	}

	String token = request.getResumptionToken();
	int index = 0;

	Optional<Integer> mr = getSetting().getMaxRecords();

	if (token != null) {
	    index = Integer.valueOf(token);
	} else {

	    tempFolder = Files.createTempDir();
	    GSLoggerFactory.getLogger(getClass()).debug("Temp folder set: {}", tempFolder);

	    try {
		List<String> links = retrieveLinks(url);

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent())
		    max = mr.get();
		else
		    max = links.size();

		iterationLogger = new IterationLogger(this, max);

		linksLists = Lists.partition(links, 10);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			DIGITAL_GLOBE_LINKS_RETRIEVAL_CONNECTION_ERROR, //
			e);
	    }
	}

	List<String> currentLinksList = linksLists.get(index);
	if (index < linksLists.size() - 1 && (getSetting().isMaxRecordsUnlimited()
		|| (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && recordsCount < mr.get()))) {

	    out.setResumptionToken(String.valueOf(++index));
	}

	if (getSetting().isMaxRecordsUnlimited() || !getSetting().isMaxRecordsUnlimited() && mr.isPresent() && recordsCount < mr.get()) {

	    try {

		List<File> files = retrieveShapeFiles(currentLinksList);

		List<OriginalMetadata> metadatas = createMetadata(currentLinksList, files);
		metadatas.forEach(m -> {
		    out.addRecord(m);
		    iterationLogger.iterationEnded();
		    recordsCount++;
		});

		GSLoggerFactory.getLogger(getClass()).debug("Records count: {}/{}", recordsCount, max);

	    } catch (IOException ioex) {

		GSLoggerFactory.getLogger(getClass()).error(ioex.getMessage(), ioex);

		throw GSException.createException(//
			getClass(), //
			ioex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			DIGITAL_GLOBE_SHAPE_READING_ERROR, //
			ioex);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			DIGITAL_GLOBE_SHAPE_RETRIEVAL_CONNECTION_ERROR, //
			e);

	    }
	}

	return out;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://partner.digitalglobe.com");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(DigitalGlobeResourceMapper.DIGITAL_GLOBE_SCHEME_URI);
    }

    private class InternalGrabber extends HREFGrabberClient {

	public InternalGrabber(URL url) {

	    super(url);

	    setHREF_A_ClosingTag("</td>");
	}

	@Override
	protected String externalizeLink(URL url, String link) {

	    return "https://partner.digitalglobe.com/partners_shpfiles/wvarchive/" + link;
	}
    }

    private List<String> retrieveLinks(String url) throws Exception {

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Links retrieval STARTED");

	String dailyURL = url + "dailylist.jsp";
	String yearlyURL = url + "yearlylist.jsp";

	ArrayList<String> links = Lists.newArrayList();

	InternalGrabber client = new InternalGrabber(new URL(dailyURL));
	links.addAll(client.grabLinks());

	client = new InternalGrabber(new URL(yearlyURL));
	links.addAll(client.grabLinks());

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Links retrieval ENEDED");

	return links;
    }

    private List<File> retrieveShapeFiles(List<String> links) throws Exception {

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Shape files retrieval STARTED");

	ArrayList<File> out = Lists.newArrayList();

	for (String link : links) {

	    Downloader downloader = new Downloader();
	    Optional<InputStream> stream = downloader.downloadOptionalStream(link);

	    if (stream.isPresent()) {

		Unzipper unzipper = new Unzipper(stream.get());
		unzipper.setOutputFolder(tempFolder);

		String zipName = link.substring(link.lastIndexOf('/'), link.length()).replace(".zip", "@");

		List<File> files = unzipper.unzipAll(zipName);
		files.forEach(f -> f.deleteOnExit());

		Optional<File> first = files.stream().//
			filter(f -> f.getName().endsWith(".shp")).//
			findFirst();

		if (first.isPresent())
		    out.add(first.get());

	    } else {

		GSLoggerFactory.getLogger(DigitalGlobeConnector.class).error("Unable to download zip file: {}", link);
	    }
	}

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Shape files retrieval ENDED");

	return out;
    }

    private List<OriginalMetadata> createMetadata(List<String> currentLinksList, List<File> shapes)
	    throws IOException, SAXException, TransformerException, XPathExpressionException {

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Metadata creation STARTED");

	ArrayList<OriginalMetadata> out = Lists.newArrayList();

	for (int i = 0; i < shapes.size(); i++) {

	    File shape = shapes.get(i);
	    File shapeMetadata = new File(shape.getAbsolutePath() + ".xml");

	    XMLDocumentReader docReader = new XMLDocumentReader(new FileInputStream(shapeMetadata));
	    XMLDocumentWriter docWriter = new XMLDocumentWriter(docReader);

	    String link = currentLinksList.get(i);

	    docWriter.addAttributes(//
		    "//metadata", //
		    "link", link, //
		    "name", shape.getName().substring(0, shape.getName().indexOf('@')));

	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setSchemeURI(DigitalGlobeResourceMapper.DIGITAL_GLOBE_SCHEME_URI);

	    ShapeReader reader = new ShapeReader(shape);
	    String typeName = reader.getTypeNames().get(0);

	    org.geotools.api.geometry.BoundingBox bBox = reader.getBBox(typeName);

	    if (bBox != null) {

		docWriter.setText("//metadata/idinfo/spdom/bounding/westbc", String.valueOf(bBox.getMinX()));
		docWriter.setText("//metadata/idinfo/spdom/bounding/eastbc", String.valueOf(bBox.getMaxX()));
		docWriter.setText("//metadata/idinfo/spdom/bounding/northbc", String.valueOf(bBox.getMaxY()));
		docWriter.setText("//metadata/idinfo/spdom/bounding/southbc", String.valueOf(bBox.getMinY()));
	    }

	    List<String> timeExtent = reader.getTimeExtent(typeName, DIG_GLOBE_DATA_PROPNAME);
	    if (timeExtent != null) {

		docWriter.setText("//metadata/idinfo/timeperd/timeinfo/sngdate/caldate", timeExtent.get(0) + " " + timeExtent.get(1));
	    }

	    metadata.setMetadata(docReader.asString(true));
	    out.add(metadata);
	}

	GSLoggerFactory.getLogger(DigitalGlobeConnector.class).debug("Metadata creation ENDED");

	return out;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected DigitalGlobeConnectorSetting initSetting() {

	return new DigitalGlobeConnectorSetting();
    }
}
