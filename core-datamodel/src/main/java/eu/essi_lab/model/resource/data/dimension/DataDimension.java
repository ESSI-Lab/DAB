package eu.essi_lab.model.resource.data.dimension;

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

import java.io.Serializable;
import java.util.Objects;

import eu.essi_lab.model.resource.data.DimensionType;

/**
 * The generic data dimension: this object represents a dimension axis for a given data object. It is defined by a name
 * and a type. subclasses specializes with different properties depending on axis regularity etc.:
 * {@link DataDimension} children:
 * {@link FiniteDimension} dimension defined by an ordered finite list of points
 * {@link ContinueDimension} dimension defined by its limits and (for regular dimensions) resolution/size
 * 
 * @author boldrini
 */
public abstract class DataDimension implements Serializable{

    private String name;
    private DimensionType type;    

    DataDimension() {

    }

    DataDimension(String name) {

	setName(name);
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public DimensionType getType() {
	return type;
    }

    public void setType(DimensionType type) {
	this.type = type;
    }

    /**
     * @param n1
     * @param n2
     * @return
     */
    public static boolean equals(Number n1, Number n2) {
	if (n1 == null && n2 == null) {
	    return true;
	}
	if (n1 != null && n2 != null) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    if (Math.abs(d1 - d2) < Math.pow(10, -10)) {
		return true;
	    }
	}
	return false;
    }

    public boolean equals(Object obj) {

	if (obj instanceof DataDimension) {

	    DataDimension dim = (DataDimension) obj;

	    if (Objects.equals(type, dim.type) && //
		    Objects.equals(name, dim.name)) {
		return true;
	    }
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	return getName() + " (" + getType() + ")";
    }

    @Override
    public abstract DataDimension clone();

    public ContinueDimension getContinueDimension() {
	if (this instanceof ContinueDimension) {
	    return (ContinueDimension) this;
	}
	return null;
    }

    public FiniteDimension getFiniteDimension() {
	if (this instanceof FiniteDimension) {
	    return (FiniteDimension) this;
	}
	return null;
    }

}
