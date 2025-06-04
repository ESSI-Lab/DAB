package eu.essi_lab.accessor.dws.client;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.model.resource.InterpolationType;

public class Variable {

    private String abbreviation;

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getLabel() {
        return label;
    }

    private String name;
    
    public String getName() {
        return name;
    }

    private String label;
    
    private String units;
    
    public String getUnits() {
        return units;
    }
    
    private InterpolationType interpolation;
    
    public InterpolationType getInterpolation() {
        return interpolation;
    }

    /**
     * @param dwsClient TODO
     * @param parameter
     * @param description
     */
    public Variable(String name, String abbreviation, String label, String units, InterpolationType interpolation) {
        this.name = name;
	this.abbreviation = abbreviation;
        this.label = label;
        this.units = units;
        this.interpolation = interpolation;
    }



}
