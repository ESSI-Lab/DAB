package eu.essi_lab.profiler.pubsub;

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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.pubsub.handler.SubscribeHandler;
import eu.essi_lab.profiler.pubsub.handler.SubscriptionsHandler;
import eu.essi_lab.profiler.pubsub.handler.UnsubscribeHandler;

/**
 * @author Fabrizio
 */
public class PubSubProfiler extends Profiler {

    static final String PUB_SUB_PROFILER_TYPE = "PUB-SUB";

    public static final ProfilerSetting PUB_SUB_SERVICE_INFO = new ProfilerSetting();
    static {
	PUB_SUB_SERVICE_INFO.setServiceName("PubSub");
	PUB_SUB_SERVICE_INFO.setServiceType(PUB_SUB_PROFILER_TYPE);
	PUB_SUB_SERVICE_INFO.setServicePath("pubsub");
	PUB_SUB_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/subscribe"), new SubscribeHandler());
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/unsubscribe"), new UnsubscribeHandler());
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/subscriptions"), new SubscriptionsHandler());

	return selector;
    }

    @Override
    protected ProfilerSetting initSetting() {

	return PUB_SUB_SERVICE_INFO;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	return null;
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return null;
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	return null;
    }

}
