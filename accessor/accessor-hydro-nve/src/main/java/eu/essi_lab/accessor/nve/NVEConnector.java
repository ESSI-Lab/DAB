package eu.essi_lab.accessor.nve;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class NVEConnector extends StationConnector<NVEConnectorSetting> {

    public static final String TYPE = "NVEConnector";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.equals("https://hydapi.nve.no/api/v1");
    }

    private NVEClient client = null;
    private Map<String, NVEStation> stations = null;

    public NVEClient getClient() {
	return client;
    }

    public void setClient(NVEClient client) {
	this.client = client;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	initClient();

	NVEStation station = stations.get(stationId);

	ListRecordsResponse<OriginalMetadata> ret = getRecords(station);

	return ret;

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	initClient();

	int step = 10;
	String token = request.getResumptionToken();
	Integer i = null;
	if (token == null || token.equals("")) {
	    i = 0;
	} else {
	    i = Integer.parseInt(token);
	}
	int end = Math.min(i + step, stations.size());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();
	if (!getSetting().isMaxRecordsUnlimited() && optionalMaxRecords.isPresent()) {
	    Integer maxRecords = optionalMaxRecords.get();
	    if (i > maxRecords) {
		ret.setResumptionToken(null);
		return ret;
	    }
	}

	String[] stationIdentifiers = stations.keySet().toArray(new String[] {});
	Arrays.sort(stationIdentifiers, new Comparator<String>() {

	    @Override
	    public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	    }
	});

	for (; i < end; i++) {

	    NVEStation station = stations.get(stationIdentifiers[i]);

	    ret = getRecords(station);

	}
	if (i >= stations.size() - 1) {
	    ret.setResumptionToken(null);
	} else {
	    ret.setResumptionToken("" + (i + 1));
	}

	return ret;

    }

    public static String token = null;

    private void initClient() throws GSException {
	if (client == null) {
	    client = new NVEClient();
	}
	if (token != null) {
	    client.setAuthorizationKey(token);
	} else {
	    client.setAuthorizationKey(ConfigurationWrapper.getCredentialsSetting().getNVEToken().orElse(null));
	}

	if (stations == null) {
	    this.stations = client.getStations();
	}

    }

    private ListRecordsResponse<OriginalMetadata> getRecords(NVEStation station) {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	List<NVESeries> series = station.getSeries();

	// map from parameter-restime to original metadata, version number
	HashMap<String, SimpleEntry<OriginalMetadata, Integer>> possibleMetadata = new HashMap<>();

	for (int j = 0; j < series.size(); j++) {
	    NVESeries serie = series.get(j);
	    String parameterId = serie.getParameterId();
	    String versionNo = serie.getVersionNo();
	    Integer version = Integer.parseInt(versionNo);
	    List<NVEResolution> resolutions = serie.getResolutions();
	    for (int r = 0; r < resolutions.size(); r++) {
		NVEResolution resolution = resolutions.get(r);

		String restime = resolution.getResTime();

		// previously the following wasn't commented
		// as for Igor mail 2021-03-03
		// if (restime.equals("0")) {
		// continue;
		// }

		OriginalMetadata metadataRecord = new OriginalMetadata();

		metadataRecord.setSchemeURI(CommonNameSpaceContext.NVE_URI);

		NVEStation clone = (NVEStation) station.clone();

		// removes series at index different than j
		clone.removeSeriesAtOtherIndex(j);

		NVESeries childSeries = clone.getSeries().get(0);

		// removes resolutions at index different than i
		childSeries.removeSeriesAtOtherIndex(r);

		metadataRecord.setMetadata(clone.getJsonObject().toString());
		String key = parameterId + "-" + restime;

		SimpleEntry<OriginalMetadata, Integer> existingEntry = possibleMetadata.get(key);

		if (existingEntry == null) {
		    possibleMetadata.put(key, new SimpleEntry<OriginalMetadata, Integer>(metadataRecord, version));
		} else {
		    Integer actualVersion = existingEntry.getValue();
		    if (actualVersion < version) {
			possibleMetadata.put(key, new SimpleEntry<OriginalMetadata, Integer>(metadataRecord, version));
		    }
		}

	    }
	}

	for (SimpleEntry<OriginalMetadata, Integer> value : possibleMetadata.values()) {
	    ret.addRecord(value.getKey());
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.NVE_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected NVEConnectorSetting initSetting() {

	return new NVEConnectorSetting();
    }
}
