package eu.essi_lab.accessor.cmr.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.mapper.OpensearchCollectionMapper;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CMRIDNOpensearchConnector<S extends CMRIDNOpensearchConnectorSetting> extends HarvestedQueryConnector<S> {

    /**
     * 
     */
    public static final String TYPE = "CMRIDNOpensearchConnector";

    private final static String IDN_REQUEST = "clientId=gs-service&";

    private final static String CWIC_REQUEST = "isCwic=true&";

    private int startIndex;

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private Integer recordsCount = null;

    public CMRIDNOpensearchConnector() {

	startIndex = 1;
    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().toLowerCase().contains("cmr.earthdata.nasa.gov/opensearch");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int startPosition = calculateStart(request.getResumptionToken());

	GSLoggerFactory.getLogger(getClass()).error("Start position: {}", startPosition);

	int pageSize = getSetting().getPageSize();
	 
	if (recordsCount == null) {
	    recordsCount = getRecordsCount();
	}

	if (recordsCount == 0) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve records count, exit");
	    return ret;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Records count: {}", recordsCount);

	Optional<Integer> recordsLimit = getSetting().getMaxRecords();

	if (startPosition < recordsCount) {

	    if (!getSetting().isMaxRecordsUnlimited() && startPosition >= recordsLimit.get()) {
		// max records reached - collection of new records is stopped
		ret.setResumptionToken(null);
		String collectionName = isCwic() ? "CWIC" : "CEOS IDN";
		GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class).debug("{} COLLECTIONS WITH GRANULES FOUND {}", collectionName,
			OpensearchCollectionMapper.COLLECTIONS_WITH_GRANULES_COUNT);
		OpensearchCollectionMapper.COLLECTIONS_WITH_GRANULES_COUNT = 0;
		GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class)
			.debug("Reached max records limit of {} records. Skipping collection of additional records!", recordsLimit);

		return ret;
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Getting result list {}/{} STARTED", startPosition, recordsCount);

	    List<String> result = getResultList(startPosition, pageSize);

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} results", result.size());

	    int count = 0;
	    for (String res : result) {
		count++;
		OriginalMetadata metadataRecord = new OriginalMetadata();
		String metadata = res;
		metadataRecord.setMetadata(metadata);
		metadataRecord.setSchemeURI(getSchemaURI());
		ret.addRecord(metadataRecord);
	    }

	    if (count == 0) {// this avoids to loop forever
		count = pageSize;
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Getting result list {}/{} ENDED", startPosition, recordsCount);

	    ret.setResumptionToken(String.valueOf(count + startPosition));
	}

	if (ret.getResumptionToken() == null) {
	    String collectionName = isCwic() ? "CWIC" : "CEOS IDN";
	    GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class).debug("{} COLLECTIONS WITH GRANULES FOUND {}", collectionName,
		    OpensearchCollectionMapper.COLLECTIONS_WITH_GRANULES_COUNT);
	    OpensearchCollectionMapper.COLLECTIONS_WITH_GRANULES_COUNT = 0;
	}

	return ret;
    }

    protected String getSchemaURI() {
	return CMRIDNOpensearchCollectionMapper.SCHEMA_URI;
    }

    /**
     * @param startPosition
     * @param pageSize
     * @return
     */
    private List<String> getResultList(int startPosition, int pageSize) {

	List<String> ret = new ArrayList<String>();
	startPosition = startPosition - 1;
	String getRequestURL = isCwic() ? getURL() + CWIC_REQUEST + IDN_REQUEST + "offset=" + startPosition + "&numberOfResults=" + pageSize
		: getURL() + IDN_REQUEST + "offset=" + startPosition + "&numberOfResults=" + pageSize;

	try {

	    String resultList = getDownloader().downloadOptionalString(getRequestURL).orElse(null);

	    XMLDocumentReader responseDocument = new XMLDocumentReader(resultList);

	    Node[] entries = responseDocument.evaluateNodes("//*:entry");

	    for (Node entry : entries) {

		if (!getSetting().isMaxRecordsUnlimited() && ret.size() > getSetting().getMaxRecords().get()) {
		    break;
		}
		ret.add(XMLDocumentReader.asString(entry));
	    }

	    return ret;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class).error("Get List Records Error!");
	    GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class).error(e.getMessage(), e);
	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(CommonNameSpaceContext.OS_1_1_NS_URI);
    }

    private int calculateStart(String token) {
	// the first request start position is "1"
	int startPosition = this.startIndex;
	if (token != null) {
	    startPosition = Integer.parseInt(token);
	}

	return startPosition;
    }

    /**
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws DOMException
     */
    private Integer getRecordsCount() {
	// String baseUrl = getURL();

	try {
	    String requesturl = isCwic() ? getURL() + CWIC_REQUEST + IDN_REQUEST + "numberOfResults=1"
		    : getURL() + IDN_REQUEST + "numberOfResults=1";

	    String result = getDownloader().downloadOptionalString(requesturl).orElse(null);

	    XMLDocumentReader reader = new XMLDocumentReader(result);

	    String count = reader.evaluateNode("//*:totalResults").getTextContent();

	    return Integer.parseInt(count);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(CMRIDNOpensearchConnector.class).error("Get records count error: {}", e.getMessage());
	}

	return 0;
    }

    /**
     * @return
     */
    private String getURL() {
	String baseUrl = getSourceURL();
	if (!baseUrl.endsWith("?"))
	    baseUrl += "?";
	return baseUrl;
    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    protected boolean isCwic() {
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected S initSetting() {

	return (S) new CMRIDNOpensearchConnectorSetting();
    }
}
