package eu.essi_lab.accessor.hiscentral.piemonte;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class HISCentralPiemonteConnector extends HarvestedQueryConnector<HISCentralPiemonteConnectorSetting> {

    public enum PiemonteStationType {
	METEO("dati_giornalieri_meteo","sensori_meteo"), //
	HYDRO("dati_giornalieri_idro","sensori_idro"), //
	SNOW("dati_giornalieri_nivo",null);

	private String dataParameter;

	public String getDataParameter() {
	    return dataParameter;
	}
	
	private String variableField;

	public String getVariableField() {
	    return variableField;
	}

	PiemonteStationType(String dataParameter,String variableField) {
	    this.dataParameter = dataParameter;
	    this.variableField = variableField;
	}
    }

    /**
     * TIPOLOGIA SENSORI:
     * 0 -- > METEO_STATION_URL ----- 1287 meteo
     * "tmedia": null,
     * "tmax": null,
     * "tmin": null,
     * "tclasse": null,
     * "ptot": null,
     * "pclasse": null,
     * "vmedia": null,
     * "vraffica": null,
     * "settore_prevalente": null,
     * "tempo_permanenza": null,
     * "durata_calma": null,
     * "vclasse": null,
     * "umedia": null,
     * "umin": null,
     * "umax": null,
     * 1 -- > HYDRO_STATION_URL ------ variabili livello,portata --- 242
     * 2 -- > SNOW_STATION_URL ------ variabili hs (altezza neve al suolo) e hn (altezza neve fresca) --- 130
     */

    public enum PIEMONTE_Variable {

	TMEDIA("Temperatura", "tmedia", "°C", InterpolationType.AVERAGE, PiemonteStationType.METEO), //
	TMAX("Temperatura", "tmax", "°C", InterpolationType.MAX, PiemonteStationType.METEO), //
	TMIN("Temperatura", "tmin", "°C", InterpolationType.MIN, PiemonteStationType.METEO), //
	UMIN("Umidità", "umin", "%", InterpolationType.MIN, PiemonteStationType.METEO), //
	UMMEDIA("Umidità", "umedia", "%", InterpolationType.AVERAGE, PiemonteStationType.METEO), //
	UMAX("Umidità", "umax", "%", InterpolationType.MAX, PiemonteStationType.METEO), //
	VMEDIA("Velocità del vento", "vmedia", "m/s", InterpolationType.AVERAGE, PiemonteStationType.METEO), //
	VRAFFICA("Velocità del vento", "vraffica", "m/s", InterpolationType.MAX, PiemonteStationType.METEO), //
	PTOT("Precipitazione", "ptot", "cm", InterpolationType.TOTAL, PiemonteStationType.METEO), //
	RADD("Radiazione", "rtot", "MJ/m²", InterpolationType.TOTAL, PiemonteStationType.METEO), //

	PORTATA("Portata", "portatamedia", "m³/s", InterpolationType.AVERAGE, PiemonteStationType.HYDRO), //
	IDRO("Livello", "livellomedio", "m", InterpolationType.AVERAGE, PiemonteStationType.HYDRO), //

	HS("Altezza neve dal suolo", "hs", "cm", InterpolationType.MAX, PiemonteStationType.SNOW), //
	HN("Altezza neve fresca", "hn", "cm", InterpolationType.TOTAL, PiemonteStationType.SNOW); //

	// pluviometria giornaliera?
	// radiazioni??

	private String label;
	private String paramId;
	private String units;
	private InterpolationType interpolation;
	private PiemonteStationType stationType;

	public PiemonteStationType getStationType() {
	    return stationType;
	}

	public InterpolationType getInterpolation() {
	    return interpolation;
	}

	public String getLabel() {
	    return label;
	}

	public String getParam() {
	    return paramId;
	}

	public String getUnits() {
	    return units;
	}

	PIEMONTE_Variable(String label, String paramId, String units, InterpolationType interpolation, PiemonteStationType stationType) {
	    this.label = label;
	    this.paramId = paramId;
	    this.units = units;
	    this.interpolation = interpolation;
	    this.stationType = stationType;
	}

	public static PIEMONTE_Variable decode(String parameterCode) {
	    for (PIEMONTE_Variable var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;
	}

	public static List<PIEMONTE_Variable> values(PiemonteStationType type) {
	    PIEMONTE_Variable[] values = values();
	    List<PIEMONTE_Variable> ret = new ArrayList<>();
	    for (PIEMONTE_Variable piemonte_Variable : values) {
		if (piemonte_Variable.getStationType().equals(type)) {
		    ret.add(piemonte_Variable);
		}
	    }
	    return ret;
	}

    }

    private int countDataset = 0;

    /**
     * 
     */
    static final String TYPE = "HISCentralPiemonteConnector";

    /**
     * 
     */
    public HISCentralPiemonteConnector() {

	this.downloader = new Downloader();
    }

    private Downloader downloader;

    /**
     * 
     */
    // static final String SENSORS_URL = "http://app.protezionecivile.marche.it/his/sensors";
    // static final String SENSOR_URL = "http://app.protezionecivile.marche.it/his/sensors";
    /**
     * 
     */
    static final String METEO_STATION_URL = "stazione_meteorologica/";
    /**
     * 
     */
    static final String SNOW_STATION_URL = "stazione_nivologica/";
    /**
     * 
     */
    static final String HYDRO_STATION_URL = "stazione_idrologica/";
    /**
     * 
     */

    /**
     * 
     */
    public static final String BASE_URL = "https://utility.arpa.piemonte.it/meteoidro/";

    private int maxRecords;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int page = 0;

	String rt = request.getResumptionToken();

	if (rt != null) {

	    page = Integer.valueOf(rt);
	}

	HISCentralPiemonteClient client = new HISCentralPiemonteClient(BASE_URL);
	
	String path = "";
	String getVariableField = "";
	String variableType = "";
	List<PIEMONTE_Variable> variableNames = null;
	

	switch (page) {
	case 0:
	    path = METEO_STATION_URL + "?format=json";
	    getVariableField = "sensori_meteo";
	    variableType = "fk_id_punto_misura_meteo";
	    variableNames = PIEMONTE_Variable.values(PiemonteStationType.METEO);
	    break;

	case 1:
	    path = HYDRO_STATION_URL + "?format=json";
	    getVariableField = "sensori_idro";
	    variableType = "fk_id_punto_misura_idro";
	    variableNames = PIEMONTE_Variable.values(PiemonteStationType.HYDRO);
	    break;

	case 2:
	    path = SNOW_STATION_URL + "?format=json";
	    variableType = "fk_id_punto_misura_nivo";
	    variableNames = PIEMONTE_Variable.values(PiemonteStationType.SNOW);
	    break;

	default:
	    break;
	}

	
	String res = client.getStations(path);
	//Optional<String> response = downloader.downloadString(requestURL);

	Set<String> varPrint = new HashSet<String>();
	
	if (res != null) {

	    JSONObject object = new JSONObject(res);

	    JSONArray results = object.optJSONArray("results");

	    if (results != null) {

		maxRecords = results.length();

		getSetting().getMaxRecords().ifPresent(v -> maxRecords = (maxRecords < v)? maxRecords : v);

		for (int i = 0; i < maxRecords; i++) {

		    JSONObject originalMetadataInfo = results.getJSONObject(i);

		    JSONObject sensorInfo;
		    if (getVariableField.isEmpty()) {
			
			// snow case
			for (PIEMONTE_Variable s : variableNames) {
			    ret.addRecord(HISCentralPiemonteMapper.create(originalMetadataInfo, variableType, s.name()));
			    countDataset++;

			}
			// ret.addRecord(HISCentralPiemonteMapper.create(originalMetadataInfo, null, variableType));

		    } else {
			// meteo - hydro case
			JSONArray variables = originalMetadataInfo.optJSONArray(getVariableField);
			List<PIEMONTE_Variable> varList = new ArrayList<PIEMONTE_Variable>();
			
			for (int k = 0; k < variables.length(); k++) {
			    String paramId = variables.getJSONObject(k).optString("id_parametro");
			    varPrint.add(paramId);
			    if (paramId.equals("IDRO")) {
				varList.add(PIEMONTE_Variable.IDRO);
			    }
			    if (paramId.equals("PORTATA")) {
				varList.add(PIEMONTE_Variable.PORTATA);
			    }
			    if (paramId.equals("PLUV")) {
				varList.add(PIEMONTE_Variable.PTOT);
			    }
			    if (paramId.equals("TERMA")) {
				varList.add(PIEMONTE_Variable.TMAX);
				varList.add(PIEMONTE_Variable.TMIN);
				varList.add(PIEMONTE_Variable.TMEDIA);
			    }
			    if (paramId.equals("IGRO")) {
				varList.add(PIEMONTE_Variable.UMIN);
				varList.add(PIEMONTE_Variable.UMAX);
				varList.add(PIEMONTE_Variable.UMMEDIA);
			    }
			    if (paramId.equals("VELV") || paramId.equals("VELS")) {
				varList.add(PIEMONTE_Variable.VMEDIA);
				varList.add(PIEMONTE_Variable.VRAFFICA);
			    }
			    if (paramId.equals("RADD")) {
				varList.add(PIEMONTE_Variable.RADD);
				// varList.add(PIEMONTE_METEOVariable.VRAFFICA);
			    }

			}
			

			// varList.contains(PIEMONTE_HYDROVariable.IDRO);
			for (PIEMONTE_Variable p : variableNames) {
			    if (varList.contains(p)) {
				ret.addRecord(HISCentralPiemonteMapper.create(originalMetadataInfo, variableType, p.name()));
				countDataset++;
			    }
			}

		    }

		}
	    }
	}
	GSLoggerFactory.getLogger(getClass()).info("VAR_LIST:");
	varPrint.forEach(System.out::println);
	Optional<Integer> mr = getSetting().getMaxRecords();
	// metadataTemplate = null;
	page = page + 1;
	if (page > 2) {
	    ret.setResumptionToken(null);
	    // GSLoggerFactory.getLogger(getClass()).info("Dataset with time interval: {}", countTimeDataset);
	    GSLoggerFactory.getLogger(getClass()).info("Total number of dataset: {}", countDataset);
	} else {
	    ret.setResumptionToken(String.valueOf(page));
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("arpa.piemonte.it");
    }

    @Override
    protected HISCentralPiemonteConnectorSetting initSetting() {

	return new HISCentralPiemonteConnectorSetting();
    }
}
