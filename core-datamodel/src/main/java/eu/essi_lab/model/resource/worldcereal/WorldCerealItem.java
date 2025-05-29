package eu.essi_lab.model.resource.worldcereal;

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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;


public class WorldCerealItem {
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String code;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String label;
    
    public WorldCerealItem() {
	//do nothing
    }

    public void setCode(String term) {
	this.code = term;
    }
    
    @XmlTransient
    public String getCode() {
	return code;
    }

    @XmlTransient
    public String getLabel() {
	if (label == null) {
	    return code;
	} else {
	    return label;
	}
    }

    public void setLabel(String label) {
	this.label = label;
    }
    
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof WorldCerealItem))
	    return false;

	WorldCerealItem item = (WorldCerealItem) object;
	return this.label == item.label && //
		this.code.equals(item.code);
    }

    @Override
    public String toString() {

	return "Code: " + getCode() + "\n" + "Label: " + getLabel() + "\n";
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }
    
}
