package eu.essi_lab.model.configuration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class Subcomponent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1333085790594234006L;
    private String label;
    private String value;

    public Subcomponent() {
    }

    public Subcomponent(String label, String clazz) {
	this.label = label;
	this.value = clazz;
    }

    public String getLabel() {
	return this.label;
    }

    public String getValue() {
	return this.value;
    }

    public void setLabel(String l) {
	this.label = l;
    }

    public void setValue(String ic) {
	this.value = ic;
    }

    @Override
    public boolean equals(Object o) {

	if (!(o instanceof Subcomponent)) {

	    return false;
	}

	Subcomponent sub = (Subcomponent) o;
	return Objects.equals(getValue(), sub.getValue()) && //
		Objects.equals(getLabel(), sub.getLabel());
    }

    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder("label: ");

	builder.append(label != null ? label : "nolabel");

	builder.append(" value: ");

	builder.append(value != null ? value : "novalue");

	return builder.toString();

    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

}
