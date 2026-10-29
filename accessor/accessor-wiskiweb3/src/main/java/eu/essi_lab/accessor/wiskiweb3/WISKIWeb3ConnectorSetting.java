package eu.essi_lab.accessor.wiskiweb3;

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
public class WISKIWeb3ConnectorSetting extends HarvestedConnectorSetting {

    public static final String WISKIWEB3_LAYERS = "WISKIWEB3_LAYERS";
    public static final String WISKIWEB3_FILES = "WISKIWEB3_FILES";
    public static final String WISKIWEB3_STATIONS = "WISKIWEB3_STATIONS";
    public static final String WISKIWEB3_APP_URL = "WISKIWEB3_APP_URL";
    public static final String WISKIWEB3_ORGANISATION = "WISKIWEB3_ORGANISATION";
    public static final String WISKIWEB3_LICENSE = "WISKIWEB3_LICENSE";
    public static final String WISKIWEB3_COUNTRY = "WISKIWEB3_COUNTRY";

    public static final String DEFAULT_FILES = WISKIWeb3Client.WEEK_FILE + "," + WISKIWeb3Client.YEAR_FILE;

    /**
     *
     */
    public WISKIWeb3ConnectorSetting() {

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_LAYERS).//
		withLabel("Comma separated identifiers of the layers to harvest, as listed by internet/layers/index.json (e.g. 10,20,50,60;"
			+ " empty for all the time series layers of the site)")
		.//
		withValue("").//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_FILES).//
		withLabel("Comma separated files to harvest: week (high resolution values of about the last week), year (daily values)").//
		withValue(DEFAULT_FILES).//
		required().//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_STATIONS).//
		withLabel("Comma separated station numbers to harvest (empty for all)").//
		withValue("").//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_APP_URL).//
		withLabel("URL of the web application, read for the organisation and licence (empty for the 'webpublic' directory next to"
			+ " the data directory)")
		.//
		withValue("").//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_ORGANISATION).//
		withLabel("Organisation publishing the data (empty to read it from the web application)").//
		withValue("").//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_LICENSE).//
		withLabel("Licence of the data (empty to read it from the web application configuration)").//
		withValue("").//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(WISKIWEB3_COUNTRY).//
		withLabel("Country of the stations (e.g. Germany)").//
		withValue("").//
		cannotBeDisabled().//
		build());
    }

    /**
     * @param object
     */
    public WISKIWeb3ConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WISKIWeb3ConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the layer identifiers to harvest, empty for all
     */
    public List<String> getLayers() {

	return split(getValue(WISKIWEB3_LAYERS));
    }

    /**
     * @param layers
     */
    public void setLayers(String layers) {

	setValue(WISKIWEB3_LAYERS, layers);
    }

    /**
     * @return the files to harvest
     */
    public List<String> getFiles() {

	List<String> ret = new ArrayList<>();
	for (String file : split(getValue(WISKIWEB3_FILES))) {
	    String normalized = file.toLowerCase().replace(".json", "");
	    if ((normalized.equals(WISKIWeb3Client.WEEK_FILE) || normalized.equals(WISKIWeb3Client.YEAR_FILE))
		    && !ret.contains(normalized)) {
		ret.add(normalized);
	    }
	}

	return ret.isEmpty() ? split(DEFAULT_FILES) : ret;
    }

    /**
     * @param files
     */
    public void setFiles(String files) {

	setValue(WISKIWEB3_FILES, files);
    }

    /**
     * @return the station numbers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getValue(WISKIWEB3_STATIONS));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	setValue(WISKIWEB3_STATIONS, stations);
    }

    /**
     * @return
     */
    public Optional<String> getAppURL() {

	return getOptionalValue(WISKIWEB3_APP_URL);
    }

    /**
     * @param url
     */
    public void setAppURL(String url) {

	setValue(WISKIWEB3_APP_URL, url);
    }

    /**
     * @return
     */
    public Optional<String> getOrganisation() {

	return getOptionalValue(WISKIWEB3_ORGANISATION);
    }

    /**
     * @param organisation
     */
    public void setOrganisation(String organisation) {

	setValue(WISKIWEB3_ORGANISATION, organisation);
    }

    /**
     * @return
     */
    public Optional<String> getLicense() {

	return getOptionalValue(WISKIWEB3_LICENSE);
    }

    /**
     * @param license
     */
    public void setLicense(String license) {

	setValue(WISKIWEB3_LICENSE, license);
    }

    /**
     * @return
     */
    public Optional<String> getCountry() {

	return getOptionalValue(WISKIWEB3_COUNTRY);
    }

    /**
     * @param country
     */
    public void setCountry(String country) {

	setValue(WISKIWEB3_COUNTRY, country);
    }

    /**
     * @param key
     * @return the trimmed value, empty if not set
     */
    private Optional<String> getOptionalValue(String key) {

	String value = getValue(key).trim();
	return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    /**
     * @param key
     * @return
     */
    private String getValue(String key) {

	return getOption(key, String.class).map(Option::getValue).map(v -> v == null ? "" : v).orElse("");
    }

    /**
     * @param key
     * @param value
     */
    private void setValue(String key, String value) {

	getOption(key, String.class).get().setValue(value);
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

	return WISKIWeb3Connector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "KISTERS WISKIweb3 Connector settings";
    }
}
