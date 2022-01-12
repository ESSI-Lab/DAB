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
import net.opengis.iso19139.gmd.v_20060504.MDBrowseGraphicType;

public class BrowseGraphic extends ISOMetadata<MDBrowseGraphicType> {

    public BrowseGraphic(InputStream stream) throws JAXBException {

	super(stream);
    }

    public BrowseGraphic(MDBrowseGraphicType type) {

	super(type);
    }

    public BrowseGraphic() {

	super(new MDBrowseGraphicType());
    }

    @Override
    public JAXBElement<MDBrowseGraphicType> getElement() {

	JAXBElement<MDBrowseGraphicType> element = ObjectFactories.GMD().createMDBrowseGraphic(type);
	return element;
    }
    public String getFileName() {
	return getStringFromCharacterString(getElementType().getFileName());

    }

    /**
     * @XPathDirective(target = "gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString")
     */
    public void setFileName(String fileName) {
	getElementType().setFileName(createCharacterStringPropertyType(fileName));
    }

    /**
     * @XPathDirective(target = "gmd:fileDescription/gco:CharacterString")
     */
    public String getFileDescription() {

	return getStringFromCharacterString(getElementType().getFileDescription());

    }

    /**
     * @XPathDirective(target = "gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString")
     */
    public void setFileDescription(String fileDescription) {
	getElementType().setFileDescription(createCharacterStringPropertyType(fileDescription));
    }

    /**
     * @XPathDirective(target = "gmd:fileType/gco:CharacterString")
     */
    public String getFileType() {

	return getStringFromCharacterString(getElementType().getFileType());

    }

    /**
     * @XPathDirective(target = "gmd:MD_BrowseGraphic/gmd:fileType/gco:CharacterString")
     */
    public void setFileType(String fileType) {
	getElementType().setFileType(createCharacterStringPropertyType(fileType));
    }

}
