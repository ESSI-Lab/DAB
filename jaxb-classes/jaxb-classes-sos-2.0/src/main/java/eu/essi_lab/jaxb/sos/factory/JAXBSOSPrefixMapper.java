package eu.essi_lab.jaxb.sos.factory;

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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JAXBSOSPrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
	if (namespaceUri == null) {
	    return null;
	}
	if (namespaceUri.equals("http://www.opengis.net/sos/2.0")) {
	    return "sos";
	} else if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema-instance")) {
	    return "xsi";
	} else if (namespaceUri.equals("http://www.opengis.net/swe/2.0")) {
	    return "swe";
	} else if (namespaceUri.equals("http://www.opengis.net/swes/2.0")) {
	    return "swes";
	} else if (namespaceUri.equals("http://www.w3.org/2005/08/addressing")) {
	    return "wsa";
	} else if (namespaceUri.equals("http://www.opengis.net/fes/2.0")) {
	    return "fes";
	} else if (namespaceUri.equals("http://www.opengis.net/gml/3.2")) {
	    return "gml";
	} else if (namespaceUri.equals("http://www.opengis.net/ogc")) {
	    return "ogc";
	} else if (namespaceUri.equals("http://www.opengis.net/ows/1.1")) {
	    return "ows";
	} else if (namespaceUri.equals("http://www.w3.org/1999/xlink")) {
	    return "xlink";
	} else if (namespaceUri.equals("http://www.opengis.net/samplingSpatial/2.0")) {
	    return "sams";
	} else if (namespaceUri.equals("http://www.opengis.net/sampling/2.0")) {
	    return "sf";
	} else if (namespaceUri.equals("http://www.opengis.net/om/2.0")) {
	    return "om";
	} else if (namespaceUri.equals("http://www.isotc211.org/2005/gco")) {
	    return "gco";
	} else if (namespaceUri.equals("http://www.isotc211.org/2005/gmd")) {
	    return "gmd";
	} else if (namespaceUri.equals("http://www.isotc211.org/2005/gts")) {
	    return "gts";
	} else if (namespaceUri.equals("http://www.isotc211.org/2005/gmd")) {
	    return "gmd";
	} else if (namespaceUri.equals("http://docs.oasis-open.org/wsn/t-1")) {
	    return "wsnt";
	} else if (namespaceUri.equals("http://www.opengis.net/sosgda/2.0")) {
	    return "sosgda";
	} else if (namespaceUri.equals("http://www.opengis.net/waterml/2.0")) {
	    return "wml2";
	} else if (namespaceUri.equals("http://www.isotc211.org/2005/gmx")) {
	    return "gmx";
	} else if (namespaceUri.equals("http://www.opengis.net/sensorml/2.0")) {
	    return "sml";
	} 

	return null;
    }

}
