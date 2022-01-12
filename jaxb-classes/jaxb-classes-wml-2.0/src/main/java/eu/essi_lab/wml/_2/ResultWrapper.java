package eu.essi_lab.wml._2;

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

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class ResultWrapper extends eu.essi_lab.jaxb.wml._2_0.om__2.Result {

    public ResultWrapper() {

    }

    public ResultWrapper(eu.essi_lab.jaxb.wml._2_0.om__2.Result res) {
	this.value = res.getValue();
    }

    public void setMeasurementTimeseriesType(MeasurementTimeseriesType measurementTimeseriesType) throws Exception {
	JAXBElement<MeasurementTimeseriesType> jaxbElement = JAXBWML2.getInstance().getFactory()
		.createMeasurementTimeseries(measurementTimeseriesType);
	XMLDocumentReader reader = new XMLDocumentReader("<om:result xmlns:om=\"http://www.opengis.net/om/2.0\"></om:result>");
	JAXBWML2.getInstance().getMarshaller().marshal(jaxbElement, reader.getDocument().getDocumentElement());
	setValue(reader.getDocument().getDocumentElement());
    }

    public MeasurementTimeseriesType getMeasurementTimeseriesType() throws Exception {
	Object myValue = getValue();
	if (myValue instanceof Element) {
	    Element element = (Element) myValue;
	    MeasurementTimeseriesType ret = JAXBWML2.getInstance().unmarshalMeasurementTimeseriesType(element.getFirstChild());
	    return ret;
	}
	return null;

    }

    public MeasureType getMeasureType() {
	Object myValue = getValue();
	if (myValue instanceof MeasureType) {
	    MeasureType ret = (MeasureType) myValue;
	    return ret;
	}
	return null;

    }
}
