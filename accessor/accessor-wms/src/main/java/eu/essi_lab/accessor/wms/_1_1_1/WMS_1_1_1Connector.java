package eu.essi_lab.accessor.wms._1_1_1;

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import eu.essi_lab.accessor.wms.IWMSCapabilities;
import eu.essi_lab.accessor.wms.WMSConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wms._1_1_1.Layer;
import eu.essi_lab.jaxb.wms._1_1_1.WMTMSCapabilities;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class WMS_1_1_1Connector extends WMSConnector {

    private static final String WMS_111_CONNECTOR_CAPABILITIES_RETRIEVE_ERROR = "WMS_111_CONNECTOR_CAPABILITIES_RETRIEVE_ERROR";
    private static final String WMS_111_CONNECTOR_LAYER_CREATION_ERROR = "WMS_111_CONNECTOR_LAYER_CREATION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "WMS Connector 1.1.1";

    static JAXBContext context;

    private Downloader downloader;
    private WMTMSCapabilities capabilities;
    private Layer baseLayer;

    static {

	try {

	    context = JAXBContext.newInstance(WMTMSCapabilities.class);

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(WMS_1_1_1Connector.class).error("Fatal initialization error in WMS 1.1.1 connector!", e);

	}
    }

    public Downloader getDownloader() {
	return downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    /**
     * 
     */
    public WMS_1_1_1Connector() {

	super();

	setDownloader(new Downloader());
    }

    @Override
    public Provider getProvider() {
	return null;
    }

    int index = 0;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	retrieveCapabilities();

	String resumptionToken = null;
	String nextResumptionToken = null;
	ByteArrayOutputStream baos = null;

	try {

	    WMTMSCapabilities emptyCapabilities = getEmptyCapabilities();

	    resumptionToken = listRecords.getResumptionToken();

	    GSLoggerFactory.getLogger(getClass()).debug("Listing record: {}", resumptionToken);

	    nextResumptionToken = "1,";

	    Layer copy = getLayerCopy();
	    index++;
	    // resumption token are in the form 1,3,4,5
	    // indicating the node to be returned in a depth first visit of the layer tree
	    if (resumptionToken == null) {
		index = 0;
		List<Layer> nextStage = copy.getLayer();
		if (!nextStage.isEmpty()) {
		    nextResumptionToken += "1";
		} else {
		    nextResumptionToken = null;
		}
		copy.getLayer().clear();

	    } else {

		Optional<Integer> maxRecords = getSetting().getMaxRecords();
		if (maxRecords.isPresent() && maxRecords.get() != 0) {
		    if (index > maxRecords.get()) {
			ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
			return ret;
		    }
		}

		String[] split = resumptionToken.split(",");
		List<Layer> layers = new ArrayList<>();
		Layer tmp = copy;

		String backtrackResumptionToken = null;
		List<Layer> backtrackLayers = new ArrayList<>();

		for (int i = 1; i < split.length; i++) {
		    String childIndexString = split[i];
		    Integer childIndex = Integer.parseInt(childIndexString) - 1;
		    List<Layer> children = tmp.getLayer();
		    Layer selected = children.get(childIndex);
		    if (i == split.length - 1) {
			// last element
			List<Layer> grandChildren = selected.getLayer();
			if (grandChildren.isEmpty()) {
			    if ((childIndex + 1) < children.size()) {
				// brother of the child next selected
				nextResumptionToken += (childIndex + 2);
			    } else {
				// backtrack
				if (backtrackResumptionToken != null) {
				    nextResumptionToken = backtrackResumptionToken;
				} else {
				    // end of the visit
				    nextResumptionToken = null;
				}
			    }
			} else {
			    // child of the child next selected
			    nextResumptionToken += (childIndex + 1) + ",1";
			}
		    } else {
			// there is a brother.. possible backtrack here
			if ((childIndex + 1) < children.size()) {
			    Layer brother = children.get(childIndex + 1);
			    for (int j = 0; j < layers.size() - 1; j++) {
				backtrackLayers.add(layers.get(j));
			    }
			    backtrackLayers.add(brother);
			    backtrackResumptionToken = nextResumptionToken + (childIndex + 2);
			}
			nextResumptionToken += (childIndex + 1) + ",";
		    }
		    layers.add(selected);
		    tmp.getLayer().clear();
		    tmp = selected;
		}
		Layer last = copy;
		for (Layer layer : layers) {
		    last.getLayer().clear();
		    last.getLayer().add(layer);
		    last = layer;
		}
		last.getLayer().clear();
	    }

	    emptyCapabilities.getCapability().setLayer(copy);

	    baos = new ByteArrayOutputStream();
	    context.createMarshaller().marshal(emptyCapabilities, baos);

	} catch (Exception t) {

	    GSLoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
	    throw GSException.createException(//
		    getClass(), //
		    "JAXBException retrieving next records", //
		    null, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ErrorInfo.ERRORTYPE_INTERNAL, WMS_111_CONNECTOR_LAYER_CREATION_ERROR);
	}

	String metadata = new String(baos.toByteArray());

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	OriginalMetadata metadataRecord = new OriginalMetadata();

	metadataRecord.setMetadata(metadata);

	metadataRecord.setSchemeURI(CommonNameSpaceContext.WMS_1_1_1_NS_URI);

	response.addRecord(metadataRecord);

	response.setResumptionToken(nextResumptionToken);

	return response;

    }

    private Layer getLayerCopy() throws Exception {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	context.createMarshaller().marshal(baseLayer, baos);

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	Layer ret = null;

	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
	spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	SAXParser parser = spf.newSAXParser();
	XMLReader reader = parser.getXMLReader();
	InputSource inputSource = new InputSource(bais);
	SAXSource source = new SAXSource(reader, inputSource);

	ret = (Layer) context.createUnmarshaller().unmarshal(source);

	bais.close();

	return ret;
    }

    @Override
    public IWMSCapabilities getCapabilities() throws GSException {
	WMTMSCapabilities fullCapabilities = retrieveCapabilities();
	return new WMS_1_1_1Capabilities(fullCapabilities);
    }

    protected WMTMSCapabilities retrieveCapabilities() throws GSException {

	if (this.capabilities == null) {
	    WMTMSCapabilities retrieved = getCapabilities(getSourceURL(), false);
	    this.capabilities = retrieved;
	    this.baseLayer = this.capabilities.getCapability().getLayer();

	}

	return capabilities;

    }

    /**
     * Returns a capabilities document that is equal to the original one, but without any layer.
     *
     * @return
     * @throws JAXBException
     */
    private WMTMSCapabilities getEmptyCapabilities() throws Exception {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	synchronized (capabilities) {
	    capabilities.getCapability().setLayer(null);
	    context.createMarshaller().marshal(capabilities, baos);
	    capabilities.getCapability().setLayer(baseLayer);
	}

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
	spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	SAXParser parser = spf.newSAXParser();
	XMLReader reader = parser.getXMLReader();
	InputSource inputSource = new InputSource(bais);
	SAXSource source = new SAXSource(reader, inputSource);

	WMTMSCapabilities cleanCapabilities = null;
	cleanCapabilities = (WMTMSCapabilities) context.createUnmarshaller().unmarshal(source);

	baos.close();
	bais.close();

	return cleanCapabilities;
    }

    @Override
    public List<String> listMetadataFormats() {

	List<String> ret = new ArrayList<>();

	ret.add(CommonNameSpaceContext.WMS_1_1_1_NS_URI);

	return ret;
    }

    @Override
    public boolean supports(GSSource source) {
	try {

	    WMTMSCapabilities caps = getCapabilities(source.getEndpoint(), true);

	    if (caps != null) {
		return true;
	    }

	} catch (GSException e) {

	    e.log();
	}

	return false;
    }

    protected WMTMSCapabilities getCapabilities(String sourceURL, boolean silent) throws GSException {

	List<URL> urlsList = getCapabilitiesURLs(sourceURL);

	for (URL url : urlsList) {

	    GSLoggerFactory.getLogger(getClass()).debug("Getting capabilities from: {}", url);

	    InputStream content = null;

	    Optional<InputStream> ret = downloader.downloadOptionalStream(url.toString());

	    if (ret.isPresent()) {
		content = ret.get();
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("No stream from WMS 1.1.1 Connector from: {}", url);

	    }

	    if (content != null) {

		try {

		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		    spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		    SAXParser parser = spf.newSAXParser();
		    XMLReader reader = parser.getXMLReader();
		    InputSource inputSource = new InputSource(content);
		    SAXSource source = new SAXSource(reader, inputSource);

		    WMTMSCapabilities r = (WMTMSCapabilities) context.createUnmarshaller().unmarshal(source);
		    content.close();
		    return r;

		} catch (Exception ex) {

		    if (!silent) {

			GSLoggerFactory.getLogger(getClass()).warn("Invalid capabilities document from: {}", url);
			GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
		    }
		}
	    }
	}

	throw GSException.createException( //
		getClass(), //
		"Unable to retrieve WMS 1.1.1 GetCapabilities from: " + sourceURL, //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		WMS_111_CONNECTOR_CAPABILITIES_RETRIEVE_ERROR);
    }

    private List<URL> getCapabilitiesURLs(String sourceURL) {

	List<URL> ret = new ArrayList<>();

	sourceURL = normalizeURL(sourceURL); // to prepare the URL to accept additional parameters
	try {
	    ret.add(new URL(sourceURL + "SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1"));

	} catch (MalformedURLException e) {

	    GSLoggerFactory.getLogger(getClass()).trace("Can't create url from source url {}", sourceURL);

	}

	return ret;
    }

    public String normalizeURL(String url) {
	if (url.endsWith("?") || url.endsWith("&")) {
	    return url;
	}

	return url.contains("?") ? url + "&" : url + "?";
    }

    public void setCapabilities(WMS_1_1_1Capabilities capabilities) {
	this.capabilities = capabilities.getCapabilities();

    }

    @Override
    public String getType() {

	return TYPE;
    }
}
