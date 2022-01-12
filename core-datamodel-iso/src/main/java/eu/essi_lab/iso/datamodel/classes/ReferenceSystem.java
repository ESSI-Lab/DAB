package eu.essi_lab.iso.datamodel.classes;

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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gmd.v_20060504.MDReferenceSystemType;
import net.opengis.iso19139.gmd.v_20060504.RSIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.RSIdentifierType;

public class ReferenceSystem extends ISOMetadata<MDReferenceSystemType> {

    public ReferenceSystem(InputStream stream) throws JAXBException {

	super(stream);
    }

    public ReferenceSystem(MDReferenceSystemType type) {

	super(type);
    }

    public ReferenceSystem() {

	super(new MDReferenceSystemType());
    }

    @Override
    public JAXBElement<MDReferenceSystemType> getElement() {

	JAXBElement<MDReferenceSystemType> element = ObjectFactories.GMD().createMDReferenceSystem(type);
	return element;
    }
    public String getCode() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getReferenceSystemIdentifier().getRSIdentifier().getCode());
	} catch (NullPointerException e) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString")
     */
    public String getCodeSpace() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getReferenceSystemIdentifier().getRSIdentifier().getCodeSpace());
	} catch (NullPointerException e) {
	}
	return null;
    }

    private void initRSIdentifier() {
	if (!type.isSetReferenceSystemIdentifier()) {
	    RSIdentifierPropertyType identifierProperty = new RSIdentifierPropertyType();
	    type.setReferenceSystemIdentifier(identifierProperty);
	}
	if (!type.getReferenceSystemIdentifier().isSetRSIdentifier()) {
	    RSIdentifierType identifierProperty = new RSIdentifierType();
	    type.getReferenceSystemIdentifier().setRSIdentifier(identifierProperty);
	}
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version/gco:CharacterString")
     */
    public String getVersion() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getReferenceSystemIdentifier().getRSIdentifier().getVersion());
	} catch (NullPointerException e) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString")
     */
    public void setCode(String code) {
	initRSIdentifier();
	type.getReferenceSystemIdentifier().getRSIdentifier().setCode(createCharacterStringPropertyType(code));
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString")
     */
    public void setCodeSpace(String codeSpace) {
	initRSIdentifier();
	type.getReferenceSystemIdentifier().getRSIdentifier().setCodeSpace(createCharacterStringPropertyType(codeSpace));
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version/gco:CharacterString")
     */
    public void setVersion(String version) {
	initRSIdentifier();
	type.getReferenceSystemIdentifier().getRSIdentifier().setVersion(createCharacterStringPropertyType(version));
    }
}
