package eu.essi_lab.accessor.csw;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.thredds.THREDDSCrawler;
import eu.essi_lab.accessor.thredds.THREDDSDataset;
import eu.essi_lab.accessor.thredds.THREDDSPage;
import eu.essi_lab.accessor.thredds.THREDDSReference;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CSWTHREDDSConnector extends CSWConnector {

 
    private static final String WEBSERVICE_ENERGY_THREDDS_URL = "http://tds.webservice-energy.org/thredds/catalog.xml";

    /**
     * 
     */
    public static final String TYPE = "CSW THREDDS Connector";

    private static final String CSW_THREDDS_CONNECTOR_ERROR = "CSW_THREDDS_CONNECTOR_ERROR";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * The CSW THREDDS tries to read Online THREDDS resources and add them requires a specific mapper
     */

    @Override
    public void addExternalResources(ListRecordsResponse<OriginalMetadata> ret) {
	try {
	    //if Webservice Energy catalogue
	    if (this.getSourceURL().contains("geocatalog.webservice-energy.org")) {
		List<OriginalMetadata> omThredds = getOriginalMetadataFromTHREDDS(WEBSERVICE_ENERGY_THREDDS_URL, null);
		for (OriginalMetadata originalThredds : omThredds) {
		    originalThredds.setSchemeURI(CommonNameSpaceContext.THREDDS_NS_URI);
		    ret.addRecord(originalThredds);
		}
	    } else {
		//if we would read from Online resources with generic THREDDS endpoint - TODO: double-check and update THREDDS mapper  
		List<OriginalMetadata> originalMetadataList = ret.getRecordsAsList();
		for (OriginalMetadata om : originalMetadataList) {
		    XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());
		    Node[] onlineResources = xdoc.evaluateNodes("//*:distributionInfo//*:onLine");
		    Set<String> urls = new HashSet<String>();
		    for (Node n : onlineResources) {
			// XMLDocumentReader onlines = new XMLDocumentReader(n.getOwnerDocument());
			String protocol = xdoc.evaluateNode(n, "//*:protocol").getTextContent();

			if (protocol.contains("threddscatalog")) {
			    String url = xdoc.evaluateNode(n, "*:CI_OnlineResource/*:linkage/*:URL").getTextContent();
			    if (url.endsWith(".html")) {
				url = url.replace(".html", ".xml");
			    } else if (url.endsWith(".htm")) {
				url = url.replace(".htm", ".xml");
			    }
			    if (url.endsWith(".xml")) {
				urls.add(url);
			    }
			}
		    }
		    for (String s : urls) {
			List<OriginalMetadata> omThredds = getOriginalMetadataFromTHREDDS(s, null);
			for (OriginalMetadata originalThredds : omThredds) {
			    originalThredds.setSchemeURI(CommonNameSpaceContext.THREDDS_NS_URI);
			    ret.addRecord(originalThredds);
			}
		    }

		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(CSWTHREDDSConnector.class).trace("Exception filtering results for CSW THREDDS Catalogue");
	}

    }

    private List<OriginalMetadata> getOriginalMetadataFromTHREDDS(String url, String token) throws GSException {
	List<OriginalMetadata> results = new ArrayList<OriginalMetadata>();

	try {
	    THREDDSCrawler crawler = new THREDDSCrawler(url);
	    // String token = request.getResumptionToken();
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
			results.add(metadataRecord);
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
			    results.add(metadataRecord);
			}

		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    System.out.println(e.getCause());
		    System.out.println(e.getMessage());
		}
	    }
	    THREDDSReference nextReference = page.getNextReference(reference);

	    if (nextReference != null) {
		List<OriginalMetadata> res = getOriginalMetadataFromTHREDDS(url, nextReference.toString());
		results.addAll(res);
	    }

	    return results;

	} catch (Exception e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_THREDDS_CONNECTOR_ERROR, //
		    e);
	}
    }

    // @Override
    // public List<String> listMetadataFormats() {
    // List<String> toret = new ArrayList<>();
    // toret.add(CommonNameSpaceContext.THREDDS_NS_URI);
    // return toret;
    // }

    /**
     * The CSW THREDDS connector applies only to the CSW WEBSERVICE Energy catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("webservice-energy.org")) {
	    boolean cswBaseSupport = super.supports(source);
	    return cswBaseSupport;
	} else {
	    return false;
	}

    }

}
