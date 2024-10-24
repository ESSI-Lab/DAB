/**
 * 
 */
package eu.essi_lab.profiler.csw;

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

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;

/**
 * Designed for the GEOSS Web Portal
 * 
 * @author Fabrizio
 */
public class CSWISOGeoProfiler extends CSWProfiler {

    public static final ProfilerSetting CSW_ISO_GEO_SERVICE_INFO = new ProfilerSetting();
    static {
	CSW_ISO_GEO_SERVICE_INFO.setServiceName("CSW ISO-GEO");
	CSW_ISO_GEO_SERVICE_INFO.setServiceType(CSW_PROFILER_TYPE);
	CSW_ISO_GEO_SERVICE_INFO.setServicePath("cswisogeo");
	CSW_ISO_GEO_SERVICE_INFO.setServiceVersion("2.0.2");
    }

    @Override
    protected ProfilerSetting initSetting() {

	return CSW_ISO_GEO_SERVICE_INFO;
    }
}
