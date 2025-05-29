package eu.essi_lab.profiler.wfs;

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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JAXBWFSPrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
	if (namespaceUri == null) {
	    return null;
	}
	if (namespaceUri.equals("http://www.opengis.net/wfs")) {
	    return "wfs";
	}else if (namespaceUri.equals("http://www.opengis.net/ows")) {
	    return "ows";
	} else if (namespaceUri.equals("http://www.opengis.net/sld")) {
	    return "sld";
	} else if (namespaceUri.equals("http://www.w3.org/1999/xlink")) {
	    return "xlink";	    
	} else if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema-instance")) {
	    return "xsi";
	} else if (namespaceUri.equals("http://essi-lab.eu")) {
	    return "essi";
	}
	
	

	return null;
    }

}
