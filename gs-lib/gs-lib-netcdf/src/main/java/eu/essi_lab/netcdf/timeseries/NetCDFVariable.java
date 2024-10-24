package eu.essi_lab.netcdf.timeseries;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayBoolean;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayObject;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;

public class NetCDFVariable<T> {

    private String name = null;
    private String longName;
    private String standardName;
    private List<T> values = null;
    private String units = null;
    private T missingValue = null;
    private DataType dataType = null;

    public void setDataType(DataType dataType) {
	this.dataType = dataType;
    }

    public DataType getDataType() {
	return dataType;
    }

    private List<SimpleEntry<String, Object>> attributes = new ArrayList<>();

    public NetCDFVariable(String name, List<T> values, String units, DataType dataType) {
	super();
	this.values = values;
	this.units = units;
	this.name = name;
	this.dataType = dataType;

    }

    public List<T> getValues() {
	return values;
    }

    public void setValues(List<T> values) {
	this.values = values;
    }

    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    public T getMissingValue() {
	return missingValue;
    }

    public void setMissingValue(T missingValue) {
	this.missingValue = missingValue;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getStandardName() {
	return this.standardName;
    }

    public void setStandardName(String standardName) {
	this.standardName = standardName;
    }

    public void addAttribute(String key, Object value) {
	this.attributes.add(new SimpleEntry<String, Object>(key, value));

    }

    public List<SimpleEntry<String, Object>> getAttributes() {
	return attributes;
    }

    // public DataType getDataType() {
    // if (values.isEmpty()) {
    // return null;
    // }
    // T value = getValues().iterator().next();
    // if (value instanceof Double) {
    // return DataType.DOUBLE;
    // }
    // if (value instanceof Boolean) {
    // return DataType.BOOLEAN;
    // }
    // if (value instanceof Byte) {
    // return DataType.BYTE;
    // }
    // if (value instanceof Character) {
    // return DataType.CHAR;
    // }
    // if (value instanceof Enum<?>) {
    // return DataType.ENUM1;
    // }
    // if (value instanceof Float) {
    // return DataType.FLOAT;
    // }
    // if (value instanceof Integer) {
    // return DataType.INT;
    // }
    // if (value instanceof Long) {
    // return DataType.LONG;
    // }
    // if (value instanceof Short) {
    // return DataType.SHORT;
    // }
    // if (value instanceof String) {
    // return DataType.STRING;
    // }
    // return null;
    // }

    public Array getArray() {
	Array ret = null;
	int size = getValues().size();
	switch (getDataType()) {
	case BOOLEAN:
	    ret = new ArrayBoolean.D1(size);
	    break;
	case CHAR:
	    ret = new ArrayChar.D1(size);
	    break;
	case BYTE:
	    ret = new ArrayByte.D1(size);
	    break;
	case DOUBLE:
	    ret = new ArrayDouble.D1(size);
	    break;
	case FLOAT:
	    ret = new ArrayFloat.D1(size);
	    break;
	case INT:
	    ret = new ArrayInt.D1(size);
	    break;
	case LONG:
	    ret = new ArrayLong.D1(size);
	    break;
	case SHORT:
	    ret = new ArrayShort.D1(size);
	    break;
	case STRING:
	    ret = new ArrayObject(String.class, new int[] { size });
	    break;
	default:
	    break;
	}
	if (ret != null) {
	    Index hIndex = ret.getIndex();
	    for (int j = 0; j < values.size(); j++) {
		hIndex.set(j);
		ret.setObject(hIndex, values.get(j));
	    }
	}
	return ret;
    }

    public void setLongName(String longName) {
	this.longName = longName;

    }

    public String getLongName() {
	return longName;
    }

}
