package eu.essi_lab.accessor.wof.client;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;

public class CUAHSINamespaceContext implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
	if (prefix == null || prefix.isEmpty()) {
	    return null;
	}
	if (prefix.equals("wml")) {
	    return "http://www.cuahsi.org/waterML/1.1/";
	}
	return null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
	if (namespaceURI == null || namespaceURI.isEmpty()) {
	    return null;
	}
	if (namespaceURI.equals("http://www.cuahsi.org/waterML/1.1/")) {
	    return "wml";
	}
	return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
	List<String>prefixes = new ArrayList<>();
	prefixes.add("wml");
	return prefixes.iterator();
    }

}
