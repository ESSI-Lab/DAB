package eu.essi_lab.accessor.ckan.packagesearch;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.ckan.CKANConnector;
import eu.essi_lab.accessor.ckan.md.CKANConstants;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;


/**
 * @author boldrini
 */
public class CKANPackageSearchConnector extends CKANConnector {

    /**
     * 
     */
    public static final String TYPE = "CKANPackageSearchConnector";

    private int startIndex;

    /**
     * This is the cached set of package identifiers, used during subsequent list records.
     */

    private Integer cachedRecordsCount = null;

    public CKANPackageSearchConnector() {
	startIndex = 0;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	if (cachedRecordsCount == null) {
	    cachedRecordsCount = getPackageCount();
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Integer id = calculateStart(listRecords.getResumptionToken());
	int pageSize = getRecordsLimit();
	if (pageSize == 0)
	    pageSize = 100;

	if (id < cachedRecordsCount) {
	    List<String> result = getPackageDesciptionList(id, pageSize);

	    // int count = 0;
	    for (String res : result) {
		// count++;
		OriginalMetadata metadataRecord = new OriginalMetadata();
		metadataRecord.setSchemeURI(CKANConstants.CKAN);
		String metadata = res;
		metadataRecord.setMetadata(metadata);
		ret.addRecord(metadataRecord);
	    }

	    ret.setResumptionToken(String.valueOf(pageSize + id));
	}

	return ret;

    }

    private String getPackageSearchEndpoint() {
	String sourceURL = getSourceURL();
	return getPackageSearchEndpoint(sourceURL);
    }

    private String getPackageSearchEndpoint(String url) {

	String sourceURL = url;

	if (url.endsWith("/")) {
	    sourceURL = url.substring(0, sourceURL.length() - 1);
	}

	String packageListEndpoint;

	if (sourceURL.toLowerCase().contains("package_search")) {
	    packageListEndpoint = sourceURL;
	} else if (sourceURL.toLowerCase().contains("api/3/action")) {
	    packageListEndpoint = sourceURL + "/package_search?";
	} else {
	    packageListEndpoint = sourceURL + "/api/3/action/package_search?";
	}

	return packageListEndpoint;
    }

    /**
     * Retrieves the package description from the remote CKAN service
     *
     * @return the packages description as a JSON string
     * @throws GSException
     */

    public List<String> getPackageDesciptionList(int start, int rows) {
	List<String> ret = new ArrayList<>();
	String packageListEndpoint = getPackageSearchEndpoint() + "start=" + start + "&rows=" + rows;

	String packageList = downloader.downloadOptionalString(packageListEndpoint).orElse(null);
	JSONObject json = new JSONObject(packageList);

	JSONArray array = json.getJSONObject("result").getJSONArray("results");
	Optional<Integer> mr = getSetting().getMaxRecords();

	for (Object arr : array) {

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && ret.size() > mr.get()) {
		break;
	    }

	    ret.add(arr.toString());
	}

	return ret;
    }

    private Integer getPackageCount() {
	String packageSearchEndpoint = getPackageSearchEndpoint();
	String packageList = downloader.downloadOptionalString(packageSearchEndpoint).orElse(null);
	JSONObject json = new JSONObject(packageList);

	return json.getJSONObject("result").getInt("count");

    }

    private int getRecordsLimit() {
	Optional<Integer> op = getSetting().getMaxRecords();

	return op.isPresent() ? op.get() : 100;
    }

    private int calculateStart(String token) {
	// the first request start position is "1"
	int startPosition = this.startIndex;
	if (token != null) {
	    startPosition = Integer.parseInt(token);
	}

	return startPosition;
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
