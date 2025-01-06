package eu.essi_lab.accessor.s3;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class FeatureMetadata {

    private static JAXBContext context;
    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;

    static {
	try {
	    context = JAXBContext.newInstance(FeatureMetadata.class);
	    marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    unmarshaller = context.createUnmarshaller();

	} catch (JAXBException e) {
	    e.printStackTrace();
	}

    }
    @XmlElement
    private String id;
    @XmlElements(@XmlElement)
    private HashMap<String, String> attributes = new HashMap<String, String>();
    @XmlElement
    private BigDecimal south;
    @XmlElement
    private BigDecimal east;
    @XmlElement
    private BigDecimal west;
    @XmlElement
    private BigDecimal north;
    @XmlElement
    private String url;

    @XmlTransient
    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    @XmlTransient
    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    @XmlTransient
    public HashMap<String, String> getAttributes() {
	return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
	this.attributes = attributes;
    }

    @XmlTransient
    public BigDecimal getSouth() {
	return south;
    }

    public void setSouth(BigDecimal south) {
	this.south = south;
    }

    @XmlTransient
    public BigDecimal getEast() {
	return east;
    }

    public void setEast(BigDecimal east) {
	this.east = east;
    }

    @XmlTransient
    public BigDecimal getWest() {
	return west;
    }

    public void setWest(BigDecimal west) {
	this.west = west;
    }

    @XmlTransient
    public BigDecimal getNorth() {
	return north;
    }

    public void setNorth(BigDecimal north) {
	this.north = north;
    }

    public String marshal() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	marshaller.marshal(this, baos);
	baos.close();
	return new String(baos.toByteArray());
    }

    public static FeatureMetadata unmarshal(InputStream stream) throws Exception {
	return (FeatureMetadata) unmarshaller.unmarshal(stream);
    }

}
