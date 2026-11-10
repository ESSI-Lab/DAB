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

import eu.essi_lab.adk.harvest.HarvestedAccessor;

/**
 * Accessor of the KISTERS WISKI-Web 3 "webpublic" sites. The source endpoint is the data directory of the site (e.g.
 * https://www.hlnug.de/static/pegel/wiskiweb3/data/ for Hesse)
 *
 * @author boldrini
 */
public class WISKIWeb3Accessor extends HarvestedAccessor<WISKIWeb3Connector> {

    /**
     *
     */
    public static final String TYPE = "WISKIWeb3";

    @Override
    protected String initSourceEndpoint() {

	return WISKIWeb3Client.DEFAULT_ENDPOINT;
    }

    @Override
    protected String initSettingName() {

	return "KISTERS WISKIweb3 Accessor";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected WISKIWeb3ConnectorSetting initHarvestedConnectorSetting() {

	return new WISKIWeb3ConnectorSetting();
    }
}
