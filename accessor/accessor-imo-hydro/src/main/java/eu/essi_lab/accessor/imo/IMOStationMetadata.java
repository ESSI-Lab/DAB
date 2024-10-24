package eu.essi_lab.accessor.imo;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class IMOStationMetadata {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(IMOStationMetadata.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlElement
    private String stationName;
    @XmlElement
    private String stationId;
    @XmlElement
    private String institute;
    @XmlElement
    private String river;
    @XmlElement
    private String latitude;
    @XmlElement
    private String longitude;

    @XmlTransient
    public String getRiver() {
	return river;
    }

    public void setRiver(String river) {
	this.river = river;
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
    public String getInstitute() {
	return institute;
    }

    public void setInstitute(String institute) {
	this.institute = institute;
    }

    @XmlTransient
    public String getLatitude() {
	return latitude;
    }

    public void setLatitude(String latitude) {
	this.latitude = latitude;
    }

    @XmlTransient
    public String getLongitude() {
	return longitude;
    }

    public void setLongitude(String longitude) {
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

    public static IMOStationMetadata unmarshal(InputStream stream) {
	try {
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    Object obj = unmarshaller.unmarshal(stream);
	    if (obj instanceof JAXBElement) {
		JAXBElement je = (JAXBElement) obj;
		obj = je.getValue();
	    }
	    if (obj instanceof IMOStationMetadata) {
		IMOStationMetadata ret = (IMOStationMetadata) obj;
		return ret;
	    }
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
