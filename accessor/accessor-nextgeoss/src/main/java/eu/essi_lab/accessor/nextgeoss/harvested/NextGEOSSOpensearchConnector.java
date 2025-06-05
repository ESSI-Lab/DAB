package eu.essi_lab.accessor.nextgeoss.harvested;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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

/**
 * @author Fabrizio
 */
public class NextGEOSSOpensearchConnector extends HarvestedQueryConnector<NextGEOSSOpenSearchConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NextGEOSSOpensearchConnector";

    // seems that NextGeoss has a static collection u
    // private final static String NEXTGEOSS_REQUEST = "httpAccept=application/atom%2Bxml&recordSchema=iso19139&";

    private int startIndex;

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());

    /**
     * This is the cached set of package identifiers, used during subsequent
     * list records.
     */

    private Integer cachedRecordsCount = null;

    public NextGEOSSOpensearchConnector() {

	startIndex = 1;
    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().toLowerCase().contains("catalogue.nextgeoss.eu/opensearch");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int startPosition = calculateStart(request.getResumptionToken());

	int pageSize = getSetting().getPageSize();
	 
	if (cachedRecordsCount == null) {
	    cachedRecordsCount = getRecordsCount();
	}

	Optional<Integer> recordsLimit = getSetting().getMaxRecords();

	if (startPosition < cachedRecordsCount) {

	    if (!getSetting().isMaxRecordsUnlimited() && startPosition >= recordsLimit.get()) {
		// max records reached - collection of new records is stopped
		ret.setResumptionToken(null);

		GSLoggerFactory.getLogger(getClass())
			.debug("Reached max records limit of {} records. Skipping collection of additional records!", recordsLimit);

		return ret;
	    }

	    List<String> result = getResultList(startPosition, pageSize);

	    int count = 0;
	    for (String res : result) {
		count++;
		OriginalMetadata metadataRecord = new OriginalMetadata();
		String metadata = res;
		metadataRecord.setMetadata(metadata);
		metadataRecord.setSchemeURI(NextGEOSSCollectionMapper.SCHEMA_URI);
		ret.addRecord(metadataRecord);
	    }

	    ret.setResumptionToken(String.valueOf(count + startPosition));
	}

	return ret;

    }

    /**
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws DOMException
     */
    private Integer getRecordsCount() {
	String baseUrl = getURL();

	try {
	    String requesturl = baseUrl + "start=1&maximumRecords=1";

	    String result = getDownloader().downloadOptionalString(requesturl).orElse(null);

	    XMLDocumentReader reader = new XMLDocumentReader(result);

	    String count = reader.evaluateNode("//*:totalResults").getTextContent();

	    return Integer.parseInt(count);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(NextGEOSSOpensearchConnector.class).error("Get Number of Records Error");
	    GSLoggerFactory.getLogger(NextGEOSSOpensearchConnector.class).error(e.getMessage(), e);
	}
	return null;

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

    /**
     * @param startPosition
     * @param pageSize
     * @return
     */
    private List<String> getResultList(int startPosition, int pageSize) {

	List<String> ret = new ArrayList<String>();
	String getRequestURL = getURL();

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
	    GSLoggerFactory.getLogger(NextGEOSSOpensearchConnector.class).error("Get List Records Error!");
	    GSLoggerFactory.getLogger(NextGEOSSOpensearchConnector.class).error(e.getMessage(), e);
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

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected NextGEOSSOpenSearchConnectorSetting initSetting() {

	return new NextGEOSSOpenSearchConnectorSetting();
    }

}
