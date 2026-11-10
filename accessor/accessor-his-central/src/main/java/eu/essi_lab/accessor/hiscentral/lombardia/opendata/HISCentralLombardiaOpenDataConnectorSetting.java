package eu.essi_lab.accessor.hiscentral.lombardia.opendata;


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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataConnectorSetting extends HarvestedConnectorSetting {

    public static final String LOMBARDIA_WFS_ENDPOINT = "LOMBARDIA_WFS_ENDPOINT";
    public static final String LOMBARDIA_APP_TOKEN = "LOMBARDIA_APP_TOKEN";

    private static final int DEFAULT_PAGE_SIZE = 50;

    public HISCentralLombardiaOpenDataConnectorSetting() {

	setPageSize(DEFAULT_PAGE_SIZE);

	addOption(StringOptionBuilder.get().//
		withKey(LOMBARDIA_WFS_ENDPOINT).//
		withLabel("ARPA Lombardia WFS endpoint, publishing rating curves and hydrometric zeros (empty to skip them)").//
		withValue(LombardiaWFSClient.DEFAULT_ENDPOINT).//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(LOMBARDIA_APP_TOKEN).//
		withLabel("Open data portal application token (sent as X-App-Token header, to avoid throttling)").//
		withValue("").//
		cannotBeDisabled().//
		build());
    }

    public HISCentralLombardiaOpenDataConnectorSetting(JSONObject object) {

	super(object);
    }

    public HISCentralLombardiaOpenDataConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the WFS endpoint, empty if rating curves and hydrometric zeros are not to be harvested
     */
    public String getWFSEndpoint() {

	return getOption(LOMBARDIA_WFS_ENDPOINT, String.class).map(Option::getValue).orElse("");
    }

    public String getAppToken() {

	return getOption(LOMBARDIA_APP_TOKEN, String.class).map(Option::getValue).orElse("");
    }

    @Override
    protected String initConnectorType() {

	return HISCentralLombardiaOpenDataConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "SIR Lombardia Open Data Connector settings";
    }
}
