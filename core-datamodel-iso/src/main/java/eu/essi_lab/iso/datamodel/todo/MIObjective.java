package eu.essi_lab.iso.datamodel.todo;

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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIObjectiveType;

public class MIObjective extends ISOMetadata<MIObjectiveType> {
    public MIObjective(InputStream stream) throws JAXBException {

	super(stream);
    }

    public MIObjective() {

	this(new MIObjectiveType());
    }

    public MIObjective(MIObjectiveType type) {

	super(type);
    }

    public JAXBElement<MIObjectiveType> getElement() {

	JAXBElement<MIObjectiveType> element = ObjectFactories.GMI().createMIObjective(type);
	return element;
    }
    /**
    *    @XPathDirective(target = "gmi:function/gco:CharacterString")
    */
    void setFunction(String funct) {
    }

    /**
    *    @XPathDirective(target = "gmi:function/gco:CharacterString")
    */
    String getFunction() {
	return null;
    }
}
