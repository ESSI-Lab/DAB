package eu.essi_lab.accessor.agrostac.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class AgrostacConnector extends HarvestedQueryConnector<AgrostacConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "AgrostacConnector";

    private String accessToken;

    public static final String BASE_URL = "https://agrostac-test.containers.wur.nl/agrostac/";

    static final String COLLECTIONS_URL = "datasets";

    static final String OVERVIEW_URL = "overview";

    public static final String CROP_TYPE = "eotypes/CropType";
    public static final String LAND_COVER_TYPE = "eotypes/LandCoverType";
    public static final String IRRIGATION_TYPE = "eotypes/IrrigationType";

    // seems that NextGeoss has a static collection u
    // private final static String NEXTGEOSS_REQUEST = "httpAccept=application/atom%2Bxml&recordSchema=iso19139&";

    private int startIndex;

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());

    JSONObject overview = null;

    /**
     * This is the cached set of package identifiers, used during subsequent
     * list records.
     */

    private Integer cachedRecordsCount = null;
    
    public AgrostacConnector() {

    }

    public AgrostacConnector(String accessToken) {

	startIndex = 0;

	setAccessToken(accessToken);
    }

    private void setAccessToken(String accessToken) {
	this.accessToken = accessToken;

	getSetting().setAccessToken(accessToken);

    }

    /**
     * @return
     */
    public String getAccessToken() {

	return accessToken;
    }

    private int recordsCount;

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().toLowerCase().contains("agrostac-test.containers.wur.nl/agrostac")
		|| source.getEndpoint().toLowerCase().contains("agrostac.wenr.wur.nl/agrostac");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	int startPosition = calculateStart(request.getResumptionToken());

	int pageSize = getSetting().getPageSize();
	if (pageSize == 0)
	    pageSize = 10;

	String token = null;
	Optional<String> accessToken = getSetting().getAccessToken();
	if (accessToken.isPresent()) {
	    token = accessToken.get();
	}

//	if (overview == null) {
//	    overview = getOverview(token);
//	}

	Optional<Integer> recordsLimit = getSetting().getMaxRecords();

	try {

	    List<JSONObject> datasets = getDatasetsCollection();
	    int count = 0;
	    for (JSONObject jsonObject : datasets) {
		count++;
		recordsCount++;
		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(AgrostacCollectionMapper.SCHEMA_URI);
		metadata.setMetadata(jsonObject.toString());
		GSPropertyHandler additionalInfo = GSPropertyHandler.of(new GSProperty<String>("accesstoken", token));
		metadata.setAdditionalInfo(additionalInfo);
		response.addRecord(metadata);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Current start position: {} -- Current records count: {}", startPosition,
		    recordsCount);

	    response.setResumptionToken(null);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Exception parsing datasets", e);
	}

	return response;

	// if (cachedRecordsCount == null) {
	// cachedRecordsCount = getRecordsCount();
	// }
	//
	// Optional<Integer> recordsLimit = getSetting().getMaxRecords();
	//
	// if (startPosition < cachedRecordsCount) {
	//
	// if (!getSetting().isMaxRecordsUnlimited() && startPosition >= recordsLimit.get()) {
	// // max records reached - collection of new records is stopped
	// ret.setResumptionToken(null);
	//
	// GSLoggerFactory.getLogger(getClass())
	// .debug("Reached max records limit of {} records. Skipping collection of additional records!", recordsLimit);
	//
	// return ret;
	// }
	//
	// List<String> result = getResultList(startPosition, pageSize);
	//
	// int count = 0;
	// for (String res : result) {
	// count++;
	// OriginalMetadata metadataRecord = new OriginalMetadata();
	// String metadata = res;
	// metadataRecord.setMetadata(metadata);
	// metadataRecord.setSchemeURI(WorldCerealCollectionMapper.SCHEMA_URI);
	// ret.addRecord(metadataRecord);
	// }
	//
	// ret.setResumptionToken(String.valueOf(count + startPosition));
	// }
	//
	// return ret;

    }

    private List<JSONObject> getDatasetsCollection() {
	String url = getSourceURL() + COLLECTIONS_URL;

	Downloader downloader = new Downloader();
	JSONObject jsonObject = new JSONObject(downloader.downloadOptionalString(url).get());

	return StreamUtils.iteratorToStream(jsonObject.getJSONArray("Datasets").iterator()).//
		map(o -> (JSONObject) o).//
		collect(Collectors.toList());
    }

    private JSONArray getEwocCode(String path) {
	String url = getSourceURL() + path;

	Downloader downloader = new Downloader();
	JSONArray jsonArray = new JSONArray(downloader.downloadOptionalString(url).get());

	return jsonArray;
    }

    /**
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws DOMException
     */
    private JSONObject getOverview(String token) {
	String baseUrl = getSourceURL() + OVERVIEW_URL;

	try {
	    String requesturl = baseUrl + "?accesstoken=" + token;

	    String result = getDownloader().downloadOptionalString(requesturl).orElse(null);

	    JSONObject jsonObject = new JSONObject(result);

	    return jsonObject;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(AgrostacConnector.class).error("Get Number of Records Error");
	    GSLoggerFactory.getLogger(AgrostacConnector.class).error(e.getMessage(), e);
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
	    GSLoggerFactory.getLogger(AgrostacConnector.class).error("Get List Records Error!");
	    GSLoggerFactory.getLogger(AgrostacConnector.class).error(e.getMessage(), e);
	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(AgrostacCollectionMapper.SCHEMA_URI);
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
    protected AgrostacConnectorSetting initSetting() {

	return new AgrostacConnectorSetting();
    }

}
