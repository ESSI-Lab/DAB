package eu.essi_lab.pdk.rsm;

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

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * Implementation specific to map a <code>ResultSet&ltDataObject&gt</code> in to a <code>ResultSet&ltT&gt</code>
 * 
 * @author Fabrizio
 * @param <T> the type to which to map the {@link DataObject}s of the response (e.g.: String, JSON, XML, etc.)
 */
public abstract class AccessResultSetMapper<T>
	implements MessageResponseMapper<AccessMessage, DataObject, T, CountSet, ResultSet<DataObject>, ResultSet<T>> {

    @Override
    public ResultSet<T> map(AccessMessage message, ResultSet<DataObject> resultSet) throws GSException {

	//
	//
	// converts the incoming ResultSet<DataObject> in a ResultSet<T>
	//
	//
	ResultSet<T> mappedResSet = new ResultSet<T>(resultSet);

	List<T> out = new ArrayList<T>();
	mappedResSet.setResultsList(out);

	for (DataObject res : resultSet.getResultsList()) {
	    try {
		T value = map(message, res);
		out.add(value);
	    } catch (GSException e) {

		throw e;
	    }
	}

	return mappedResSet;
    }

    /**
     * @param message
     * @param resource
     * @return
     * @throws GSException
     */
    public abstract T map(AccessMessage message, DataObject resource) throws GSException;

}
