package eu.essi_lab.pdk.handler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ServiceLoader;

import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.resource.ScheduleReport;
import eu.essi_lab.request.executor.IRequestExecutor;
import eu.essi_lab.request.executor.IScheduleExecutor;

/**
 * @author Fabrizio
 */
public class ScheduleHandler<M extends RequestMessage, I, O, CR extends AbstractCountResponse, IN extends MessageResponse<I, CR>, OUT extends MessageResponse<O, CR>, T>

	extends

	ProfilerHandler<RequestMessage, ScheduleReport, T, CountSet, ResultSet<ScheduleReport>, ResultSet<T>> {

    @SuppressWarnings("unchecked")
    public ScheduleHandler(ProfilerHandler<M, I, O, CR, IN, OUT> workerHandler) {
	super();

	IScheduleExecutor<M, I, CR, IN> scheduleExecutor = (IScheduleExecutor<M, I, CR, IN>) getExecutor();

	String workerHandlerClass = workerHandler.getClass().getName();
	String workerMapperClass = workerHandler.getMessageResponseMapper().getClass().getName();
	String workerFormatterClass = workerHandler.getMessageResponseFormatter().getClass().getName();

	scheduleExecutor.setWorkerHandler(workerHandlerClass, workerMapperClass, workerFormatterClass);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected IRequestExecutor<RequestMessage, ScheduleReport, CountSet, ResultSet<ScheduleReport>> createExecutor() {

	ServiceLoader<IScheduleExecutor> loader = ServiceLoader.load(IScheduleExecutor.class);
	for (IScheduleExecutor e : loader) {

	    return e;
	}

	return null;
    }

}
