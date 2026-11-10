package eu.essi_lab.accessor.ittrentinofloods;

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
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class TrentinoFloodsConnectorSetting extends HarvestedConnectorSetting {

    public static final String TRENTINO_FLOODS_SERIES = "TRENTINO_FLOODS_SERIES";

    /**
     *
     */
    public TrentinoFloodsConnectorSetting() {

	Option<String> seriesOption = StringOptionBuilder.get().//
		withKey(TRENTINO_FLOODS_SERIES).//
		withLabel("Comma separated series identifiers to harvest, e.g. 32207,42437 (empty for all the series of the index)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(seriesOption);
    }

    /**
     * @param object
     */
    public TrentinoFloodsConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public TrentinoFloodsConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the series identifiers to harvest, empty for all
     */
    public List<String> getSeries() {

	return split(getOption(TRENTINO_FLOODS_SERIES, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param series
     */
    public void setSeries(String series) {

	getOption(TRENTINO_FLOODS_SERIES, String.class).get().setValue(series);
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

	return TrentinoFloodsConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Trentino Floods Connector settings";
    }
}
