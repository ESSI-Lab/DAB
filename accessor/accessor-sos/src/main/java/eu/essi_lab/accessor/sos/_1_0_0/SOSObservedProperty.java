package eu.essi_lab.accessor.sos._1_0_0;

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

/**
 * @author Fabrizio
 */
public class SOSObservedProperty {

    private String name;
    private String definition;
    private String uom;

    /**
     * @param name
     * @param definition
     * @param uom
     */
    public SOSObservedProperty(String name, String definition, String uom) {
	super();
	this.name = name;
	this.definition = definition;
	this.uom = uom;
    }

    /**
     * @return the name
     */
    public final String getName() {

	return name;
    }

    /**
     * @param name
     */
    public final void setName(String name) {

	this.name = name;
    }

    /**
     * @return the definition
     */
    public final String getDefinition() {

	return definition;
    }

    /**
     * @param definition
     */
    public final void setDefinition(String definition) {

	this.definition = definition;
    }

    /**
     * @return the uom
     */
    public final String getUom() {

	return uom;
    }

    /**
     * @param uom
     */
    public final void setUom(String uom) {

	this.uom = uom;
    }

}
