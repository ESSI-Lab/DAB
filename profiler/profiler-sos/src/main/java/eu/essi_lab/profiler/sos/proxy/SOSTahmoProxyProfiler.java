package eu.essi_lab.profiler.sos.proxy;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.sos.SOSProfiler;
import eu.essi_lab.profiler.sos.observation.GetObservationRequestFilter;

public class SOSTahmoProxyProfiler extends SOSProfiler<SOSTahmoProxyProfilerSetting> {

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
    protected SOSTahmoProxyProfilerSetting initSetting() {

	return new SOSTahmoProxyProfilerSetting();
    }
}
