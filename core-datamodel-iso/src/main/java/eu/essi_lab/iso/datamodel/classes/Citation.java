package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.CIDatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIDateType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;

public class Citation extends ISOMetadata<CICitationType> {

    public Citation(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Citation(CICitationType type) {

	super(type);
    }

    public Citation() {

	super(new CICitationType());
    }

    @Override
    public JAXBElement<CICitationType> getElement() {

	return ObjectFactories.GMD().createCICitation(type);

    }

    public void setTitle(String title) {
	getElementType().setTitle(createCharacterStringPropertyType(title));

    }

    public String getTitle() {
	try {
	    CharacterStringPropertyType title = getElementType().getTitle();
	    return getStringFromCharacterString(title);

	} catch (NullPointerException ex) {
	    //nothing to do here
	}
	return null;

    }

    public void addAlternateTitle(String title) {
	getElementType().getAlternateTitle().add(createCharacterStringPropertyType(title));

    }

    public void addDate(String date, String dateType) {
	CIDatePropertyType ciDateProperty = new CIDatePropertyType();
	Date myDate = new Date();
	myDate.setDate(date);
	myDate.setDateType(dateType);
	CIDateType ciDateType = myDate.getElementType();
	ciDateProperty.setCIDate(ciDateType);
	getElementType().getDate().add(ciDateProperty);
    }

    public void setEdition(String edition) {
	getElementType().setEdition(createCharacterStringPropertyType(edition));
    }

    public String getEdition() {
	return getStringFromCharacterString(getElementType().getEdition());
    }

    public void addIdentifier(String identifier) {
	MDIdentifierPropertyType identifierProperty = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifier = new MDIdentifierType();
	mdIdentifier.setCode(createCharacterStringPropertyType(identifier));
	identifierProperty.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifier));
	getElementType().getIdentifier().add(identifierProperty);

    }

}
