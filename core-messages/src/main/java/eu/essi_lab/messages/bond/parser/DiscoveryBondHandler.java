package eu.essi_lab.messages.bond.parser;

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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.ComposedElementBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;

/**
 * Handles the {@link Bond}s events sent by the {@link DiscoveryBondParser}
 * 
 * @see DiscoveryBondParser#parse(LogicalBondHandler)
 * @author Fabrizio
 */
public interface DiscoveryBondHandler extends LogicalBondHandler {

    /**
     * Receives notification of a {@link ViewBond}
     *
     * @param bond
     */
    void viewBond(ViewBond bond);

    /**
     * Receives notification of a {@link ResourcePropertyBond}
     *
     * @param bond
     */
    void resourcePropertyBond(ResourcePropertyBond bond);

    /**
     * Receives notification of a custom {@link QueryableBond}
     *
     * @param bond
     */
    void customBond(QueryableBond<String> bond);

    /**
     * Receives notification of a {@link SimpleValueBond}
     *
     * @param bond
     */
    void simpleValueBond(SimpleValueBond bond);

    /**
     * Receives notification of a {@link ComposedElementBond}
     *
     * @param bond
     */
    default void composedElementBond(ComposedElementBond bond) {

    }

    /**
     * Receives notification of a {@link SpatialBond}
     *
     * @param bond
     */
    void spatialBond(SpatialBond bond);

    /**
     * Receives notification of a {@link RuntimeInfoElementBond}
     *
     * @param bond
     */
    void runtimeInfoElementBond(RuntimeInfoElementBond bond);

}
