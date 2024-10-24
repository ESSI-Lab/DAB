package eu.essi_lab.accessor.ckan;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.ckan.md.CKANConstants;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;


/**
 * @author boldrini
 */
public class CKANConnector extends HarvestedQueryConnector<CKANConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CKANConnector";

    private static final String CKAN_CONNECTOR_ERROR = "CKAN_CONNECTOR_ERROR";

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());
    /**
     * This is the cached set of package identifiers, used during subsequent list records.
     */

    private Set<String> cachedPackageList = null;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    public CKANConnector() {
	this.downloader = new Downloader();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {
	if (cachedPackageList == null) {
	    cachedPackageList = getPackageList();
	}
	Iterator<String> iterator = cachedPackageList.iterator();
	String id = listRecords.getResumptionToken();
	String nextId = null;
	if (id == null) {
	    if (iterator.hasNext()) {
		// we start from the first
		id = iterator.next();
		if (iterator.hasNext()) {
		    nextId = iterator.next();
		}
	    } else {
		// empty package list
		// nextId remains null
	    }
	} else {
	    if (cachedPackageList.contains(id)) {
		while (iterator.hasNext()) {
		    String tmp = iterator.next();
		    if (tmp.equals(id) && iterator.hasNext()) {
			nextId = iterator.next();
			break;
		    }
		    // if it is the last element
		    // nextId remains null
		}

	    } else {
		// if the package id is not found in the package list
		throw GSException.createException(//
			getClass(), //
			"Unable to resume from resumption token: " + id, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CKAN_CONNECTOR_ERROR //

		);
	    }
	}
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (id != null) {
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setSchemeURI(CKANConstants.CKAN);
	    String metadata = showPackage(id);
	    metadataRecord.setMetadata(metadata);
	    ret.addRecord(metadataRecord);
	}
	ret.setResumptionToken(nextId);
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CKANConstants.CKAN);
	return toret;
    }

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();
	String packageListEndpoint = getPackageListEndpoint(baseEndpoint);

	try {
	    String packageList = getDownloader().downloadOptionalString(packageListEndpoint).orElse(null);
	    JSONObject json = new JSONObject(packageList);
	    JSONArray array = getJSONArray(json, "result");
	    if (array.length() > 0) {
		return true;
	    }
	} catch (Exception e) {
	    // any exception during download or during json serialization

	    logger.warn("exception during download or during json serialization", e);
	}
	return false;
    }

    /**
     * Retrieves an alphabetically ordered list of CKAN package identifiers from the remote CKAN service
     *
     * @return a list of CKAN package identifiers
     */

    public Set<String> getPackageList() {
	Set<String> toret = new TreeSet<>();
	String packageListEndpoint = getPackageListEndpoint();

	String packageList = getDownloader().downloadOptionalString(packageListEndpoint).orElse(null);
	
	JSONObject json = new JSONObject(packageList);
	JSONArray array = getJSONArray(json, "result");

	Optional<Integer> mr = getSetting().getMaxRecords();

	for (Object arr : array) {

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && toret.size() > mr.get()) {
		break;
	    }

	    toret.add(arr.toString());
	}

	return toret;
    }

    /**
     * Retrieves the package description from the remote CKAN service
     *
     * @param id the package id
     * @return the package description as a JSON string
     */
    public String showPackage(String id) {

	String packageShowEndpoint = getShowPackageEndpoint();

	return getDownloader().downloadOptionalString(packageShowEndpoint + "?id=" + id).orElse(null);

    }

    private String getPackageListEndpoint() {
	String sourceURL = getSourceURL();
	return getPackageListEndpoint(sourceURL);
    }

    private String getShowPackageEndpoint() {
	String packageListEndpoint = getPackageListEndpoint();
	String baseEndpoint = packageListEndpoint.substring(0, packageListEndpoint.indexOf("/api/3/action"));
	return baseEndpoint + "/api/3/action/package_show";
    }

    private String getPackageListEndpoint(String url) {

	String sourceURL = url;

	if (url.endsWith("/")) {
	    sourceURL = url.substring(0, sourceURL.length() - 1);
	}

	String packageListEndpoint;
	if (sourceURL.toLowerCase().contains("datacatalog.worldbank.org")) {
	    packageListEndpoint = sourceURL + "/api/3/action/package_list";
	} else {

	    if (sourceURL.toLowerCase().contains("package_list")) {
		packageListEndpoint = sourceURL;
	    } else if (sourceURL.toLowerCase().contains("api/3/action")) {
		packageListEndpoint = sourceURL + "/package_list";
	    } else {
		packageListEndpoint = sourceURL + "/api/3/action/package_list?q=";
	    }
	    if (!packageListEndpoint.contains("?q=") && !packageListEndpoint.contains("&q=")) {
		if (packageListEndpoint.contains("?")) {
		    if (packageListEndpoint.endsWith("&")) {
			packageListEndpoint = packageListEndpoint + "q=";
		    } else {
			packageListEndpoint = packageListEndpoint + "&q=";
		    }
		} else {
		    packageListEndpoint = packageListEndpoint + "?q=";
		}
	    }
	}
	return packageListEndpoint;
    }

    protected JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    JSONArray toret = result.getJSONArray(key);
	    if (toret == null) {
		toret = new JSONArray();
	    }
	    return toret;
	} catch (Exception e) {

	    logger.warn(e.getMessage(), e);
	    return new JSONArray();
	}
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CKANConnectorSetting initSetting() {

	return new CKANConnectorSetting();
    }
}
