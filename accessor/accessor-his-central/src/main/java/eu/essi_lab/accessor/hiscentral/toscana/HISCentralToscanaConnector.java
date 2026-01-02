package eu.essi_lab.accessor.hiscentral.toscana;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class HISCentralToscanaConnector extends HarvestedQueryConnector<HISCentralToscanaConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralToscanaConnector";

    private static final String HISCENTRAL_TOSCANA_READ_ERROR = "Unable to find stations URL";

    private static final String HISCENTRAL_TOSCANA_URL_NOT_FOUND_ERROR = "HISCENTRAL_TOSCANA_URL_NOT_FOUND_ERROR";
    /**
     * ENDPOINTS:
     * platformCode: https://fleetmonitoring.euro-argo.eu/platformCodes
     * metadata basic: e.g. https://fleetmonitoring.euro-argo.eu/floats/basic/6903238
     * metadata full: e.g. https://fleetmonitoring.euro-argo.eu/floats/6903238
     **/

    private static final String PLATFORM_CODES = "platformCodes";

    private static final String METADATA_BASIC = "floats/basic/";

    private static final String METADATA_FULL = "floats/";

    private List<String> originalMetadata;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public HISCentralToscanaConnector() {

	this.downloader = new Downloader();

	this.originalMetadata = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("sir.toscana");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (originalMetadata.isEmpty()) {
	    originalMetadata = getOriginalMetadata();
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (start < originalMetadata.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > originalMetadata.size()) {
		end = originalMetadata.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String om = originalMetadata.get(i);

		if (om != null && !om.isEmpty()) {

		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI);
		    metadata.setMetadata(om);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}

	    }
	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed floats: {}", partialNumbers, String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, originalMetadata.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private List<String> getOriginalMetadata() throws GSException {
	logger.trace("SIR TOSCANA List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String sirToscanaUrl = getSourceURL();

	logger.trace("SIR TOSCANA LIST COLLECTION IDENTIFIER URL: {}", sirToscanaUrl);

	Optional<String> listResponse = downloader.downloadOptionalString(sirToscanaUrl);

	if (listResponse.isPresent()) {

	    JSONObject features = new JSONObject(listResponse.get());

	    // JSONObject feature = features.optJSONObject("features");

	    if (features != null) {

		JSONArray arrayResults = features.optJSONArray("features");

		for (Object arr : arrayResults) {

		    JSONObject data = (JSONObject) arr;

		    JSONObject propertiesObject = data.optJSONObject("properties");
		    JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");

		    if (infoObject != null) {

			Map<String, Object> variables = infoObject.toMap();

			for (Map.Entry<String, Object> entry : variables.entrySet()) {
			    String variableName = entry.getKey();
			    JSONObject var = new JSONObject();
			    var.put(variableName, data);
			    ret.add(var.toString());
			}
		    }

		}
	    }
	    // String[] splittedString = listResponse.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]",
	    // "").split(",");
	    // ret = Arrays.asList(splittedString);

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    HISCENTRAL_TOSCANA_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_TOSCANA_URL_NOT_FOUND_ERROR);

	}

	logger.trace("SIR Toscana List Data finding ENDED");

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HISCentralToscanaConnectorSetting initSetting() {

	return new HISCentralToscanaConnectorSetting();
    }

    public static void main(String[] args) throws Exception {
	String urlPlatform = "http://www.sir.toscana.it/archivio/dati.php?D=json_stations";
	Downloader d = new Downloader();
	Optional<String> res = d.downloadOptionalString(urlPlatform);
	System.out.println(res.get());

	JSONObject object = new JSONObject(res.get());

	JSONArray arrayResults = object.optJSONArray("features");

	JSONObject data = (JSONObject) arrayResults.get(1);

	Map<String, Object> map = data.toMap();

	Set<Entry<String, Object>> treemap = map.entrySet();

	JSONObject geometryObject = data.optJSONObject("geometry");
	JSONObject propertiesObject = data.optJSONObject("properties");
	JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");

	Map<String, Object> variables = infoObject.toMap();

	Map<String, String> dataVariables = new HashMap<String, String>();

	for (Map.Entry<String, Object> entry : variables.entrySet()) {
	    String variableName = entry.getKey();

	    String dataUrl = "";

	    JSONObject newObj = new JSONObject();
	    newObj.put(variableName, data);

	    String s = newObj.toString();
	    System.out.println(s);

	    JSONObject oo = new JSONObject(s);
	    Map<String, Object> testJson = oo.toMap();

	    for (Map.Entry<String, Object> newEntry : testJson.entrySet()) {
		String var = newEntry.getKey();
		JSONObject ooo = oo.getJSONObject(var);

		System.out.println(ooo.length());

	    }

	}

	System.out.println(arrayResults.length());

	System.out.println(object.keys().hasNext());
    }
}
