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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;

/**
 * Extends the super interface in order to receive a {@link DiscoveryMessage} and to format a {@link ResultSet} with
 * resources of type <code>T</code>
 * 
 * @author Fabrizio
 * @param <T> the type of the resources provided by the {@link ResultSet} to format
 */
public abstract class DiscoveryResultSetFormatter<T> implements MessageResponseFormatter<DiscoveryMessage, T, CountSet, ResultSet<T>> {

}
