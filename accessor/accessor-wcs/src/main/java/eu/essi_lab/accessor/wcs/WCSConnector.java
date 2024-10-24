/**
 *
 */
package eu.essi_lab.accessor.wcs;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.wrapper.WrappedConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public abstract class WCSConnector extends WrappedConnector {

    /**
     *
     */
    public static final String WCS_SCHEME = "WCS_SCHEME_";
    /**
     *
     */
    private static final String WCS_CONNECTOR_NO_CAPABILITIES_ERROR = "WCS_CONNECTOR_NO_CAPABILITIES_ERROR";
    private static final String WCS_CONNECTOR_INVALID_CAPABILITIES_ERROR = "WCS_CONNECTOR_INVALID_CAPABILITIES_ERROR";
    private static final String WCS_CONNECTOR_UNEXPECTED_RESUMPTION_TOKEN_ERROR = "WCS_CONNECTOR_UNEXPECTED_RESUMPTION_TOKEN_ERROR";
    private List<String> identifiers = null;
    private XMLDocumentReader capabilities = null;
    private static ExpiringCache<XMLDocumentReader> coverageDescriptions;

    static {
	coverageDescriptions = new ExpiringCache<XMLDocumentReader>();
	coverageDescriptions.setDuration(1200000); // 20 minute cache
	coverageDescriptions.setMaxSize(50);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (identifiers == null) {
	    try {
		capabilities = getCapabilities(getSourceURL(), getVersion());
	    } catch (Exception e) {

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			WCS_CONNECTOR_NO_CAPABILITIES_ERROR, //
			e);
	    }

	    if (capabilities == null) {

		throw GSException.createException(//
			getClass(), //
			"Unable to retrieve capabilities document from: " + getSourceURL(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			WCS_CONNECTOR_NO_CAPABILITIES_ERROR);
	    }

	    if (!checkCapabilities(capabilities)) {

		throw GSException.createException(//
			getClass(), //
			"Invalid capabilities found: " + capabilities, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			WCS_CONNECTOR_INVALID_CAPABILITIES_ERROR);
	    }

	    this.identifiers = getCoverageIdentifiers(capabilities);
	}

	String resumptionToken = request.getResumptionToken();

	Integer index = resumptionToken == null ? 0 : Integer.parseInt(resumptionToken);

	Optional<Integer> mr = getSetting().getMaxRecords();

	if (mr.isPresent()) {

	    Integer maxRecords = mr.get();

	    if (identifiers.size() > maxRecords) {

		identifiers = identifiers.subList(0, maxRecords);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Found {} coverage identifiers", identifiers.size());

	String nextResumptionToken;
	if (index < identifiers.size() - 1) {
	    nextResumptionToken = "" + (index + 1);
	} else if (index == identifiers.size() - 1) {
	    nextResumptionToken = null;
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Unexpected resumption token");
	    throw GSException.createException(//
		    getClass(), //
		    "Unexpected resumption token", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    WCS_CONNECTOR_UNEXPECTED_RESUMPTION_TOKEN_ERROR);
	}

	String id = identifiers.get(index);

	XMLDocumentReader description = null;
	try {
	    description = getCoverageDescription(id);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	if (description == null) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve coverage description with id {}", id);

	} else {

	    OriginalMetadata record = new OriginalMetadata();
	    record.setSchemeURI(WCS_SCHEME + getClass().getSimpleName());

	    //
	    // adds the additional information
	    //
	    GSPropertyHandler handler = new GSPropertyHandler();
	    // the entire capabilities document
	    handler.add(new GSProperty<XMLDocumentReader>("capabilities", capabilities));
	    // the coverage identifier from the capabilities which in come cases
	    // do not match with the identifier provided in the coverage description
	    handler.add(new GSProperty<String>("capabilitiesId", id));
	    // the service endpoint
	    handler.add(new GSProperty<String>("endpoint", getSourceURL()));

	    record.setAdditionalInfo(handler);

	    try {
		record.setMetadata(description.asString());
		response.addRecord(record);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	response.setResumptionToken(nextResumptionToken);

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(WCS_SCHEME + getClass().getSimpleName());
    }

    /**
     * @param capabilities
     * @return
     */
    protected abstract boolean checkCapabilities(XMLDocumentReader capabilities);

    /**
     * @return
     */
    protected abstract String getVersion();

    /**
     * @param capabilities
     * @return
     */
    public abstract List<String> getCoverageIdentifiers(XMLDocumentReader capabilities);

    /**
     * @param id
     * @return
     */
    protected abstract String getIdentifierParameter(String id);

    /**
     * @param id
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public synchronized XMLDocumentReader getCoverageDescription(String id) throws SAXException, IOException {

	XMLDocumentReader ret = coverageDescriptions.get(getSourceURL() + id + getVersion());
	if (ret != null) {
	    return ret;
	}

	String url = normalizeURL(getSourceURL()) + "request=DescribeCoverage&service=WCS&version=" + getVersion() + "&"
		+ getIdentifierParameter(id);

	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(url);
	if (optional.isPresent()) {

	    InputStream stream = optional.get();
	    ret = new XMLDocumentReader(stream);
	    coverageDescriptions.put(getSourceURL() + id + getVersion(), ret);
	    return ret;
	}

	return null;
    }

    protected String normalizeURL(String endpoint) {

	if (endpoint.contains("?") && !endpoint.endsWith("?")) {
	    // e.g.:
	    // http://gis.csiss.gmu.edu/cgi-bin/mapserv?MAP=/media/gisiv01/mapfiles/drought/16days/2012/drought.2012.065.map&
	    if (!endpoint.endsWith("&")) {
		endpoint += "&";
	    }
	} else {
	    endpoint = endpoint.endsWith("?") ? endpoint : endpoint + "?";
	}

	return endpoint;
    }

    /**
     * @param endpoint
     * @param version
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentReader getCapabilities(String endpoint, String version) throws SAXException, IOException {

	String url = normalizeURL(endpoint) + "request=GetCapabilities&service=WCS&version=" + version;

	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(url);
	if (optional.isPresent()) {

	    InputStream stream = optional.get();
	    return new XMLDocumentReader(stream);
	}

	return null;
    }
}
