package eu.essi_lab.accessor.wfs;

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.wfs.v_1_1_0.FeatureTypeType;
import net.opengis.wfs.v_1_1_0.ObjectFactory;
import net.opengis.wfs.v_1_1_0.WFSCapabilitiesType;
public class WFS_1_1_0Connector extends AbstractGSconfigurable implements IHarvestedQueryConnector {

    @JsonIgnore
    public static Unmarshaller unmarshaller;
    @JsonIgnore
    public static Marshaller marshaller;
    @JsonIgnore
    private static final String WFS_CONNECTOR_ERROR = "WFS_CONNECTOR_CAPABILITIES_RETRIEVE_ERROR";
    @JsonIgnore
    private Map<String, GSConfOption<?>> connectorOptions = new HashMap<>();
    @JsonIgnore
    private String sourceUrl;
    @JsonIgnore
    private Downloader downloader;
    @JsonIgnore
    private transient WFSCapabilitiesType capabilities;
    private static final String CLEAN_CAP_CREATION_ERR = "CLEAN_CAP_CREATION_ERR";
    private static final String CANT_CREATE_CAP_URL = "CANT_CREATE_CAP_URL";

    static {
	try {
	    JAXBContext context = JAXBContext.newInstance(ObjectFactories.WFS().getClass());
	    unmarshaller = context.createUnmarshaller();
	    marshaller = context.createMarshaller();
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(WFS_1_1_0Connector.class).error("Fatal initialization error in WFS connector!");
	    GSLoggerFactory.getLogger(WFS_1_1_0Connector.class).error(e.getMessage(), e);
	}
    }

    public Downloader getDownloader() {
	return downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    public WFS_1_1_0Connector() {

	setLabel("WFS 1.1.0 Connector");

	setDownloader(new Downloader());

    }

    @Override
    public Provider getProvider() {
	return null;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	retrieveCapabilities();

	JAXBElement<WFSCapabilitiesType> emptyCapabilities = getEmptyCapabilities();

	String resumptionToken = listRecords.getResumptionToken();

	GSLoggerFactory.getLogger(getClass()).info("Listing record: {}" , resumptionToken);

	String nextResumptionToken = null;

	List<FeatureTypeType> featureList = capabilities.getFeatureTypeList().getFeatureType();

	FeatureTypeType featureType = null;

	boolean skip = false;

	// resumption token are in the form: i
	// indicating the feature type index to be returned from the feature type list
	if (resumptionToken == null) {

	    if (featureList.isEmpty() || featureList.size() == 1) {
		nextResumptionToken = null;
	    } else {
		nextResumptionToken = "1";
	    }
	    if (!featureList.isEmpty()) {
		featureType = featureList.get(0);
	    }

	} else {

	    try {
		Integer index = Integer.parseInt(resumptionToken);
		featureType = featureList.get(index);
		if (featureList.size() > (index + 1)) {
		    nextResumptionToken = "" + (index + 1);
		}
	    } catch (Exception e) {
		throw GSException.createException( //
			getClass(), //
			"Invalid resumption token", //
			null, //
			ErrorInfo.ERRORTYPE_CLIENT, //
			ErrorInfo.SEVERITY_ERROR, //
			WFS_CONNECTOR_ERROR);
	    }
	}

	if (getSourceURL().contains("147.102.5.93") && featureType != null && isApprovedNamespace(featureType.getName().getPrefix())) {
	    skip = true;// featureType.getName()
	}

	emptyCapabilities.getValue().getFeatureTypeList().getFeatureType().add(featureType);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
	    marshaller.marshal(emptyCapabilities, baos);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Can't marshal empty capabilities", e);
	}
	String metadata = new String(baos.toByteArray());

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	OriginalMetadata metadataRecord = new OriginalMetadata();

	if (!skip) {

	    metadataRecord.setMetadata(metadata);

	    metadataRecord.setSchemeURI(CommonNameSpaceContext.WFS_1_1_0_NS_URI);

	    response.addRecord(metadataRecord);
	}

	response.setResumptionToken(nextResumptionToken);

	return response;

    }

