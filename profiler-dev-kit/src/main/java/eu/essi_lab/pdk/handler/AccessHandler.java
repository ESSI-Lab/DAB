package eu.essi_lab.pdk.handler;

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

import java.util.ServiceLoader;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IRequestExecutor;

/**
 * Overrides the superclass in order to provide an implementation specific for access requests
 * 
 * @param <T> the type of the {@link ResultSet} to map and format (see {@link MessageResponseMapper} and
 *        {@link MessageResponseFormatter})
 * @author Fabrizio
 */
public class AccessHandler<T> extends ProfilerHandler<AccessMessage, DataObject, T, CountSet, ResultSet<DataObject>, ResultSet<T>> {

    public AccessHandler() {

	super();
    }

    @Override
    protected IRequestExecutor<AccessMessage, DataObject, CountSet, ResultSet<DataObject>> createExecutor() {

	ServiceLoader<IAccessExecutor> loader = ServiceLoader.load(IAccessExecutor.class);
	for (IAccessExecutor e : loader) {

	    return e;
	}

	return null;
    }
}
