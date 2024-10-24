package eu.essi_lab.accessor.whos.sigedac;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
 * @author boldrini
 */
public class SIGEDACAccessor extends HarvestedAccessor<SIGEDACConnector> {

    private static final String TYPE = "SIGEDACAccessor";

    @Override
    protected String initSettingName() {

	return "Paraguay SIGEDAC Accessor";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected SIGEDACConnectorSetting initHarvestedConnectorSetting() {

	return new SIGEDACConnectorSetting();
    }
}
