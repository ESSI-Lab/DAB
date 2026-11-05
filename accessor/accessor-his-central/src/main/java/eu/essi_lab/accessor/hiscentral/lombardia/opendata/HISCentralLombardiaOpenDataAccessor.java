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

import eu.essi_lab.adk.harvest.HarvestedAccessor;

/**
 * Accessor of the ARPA Lombardia hydro-nivo-meteorological data published on the Regione Lombardia open data portal,
 * with the rating curves and hydrometric zeros published by the ARPA Lombardia WFS
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataAccessor extends HarvestedAccessor<HISCentralLombardiaOpenDataConnector> {

    public static final String TYPE = "SIR_LOMBARDIA_OPEN_DATA";

    @Override
    protected String initSettingName() {

	return "HIS-Central Lombardia Open Data Accessor";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected HISCentralLombardiaOpenDataConnectorSetting initHarvestedConnectorSetting() {

	return new HISCentralLombardiaOpenDataConnectorSetting();
    }
}
