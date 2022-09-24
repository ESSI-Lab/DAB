package eu.essi_lab.accessor.sos.proxy;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.accessor.sos.observation.GetObservationRequestFilter;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

public class SOSTahmoProxyProfiler extends SOSProxyProfiler {

    private static final String SOS_TAHMO_PROXY_PROFILER_TYPE = "SOS-TAHMO-PROXY";

    public static final ProfilerSetting SOS_TAHMO_PROXY_SERVICE_INFO = new ProfilerSetting();
    static {
	SOS_TAHMO_PROXY_SERVICE_INFO.setServiceName("Sensor Observation Service TAHMO-PROXY");
	SOS_TAHMO_PROXY_SERVICE_INFO.setServiceType(SOS_TAHMO_PROXY_PROFILER_TYPE);
	SOS_TAHMO_PROXY_SERVICE_INFO.setServicePath("sos-tahmo-proxy");
	SOS_TAHMO_PROXY_SERVICE_INFO.setServiceVersion("2.0");
    }

    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("2.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	SOSTahmoProxyHandler handler = new SOSTahmoProxyHandler();
	selector.register(new GetObservationRequestFilter(), handler);

	return selector;

    }

    @Override
    protected ProfilerSetting initSetting() {

	return SOS_TAHMO_PROXY_SERVICE_INFO;
    }
}
