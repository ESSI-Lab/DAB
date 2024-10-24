package eu.essi_lab.profiler.os.handler.discover.eiffel;

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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;

/**
 * @author Fabrizio
 */
public interface EiffelMapper {

    /**
     * @param message
     * @param resultSet
     * @param sortedIds
     * @return
     * @throws GSException
     */
    public default ResultSet<String> map(//	  
	    DiscoveryMessage message, //
	    ResultSet<GSResource> resultSet, //
	    List<String> sortedIds) throws GSException {

	ResultSet<String> mappedResSet = new ResultSet<String>(resultSet);

	List<String> out = new ArrayList<String>();
	mappedResSet.setResultsList(out);

	for (String id : sortedIds) {

	    for (GSResource res : resultSet.getResultsList()) {

		if (res.getPublicId().equals(id)) {

		    res.getPropertyHandler().setIsGDC(true);

		    try {
			String value = getMapper().map(message, res);
			out.add(value);
		    } catch (GSException e) {

			throw e;
		    }
		}
	    }
	}

	return mappedResSet;
    }

    /**
     * 
     * @return
     */
    public DiscoveryResultSetMapper<String> getMapper();   
}
