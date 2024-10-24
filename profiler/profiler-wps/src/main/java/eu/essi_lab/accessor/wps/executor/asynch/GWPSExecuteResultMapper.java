package eu.essi_lab.accessor.wps.executor.asynch;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.ScheduleReport;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;

public class GWPSExecuteResultMapper
	implements MessageResponseMapper<RequestMessage, ScheduleReport, String, CountSet, ResultSet<ScheduleReport>, ResultSet<String>> {

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public ResultSet<String> map(RequestMessage message, ResultSet<ScheduleReport> messageResponse) throws GSException {
	ResultSet<String> ret = new ResultSet<>();
	ret.setResultsList(new ArrayList<>());
	List<ScheduleReport> list = messageResponse.getResultsList();
	for (ScheduleReport scheduleReport : list) {
	    ret.getResultsList().add(scheduleReport.getJobId());
	}
	return ret;
    }

    @Override
    public MappingSchema getMappingSchema() {
	return null;
    }

}
