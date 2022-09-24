package eu.essi_lab.jaxb.wms.extension;

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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JAXBWMSPrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
	if (namespaceUri == null) {
	    return null;
	}
	if (namespaceUri.equals("http://www.opengis.net/wms")) {
	    return "wms";
	} else if (namespaceUri.equals("http://www.opengis.net/sld")) {
	    return "sld";
	} else if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema-instance")) {
	    return "xsi";
	}

	return null;
    }

}
