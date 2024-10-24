package eu.essi_lab.jaxb.common;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLFactories;

/**
 * A JAXB context including:
 * <ul>
 * <li>CSW core elements</li>
 * <li>Dublin Core elements</li>
 * <li>Dublin Core terms</li>
 * <li>GML 3.2.0</li>
 * <li>OWS BoundingBox</li>
 * <li>GCO</li>
 * <li>GMD</li>
 * <li>GMX</li>
 * <li>GSR</li>
 * <li>GSS</li>
 * <li>GTS</li>
 * <li>SRV</li>
 * <li>GMI</li>
 * <li>OAI-PMH</li>
 * </ul>
 *
 * @author Fabrizio
 */
public class CommonContext {

    private static JAXBContext jaxbContext;

    static {

	//
	// the file access is required for local schemas while http is required in particular
	// for the authzforce XACML data model
	//
	System.setProperty("javax.xml.accessExternalSchema", "all");

	// ---------------------------------------------------------------------------------------
	//
	// init the common schemas
	//
	new CommonSchemas();

	try {

	    jaxbContext = JAXBContext.newInstance(//
		    eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory.class, // CSW core
		    eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.ObjectFactory.class, // Dublin Core elements
		    eu.essi_lab.jaxb.org.purl.dc.terms.ObjectFactory.class, // Dublin Core terms
		    net.opengis.gml.v_3_2_0.ObjectFactory.class, // GML 3.2.0

		    ObjectFactories.GCO().getClass(), //
		    ObjectFactories.GMD().getClass(), //
		    ObjectFactories.GMX().getClass(), //
		    ObjectFactories.GSR().getClass(), //
		    ObjectFactories.GSS().getClass(), //
		    ObjectFactories.GTS().getClass(), //
		    ObjectFactories.SRV().getClass(), //
		    ObjectFactories.GMI().getClass(), //
		    ObjectFactories.PUB_SUB().getClass(), //

		    ObjectFactories.XACML().getClass(), // authzforce XACML 3.0

		    OAIPMHtype.class // OAI-PMH
	    );
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(CommonContext.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(CommonContext.class).error(e.getMessage(), e);
	}
    }

    /**
     * A marhsaller created from the common context, with the following properties set:
     * <ul>
     * <li>Marshaller.JAXB_FORMATTED_OUTPUT -> true</li>
     * <li>Marshaller.JAXB_FRAGMENT -> <code>omitXMLdeclaration</code></li>
     * <li>"org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper" -> <code>new CommonNameSpaceContext()</code></li>
     * </ul>
     *
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     */
    public static Marshaller createMarshaller(boolean omitXMLdeclaration) throws JAXBException {

	return createMarshaller(omitXMLdeclaration, new CommonNameSpaceContext());
    }

    /**
     * A marhsaller created from the common context, with the following properties set:
     * <ul>
     * <li>Marshaller.JAXB_FORMATTED_OUTPUT -> true</li>
     * <li>Marshaller.JAXB_FRAGMENT -> <code>omitXMLdeclaration</code></li>
     * <li>"org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper" -> <code>new CommonNameSpaceContext()</code></li>
     * <li>Marshaller.JAXB_SCHEMA_LOCATION -> schemaLocation</li>
     * </ul>
     *
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     */
    public static Marshaller createMarshaller(boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper, String schemaLocation)
	    throws JAXBException {

	Marshaller marshaller = jaxbContext.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitXMLdeclaration);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, prefixMapper);
	if (schemaLocation != null) {
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
	}

