package eu.essi_lab.request.executor;

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

import eu.essi_lab.authorization.MessageAuthorizer;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 * @param <M> the type of the incoming {@link RequestMessage}
 * @param <I> the type provided as parameter to the {@link MessageResponse} <code>MR</code>
 * @param <CR> the type to provide as result of the {@link #count(RequestMessage)} operation
 * @param <MR> the type to provide as result of the {@link #retrieve(RequestMessage)} operation
 */
public interface IRequestExecutor<M extends RequestMessage, I, CR extends AbstractCountResponse, MR extends MessageResponse<I, CR>> extends MessageAuthorizer<M>{

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public CR count(M message) throws GSException;

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public MR retrieve(M message) throws GSException;
}
