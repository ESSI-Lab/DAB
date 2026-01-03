package eu.essi_lab.accessor.bndmet;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.accessor.bndmet.model.BNDMETData;
import eu.essi_lab.accessor.bndmet.model.BNDMETData.BNDMET_Data_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter.BNDMET_Parameter_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class BNDMETClient {

    public static final String STANDARD_ENDPOINT = "https://apitempo.inmet.gov.br/BNDMET/";

    public BNDMETClient() {
	// TODO Auto-generated constructor stub
    }

    public BNDMETClient(String endpoint) {
	this.endpoint = endpoint;
    }

    public static void main(String[] args) throws Exception {
	// HttpGet get = new HttpGet("https://apitempo.inmet.gov.br/BNDMET/estacoes/T");
	// get.addHeader("Accept", "*/*");
	// CloseableHttpResponse response = HttpClientBuilder.create().build().execute(get);
	// InputStream content = response.getEntity().getContent();
	// IOUtils.copy(content, System.out);
	BNDMETClient client = new BNDMETClient();
	String endpoint = "https://apitempo.inmet.gov.br/BNDMET/";
	client.setEndpoint(endpoint);

	List<BNDMETStation> stations = client.getStations();
	for (BNDMETStation station : stations) {
	    System.out.println(station.getValue(BNDMET_Station_Code.DC_NOME));
	    String stationCode = station.getValue(BNDMET_Station_Code.CD_ESTACAO);
	    List<BNDMETParameter> parameters = client.getStationParameters(stationCode);
	    station.setParameters(parameters);
	    for (BNDMETParameter parameter : parameters) {
		System.out.println(parameter.getValue(BNDMET_Parameter_Code.DESCRICAO));
		String parameterCode = parameter.getValue(BNDMET_Parameter_Code.CODIGO);
		System.out.println(parameterCode);
		List<BNDMETData> datas = client.getData(stationCode, parameterCode, "2020-01-01", "2020-02-02");
		for (BNDMETData data : datas) {
		    System.out.println(data.getValue(BNDMET_Data_Code.DT_MEDICAO));
		    System.out.println(data.getValue(BNDMET_Data_Code.HR_MEDICAO));
		    System.out.println(data.getValue(BNDMET_Data_Code.VL_MEDICAO));
		}
	    }
	}

    }

    public List<BNDMETData> getData(String stationCode, String parameterCode, String dateBegin, String dateEnd) {
	Optional<InputStream> optionalStream = getStream(
		endpoint + "valores/" + stationCode + "/" + parameterCode + "/" + dateBegin + "/" + dateEnd);
	return parseData(optionalStream);
    }

    private List<BNDMETData> parseData(Optional<InputStream> optionalStream) {
	ArrayList<BNDMETData> ret = new ArrayList<>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		String str = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(str);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject dataJSON = stations.getJSONObject(i);
		    BNDMETData data = new BNDMETData(dataJSON);
		    ret.add(data);
		}
	    } catch (JSONException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

	    } finally {
		try {
		    stream.close();
		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	}
	return ret;
    }

    public BNDMETParameter getStationParameter(String stationCode, String parameterCode) {
	List<BNDMETParameter> parameters = getStationParameters(stationCode);
	for (BNDMETParameter parameter : parameters) {
	    if (parameter.getValue(BNDMET_Parameter_Code.CODIGO).equals(parameterCode)) {
		return parameter;
	    }
	}
	return null;
    }

    public List<BNDMETParameter> getStationParameters(String stationCode) {
	Optional<InputStream> optionalStream = getStream(endpoint + "atributos/" + stationCode);
	return parseParameters(optionalStream, stationCode);
    }

    private List<BNDMETParameter> parseParameters(Optional<InputStream> optionalStream, String stationCode) {
	ArrayList<BNDMETParameter> ret = new ArrayList<BNDMETParameter>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		stream.close();
		String stationString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(stationString);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject parameterJSON = stations.getJSONObject(i);
		    BNDMETParameter parameter = new BNDMETParameter(parameterJSON);
		    ret.add(parameter);
		}
	    } catch (JSONException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

	    } finally {
		try {
		    stream.close();
		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	}
	return ret;
    }

    private static List<BNDMETStation> stations = new ArrayList<>();

    public List<BNDMETStation> getStations() {
	if (stations.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Getting stations");
	    synchronized (stations) {
		GSLoggerFactory.getLogger(getClass()).info("Actually getting stations");
		if (stations.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).info("Really actually getting stations");
		    List<BNDMETStation> automaticStations = getAutomaticStations();
		    stations.addAll(automaticStations);
		    List<BNDMETStation> manualStations = getManualStations();
		    stations.addAll(manualStations);
		}
	    }
	}
	return stations;

    }

    public BNDMETStation getStation(String stationCode) {
	List<BNDMETStation> stations = getStations();
	for (BNDMETStation station : stations) {
	    if (station.getValue(BNDMET_Station_Code.CD_ESTACAO).equals(stationCode)) {
		return station;
	    }
	}
	return null;
    }

    public static synchronized Optional<InputStream> getStream(String url) {
	// this parameter is added to recognize the requests are coming from WHOS, and hence will not be blocked by the
	// INMET firewall
	url = url + "?origin=WHOS";
	int tries = 3;
	Exception e = null;
	for (int i = 0; i < tries; i++) {
	    try {
		Downloader downloader = new Downloader();
		downloader.setConnectionTimeout(TimeUnit.SECONDS, 8);
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "*/*");

		Optional<InputStream> ret = downloader.downloadOptionalStream(url, HttpHeaderUtils.build(headers));
		if (ret.isPresent()) {
		    return ret;
		}
	    } catch (Exception tmp) {
		e = tmp;
		GSLoggerFactory.getLogger(BNDMETClient.class).warn("Exception contacting INMET: " + e.getMessage());
	    }
	    if (i == (tries - 1)) {
		GSLoggerFactory.getLogger(BNDMETClient.class).warn("Last try failed.");
		if (e != null) {
		    throw new RuntimeException(e.getMessage());
		}
	    } else {
		long ms = (long) (10000.0 * Math.random());
		GSLoggerFactory.getLogger(BNDMETClient.class).warn("Retrying (#{}) after a little sleep: {}ms", i, ms);
		try {
		    Thread.sleep(ms);
		} catch (InterruptedException e1) {
		}
	    }
	}
	return Optional.empty();

    }

    public List<BNDMETStation> getAutomaticStations() {
	Optional<InputStream> optionalStream = getStream(endpoint + "estacoes/T");
	return parseStations(optionalStream);
    }

    public List<BNDMETStation> getManualStations() {
	Optional<InputStream> optionalStream = getStream(endpoint + "estacoes/M");
	return parseStations(optionalStream);
    }

    private List<BNDMETStation> parseStations(Optional<InputStream> optionalStream) {
	ArrayList<BNDMETStation> ret = new ArrayList<BNDMETStation>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		stream.close();
		String stationString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(stationString);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject stationJSON = stations.getJSONObject(i);
		    BNDMETStation station = new BNDMETStation(stationJSON);
		    ret.add(station);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return ret;
    }

    private String endpoint = STANDARD_ENDPOINT;

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	if (!endpoint.endsWith("/")) {
	    endpoint = endpoint + "/";
	}
	this.endpoint = endpoint;

    }

}