	return marshaller;
    }

    /**
     * A marhsaller created from the common context, with the following properties set:
     * <ul>
     * <li>Marshaller.JAXB_FORMATTED_OUTPUT -> true</li>
     * <li>Marshaller.JAXB_FRAGMENT -> <code>omitXMLdeclaration</code></li>
     * <li>"org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper" -> <code>new CommonNameSpaceContext()</code></li>
     * </ul>
     *
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     */
    public static Marshaller createMarshaller(boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper) throws JAXBException {

	Marshaller marshaller = createMarshaller(omitXMLdeclaration, prefixMapper, null);
	return marshaller;
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Object object, boolean omitXMLdeclaration) throws JAXBException, UnsupportedEncodingException {

	return asString(object, omitXMLdeclaration, new CommonNameSpaceContext());
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @param prefixMapper
     * @param schemaLocation
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Object object, boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper, String schemaLocation)
	    throws JAXBException, UnsupportedEncodingException {

	return asOutputStream(object, omitXMLdeclaration, prefixMapper, schemaLocation).toString("UTF-8").trim();
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @param prefixMapper
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Object object, boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper)
	    throws JAXBException, UnsupportedEncodingException {

	return asString(object, omitXMLdeclaration, prefixMapper, null);
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @param prefixMapper
     * @param schemaLocation
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static ByteArrayOutputStream asOutputStream(Object object, boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper,
	    String schemaLocation) throws JAXBException, UnsupportedEncodingException {

	Marshaller marshaller = createMarshaller(omitXMLdeclaration, prefixMapper, schemaLocation);
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	marshaller.marshal(object, outputStream);
	return outputStream;
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @param prefixMapper
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static ByteArrayOutputStream asOutputStream(Object object, boolean omitXMLdeclaration, NamespacePrefixMapper prefixMapper)
	    throws JAXBException, UnsupportedEncodingException {

	return asOutputStream(object, omitXMLdeclaration, prefixMapper, null);
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static ByteArrayOutputStream asOutputStream(Object object, boolean omitXMLdeclaration)
	    throws JAXBException, UnsupportedEncodingException {

	return asOutputStream(object, omitXMLdeclaration, new CommonNameSpaceContext());
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @return
     * @throws JAXBException
     * @throws ParserConfigurationException
     * @throws UnsupportedEncodingException
     */
    public static Document asDocument(Object object, boolean omitXMLdeclaration) throws JAXBException, ParserConfigurationException {

	Marshaller marshaller = createMarshaller(omitXMLdeclaration);
	DocumentBuilderFactory builderFactory = XMLFactories.newDocumentBuilderFactory();
	builderFactory.setNamespaceAware(true);
	DocumentBuilder builder = builderFactory.newDocumentBuilder();
	Document document = builder.newDocument();
	marshaller.marshal(object, document);
	return document;
    }

    /**
     * @param object
     * @param omitXMLdeclaration
     * @return
     * @throws UnsupportedEncodingException
     * @throws JAXBException
     */
    public static ByteArrayInputStream asInputStream(Object object, boolean omitXMLdeclaration)
	    throws UnsupportedEncodingException, JAXBException {

	return new ByteArrayInputStream(asOutputStream(object, omitXMLdeclaration).toByteArray());
    }

    /**
     * @param string
     * @param type
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static <T> T unmarshal(String string, Class<T> type) throws JAXBException, UnsupportedEncodingException {

	Unmarshaller unmarshaller = createUnmarshaller();
	Object unmarshal = unmarshaller.unmarshal(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));

	return doCast(type, unmarshal);
    }

    /**
     * @param reader
     * @param type
     * @return
     * @throws JAXBException
     */
    public static <T> T unmarshal(Reader reader, Class<T> type) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();
	Object unmarshal = unmarshaller.unmarshal(reader);

	return doCast(type, unmarshal);
    }

    /**
     * @param stream
     * @param type
     * @return
     * @throws JAXBException
     */
    public static <T> T unmarshal(InputStream stream, Class<T> type) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();
	Object unmarshal = unmarshaller.unmarshal(stream);

	return doCast(type, unmarshal);

    }

    /**
     * @param stream
     * @param type
     * @return
     * @throws JAXBException
     */
    public static <T> T unmarshal(File file, Class<T> type) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();
	Object unmarshal = unmarshaller.unmarshal(file);

	return doCast(type, unmarshal);

    }

    private static <T> T doCast(Class<T> type, Object unmarshal) throws JAXBException {

	if (type.isAssignableFrom(unmarshal.getClass()))
	    return type.cast(unmarshal);

	throw new JAXBException("Requested class " + type.getName() + " is not assignable from " + unmarshal.getClass());

    }

    /**
     * @param node
     * @param type
     * @return
     * @throws JAXBException
     */
    public static <T> T unmarshal(Node node, Class<T> type) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();
	Object unmarshal = unmarshaller.unmarshal(node);

	return doCast(type, unmarshal);
    }

    /**
     * @return
     * @throws JAXBException
     */
    public static Unmarshaller createUnmarshaller() throws JAXBException {

	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	return unmarshaller;
    }
}