    protected WFSCapabilitiesType retrieveCapabilities() throws GSException {
	try {
	    if (this.capabilities == null) {
		WFSCapabilitiesType retrieved = getCapabilities(getSourceURL(), false);
		String version = retrieved.getVersion();
		if (version != null && !version.startsWith("1.1")) {
		    throw GSException.createException( //
			    getClass(), //
			    "Connector mismatch, using WFS 1.1.0 connector to retrieve from WFS " + version, //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    WFS_CONNECTOR_ERROR);

		}

		this.capabilities = retrieved;

	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).debug(e.getMessage());
	}

	if (capabilities == null) {
	    throw GSException.createException( //
		    getClass(), //
		    "Unable to retrieve WFS GetCapabilities", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WFS_CONNECTOR_ERROR);
	} else {
	    return capabilities;
	}

    }

    /**
     * Returns a capabilities document that is equal to the original one, but without any feature type.
     *
     * @return
     */
    private JAXBElement<WFSCapabilitiesType> getEmptyCapabilities() throws GSException {

	JAXBElement<WFSCapabilitiesType> cleanCapabilities = null;

	try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

	    synchronized (capabilities) {
		List<FeatureTypeType> features = new ArrayList<>();

		features.addAll(capabilities.getFeatureTypeList().getFeatureType());

		capabilities.getFeatureTypeList().getFeatureType().clear();

		ObjectFactory factory = new ObjectFactory();

		JAXBElement<WFSCapabilitiesType> jaxbCapabilities = factory.createWFSCapabilities(capabilities);

		marshaller.marshal(jaxbCapabilities, baos);

		capabilities.getFeatureTypeList().getFeatureType().addAll(features);
	    }

	    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

	    cleanCapabilities = (JAXBElement<WFSCapabilitiesType>) unmarshaller.unmarshal(bais);

	} catch (Exception e) {

	    throw GSException.createException(getClass(), "Error creating clean capabilities", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CLEAN_CAP_CREATION_ERR, e);
	}

	return cleanCapabilities;

    }

    @Override
    public List<String> listMetadataFormats() {

	List<String> ret = new ArrayList<>();

	ret.add(CommonNameSpaceContext.WFS_1_1_0_NS_URI);

	return ret;
    }

    @Override
    public boolean supports(Source source) {
	try {
	    WFSCapabilitiesType wfscapabilities = getCapabilities(source.getEndpoint(), true);
	    if (wfscapabilities != null) {
		return true;
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).debug("Exception in getting capabilities");
	}

	return false;
    }

    protected WFSCapabilitiesType getCapabilities(String sourceURL, boolean silent) throws GSException {

	List<URL> urlsList = getCapabilitiesURLs(sourceURL);

	for (URL url : urlsList) {

	    GSLoggerFactory.getLogger(getClass()).debug("Getting capabilities with URL: {}" , url);
	    InputStream content = null;
	    try {

		Optional<InputStream> ret = downloader.downloadStream(url.toString());
		if (ret.isPresent()) {
		    content = ret.get();
		} else {
		    throw GSException.createException(getClass(), "No stream from WFS 1.1.0 Connector " + sourceURL, null, ErrorInfo.ERRORTYPE_SERVICE,
			    ErrorInfo.SEVERITY_ERROR, CANT_CREATE_CAP_URL); 
		}

	    } catch (Exception ex) {

		if (!silent) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to contact the WFS service");
		    GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
		}
	    }

	    if (content != null) {
		try {
		    Object object = unmarshaller.unmarshal(content);
		    if (object instanceof JAXBElement<?>) {
			object = ((JAXBElement<?>) object).getValue();
		    }
		    return (WFSCapabilitiesType) object;

		} catch (Exception ex) {

		    if (!silent) {

			GSLoggerFactory.getLogger(getClass()).warn("Invalid capabilities document");
			GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
		    }
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).error("Unable to get Capabilities document");
	return null;
    }

    private List<URL> getCapabilitiesURLs(String sourceURL) throws GSException {

	List<URL> ret = new ArrayList<>();

	sourceURL = normalizeURL(sourceURL); // to prepare the URL to accept additional parameters

	try {

	    ret.add(new URL(sourceURL + "SERVICE=WFS&REQUEST=GetCapabilities&VERSION=1.1.0"));

	} catch (MalformedURLException e) {

	    throw GSException.createException(getClass(), "Malformed url from " + sourceURL, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CANT_CREATE_CAP_URL, e);
	}

	return ret;
    }

    public String normalizeURL(String url) {
	if (url.endsWith("?") || url.endsWith("&")) {
	    return url;
	}

	return url.contains("?") ? url + "&" : url + "?";
    }

    @Override
    public String getSourceURL() {
	return sourceUrl;
    }

    @Override
    public void setSourceURL(String sourceURL) {
	sourceUrl = sourceURL;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return connectorOptions;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	//nothing
    }

    @Override
    public void onFlush() throws GSException {
	//nothing
    }

    @Override
    public boolean supportsIncrementalHarvesting() {
	return false;
    }

    public void setCapabilities(WFSCapabilitiesType capabilities) {
	this.capabilities = capabilities;

    }

    private boolean isApprovedNamespace(String layerName) {
	return layerName != null && !layerName.equals("")
		&& (!(layerName.contains("geomesa") || layerName.contains("kifisos") || layerName.contains("danube")));
    }

}
