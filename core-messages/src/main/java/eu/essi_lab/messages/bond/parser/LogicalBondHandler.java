package eu.essi_lab.messages.bond.parser;

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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;

/**
 * Handles the {@link LogicalBond}s events sent by a {@link BondParser}
 * 
 * @author Fabrizio
 */
public interface LogicalBondHandler {

    /**
     * Receives notification of the start of a {@link LogicalBond}
     *
     * @param bond
     */
    public void startLogicalBond(LogicalBond bond);

    /**
     * Receives notification of a {@link LogicalBond} operands separator
     *
     * @param bond
     */
    public void separator();
    
    /**
     * Receives notification of a non {@link LogicalBond}
     *
     * @param bond
     */
    public void nonLogicalBond(Bond bond);

    /**
     * Receives notification of the end of a {@link LogicalBond}
     *
     * @param bond
     */
    public void endLogicalBond(LogicalBond bond);

}
