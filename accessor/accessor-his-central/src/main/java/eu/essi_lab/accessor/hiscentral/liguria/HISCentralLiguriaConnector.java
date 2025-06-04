package eu.essi_lab.accessor.hiscentral.liguria;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class HISCentralLiguriaConnector extends HarvestedQueryConnector<HISCentralLiguriaConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralLiguriaConnector";

    /**
     * 
     */
    public HISCentralLiguriaConnector() {

    }

    /**
     * 
     */
    static final String SENSORS_URL = "ANAGRAFICA";

    static final String DATI_URL = "DATI";

    static final String VAR_DESCRIPTION = "DESCRIZIONE";

    public static final String BASE_URL = "https://aws.arpal.liguria.it/ords/ws/HIS_CENTRAL/";

    private int maxRecords;

    /**
     * Anagrafica delle stazioni: https://aws.arpal.liguria.it/ords/ws/HIS_CENTRAL/ANAGRAFICA
     * Descrizione variabili: https://aws.arpal.liguria.it/ords/ws/HIS_CENTRAL/DESCRIZIONE
     * Dati da stazione:
     * https://aws.arpal.liguria.it/ords/ws/HIS_CENTRAL/DATI?dtrf_beg=202301010000&dtrf_end=202301010100&code=CFUNZ
     **/

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Downloader downloader = new Downloader();

	String url = getSourceURL().endsWith("/") ? getSourceURL() + SENSORS_URL : getSourceURL() + "/" + SENSORS_URL;

	String descriptionVariableURL = getSourceURL().endsWith("/") ? getSourceURL() + VAR_DESCRIPTION
		: getSourceURL() + "/" + VAR_DESCRIPTION;

	Optional<String> response = downloader.downloadOptionalString(url);

	if (response.isPresent()) {

	    JSONObject object = new JSONObject(response.get());

	    Optional<String> description_Response = downloader.downloadOptionalString(descriptionVariableURL);

	    JSONObject descriptionVarObj = null;
	    if (description_Response.isPresent()) {

		descriptionVarObj = new JSONObject(description_Response.get());
	    }

	    // JSONObject datasetMetadata = object.getJSONObject("dataset-metadata");

	    JSONArray metadataStations = object.getJSONArray("items");

	    maxRecords = metadataStations.length();

	    getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);

	    for (int i = 0; i < maxRecords; i++) {

		JSONObject sensorInfo = metadataStations.getJSONObject(i);

		String code = sensorInfo.optString("code");
		if (code != null && !code.isEmpty()) {
		    Date d = new Date();
		    String date = HISCentralLiguriaMapper.getDate(d);
		    String initialDate = "190001010000";
		    String linkageUrl = url + "?code=" + code;
		    String dataUrl = getSourceURL().endsWith("/")
			    ? getSourceURL() + DATI_URL + "?dtrf_beg=" + initialDate + "&dtrf_end=" + date + "&code=" + code
			    : getSourceURL() + "/" + DATI_URL + "?dtrf_beg=" + initialDate + "&dtrf_end=" + date + "&code=" + code;

		    Optional<String> dataResp = downloader.downloadOptionalString(dataUrl);
		    if (dataResp.isPresent()) {
			JSONObject dataObj = new JSONObject(dataResp.get());
			JSONArray dataItems = dataObj.getJSONArray("items");
			String startTime = null;
			if (dataItems != null && dataItems.length() > 0) {
			    JSONObject varObject = dataItems.optJSONObject(0);
			    startTime = varObject.optString("dtrf");
			    Iterator<String> iterator = varObject.keys();
			    while (iterator.hasNext()) {
				String s = iterator.next();
				if (s.contains("code") || s.contains("dtrf")) {
				    continue;
				}
				ret.addRecord(HISCentralLiguriaMapper.create(s, startTime, linkageUrl, sensorInfo, descriptionVarObj));
			    }
			}
		    }

		}

	    }
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("arpal.liguria.it");
    }

    @Override
    protected HISCentralLiguriaConnectorSetting initSetting() {

	return new HISCentralLiguriaConnectorSetting();
    }

    public static void main(String[] args) throws ParseException {

	float a = 4388137;
	float c = 784759;
	float b = 100000;
	double d = 4388137;
	double d1 = 784759;
	double div = 100000;
	double res = d / div;
	double res1 = d1 / div;
	float result = a / b;
	float result2 = c / b;

	System.out.println(result);
	System.out.println(result2);
	System.out.println(res);
	System.out.println(res1);

	Date date = new Date();
	Date dateBefore = new Date(date.getTime() - 30 * 24 * 3600 * 1000l); // Subtract n days
	String isotime = ISO8601DateTimeUtils.getISO8601Date(dateBefore);

	System.out.println(isotime.replace("-", "") + "0000");

	Optional<Date> notStandard = ISO8601DateTimeUtils.parseNotStandard2ToDate("190001010000");

	if (notStandard.isPresent()) {
	    Date dat = notStandard.get();
	}

    }

}
