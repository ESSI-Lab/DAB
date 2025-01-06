package eu.essi_lab.accessor.rasaqm;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class RasaqmDataset {
    @XmlElement
    private String parameterId;
    @XmlElement
    private String parameterName;
    @XmlElement
    private String units;

    @XmlElement
    private HashMap<String, RasaqmSeries> series = new HashMap<String, RasaqmSeries>();

    @XmlTransient
    private static JAXBContext context;
    static {
	try {
	    context = JAXBContext.newInstance(RasaqmDataset.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public static RasaqmDataset unmarshal(InputStream bais) throws JAXBException {
	Unmarshaller u = context.createUnmarshaller();
	Object ret = u.unmarshal(bais);
	if (ret instanceof JAXBElement<?>) {
	    JAXBElement<?> jaxb = (JAXBElement) ret;
	    ret = jaxb.getValue();
	}
	if (ret instanceof RasaqmDataset) {
	    RasaqmDataset ds = (RasaqmDataset) ret;
	    return ds;
	}
	return null;

    }
    
    /**
     * 
     * @return
     */
    public boolean isEmpty() {
	
	return series.isEmpty();
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

    public RasaqmSeries getSeries(String stationName) {
	return series.get(stationName);
    }

    public void addSeries(RasaqmSeries station) {
	series.put(station.getStationName(), station);
    }

    @XmlTransient
    public Set<String> getStationNames() {
	return series.keySet();

    }

    public void marshal(OutputStream baos) throws JAXBException {
	Marshaller m = context.createMarshaller();
	m.marshal(this, baos);

    }

}
