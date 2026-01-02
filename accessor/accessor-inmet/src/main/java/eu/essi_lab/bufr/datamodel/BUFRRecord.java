package eu.essi_lab.bufr.datamodel;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

@XmlRootElement
public class BUFRRecord {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(BUFRRecord.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlElement(name = "element")
    private List<BUFRElement> elements = new ArrayList<>();

    @XmlTransient
    public List<BUFRElement> getElements() {
	return elements;
    }

    public void print() {
	for (BUFRElement element : elements) {
	    element.print();
	}
    }

    public void marshal(OutputStream out) {
	try {
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.marshal(this, out);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public String getElementValue(String elementName) {
	BUFRElement element = getElement(elementName);
	if (element != null) {
	    String ret = element.getValue();
	    if (ret != null) {
		return ret.trim();
	    }
	}
	return null;
    }

    public BUFRElement getElement(String elementName) {
	String ignoreCaseElementName = elementName.toLowerCase();
	for (BUFRElement element : elements) {
	    if (element.getName().toLowerCase().startsWith(ignoreCaseElementName)) {
		return element;
	    }
	}
	return null;
    }

    public BUFRElement getStationHeight() {
	return getElement("Height_of_station_ground_above_mean_sea_level");
    }

    public Double getLatitude() {
	String latitude = getElementValue("Latitude");
	if (latitude != null) {
	    return Double.parseDouble(latitude);
	}
	return null;
    }

    public Double getLongitude() {
	String longitude = getElementValue("Longitude");
	if (longitude != null) {
	    return Double.parseDouble(longitude);
	}
	return null;
    }

    public String getBUFRCenterName() {
	BUFRElement element = getElement("BUFR:centerName");
	if (element == null) {
	    return null;
	}
	return element.getValue();
    }

    public Date getTime() {
	String year = getElementValue("Year");
	String month = getElementValue("Month");
	if (month.length() == 1) {
	    month = "0" + month;
	}
	String day = getElementValue("Day");
	if (day.length() == 1) {
	    day = "0" + day;
	}
	String hour = getElementValue("Hour");
	if (hour.length() == 1) {
	    hour = "0" + hour;
	}
	String minute = getElementValue("Minute");
	if (minute.length() == 1) {
	    minute = "0" + minute;
	}
	Optional<Date> optionalDate = ISO8601DateTimeUtils
		.parseISO8601ToDate(year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00Z");
	if (optionalDate.isPresent()) {
	    return optionalDate.get();
	}
	return null;

    }

    public static BUFRRecord unmarshal(InputStream stream) {
	try {
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    Object obj = unmarshaller.unmarshal(stream);
	    if (obj instanceof JAXBElement) {
		JAXBElement je = (JAXBElement) obj;
		obj = je.getValue();
	    }
	    if (obj instanceof BUFRRecord) {
		BUFRRecord ret = (BUFRRecord) obj;
		return ret;
	    }
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public String getWMOBlockNumber() {
	return getElementValue("WMO_block_number");
    }

    public String getWMOStationNumber() {
	return getElementValue("WMO_station_number");
    }

    public String getStationOrSiteName() {
	return getElementValue("Station_or_site_name");
    }

    public String getTypeOfStation() {
	return getElementValue("Type_of_station");
    }

    public void removeElement(String otherName) {
	Iterator<BUFRElement> iterator = elements.iterator();
	while (iterator.hasNext()) {
	    BUFRElement element = (BUFRElement) iterator.next();
	    if (element.getName().equals(otherName)) {
		iterator.remove();
	    }
	}

    }

    public BUFRRecord clone() {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	marshal(baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	BUFRRecord ret = unmarshal(bais);
	try {
	    baos.close();
	    bais.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return ret;
    }

    public List<BUFRElement> identifyVariables() {
	List<BUFRElement> ret = new ArrayList<>();
	for (BUFRElement element : elements) {
	    if (element.isVariable()) {

		String value = element.getValue();
		if (value != null) {
		    try {
			Double.parseDouble(value);
			ret.add(element);
		    } catch (Exception e) {
		    }
		}

	    }
	}
	return ret;
    }

    public boolean hasNumericVariable(String variableName) {
	String value = getElementValue(variableName);
	if (value == null) {
	    return false;
	}
	try {
	    Double.parseDouble(value);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

}
