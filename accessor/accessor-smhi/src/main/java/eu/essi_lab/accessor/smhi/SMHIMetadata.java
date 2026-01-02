package eu.essi_lab.accessor.smhi;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class SMHIMetadata {

    @XmlTransient
    private static JAXBContext context;
    @XmlTransient
    private static Marshaller marshaller;
    @XmlTransient
    private static Unmarshaller unmarshaller;

    static {
	try {
	    context = JAXBContext.newInstance(SMHIMetadata.class);
	    marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            unmarshaller = context.createUnmarshaller();

	} catch (JAXBException e) {
	    e.printStackTrace();
	}

    }

    @XmlElement
    private SMHIParameter parameter;
    @XmlElement
    private SMHIStation station;

    @XmlTransient
    public SMHIParameter getParameter() {
	return parameter;
    }

    public void setParameter(SMHIParameter parameter) {
	this.parameter = parameter;
    }

    @XmlTransient
    public SMHIStation getStation() {
	return station;
    }

    public void setStation(SMHIStation station) {
	this.station = station;
    }

    public String marshal() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	marshaller.marshal(this,baos);
	baos.close();
	return new String(baos.toByteArray());
    }
    
    public static SMHIMetadata unmarshal(InputStream stream) throws Exception {
	return (SMHIMetadata) unmarshaller.unmarshal(stream);
    }
}
