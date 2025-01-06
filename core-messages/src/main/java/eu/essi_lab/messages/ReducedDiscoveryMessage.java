package eu.essi_lab.messages;

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

import eu.essi_lab.messages.bond.Bond;

/**
 * The reduced discovery message is a decorator for a discovery message that delegates all the methods to the original
 * message and adds the following new payloads:
 * <ul>
 * <li>Reduced bond of type {@link Bond}</li>
 * </ul>
 * 
 * @author boldrini
 */
public class ReducedDiscoveryMessage extends DiscoveryMessage {

    private Bond reducedBond;

    public ReducedDiscoveryMessage(DiscoveryMessage message, Bond reducedBond) {
	header = message.getHeader();
	payload = message.getPayload();
	setReducedBond(reducedBond);
    }

    /**
     * Used by the query submitter and the distributed accessor to get the reduced bond (that is a complex bond reduced
     * for a given source S1 so that it doesn't contain source identifiers bonds).
     * This is previously computed by the Distributor component extracting it from the normalized bond.
     * 
     * @return
     * @see IQueryInitializer
     */
    public Bond getReducedBond() {

	return reducedBond;
    }

    /**
     * The Distributor component uses this method to set the reduced bond for a specific submitter.
     * 
     * @param bond
     * @see IQueryInitializer
     */
    public void setReducedBond(Bond bond) {

	this.reducedBond = bond;
    }

}
