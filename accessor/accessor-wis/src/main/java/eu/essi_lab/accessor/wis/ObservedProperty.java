package eu.essi_lab.accessor.wis;

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

public class ObservedProperty {

    private String name;
    private String units;

    public ObservedProperty(String name, String units) {
	super();
	this.name = name;
	this.units = units;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ObservedProperty) {
	    ObservedProperty op = (ObservedProperty) obj;
	    return op.getName().equals(getName()) && op.getUnits().equals(getUnits());
	}
	return super.equals(obj);
    }

    @Override
    public int hashCode() {
	return (name + units).hashCode();
    }

}