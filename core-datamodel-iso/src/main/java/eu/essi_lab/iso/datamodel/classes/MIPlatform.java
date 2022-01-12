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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIPlatformType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;
public class MIPlatform extends ISOMetadata<MIPlatformType> {

    public MIPlatform(MIPlatformType type) {

	super(type);
    }

    public MIPlatform(InputStream stream) throws JAXBException {

	super(stream);
    }

    public MIPlatform() {

	this(new MIPlatformType());
    }

    /**
     * @XPathDirective(target = "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public void setMDIdentifierCode(String code) {

	MDIdentifierPropertyType pType = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	if (type.getIdentifier() != null) {
	    pType = type.getIdentifier();
	    mdIdentifierType = pType.getMDIdentifier().getValue();
	}

	mdIdentifierType.setCode(createCharacterStringPropertyType(code));
	pType.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));

	type.setIdentifier(pType);
    }

    /**
     * 
     */
    public void setMDIdentifierAuthority(Citation citation) {

	MDIdentifierPropertyType pType = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	if (type.getIdentifier() != null) {
	    pType = type.getIdentifier();
	    mdIdentifierType = pType.getMDIdentifier().getValue();
	}
	CICitationPropertyType citationProperty = null;
	if (citation != null) {
	    citationProperty = new CICitationPropertyType();
	    citationProperty.setCICitation(citation.getElementType());
	    mdIdentifierType.setAuthority(citationProperty);
	}
	mdIdentifierType.setAuthority(citationProperty);

	pType.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));

	type.setIdentifier(pType);
    }

    /**
     * @XPathDirective(target = "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public String getMDIdentifierCode() {

	try {
	    return getStringFromCharacterString(type.getIdentifier().getMDIdentifier().getValue().getCode());
	} catch (NullPointerException e) {
	}
	return null;

    }

    public Citation getMDIdentifierAuthority() {

	try {
	    return new Citation(type.getIdentifier().getMDIdentifier().getValue().getAuthority().getCICitation());
	} catch (NullPointerException e) {
	}
	return null;

    }

    public void setCitation(Citation citation) {

	if (citation == null) {
	    type.setCitation(null);
	} else {
	    List<CICitationPropertyType> list = new ArrayList<>();
	    CICitationPropertyType citationProperty = new CICitationPropertyType();
	    citationProperty.setCICitation(citation.getElementType());
	    list.add(citationProperty);
	    type.setCitation(list);
	}

    }

    public Citation getCitation() {

	try {
	    List<CICitationPropertyType> list = type.getCitation();
	    if (list == null || list.isEmpty()) {
		return null;
	    }
	    return new Citation(list.get(0).getCICitation());
	} catch (NullPointerException e) {
	}
	return null;

    }

    /**
     * Sets the description: narrative description of the platform supporting the instrument
     */
    public void setDescription(String desc) {

	type.setDescription(createCharacterStringPropertyType(desc));
    }

    /**
     * Gets the description: narrative description of the platform supporting the instrument
     */
    public String getDescription() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getDescription());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    @Override
    public JAXBElement<MIPlatformType> getElement() {

	JAXBElement<MIPlatformType> element = ObjectFactories.GMI().createMIPlatform(type);
	return element;
    }
}
