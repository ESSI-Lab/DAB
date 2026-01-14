package eu.essi_lab.iso.datamodel.classes;

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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gmd.v_20060504.MDFormatType;

/**
 * MD_Format
 * 
 * @author Fabrizio
 */
public class Format extends ISOMetadata<MDFormatType> {

    public Format(MDFormatType type) {

	super(type);
    }

    public Format(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Format() {

	this(new MDFormatType());
    }

    /**
     * @XPathDirective(target = "gmd:name/gco:CharacterString")
     * @param name
     */
    public void setName(String name) {

	type.setName(createCharacterStringPropertyType(name));
    }

    /**
     * @XPathDirective(target = "gmd:name/gco:CharacterString")
     * @return
     */
    public String getName() {

	try {
	    return getStringFromCharacterString(type.getName());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:version/gco:CharacterString")
     * @param version
     */
    public void setVersion(String version) {

	type.setVersion(createCharacterStringPropertyType(version));
    }

    /**
     * @XPathDirective(target = "gmd:version/gco:CharacterString")
     * @return
     */
    public String getVersion() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getVersion());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:specification/gco:CharacterString")
     * @param specification
     */
    public void setSpecification(String specification) {

	type.setSpecification(createCharacterStringPropertyType(specification));
    }

    /**
     * @XPathDirective(target = "gmd:specification/gco:CharacterString")
     * @return
     */
    public String getSpecification() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getSpecification());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    @Override
    public JAXBElement<MDFormatType> getElement() {

	JAXBElement<MDFormatType> element = ObjectFactories.GMD().createMDFormat(type);
	return element;
    }
}
