package eu.essi_lab.bufr.datamodel;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "collection")
public class BUFRCollection {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(BUFRCollection.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlElement(name = "record")
    private List<BUFRRecord> records = new ArrayList<>();

    @XmlTransient
    public List<BUFRRecord> getRecords() {

	return records;
    }

    public void setRecords(List<BUFRRecord> records) {
	this.records = records;
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

    public static BUFRCollection unmarshal(InputStream stream) {
	try {
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    Object obj = unmarshaller.unmarshal(stream);
	    if (obj instanceof JAXBElement) {
		JAXBElement<?> je = (JAXBElement<?>) obj;
		obj = je.getValue();
	    }
	    if (obj instanceof BUFRCollection) {
		BUFRCollection ret = (BUFRCollection) obj;
		return ret;
	    }
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void addRecord(BUFRRecord record) {
	Date time = record.getTime();
	if (time == null) {
	    return;
	}
	if (record.getLatitude() == null || record.getLongitude() == null) {
	    return;
	}
	if (alreadyPresentDates.contains(time)) {
	    return;
	}
	alreadyPresentDates.add(time);
	getRecords().add(record);

	records.sort(new Comparator<BUFRRecord>() {

	    @Override
	    public int compare(BUFRRecord o1, BUFRRecord o2) {
		return o1.getTime().compareTo(o2.getTime());
	    }
	});
    }

    private HashSet<Date> alreadyPresentDates = new HashSet<Date>();

    /**
     * Returns a map from variable name to a collection of bufr record containing values for this variable
     * (constructed from the bufr records of this collection)
     */
    public Map<String, BUFRCollection> getVariableCollections() {
	Map<String, BUFRCollection> ret = new HashMap<>();

	Set<String> variableNames = getVariableNames();

	for (String variableName : variableNames) {

	    BUFRCollection varCollection = new BUFRCollection();

	    for (BUFRRecord record : getRecords()) {

		if (!record.hasNumericVariable(variableName)) {
		    continue;
		}

		BUFRRecord newRecord = record.clone();

		for (String otherName : variableNames) {
		    if (!otherName.equals(variableName)) {
			newRecord.removeElement(otherName);
		    }
		}
		varCollection.addRecord(newRecord);

	    }

	    ret.put(variableName, varCollection);

	}

	return ret;
    }

    private Set<String> getVariableNames() {
	Set<String> ret = new HashSet<>();

	for (BUFRRecord record : getRecords()) {
	    List<BUFRElement> variableElements = record.identifyVariables();
	    for (BUFRElement variableElement : variableElements) {
		ret.add(variableElement.getName());
	    }
	}

	return ret;
    }

}
