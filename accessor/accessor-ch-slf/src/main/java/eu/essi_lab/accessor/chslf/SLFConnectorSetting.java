package eu.essi_lab.accessor.chslf;

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
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class SLFConnectorSetting extends HarvestedConnectorSetting {

    public final String SLF_STATION_CODES = "SLF_STATION_CODES";

    /**
     *
     */
    public SLFConnectorSetting() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(SLF_STATION_CODES).//
		withLabel("Comma separated station codes (empty for all stations)").//
		withValue("").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @return
     */
    public List<String> getStationCodes() {

	List<String> ret = new ArrayList<>();

	String value = getOption(SLF_STATION_CODES, String.class).get().getValue();
	if (value == null) {
	    return ret;
	}

	for (String code : value.split(",")) {
	    if (!code.trim().isEmpty()) {
		ret.add(code.trim());
	    }
	}

	return ret;
    }

    /**
     * @param object
     */
    public SLFConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SLFConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return SLFConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "SLF Connector settings";
    }
}
