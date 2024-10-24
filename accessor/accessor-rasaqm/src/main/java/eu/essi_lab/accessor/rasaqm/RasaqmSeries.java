package eu.essi_lab.accessor.rasaqm;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class RasaqmSeries {
    @XmlElement
    private String stationName;
    @XmlElement
    private BigDecimal latitude;
    @XmlElement
    private BigDecimal longitude;
    @XmlElement
    private String parameterId;
    @XmlElement
    private String parameterName;
    @XmlElement
    private String units;
    @XmlElement
    private List<RasaqmData> data = new ArrayList<>();

    @XmlTransient
    private static JAXBContext context;
    static {
	try {
	    context = JAXBContext.newInstance(RasaqmSeries.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public static RasaqmSeries unmarshal(InputStream bais) throws JAXBException {
	Unmarshaller u = context.createUnmarshaller();
	Object ret = u.unmarshal(bais);
	if (ret instanceof JAXBElement<?>) {
	    JAXBElement<?> jaxb = (JAXBElement) ret;
	    ret = jaxb.getValue();
	}
	if (ret instanceof RasaqmSeries) {
	    RasaqmSeries ds = (RasaqmSeries) ret;
	    return ds;
	}
	return null;

    }

    public RasaqmSeries() {
	// JAXB no args constructor
    }

    @XmlTransient
    public List<RasaqmData> getData() {
	return data;
    }

    @XmlTransient
    public String getStationName() {
	return stationName;
    }

    public void setStationName(String stationName) {
	this.stationName = stationName;
    }

    @XmlTransient
    public BigDecimal getLatitude() {
	return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
    }

    @XmlTransient
    public BigDecimal getLongitude() {
	return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
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
    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    public void marshal(OutputStream baos) throws JAXBException {
	Marshaller m = context.createMarshaller();
	m.marshal(this, baos);

    }
}
