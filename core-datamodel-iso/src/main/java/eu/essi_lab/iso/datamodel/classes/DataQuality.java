package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.hisrc.w3c.xlink.v_1_0.TypeType;
import org.opengis.metadata.quality.QuantitativeAttributeAccuracy;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.DateTimePropertyType;
import net.opengis.iso19139.gmd.v_20060504.AbstractDQElementType;
import net.opengis.iso19139.gmd.v_20060504.DQAccuracyOfATimeMeasurementType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;
import net.opengis.iso19139.gmd.v_20060504.DQElementPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQQuantitativeAttributeAccuracyType;
import net.opengis.iso19139.gmd.v_20060504.DQResultPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopePropertyType;
import net.opengis.iso19139.gmd.v_20060504.LILineagePropertyType;
import net.opengis.iso19139.gmd.v_20060504.LILineageType;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

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

    public void addReport(JAXBElement<DQAccuracyOfATimeMeasurementType> accuracyOfMeasure) {
	
	List<DQElementPropertyType> reports = type.getReport();
	DQElementPropertyType elem = new DQElementPropertyType();
	elem.setAbstractDQElement(accuracyOfMeasure);
	reports.add(elem);
	type.setReport(reports);
    }
	
    

    public List<DQElementPropertyType> getReports() {
	return type.getReport();
    }

}
