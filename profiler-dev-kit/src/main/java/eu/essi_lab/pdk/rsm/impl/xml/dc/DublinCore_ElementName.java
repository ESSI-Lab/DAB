package eu.essi_lab.pdk.rsm.impl.xml.dc;

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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.google.common.base.CaseFormat;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLPruner;

/**
 * 
 * @author Fabrizio
 *
 */
public enum DublinCore_ElementName {

    IDENTIFIER("*/*:identifier"), //
    TITLE("*/*:title"), //
    SUBJECT("*/*:subject"), //
    FORMAT("*/*:format"), //
    RELATION("*/*:relation"), //
    MODIFIED("*/*:modified"), //
    DATE("*/*:date"), //
    ABSTRACT("*/*:abstract"), //
    CREATOR("*/*:creator"), //
    PUBLISHER("*/*:publisher"), //
    CONTRIBUTOR("*/*:contributor"), //
    LANGUAGE("*/*:language"), //
    SOURCE("*/*:source"), //
    BOUNDING_BOX("*/*:BoundingBox//*"), //
    TYPE("*/*:type");//

    /**
     * @param metadata
     * @param properties
     * @return
     * @throws Exception
     */
    public static Document subset(Document metadata, DublinCore_ElementName[] properties) throws Exception {

	List<String> xpaths = new ArrayList<String>();
	for (DublinCore_ElementName property : properties) {
	    String mapping = property.getXPath();
	    xpaths.add(mapping);
	}

	XMLPruner pruner = new XMLPruner(xpaths, new CommonNameSpaceContext());
	Document ret = metadata;

	return pruner.prune(ret);
    }

    /**
     * @param name
     * @return
     */
    public static DublinCore_ElementName decode(String name) {

	name = name.toLowerCase();
	DublinCore_ElementName[] values = values();
	for (int i = 0; i < values.length; i++) {
	    DublinCore_ElementName set = values[i];
	    if (set.getName().toLowerCase().contains(name) || name.contains(set.getName().toLowerCase())) {
		return set;
	    }
	}
	return null;
    }

    private String xPath;

    private DublinCore_ElementName(String xPath) {

	this.xPath = xPath;
    }

    public String getName() {

	return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
    }

    public String getXPath() {

	return xPath;
    }
}
