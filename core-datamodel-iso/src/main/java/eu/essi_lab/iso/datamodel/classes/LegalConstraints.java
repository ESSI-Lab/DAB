package eu.essi_lab.iso.datamodel.classes;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDLegalConstraintsType;
import net.opengis.iso19139.gmd.v_20060504.MDRestrictionCodePropertyType;

public class LegalConstraints extends ISOMetadata<MDLegalConstraintsType> {

    public LegalConstraints(InputStream stream) throws JAXBException {

	super(stream);
    }

    public LegalConstraints(MDLegalConstraintsType type) {

	super(type);
    }

    public LegalConstraints() {

	super(new MDLegalConstraintsType());
    }

    @Override
    public JAXBElement<MDLegalConstraintsType> getElement() {

	JAXBElement<MDLegalConstraintsType> element = ObjectFactories.GMD().createMDLegalConstraints(type);
	return element;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:useLimitation/gco:CharacterString", position = Position.LAST)
     * @param useLimitation
     */
    public void addUseLimitation(String useLimitation) {
	type.getUseLimitation().add(createCharacterStringPropertyType(useLimitation));
    }

    /**
     * @XPathDirective(target = ".//gmd:useLimitation/gco:CharacterString")
     * @return
     */
    public String getUseLimitation() {
	if (type.isSetUseLimitation() && !type.getUseLimitation().isEmpty() && type.getUseLimitation().get(0).isSetCharacterString()) {
	    return ISOMetadata.getStringFromCharacterString(type.getUseLimitation().get(0));
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".//gmd:useLimitation/gco:CharacterString")
     * @return
     */
    public Iterator<String> getUseLimitations() {
	List<String> ret = new ArrayList<>();
	if (type.isSetUseLimitation() && !type.getUseLimitation().isEmpty()) {
	    for (CharacterStringPropertyType useLimitation : type.getUseLimitation()) {
		if (useLimitation.isSetCharacterString()) {
		    ret.add(ISOMetadata.getStringFromCharacterString(useLimitation));
		}
	    }
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:useLimitation/gco:CharacterString", position = Position.LAST)
     */
    public void clearUseLimitation() {
	type.unsetUseLimitation();
    }

    /**
     * @XPathDirective(target = "gmd:otherConstraints/gco:CharacterString")
     * @return
     */
    public String getOtherConstraint() {
	if (type.isSetOtherConstraints() && !type.getOtherConstraints().isEmpty()
		&& type.getOtherConstraints().get(0).isSetCharacterString()) {
	    return ISOMetadata.getStringFromCharacterString(type.getOtherConstraints().get(0));
	}
	return null;
    }

    public Iterator<String> getOtherConstraints() {
	List<String> ret = new ArrayList<>();
	if (type.isSetOtherConstraints() && !type.getOtherConstraints().isEmpty()) {
	    for (CharacterStringPropertyType otherConstraints : type.getOtherConstraints()) {
		if (otherConstraints.isSetCharacterString()) {
		    ret.add(ISOMetadata.getStringFromCharacterString(otherConstraints));
		}
	    }
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = "gmd:otherConstraints/gco:CharacterString")
     * @param otherConstraints
     */
    public void addOtherConstraints(String otherConstraints) {
	if (otherConstraints.contains("creativecommons.org/licenses/by/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution 4.0 International (CC BY 4.0)");
	}else if (otherConstraints.contains("creativecommons.org/licenses/by-nc/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)");
	}else if (otherConstraints.contains("creativecommons.org/licenses/by-nc-nd/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
	}else if (otherConstraints.contains("creativecommons.org/licenses/by-nc-sa/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)");
	}else if (otherConstraints.contains("creativecommons.org/licenses/by-nd/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution-NoDerivatives 4.0 International (CC BY-ND 4.0)");
	}else if (otherConstraints.contains("creativecommons.org/licenses/by-sa/4.0/")) {
	    addOtherConstraints(otherConstraints, "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)");
	} else {
	    type.getOtherConstraints().add(createCharacterStringPropertyType(otherConstraints));
	}
    }

    public void addOtherConstraints(String href, String otherConstraints) {
	type.getOtherConstraints().add(createAnchorPropertyType(href, otherConstraints));
    }

    /**
     * @XPathDirective(target = "gmd:otherConstraints/gco:CharacterString")
     */
    public void clearOtherConstraints() {
	type.unsetOtherConstraints();
    }

    public String getAccessConstraintCode() {
	if (type.isSetAccessConstraints() && !type.getAccessConstraints().isEmpty()
		&& type.getAccessConstraints().get(0).isSetMDRestrictionCode()) {
	    return type.getAccessConstraints().get(0).getMDRestrictionCode().getCodeListValue();
	}
	return null;
    }

    public Iterator<String> getAccessConstraintCodes() {
	List<String> ret = new ArrayList<>();
	if (type.isSetAccessConstraints() && !type.getAccessConstraints().isEmpty()) {
	    for (MDRestrictionCodePropertyType accessConstraints : type.getAccessConstraints()) {
		if (accessConstraints.isSetMDRestrictionCode()) {
		    ret.add(accessConstraints.getMDRestrictionCode().getCodeListValue());
		}
	    }
	}
	return ret.iterator();
    }

    public void addAccessConstraintsCode(String restrictionCode) {
	MDRestrictionCodePropertyType restrictionCodeProperty = new MDRestrictionCodePropertyType();
	restrictionCodeProperty.setMDRestrictionCode(
		createCodeListValueType(MD_RESTRICTION_CODE_CODELIST, restrictionCode, ISO_19115_CODESPACE, restrictionCode));
	type.getAccessConstraints().add(restrictionCodeProperty);
    }

    public void clearAccessConstraints() {
	type.unsetAccessConstraints();
    }

    public String getUseConstraintsCode() {
	if (type.isSetUseConstraints() && !type.getUseConstraints().isEmpty() && type.getUseConstraints().get(0).isSetMDRestrictionCode()) {
	    return type.getUseConstraints().get(0).getMDRestrictionCode().getCodeListValue();
	}
	return null;
    }

    public Iterator<String> getUseConstraintsCodes() {
	List<String> ret = new ArrayList<>();
	if (type.isSetUseConstraints() && !type.getUseConstraints().isEmpty()) {
	    for (MDRestrictionCodePropertyType useConstraints : type.getUseConstraints()) {
		if (useConstraints.isSetMDRestrictionCode()) {
		    ret.add(useConstraints.getMDRestrictionCode().getCodeListValue());
		}
	    }
	}
	return ret.iterator();
    }

    public void addUseConstraintsCode(String restrictionCode) {
	MDRestrictionCodePropertyType restrictionCodeProperty = new MDRestrictionCodePropertyType();
	restrictionCodeProperty.setMDRestrictionCode(
		createCodeListValueType(MD_RESTRICTION_CODE_CODELIST, restrictionCode, ISO_19115_CODESPACE, restrictionCode));
	type.getUseConstraints().add(restrictionCodeProperty);
    }

    public void clearUseConstraints() {
	type.unsetUseConstraints();
    }
}
