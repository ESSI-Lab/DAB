package eu.essi_lab.accessor.invenio;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class INVENIOConnector extends HarvestedQueryConnector<INVENIOConnectorSetting> {

    /**
     * ENDPOINT:
     * https://35.166.90.189/api/records?
     * Get a record with {id}: https://35.166.90.189/api/records/{id}
     **/

    public static final String TYPE = "INVENIOConnector";

    private static final String INVENIO_READ_ERROR = "Unable to find stations URL";

    private static final String INVENIO_URL_NOT_FOUND_ERROR = "INVENIO_URL_NOT_FOUND_ERROR";

    private Integer cachedRecordsCount = null;

    // private static final String PLATFORM_CODES = "platformCodes";
    //
    // private static final String METADATA_BASIC = "floats/basic/";
    //
    // private static final String METADATA_FULL = "floats/";

    private int partialNumbers;

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public INVENIOConnector() {

	this.downloader = new Downloader();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("35.166.90.189");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String token = request.getResumptionToken();
	int start = 1;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();
//	if (pageSize == 0)
//	    pageSize = 20;

	if (cachedRecordsCount == null) {
	    cachedRecordsCount = getRecordsCount();
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (start < cachedRecordsCount && !maxNumberReached && partialNumbers < cachedRecordsCount) {

	    List<String> result = getResultList(start, pageSize);

	    for (String s : result) {

		if (s != null && !s.isEmpty()) {

		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.INVENIO_NS_URI);
		    metadata.setMetadata(s);
		    ret.addRecord(metadata);
		    partialNumbers++;
		}

	    }
	    ret.setResumptionToken(String.valueOf(start + 1));
	    logger.debug("ADDED {} records. Number of analyzed page: {}", partialNumbers, String.valueOf(start));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL RECORDS SIZE: {}", partialNumbers, cachedRecordsCount);
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    /**
     * @param startPosition
     * @param pageSize
     * @return
     */
    private List<String> getResultList(int startPosition, int pageSize) {

	List<String> ret = new ArrayList<String>();
	String baseUrl = getURL();

	try {
	    String requesturl = baseUrl + "page=" + startPosition + "&size=" + pageSize;

	    String resultList = getDownloader().downloadOptionalString(requesturl).orElse(null);

	    if (resultList != null) {
		JSONObject object = new JSONObject(resultList);

		JSONObject hit = object.optJSONObject("hits");

		if (hit != null) {

		    JSONArray arrayResults = hit.optJSONArray("hits");

		    for (Object arr : arrayResults) {
			ret.add(arr.toString());
		    }
		}

	    }

	    return ret;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(INVENIOConnector.class).error("Get List Records Error!");
	    GSLoggerFactory.getLogger(INVENIOConnector.class).error(e.getMessage(), e);
	    return ret;
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.INVENIO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected INVENIOConnectorSetting initSetting() {

	return new INVENIOConnectorSetting();
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
     * @return
     * @throws IOException
     */
    private Integer getRecordsCount() {
	String baseUrl = getURL();

	try {
	    String requesturl = baseUrl + "page=1&size=1";

	    String result = getDownloader().downloadOptionalString(requesturl).orElse(null);

	    if (result != null) {
		JSONObject object = new JSONObject(result);
		JSONObject hit = object.optJSONObject("hits");
		
		String total = (hit != null) ? hit.optString("total", null) : null;
		
		
		if (total == null || "".equals(total) || "[]".equals(total) || "null".equals(total)) {
		    return null;
		}
		return Integer.parseInt(total);

	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(INVENIOConnector.class).error("Get Number of Records Error");
	    GSLoggerFactory.getLogger(INVENIOConnector.class).error(e.getMessage(), e);
	    return null;
	}
	return null;
    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

}
