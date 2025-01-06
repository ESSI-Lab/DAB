
package eu.essi_lab.accessor.inpe;

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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class INPERecord {

    public enum FIELD {
	Id, //
	Lat, //
	Long, //
	LatGMS, //
	LongGMS, //
	DateTime, //
	Satellite, //
	City, //
	State, //
	Country, //
	Vegetation, //
	Suitability, //
	Precipitation, //
	DaysNoRain, //
	Risk, //
	Biome
    }

    private XMLDocumentReader reader;

    public INPERecord(XMLDocumentReader reader) {
	this.reader = reader;
    }

    public String getField(FIELD field) {
	int position = field.ordinal();
	try {
	    String ret = reader.evaluateString("//*:tr[" + (position + 2) + "]/*:td[2]/*:font");
	    if (ret != null) {
		ret = ret.trim();
	    }
	    return ret;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("error");
	}
	return "";
    }

}
