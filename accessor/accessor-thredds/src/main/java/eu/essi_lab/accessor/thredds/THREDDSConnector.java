package eu.essi_lab.accessor.thredds;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class THREDDSConnector extends HarvestedQueryConnector<THREDDSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "THREDDSConnector";

    private static final String THREAD_CONNECTOR_ERROR = "THREAD_CONNECTOR_ERROR";

    private int iteractionsNumber = 0;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(endpoint);
	if (stream.isPresent()) {
	    InputStream input = stream.get();
	    try {
		XMLDocumentReader reader = new XMLDocumentReader(input);
		String root = reader.evaluateString("local-name('/[1]')");
		return root.equals("catalog");
	    } catch (Exception e) {
		return false;
	    } finally {
		try {
		    input.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	}
	return false;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	try {
	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	    Optional<Integer> mr = getSetting().getMaxRecords();
	    boolean maxNumberReached = false;
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && iteractionsNumber > mr.get() - 1) {
		// max record set
		maxNumberReached = true;
	    }
	    if (!maxNumberReached) {

		String endpoint = getSourceURL();
		THREDDSCrawler crawler = new THREDDSCrawler(endpoint);
		String token = request.getResumptionToken();
		THREDDSReference reference = new THREDDSReference(token);
		THREDDSPage page = crawler.crawl(reference);
		List<THREDDSDataset> datasets = page.getDatasets();
		for (THREDDSDataset dataset : datasets) {
		    try {
			OriginalMetadata metadataRecord = new OriginalMetadata();
			URL isoMetadataURL = dataset.getServices().get("ISO");
			if (isoMetadataURL != null) {
			    Downloader downloader = new Downloader();
			    downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
			    String metadata = downloader.downloadOptionalString(isoMetadataURL.toExternalForm()).get();
			    metadataRecord.setMetadata(metadata);
			    MIMetadata meta = new MIMetadata(metadata);
			    String id = meta.getFileIdentifier();
			    if (id == null) {
				meta.setFileIdentifier(UUID.randomUUID().toString());
				metadata = meta.asString(true);
				metadataRecord.setMetadata(metadata);
			    }
			    metadataRecord.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
			    ret.addRecord(metadataRecord);
			} else {
			    HashMap<String, URL> servicesList = dataset.getServices();
			    List<String> refs = page.getCatalogRefs();
			    URL pageUrl = page.getURL();
			    XMLDocumentReader reader = page.getReader();

			    // try to rewrite only
			    ClonableInputStream cis = new ClonableInputStream(reader.asStream());
			    XMLDocumentReader copyReader = new XMLDocumentReader(cis.clone());
			    Node[] datasetPaths = copyReader.evaluateNodes("//*:dataset[@urlPath]");
			    XMLDocumentWriter writer = new XMLDocumentWriter(copyReader);
			    String id = dataset.getId();
			    boolean found = false;
			    for (Node n : datasetPaths) {
				String nodeID = n.getAttributes().getNamedItem("ID").getNodeValue();
				if (nodeID.endsWith("txt") || nodeID.endsWith("png")) {
				    GSLoggerFactory.getLogger(getClass()).info("Skip TXT or PNG files.");
				    continue;
				}
				if (id.equals(nodeID)) {
				    found = true;
				} else {
				    // Node nnn = copyReader.evaluateNode("//*:dataset[@urlPath='"+ nodeID +"']");
				    writer.remove("//*:dataset[@ID='" + nodeID + "']");
				}
			    }

			    if (found) {
				metadataRecord.setMetadata(copyReader.asString());
				metadataRecord.setSchemeURI(CommonNameSpaceContext.THREDDS_NS_URI);
				ret.addRecord(metadataRecord);
			    }

			}
		    } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
		    }
		}
		THREDDSReference nextReference = page.getNextReference(reference);

		if (nextReference == null) {
		    ret.setResumptionToken(null);
		} else {
		    ret.setResumptionToken(nextReference.toString());
		}
	    } else {
		ret.setResumptionToken(null);
	    }
	    iteractionsNumber++;
	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    THREAD_CONNECTOR_ERROR, //
		    e);
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GMI_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected THREDDSConnectorSetting initSetting() {

	return new THREDDSConnectorSetting();
    }

    public static void main(String[] args) throws Exception {

	System.out.println("TEST STARTED");
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	String endpoint = "http://tds.webservice-energy.org/thredds/catalog/ground-albedo/catalog.xml";
	THREDDSCrawler crawler = new THREDDSCrawler(endpoint);
	String token = null;
	THREDDSReference reference = new THREDDSReference(token);
	THREDDSPage page = crawler.crawl(reference);
	List<THREDDSDataset> datasets = page.getDatasets();
	for (THREDDSDataset dataset : datasets) {
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    URL isoMetadataURL = dataset.getServices().get("ISO");
	    if (isoMetadataURL != null) {
		Downloader downloader = new Downloader();
	    } else {
		HashMap<String, URL> servicesList = dataset.getServices();
		List<String> refs = page.getCatalogRefs();
		URL pageUrl = page.getURL();
		XMLDocumentReader reader = page.getReader();

		// try to rewrite only
		ClonableInputStream cis = new ClonableInputStream(reader.asStream());
		XMLDocumentReader copyReader = new XMLDocumentReader(cis.clone());
		Node[] datasetPaths = copyReader.evaluateNodes("//*:dataset[@urlPath]");
		XMLDocumentWriter writer = new XMLDocumentWriter(copyReader);
		String id = dataset.getId();
		boolean found = false;
		for (Node n : datasetPaths) {
		    String nodeID = n.getAttributes().getNamedItem("ID").getNodeValue();
		    if (nodeID.endsWith("txt") || nodeID.endsWith("png")) {
			GSLoggerFactory.getLogger(THREDDSConnector.class).info("Skip TXT or PNG files.");
			continue;
		    }
		    if (id.equals(nodeID)) {
			found = true;
		    } else {
			// Node nnn = copyReader.evaluateNode("//*:dataset[@urlPath='"+ nodeID +"']");
			writer.remove("//*:dataset[@ID='" + nodeID + "']");
		    }
		}

		if (found) {
		    metadataRecord.setMetadata(copyReader.asString());
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.THREDDS_NS_URI);
		    ret.addRecord(metadataRecord);
		}

	    }
	}
	System.out.println("TEST ENDED");
    }

}
