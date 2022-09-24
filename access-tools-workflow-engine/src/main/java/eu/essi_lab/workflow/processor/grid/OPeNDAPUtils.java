package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import ucar.ma2.DataType;

public class OPeNDAPUtils {
    public static String getBaseType(DataType dataType) {
	String bt = null;
	switch (dataType) {

	case BYTE:
	    bt = "Byte";
	case DOUBLE:
	    bt = "Float64";
	    break;
	case FLOAT:
	    bt = "Float32";
	    break;
	case INT:
	    bt = "Int16";
	    break;
	case LONG:
	    bt = "Int32";
	    break;
	case SHORT:
	    bt = "Int16";
	    break;
	case STRING:
	    bt = "String";
	    break;
	case STRUCTURE:
	    bt = "Structure";
	    break;
	default:
	case BOOLEAN:
	case CHAR:
	case ENUM1:
	case ENUM2:
	case ENUM4:
	case OBJECT:
	case OPAQUE:
	case SEQUENCE:
	    break;
	}
	return bt;
    }
}
