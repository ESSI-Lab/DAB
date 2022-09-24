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
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;
import net.opengis.iso19139.gmd.v_20060504.LILineagePropertyType;
import net.opengis.iso19139.gmd.v_20060504.LILineageType;

public class DataQuality extends ISOMetadata<DQDataQualityType> {
    public DataQuality(InputStream stream) throws JAXBException {

	super(stream);
    }

    public DataQuality() {

	this(new DQDataQualityType());
    }

    public DataQuality(DQDataQualityType type) {

	super(type);
    }

    public JAXBElement<DQDataQualityType> getElement() {

	JAXBElement<DQDataQualityType> element = ObjectFactories.GMD().createDQDataQuality(type);
	return element;
    }

    public void setLineageStatement(String lineage) {
	LILineagePropertyType property = type.getLineage();
	if (property == null) {
	    property = new LILineagePropertyType();
	    type.setLineage(property);
	}
	LILineageType lt = property.getLILineage();
	if (lt == null) {
	    lt = new LILineageType();
	    property.setLILineage(lt);
	}
	lt.setStatement(createCharacterStringPropertyType(lineage));
    }

    public String getLineageStatement() {
	try {
	    return getStringFromCharacterString(type.getLineage().getLILineage().getStatement());
	} catch (Exception e) {
	    return null;
	}
    }

}
