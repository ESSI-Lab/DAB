package eu.essi_lab.accessor.rarcticnet;

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
public class RArcticNetConnectorSetting extends HarvestedConnectorSetting {

    public static final String R_ARCTICNET_DATASETS = "R_ARCTICNET_DATASETS";
    public static final String R_ARCTICNET_SITES = "R_ARCTICNET_SITES";

    public static final String DEFAULT_DATASETS = "MONTHLY,DAILY";

    /**
     *
     */
    public RArcticNetConnectorSetting() {

	Option<String> datasetsOption = StringOptionBuilder.get().//
		withKey(R_ARCTICNET_DATASETS).//
		withLabel("Comma separated datasets to harvest (MONTHLY = monthly discharge of all the gauges, DAILY = daily discharge"
			+ " of the Russian gauges)").//
		withValue(DEFAULT_DATASETS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(datasetsOption);

	Option<String> sitesOption = StringOptionBuilder.get().//
		withKey(R_ARCTICNET_SITES).//
		withLabel("Comma separated R-ArcticNet PointIDs of the gauges to harvest (empty for all the gauges)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(sitesOption);
    }

    /**
     * @param object
     */
    public RArcticNetConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public RArcticNetConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<RArcticNetDataset> getDatasets() {

	String value = getOption(R_ARCTICNET_DATASETS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_DATASETS);

	List<RArcticNetDataset> ret = new ArrayList<>();
	for (String name : split(value)) {
	    Optional<RArcticNetDataset> dataset = RArcticNetDataset.decode(name);
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

	getOption(R_ARCTICNET_DATASETS, String.class).get().setValue(datasets);
    }

    /**
     * @return the PointIDs of the gauges to harvest, empty for all
     */
    public List<String> getSites() {

	return split(getOption(R_ARCTICNET_SITES, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param sites
     */
    public void setSites(String sites) {

	getOption(R_ARCTICNET_SITES, String.class).get().setValue(sites);
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

	return RArcticNetConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "R-ArcticNet Connector settings";
    }
}
