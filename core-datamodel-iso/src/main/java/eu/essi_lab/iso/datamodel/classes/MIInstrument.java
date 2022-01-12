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
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIInstrumentType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;

public class MIInstrument extends ISOMetadata<MIInstrumentType> {
    public MIInstrument(InputStream stream) throws JAXBException {

	super(stream);
    }

    public MIInstrument() {

	this(new MIInstrumentType());
    }

    public MIInstrument(MIInstrumentType type) {

	super(type);
    }

    public JAXBElement<MIInstrumentType> getElement() {

	JAXBElement<MIInstrumentType> element = ObjectFactories.GMI().createMIInstrument(type);
	return element;
    }
    public String getDescription() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getDescription());
	} catch (Exception ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmi:description/gco:CharacterString")
     */
    public String setDescription(String description) {
	try {
	    type.setDescription(createCharacterStringPropertyType(description));
	} catch (Exception ex) {
	}
	return null;
    }

    /**
     * 
     */
    public void setMDIdentifierTypeIdentifier(String id) {

	MDIdentifierPropertyType identifierPropertyType = type.getIdentifier();
	if (identifierPropertyType != null) {
	    JAXBElement<? extends MDIdentifierType> mdIdentifier = identifierPropertyType.getMDIdentifier();
	    if (mdIdentifier != null) {
		MDIdentifierType mdIdentifierType = mdIdentifier.getValue();
		if (mdIdentifierType != null) {
		    mdIdentifierType.setId(id);
		    return;
		}
	    }
	}

	MDIdentifierPropertyType identifier = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	mdIdentifierType.setId(id);
	identifier.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));

	type.setIdentifier(identifier);
    }

    /**
     * @XPathDirective(target = "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public void setMDIdentifierTypeCode(String code) {

	MDIdentifierPropertyType identifierPropertyType = type.getIdentifier();
	if (identifierPropertyType != null) {
	    JAXBElement<? extends MDIdentifierType> mdIdentifier = identifierPropertyType.getMDIdentifier();
	    if (mdIdentifier != null) {
		MDIdentifierType mdIdentifierType = mdIdentifier.getValue();
		if (mdIdentifierType != null) {
		    mdIdentifierType.setCode(createCharacterStringPropertyType(code));
		    return;
		}
	    }
	}

	MDIdentifierPropertyType identifier = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	mdIdentifierType.setCode(createCharacterStringPropertyType(code));
	identifier.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));

	type.setIdentifier(identifier);
    }

    /**
     * 
     */
    public MDIdentifierType getMDIdentifierType() {

	try {
	    return type.getIdentifier().getMDIdentifier().getValue();
	} catch (Throwable t) {
	}
	return null;
    }

    public String getMDIdentifierAuthority() {

	return getStringFromCharacterString(type.getIdentifier().getMDIdentifier().getValue().getAuthority().getCICitation().getTitle());
    }

    /**
     * @XPathDirective(target = "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public String getMDIdentifierCode() {

	try {
	    return getStringFromCharacterString(type.getIdentifier().getMDIdentifier().getValue().getCode());
	} catch (Exception e) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmi:type/gco:CharacterString")
     */
    public void setSensorType(String sensorType) {

	CharacterStringPropertyType charType = type.getType();
	if (charType != null) {
	    charType.setCharacterString(createCharacterStringPropertyType(sensorType).getCharacterString());
	} else {
	    charType = new CharacterStringPropertyType();
	    charType.setCharacterString(createCharacterStringPropertyType(sensorType).getCharacterString());
	    type.setType(charType);
	}
    }

    /**
     * @XPathDirective(target = "gmi:type/gco:CharacterString")
     */
    public String getSensorType() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getType());
	} catch (Exception ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString")
     */
    public void setTitle(String title) {

	List<CICitationPropertyType> citation = type.getCitation();
	if (!citation.isEmpty()) {
	    CICitationPropertyType ciCitationPropertyType = citation.get(0);
	    CICitationType ciCitation = ciCitationPropertyType.getCICitation();
	    if (ciCitation != null) {
		ciCitation.setTitle(createCharacterStringPropertyType(title));
	    } else {
		ciCitation = new CICitationType();
		ciCitation.setTitle(createCharacterStringPropertyType(title));
		ciCitationPropertyType.setCICitation(ciCitation);
	    }
	} else {
	    CICitationPropertyType ciCitationPropertyType = new CICitationPropertyType();
	    CICitationType ciCitation = new CICitationType();
	    ciCitation.setTitle(createCharacterStringPropertyType(title));
	    ciCitationPropertyType.setCICitation(ciCitation);

	    type.getCitation().add(ciCitationPropertyType);
	}
    }

    /**
     * @XPathDirective(target = "gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString")
     */
    public String getTitle() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getCitation().get(0).getCICitation().getTitle());
	} catch (Exception ex) {
	}
	return null;
    }
}
