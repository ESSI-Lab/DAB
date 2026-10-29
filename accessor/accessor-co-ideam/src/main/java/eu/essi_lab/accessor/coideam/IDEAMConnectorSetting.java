package eu.essi_lab.accessor.coideam;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class IDEAMConnectorSetting extends HarvestedConnectorSetting {

    public static final String IDEAM_DATASETS = "IDEAM_DATASETS";
    public static final String IDEAM_STATIONS = "IDEAM_STATIONS";
    public static final String IDEAM_APP_TOKEN = "IDEAM_APP_TOKEN";

    /**
     * Precipitation is not included by default, since its dataset is so large that the aggregation queries over the whole
     * dataset may time out
     */
    public static final String DEFAULT_DATASETS = "NIVEL,NIVEL_MIN,NIVEL_MAX";

    /**
     *
     */
    public IDEAMConnectorSetting() {

	Option<String> datasetsOption = StringOptionBuilder.get().//
		withKey(IDEAM_DATASETS).//
		withLabel("Comma separated datasets to harvest (NIVEL = instantaneous water level, NIVEL_MIN = hourly minimum water level,"
			+ " NIVEL_MAX = hourly maximum water level, PRECIPITACION = 10 minutes precipitation)").//
		withValue(DEFAULT_DATASETS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(datasetsOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(IDEAM_STATIONS).//
		withLabel("Comma separated station codes to harvest, e.g. 0021209920 (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);

	Option<String> tokenOption = StringOptionBuilder.get().//
		withKey(IDEAM_APP_TOKEN).//
		withLabel("Optional Socrata application token (raises the throttling limits of datos.gov.co)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(tokenOption);
    }

    /**
     * @param object
     */
    public IDEAMConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public IDEAMConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<IDEAMDataset> getDatasets() {

	String value = getOption(IDEAM_DATASETS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_DATASETS);

	List<IDEAMDataset> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<IDEAMDataset> dataset = IDEAMDataset.decode(code);
	    if (dataset.isPresent() && !ret.contains(dataset.get())) {
		ret.add(dataset.get());
	    }
	}

	return ret;
    }

    /**
     * @param datasets
     */
    public void setDatasets(String datasets) {

	getOption(IDEAM_DATASETS, String.class).get().setValue(datasets);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(IDEAM_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(IDEAM_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @return the application token, or null if not set
     */
    public String getAppToken() {

	return getOption(IDEAM_APP_TOKEN, String.class).//
		map(Option::getValue).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		orElse(null);
    }

    /**
     * @param token
     */
    public void setAppToken(String token) {

	getOption(IDEAM_APP_TOKEN, String.class).get().setValue(token);
    }

    /**
     * @param value
     * @return
     */
    private static List<String> split(String value) {

	if (value == null) {
	    return new ArrayList<>();
	}

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		collect(Collectors.toList());
    }

    @Override
    protected String initConnectorType() {

	return IDEAMConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "IDEAM Connector settings";
    }
}
