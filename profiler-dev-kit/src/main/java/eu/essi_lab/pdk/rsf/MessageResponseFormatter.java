package eu.essi_lab.pdk.rsf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import javax.ws.rs.core.Response;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.ProfilerHandler;

/**
 * Formats a {@link MessageResponse} with resources of type <code>T</code> in order to provide a valid
 * {@link Response} entity, according to the related {@link #getEncoding()}.<br>
 * In addition to the resources, all the {@link MessageResponse} properties can be formatted, including
 * {@link MessageResponse#getExceptions()}, and the {@link MessageResponse#getCountResponse()} properties.<br>
 * This component is part of the {@link ProfilerHandler} composition and the calling of
 * {@link #format(DiscoveryMessage, MessageResponse)} method is the fourth and last step of the workflow
 * <br>
 * 
 * @see DiscoveryHandler
 * @see DiscoveryHandler#setMessageResponseFormatter(MessageResponseFormatter)
 * @see DiscoveryHandler#getMessageResponseFormatter()
 * @author Fabrizio
 * @param <M> the type of the incoming {@link RequestMessage}
 * @param <T> the type of the resources provided by <code>MR</code>
 * @param <CR> the type of the {@link AbstractCountResponse} handled by <code>MR</code>
 * @param <MR> the type of the {@link MessageResponse} provided as input of the
 *        {@link #format(RequestMessage, MessageResponse)} operation
 */
public interface MessageResponseFormatter<//
	M extends RequestMessage, //
	T, //
	CR extends AbstractCountResponse, //
	MR extends MessageResponse<T, CR>> extends Pluggable {

    /**
     * Formats the resources of the given {@link MessageResponse} of type <code>T</code> in order to provide a valid
     * {@link Response} entity, according to the related {@link DABPRofiler} implementation. In addition to the
     * resources, all the {@link MessageResponse} properties can be formatted, including:
     * <ul>
     * <li>{@link MessageResponse#getException()}</li>
     * <li>{@link MessageResponse#getCountResponse()} properties</li>
     * </ul>
     * In addition to the above {@link MessageResponse} properties, a formatter can also include {@link RequestMessage}
     * properties (for example
     * {@link RequestMessage#getException()} in order to show errors and/or warnings occurred during the first phases
     * of the {@link DiscoveryHandler} workflow)
     * <br>
     * 
     * @param message
     * @param messageResponse
     * @return
     * @throws GSException
     */
    public Response format(M message, MR messageResponse) throws GSException;

    /**
     * A {@link FormattingEncoding} which describes this formatting
     * 
     * @return a non <code>null</code> {@link FormattingEncoding}
     */
    public FormattingEncoding getEncoding();
}
