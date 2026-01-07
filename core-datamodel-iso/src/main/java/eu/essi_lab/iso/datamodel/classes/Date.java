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
import net.opengis.iso19139.gco.v_20060504.DatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIDateType;
import net.opengis.iso19139.gmd.v_20060504.CIDateTypeCodePropertyType;

/**
 * CI_Date
 * 
 * @author boldrini
 */
public class Date extends ISOMetadata<CIDateType> {

    public Date(CIDateType type) {

	super(type);
    }

    public Date(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Date() {

	this(new CIDateType());
    }

    public void setDate(String date) {

	DatePropertyType dateProperty = new DatePropertyType();
	dateProperty.setDate(date);
	getElementType().setDate(dateProperty);
    }

    public String getDate() {

	try {
	    return getElementType().getDate().getDate();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    public void setDateType(String dateType) {

	CIDateTypeCodePropertyType dateTypeProperty = new CIDateTypeCodePropertyType();
	dateTypeProperty.setCIDateTypeCode(createCodeListValueType(CI_DATE_TYPE_CODE_CODELIST, dateType, ISO_19115_CODESPACE, dateType));
	getElementType().setDateType(dateTypeProperty);
    }

    public String getDateType() {

	try {
	    return getElementType().getDateType().getCIDateTypeCode().getCodeListValue();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    @Override
    public JAXBElement<CIDateType> getElement() {

	JAXBElement<CIDateType> element = ObjectFactories.GMD().createCIDate(type);
	return element;
    }
}
