package eu.essi_lab.jaxb.common;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class ISO2014NameSpaceContext extends NamespacePrefixMapper implements NamespaceContext {

    public static final String GMD_NS_URI = "http://www.isotc211.org/2005/gmd";
    public static final String GMI_NS_URI = "http://standards.iso.org/iso/19115/-2/gmi/1.0";
    public static final String GMIOLD_NS_URI = "http://www.isotc211.org/2005/gmi";
    public static final String GCO_NS_URI = "http://www.isotc211.org/2005/gco";
    public static final String GMLOLD_NS_URI = "http://www.opengis.net/gml";
    public static final String GML_NS_URI = "http://www.opengis.net/gml/3.2";
    public static final String GML32_NS_URI = "http://www.opengis.net/gml/3.2";
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
    public static final String GMX_NS_URI = "http://www.isotc211.org/2005/gmx";
    public static final String XSI_SCHEMA_INSTANCE_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String GTS_NS_URI = "http://www.isotc211.org/2005/gts";
    public static final String WOSIS_NS_URI = "https://www.isric.org/explore/wosis";
    public static final String BLUECLOUD_NS_URI = "https://www.blue-cloud.org/";
    

    protected HashMap<String, String> map;

    public ISO2014NameSpaceContext() {

	map = new HashMap<String, String>();
	map.put(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX, NameSpace.GS_DATA_MODEL_SCHEMA_URI);

	map.put("gmd", GMD_NS_URI);
	
	map.put("gmiold", GMIOLD_NS_URI);

	map.put("gmi2019", GMI_NS_URI);
	
	map.put("gco", GCO_NS_URI);
	map.put("gml", GML_NS_URI);
	map.put("gmlold", GMLOLD_NS_URI);
	map.put("gml32", GML32_NS_URI);
	map.put("gmx", GMX_NS_URI);
	map.put("xlink", XLINK_NS_URI);
	map.put("xsi", XSI_SCHEMA_INSTANCE_NS_URI);
	map.put("wosis", WOSIS_NS_URI);
	map.put("gts", GTS_NS_URI);
	map.put("bluecloud", BLUECLOUD_NS_URI);
	
	
	

    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link ISO2014NameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param document
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(Document document) {

	XMLDocumentReader reader = new XMLDocumentReader(document);

	reader.setNamespaceContext(new ISO2014NameSpaceContext());

	return reader;
    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link ISO2014NameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param document
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(String document) throws SAXException, IOException {

	XMLDocumentReader reader = new XMLDocumentReader(document);

	reader.setNamespaceContext(new ISO2014NameSpaceContext());

	return reader;
    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link ISO2014NameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param stream
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(InputStream stream) throws SAXException, IOException {

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	reader.setNamespaceContext(new ISO2014NameSpaceContext());

	return reader;
    }

    @Override
    public String getNamespaceURI(String prefix) {

	return map.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {

	return map.keySet().stream().//
		filter(p -> map.get(p).equals(namespaceURI)).//
		findFirst().//
		orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {

	return map.keySet().stream().//
		filter(p -> map.get(p).equals(namespaceURI)).//
		collect(Collectors.toList()).//
		iterator();
    }

    @Override
    public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix) {

	return getPrefix(namespaceURI);
    }

    @Override
    public String[] getPreDeclaredNamespaceUris2() {

	return new String[] { "xsi", "http://www.w3.org/2001/XMLSchema-instance", "xs", "http://www.w3.org/2001/XMLSchema" };
    }

    /**
     * @return
     */

    public HashMap<String, String> getMap() {
	return map;
    }
}
