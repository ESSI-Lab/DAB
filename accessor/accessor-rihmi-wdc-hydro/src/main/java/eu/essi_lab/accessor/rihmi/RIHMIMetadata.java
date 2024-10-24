package eu.essi_lab.accessor.rihmi;

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
import java.io.OutputStream;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.model.resource.InterpolationType;

@XmlRootElement
public class RIHMIMetadata {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(RIHMIMetadata.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlElement
    private String parameterId;
    @XmlElement
    private String parameterName;
    @XmlElement
    private String stationName;
    @XmlElement
    private String stationId;
    @XmlElement
    private String units;
    @XmlElement
    private Date begin;
    @XmlElement
    private Date end;
    @XmlElement
    private Double latitude;
    @XmlElement
    private Double longitude;
    @XmlElement
    private InterpolationType interpolation;
    @XmlElement
    private String aggregationDuration;

    @XmlTransient
    public InterpolationType getInterpolation() {
	return interpolation;
    }

    public void setInterpolation(InterpolationType interpolation) {
	this.interpolation = interpolation;
    }

    @XmlTransient
    public String getAggregationDuration() {
	return aggregationDuration;
    }

    public void setAggregationDuration(String aggregationDuration) {
	this.aggregationDuration = aggregationDuration;
    }

    @XmlTransient
    public String getParameterId() {
	return parameterId;
    }

    public void setParameterId(String parameterId) {
	this.parameterId = parameterId;
    }

    @XmlTransient
    public String getParameterName() {
	return parameterName;
    }

    public void setParameterName(String parameterName) {
	this.parameterName = parameterName;
    }

    @XmlTransient
    public String getStationName() {
	return stationName;
    }

    public void setStationName(String stationName) {
	this.stationName = stationName;
    }

    @XmlTransient
    public String getStationId() {
	return stationId;
    }

    public void setStationId(String stationId) {
	this.stationId = stationId;
    }

    @XmlTransient
    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    @XmlTransient
    public Date getBegin() {
	return begin;
    }

    public void setBegin(Date begin) {
	this.begin = begin;
    }

    @XmlTransient
    public Date getEnd() {
	return end;
    }

    public void setEnd(Date end) {
	this.end = end;
    }

    @XmlTransient
    public Double getLatitude() {
	return latitude;
    }

    public void setLatitude(Double latitude) {
	this.latitude = latitude;
    }

    @XmlTransient
    public Double getLongitude() {
	return longitude;
    }

    public void setLongitude(Double longitude) {
	this.longitude = longitude;
    }

    public void marshal(OutputStream out) {
	try {
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.marshal(this, out);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}

    }

    public static RIHMIMetadata unmarshal(InputStream stream) {
	try {
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    Object obj = unmarshaller.unmarshal(stream);
	    if (obj instanceof JAXBElement) {
		JAXBElement je = (JAXBElement) obj;
		obj = je.getValue();
	    }
	    if (obj instanceof RIHMIMetadata) {
		RIHMIMetadata ret = (RIHMIMetadata) obj;
		return ret;
	    }
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
