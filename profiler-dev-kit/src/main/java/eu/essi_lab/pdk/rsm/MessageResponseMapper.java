package eu.essi_lab.pdk.rsm;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.pdk.handler.ProfilerHandler;

/**
 * This mapper provides a "one to one" mapping of a {@link MessageResponse} of resources having the type <code>I</code>,
 * to a {@link MessageResponse} of resources having the type <code>O</code>. The mapping is performed
 * according to the {@link #getMappingSchema()}.<br>
 * This component is part of the {@link ProfilerHandler} composition and the calling of
 * {@link #map(RequestMessage, MessageResponse)} method is the third step of the workflow.
 * <br>
 * The {@link MessageResponse} of <code>O</code> returned by the {@link #map(DiscoveryMessage, GSResourceResultSet)}
 * method has the same properties of the original {@link MessageResponse} of <code>I</code> but as consequence of the
 * mapping, the results list contains mapped resources instead of the original ones.
 * <br>
 * The {@link #getMappingSchema()} is also used by the {@link DiscoveryResultSetMapperFactory} to load one or more
 * mappers which satisfy the given mapping properties
 * 
 * @author Fabrizio
 * @param <M> the type of the incoming {@link RequestMessage}
 * @param <I> the type of the resources provided by <code>IN</code>
 * @param <O> the type of the resources provided by <code>OUT</code>
 * @param <CR> the type of the {@link AbstractCountResponse} provided as parameter to <code>IN</code> and
 *        <code>OUT</code>
 * @param <IN> the type of {@link MessageResponse} provided as input to the
 *        {@link #map(RequestMessage, MessageResponse)} operation
 * @param <OUT> the type of the {@link MessageResponse} generated as result of
 *        {@link #map(RequestMessage, MessageResponse)} operation
 */
public interface MessageResponseMapper<//
	M extends RequestMessage, //
	I, //
	O, //
	CR extends AbstractCountResponse, //
	IN extends MessageResponse<I, CR>, //
	OUT extends MessageResponse<O, CR>> extends Pluggable {

    /**
     * Provides a "one to one" mapping of a {@link MessageResponse} resources according to a mapping schema
     * 
     * @param message
     * @param messageResponse the {@link MessageResponse} to map
     * @return the mapped {@link MessageResponse}
     * @throws GSException
     */
    public OUT map(M message, IN messageResponse) throws GSException;

    /**
     * Returns the mapping schema also used by the factories to load one or more
     * mappers which satisfy the given schema properties
     * 
     * @return a non <code>null</code> {@link MappingSchema}
     */
    public MappingSchema getMappingSchema();

}
