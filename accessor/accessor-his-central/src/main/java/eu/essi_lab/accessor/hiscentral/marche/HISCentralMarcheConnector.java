package eu.essi_lab.accessor.hiscentral.marche;

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
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class HISCentralMarcheConnector extends HarvestedQueryConnector<HISCentralMarcheConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralMarcheConnector";

    /**
     * 
     */
    public HISCentralMarcheConnector() {

    }

    /**
     * 
     */
    static final String SENSORS_URL = "http://app.protezionecivile.marche.it/his/sensors";

    /**
     * 
     */
    static final String SENSOR_URL = "http://app.protezionecivile.marche.it/his/sensor";

    /**
     * 
     */
    public static final String BASE_URL = "http://app.protezionecivile.marche.it";

    private int maxRecords;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Downloader downloader = new Downloader();

	Optional<String> response = downloader.downloadOptionalString(SENSORS_URL);

	if (response.isPresent()) {

	    JSONObject object = new JSONObject(response.get());

	    JSONObject datasetMetadata = object.getJSONObject("dataset-metadata");

	    JSONArray pointTimeSeriesObservation = object.getJSONArray("pointTimeSeriesObservation");

	    maxRecords = pointTimeSeriesObservation.length();

	    getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);

	    for (int i = 0; i < maxRecords; i++) {

		JSONObject sensorInfo = pointTimeSeriesObservation.getJSONObject(i);
		ret.addRecord(HISCentralMarcheMapper.create(datasetMetadata, sensorInfo));
	    }
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("app.protezionecivile.marche.it");
    }

    @Override
    protected HISCentralMarcheConnectorSetting initSetting() {

	return new HISCentralMarcheConnectorSetting();
    }
}
