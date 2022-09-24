package eu.essi_lab.iso.datamodel;

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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Fabrizio
 */
public class CDATA_Adapter extends XmlAdapter<String, String> {

    private static final String CDATA_BEGIN = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    @Override
    public String marshal(String arg0) throws Exception {
	return CDATA_BEGIN + arg0 + CDATA_END;
    }

    @Override
    public String unmarshal(String arg0) throws Exception {
	if (arg0.startsWith(CDATA_BEGIN) && arg0.endsWith(CDATA_END)) {
	    arg0 = arg0.substring(CDATA_BEGIN.length(), arg0.length() - CDATA_END.length());
	}
	return arg0;
    }
}
