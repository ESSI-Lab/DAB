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

import eu.essi_lab.adk.harvest.HarvestedAccessor;

/**
 * Accessor for the SLF Measurement API of the WSL Institute for Snow and Avalanche Research SLF (Switzerland)
 *
 * @author boldrini
 */
public class SLFAccessor extends HarvestedAccessor<SLFConnector> {

    /**
     *
     */
    public static final String TYPE = "SLF";

    @Override
    protected String initSourceEndpoint() {

	return SLFClient.DEFAULT_ENDPOINT;
    }

    @Override
    protected String initSettingName() {

	return "SLF Accessor";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected SLFConnectorSetting initHarvestedConnectorSetting() {

	return new SLFConnectorSetting();
    }
}
