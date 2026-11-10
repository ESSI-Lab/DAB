package eu.essi_lab.accessor.hiscentral.sicilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the stations and sensors (elements) of the Protezione Civile Regione Siciliana (CAE Datascape API, e.g.
 * https://www.protezionecivilesicilia.it:8443/DatascapeA). One record per element.
 *
 * @author Roberto
 */
public class HISCentralSiciliaConnector extends HarvestedQueryConnector<HISCentralSiciliaConnectorSetting> {

    static final String TYPE = "HISCentralSiciliaConnector";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	HISCentralSiciliaClient client = new HISCentralSiciliaClient(getSourceURL());

	Map<String, JSONObject> stations = new HashMap<>();
	JSONArray stationsArray = client.getStations();
	for (int i = 0; i < stationsArray.length(); i++) {
	    JSONObject station = stationsArray.getJSONObject(i);
	    stations.put(station.optString("i"), station);
	}

	JSONArray elements = client.getElements();

	GSLoggerFactory.getLogger(getClass()).info("Found {} stations and {} elements", stations.size(), elements.length());

	int maxRecords = getSetting().getMaxRecords().orElse(Integer.MAX_VALUE);

	for (int i = 0; i < elements.length() && ret.getRecordsAsList().size() < maxRecords; i++) {

	    JSONObject element = elements.getJSONObject(i);
	    JSONObject station = stations.get(element.optString("stationId"));

	    if (station == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Station {} of element {} not found, skipping", //
			element.optString("stationId"), element.optString("elementId"));
		continue;
	    }

	    ret.addRecord(HISCentralSiciliaMapper.create(station, element));
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("protezionecivilesicilia.it");
    }

    @Override
    protected HISCentralSiciliaConnectorSetting initSetting() {

	return new HISCentralSiciliaConnectorSetting();
    }
}
