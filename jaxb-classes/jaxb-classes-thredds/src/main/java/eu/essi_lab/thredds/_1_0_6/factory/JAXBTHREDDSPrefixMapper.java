package eu.essi_lab.thredds._1_0_6.factory;

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

public class JAXBTHREDDSPrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
	if (namespaceUri.equals("http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0")) {
	    return null;
	} else if (namespaceUri.equals("http://www.w3.org/1999/xlink")) {
	    return "xlink";
	} else if (namespaceUri.equals("http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2")) {
	    return "ncml";
	}
	return null;
    }

}
