/**
 * 
 */
package eu.essi_lab.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Optional;

/**
 * @author Fabrizio
 */
public class SearchAfter implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6533469371427721506L;
    private List<Object> values = new ArrayList<>();

    public SearchAfter(List<Object> values) {
	this.values.addAll(values);
    }
    
    public SearchAfter() {	
    }

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(String value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.addStringValue(value);

	return searchAfter;
    }

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(double value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.addDoubleValue(value);

	return searchAfter;
    }

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(long value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.addLongValue(value);

	return searchAfter;
    }

    /**
     * @return the values
     */
    public Optional<List<Object>> getValues() {
	return Optional.ofNullable(values);
    }

    /**
     * @param stringValue
     */
    public void addStringValue(String stringValue) {
	this.values.add(stringValue);
    }

    /**
     * @param doubleValue
     */
    public void addDoubleValue(Double doubleValue) {
	this.values.add(doubleValue);
    }

    /**
     * @param longValue
     */
    public void addLongValue(Long longValue) {
	this.values.add(longValue);
    }

    /**
     * @return
     */
    public Optional<String> toStringValue() {

	if (values.isEmpty()) {
	    return Optional.empty();
	}

	String ret = "";

	for (Object value : values) {
	    ret += String.valueOf(value) + ",";
	}
	if (ret.endsWith(",")) {
	    ret = ret.substring(0, ret.length() - 1);
	}
	return Optional.of(ret);

    }

    @Override
    public String toString() {

	return toStringValue().orElse("empty");
    }

    /**
     * @param searchAfter
     * @return
     * @throws Exception
     */
    public static InputStream serialize(SearchAfter searchAfter) throws Exception {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
	objectOutputStream.writeObject(searchAfter);
	objectOutputStream.flush();
	objectOutputStream.close();

	return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static SearchAfter deserialize(InputStream stream) throws Exception {

	ObjectInputStream objectInputStream = new ObjectInputStream(stream);
	SearchAfter out = (SearchAfter) objectInputStream.readObject();
	objectInputStream.close();

	return out;
    }
}
