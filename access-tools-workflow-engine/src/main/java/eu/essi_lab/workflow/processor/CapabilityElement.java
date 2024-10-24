package eu.essi_lab.workflow.processor;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

/**
 * @param <T>
 * @author boldrini
 */
public class CapabilityElement<T> {

    protected Set<T> values;
    protected PresenceType presence;

    /**
     * @param value
     * @return
     */
    public static BooleanCapabilityElement anyFromBoolean(Boolean value) {

	return new BooleanCapabilityElement(value);
    }

    /**
     * @param dataType
     * @return
     */
    public static CapabilityElement<DataType> anyFromDataType(DataType dataType) {

	return new CapabilityElement<>(dataType);
    }

    /**
     * @param dataType
     * @return
     */
    public static CapabilityElement<DataType> sameAsFromDataType(DataType dataType) {

	return new CapabilityElement<>(PresenceType.SAME_AS, Arrays.asList(dataType));
    }

    /**
     * @param crs
     * @return
     */
    public static CapabilityElement<CRS> anyFromCRSAsString(String crs) {

	return new CapabilityElement<>(CRS.fromIdentifier(crs));
    }

    /**
     * @param crs
     * @return
     */
    public static CapabilityElement<CRS> anyFromCRS(CRS crs) {

	return new CapabilityElement<>(PresenceType.ANY, crs.resolveList());
    }

    /**
     * @param crsList
     * @return
     */
    public static CapabilityElement<CRS> anyFromCRSAsStrings(List<String> crsList) {

	return new CapabilityElement<>(//
		PresenceType.ANY, //
		crsList.stream().map(c -> CRS.fromIdentifier(c)).collect(Collectors.toList()));
    }

    /**
     * @param crsList
     * @return
     */
    public static CapabilityElement<CRS> anyFromCRS(List<CRS> crsList) {

	return new CapabilityElement<>(//
		PresenceType.ANY, //
		crsList.stream().flatMap(c -> c.resolveList().stream()).collect(Collectors.toList()));
    }

    /**
     * @param crsList
     * @return
     */
    public static CapabilityElement<CRS> sameAsFromCRS(List<CRS> crsList) {
	return new CapabilityElement<>(//
		PresenceType.SAME_AS, //
		crsList.stream().flatMap(c -> c.resolveList().stream()).collect(Collectors.toList()));
    }

    /**
     * @param format
     * @return
     */
    public static CapabilityElement<DataFormat> anyFromDataFormat(String format) {

	return new CapabilityElement<>(DataFormat.fromIdentifier(format));
    }

    /**
     * @param format
     * @return
     */
    public static CapabilityElement<DataFormat> anyFromDataFormat(DataFormat format) {

	return new CapabilityElement<>(format);
    }

    /**
     * @param formatsList
     * @return
     */
    public static CapabilityElement<DataFormat> anyFromDataFormatAsString(List<String> formatsList) {

	return new CapabilityElement<>(//
		PresenceType.ANY, //
		formatsList.stream().map(f -> DataFormat.fromIdentifier(f)).collect(Collectors.toList()));
    }

    /**
     * @param formatsList
     * @return
     */
    public static CapabilityElement<DataFormat> anyFromDataFormat(List<DataFormat> formatsList) {

	return new CapabilityElement<>(//
		PresenceType.ANY, //
		formatsList);
    }

    /**
     * @param formatsList
     * @return
     */
    public static CapabilityElement<DataFormat> sameAsFromDataFormat(List<DataFormat> formatsList) {

	return new CapabilityElement<>(//
		PresenceType.SAME_AS, //
		formatsList);
    }

    /**
     * @param value
     */
    protected CapabilityElement(T value) {

	if (value == null) {
	    throw new IllegalArgumentException("Invalid null value");
	}

	this.values = new HashSet<>();
	this.values.add(value);
	this.presence = PresenceType.ANY;
    }

    /**
     * @param presence
     * @param values
     */
    protected CapabilityElement(PresenceType presence, List<T> values) {

	if (values == null || values.isEmpty()) {
	    throw new IllegalArgumentException("Invalid list");
	}

	this.presence = presence;
	this.values = new HashSet<>();
	this.values.addAll(values);
    }

    public T getFirstValue() {

	return values.iterator().next();
    }

    public Set<T> getValues() {

	return values;
    }

    public PresenceType getPresence() {
	return presence;
    }

    public enum PresenceType {
	ANY, // the metadata element can be present with any possible values (e.g. both T and F)
	SAME_AS // the metadata element must be present with the same value for input and for output (e.g. Input: T ->
	// Output T)
    }

    public boolean accept(CapabilityElement<T> outputElement) {

	Set<T> previousBlockValues = outputElement.getValues();

	for (T previousBlockValue : previousBlockValues) {
	    for (T currentBlockInputValue : values) {
		if (currentBlockInputValue.equals(previousBlockValue)) {
		    return true;
		}
		if (currentBlockInputValue instanceof DataFormat) {
		    DataFormat currentBlockFormat = (DataFormat) currentBlockInputValue;
		    DataFormat previousBlockFormat = (DataFormat) previousBlockValue;
		    if (previousBlockFormat.isSubTypeOf(currentBlockFormat)) {
			// it means that this block can accept all the specific sub types
			return true;
		    }
		}
	    }
	}

	return false;
    }

    @Override
    public boolean equals(Object o) {

	if (o instanceof CapabilityElement<?>) {

	    CapabilityElement<?> c = (CapabilityElement<?>) o;

	    return c.presence.equals(this.presence) && c.values.equals(this.values);
	}

	return super.equals(o);
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }

    @Override
    public String toString() {
	switch (presence) {
	case ANY:
	    return valuesToString();
	case SAME_AS:
	    return valuesToString();
	default:
	    return "null";
	}
    }

    private String valuesToString() {

	StringBuilder ret = new StringBuilder("");

	for (T t : values) {
	    if (t instanceof Boolean) {
		Boolean bool = (Boolean) t;
		if (Boolean.TRUE.equals(bool)) {
		    ret.append("T-");
		} else {
		    ret.append("F-");
		}
	    } else {
		ret.append(t.toString()).append(" ");
	    }
	}

	String val = ret.toString();

	if (val.endsWith("-")) {
	    val = val.substring(0, val.length() - 1);
	}
	return val;
    }
}
